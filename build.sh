#!/bin/bash  
set -e  
set -x
# 构建镜像  
echo "Building Docker image..."  
docker build -t pixel-volte-builder .  
  
# 创建必要的目录  

mkdir -p  /mnt/sda1/.gradle
# 运行构建  
echo "Building APK..." 
if [ "$(docker ps -qa -f name=andriod_complier)" ]; then
  docker stop andriod_complier && docker rm andriod_complier
else
  echo "容器 andriod_complier 不存在"
fi
docker run  --name=andriod_complier  -d --restart=always -v /mnt/sda1/workspace_study/andriod:/workspace   -v /mnt/sda1/.gradle:/root/.gradle   pixel-volte-builder:latest sleep infinity

echo "Build development  env completed! "
