#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
if [ -e .env ]; then
  echo "Đã có Backend/.env; giữ nguyên cấu hình."
  exit 0
fi
umask 077
db_password=""
if command -v docker >/dev/null 2>&1; then
  db_password=$(docker inspect mob402-delivery-postgres --format '{{range .Config.Env}}{{println .}}{{end}}' 2>/dev/null | sed -n 's/^POSTGRES_PASSWORD=//p') || true
fi
db_password=${db_password:-$(openssl rand -hex 24)}
jwt_value=$(openssl rand -hex 32)
demo_value=$(openssl rand -hex 8)
cat > .env <<EOF
POSTGRES_DB=delivery_db
POSTGRES_USER=delivery_user
POSTGRES_PASSWORD=$db_password
JWT_SECRET=$jwt_value
DEMO_ENABLED=true
DEMO_PASSWORD=$demo_value
EOF
echo "Đã tạo Backend/.env (không đưa lên Git). Mật khẩu tài khoản demo mới nằm ở DEMO_PASSWORD."
echo "Tài khoản đã tồn tại trong PostgreSQL giữ nguyên mật khẩu cũ."
