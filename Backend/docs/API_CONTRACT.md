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

`distanceKm` được giữ lại để tương thích DTO Android đang được phát triển, nhưng server **không tin giá trị này**.
Khi tạo đơn, server tính lại quãng đường chạy xe từ bốn tọa độ rồi lưu khoảng cách và phí do server xác nhận.
Nhờ vậy client không thể gửi `1 km` cho một tuyến thực tế dài `10 km` để giảm phí.

Phí server tính theo công thức demo: `15.000 + 5.000/km + 3.000/kg + 5.000 nếu dễ vỡ + 10.000 nếu hỏa tốc`.

Bốn trường tọa độ là bắt buộc. API trả lại các trường này trong `OrderResponse` để Shipper hiển thị
marker pickup/delivery và mở Google Maps chỉ đường.

## Địa chỉ thật và ước lượng tuyến đường

Hai endpoint này thuộc Backend/API Contract. Android network layer chỉ cần khai báo DTO và gọi API;
không gọi trực tiếp Photon hoặc OSRM từ ứng dụng Android.

### `GET /locations/autocomplete`

Chỉ dành cho role `CLIENT`.

```http
GET /api/locations/autocomplete?query=Nguyen%20Trai&limit=6
Authorization: Bearer <clientToken>
```

- `query`: tối thiểu 3 ký tự.
- `limit`: mặc định 6, server giới hạn tối đa 8.
- Android nên debounce khoảng 400-500 ms và hủy request cũ khi người dùng nhập tiếp.
- Kết quả được giới hạn trong phạm vi Việt Nam.

Response:

```json
[
  {
    "placeId": "W:189067626",
    "formattedAddress": "314 Đường Nguyễn Trãi, Khu phố 18, An Đông, Thành phố Hồ Chí Minh, 72760, Việt Nam",
    "primaryText": "314 Đường Nguyễn Trãi",
    "secondaryText": "Khu phố 18, An Đông, Thành phố Hồ Chí Minh, 72760",
    "ward": "Khu phố 18",
    "district": "An Đông",
    "province": "Thành phố Hồ Chí Minh",
    "country": "Việt Nam",
    "latitude": 10.7568827,
    "longitude": 106.6750459
  }
]
```

Android phải lưu `formattedAddress`, `latitude`, `longitude` của item người dùng đã chọn. Không xem chuỗi
người dùng tự gõ nhưng chưa chọn suggestion là một địa chỉ đã xác thực.

### `POST /routes/estimate`

Chỉ dành cho role `CLIENT`. Endpoint trả về quãng đường chạy xe, thời gian dự kiến và phí tạm tính.

```json
{
  "pickup": {
    "latitude": 10.7700000,
    "longitude": 106.6800000
  },
  "delivery": {
    "latitude": 10.8000000,
    "longitude": 106.7100000
  },
  "totalWeightKg": 2.5,
  "fragile": true,
  "express": false
}
```

Response:

```json
{
  "distanceKm": 5.41,
  "estimatedDurationMinutes": 8,
  "baseFee": 15000.00,
  "distanceFee": 27050.00,
  "weightFee": 7500.00,
  "serviceFee": 5000.00,
  "totalFee": 54550.00
}
```

Giá trị ở endpoint này dùng để preview. `POST /orders` vẫn tính lại quãng đường và phí lần cuối trước khi lưu.
Tọa độ ngoài Việt Nam hoặc pickup trùng delivery sẽ bị từ chối.

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
| 400 | `ADDRESS_QUERY_TOO_SHORT` | Chuỗi tìm địa chỉ dưới 3 ký tự |
| 400 | `LOCATION_OUTSIDE_VIETNAM` | Tọa độ nằm ngoài phạm vi phục vụ demo |
| 422 | `ROUTE_NOT_FOUND`, `ROUTE_POINTS_IDENTICAL` | Không tìm được tuyến hoặc hai điểm trùng nhau |
| 503 | `LOCATION_PROVIDER_UNAVAILABLE`, `ROUTE_PROVIDER_UNAVAILABLE` | Provider bản đồ tạm thời không khả dụng |
| 504 | `LOCATION_PROVIDER_TIMEOUT`, `ROUTE_PROVIDER_TIMEOUT` | Provider bản đồ quá thời gian phản hồi |

Response lỗi có cấu trúc thống nhất: `timestamp`, `status`, `code`, `message`, `path`, `fields`.
