# 🏥 中醫資訊系統 (TCM Microservice System)

> 本專案為一套轉型為微服務架構（Spring Cloud）並加入語音轉文字服務（Python Whisper ASR）的中醫醫療資訊管理系統。採用高解耦的服務拆分設計，支援 Docker 容器化部署與靈活的服務擴充。

本專案著重於**微服務架構轉型合、系統設計（System Design）與雲端容器化部署**之實作展示：
* **核心架構與部署（個人獨立負責）：** 獨立完成 Spring Cloud 微服務拆分、API Gateway 路由整合、 AWS EC2 雲端環境架設與 Docker Compose 自動化部署，並協助各微服務程式開發。
* **業務模組整合：** 部分基礎業務模組為團隊/既有系統提供之介面整合，以確保整體系統之完整執行鏈結。
---

## 📐 系統架構 (Architecture)

本系統採微服務架構設計，經由統一入口進行動態路由與服務發現：

* **微服務基礎設施 (Infrastructure Services)**
  
  ├── **[Eureka Server](https://github.com/MasterOuO/microservice-tcm-system/tree/eureka-server)**：服務註冊與發現中心，維護所有微服務實體之健康狀態
  
  └── **[API Gateway](https://github.com/MasterOuO/microservice-tcm-system/tree/api-gateway)**：系統統一入口，負責 Request 路由轉發與全域驗證
* **後端微服務群 (Backend Services)**

  ├── **[Differentination Service](https://github.com/MasterOuO/microservice-tcm-system/tree/differentiation)**：處理中醫門診斷核心業務，判斷病人的症狀

  ├── **[Medicinal Service](https://github.com/MasterOuO/microservice-tcm-system/tree/medicinal)**：處理中藥材庫存、藥性與處方管理
  
  └── **[Whisper ASR Service](https://github.com/MasterOuO/microservice-tcm-system/tree/python-whisper)**：處理語音處方轉譯文字服務
* **前端使用者介面 (Frontend UI)**
  
  └── **[Web UI](https://github.com/MasterOuO/microservice-tcm-system/tree/admin-ui)**：提供醫師與行政人員操作之使用者介面

* **容器化環境與部署 (Containerization)**
  └── **[Docker / Docker Compose](https://github.com/MasterOuO/microservice-tcm-system/tree/docker)**：全系統服務打包與環境變數配置（請參閱 [docker 分支](https://github.com/MasterOuO/microservice-tcm-system/tree/docker)）
---

## 🛠️ 技術棧 (Tech Stack)

| 類別 | 技術 / 工具 | 說明 |
| :--- | :--- | :--- |
| **後端核心** | Java 17, Spring Boot | 微服務基礎架構與 API 開發 |
| **微服務組件** | Spring Cloud Gateway, Eureka | 服務註冊、發現與動態路由 |
| **資料庫與存取**| MySQL, Spring Data JPA | 關聯式資料庫設計、病歷資料與藥材庫存持久化 |
| **AI / ASR** | Python, OpenAI Whisper | 醫師診斷口述自動轉文字服務 |
| **雲端與系統環境**| AWS EC2, Linux (Ubuntu) | 雲端伺服器架設與 Linux 系統環境維護 |
| **容器化與部署**| Docker, Docker Compose | 服務打包與本地/雲端一鍵部署 |
| **反向代理** | Nginx | SSL 憑證與網域映射對外服務 |

---


