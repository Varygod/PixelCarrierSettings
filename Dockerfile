FROM ubuntu:22.04  
  
ENV DEBIAN_FRONTEND=noninteractive  
  
# 安装基础依赖（与 CI 一致）  
RUN apt-get update && apt-get install -y \  
    curl \
    android-sdk-build-tools \  
    vim  \  
    unzip \  
    wget \  
    openjdk-17-jdk \  
    python3 \  
    python3-pip \  
    git \  
    && rm -rf /var/lib/apt/lists/*  
  
# 设置 Java 环境（CI 使用 Zulu OpenJDK 17）  
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64  
ENV PATH=$PATH:$JAVA_HOME/bin  
  
# 安装 Android SDK（与 CI 相同的版本）  
RUN wget  https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O tools.zip && \  
    unzip -q tools.zip && \  
    mkdir -p /usr/local/lib/android/sdk/cmdline-tools/latest && \  
    mv cmdline-tools/* /usr/local/lib/android/sdk/cmdline-tools/latest/ && \  
    rm tools.zip  

ENV ANDROID_SDK_ROOT=/usr/local/lib/android/sdk  
ENV ANDROID_HOME=/usr/local/lib/android/sdk  
ENV PATH=$PATH:$ANDROID_SDK_ROOT/cmdline-tools/latest/bin:$ANDROID_SDK_ROOT/platform-tools  
  
# 安装 Android 平台和构建工具（与 CI 完全一致）  
RUN sdkmanager --sdk_root=$ANDROID_SDK_ROOT "platforms;android-36" "build-tools;36.0.0"  
  
# 替换 android.jar（与 CI 相同的源）  
RUN mkdir -p $ANDROID_SDK_ROOT/platforms/android-36 && \  
    curl --retry 10  -L https://github.com/Reginer/aosp-android-jar/raw/main/android-36/android.jar > $ANDROID_SDK_ROOT/platforms/android-36/android.jar  
  
# 预下载并安装 Gradle（避免运行时下载，对应 CI 的缓存策略）  
RUN wget  https://services.gradle.org/distributions/gradle-8.13-bin.zip -O gradle.zip && \  
    unzip -q gradle.zip -d /opt && \  
    rm gradle.zip  
  
ENV GRADLE_HOME=/opt/gradle-8.13  
ENV PATH=$PATH:$GRADLE_HOME/bin  
  
# 接受所有 SDK 许可证  
RUN yes | sdkmanager --sdk_root=$ANDROID_SDK_ROOT --licenses  
  
WORKDIR /workspace
