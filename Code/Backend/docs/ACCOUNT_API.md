# API hồ sơ và email bảo mật

Base: `/api`. Những API `/account*` dùng Bearer token; ID người dùng lấy từ token đã xác thực, không nhận ID tài khoản tùy ý trong body.

| Method/path | Body | Kết quả |
| --- | --- | --- |
| GET `/account` | — | Hồ sơ của chính người dùng: id, username, fullName, phoneNumber, role, licensePlate, email, emailVerified, avatarBase64 |
| PUT `/account` | username, fullName, phoneNumber, currentPassword, avatarBase64 | Hồ sơ đã lưu; avatarBase64 null để xóa, gửi giá trị hiện tại nếu giữ ảnh |
| POST `/account/email/request` | email, currentPassword | Tiếp nhận yêu cầu xác minh email, không trả OTP |
| GET `/account/email/status` | — | Thông báo QUEUED/SENT/FAILED/kết thúc cho yêu cầu liên kết gần nhất |
| POST `/account/email/verify` | code, currentPassword | Hồ sơ với email đã xác minh |
| POST `/auth/recovery/request` | phoneNumber | Thông báo chung, kể cả tài khoản không tồn tại hoặc chưa có email |
| POST `/auth/recovery/complete` | phoneNumber, code, newPassword | Đổi mật khẩu và vô hiệu hóa toàn bộ token cũ |

OTP 6 chữ số, 10 phút, dùng một lần, tối đa 5 lần đoán. Gửi lại hủy mã trước. LINK và RESET là hai mục đích tách biệt. Mã RESET ràng buộc với email đã xác minh, số điện thoại và phiên bản mật khẩu; đổi thông tin liên quan làm mã cũ không dùng được. Cập nhật có khóa hàng/transaction để xử lý đồng thời.

Giới hạn lưu DB: gửi RESET 3/giờ/số điện thoại, tối thiểu 60 giây giữa hai lần, 20/giờ/IP; xác nhận RESET 60/giờ/IP. Liên kết email 3/giờ/tài khoản, tối thiểu 60 giây. Xác thực lại 20/giờ/tài khoản, sửa hồ sơ 20/giờ/tài khoản. Mã sai vẫn tăng bộ đếm. `429` khi chạm giới hạn, `503 EMAIL_UNAVAILABLE` khi chưa cấu hình email, `400 ACCOUNT_INVALID` cho dữ liệu hoặc mã không hợp lệ. Số điện thoại chuẩn hóa +84 thành 0, chấp nhận 10–11 số để tương thích dữ liệu demo.

Ảnh tối đa 160 KB sau giải mã, chiều không quá 1024 px; backend giải mã và mã hóa lại JPEG. Ảnh lưu trong PostgreSQL cùng hồ sơ, không đưa ảnh/email vào danh sách đơn hàng hay danh sách công khai. Android thu nhỏ ảnh xuống tối đa 384 px trước khi tải lên.

Email dùng SMTP qua Spring Mail, tên hiển thị `GoDrop | Bảo mật tài khoản`. Hàng chờ lưu mã AES-GCM, xóa payload khi gửi/lỗi/hết hạn, chỉ giữ HMAC để xác nhận. Không ghi OTP, mật khẩu hoặc địa chỉ nhận vào log lỗi gửi thư. SMTP tiếp nhận không đồng nghĩa đảm bảo vào Inbox. Khi gửi lỗi phải yêu cầu mã mới. Mặc định tắt email đến khi cấu hình `.env`.

Xem [hướng dẫn cấu hình và demo](EMAIL_ACCOUNT_GUIDE.md).
