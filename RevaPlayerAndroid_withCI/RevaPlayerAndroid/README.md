# Reva Player — Android
**مشغل فيديو محلي لـ Android مستوحى من Reva Player Linux**

---

## ⚡ خطوات التشغيل السريع

### 1. افتح المشروع في Android Studio
```
File → Open → اختر مجلد RevaPlayerAndroid
```
انتظر حتى تنتهي مزامنة Gradle (قد تستغرق 2-5 دقائق أول مرة)

### 2. ربط جهازك
- فعّل **Developer Options** على جهاز Android
- فعّل **USB Debugging**
- وصّل الجهاز بالكمبيوتر

### 3. تشغيل التطبيق
اضغط ▶️ **Run** أو `Shift + F10`

---

## ✅ الميزات المنفذة

| الميزة | الحالة |
|--------|--------|
| تشغيل MP4, MKV, AVI, MOV... | ✅ |
| استئناف تلقائي من آخر نقطة | ✅ |
| سجل مشاهدة كامل | ✅ |
| علامات (Bookmarks) مع ملاحظات | ✅ |
| قوائم تشغيل متعددة | ✅ |
| تحكم بالسرعة 0.25× - 3× | ✅ |
| صورة داخل صورة (PiP) | ✅ |
| ضغطتان للتقديم ±10 ثانية | ✅ |
| سحب للتحكم بالصوت | ✅ |
| تصفح المجلدات المحلية | ✅ |
| بحث في السجل | ✅ |
| تشغيل مجلد كامل كقائمة | ✅ |
| دعم فتح الفيديو من مدير الملفات | ✅ |
| تصميم داكن مطابق لـ Reva الأصلي | ✅ |

---

## 🗂️ هيكل المشروع

```
app/src/main/
├── java/com/revaplayer/
│   ├── RevaApplication.java
│   ├── ui/
│   │   ├── activities/
│   │   │   ├── MainActivity.java        ← الرئيسية + التنقل
│   │   │   ├── PlayerActivity.java      ← المشغّل الكامل
│   │   │   ├── HomeFragment.java        ← الصفحة الرئيسية
│   │   │   ├── HistoryFragment.java     ← السجل
│   │   │   ├── BookmarksFragment.java   ← العلامات
│   │   │   ├── SettingsFragment.java    ← الإعدادات
│   │   │   ├── FolderBrowserActivity.java ← تصفح المجلدات
│   │   │   └── SearchActivity.java      ← البحث
│   │   ├── adapters/
│   │   │   ├── HomeTabAdapter.java
│   │   │   ├── MediaListAdapter.java
│   │   │   ├── BookmarkAdapter.java
│   │   │   ├── PlaylistAdapter.java
│   │   │   └── FileAdapter.java
│   │   ├── dialogs/
│   │   │   └── BookmarkDialog.java
│   │   └── viewmodels/
│   │       ├── MainViewModel.java
│   │       └── PlayerViewModel.java
│   ├── data/database/
│   │   └── RevaDatabase.java            ← SQLite كاملة
│   └── domain/model/
│       ├── MediaItem.java
│       └── Bookmark.java
├── res/
│   ├── layout/          (12 ملف XML)
│   ├── drawable/        (26 ملف: أيقونات + خلفيات)
│   ├── values/          (ألوان + themes + نصوص)
│   └── menu/            (bottom_nav_menu)
└── AndroidManifest.xml
```

---

## 🎮 إيماءات المشغّل

| الإيماءة | الوظيفة |
|----------|---------|
| ضغطة واحدة | إظهار/إخفاء التحكم |
| ضغطتان (يسار) | إرجاع 10 ثانية ⏪ |
| ضغطتان (يمين) | تقديم 10 ثانية ⏩ |
| ضغطتان (وسط) | تشغيل/إيقاف |
| سحب رأسي (يمين) | التحكم بالصوت 🔊 |
| الخروج من التطبيق | تشغيل PiP تلقائياً |

---

## 🗄️ قاعدة البيانات

الملف: `reva_player.db` في `App Data`

| الجدول | المحتوى |
|--------|---------|
| `playback_history` | كل الفيديوهات المشاهدة مع التقدم |
| `resume_state` | آخر موضع لكل فيديو |
| `bookmarks` | العلامات مع الملاحظات والتصنيف |
| `settings` | إعدادات التطبيق |

---

## 📋 متطلبات التشغيل
- Android Studio Hedgehog 2023.1.1+
- Android SDK 34, Build Tools 34
- Java 17
- Android 8.0+ (API 26+)
- حجم التطبيق: ~8MB (بدون الفيديوهات)

---

## 🔧 ملاحظات للمطورين

- **ExoPlayer** يستبدل libmpv من النسخة الأصلية
- قاعدة البيانات SQLite محلية بالكامل بلا إنترنت
- كل الـ ViewModels تستخدم LiveData
- الـ Executor يمنع العمليات الثقيلة على Main Thread
- PiP يعمل تلقائياً عند `onUserLeaveHint()`
