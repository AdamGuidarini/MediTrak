#!/usr/bin/env bash
set -euo pipefail

FILE="gradle/wrapper/gradle-wrapper.properties"
MODE="${1:-verify}" # verify | fix

if [[ "$MODE" != "verify" && "$MODE" != "fix" ]]; then
  echo "Usage: $0 [verify|fix]"
  exit 1
fi

if [[ ! -f "$FILE" ]]; then
  echo "Missing $FILE"
  exit 1
fi

url_line="$(grep -E '^distributionUrl=' "$FILE" || true)"
if [[ -z "$url_line" ]]; then
  echo "distributionUrl is missing in $FILE"
  exit 1
fi

url_escaped="${url_line#distributionUrl=}"
distribution_url="${url_escaped//\\:/:}"
sha_url="${distribution_url}.sha256"

expected="$(curl -fsSL "$sha_url" | awk 'NR==1 {print $1}')"
if [[ ! "$expected" =~ ^[a-fA-F0-9]{64}$ ]]; then
  echo "Fetched checksum is invalid from $sha_url"
  exit 1
fi

expected="$(printf '%s' "$expected" | tr '[:upper:]' '[:lower:]')"
current="$(awk -F= '/^distributionSha256Sum=/{print $2; exit}' "$FILE" | tr '[:upper:]' '[:lower:]' || true)"

if [[ "$MODE" == "fix" ]]; then
  tmp_file="$(mktemp)"
  awk -v expected="$expected" '
    BEGIN { found=0 }
    /^distributionSha256Sum=/ {
      print "distributionSha256Sum=" expected
      found=1
      next
    }
    { print }
    END {
      if (found == 0) {
        print "distributionSha256Sum=" expected
      }
    }
  ' "$FILE" > "$tmp_file"
  mv "$tmp_file" "$FILE"
  echo "Updated distributionSha256Sum to $expected"
  exit 0
fi

if [[ "$current" != "$expected" ]]; then
  echo "Gradle wrapper checksum mismatch (or missing)."
  echo "Current:  ${current:-<missing>}"
  echo "Expected: $expected"
  echo "Run: $0 fix"
  exit 1
fi

echo "Gradle wrapper checksum is valid."
