#!/usr/bin/env bash
# Package rename only. Run from project root: ./rename_package.sh
set -euo pipefail
cd "$(dirname "$0")"

# ============ CONFIG ============
OLD_PACKAGE="" # e.g. "com.tomato.drawingsteps" — empty = read from app/build.gradle.kts
NEW_PACKAGE="com.stepbystepdrawing.HowToDrawLabubu"
# ================================
[[ -n "${1:-}" ]] && NEW_PACKAGE="$1"

[[ -z "$OLD_PACKAGE" ]] && OLD_PACKAGE="$(grep -E '^\s*(namespace|applicationId)\s*=' app/build.gradle.kts | head -1 | sed -E 's/.*["'"'"']([^"'"'"']+)["'"'"'].*/\1/')"
[[ -n "$OLD_PACKAGE" ]] || exit 1
[[ "$OLD_PACKAGE" == "$NEW_PACKAGE" ]] && exit 0

OLD_ROOT="${OLD_PACKAGE//.//}"
NEW_ROOT="${NEW_PACKAGE//.//}"
OLD_ESC="${OLD_PACKAGE//./\\.}"
NEW_ESC="${NEW_PACKAGE//&/\\&}"
SED=(sed -i); [[ "$(uname)" == Darwin ]] && SED=(sed -i '')

move() {
  local base="$1"
  local old="$PWD/$base/$OLD_ROOT" new="$PWD/$base/$NEW_ROOT"
  [[ -d "$old" ]] || return 0
  [[ -d "$new" ]] && rm -rf "$new"
  mkdir -p "${new%/*}"
  mv "$old" "$new"
  local p="${OLD_ROOT%/*}"
  while [[ -n "$p" && -d "$PWD/$base/$p" ]]; do
    rmdir "$PWD/$base/$p" 2>/dev/null || break
    p="${p%/*}"
  done
}

move app/src/main/java
move app/src/test/java
move app/src/androidTest/java

while IFS= read -r -d '' f; do
  grep -q "$OLD_PACKAGE" "$f" 2>/dev/null || continue
  "${SED[@]}" "s/$OLD_ESC/$NEW_ESC/g" "$f"
done < <(find "$PWD" -type f \( -name "*.kt" -o -name "*.kts" -o -name "*.java" -o -name "*.xml" -o -name "*.properties" -o -name "*.pro" -o -name "*.json" \) ! -path "*/.git/*" ! -path "*/build/*" ! -path "*/.gradle/*" ! -name "rename_package.sh" -print0 2>/dev/null)
