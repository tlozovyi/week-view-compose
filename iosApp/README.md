# iOS Sample App

Xcode shell for the `week-view-compose` sample. It hosts the shared Compose UI from the `:sample` Kotlin module.

## Requirements

- macOS with Xcode 15+
- Apple Silicon Mac (project targets `iosSimulatorArm64` / `iosArm64`)

## Run in Xcode

1. Open the project:

   ```bash
   open iosApp/iosApp.xcodeproj
   ```

2. Select the **iosApp** scheme and an **iPhone simulator**.

3. Press **Run** (⌘R).

The **Compile Kotlin Framework** build phase invokes Gradle (`:sample:embedAndSignAppleFrameworkForXcode`) to build and embed `SampleApp.framework` before compiling Swift.

## Signing

For a physical device, set your Apple Team ID in `Configuration/Config.xcconfig`:

```
TEAM_ID=YOUR_TEAM_ID
```

Simulator builds work without a team ID.

## Command line build

```bash
xcodebuild \
  -project iosApp/iosApp.xcodeproj \
  -scheme iosApp \
  -sdk iphonesimulator \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  build
```

## Troubleshooting

- **"Command PhaseScriptExecution failed"** — Xcode uses a minimal shell environment. The build script requires **JDK 17** (Kotlin 2.0 does not support Java 25 as the Gradle runtime). Install JDK 17, or set `JAVA_HOME` in `iosApp/Configuration/Config.xcconfig`:
  ```
  JAVA_HOME=/path/to/jdk-17
  ```
  Verify from Terminal: `/usr/libexec/java_home -v 17`
- **"No such module SampleApp"** — Build once from Xcode so the Gradle script runs, or run `./gradlew :sample:linkDebugFrameworkIosSimulatorArm64` from the repo root.
- **Gradle script blocked** — Ensure `ENABLE_USER_SCRIPT_SANDBOXING = NO` in the Xcode project (already set here).
- **Stale Kotlin changes** — Product → Clean Build Folder in Xcode, then rebuild.
