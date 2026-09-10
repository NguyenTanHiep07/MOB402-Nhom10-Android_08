#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if [ -e .env ]; then
  echo "Đã có Backend/.env; giữ nguyên cấu hình."
  exit 0
fi
umask 077
jwt_value=$(openssl rand -hex 32)
demo_value=$(openssl rand -hex 8)
cat > .env <<EOF
MONGODB_URI=mongodb://localhost:27017/delivery_db
JWT_SECRET=$jwt_value
DEMO_ENABLED=true
DEMO_PASSWORD=$demo_value
JWT_EXPIRATION_MS=86400000
PHOTON_BASE_URL=https://photon.komoot.io
OSRM_BASE_URL=https://router.project-osrm.org
LOCATION_REQUEST_TIMEOUT_MS=8000
LOCATION_USER_AGENT=GoDrop-UTH-08/1.0-student-project
EOF
echo "Đã tạo Backend/.env (không đưa lên Git). Mật khẩu tài khoản demo mới nằm ở DEMO_PASSWORD."
echo "Tài khoản đã tồn tại trong MongoDB giữ nguyên mật khẩu cũ."
