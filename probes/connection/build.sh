#!/usr/bin/env bash
set -euo pipefail
if [[ $# != 4 ]]; then echo 'Usage: bash build.sh JDK_DIR BUILD_TOOLS_DIR ANDROID_JAR OUTPUT_APK' >&2; exit 64; fi
export JAVA_HOME="$(realpath "$1")"; export PATH="$JAVA_HOME/bin:$PATH"
B=$(realpath "$2"); P=$(realpath "$3"); DEST=$(realpath -m "$4"); A=$(cd "$(dirname "$0")" && pwd); O=$(mktemp -d); trap 'rm -rf "$O"' EXIT
mkdir -p "$O/classes" "$O/dex" "$(dirname "$DEST")"
"$B/aapt2" link -I "$P" --manifest "$A/AndroidManifest.xml" -o "$O/unsigned.apk"
mapfile -d '' sources < <(find "$A/src" -name '*.java' -print0)
javac -encoding UTF-8 --release 8 -classpath "$P" -d "$O/classes" "${sources[@]}"
jar cf "$O/classes.jar" -C "$O/classes" .
"$B/d8" --lib "$P" --min-api 28 --output "$O/dex" "$O/classes.jar"
python3 - "$O" <<'PY'
import hashlib,struct,sys,zlib,zipfile
from pathlib import Path
p=Path(sys.argv[1]); d=p/'dex/classes.dex'; b=d.read_bytes()
if len(b)<112 or b[:4]!=b'dex\n' or b[7]!=0 or struct.unpack_from('<I',b,32)[0]!=len(b) or b[12:32]!=hashlib.sha1(b[32:]).digest() or struct.unpack_from('<I',b,8)[0]!=(zlib.adler32(b[12:])&0xffffffff): raise SystemExit('DEX integrity failure')
with zipfile.ZipFile(p/'unsigned.apk','a',compression=zipfile.ZIP_DEFLATED) as z:z.write(d,'classes.dex')
print('DEX integrity PASS',len(b))
PY
"$B/dexdump" "$O/dex/classes.dex" > "$O/dexdump.txt"
for descriptor in ProbeApplication ProbeActivity 'ProbeActivity$BrowserA' 'ProbeActivity$BrowserB' 'ProbeActivity$BrowserActivity'; do grep -Fq "Lgroup/backgroundgremlin/clankcon/probe/$descriptor;" "$O/dexdump.txt"; done
echo 'Expected application classes present in DEX'
"$B/zipalign" -f 4 "$O/unsigned.apk" "$O/aligned.apk"
keytool -genkeypair -keystore "$O/probe-only.p12" -storetype PKCS12 -storepass localprobe -keypass localprobe -alias diagnostic -keyalg RSA -keysize 2048 -validity 365 -dname 'CN=Clank-Con Diagnostic Only'
"$B/apksigner" sign --ks "$O/probe-only.p12" --ks-pass pass:localprobe --key-pass pass:localprobe --out "$O/signed.apk" "$O/aligned.apk"
"$B/zipalign" -c 4 "$O/signed.apk"
"$B/apksigner" verify --verbose --print-certs "$O/signed.apk"
"$B/aapt2" dump badging "$O/signed.apk"
unzip -t "$O/signed.apk"
cp "$O/signed.apk" "$DEST"
sha256sum "$DEST"
