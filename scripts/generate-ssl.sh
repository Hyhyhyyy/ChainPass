#!/bin/bash

# =====================================================
# ChainPass SSL证书生成脚本
# 用于生成开发环境的自签名SSL证书
# =====================================================

echo "🔒 ChainPass SSL证书生成工具"
echo "=================================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m'

# 证书目录
CERT_DIR="apps/server/src/main/resources/ssl"
mkdir -p "$CERT_DIR"

# 证书有效期（天）
DAYS=365

# 证书信息
DOMAIN="localhost"
ORG="ChainPass"
UNIT="Development"
COUNTRY="CN"

echo ""
echo "📝 生成PKCS12格式证书..."

# 生成私钥和证书（PKCS12格式）
openssl req -x509 -newkey rsa:2048 -keyout "$CERT_DIR/key.pem" -out "$CERT_DIR/cert.pem" \
  -days $DAYS -nodes \
  -subj "/C=$COUNTRY/O=$ORG/OU=$UNIT/CN=$DOMAIN" \
  -addext "subjectAltName=DNS:localhost,DNS:127.0.0.1,IP:127.0.0.1"

# 转换为PKCS12格式（Java KeyStore）
openssl pkcs12 -export -in "$CERT_DIR/cert.pem" -inkey "$CERT_DIR/key.pem" \
  -out "$CERT_DIR/keystore.p12" \
  -name "chainpass" \
  -password pass:chainpass_ssl_password

echo "${GREEN}✓ SSL证书已生成${NC}"
echo ""
echo "证书文件位置:"
echo "  - $CERT_DIR/keystore.p12 (Java KeyStore)"
echo "  - $CERT_DIR/cert.pem     (PEM格式证书)"
echo "  - $CERT_DIR/key.pem      (PEM格式私钥)"
echo ""
echo "=================================="
echo "${YELLOW}⚠️  重要提示：${NC}"
echo "1. 此证书为自签名证书，仅用于开发环境"
echo "2. 生产环境请使用正规CA颁发的证书"
echo "3. keystore密码: chainpass_ssl_password"
echo "4. 证书有效期: $DAYS 天"
echo "=================================="
echo ""
echo "📖 使用方法："
echo "在 application-prod.yml 中添加:"
echo ""
echo "server:"
echo "  ssl:"
echo "    enabled: true"
echo "    key-store: classpath:ssl/keystore.p12"
echo "    key-store-password: chainpass_ssl_password"
echo "    key-store-type: PKCS12"
echo ""