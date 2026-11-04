# 🌿 Medicinal Service (中藥材與處方管理服務)

> 本模組為中醫資訊系統（TCM Microservice System）的**藥材業務微服務**。負責中藥材配伍、藥性屬性查詢、處方簽紀錄管理以及庫存資料之 RESTful API 提供。

---

## 🛠️ 核心功能與系統設計 (Key Features & Design)

* **藥材與處方資料管理：** 提供中藥材資訊查詢、處方簽資料紀錄與庫存狀態持久化之 API 介面。
* **微服務架構整合：** 作為 Eureka Client 自動註冊至服務中心，並經由 API Gateway 接受統一路由調用，與核心診斷服務（`tcm-core-service`）維持高解耦設計。
* **資料庫持久化：** 結合 Spring Data JPA 與 MySQL 進行藥材資料庫之結構化設計與高效存取。

---

## 🛠️ 技術棧 (Tech Stack)

* **Java Version:** 17
* **Framework:** Spring Boot, Spring Data JPA
* **Service Discovery:** Spring Cloud Netflix Eureka Client
* **Database:** MySQL
* **Build Tool:** Maven

---

[回到總覽](https://github.com/MasterOuO/microservice-tcm-system/tree/main)
