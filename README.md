# 🚪 API Gateway (微服務統一入口閘道)

> 本模組為中醫資訊系統（TCM Microservice System）的**單一入口閘道**，基於 Spring Cloud Gateway 建置。負責全系統前端請求的統一接收、動態路由轉發、負載平衡與安全性切面處理，將內部微服務架構細節與外部進行解耦與隔離。

---

## 🛠️ 核心功能與系統設計 (Key Features & Design)

* **單一入口與隱密性 (Single Entry Point)：** 統一由 API Gateway 接收所有外部 Request，隱藏後端微服務（如 Differentination、Medicinal）的真實 IP 與 Port，提升系統安全性。
* **動態路由轉發 (Dynamic Routing)：** 結合 Eureka 註冊中心，透過服務名稱進行動態路由尋址與請求轉發，無需硬編碼（Hardcode）下游服務位址。
* **全域過濾與驗證預留 (Global Filters)：** 支援 GatewayFilter / GlobalFilter 切面機制，可於請求進入後端微服務前進行全域 Token 驗證、請求標頭修飾與日誌紀錄。

---

## 🛠️ 技術棧 (Tech Stack)

* **Java Version:** 17
* **Framework:** Spring Boot, Spring Cloud Gateway
* **Build Tool:** Maven

---

[回到總覽](https://github.com/MasterOuO/microservice-tcm-system/tree/main)
