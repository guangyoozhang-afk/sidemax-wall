name: Build SIDE MAX TV APK

on:
  workflow_dispatch:
  push:
    branches:
      - main

jobs:
  build:
    runs-on: ubuntu-22.04

    steps:

      # 1. 下载项目
      - name: Checkout source
        uses: actions/checkout@v4

      # 2. 先使用 JDK 17
      # 新版 Android SDK command-line tools 要求 JDK 17+
      - name: Setup JDK 17
        uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '17'

      # 3. 设置 Android SDK
      - name: Setup Android SDK
        uses: android-actions/setup-android@v4
        with:
          packages: 'platform-tools'

      # 4. 安装 Android 28 SDK 和 Build Tools
      - name: Install Android SDK 28
        run: |
          yes | sdkmanager --licenses
          sdkmanager "platforms;android-28" "build-tools;28.0.3" "platform-tools"

      # 5. 切换回 JDK 8
      # Android Gradle Plugin 3.5.4 + Gradle 5.4.1 使用这套旧构建环境
      - name: Setup JDK 8
        uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '8'

      # 6. 设置 Gradle 5.4.1
      - name: Setup Gradle 5.4.1
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '5.4.1'

      # 7. 检查环境
      - name: Check build environment
        run: |
          echo "===== Java ====="
          java -version

          echo "===== Gradle ====="
          gradle --version

          echo "===== Android SDK ====="
          echo $ANDROID_SDK_ROOT

      # 8. 编译 APK
      - name: Build APK
        run: |
          gradle assembleDebug --no-daemon --stacktrace

      # 9. 查找 APK
      - name: Find APK
        run: |
          find . -name "*.apk" -type f -print

      # 10. 上传 APK
      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: SIDE-MAX-Android5-TV
          path: app/build/outputs/apk/debug/app-debug.apk
          if-no-files-found: error
