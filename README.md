# 🔍 Eureka Server (服務註冊與整合中心)

> 本模組為中醫資訊系統（TCM Microservice System）的**中心化服務總線與註冊中心**，基於 Spring Cloud Netflix Eureka 建置。負責全系統微服務實體的動態註冊、健康狀態監控與服務發現，並作為外部/既有業務模組無縫接入系統的核心橋樑。

---

## 🛠️ 核心功能與系統設計 (Key Features & Design)

* **動態服務註冊與發現 ：** 後端各微服務（如 `api-gateway`、`tcm-core-service`、`medicinal` 等）啟動時會自動向 Eureka Server 註冊其網路位址（IP 與 Port），達成服務間解耦。
* **中心化整合橋樑 ：** 方便既有模組或第三方服務進行微服務網格接入，無需硬編碼（Hardcode）任何服務位址。
* **心跳機制與健康檢查 ：** 定期接收各服務節點發送的心跳封包，自動剔除失效或斷線的服務實體，確保系統整體高可用性。
* **視覺化儀表板 ：** 提供網頁監控介面，可即時查看當前已註冊的所有服務清單、實體數量與系統運作狀態。

---

## 🛠️ 技術棧 (Tech Stack)

* **Java Version:** 17
* **Framework:** Spring Boot, Spring Cloud Netflix Eureka Server
* **Build Tool:** Maven

---
