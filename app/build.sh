#!/usr/bin/env bash
set -euo pipefail
if [[ $# -ne 6 ]]; then echo 'Usage: bash build.sh JDK_DIR BUILD_TOOLS_DIR ANDROID_JAR OUTPUT_APK KEYSTORE KEY_ALIAS' >&2; exit 64; fi
export JAVA_HOME="$(realpath "$1")"; export PATH="$JAVA_HOME/bin:$PATH"
B=$(realpath "$2"); PLATFORM=$(realpath "$3"); DEST=$(realpath -m "$4"); KEY=$(realpath "$5"); ALIAS=$6
: "${CLANK_KEYSTORE_PASS:?Set CLANK_KEYSTORE_PASS in the environment}"
: "${CLANK_KEY_PASS:?Set CLANK_KEY_PASS in the environment}"
A=$(cd "$(dirname "$0")" && pwd); O=$(mktemp -d); trap 'rm -rf "$O"' EXIT
mkdir -p "$O/classes" "$O/dex" "$O/generated" "$(dirname "$DEST")"
"$B/aapt2" compile --dir "$A/res" -o "$O/resources.zip"
"$B/aapt2" link -I "$PLATFORM" --manifest "$A/AndroidManifest.xml" --java "$O/generated" -o "$O/unsigned.apk" "$O/resources.zip"
mapfile -d '' sources < <(find "$A/src" "$O/generated" -name '*.java' -print0)
javac -encoding UTF-8 --release 8 -classpath "$PLATFORM" -d "$O/classes" "${sources[@]}"
jar cf "$O/classes.jar" -C "$O/classes" .
"$B/d8" --lib "$PLATFORM" --min-api 28 --output "$O/dex" "$O/classes.jar"
python3 - "$O" <<'PY'
import hashlib,struct,sys,zlib,zipfile
from pathlib import Path
p=Path(sys.argv[1]); dex=sorted((p/'dex').glob('classes*.dex'))
assert dex and dex[0].name=='classes.dex'
with zipfile.ZipFile(p/'unsigned.apk','a',compression=zipfile.ZIP_DEFLATED) as z:
    for d in dex:
        b=d.read_bytes()
        assert len(b)>=112 and b[:4]==b'dex\n' and b[7]==0
        assert struct.unpack_from('<I',b,32)[0]==len(b)
        assert b[12:32]==hashlib.sha1(b[32:]).digest()
        assert struct.unpack_from('<I',b,8)[0]==zlib.adler32(b[12:])&0xffffffff
        z.write(d,d.name);print(d.name,len(b),'DEX integrity PASS')
PY
for d in "$O/dex"/classes*.dex; do "$B/dexdump" "$d"; done > "$O/dexdump.txt"
for c in MainActivity Vault Framed Exports Backup Transcript; do grep -Fq "Lgroup/backgroundgremlin/clankcon/$c;" "$O/dexdump.txt"; done
"$B/zipalign" -f 4 "$O/unsigned.apk" "$O/aligned.apk"
"$B/apksigner" sign --ks "$KEY" --ks-key-alias "$ALIAS" --ks-pass env:CLANK_KEYSTORE_PASS --key-pass env:CLANK_KEY_PASS --out "$O/signed.apk" "$O/aligned.apk"
"$B/zipalign" -c 4 "$O/signed.apk"
"$B/apksigner" verify --verbose --print-certs "$O/signed.apk"
"$B/aapt2" dump badging "$O/signed.apk"
unzip -t "$O/signed.apk"
cp "$O/signed.apk" "$DEST"
sha256sum "$DEST"
