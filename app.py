import os
import tempfile
import traceback
import librosa
from fastapi import FastAPI, File, UploadFile
from fastapi.responses import HTMLResponse

from whisper_service import WhisperService

app = FastAPI()
ai_service = WhisperService()

@app.get("/", response_class=HTMLResponse)
async def main_page():
    return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <title>麥克風即時錄音轉文字</title>
        <style>
            body { font-family: Arial, sans-serif; margin: 40px; background-color: #f9f9f9; }
            .card { border: 1px solid #ddd; padding: 25px; border-radius: 10px; max-width: 600px; background: #fff; box-shadow: 0 4px 6px rgba(0,0,0,0.1); margin: 0 auto; text-align: center; }
            .btn-group { margin: 20px 0; }
            button { padding: 12px 24px; font-size: 16px; border: none; border-radius: 5px; cursor: pointer; margin: 0 5px; }
            #recordBtn { background-color: #28a745; color: white; }
            #recordBtn.recording { background-color: #dc3545; }
            #stopBtn { background-color: #6c757d; color: white; }
            #stopBtn:disabled { background-color: #ccc; cursor: not-allowed; }
            #status { font-weight: bold; margin-top: 10px; color: #555; }
            #result { margin-top: 20px; padding: 15px; border-radius: 6px; display: none; background: #eef2f5; text-align: left; }
            .text-box { background: white; border: 1px solid #ccc; padding: 12px; border-radius: 5px; margin-top: 10px; font-size: 18px; color: #155724; font-weight: bold; }
        </style>
    </head>
    <body>
        <div class="card">
            <h2> 麥克風即時錄音轉文字</h2>
            <p>點擊「開始錄音」說話，錄音結束後會自動傳送給 AI 進行辨識。</p>
            
            <div class="btn-group">
                <button id="recordBtn" onclick="startRecording()">開始錄音</button>
                <button id="stopBtn" onclick="stopRecording()" disabled>停止並辨識</button>
            </div>
            
            <div id="status">提示：請允許瀏覽器存取麥克風權限</div>
            <audio id="audioPreview" controls style="margin-top: 15px; display: none; width: 100%;"></audio>
            <div id="result"></div>
        </div>

        <script>
            let mediaRecorder = null;
            let audioChunks = [];

            async function startRecording() {
                try {
                    const stream = await navigator.mediaDevices.getUserMedia({ audio: true });
                    
                    // 優先使用 wav/mp4/ogg 等通用相容格式
                    let options = {};
                    if (MediaRecorder.isTypeSupported('audio/wav')) {
                        options = { mimeType: 'audio/wav' };
                    } else if (MediaRecorder.isTypeSupported('audio/mp4')) {
                        options = { mimeType: 'audio/mp4' };
                    }

                    mediaRecorder = new MediaRecorder(stream, options);
                    audioChunks = [];

                    mediaRecorder.ondataavailable = function(event) {
                        if (event.data && event.data.size > 0) {
                            audioChunks.push(event.data);
                        }
                    };

                    mediaRecorder.onstop = async function() {
                        const mimeType = mediaRecorder.mimeType || 'audio/wav';
                        const audioBlob = new Blob(audioChunks, { type: mimeType });
                        
                        const audioUrl = URL.createObjectURL(audioBlob);
                        const audioPreview = document.getElementById('audioPreview');
                        audioPreview.src = audioUrl;
                        audioPreview.style.display = 'block';

                        sendAudioToServer(audioBlob);
                    };

                    mediaRecorder.start();

                    document.getElementById('recordBtn').disabled = true;
                    document.getElementById('recordBtn').classList.add('recording');
                    document.getElementById('recordBtn').innerText = ' 錄音中...';
                    document.getElementById('stopBtn').disabled = false;
                    document.getElementById('status').innerText = '正在錄音中...';
                    document.getElementById('result').style.display = 'none';
                    document.getElementById('audioPreview').style.display = 'none';

                } catch (err) {
                    alert('開啟麥克風失敗：' + err.message);
                }
            }

            function stopRecording() {
                if (mediaRecorder && mediaRecorder.state !== 'inactive') {
                    mediaRecorder.stop();
                    if (mediaRecorder.stream) {
                        mediaRecorder.stream.getTracks().forEach(track => track.stop());
                    }
                    document.getElementById('recordBtn').disabled = false;
                    document.getElementById('recordBtn').classList.remove('recording');
                    document.getElementById('recordBtn').innerText = ' 開始錄音';
                    document.getElementById('stopBtn').disabled = true;
                    document.getElementById('status').innerText = '錄音結束，正在發送給 AI 辨識...';
                }
            }

            async function sendAudioToServer(blob) {
                const resultDiv = document.getElementById('result');
                resultDiv.style.display = 'block';
                resultDiv.innerHTML = ' AI 正在辨識語音中，請稍候...';

                const formData = new FormData();
                formData.append('file', blob, 'recorded_audio.wav');

                try {
                    const response = await fetch('/upload', {
                        method: 'POST',
                        body: formData
                    });
                    const data = await response.json();

                    if (data.status === 'success') {
                        document.getElementById('status').innerText = ' 處理完成！';
                        resultDiv.innerHTML = `
                             <strong>辨識成功！</strong><br><br>
                             <strong>檔案大小：</strong> ${data.size_mb} MB<br><br>
                             <strong>語音辨識結果：</strong>
                            <div class="text-box">${data.text}</div>
                        `;
                    } else {
                        resultDiv.innerHTML = ' 辨識失敗：' + data.message;
                    }
                } catch (err) {
                    resultDiv.innerHTML = ' 連線失敗或伺服器發生錯誤！';
                }
            }
        </script>
    </body>
    </html>
    """

@app.post("/upload")
async def process_audio(file: UploadFile = File(...)):
    try:
        print("\n[LOG] 收到前端傳來的語音檔案！")
        contents = await file.read()
        size_mb = round(len(contents) / (1024 * 1024), 2)
        print(f"[LOG] 檔案大小: {size_mb} MB")

        print("[LOG] 正在丟給 Whisper 模型推論中...")
        transcribed_text = ai_service.transcribe_mp3(contents)
        print(f"[LOG] 辨識成功！結果：{transcribed_text}\n")

        return {
            "status": "success",
            "size_mb": size_mb,
            "text": transcribed_text,
        }
    except Exception as e:
        print("\n[LOG] 後端處理發生錯誤：")
        traceback.print_exc()
        print("\n")
        return {"status": "error", "message": str(e)}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="127.0.0.1", port=8011)