#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

usage() {
  cat <<'EOF'
Uso: ./build.sh [opciones]

Compila y firma Expressive Web Files (build/fotospixel.apk).
En terminal interactiva te preguntará si esta compilación es una nueva versión
para añadirla al histórico (apks/history/) y si quieres instalarla por ADB.

Opciones:
  --install              Instala por ADB sin preguntar
  --no-install           No instala por ADB
  --history              Guarda el APK en apks/history/ sin subir versión
  --no-history           No añade este APK a apks/history/
  --bump <tipo>          Sube versión (fix|patch|feature|minor|major) y guarda en histórico
  -h, --help             Muestra esta ayuda
EOF
}

INSTALL_MODE=""
HISTORY_MODE=""
BUMP_MODE=""

if [ "${SKIP_ADB_INSTALL:-0}" = "1" ]; then
  INSTALL_MODE="no"
fi

while [ "$#" -gt 0 ]; do
  case "$1" in
    -h|--help|help)
      usage
      exit 0
      ;;
    --install)
      INSTALL_MODE="yes"
      ;;
    --no-install)
      INSTALL_MODE="no"
      ;;
    --history)
      HISTORY_MODE="yes"
      ;;
    --no-history)
      HISTORY_MODE="no"
      ;;
    --bump)
      shift
      if [ "$#" -eq 0 ]; then
        echo "Falta el tipo tras --bump (fix|feature|major)" >&2
        exit 1
      fi
      BUMP_MODE="$1"
      HISTORY_MODE="yes"
      ;;
    --bump=*)
      BUMP_MODE="${1#--bump=}"
      HISTORY_MODE="yes"
      ;;
    *)
      echo "Opción desconocida: $1 (usa --help)" >&2
      exit 1
      ;;
  esac
  shift
done

export JAVA_HOME="${JAVA_HOME:-/opt/homebrew/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home}"
ANDROID_SDK="${ANDROID_SDK:-$HOME/Library/Android/sdk}"
BUILD_TOOLS="$ANDROID_SDK/build-tools/35.0.0"
PLATFORM="$ANDROID_SDK/platforms/android-35/android.jar"

AAPT2="$BUILD_TOOLS/aapt2"
D8="$BUILD_TOOLS/d8"
ZIPALIGN="$BUILD_TOOLS/zipalign"
APKSIGNER="$BUILD_TOOLS/apksigner"

BUILD_DIR="$ROOT_DIR/build"
VERSION_FILE="$ROOT_DIR/VERSION"
VERSION_NAME="1.0.0"
if [ -f "$VERSION_FILE" ]; then
  VERSION_NAME="$(tr -d ' \t\r\n' < "$VERSION_FILE")"
fi

SAVE_HISTORY=0
if [ -n "$BUMP_MODE" ]; then
  "$ROOT_DIR/version.sh" "$BUMP_MODE"
  SAVE_HISTORY=1
elif [ "$HISTORY_MODE" = "yes" ]; then
  SAVE_HISTORY=1
elif [ "$HISTORY_MODE" = "no" ]; then
  SAVE_HISTORY=0
elif [ -t 0 ]; then
  echo "Versión actual: v${VERSION_NAME}"
  echo "¿Es una versión nueva para añadir al histórico (apks/history/)?"
  echo "  0 / Enter) No (compilar solo en build/fotospixel.apk sin añadir al histórico)"
  echo "  1)         Sí — Corrección de errores (+0.0.1) y guardar en histórico"
  echo "  2)         Sí — Nueva función          (+0.1)   y guardar en histórico"
  echo "  3)         Sí — Cambio mayor           (+1.0)   y guardar en histórico"
  echo "  4)         Sí — Guardar v${VERSION_NAME} actual en el histórico sin subir número"
  printf "Elige [0-4] (por defecto 0): "
  read -r H_CHOICE
  case "$H_CHOICE" in
    1)
      "$ROOT_DIR/version.sh" fix
      SAVE_HISTORY=1
      ;;
    2)
      "$ROOT_DIR/version.sh" feature
      SAVE_HISTORY=1
      ;;
    3)
      "$ROOT_DIR/version.sh" major
      SAVE_HISTORY=1
      ;;
    4|s|S|y|Y)
      SAVE_HISTORY=1
      ;;
    ""|0|n|N)
      SAVE_HISTORY=0
      ;;
    *)
      echo "Opción no reconocida; compilando sin añadir al histórico."
      SAVE_HISTORY=0
      ;;
  esac
fi

if [ -f "$VERSION_FILE" ]; then
  VERSION_NAME="$(tr -d ' \t\r\n' < "$VERSION_FILE")"
fi
IFS='.' read -r V_MAJOR V_MINOR V_PATCH <<< "$VERSION_NAME"
VERSION_CODE=$(( ${V_MAJOR:-1} * 10000 + ${V_MINOR:-0} * 100 + ${V_PATCH:-0} ))
echo "Building Expressive Web Files v${VERSION_NAME} (${VERSION_CODE})..."

rm -rf "$BUILD_DIR"
mkdir -p "$BUILD_DIR/gen/com/fotospixel/transfer" "$BUILD_DIR/obj" "$BUILD_DIR/apk" "$BUILD_DIR/res_flat"

cat > "$BUILD_DIR/gen/com/fotospixel/transfer/AppVersion.java" <<EOF
package com.fotospixel.transfer;

public final class AppVersion {
    public static final String NAME = "${VERSION_NAME}";
    public static final int CODE = ${VERSION_CODE};
    public static final String FULL = "v${VERSION_NAME} (${VERSION_CODE})";
    private AppVersion() {}
}
EOF

echo "[1/6] Compiling resources with aapt2..."
"$AAPT2" compile --dir app/src/main/res -o "$BUILD_DIR/res_flat/res.zip"
"$AAPT2" link \
  -o "$BUILD_DIR/unsigned.apk" \
  -I "$PLATFORM" \
  --manifest app/src/main/AndroidManifest.xml \
  --version-code "$VERSION_CODE" \
  --version-name "$VERSION_NAME" \
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

if [ "$SAVE_HISTORY" = "1" ]; then
  HISTORY_DIR="$ROOT_DIR/apks/history"
  mkdir -p "$HISTORY_DIR"
  BUILD_TS="$(date +%Y%m%d-%H%M%S)"
  GIT_SHA="$(git -C "$ROOT_DIR" rev-parse --short HEAD 2>/dev/null || echo nogit)"
  HISTORY_APK="$HISTORY_DIR/expressivewebfiles-v${VERSION_NAME}_${BUILD_TS}_${GIT_SHA}.apk"
  cp "$BUILD_DIR/fotospixel.apk" "$HISTORY_APK"
  echo "Archived APK in apks/history/$(basename "$HISTORY_APK")"
else
  echo "Histórico omitido (no marcada como nueva versión)."
fi

DO_INSTALL=1
if [ "$INSTALL_MODE" = "no" ]; then
  DO_INSTALL=0
elif [ "$INSTALL_MODE" = "yes" ]; then
  DO_INSTALL=1
elif [ -t 0 ]; then
  printf "¿Quieres instalar el APK por ADB en el dispositivo conectado? [S/n]: "
  read -r I_CHOICE
  case "$I_CHOICE" in
    n|N|no|NO)
      DO_INSTALL=0
      ;;
    *)
      DO_INSTALL=1
      ;;
  esac
fi

echo "[6/6] Checking ADB device for installation..."
if [ "$DO_INSTALL" = "0" ]; then
  echo "Instalación por ADB omitida; APK listo en $BUILD_DIR/fotospixel.apk"
elif adb get-state >/dev/null 2>&1; then
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
