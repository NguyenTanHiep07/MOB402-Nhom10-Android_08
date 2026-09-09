# GoDrop — Email bảo mật và chỉnh sửa hồ sơ

## Cấu hình Gmail gửi thư (làm một lần)

Tên người gửi: **GoDrop | Bảo mật tài khoản**. Backend Java gửi thư, Android Kotlin gọi API; không cần Gmail API key.

1. Bật **Xác minh 2 bước** trên Google của hộp thư dùng gửi OTP.
2. Mở https://myaccount.google.com/apppasswords, tạo mật khẩu ứng dụng tên `GoDrop Backend`.
3. Mở `Backend/.env`, điền:

```dotenv
MAIL_ENABLED=true
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=diachi_cua_ban@gmail.com
MAIL_PASSWORD=mat_khau_ung_dung_16_ky_tu
MAIL_FROM=diachi_cua_ban@gmail.com
MAIL_AUTH=true
MAIL_TLS=true
```

`MAIL_PASSWORD` là **mật khẩu ứng dụng Google**, không phải mật khẩu đăng nhập Gmail. Backend chấp nhận cả chuỗi có hoặc không có khoảng trắng giữa các nhóm ký tự. `MAIL_FROM` giống `MAIL_USERNAME`; tên hiển thị được backend đặt riêng. `.env` đã được Git bỏ qua, không chia sẻ file hoặc chụp mật khẩu.

4. Dừng backend bằng Ctrl+C tại terminal đang chạy nó và chạy lại từ thư mục Backend:

```sh
../Code/gradlew bootRun
```

Giữ Docker/PostgreSQL hoạt động. Không cần mở Swagger. Nếu Google không hiện Mật khẩu ứng dụng, kiểm tra xác minh 2 bước; tài khoản tổ chức hoặc bảo vệ nâng cao có thể không hỗ trợ. [Hướng dẫn Google](https://support.google.com/accounts/answer/185833?hl=vi).

## Liên kết email trước khi quên mật khẩu

1. Đăng nhập khách hàng hoặc tài xế → **Hồ sơ → Email bảo mật → Liên kết email**.
2. Nhập email bạn mở được và **mật khẩu GoDrop hiện tại**. Không nhập mật khẩu Gmail trong app.
3. Bấm **Gửi mã xác minh email**. Mở hộp thư/thư rác lấy mã 6 số.
4. Nhập mã → **Xác nhận liên kết email**. App hiển thị **Đã xác minh**.

Chỉ sau bước 4, email mới dùng khôi phục mật khẩu. Mỗi email liên kết một tài khoản; demo nhiều tài khoản dùng email khác nhau. Có thể dùng hộp thư gửi làm email nhận cho một tài khoản demo.

Đổi email cũng cần mật khẩu GoDrop và mã gửi tới email mới. Email cũ còn hiệu lực đến khi email mới được xác minh. Không tự gắn hoặc tự đánh dấu xác minh email của dữ liệu mẫu.

## Demo quên mật khẩu — phương án B

1. Đăng xuất → **Quên mật khẩu?**.
2. Nhập số điện thoại đang lưu trong Hồ sơ → **Gửi mã qua email**.
3. Backend tìm tài khoản theo số điện thoại và gửi OTP tới email đã xác minh. Đây là email thật, không phải SMS hay mã hiển thị trong app.
4. Mở email lấy mã, nhập trong app cùng mật khẩu mới và nhập lại mật khẩu.
5. Bấm **Đổi mật khẩu** → về đăng nhập bằng **tên đăng nhập** và mật khẩu mới. Màn đăng nhập vẫn dùng tên đăng nhập.

Mã có hạn 10 phút, dùng một lần, tối đa 5 lần nhập sai. Gửi mã cách nhau ít nhất 60 giây, tối đa 3 lần/giờ cho mỗi số điện thoại hoặc tài khoản liên kết. Mật khẩu mới 12–64 ký tự, có chữ và số, tối đa 72 byte UTF-8. Mọi phiên đăng nhập cũ mất hiệu lực sau khi đổi mật khẩu.

Thông báo công khai không tiết lộ số điện thoại có tài khoản hay không. Chưa liên kết email/mất quyền truy cập email: liên hệ quản trị viên để xác minh bên ngoài, không có nút bỏ qua xác minh. Phần Hồ sơ Admin không thuộc phạm vi chỉnh sửa hồ sơ khách hàng/tài xế này.

## Sửa hồ sơ và ảnh đại diện

**Hồ sơ → Chỉnh sửa hồ sơ**: sửa họ tên, tên đăng nhập, số điện thoại, chọn/xóa ảnh. Nhập mật khẩu GoDrop hiện tại rồi lưu. Tên đăng nhập và số điện thoại không được trùng tài khoản khác. Tên đăng nhập 3–80 ký tự chữ không dấu, số, `.`, `_`, `-`.

Trình chọn ảnh hệ thống không yêu cầu đọc toàn bộ thư viện. Android xử lý ảnh ở luồng nền; backend kiểm tra, mã hóa lại JPEG bỏ metadata và lưu trong PostgreSQL. Hồ sơ lấy từ backend; tên/số điện thoại cập nhật vào Room và màn hình hiện tại. Ảnh vẫn còn khi đổi thiết bị.

Đổi số điện thoại hủy mã khôi phục đang chờ; lần sau dùng số mới. Email giữ nguyên. Đổi tên đăng nhập không đổi chủ đơn hàng vì đơn liên kết theo ID.

## Không nhận được thư

- Mặc định `MAIL_ENABLED=false`: app báo dịch vụ chưa cấu hình. Điền Gmail rồi bật và khởi động lại backend.
- Trong liên kết email, bấm **Chưa nhận được mã? Kiểm tra gửi thư** để xem lỗi SMTP/hàng chờ.
- Kiểm tra email nhập đúng, thư rác, Internet và mật khẩu ứng dụng. Máy chủ SMTP tiếp nhận thư chưa đảm bảo thư đã vào Inbox.
- Gửi lỗi sẽ hủy mã. Sửa cấu hình, gửi mã mới sau thời gian chờ. Không tự gửi lại thư cũ vô hạn.
- OTP không trả qua API, không in log. Lưu HMAC; payload trong hàng chờ được mã hóa rồi xóa sau khi gửi/lỗi/hết hạn. Hàng chờ và giới hạn tồn tại qua lần khởi động lại.
- Dùng HTTPS khi triển khai ngoài môi trường demo nội bộ; bản local dùng HTTP cho máy ảo.

Không tạo file test mới cho tính năng này. Kiểm tra sử dụng test sẵn có và lệnh API tạm trên database riêng.
