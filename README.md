# Local Night Vision

**On-device low-light image enhancement using MIRNet — Kotlin Multiplatform + Compose Multiplatform.**

| Platform | Camera | Picker | ML Inference | Touch-to-Compare |
|----------|--------|--------|-------------|-----------------|
| Android  | ✅ CameraX | ✅ GetContent | ✅ TFLite GPU | ✅ GestureDetector |
| iOS      | ⚠️ scaffold | ⚠️ scaffold | ⚠️ scaffold | ⚠️ scaffold |

---

## Research Paper

> **MIRNet: Learning Enriched Features for Real Image Restoration and Enhancement**  
> *Zamir et al., arXiv:2003.06792v2*  
> [arxiv.org/pdf/2003.06792v2.pdf](https://arxiv.org/pdf/2003.06792v2.pdf)

---

## Project Structure

```
localnightvision/
├── composeApp/                              ## ── Shared KMP module ──
│   └── src/
│       ├── commonMain/                      ##  Cross-platform code  │
│       │   └── kotlin/com/localnightimage/                          │
│       │       ├── App.kt                   #  Root: perm → cam → result
│       │       ├── CameraScreen.kt          #  Camera UI + controls
│       │       ├── CameraPreview.kt         #  expect camera composable
│       │       ├── PermissionScreen.kt      #  Permission request UI
│       │       ├── ResultScreen.kt          #  Enhanced + touch-to-compare
│       │       ├── model/
│       │       │   ├── ImageState.kt        #  image bundles & states
│       │       │   └── MlConfig.kt          #  model params (400×400)
│       │       ├── platform/
│       │       │   ├── ImageProcessor.kt    #  expect ML interface
│       │       │   ├── ImagePicker.kt       #  expect gallery picker
│       │       │   └── PermissionManager.kt #  expect permission API
│       │       └── ui/theme/
│       │           └── Theme.kt             #  Material3 dark theme
│       │                                                          │
│       ├── androidMain/                     ##  Android actuals  ──┤
│       │   ├── kotlin/com/localnightimage/                        │
│       │   │   ├── MainActivity.kt          #  Activity entry
│       │   │   ├── CameraPreview.android.kt #  CameraX PreviewView
│       │   │   ├── ImageBitmapUtil.android.kt#  ByteArray→Bitmap
│       │   │   └── platform/
│       │   │       ├── ImageProcessor.android.kt  # TFLite Interpreter
│       │   │       ├── ImagePicker.android.kt     # GetContent
│       │   │       └── PermissionManager.android.kt # RequestPermission
│       │   ├── assets/model/
│       │   │   └── lite-model_mirnet-fixed_integer_1.tflite  # MIRNet (~39MB)
│       │   └── res/                         #  Android resources
│       │                                                          │
│       └── iosMain/                         ##  iOS actuals  ────┤
│           └── kotlin/com/localnightimage/                       │
│               ├── MainViewController.kt    #  UIKit entry
│               ├── CameraPreview.ios.kt     #  AVFoundation scaffold
│               ├── ImageBitmapUtil.ios.kt   #  Skia ByteArray→Bitmap
│               └── platform/
│                   ├── ImageProcessor.ios.kt    # TFLite C API scaffold
│                   ├── ImagePicker.ios.kt       # UIImagePicker scaffold
│                   └── PermissionManager.ios.kt # auto-prompt scaffold
│
├── iosApp/                                  ## ── iOS app wrapper ──
│   ├── iosApp/
│   │   ├── iOSApp.swift                    #  SwiftUI App
│   │   ├── ContentView.swift               #  Compose↔SwiftUI bridge
│   │   └── Info.plist
│   └── iosApp.xcodeproj/
│
├── gradle/
│   └── libs.versions.toml                  #  dependency catalog
├── build.gradle.kts                        #  root build
├── settings.gradle.kts                     #  module setup
├── gradle.properties                       #  KMP + Android flags
└── docs/
    └── index.html                          #  project page
```

---

## Data Flow

```
                          ┌─────────────────────────┐
                          │    Camera / Gallery     │
                          │  CameraX / UIImagePicker│
                          └───────────┬─────────────┘
                                      │ ByteArray
                                      ▼
┌──────────────────────────────────────────────────────────┐
│                 ImageProcessor  (expect/actual)            │
│                                                           │
│   commonMain  :  interface ImageProcessor {               │
│                      suspend fun process(bytes): Result   │
│                  }                                        │
│                                                           │
│   ┌───────────────────── Android ──────────────────────┐  │
│   │  AndroidImageProcessor                              │  │
│   │  1. BitmapFactory.decodeByteArray()                 │  │
│   │  2. Bitmap.createScaledBitmap()  →  400×400        │  │
│   │  3. Normalize RGB to [0, 1]  (Float32)             │  │
│   │  4. TFLite Interpreter.run()  ←  GpuDelegate       │  │
│   │  5. De-normalize to [0, 255]  (Int)                │  │
│   │  6. Bitmap.compress(JPEG)  →  ByteArray            │  │
│   └────────────────────────────────────────────────────┘  │
│                                                           │
│   ┌────────────────────── iOS ─────────────────────────┐  │
│   │  IosImageProcessor  (scaffold)                     │  │
│   │  Uses TFLite C API via cinterop                    │  │
│   └────────────────────────────────────────────────────┘  │
└───────────────────────────┬──────────────────────────────┘
                            │ ProcessingResult
                            ▼
┌──────────────────────────────────────────────────────────┐
│                    ResultScreen  (commonMain)              │
│                                                           │
│   ┌────────────────────────────────────────────────────┐  │
│   │                                                    │  │
│   │               Enhanced Image                       │  │
│   │   ┌──────────────────────────────────────────┐     │  │
│   │   │  pointerInput(detectTapGestures)          │     │  │
│   │   │  onPress   →  showOriginal = true         │     │  │
│   │   │  onRelease →  showOriginal = false        │     │  │
│   │   │  display = showOriginal ? orig : enhanced  │     │  │
│   │   └──────────────────────────────────────────┘     │  │
│   │                                                    │  │
│   │         [✕]          [💾]          [↗]             │  │
│   │        dismiss       save         share           │  │
│   └────────────────────────────────────────────────────┘  │
└──────────────────────────────────────────────────────────┘
```

---

## Expect / Actual Pattern

```
                     commonMain/
                      ImageProcessor.kt
                     interface ImageProcessor
                     ─────────────────
                     suspend fun initialize()
                     suspend fun process(bytes, w, h): Result
                     fun release()
                           │
            ┌──────────────┴──────────────┐
            │                             │
   androidMain/                    iosMain/
   ImageProcessor.android.kt       ImageProcessor.ios.kt
   ───────────────────────         ──────────────────
   class AndroidImageProcessor     class IosImageProcessor
   : ImageProcessor {              : ImageProcessor {
     TFLite Java API                 TFLite C API
     GpuDelegate support             (scaffold)
     CameraX integration             AVFoundation
   }                               }
```

```
                     CameraPreview
                     ─────────────
                     expect @Composable
                     fun CameraPreview(...)
                           │
            ┌──────────────┴──────────────┐
            │                             │
   AndroidView                     UIViewRepresentable
   (CameraX PreviewView)           (AVCaptureVideoPreviewLayer)
```

---

## Tech Stack

| Layer | Choice |
|-------|--------|
| Language | Kotlin 2.0.21 |
| UI Framework | Compose Multiplatform 1.7.1 (Material3) |
| ML Runtime | TensorFlow Lite 2.14.0 |
| GPU Compute | GpuDelegate (Android) / Metal (iOS scaffold) |
| Camera | CameraX 1.3.4 (Android) / AVFoundation (iOS scaffold) |
| Image Picker | ActivityResultContracts (Android) / UIImagePicker (iOS scaffold) |
| State Mgmt | Compose `mutableStateOf` |
| Build | Gradle 8.5 + AGP 8.2.2 |

---

## Build & Run

```bash
# ── Android ──
./gradlew :composeApp:assembleDebug
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk

# ── iOS framework ──
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
# then open iosApp/iosApp.xcodeproj → Cmd+R
```

### Prerequisites

- JDK 17+ (recommended: OpenJDK 21)
- Android SDK (API 34)
- Xcode 16+ (iOS only)

---

## Model

| Property | Value |
|----------|-------|
| Architecture | MIRNet-Fixed (integer quantized) |
| Input shape | `[1, 400, 400, 3]` float32 |
| Output shape | `[1, 400, 400, 3]` float32 |
| File size | ~39 MB |
| Source | TensorFlow Hub: `sayakpaul/lite-model/mirnet-fixed/dr/1` |
| Loading | `AssetFileDescriptor` → `FileChannel.map()` (zero-copy mmap) |

---

## Status

- ✅ **Android:** Camera, image picker, TFLite ML inference, touch-to-compare — fully functional
- ⚠️ **iOS:** Architecture fully wired up — needs AVFoundation + TFLite C interop implementations
- 📝 **Pending:** Save to gallery, share sheet, EXIF orientation correction
