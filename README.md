# 🎙️ Whisper Speech Service (語音轉譯微服務)

> 本模組為中醫資訊系統（TCM Microservice System）的**語音轉文字 (ASR) 介面服務**。基於 Python 與 OpenAI Whisper 轉譯工具包建置，負責接收門診錄音檔並轉譯為文字病歷文本，提供 RESTful API 供系統其他微服務調用。

---

## 🛠️ 核心功能與系統設計 (Key Features & Design)

* **門診語音轉轉譯 API：** 提供 RESTful 介面接收醫師門診錄音檔並進行語音處理交由訓練好的 Whisper 模型轉譯引擎生成相對應的文字病歷文本。
* **跨語言/服務組件對接：** 封裝 Python 語音處理邏輯為獨立服務，並透過 HTTP / API Gateway 與 Java (Spring Cloud) 主架構進行跨語言服務整合。
* **非同步與音訊傳輸處理：** 支援音訊檔案上傳、暫存與轉譯文字結果回傳之標準化 JSON 資料結構封裝。

---

## 🛠️ 技術棧 (Tech Stack)

* **Language:** Python 3.x
* **Core Engine:** OpenAI Whisper
* **Web Framework:** FastAPI 
* **Service Integration:** RESTful API

---

[回到總覽](https://github.com/MasterOuO/microservice-tcm-system/tree/main)
