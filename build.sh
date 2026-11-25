#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
ANDROID_SDK="${ANDROID_SDK:-$HOME/Library/Android/sdk}"
BUILD_TOOLS="$ANDROID_SDK/build-tools/35.0.0"
PLATFORM="$ANDROID_SDK/platforms/android-35/android.jar"

AAPT2="$BUILD_TOOLS/aapt2"
D8="$BUILD_TOOLS/d8"
ZIPALIGN="$BUILD_TOOLS/zipalign"
APKSIGNER="$BUILD_TOOLS/apksigner"

BUILD_DIR="$ROOT_DIR/build"
rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/gen" "$BUILD_DIR/obj" "$BUILD_DIR/apk" "$BUILD_DIR/res_flat"

echo "[1/6] Compiling resources with aapt2..."
"$AAPT2" compile --dir app/src/main/res -o "$BUILD_DIR/res_flat/res.zip"
"$AAPT2" link \
  -o "$BUILD_DIR/unsigned.apk" \
  -I "$PLATFORM" \
  --manifest app/src/main/AndroidManifest.xml \
  --java "$BUILD_DIR/gen" \
  "$BUILD_DIR/res_flat/res.zip" \
  --auto-add-overlay

echo "[2/6] Compiling Java sources..."
find app/src/main/java "$BUILD_DIR/gen" -name '*.java' > "$BUILD_DIR/sources.txt"
"$JAVA_HOME/bin/javac" \
  -source 11 \
  -target 11 \
  -encoding UTF-8 \
  -classpath "$PLATFORM" \
  -d "$BUILD_DIR/obj" \
  @"$BUILD_DIR/sources.txt"

echo "[3/6] Dexing bytecode with d8..."
find "$BUILD_DIR/obj" -name '*.class' > "$BUILD_DIR/classes.txt"
"$D8" \
  --min-api 26 \
  --lib "$PLATFORM" \
  --output "$BUILD_DIR/apk" \
  $(cat "$BUILD_DIR/classes.txt")

echo "[4/6] Packaging and aligning APK..."
zip -j -u "$BUILD_DIR/unsigned.apk" "$BUILD_DIR/apk/classes.dex"
"$ZIPALIGN" -f -p 4 "$BUILD_DIR/unsigned.apk" "$BUILD_DIR/aligned.apk"

KEYSTORE="$ROOT_DIR/debug.keystore"
if [ ! -f "$KEYSTORE" ]; then
  echo "[5/6] Generating debug keystore..."
  "$JAVA_HOME/bin/keytool" -genkeypair -v \
    -keystore "$KEYSTORE" \
    -storepass android \
    -alias androiddebugkey \
    -keypass android \
    -keyalg RSA \
    -keysize 2048 \
    -validity 10000 \
    -dname "CN=FotosPixel,O=Local,C=ES"
fi

echo "[5/6] Signing APK..."
"$APKSIGNER" sign \
  --ks "$KEYSTORE" \
  --ks-pass pass:android \
  --key-pass pass:android \
  --out "$BUILD_DIR/fotospixel.apk" \
  "$BUILD_DIR/aligned.apk"

echo "[6/6] Checking ADB device for installation..."
if adb get-state >/dev/null 2>&1; then
  adb install -r "$BUILD_DIR/fotospixel.apk"
  adb shell appops set com.fotospixel.transfer MANAGE_EXTERNAL_STORAGE allow || true
  adb shell appops set com.fotospixel.transfer POST_PROMOTED_NOTIFICATIONS allow || true
  adb shell pm grant com.fotospixel.transfer android.permission.READ_EXTERNAL_STORAGE || true
  adb shell pm grant com.fotospixel.transfer android.permission.WRITE_EXTERNAL_STORAGE || true
  adb shell pm grant com.fotospixel.transfer android.permission.POST_NOTIFICATIONS || true
  adb shell dumpsys deviceidle whitelist +com.fotospixel.transfer || true
  adb shell am start -n com.fotospixel.transfer/.MainActivity
else
  echo "No ADB device connected; APK built and ready at $BUILD_DIR/fotospixel.apk"
fi

echo "Done! APK built at $BUILD_DIR/fotospixel.apk"
