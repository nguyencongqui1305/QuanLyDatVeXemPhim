# HƯỚNG DẪN CHẠY DỰ ÁN UTC CINEMA

## 1. YÊU CẦU HỆ THỐNG
- Java 17+
- Maven 3.8+
- Oracle Database 19c/21c (đang chạy ở localhost:1521)

---

## 2. SETUP ORACLE DATABASE

### Bước 1: Mở SQL*Plus với quyền SYSDBA
```bat
sqlplus / as sysdba
```

### Bước 2: Tạo user và cấp quyền
```sql
CREATE USER QL_RAP_PHIM IDENTIFIED BY "abc123"
    DEFAULT TABLESPACE USERS
    TEMPORARY TABLESPACE TEMP
    QUOTA UNLIMITED ON USERS;

GRANT CONNECT, RESOURCE, CREATE SESSION TO QL_RAP_PHIM;
GRANT UNLIMITED TABLESPACE TO QL_RAP_PHIM;
EXIT;
```

Hoặc chạy file tự động:
```bat
sqlplus / as sysdba @setup_oracle_user.sql
```

### Bước 3: Đăng nhập bằng user mới và chạy schema
```bat
sqlplus QL_RAP_PHIM/abc123@localhost:1521/orcl
```
Sau đó trong SQL*Plus:
```sql
@src/main/resources/schema.sql
@src/main/resources/data.sql
```

---

## 3. CẤU HÌNH KẾT NỐI

Kiểm tra file `src/main/resources/application.yml`:
```yaml
spring:
  datasource:
    url: jdbc:oracle:thin:@//localhost:1521/orcl  # Đổi service name nếu khác
    username: QL_RAP_PHIM
    password: abc123
```

Để kiểm tra service name Oracle đang dùng:
```bat
lsnrctl status
```
Tìm dòng `Service "..." has N instance(s)` - đó là tên service.

---

## 4. CHẠY ỨNG DỤNG

```bat
cd DatVeXemPhim
mvn spring-boot:run
```

Truy cập: **http://localhost:8080**

---

## 5. TÀI KHOẢN DEMO (sau khi import data.sql)

| Vai trò     | Email                   | Mật khẩu |
|-------------|-------------------------|-----------|
| Khách hàng  | an.nguyen@email.com     | 123456    |
| Quản lý     | quanly@utccinema.vn     | 123456    |
| Nhân viên   | banve@utccinema.vn      | 123456    |

---

## 6. CẤU TRÚC DỰ ÁN

```
src/
├── main/
│   ├── java/com/qlrapphim/
│   │   ├── config/         # SecurityConfig, WebConfig
│   │   ├── controller/     # admin/, client/
│   │   ├── dto/            # Request/Response DTOs
│   │   ├── entity/         # 13 JPA Entities
│   │   ├── exception/      # GlobalExceptionHandler
│   │   ├── repository/     # 13 Spring Data Repositories
│   │   ├── security/       # UserPrincipal, CustomUserDetailsService
│   │   └── service/        # Business logic services
│   └── resources/
│       ├── templates/      # Thymeleaf HTML templates
│       │   ├── admin/      # Trang quản trị
│       │   ├── client/     # Trang khách hàng
│       │   ├── error/      # Trang lỗi
│       │   └── layout/     # Layout templates
│       ├── static/         # CSS, JS, Images
│       ├── application.yml
│       ├── schema.sql      # DDL tạo bảng
│       └── data.sql        # Dữ liệu mẫu
```

---

## 7. LUỒNG ĐẶT VÉ

1. `/` → Trang chủ (xem phim đang chiếu)
2. `/phim/{id}` → Chi tiết phim + Chọn suất chiếu
3. `/dat-ve/chon-ghe/{maLich}` → Sơ đồ ghế (cần đăng nhập)
4. `/dat-ve/xac-nhan?maLich=...&maGhes=...` → Xác nhận + Khuyến mãi
5. `/dat-ve/thanh-toan` (POST) → Xử lý thanh toán
6. `/dat-ve/ket-qua/{maDat}` → Kết quả + In vé

---

## 8. XỬ LÝ SỰ CỐ

### Lỗi ORA-01017 (username/password sai):
- Đảm bảo user `QL_RAP_PHIM` đã được tạo trong Oracle
- Kiểm tra mật khẩu trong `application.yml`

### Lỗi ORA-12514 (service không tìm thấy):
- Chạy `lsnrctl status` để xem service name thực
- Cập nhật URL trong `application.yml`

### Lỗi validate schema (ddl-auto=validate):
- Đảm bảo đã chạy `schema.sql` trên Oracle
- Kiểm tra `hibernate.default_schema=QL_RAP_PHIM` trong `application.yml`
