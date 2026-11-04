# 🐳 Docker & Cloud Deployment (容器化與 AWS EC2 雲端部署)

> 本分支專門維護中醫資訊系統（TCM Microservice System）的 **Docker 容器化配置、Docker Compose 服務編排與 AWS EC2 雲端部署腳本**。透過環境隔離與手動部署，確保系統在本地開發環境與 AWS 雲端伺服器上具備一致的執行環境。

---

## 🛠️ 核心配置與部署特色 (Deployment Highlights)

* **一鍵式多服務編排 (Multi-Container Orchestration)：** 使用 `docker-compose.yml` 統籌管理 API Gateway、Eureka Server、TCM Core Service、Medicinal Service 及 MySQL 資料庫等容器之啟動順序與網路相依性。
* **AWS EC2 雲端架設 (AWS Cloud Hosting)：** 將全套 Docker 容器化服務部署於 **AWS EC2 (Linux/Ubuntu)** 虛擬伺服器，配置安全組（Security Groups）與 Port 映射，實現高可靠性的雲端服務運作。
* **環境變數與安全隔離：** 透過 `.env` 檔案管理資料庫密碼與系統配置，避免敏感資訊硬編碼於原始碼中。
* **Nginx 反向代理與網域路由：** 配合 Nginx 作為 EC2 前端反向代理，整合 Let's Encrypt SSL 憑證，提供安全的 HTTPS 連線。

---

## 🛠️ 技術棧 (Tech Stack)

* **Containerization:** Docker, Dockerfile
* **Orchestration:** Docker Compose
* **Cloud Infrastructure:** AWS EC2 (Linux / Ubuntu)
* **Web Server & Reverse Proxy:** Nginx, Let's Encrypt SSL

---

[回到總覽](https://github.com/MasterOuO/microservice-tcm-system/tree/main)
