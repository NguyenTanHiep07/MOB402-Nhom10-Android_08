# Implementation Plan - Convert Client Home HTML to Kotlin (Compose)

The goal is to implement the "Client Home" screen design provided in HTML/Tailwind into the Android project using Jetpack Compose.

## Proposed Changes

### Infrastructure

#### [MODIFY] [libs.versions.toml](file:///C:/MOB402-Nhom10-Android_UTH_08/Code/Android_UTH_08/gradle/libs.versions.toml)
- Add Jetpack Compose, Material3, and related dependencies (navigation, lifecycle, etc.).

#### [MODIFY] [build.gradle.kts](file:///C:/MOB402-Nhom10-Android_UTH_08/Code/Android_UTH_08/app/build.gradle.kts)
- Enable Compose feature.
- Add Compose-related dependencies.

### UI Components

#### [NEW] [Color.kt](file:///C:/MOB402-Nhom10-Android_UTH_08/Code/Android_UTH_08/app/src/main/java/com/mob10/deliveryapp/ui/theme/Color.kt)
- Define the color palette based on the Tailwind configuration in the HTML (e.g., `#f7f9fb` for background, `#000000` for primary).

#### [NEW] [Theme.kt](file:///C:/MOB402-Nhom10-Android_UTH_08/Code/Android_UTH_08/app/src/main/java/com/mob10/deliveryapp/ui/theme/Theme.kt)
- Set up the Material3 theme for the app.

#### [NEW] [HomeScreen.kt](file:///C:/MOB402-Nhom10-Android_UTH_08/Code/Android_UTH_08/app/src/main/java/com/mob10/deliveryapp/HomeScreen.kt)
- Implement the "Client Home" UI:
    - Top bar with menu and profile photo.
    - Greeting section.
    - Bento grid-style stats cards.
    - Primary CTA button (+ Tạo yêu cầu giao hàng).
    - Quick Action list.
    - Bottom navigation bar (for mobile).

#### [MODIFY] [MainActivity.kt](file:///C:/MOB402-Nhom10-Android_UTH_08/Code/Android_UTH_08/app/src/main/java/com/mob10/deliveryapp/MainActivity.kt)
- Update `MainActivity` to host the Compose UI instead of using Fragments/XML (or provide a way to transition).

## Verification Plan

### Manual Verification
- Deploy the app to a device/emulator.
- Verify that the layout matches the provided HTML design (cards, colors, buttons).
- Check responsiveness (mobile bottom nav vs desktop top nav logic if applicable, though primarily mobile focus).
- Ensure the "Tạo yêu cầu giao hàng" button is clickable.
