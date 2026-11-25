#!/usr/bin/env bash
# Read-only checks of supplied, extracted build tools. No installation or downloads.
set -euo pipefail
if [[ $# -ne 3 ]]; then
  printf 'Usage: bash %s JDK_DIRECTORY BUILD_TOOLS_DIRECTORY ANDROID_JAR\n' "$0" >&2
  exit 64
fi
jdk=$(realpath "$1")
tools=$(realpath "$2")
platform=$(realpath "$3")
export JAVA_HOME="$jdk"
export PATH="$jdk/bin:$PATH"
for binary in "$jdk/bin/java" "$jdk/bin/javac" "$jdk/bin/jar" "$jdk/bin/keytool" "$tools/aapt2" "$tools/d8" "$tools/zipalign" "$tools/apksigner"; do
  if [[ ! -f "$binary" || ! -x "$binary" ]]; then
    printf 'Missing or non-executable tool: %s\n' "$binary" >&2
    exit 1
  fi
done
[[ -f "$platform" ]] || { printf 'Missing Android API reference: %s\n' "$platform" >&2; exit 1; }
"$jdk/bin/java" -version
version=$("$jdk/bin/javac" -version 2>&1)
printf '%s\n' "$version"
[[ "$version" =~ ^javac[[:space:]]17([.]|$) ]] || { echo 'JDK 17 required' >&2; exit 1; }
"$tools/aapt2" version
"$tools/d8" --version
"$tools/apksigner" version
tmp=$(mktemp -d)
trap 'rm -rf "$tmp"' EXIT
"$jdk/bin/jar" tf "$platform" > "$tmp/platform.list"
grep -Fxq 'android/app/Activity.class' "$tmp/platform.list"
grep -Fxq 'android/webkit/WebView.class' "$tmp/platform.list"
grep -Fxq 'android/security/keystore/KeyGenParameterSpec.class' "$tmp/platform.list"
printf 'public final class ClankConToolchainProbe { public static int value() { return 36; } }\n' > "$tmp/ClankConToolchainProbe.java"
"$jdk/bin/javac" --release 8 -d "$tmp" "$tmp/ClankConToolchainProbe.java"
mkdir "$tmp/dex"
"$tools/d8" --lib "$platform" --min-api 28 --output "$tmp/dex" "$tmp/ClankConToolchainProbe.class"
test -s "$tmp/dex/classes.dex"
python3 - "$tmp/dex/classes.dex" <<'PY'
import hashlib, struct, sys, zlib
from pathlib import Path
data = Path(sys.argv[1]).read_bytes()
if len(data) < 112 or data[:4] != b'dex\n' or data[7] != 0:
    raise SystemExit('Invalid DEX magic/header')
if struct.unpack_from('<I', data, 32)[0] != len(data):
    raise SystemExit('Invalid DEX file size')
if data[12:32] != hashlib.sha1(data[32:]).digest():
    raise SystemExit('Invalid DEX SHA-1 signature')
if struct.unpack_from('<I', data, 8)[0] != zlib.adler32(data[12:]) & 0xffffffff:
    raise SystemExit('Invalid DEX Adler-32 checksum')
print('D8 output integrity: PASS')
PY
# Check zipalign and apksigner using an actual locally linked, signed test APK.
printf '<manifest xmlns:android="http://schemas.android.com/apk/res/android" package="local.clankcon.toolchainprobe"><uses-sdk android:minSdkVersion="28" android:targetSdkVersion="36"/><application android:label="Toolchain Probe"/></manifest>\n' > "$tmp/AndroidManifest.xml"
"$tools/aapt2" link -I "$platform" --manifest "$tmp/AndroidManifest.xml" -o "$tmp/unsigned.apk"
python3 - "$tmp/unsigned.apk" "$tmp/dex/classes.dex" <<'PY'
import sys, zipfile
with zipfile.ZipFile(sys.argv[1], 'a', compression=zipfile.ZIP_DEFLATED) as apk:
    apk.write(sys.argv[2], 'classes.dex')
PY
"$tools/zipalign" -f 4 "$tmp/unsigned.apk" "$tmp/aligned.apk"
"$jdk/bin/keytool" -genkeypair -keystore "$tmp/ephemeral.p12" -storetype PKCS12 -storepass localprobe -keypass localprobe -alias probe -keyalg RSA -keysize 2048 -validity 1 -dname 'CN=Disposable Local Toolchain Probe' >/dev/null 2>&1
"$tools/apksigner" sign --ks "$tmp/ephemeral.p12" --ks-pass pass:localprobe --key-pass pass:localprobe --out "$tmp/signed.apk" "$tmp/aligned.apk"
"$tools/zipalign" -c 4 "$tmp/signed.apk"
"$tools/apksigner" verify --verbose "$tmp/signed.apk"
"$tools/aapt2" dump badging "$tmp/signed.apk"
sha256sum "$platform" "$tmp/dex/classes.dex" "$tmp/signed.apk"
printf 'Direct toolchain probe passed. Disposable APK and key will be deleted. This is not the Clank-Con application or device acceptance.\n'
