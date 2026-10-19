#!/usr/bin/env bash

set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."

ENGINE="${CONTAINER_ENGINE:-podman}"
BUILD_TYPE="debug"
SIGN=false
USAGE="Usage: $0 [-r|--release] [-s|--sign] [-h|--help]"

command -v podman >/dev/null || { echo "podman is not installed" >&2; exit 1; }

# TODO: handle combined options like -rs
while [[ $# -gt 0 ]]; do
  case "$1" in
    -r|--release)
      BUILD_TYPE="release"
      ;;
    -s|--sign)
      SIGN=true
      ;;
    -h|--help)
      echo "$USAGE"
      exit 0
      ;;
    *)
      echo "$USAGE" >&2
      exit 1
      ;;
  esac
  shift
done

ARGS=(run --rm
  -v "$PWD":/project:Z
  --userns=keep-id
)

if [ "$SIGN" = true ] && [ "$BUILD_TYPE" = "debug" ]; then
  echo "Cannot sign debug builds" >&2
  exit 1
fi

if [ "$SIGN" = true ]; then
  if [ -z "${KEYSTORE_PASSWORD:-}" ]; then
    read -rs -p "Keystore password: " KEYSTORE_PASSWORD
    echo
    export KEYSTORE_PASSWORD
  fi

  if [ ! -f "${KEYSTORE_PATH:-}" ]; then
    echo "KEYSTORE_PATH must point to an existing keystore file" >&2
    exit 1
  fi

  if [ -z "${KEY_ALIAS:-}" ]; then
    echo "KEY_ALIAS must be set" >&2
    exit 1
  fi

  if [ -z "${KEYSTORE_PASSWORD:-}" ]; then
    echo "Keystore password is empty" >&2
    exit 1
  fi

  ARGS+=(-v "$KEYSTORE_PATH:/keystore.jks:ro,Z" -e KEYSTORE_PASSWORD -e KEY_ALIAS)
fi

case "$BUILD_TYPE" in
  debug)
    TASK="assembleDebug"
    ;;
  release)
    TASK="assembleRelease"
    ;;
  *)
    echo "Unknown build type: $BUILD_TYPE" >&2
    exit 1
    ;;
esac

IMAGE="localhost/myapp-builder"

rm -rf app/.cxx app/build .gradle

podman build --quiet -t "$IMAGE" .build/

podman "${ARGS[@]}" "$IMAGE" ./gradlew clean "$TASK"
#podman "${ARGS[@]}" "$IMAGE" sh -c 'echo "pw set: ${KEYSTORE_PASSWORD:+yes}"; ls -l /keystore.jks'