# Prompt chuẩn cho Google Stitch

Sao chép prompt dưới đây vào project Stitch **GoDrop — Android_08**. Chọn phong cách
Android mobile, Material 3, light theme và kích thước tham chiếu 390×844.

## Prompt tổng

> Thiết kế prototype Android cho ứng dụng giao hàng GoDrop. Dùng Material 3, font
> Roboto, nền #F7F8FC, primary #4F46E5, primary dark #3730A3, secondary #F97316,
> surface trắng và chữ chính #172033. Phong cách hiện đại, rõ ràng, card bo tròn,
> khoảng cách theo grid 4dp. Prototype gồm Login và Home shell cho CLIENT, DELIVERY,
> ADMIN. Sau đăng nhập, role từ tài khoản quyết định Home; không cho chọn role thủ công.
> Mỗi Home có gradient header tím, logo GoDrop, tên người dùng, nút profile, logout và
> bottom navigation bốn mục. Giữ màu, font, button và input nhất quán giữa mọi màn hình.

## AUTH-01 Login

> Tạo màn hình Login GoDrop 390×844. Nền #F7F8FC, card trắng bo 28dp, logo giao hàng,
> tiêu đề GoDrop tím đậm, subtitle tiếng Việt. Có outlined input Số điện thoại với icon
> phone, input Mật khẩu với icon lock và toggle ẩn/hiện, link Quên mật khẩu, button
> Đăng nhập cao 53dp màu #4F46E5 bo 15dp. Tạo thêm trạng thái loading, lỗi thiếu trường
> và lỗi xác thực. Không thêm chọn role hay đăng ký.

## Home shell

> Tạo ba biến thể Home shell CLIENT, DELIVERY, ADMIN dùng cùng design system. Header
> cao 228dp gradient #3730A3 sang #4F46E5, bo đáy 32dp. Hiển thị role, lời chào, tên,
> profile và logout. Body nền #F7F8FC, lề 20dp, card trắng. Bottom navigation bốn mục,
> item chọn dùng nền #EEF2FF và icon #4F46E5. Client dùng Trang chủ/Đơn hàng/Theo dõi/
> Hồ sơ; Delivery dùng Trang chủ/Đơn chờ/Đang giao/Hồ sơ; Admin dùng Tổng quan/Yêu cầu/
> Người dùng/Cài đặt.

Sau khi Stitch tạo xong, so sánh token với `Stitch-Design-System.md`, đặt quyền chia sẻ
“Anyone with the link can view”, rồi dán URL project vào `Extra/Stitch/README.md`.
