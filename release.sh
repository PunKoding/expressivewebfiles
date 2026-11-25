#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$ROOT_DIR"

usage() {
  cat <<'EOF'
Uso: ./release.sh [tipo] [notas] [--install] [--tag]

Prepara una nueva versión compilada, firmada y lista para publicar en GitHub Releases.
(No se ejecuta automáticamente con build.sh; úsalo solo cuando quieras sacar versión.)

Tipos:
  fix | patch     Sube +0.0.1 para corrección de errores
  feature | minor Sube +0.1   para nuevas funciones
  major           Sube +1.0   para cambios mayores
  current         Mantiene la versión actual en VERSION sin subirla

Opciones:
  --install       Instala también el APK por ADB tras compilar
  --tag           Crea el tag local en Git (vX.Y.Z) tras compilar
  -h, --help      Muestra esta ayuda

Si no pasas argumentos, se abrirá un menú interactivo para elegir el tipo y escribir notas.
EOF
}

BUMP_TYPE=""
NOTES=""
DO_INSTALL=0
CREATE_TAG=0

for arg in "$@"; do
  case "$arg" in
    -h|--help|help)
      usage
      exit 0
      ;;
    --install)
      DO_INSTALL=1
      ;;
    --tag)
      CREATE_TAG=1
      ;;
    fix|patch|feature|minor|major|current)
      if [ -z "$BUMP_TYPE" ]; then
        BUMP_TYPE="$arg"
      elif [ -z "$NOTES" ]; then
        NOTES="$arg"
      else
        NOTES="$NOTES $arg"
      fi
      ;;
    *)
      if [ -z "$NOTES" ]; then
        NOTES="$arg"
      else
        NOTES="$NOTES $arg"
      fi
      ;;
  esac
done

CURRENT_VER="$(tr -d ' \t\r\n' < "$ROOT_DIR/VERSION")"

if [ -z "$BUMP_TYPE" ]; then
  echo "Versión actual: v${CURRENT_VER}"
  echo "¿Qué tipo de versión quieres preparar para GitHub?"
  echo "  1) Corrección de errores (+0.0.1)"
  echo "  2) Nueva función          (+0.1)"
  echo "  3) Cambio mayor           (+1.0)"
  echo "  4) Mantener v${CURRENT_VER} sin subir"
  printf "Elige [1-4]: "
  read -r CHOICE
  case "$CHOICE" in
    1) BUMP_TYPE="fix" ;;
    2) BUMP_TYPE="feature" ;;
    3) BUMP_TYPE="major" ;;
    4) BUMP_TYPE="current" ;;
    *)
      echo "Opción inválida." >&2
      exit 1
      ;;
  esac

  if [ -z "$NOTES" ]; then
    printf "Notas breves para el Release (Enter para generar desde Git): "
    read -r NOTES
  fi
fi

if [ "$BUMP_TYPE" != "current" ]; then
  "$ROOT_DIR/version.sh" "$BUMP_TYPE"
fi

NEW_VER="$(tr -d ' \t\r\n' < "$ROOT_DIR/VERSION")"

if [ "$DO_INSTALL" = "1" ]; then
  "$ROOT_DIR/build.sh" --history --install
else
  "$ROOT_DIR/build.sh" --history --no-install
fi

RELEASE_DIR="$ROOT_DIR/apks/releases/v${NEW_VER}"
mkdir -p "$RELEASE_DIR"

APK_NAME="expressivewebfiles-v${NEW_VER}.apk"
RELEASE_APK="$RELEASE_DIR/$APK_NAME"
SHA_FILE="$RELEASE_APK.sha256"
NOTES_FILE="$RELEASE_DIR/expressivewebfiles-v${NEW_VER}.notes.md"

cp "$ROOT_DIR/build/fotospixel.apk" "$RELEASE_APK"

if command -v shasum >/dev/null 2>&1; then
  SHA_HASH="$(shasum -a 256 "$RELEASE_APK" | awk '{print $1}')"
else
  SHA_HASH="$(sha256sum "$RELEASE_APK" | awk '{print $1}')"
fi
printf '%s  %s\n' "$SHA_HASH" "$APK_NAME" > "$SHA_FILE"

LAST_TAG="$(git -C "$ROOT_DIR" describe --tags --abbrev=0 2>/dev/null || true)"
if [ -n "$LAST_TAG" ]; then
  GIT_LOG="$(git -C "$ROOT_DIR" log "${LAST_TAG}..HEAD" --pretty=format:'- %s (%h)' 2>/dev/null || true)"
else
  GIT_LOG="$(git -C "$ROOT_DIR" log -n 10 --pretty=format:'- %s (%h)' 2>/dev/null || true)"
fi

{
  echo "# Expressive Web Files v${NEW_VER}"
  echo ""
  if [ -n "$NOTES" ]; then
    echo "$NOTES"
    echo ""
  fi
  if [ -n "$GIT_LOG" ]; then
    echo "## Cambios incluidos"
    echo "$GIT_LOG"
    echo ""
  fi
  echo "SHA-256 (\`${APK_NAME}\`): \`${SHA_HASH}\`"
} > "$NOTES_FILE"

if [ "$CREATE_TAG" = "1" ]; then
  if git -C "$ROOT_DIR" rev-parse "v${NEW_VER}" >/dev/null 2>&1; then
    echo "El tag v${NEW_VER} ya existía en Git."
  else
    git -C "$ROOT_DIR" tag -a "v${NEW_VER}" -m "Release v${NEW_VER}"
    echo "Tag local creado: v${NEW_VER}"
  fi
fi

echo ""
echo "=================================================================="
echo "Release v${NEW_VER} listo para publicar en GitHub"
echo "=================================================================="
echo "APK Release : apks/releases/v${NEW_VER}/${APK_NAME}"
echo "SHA-256     : apks/releases/v${NEW_VER}/${APK_NAME}.sha256"
echo "Notas       : apks/releases/v${NEW_VER}/expressivewebfiles-v${NEW_VER}.notes.md"
echo ""
echo "Siguientes pasos:"
echo "  1. Haz commit y push de los cambios (incluido VERSION y AndroidManifest.xml) desde GitHub Desktop."
echo "  2. Abre: https://github.com/PunKoding/expressivewebfiles/releases/new?tag=v${NEW_VER}"
echo "  3. Arrastra ambos archivos:"
echo "       - ${RELEASE_APK}"
echo "       - ${SHA_FILE}"
echo "  4. Copia el contenido de ${NOTES_FILE} y pulsa 'Publish release'."
