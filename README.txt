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
      - name: Checkout source
        uses: actions/checkout@v4

      # Android SDK 工具现在要求 JDK 17
      - name: Setup JDK 17
        uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '17'

      # 安装 Android SDK
      - name: Setup Android SDK
        uses: android-actions/setup-android@v4
        with:
          packages: 'platform-tools'

      # 安装项目需要的 Android 28
      - name: Install Android SDK 28
        run: |
          yes | sdkmanager --licenses
          sdkmanager "platforms;android-28" "build-tools;28.0.3" "platform-tools"

      # 老项目的 AGP 3.5.4 / Gradle 5.4.1 使用 JDK 8
      - name: Setup JDK 8
        uses: actions/setup-java@v5
        with:
          distribution: temurin
          java-version: '8'

      - name: Setup Gradle 5.4.1
        uses: gradle/actions/setup-gradle@v4
        with:
          gradle-version: '5.4.1'

      - name: Check build environment
        run: |
          echo "===== Java ====="
          java -version
          echo "===== Gradle ====="
          gradle --version
          echo "===== Android SDK ====="
          echo $ANDROID_SDK_ROOT

      - name: Build APK
        run: |
          gradle assembleDebug --no-daemon --stacktrace

      - name: Find APK
        run: |
          find . -name "*.apk" -type f -print

      - name: Upload APK
        uses: actions/upload-artifact@v4
        with:
          name: SIDE-MAX-Android5-TV
          path: app/build/outputs/apk/debug/app-debug.apk
          if-no-files-found: error
