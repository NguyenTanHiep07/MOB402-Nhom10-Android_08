# Implementation Plan - Fix Android Resource Linking Failed (Missing Material3 Theme)

The project is failing to build because it references `Theme.Material3.DayNight.NoActionBar` in `themes.xml`, but the required Material Components for Android library (`com.google.android.material:material`) is missing from the `app` module's dependencies.

## Proposed Changes

### Build Configuration

#### [MODIFY] [app/build.gradle.kts](file:///C:/MOB402-Nhom10-Android_UTH_08/Code/Android_UTH_08/app/build.gradle.kts)
- Add `implementation(libs.material)` to the dependencies block.
- (Optional but recommended) Update other dependencies to use the version catalog (`libs`) for consistency.

## Verification Plan

### Automated Tests
- Run `./gradlew :app:processDebugResources` to verify that resource linking now succeeds.
- Run `./gradlew assembleDebug` to ensure the entire app builds correctly.

### Manual Verification
- None required as this is a build-time issue.
