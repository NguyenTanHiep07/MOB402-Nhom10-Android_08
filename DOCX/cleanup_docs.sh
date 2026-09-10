#!/bin/bash
PROJECT_ROOT="$(cd "$(dirname "$0")/.." && pwd)"
cd "$PROJECT_ROOT"

echo "Bắt đầu dọn dẹp thêm các file/thư mục rác không cần thiết..."

# 1. Xóa các script cũ không còn sử dụng ở thư mục gốc
rm -f cleanup.sh
rm -f fix_pr.sh

# 2. Xóa các thư mục rỗng trong Extra/Diagrams (do đã xóa hết file .md bên trong)
find Extra/Diagrams -type d -empty -delete 2>/dev/null

# 3. Tự hủy script dọn dẹp này nếu bạn không cần dùng lại nữa (tùy chọn)
# rm -f DOCX/cleanup_docs.sh

echo "Đã xóa các file script cũ và dọn dẹp các thư mục rỗng thành công!"
# Script EOF
