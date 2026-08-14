# SF-00 — Authentication and role navigation

```mermaid
flowchart TD
    START([Mở ứng dụng]) --> INIT[Khởi tạo tài khoản mẫu]
    INIT -->|Thành công| LOGIN[Login]
    INIT -. Lỗi / hiện thông báo .-> LOGIN
    LOGIN -->|Thiếu trường hoặc sai tài khoản| ERROR[Hiển thị lỗi]
    ERROR -->|Nhập lại| LOGIN
    LOGIN -->|role = CLIENT| CLIENT[Client Home]
    LOGIN -->|role = DELIVERY| DELIVERY[Delivery Home]
    LOGIN -->|role = ADMIN| ADMIN[Admin Home]
    CLIENT -->|Đăng xuất| LOGOUT[Clear currentUser]
    DELIVERY -->|Đăng xuất| LOGOUT
    ADMIN -->|Đăng xuất| LOGOUT
    LOGOUT --> LOGIN
```

## Mapping source

- `AuthViewModel`: trạng thái khởi tạo, login, logout.
- `destinationFor(Role?)`: ánh xạ role thành destination.
- `DeliveryApp`: render Login hoặc đúng Home.
- `DashboardScaffold`: shell chung của ba Home.
