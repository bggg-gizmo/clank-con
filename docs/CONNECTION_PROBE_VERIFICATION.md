# Connection diagnostic — build verification

Date: 2026-11-24 local / 2026-11-25 UTC.

The previously failing execution container became accessible. The supplied JDK, build tools, platform-36 Android JAR and ADB were extracted and used directly. No SDK installation or tool download.

## Toolchain results
- OpenJDK/javac 17.0.20.1: PASS.
- AAPT2 2.20-13193326: binary manifest and resource linking PASS.
- D8 8.10.9-dev: disposable toolchain probe classes.dex (700 bytes), DEX magic, file length, SHA-1 and Adler-32 PASS.
- APK alignment and signature v3 verification: PASS.
- ADB executable: PASS; no devices attached. No install/launch claim.

## Clank-Con Connection Test 0.0.1-probe
Source: probes/connection. Package group.backgroundgremlin.clankcon.probe, min API 28, target API 36. Eight provider website destinations, two independent process-specific WebView data directory suffixes, visible origin, HTTPS-only top-level navigation, TLS rejection, file/content access disabled, no debugging or JavaScript bridges, no chat extraction or credential/token logging. External-browser fallback opens the provider's initial website only; it does not transfer authenticated sessions. Third-party cookies are disabled. Real compatibility and profile isolation are unverified. Profiles are persistent; this probe does not implement temporary-session cleanup or the vault. Default system launcher icon; production icon remains undecided.

First compilation failed due to the host's US-ASCII source default; explicit javac -encoding UTF-8 fixed it. Rebuild completed successfully. Deprecated Android API warnings remain, including legacy navigation/inset calls. No device attached, so visual appearance and runtime behavior are not verified.

- DEX 13,800 bytes; magic, file size, SHA-1, Adler-32 PASS.
- Expected application class descriptors found via dexdump.
- ZIP integrity PASS, alignment PASS, apksigner verify PASS (v3).
- AAPT2 badging confirms expected package, launcher activity and API levels.
- APK size: 16,787 bytes.
- APK SHA-256: 859db619c1fed6144b5a59dd2994922b20604388323b702cd546157f900a9b32.
- Disposable diagnostic certificate SHA-256: 5b8a0871de3f87d9fccf8b14ed71e2747c1a7cd1901a70e8b210dbe909ac47a2. Key discarded after build; subsequent probe builds require uninstall before install unless the same signer is available. This is not a production signing scheme.
- Local compiled source Git blob hashes independently matched the repository copies: manifest d2c6b70a2273ac8417d00b1df3b7b4e73fe9b6b2; application 06a0727a284cec559a1c7505a534be1861f30ac3; activity 3a17c059c3f9d1c8d97307e516a21741c86587b1; build script 979830ce5559d3b0c7af09f558d97c5f0ab38680.

## Acceptance outstanding
Use a disposable/test account first. Verify install/launch, official origin, provider authentication, MFA, reconnect and actual cookie isolation between A/B. Do not bypass provider restrictions. Clear diagnostic app storage to remove both sessions. All capture, export, vault, backup, temporary profiles and production UI remain outstanding and in scope. APK provided in the conversation, not committed as binary to Git. No Gradle, LFS, paid Actions or hosted builds used.
