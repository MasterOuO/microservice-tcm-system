# 🩺 TCM Core Service (中醫診斷與病歷核心服務)

> 本模組為中醫資訊系統（TCM Microservice System）的**核心業務微服務**。負責處理中醫臨床診斷資料管理、病歷紀錄存取，並提供 RESTful API 供前端與 Gateway 調用。

---

## 🛠️ 核心功能與系統設計 (Key Features & Design)

* **診斷與病歷資料管理：** 提供診斷紀錄存取與病歷資料持久化之 RESTful API，維持診療資料的完整性。
* **微服務架構整合：** 作為 Eureka Client 自動註冊至服務中心，經由 API Gateway 接受統一路由調用，實現與其他業務模組（如 `medicinal`）的解耦。
* **資料庫存取設計：** 採用 Spring Data JPA / MyBatis 進行關聯式資料庫（MySQL）之 CRUD 操作與資料傳輸物件（DTO）封裝。

---

## 🛠️ 技術棧 (Tech Stack)

* **Java Version:** 17
* **Framework:** Spring Boot, Spring Data JPA 
* **Service Discovery:** Spring Cloud Netflix Eureka Client
* **Database:** MySQL
* **Build Tool:** Maven

---

[回到總覽](https://github.com/MasterOuO/microservice-tcm-system/tree/main)
