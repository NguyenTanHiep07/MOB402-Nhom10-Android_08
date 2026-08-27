# API Contract v1

Base URL local: `http://localhost:8080/api`

Trừ endpoint đăng nhập, mọi request phải gửi:

```http
Authorization: Bearer <accessToken>
Content-Type: application/json
```

## Authentication

### `POST /auth/login`

```json
{
  "username": "shipper2",
  "password": "123456"
}
```

Response chứa `accessToken`, `expiresInMs` và thông tin user. Role luôn được xác định từ token/server, không nhận role hoặc userId do client tự gửi.

## Orders dùng chung

| Method | Endpoint | Role | Ý nghĩa |
|---|---|---|---|
| POST | `/orders` | CLIENT | Tạo đơn cho client hiện tại |
| GET | `/orders` | CLIENT/DELIVERY/ADMIN | Danh sách theo phạm vi quyền hiện tại |
| GET | `/orders/{id}` | Có quyền xem đơn | Chi tiết đơn |
| GET | `/orders/{id}/history` | Có quyền xem đơn | Lịch sử trạng thái |
| POST | `/orders/{id}/cancel` | CLIENT chủ đơn | Hủy khi `CHO_TIEP_NHAN` hoặc `DA_CHAP_NHAN` |

Body tạo đơn:

```json
{
  "pickupAddress": "123 Nguyễn Văn A, Quận 1",
  "deliveryAddress": "456 Lê Văn B, Quận 3",
  "pickupLatitude": 10.7768890,
  "pickupLongitude": 106.7008060,
  "deliveryLatitude": 10.7826810,
  "deliveryLongitude": 106.6957540,
  "senderName": "Nhà hàng ABC",
  "senderPhone": "0901234567",
  "recipientName": "Nguyễn Văn B",
  "recipientPhone": "0987654321",
  "distanceKm": 2.5,
  "packages": [
    {
      "name": "Cơm gà",
      "packageType": "FOOD",
      "weightKg": 0.5,
      "quantity": 1,
      "notes": "Nhiều cơm",
      "fragile": false,
      "express": false
    }
  ],
  "scheduledPickupTime": null,
  "note": null
}
```

Phí server tính theo công thức demo thống nhất với Android hiện tại: `15.000 + 5.000/km + 3.000/kg + 5.000 nếu dễ vỡ + 10.000 nếu hỏa tốc`.

Bốn trường tọa độ là bắt buộc. API trả lại các trường này trong `OrderResponse` để Shipper hiển thị
marker pickup/delivery và mở Google Maps chỉ đường.

## Delivery

| Method | Endpoint | Ý nghĩa |
|---|---|---|
| GET | `/driver/orders/open` | Đơn chờ chưa Reject bởi tài xế hiện tại |
| GET | `/driver/orders/mine` | Đơn tài xế đang/đã phụ trách |
| POST | `/driver/orders/{id}/accept` | Nhận đơn atomic |
| POST | `/driver/orders/{id}/reject` | Từ chối nhưng không xóa khỏi Open Pool chung |
| PATCH | `/driver/orders/{id}/status` | Cập nhật trạng thái đúng chuỗi |
| GET | `/driver/rejection-reasons` | Danh sách reason từ backend |
| GET | `/driver/statistics/me` | Reliability Score và trạng thái khóa |
| PATCH | `/driver/availability` | `AVAILABLE`, `BUSY`, `OFFLINE` |

Body Reject:

```json
{
  "reasonCode": "VEHICLE_ISSUE",
  "note": "Xe bị thủng lốp"
}
```

Body cập nhật trạng thái:

```json
{
  "status": "DA_DEN_NHA_HANG",
  "note": "Đã đến điểm lấy hàng"
}
```

Chuỗi hợp lệ:

```text
CHO_TIEP_NHAN
  -> DA_CHAP_NHAN
  -> DA_DEN_NHA_HANG
  -> DA_LAY_HANG
  -> DA_DEN_KHACH_HANG
  -> DA_GIAO
```

`DA_GIAO` và `DA_HUY` là trạng thái kết thúc. Chỉ tài xế được gán đơn mới được cập nhật trạng thái.

## Admin

| Method | Endpoint | Ý nghĩa |
|---|---|---|
| GET | `/admin/users` | Danh sách tài khoản |
| GET | `/admin/drivers` | Tài xế và thống kê |
| GET | `/admin/drivers/alerts` | Tài xế có Reliability Score dưới 70 |
| GET | `/admin/orders` | Toàn bộ đơn |

## Mã lỗi quan trọng

| HTTP | Code | Trường hợp |
|---|---|---|
| 400 | `VALIDATION_ERROR` | Body sai hoặc thiếu field |
| 401 | `UNAUTHORIZED` | Thiếu token, token sai hoặc token hết hạn |
| 401 | `INVALID_CREDENTIALS` | Sai tài khoản/mật khẩu |
| 403 | `NOT_ORDER_OWNER`, `NOT_ASSIGNED_DRIVER` | Sai quyền/sai chủ đơn |
| 404 | `ORDER_NOT_FOUND` | Không tồn tại đơn |
| 409 | `ORDER_ALREADY_TAKEN` | Tài xế khác đã nhận trước |
| 409 | `INVALID_STATUS_TRANSITION` | Cập nhật sai chuỗi trạng thái |
| 409 | `ORDER_ALREADY_REJECTED` | Tài xế đã Reject đơn này |
| 423 | `DRIVER_TEMPORARILY_LOCKED` | Bị giới hạn nhận đơn tạm thời |

Response lỗi có cấu trúc thống nhất: `timestamp`, `status`, `code`, `message`, `path`, `fields`.
