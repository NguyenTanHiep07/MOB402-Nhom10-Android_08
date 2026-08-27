# Database Contract

Flyway migrations:

- `V1__create_delivery_schema.sql`: tạo schema chính.
- `V2__add_order_coordinates.sql`: thêm tọa độ pickup/delivery và backfill database demo cũ.

| Bảng | Chức năng chính |
|---|---|
| `users` | Tài khoản, role, biển số và trạng thái tài xế |
| `delivery_requests` | Đơn giao hàng, tọa độ pickup/delivery và tài xế được gán |
| `packages` | Các kiện hàng thuộc đơn |
| `status_histories` | Nhật ký chuyển trạng thái |
| `rejection_reasons` | Danh mục lý do và điểm phạt |
| `order_rejections` | Một lần Reject trên mỗi cặp đơn–tài xế |
| `driver_statistics` | Tổng Accept/Reject, Reliability Score và thời hạn khóa |

Các ràng buộc quan trọng:

- `order_rejections(delivery_request_id, driver_id)` là duy nhất.
- Accept dùng update có điều kiện `status = CHO_TIEP_NHAN AND delivery_person_id IS NULL`.
- Khóa ngoại package/history/rejection dùng cascade theo đơn; tài khoản tham chiếu từ đơn không bị xóa cứng.
- Reliability Score nằm trong khoảng 0–100.
- Tiền dùng `NUMERIC`, không dùng số thực dấu phẩy động.
- Thời gian lưu `TIMESTAMPTZ` theo UTC.
- Tọa độ pickup/delivery bắt buộc, latitude trong `[-90, 90]` và longitude trong `[-180, 180]`.
