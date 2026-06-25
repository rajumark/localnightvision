# Local Night Vision

**On-device low-light image enhancement using MIRNet — powered by Kotlin Multiplatform + Compose Multiplatform.**

| Android | iOS (WIP) |
|---------|-----------|
| ✅ CameraX preview + capture | ⚠️ AVFoundation scaffold |
| ✅ Gallery image picker | ⚠️ UIImagePicker scaffold |
| ✅ TFLite GPU inference | ⚠️ TFLite C interop scaffold |
| ✅ Touch-to-compare gesture | ⚠️ Gesture handling scaffold |

---

## Research Paper

This project implements the **MIRNet** architecture described in:

> **"MIRNet: Learning Enriched Features for Real Image Restoration and Enhancement"**  
> *Syed Waqas Zamir, Aditya Arora, Salman Khan, Munawar Hayat, Fahad Shahbaz Khan, Ming-Hsuan Yang, Ling Shao*  
> **arXiv:2003.06792v2**
>
> [Read the paper](https://arxiv.org/pdf/2003.06792v2.pdf)

---

## Features

- **Live camera preview** with real-time viewfinder
- **Capture button** to take a photo and auto-enhance
- **Gallery picker** to select existing photos for enhancement
- **On-device processing** via TensorFlow Lite — zero network calls
- **GPU acceleration** via OpenGL/OpenCL (Android) and Metal (iOS)
- **Touch-to-compare** — hold finger on enhanced image to see original, release to compare
- **~800ms inference** on modern devices (MIRNet 400×400 fixed input)

---

## Architecture

```
localnightvision/
├── composeApp/                          # Shared Compose UI + platform entry points
│   └── src/
│       ├── commonMain/kotlin/com/localnightimage/
│       │   ├── App.kt                   # Root composable, permission → camera → result
│       │   ├── CameraScreen.kt          # Camera preview + capture + picker UI
│       │   ├── CameraPreview.kt         # expect/actual platform camera view
│       │   ├── PermissionScreen.kt      # Runtime permission request UI
│       │   ├── ResultScreen.kt          # Enhanced image + touch-to-compare
│       │   ├── model/
│       │   │   ├── ImageState.kt        # ImageBundle, CaptureMode
│       │   │   └── MlConfig.kt          # Model dimensions, file path
│       │   ├── platform/
│       │   │   ├── ImageProcessor.kt    # expect ML inference interface
│       │   │   ├── ImagePicker.kt       # expect gallery picker interface
│       │   │   └── PermissionManager.kt # expect camera permission interface
│       │   └── ui/theme/
│       │       └── Theme.kt             # Material3 dark/light theme
│       ├── androidMain/kotlin/com/localnightimage/
│       │   ├── MainActivity.kt          # Android Activity entry point
│       │   ├── CameraPreview.android.kt # CameraX PreviewView + ImageCapture
│       │   ├── ImageBitmapUtil.android.kt # ByteArray → ImageBitmap
│       │   └── platform/
│       │       ├── ImageProcessor.android.kt  # TFLite Interpreter + GPU delegate
│       │       ├── ImagePicker.android.kt     # ActivityResultContracts.GetContent
│       │       └── PermissionManager.android.kt # RequestPermission launcher
│       ├── androidMain/assets/model/
│       │   └── lite-model_mirnet-fixed_integer_1.tflite  # MIRNet quantized model
│       ├── androidMain/AndroidManifest.xml
│       └── iosMain/kotlin/com/localnightimage/
│           ├── MainViewController.kt    # iOS UIKit entry point
│           ├── CameraPreview.ios.kt     # AVFoundation scaffold
│           ├── ImageBitmapUtil.ios.kt   # Skia ByteArray → ImageBitmap
│           └── platform/
│               ├── ImageProcessor.ios.kt   # TFLite C API scaffold
│               ├── ImagePicker.ios.kt      # UIImagePicker scaffold
│               └── PermissionManager.ios.kt # AVFoundation auto-prompt
│
├── iosApp/                              # iOS Xcode project wrapper
│   ├── iosApp/
│   │   ├── iOSApp.swift                 # SwiftUI App entry point
│   │   ├── ContentView.swift            # UIViewControllerRepresentable bridge
│   │   └── Info.plist
│   └── iosApp.xcodeproj/                # Auto-generated Xcode project
│
├── docs/
│   └── index.html                       # Project documentation page
│
├── gradle/
│   ├── libs.versions.toml               # Version catalog
│   └── wrapper/
├── build.gradle.kts                     # Root build file
├── settings.gradle.kts                  # Module declarations
├── gradle.properties                    # KMP & Android config
└── local.properties                     # Android SDK path (local)
```

---

## Data Flow

```
 User taps capture/picks image
         │
         ▼
┌─────────────────────┐
│  Camera / Gallery   │  ← Platform-specific (CameraX / UIImagePicker)
│  captures image     │
└─────────┬───────────┘
          │ ByteArray
          ▼
┌─────────────────────┐
│  ImageProcessor     │  ← expect/actual (commonMain → androidMain/iosMain)
│                     │
│  1. Decode to Bitmap│
│  2. Resize to 400x400
│  3. Normalize [0,1] │
│  4. TFLite inference│  ← GPU delegate (OpenGL / Metal)
│  5. Denormalize     │
│  6. Encode to JPEG  │
└─────────┬───────────┘
          │ ProcessingResult
          ▼
┌─────────────────────┐
│  ResultScreen       │  ← Shared Compose UI
│                     │
│  ┌───────────────┐  │
│  │  Enhanced Img  │  │  ← GestureDetector: onPress → show original
│  │  (hold ↕ orig) │  │                    onRelease → show enhanced
│  └───────────────┘  │
│                     │
│  [Back] [Save] [Share]
└─────────────────────┘
```

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Kotlin 2.0.21 |
| **UI** | Compose Multiplatform 1.7.1 (Material3) |
| **ML Runtime** | TensorFlow Lite 2.14.0 |
| **GPU Compute** | GpuDelegate (Android) / Metal (iOS) |
| **Camera (Android)** | CameraX 1.3.4 |
| **Camera (iOS)** | AVFoundation (scaffold) |
| **DI / State** | Compose state, no framework |
| **Build** | Gradle 8.5 + AGP 8.2.2 |

---

## Getting Started

### Prerequisites

- **Android Studio** (or IntelliJ with KMP plugin)
- **JDK 17+** (recommended: OpenJDK 21)
- **Android SDK** (API 34)
- **Xcode 16+** (for iOS, optional)

### Clone & Build

```bash
git clone <repo-url>
cd localnightvision

# Android
./gradlew :composeApp:assembleDebug
adb install -r composeApp/build/outputs/apk/debug/composeApp-debug.apk

# iOS framework
./gradlew :composeApp:linkDebugFrameworkIosSimulatorArm64
# Then open iosApp/iosApp.xcodeproj in Xcode and run
```

### Run on device

```bash
# Android
./gradlew :composeApp:installDebug

# iOS (after first framework link)
# Open iosApp.xcodeproj → select simulator → Cmd+R
```

---

## Model

The app bundles **MIRNet-Fixed (integer quantized)** from TensorFlow Hub:

- **Input:** `[1, 400, 400, 3]` float32 (resized, normalized)
- **Output:** `[1, 400, 400, 3]` float32 (denormalized to 0–255)
- **Format:** Full-integer quantized TFLite (~39 MB)
- **Source:** `sayakpaul/lite-model/mirnet-fixed/dr/1`

The model is loaded from Android assets via `AssetFileDescriptor` → `FileChannel.map()` for zero-copy mmap loading.

---

## Project Status

- ✅ **Android:** Fully functional — camera, picker, ML inference, touch-to-compare
- ⚠️ **iOS:** Scaffold complete — requires AVFoundation camera + TFLite C interop implementations
- 📝 **Pending:** Save-to-gallery, share sheet, EXIF orientation handling, model download fallback

---

## License

```
MIT License

Copyright (c) 2024

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files...
```
