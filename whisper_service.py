import io
import os
import re
import subprocess
import tempfile
import imageio_ffmpeg
import librosa
import torch
import opencc
from transformers import WhisperForConditionalGeneration, WhisperProcessor
from peft import PeftModel

_CC = opencc.OpenCC('s2twp')  # Simplified → Traditional (Taiwan phrases)

# ── 全形轉半形 ─────────────────────────────────────────────────────────────────

def _full2half(text: str) -> str:
    """
    全形 ASCII (U+FF01–U+FF5E) → 半形 (U+0021–U+007E)
    全形空格 (U+3000) → 半形空格
    """
    result = []
    for ch in text:
        cp = ord(ch)
        if cp == 0x3000:           # 　→ 空格
            result.append(' ')
        elif 0xFF01 <= cp <= 0xFF5E:  # ！…～、全形英數標點
            result.append(chr(cp - 0xFEE0))
        else:
            result.append(ch)
    return ''.join(result)

# ── 數字轉中文 ─────────────────────────────────────────────────────────────────

_ZH_DIGITS = '零一二三四五六七八九'

def _section_zh(n: int) -> str:
    """將 0–9,999 轉為中文（內部使用）。"""
    parts, need_zero = [], False
    for char, w in [('千', 1000), ('百', 100), ('十', 10), ('', 1)]:
        d, n = divmod(n, w)
        if d:
            if need_zero:
                parts.append('零')
            parts.append(_ZH_DIGITS[d] + char)
            need_zero = False
        elif parts:
            need_zero = True
    return ''.join(parts)

def _int_zh(n: int) -> str:
    """將非負整數轉為中文數字。"""
    if n == 0:
        return '零'
    if n < 10_000:
        return _section_zh(n)
    if n < 100_000_000:
        wan, rest = divmod(n, 10_000)
        if rest == 0:
            return _section_zh(wan) + '萬'
        if rest < 1_000:
            return _section_zh(wan) + '萬零' + _section_zh(rest)
        return _section_zh(wan) + '萬' + _section_zh(rest)
    yi, rest = divmod(n, 100_000_000)
    if rest == 0:
        return _int_zh(yi) + '億'
    if rest < 10_000_000:
        return _int_zh(yi) + '億零' + _int_zh(rest)
    return _int_zh(yi) + '億' + _int_zh(rest)

def _num_to_zh(m: re.Match) -> str:
    s = m.group()
    if '.' in s:
        left, right = s.split('.', 1)
        int_zh = _int_zh(int(left)) if left else '零'
        dec_zh = ''.join(_ZH_DIGITS[int(c)] for c in right)
        return int_zh + '點' + dec_zh
    return _int_zh(int(s))

# ── 標點正則 ───────────────────────────────────────────────────────────────────

# 全形轉半形後，，！？；：已變為半形，因此兩者都納入
_PUNCT_RE = re.compile(
    r'[。、…—～·「」『』【】《》〈〉'
    r'.,!?;"\'`@#$%^&*+=<>~_|/()\[\]{}\-\\]'
)
_TRAILING_RE = re.compile(
    r'[。、…—～·「」『』【】《》〈〉'
    r'.,!?;"\'`@#$%^&*+=<>~_|/()\[\]{}\-\\\s]+'
    r'$'
)

# ── 主函式 ─────────────────────────────────────────────────────────────────────

def normalize_text(text: str) -> str:
    # 規則 0a：簡體 → 繁體（臺灣用詞）
    text = _CC.convert(text)

    # 規則 0b：全形 → 半形（含英數、標點、空格）
    #          ：(U+FF1A) 在此一併轉為 :，後續無需特別處理全形冒號
    text = _full2half(text)

    # 規則 5：'兩' → '2'（接著在規則 3 統一轉為「二」）
    text = text.replace('兩', '2')

    # 規則 4+3：先匹配小數再匹配整數，統一轉中文
    text = re.sub(r'\d+\.\d+', _num_to_zh, text)
    text = re.sub(r'\d+', _num_to_zh, text)

    # 規則 2：英文轉大寫
    text = text.upper()

    # 規則 1b：句尾標點移除
    text = _TRAILING_RE.sub('', text)

    # 規則 1c：其餘標點轉空格（冒號 ':' 不在此 pattern，得以保留）
    text = _PUNCT_RE.sub(' ', text)

    # 整理多餘空白
    text = re.sub(r'\s+', '', text).strip()

    return text

class WhisperService:

  def __init__(self, model_path="./tcmWhisper7"):
    print("正在載入 Whisper 模型...")


    # OpenAI 官方微型模型 
    self.model = WhisperForConditionalGeneration.from_pretrained(
        "openai/whisper-tiny", device_map="auto"
    )

    # 切換微調模型
    # base_model = WhisperForConditionalGeneration.from_pretrained(
    #     "openai/whisper-tiny",
    #     device_map="auto"
    # )
    # self.model = PeftModel.from_pretrained(base_model, model_path)

    self.model.eval()

    self.processor = WhisperProcessor.from_pretrained(
        "openai/whisper-tiny", language="zh", task="transcribe"
    )
    self.model.config.forced_decoder_ids = (
        self.processor.get_decoder_prompt_ids(language="zh", task="transcribe")
    )
    self.model.config.suppress_tokens = []

    # 取得 imageio_ffmpeg 的 ffmpeg 執行檔路徑
    self.ffmpeg_exe = imageio_ffmpeg.get_ffmpeg_exe()
    print("模型與 FFmpeg 解碼器載入完畢！")

  def transcribe_mp3(self, audio_bytes: bytes) -> str:
    """給網頁介面呼叫：接收任何格式的語音二進位內容，轉碼後推論出文字"""
    in_path = None
    out_path = None
    try:
      # 1. 將輸入寫入暫存檔 (WebM/Ogg/MP3...)
      with tempfile.NamedTemporaryFile(delete=False, suffix=".raw") as tmp_in:
        tmp_in.write(audio_bytes)
        in_path = tmp_in.name

      # 2. 建立轉碼輸出的 .wav 暫存檔
      with tempfile.NamedTemporaryFile(delete=False, suffix=".wav") as tmp_out:
        out_path = tmp_out.name

      # 3. 使用 ffmpeg 轉成 16kHz 單聲道的真實 WAV 檔
      cmd = [
          self.ffmpeg_exe,
          "-y",
          "-i",
          in_path,
          "-ar",
          "16000",
          "-ac",
          "1",
          out_path,
      ]
      subprocess.run(
          cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, check=True
      )

      # 4. 讀取標準 16kHz WAV 檔
      audio_data, sr = librosa.load(out_path, sr=16000)

      # 5. 特徵提取
      input_features = self.processor(
          audio_data, sampling_rate=16000, return_tensors="pt"
      ).input_features

      if torch.cuda.is_available():
        input_features = input_features.to("cuda")
        self.model.to("cuda")

      # 6. 模型推論
      with torch.no_grad():
        predicted_ids = self.model.generate(input_features)

      # 7. 解碼成中文字
      
      transcription = self.processor.batch_decode(
          predicted_ids, skip_special_tokens=True
      )[0]

     # 文字正規化 
      final_transcription = normalize_text(transcription)
      return final_transcription

    finally:
      # 8. 清理臨時檔
      for p in [in_path, out_path]:
        if p and os.path.exists(p):
          try:
            os.remove(p)
          except Exception:
            pass