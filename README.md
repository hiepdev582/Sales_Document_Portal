# 🛡️ Sales Document Portal - Hệ Thống Quản Lý Tài Liệu Phòng Bán Hàng Bảo Mật

Ứng dụng web quản lý, lưu trữ và chia sẻ hợp đồng kinh doanh bảo mật dành cho phòng Bán hàng (Sales Department), được thiết kế tuân thủ các tiêu chuẩn bảo mật nâng cao (**OWASP Top 10 API Security & Web Application Security**).

---

## 📋 Mục Lục

- [1. Đánh Giá & Điểm Bổ Sung Bảo Mật](#1-đánh-giá--điểm-bổ-sung-bảo-mật)
- [2. Kiến Trúc & Tính Năng Bảo Mật Hệ Thống](#2-kiến-trúc--tính-năng-bảo-mật-hệ-thống)
- [3. Công Nghệ Sử Dụng](#3-công-nghệ-sử-dụng)
- [4. Cấu Hình Infra & Docker Compose](#4-cấu-hình-infra--docker-compose)
- [5. Kịch Bản Kiểm Thử Bảo Mật (Security Test Cases)](#5-kịch-bản-kiểm-thử-bảo-mật-security-test-cases)

---

## 1. 🔍 Đánh Giá Kiến Trúc & Điểm Bổ Sung / Điều Chỉnh

Dựa trên kịch bản bài toán và các yêu cầu ban đầu, hệ thống đã bao phủ đầy đủ các nhóm lỗ hổng OWASP quan trọng (BOLA, BFLA, Broken Auth, Hardcoded Secrets, XSS). Để hệ thống hoạt động hoàn hảo và an toàn nhất trong môi trường thực tế, dưới đây là các đánh giá và đề xuất tinh chỉnh:

| Hạng mục                 | Đánh giá hiện tại                                                         | Đề xuất bổ sung / Điều chỉnh tối ưu                                                                                                                                                                                                                                                                                                                                                                             |
| :----------------------- | :------------------------------------------------------------------------ | :-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------- |
| **Authentication**       | Access Token (In-Memory) + Refresh Token (HttpOnly Cookie, Rotation).     | **Access Token Revocation (Blacklist)**: Do Access Token dạng JWT là stateless, khi người dùng Logout, ngoài việc revoke Refresh Token trong Redis, nên thêm `jti` (JWT ID) của Access Token vào Redis Blacklist với TTL bằng đúng thời gian hết hạn còn lại của Access Token để chặn ngay lập tức Access Token bị lạm dụng.                                                                                    |
| **Authorization (ABAC)** | Kiểm tra `owner_id == user.id OR user.department == document.department`. | **Phân loại cấp độ bảo mật (Classification Level)**: Nếu chỉ quy định theo `department`, mọi nhân viên phòng Sales đều xem được toàn bộ hợp đồng Sales của nhau. Đề xuất thêm thuộc tính `classification` (ví dụ: `PUBLIC`, `INTERNAL`, `CONFIDENTIAL`). Nhân viên cùng phòng chỉ xem được tài liệu `INTERNAL`, còn tài liệu `CONFIDENTIAL` bắt buộc chỉ `owner_id` hoặc `Department Manager` mới có quyền xem. |
| **Encryption at Rest**   | Mã hóa file hợp đồng bằng AES-256-GCM trước khi ghi xuống đĩa.            | **Quản lý IV & Auth Tag**: Mã hóa AES-GCM là mã hóa xác thực (Authenticated Encryption). Mỗi file khi mã hóa **bắt buộc dùng một IV (Initialization Vector - 12 bytes) ngẫu nhiên duy nhất** và lưu trữ kèm IV + Authentication Tag (16 bytes) để phục vụ giải mã và kiểm tra tính toàn vẹn của file.                                                                                                           |
| **Secrets Management**   | Lấy DB Credentials & Master Key từ HashiCorp Vault.                       | **Vault AppRole Authentication**: Sử dụng cơ chế AppRole trong Spring Cloud Vault để ứng dụng tự lấy secret động an toàn khi khởi động (Startup) mà không cần lưu bất kỳ secret thô nào trong mã nguồn/file cấu hình.                                                                                                                                                                                           |
| **Cookie In Dev/Prod**   | Secure Cookie (`SameSite=Strict`, `Secure`, `HttpOnly`).                  | Thuộc tính `Secure` trên Cookie bắt buộc chạy qua HTTPS. Cần thiết lập cấu hình linh hoạt theo Profile: môi trường `dev` chạy HTTP có thể tạm để `Secure=false`, nhưng trên `staging/prod` bắt buộc `Secure=true; SameSite=Strict; HttpOnly`.                                                                                                                                                                   |

---

## 2. 🏗️ Kiến Trúc & Tính Năng Bảo Mật Hệ Thống

### 🔑 A. Authentication & Token Security

- **Cơ chế Dual-Token**:
  - **Access Token (JWT)**: Thời gian sống ngắn (ví dụ: 15 phút), lưu trong In-Memory/State phía Frontend (React/Vue State), tránh lưu ở `localStorage` hoặc `sessionStorage` để phòng chống XSS.
  - **Refresh Token**: Thời gian sống dài (ví dụ: 7 ngày), lưu trong Cookie với đầy đủ flag `HttpOnly`, `SameSite=Strict`, `Secure`.
- **Refresh Token Rotation (RTR)**: Mỗi khi endpoint `/api/auth/refresh` được gọi, backend thu hồi (revoke) Refresh Token cũ và cấp một cặp token mới. Nếu phát hiện lại Refresh Token đã bị thu hồi (Token Reuse Attack), backend lập tức revoke toàn bộ chuỗi session của user đó.
- **Redis Revocation List (Blacklist)**: Dùng Redis lưu danh sách Refresh Token và Access Token ID (`jti`) đã bị hủy khi người dùng bấm Logout hoặc khi Token bị thu hồi.

### 🛡️ B. Authorization (Tránh BFLA & BOLA với ABAC)

- **Chống BFLA (Broken Function Level Authorization)**:
  - Phân quyền vai trò chi tiết trên Backend Service (sử dụng Spring Security `@PreAuthorize("hasRole('ADMIN')")`).
  - Ví dụ: Chỉ tài khoản Admin mới có quyền gọi API xóa tài liệu (`DELETE /api/documents/{id}`). UI ẩn nút xóa chỉ phục vụ trải nghiệm người dùng, Backend mới là nơi bắt buộc kiểm tra.
- **Chống BOLA (Broken Object Level Authorization) kết hợp ABAC**:
  - Ngăn chặn hành vi khai thác thay đổi ID tài liệu trên URL (ví dụ: sửa `/api/documents/101` thành `/api/documents/102`).
  - Thực thi quy tắc ABAC (Attribute-Based Access Control) tại tầng Service:
    $$\text{CanAccess} = (\text{document.owner\_id} == \text{current\_user.id}) \lor (\text{current\_user.department} == \text{document.department} \land \text{document.classification} \neq \text{'CONFIDENTIAL'}) \lor (\text{current\_user.role} == \text{'ADMIN'})$$

### 🔐 C. Mã Hóa & Quản Lý Bí Mật (Cryptography & Secrets)

- **Password Hashing**: Mã hóa mật khẩu người dùng lúc đăng ký/đổi mật khẩu bằng thuật toán **Argon2id** (hoặc `bcrypt` với cost factor $\ge 12$).
- **Data at Rest Encryption**:
  - Nội dung file hợp đồng trước khi lưu xuống đĩa cứng được mã hóa bằng **AES-256-GCM**.
  - Mỗi file sinh ngẫu nhiên một Vector khởi tạo (IV 12-byte) riêng biệt.
  - Master Key dùng để mã hóa file được ứng dụng lấy trực tiếp từ HashiCorp Vault.
- **HashiCorp Vault Integration**:
  - Không lưu Master Key hoặc Database Password trong file `.env` thô.
  - Ứng dụng đọc secret động (Dynamic Secrets) từ HashiCorp Vault thông qua Vault API / Spring Cloud Vault khi khởi động.

### 🌐 D. XSS & CSP Protection

- **Header Content Security Policy (CSP)**:
  ```http
  Content-Security-Policy: default-src 'self'; script-src 'self'; style-src 'self' 'unsafe-inline'; object-src 'none'; frame-ancestors 'none';
  X-Content-Type-Options: nosniff
  X-Frame-Options: DENY
  ```
- **Frontend Output Escaping**: Mọi dữ liệu do người dùng nhập (tên file, tên hiển thị, ghi chú) trước khi render ra giao diện web đều được tự động Escape/Encode HTML để vô hiệu hóa script độc hại (Stored & DOM XSS).

---

## 🛠️ 3. Công Nghệ Sử Dụng

- **Backend Framework**: Java 17 / 21, Spring Boot 3.x, Spring Security, Spring Data JPA, Spring Cloud Vault.
- **Database**: PostgreSQL 16.
- **Cache & Session Revocation**: Redis 7.
- **Secrets Management**: HashiCorp Vault.
- **Mã hóa & Bảo mật**: Argon2id / BCrypt, AES-256-GCM.
- **Containerization**: Docker & Docker Compose.

---

## 🚀 4. Cấu Hình Infra & Docker Compose

File `docker-compose.yml` phục vụ môi trường phát triển (Development):

```yaml
version: "3.8"

services:
  postgres:
    image: postgres:16-alpine
    container_name: sales_portal_db
    environment:
      POSTGRES_DB: sales_db
      POSTGRES_USER: sales_user
      POSTGRES_PASSWORD: sales_password_secret
    ports:
      - "5432:5432"
    volumes:
      - pgdata:/var/lib/postgresql/data

  redis:
    image: redis:7-alpine
    container_name: sales_portal_redis
    ports:
      - "6379:6379"

  vault:
    image: hashicorp/vault:1.15.0
    container_name: sales_portal_vault
    ports:
      - "8200:8200"
    environment:
      VAULT_DEV_ROOT_TOKEN_ID: "root_dev_token"
      VAULT_DEV_LISTEN_ADDRESS: "0.0.0.0:8200"
    cap_add:
      - IPC_LOCK

volumes:
  pgdata:
```

### Lệnh khởi chạy các dịch vụ bổ trợ:

```bash
docker-compose up -d
```

---

## 🧪 5. Kịch Bản Kiểm Thử Bảo Mật (Security Test Cases)

### 🔹 Test Case 1: Kiểm thử BOLA (IDOR) & ABAC

1. Đăng nhập tài khoản **Staff A** (`id: 101`, `department: SALES`).
2. Tải lên một hợp đồng Sales (ID tài liệu: `1001`).
3. Đăng nhập tài khoản **Staff B** (`id: 102`, `department: MARKETING`).
4. Dùng Token của **Staff B** gọi `GET /api/documents/1001`.
5. **Kỳ vọng**: Backend trả về `HTTP 403 Forbidden` do vi phạm điều kiện ABAC (khác phòng ban và không phải owner).

### 🔹 Test Case 2: Kiểm thử BFLA (Function Level Access Control)

1. Đăng nhập tài khoản **Staff A** (Role: `STAFF`).
2. Dùng Postman gửi request `DELETE /api/documents/1001`.
3. **Kỳ vọng**: Backend trả về `HTTP 403 Forbidden` do thiếu đặc quyền Role `ADMIN`.

### 🔹 Test Case 3: Kiểm thử Stored XSS & CSP

1. Tải lên file hoặc tạo người dùng với tên chứa payload XSS: `<img src=x onerror="alert(document.cookie)">`.
2. Truy cập trang danh sách tài liệu trên Frontend.
3. **Kỳ vọng**:
   - Giao diện render tên hiển thị dưới dạng văn bản an toàn (escaped HTML string).
   - Chrome DevTools / Console báo lỗi block thực thi inline script do vị phạm quy tắc **Content Security Policy (CSP)**.
