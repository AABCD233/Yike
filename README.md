# 一刻

一个自己写的番茄钟：待办、专注计时、锁机、数据统计。

界面基座来自 [KernelSU-Style-UI-Kit](https://github.com/chenaizhang/KernelSU-Style-UI-Kit)。

## 功能

- 待办：普通番茄钟 / 定目标 / 养习惯，倒计时、正向计时、不计时，时长可单条设置
- 待办集：新建、重命名、删除，点开展开直接计时
- 专注：圆环倒计时，暂停、继续、放弃、结束；计时状态写本地，切后台或进程被杀再回来能接上
- 锁机：选时长进全屏锁机页，屏幕常亮、返回键屏蔽，到点自动结束
- 统计：累计与当日专注、日周月饼图、24 小时时段柱图、最近七天柱图、锁机数据
- 专注记录：按天分组，逐条可删
- 备份：导出 JSON，也能从 JSON 导回来
- 深色模式

## 安装

从 Actions 里最新一次 `Build APK` 下载 artifact `yike-apk`，装里面的 APK。

- Android 12（API 31）以上
- 每次构建都用仓库里的 `keystore/yike.p12` 签名，可以覆盖安装升级
- 不给通知权限也能用，只是专注结束没有提醒

## 构建

本机没有 JDK 和 Android SDK，编译全在 GitHub Actions。
工作流在 `.github/workflows/build-apk.yml`：装 SDK 37 和 build-tools 37，
跑 `./gradlew :app:assembleDebug`，最后校验签名指纹。

版本栈：Gradle 9.7.1 / AGP 9.4.1 / Kotlin 2.4.20 / compileSdk 37 / minSdk 31 /
Compose BOM 2026.09.00 / Miuix 0.9.4。

## 数据

全部在本地，没有网络请求。

- 待办、待办集、专注记录：SQLite（`data/TodoDb.kt`）
- 正在进行的计时：SharedPreferences（`data/FocusTimer.kt`）

## 许可

GPL-3.0。界面基座是 GPL-3.0 的 KernelSU-Style-UI-Kit，所以这个项目也一样。
