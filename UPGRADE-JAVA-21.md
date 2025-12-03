Upgrade notes — Java 21

What I changed in this repo
- Bumped Kotlin Gradle plugin in `build.gradle` to `1.9.20` (required for newer JVM targets).
- Set module `compileOptions` to target Java 21 in `app/build.gradle`.
- Set `kotlinOptions.jvmTarget = '21'` in `app/build.gradle`.

Why this was done
- The automated GitHub Copilot `generate_upgrade_plan` tool is not available for this account, so I prepared the project to target Java 21 manually.

Local steps you must take
1) Install JDK 21 (choose one method):
   - Homebrew (Temurin):
     ```bash
     brew install --cask temurin
     # or for a specific 21 build (if available):
     brew install --cask temurin21
     ```
   - SDKMAN (recommended for multiple JDKs):
     ```bash
     curl -s "https://get.sdkman.io" | bash
     source "$HOME/.sdkman/bin/sdkman-init.sh"
     sdk install java 21-open
     sdk use java 21-open
     ```
   - Adoptium / Eclipse Temurin installer from https://adoptium.net

2) Point Gradle to JDK 21 (one of these):
   - Set `JAVA_HOME` in your shell:
     ```bash
     export JAVA_HOME="/Library/Java/JavaVirtualMachines/<temurin-21>.jdk/Contents/Home"
     echo $JAVA_HOME
     ```
   - Or pin Gradle to a specific JDK by adding to `gradle.properties`:
     ```properties
     org.gradle.java.home=/Library/Java/JavaVirtualMachines/<temurin-21>.jdk/Contents/Home
     ```
     Replace the path above with the actual JDK 21 install path on your mac.

3) Verify Java and Gradle versions:
   ```bash
   java -version
   ./gradlew --version
   ```

4) Clean and build the project:
   ```bash
   ./gradlew clean assembleDebug
   ```

Notes and caveats
- Kotlin plugin was bumped to `1.9.20` — if you see Kotlin-related incompatibilities, consider pinning the plugin to a different 1.9.x patch.
- Android Gradle Plugin (AGP) is `8.13.0` — verify AGP + Gradle + JDK21 compatibility if build fails.
- Some Android language features still require desugaring; targeting JVM 21 does not automatically enable all new language features on older Android runtimes.

Next actions I can do for you
- Install JDK 21 on this machine (requires permission to run Homebrew/SDKMAN commands).
- Pin `org.gradle.java.home` in `gradle.properties` with detected JDK path.
- Run `./gradlew clean assembleDebug` and fix any compile errors.
- Create a branch and commit these changes and open a PR.

If you want me to continue, tell me which next action to take (install JDK 21, run build, or commit changes).