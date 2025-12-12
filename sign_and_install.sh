
#!/bin/bash

# 检查工具
if ! command -v apksigner &> /dev/null; then
    echo "错误: apksigner未安装，请安装Android SDK Build-Tools"
    exit 1
fi

# 生成密钥库（若不存在）
if [ ! -f "android.keystore" ]; then
    echo "生成密钥库: android.keystore"
    keytool -genkey -v \
        -keystore android.keystore \
        -keyalg RSA \
        -keysize 2048 \
        -validity 10000 \
        -alias androidkey \
        -storepass android123 \
        -keypass android123 \
        -dname "CN=Unknown, OU=Android, O=Developer, L=Unknown, ST=Unknown, C=Unknown"
fi

# 签名APK
APK_PATH="$1"
SIGNED_APK="${APK_PATH%.*}-signed.apk"

echo "签名APK: $APK_PATH"
apksigner sign \
    --ks android.keystore \
    --ks-key-alias androidkey \
    --ks-pass pass:android123 \
    --key-pass pass:android123 \
    --out "$SIGNED_APK" \
    "$APK_PATH"

# 验证签名
echo "验证签名..."
if apksigner verify "$SIGNED_APK" &> /dev/null; then
    echo "签名验证成功"
    echo "已生成签名文件: $SIGNED_APK"
else
    echo "签名验证失败"
    exit 1
fi

