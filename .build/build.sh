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
  if [ ! -f "${KEYSTORE_PATH:-}" ] || [ ! -f "${KEYSTORE_PASSWORD:-}" ]; then
    echo "KEYSTORE_PATH and KEYSTORE_PASSWORD must be set" >&2
    exit 1
  fi

  ARGS+=(-v "$KEYSTORE_PATH":/keystore.jks:ro -e KEYSTORE_PASSWORD)
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

podman build --quiet -t "$IMAGE" .build/

podman "${ARGS[@]}" myapp-builder ./gradlew clean "$TASK"