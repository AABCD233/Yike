# 番茄计划

一个自己写的番茄钟 + 待办 + 统计 App。界面基座来自
[KernelSU-Style-UI-Kit](https://github.com/chenaizhang/KernelSU-Style-UI-Kit)，
功能上参考了市面上的番茄类应用，代码是独立实现，与「番茄ToDo」及其作者无关。

## 现在能做什么

- 待办：普通番茄钟 / 定目标 / 养习惯三种类型，倒计时、正向计时、不计时三种模式，
  时长可以单条设置；卡片右上角 ⋮ 里重命名、删除
- 番茄钟：专注页圆环倒计时，暂停、继续、放弃、结束；计时状态写本地，
  切后台或进程被杀再回来还能接上；归零自动入库、发通知、问要不要休息五分钟
- 锁机：选时长（15/25/45/60 或自定义）进全屏锁机页，屏幕常亮、返回键屏蔽，
  到点自动结束，提前结束记一次放弃
- 待办集：新建、重命名、删除，点开展开里面的待办直接开始计时
- 统计：累计与当日专注（可翻前一天）、日/周/月的时长饼图与占比、
  24 小时时段柱图、最近七天柱图、锁机次数与时长
- 专注记录：按天分组，逐条可删
- 我的：默认番茄时长、备份导出 JSON、从 JSON 恢复、设置、关于、权限
- 深色模式：页面底色和卡片会跟着系统切换

## 安装

仓库是私有的，装包从 Actions 拿：打开最新一次 `Build APK` 的 run，
下载 artifact `tomatoplan-apk`，里面是 `TomatoPlan_v0.1.0_1-debug.apk`。

- 需要 Android 12（API 31）以上
- 每次构建都用仓库里的 `keystore/tomato.p12` 签名，可以直接覆盖安装升级
- 第一次打开会申请通知权限，不给也能用，只是专注结束没有提醒

## 构建

GitHub Actions 负责编译，本机（手机上的 proot 环境）没有 JDK 和 Android SDK。
工作流在 `.github/workflows/build-apk.yml`，会先装 SDK 37 和 build-tools 37，
然后 `./gradlew :app:assembleDebug`，最后校验签名指纹。

版本栈：Gradle 9.7.1 / AGP 9.4.1 / Kotlin 2.4.20 / compileSdk 37 / minSdk 31 /
Compose BOM 2026.09.00 / Miuix 0.9.4。

## 数据

全部本地存储，没有任何网络请求，也不上传任何东西。

- 待办、待办集、专注记录：SQLite（`data/TodoDb.kt`，表 `todo_set` / `todo` / `session`）
- 正在进行的计时：SharedPreferences（`data/FocusTimer.kt`）
- 设置项：SharedPreferences

备份就是「我的 → 备份到文件」导出的那份 JSON，可以再导回来。

## 许可

本项目基于 GPL-3.0 发布，因为界面基座是 GPL-3.0 的 KernelSU-Style-UI-Kit。
自己用没问题，如果哪天想闭源，得先把 Miuix 那套界面组件换掉。

## 还没做

学霸模式的非白名单应用拦截（需要「使用情况访问」权限和悬浮窗拦截页）、
定时锁机的时间段管理、桌面小组件、悬浮计时器。
