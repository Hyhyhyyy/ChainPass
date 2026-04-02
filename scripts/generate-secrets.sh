#!/bin/bash

# =====================================================
# ChainPass 生产环境安全配置脚本
# 此脚本用于生成安全的密钥和配置
# =====================================================

echo "🔐 ChainPass 安全配置工具"
echo "=============================="

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
NC='\033[0m' # No Color

# 1. 生成 JWT 密钥 (HS256 需要至少 256 位 = 32 字符)
echo ""
echo "📝 生成 JWT 密钥..."
JWT_SECRET=$(openssl rand -base64 64 | tr -d '\n' | head -c 64)
echo "${GREEN}✓ JWT 密钥已生成${NC}"

# 2. 生成数据库密码
echo ""
echo "📝 生成数据库密码..."
DB_PASSWORD=$(openssl rand -base64 24 | tr -d '\n' | head -c 24)
echo "${GREEN}✓ 数据库密码已生成${NC}"

# 3. 生成 Redis 密码
echo ""
echo "📝 生成 Redis 密码..."
REDIS_PASSWORD=$(openssl rand -base64 24 | tr -d '\n' | head -c 24)
echo "${GREEN}✓ Redis 密码已生成${NC}"

# 4. 生成 Refresh Token 密钥
echo ""
echo "📝 生成 Refresh Token 密钥..."
REFRESH_SECRET=$(openssl rand -base64 64 | tr -d '\n' | head -c 64)
echo "${GREEN}✓ Refresh Token 密钥已生成${NC}"

# 5. 生成 OAuth State 密钥
echo ""
echo "📝 生成 OAuth State 密钥..."
OAUTH_STATE_SECRET=$(openssl rand -hex 32)
echo "${GREEN}✓ OAuth State 密钥已生成${NC}"

# 输出到 .env.production 文件
echo ""
echo "📄 生成 .env.production 文件..."

cat > .env.production << EOF
# =====================================================
# ChainPass 生产环境配置
# 生成时间: $(date '+%Y-%m-%d %H:%M:%S')
# 警告: 此文件包含敏感信息，请勿提交到版本控制！
# =====================================================

# ==================== 数据库配置 ====================
DB_URL=jdbc:mysql://localhost:3306/chainpass?useUnicode=true&characterEncoding=utf-8&useSSL=true&allowPublicKeyRetrieval=true&serverTimezone=Asia/Shanghai
DB_USERNAME=chainpass
DB_PASSWORD=${DB_PASSWORD}

# ==================== Redis 配置 ====================
REDIS_HOST=localhost
REDIS_PORT=6379
REDIS_PASSWORD=${REDIS_PASSWORD}

# ==================== JWT 配置 ====================
JWT_SECRET=${JWT_SECRET}
JWT_ACCESS_TOKEN_TTL=900000
JWT_REFRESH_TOKEN_TTL=604800000

# ==================== OAuth 配置 ====================
# Gitee OAuth (请替换为实际值)
GITEE_CLIENT_ID=your_gitee_client_id
GITEE_CLIENT_SECRET=your_gitee_client_secret
GITEE_REDIRECT_URI=https://your-domain.com/oauth/gitee/callback

# OAuth State 加密密钥
OAUTH_STATE_SECRET=${OAUTH_STATE_SECRET}

# ==================== 应用配置 ====================
SPRING_PROFILES_ACTIVE=prod
SERVER_PORT=8080
SERVER_SSL_ENABLED=true

# ==================== 日志配置 ====================
LOG_LEVEL_ROOT=WARN
LOG_LEVEL_APP=INFO
EOF

echo "${GREEN}✓ .env.production 文件已生成${NC}"

# 输出到控制台（供手动配置）
echo ""
echo "=============================="
echo "📋 以下是你需要的信息："
echo "=============================="
echo ""
echo "JWT_SECRET=${JWT_SECRET}"
echo ""
echo "DB_PASSWORD=${DB_PASSWORD}"
echo ""
echo "REDIS_PASSWORD=${REDIS_PASSWORD}"
echo ""
echo "OAUTH_STATE_SECRET=${OAUTH_STATE_SECRET}"
echo ""
echo "=============================="
echo "${YELLOW}⚠️  安全提示：${NC}"
echo "1. 请妥善保管 .env.production 文件"
echo "2. 不要将此文件提交到 Git"
echo "3. 定期轮换这些密钥"
echo "=============================="