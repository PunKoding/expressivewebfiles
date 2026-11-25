#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
VERSION_FILE="$ROOT_DIR/VERSION"
MANIFEST_FILE="$ROOT_DIR/app/src/main/AndroidManifest.xml"

if [ ! -f "$VERSION_FILE" ]; then
  echo "1.0.0" > "$VERSION_FILE"
fi

CURRENT="$(tr -d ' \t\r\n' < "$VERSION_FILE")"
if ! echo "$CURRENT" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+$'; then
  echo "Invalid version in $VERSION_FILE: '$CURRENT'" >&2
  exit 1
fi

IFS='.' read -r MAJOR MINOR PATCH <<< "$CURRENT"
compute_code() {
  echo $(( $1 * 10000 + $2 * 100 + $3 ))
}

CMD="${1:-show}"
case "$CMD" in
  show|current)
    CODE=$(compute_code "$MAJOR" "$MINOR" "$PATCH")
    echo "v$CURRENT (versionCode=$CODE)"
    exit 0
    ;;
  fix|patch)
    PATCH=$((PATCH + 1))
    ;;
  feature|minor)
    MINOR=$((MINOR + 1))
    PATCH=0
    ;;
  major)
    MAJOR=$((MAJOR + 1))
    MINOR=0
    PATCH=0
    ;;
  set)
    TARGET="${2:-}"
    if ! echo "$TARGET" | grep -Eq '^[0-9]+\.[0-9]+\.[0-9]+$'; then
      echo "Usage: $0 set MAJOR.MINOR.PATCH" >&2
      exit 1
    fi
    IFS='.' read -r MAJOR MINOR PATCH <<< "$TARGET"
    ;;
  -h|--help|help)
    cat <<EOF
Uso: ./version.sh [comando]

Comandos:
  show              Muestra la versión actual y su versionCode (por defecto)
  fix | patch       Sube +0.0.1 para arreglos de errores (p. ej. 1.0.0 -> 1.0.1)
  feature | minor   Sube +0.1.0 para nuevas funciones    (p. ej. 1.0.1 -> 1.1.0)
  major             Sube +1.0.0 para cambios mayores     (p. ej. 1.1.0 -> 2.0.0)
  set X.Y.Z         Fija una versión exacta
EOF
    exit 0
    ;;
  *)
    echo "Unknown command: $CMD (try --help)" >&2
    exit 1
    ;;
esac

NEW_VERSION="${MAJOR}.${MINOR}.${PATCH}"
NEW_CODE=$(compute_code "$MAJOR" "$MINOR" "$PATCH")

printf '%s\n' "$NEW_VERSION" > "$VERSION_FILE"

python3 - "$MANIFEST_FILE" "$NEW_VERSION" "$NEW_CODE" <<'PY'
import re, sys
path, vname, vcode = sys.argv[1], sys.argv[2], sys.argv[3]
with open(path, 'r', encoding='utf-8') as f:
    text = f.read()
text = re.sub(r'android:versionCode="[^"]*"', f'android:versionCode="{vcode}"', text, count=1)
text = re.sub(r'android:versionName="[^"]*"', f'android:versionName="{vname}"', text, count=1)
with open(path, 'w', encoding='utf-8') as f:
    f.write(text)
PY

echo "Updated version: v$CURRENT -> v$NEW_VERSION (versionCode=$NEW_CODE)"
