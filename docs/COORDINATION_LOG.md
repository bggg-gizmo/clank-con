# Coordination log

## 2026-11-24 — Initialization
- Owner-provided repository URL: bggg-gizmo/clank-con. GitHub verified private, empty, default main, write access.
- User approved all eight named providers, all seven export formats, all nine retrieval categories, both storage modes, CRT UI, and page-based workflows. No LFS explicitly required.
- Authentication architecture and icon undecided. No application implementation or signed APK yet.
- Research confirms Android Custom Tabs share browser state without full DOM access; embedded WebView has separate login state and Google OAuth limitations. No live provider tests performed.
- Build archives known by name: OpenJDK17U-jdk_x64_linux_hotspot_17.0.20.1_1.tar.gz, build-tools_r36_linux.zip, platform-36_r02.zip, platform-tools-latest-linux.zip. Container listing attempt returned ClientError; contents, checksums, tool execution and device availability unverified.
- Next: user authentication and icon decision; confirm tool availability; establish provider capability matrix and safe live capture acceptance; implement and test native vault and exports; direct APK build, actual UI review and release verification.
- No GitHub Actions, hosted build minutes or LFS used. No private chat content or credentials added.

## 2026-11-24 — Authentication research and toolchain verification preparation
- User clarified native Android app usage and successful ChatGPT browsing through DuckDuckGo Android. Login method remains unspecified; user directed work to continue rather than repeat questions.
- Read existing engineering contract and requirements. All eight providers, all formats, full retrieval goals, storage modes and CRT workflow remain intact.
- Added docs/AUTHENTICATION_RESEARCH.md with sources, provider matrix, session isolation, WebView-first investigation, GeckoView alternative, and explicit live acceptance protocol. This is a candidate selection, not a validated login implementation.
- Added scripts/check-direct-toolchain.sh to exercise supplied extracted JDK, AAPT2, D8, zipalign, apksigner and Android API JAR, validate DEX checksums and sign/verify a disposable local test APK. Script has not been executed; no production signing key is generated or committed.
- Container execution repeatedly returned ClientError, including a minimal pwd call. Therefore no filesystem inspection, build-tool extraction, compilation, app rendering, or Android live testing could be performed. No production APK exists. Do not report a successful build.
- GitHub-only changes are on main. No LFS, Gradle, SDK installation, paid actions, hosted build, provider credentials, or private conversation data used.

## 2026-11-24 — Environment recovered; connection diagnostic built
- User reattached build archives. Container accessible; existing platform-36_r02.zip also present. Extracted supplied tools only; no new SDK installation.
- Actual direct compilation, D8 verification, APK linking, alignment and signing passed. ADB has no attached device.
- Added separate native connection diagnostic under probes/connection, with eight provider entries and two process-isolated persistent profile candidates. It does not extract chats or claim provider support. Compiled and verified APK delivered in conversation; production APK not built.
- Source Git blob identities match the compiled local files. Build results and limitations recorded in docs/CONNECTION_PROBE_VERIFICATION.md. Initial javac encoding failure resolved with UTF-8; deprecated API warnings remain.
- Next gate is actual device launch, provider sign-in and profile isolation. All approved production features remain in scope; production icon and signing remain undecided. No LFS, Gradle, SDK installation, paid Actions or hosted builds.

## 2026-11-24 — DuckDuckGo source review
- User handles physical-device tests and prefers avoiding manual password entry. Reviewed upstream browser popup lifecycle, cookie compatibility, system autofill and WebAuthn eligibility directly. Findings and source references in docs/DUCKDUCKGO_BROWSER_REVIEW.md.
- Existing DuckDuckGo session cannot be transferred to Clank-Con; external browser access and system autofill remain candidates with explicit limits. Keep existing diagnostic unchanged during test; no new build or provider acceptance claim.

## Connection diagnostic v0.0.2
- User authorized build following DuckDuckGo review. Added side-by-side native WebView diagnostic in probes/connection-v2; v1 retained. External DuckDuckGo launch, system autofill, popup stack, per-window cookie setting and error reporting implemented. Direct APK built and static verification passed; source blob identities match. See docs/CONNECTION_PROBE_V2_VERIFICATION.md.
- User performs physical device tests; no device attached here. No provider acceptance, full retrieval or production release claim. APK delivered in conversation. All approved production scope unchanged. No Gradle, SDK installation, LFS or paid Actions.

## Clank-Con v0.1.0 — local archival application
- User verified DuckDuckGo opens and sign-in succeeds, but no session returns to Clank-Con. Explained actual Android session boundary. User approved robot-taking-notes launcher icon and directed remaining implementation.
- Built native local archiver with prior browser workflows, explicit text/file import/share/PROCESS_TEXT, vault, all seven export writers, ChatGPT supplied graph parsing/branch explorer, ZIP originals and attachment expansion, direct-export staging, portable encrypted backup/restore and paged navigation. Source and verification logs committed as ordinary Git objects in 0044297eb9e8d36439db5cf9df8725d858d81331, 18 byte-identity-verified blobs. All prior probes retained. 13 host tests pass. APK direct build verified; supplied in conversation. See docs/V0.1.0_VERIFICATION.md.
- No device attached; user handles physical testing. External sign-in does not grant history access. Live history retrieval, all provider-specific semantic adapters, temporary browser profiles, production signing, runtime and seven-format verification remain outstanding. No scope removed. No Gradle, SDK installation, LFS, paid Actions, credentials or conversation records committed.

## 2026-11-25 — Passkey device evidence and release acceptance
- User clarified that successful ChatGPT login in DuckDuckGo used a passkey. External passkey login verified by user report only; no in-app session or live retrieval. Reviewed Android official WebAuthn/Credential Manager origin and privileged caller requirements. Added docs/RELEASE_ACCEPTANCE.md to record authentication, retrieval, export, vault, upgrade and direct-build gates. No new APK or application code change; all scope retained.

## 2026-11-25 — Exporter review and v0.1.1
- User directed actual source review and implementation. Inspected AIExporter, ACE, thekester/chatgpt-export, ThierryAbalea/chatgpt-conversation-export and yanglinfang parser code/manifests, plus ChatSave and ChatGPT Helper documentation. See docs/V0.1.1_EXPORTER_REVIEW.md for file identities and mechanisms.
- Implemented ACE v1 and AI Exporter-compatible offline transcript recognition/rendering with ordered content and source metadata, original JSON preserved. Seven source/build files byte-verified and committed in 220a2e130a110dfe7962be43093b21d1feec3862. Built v0.1.1 with same package, signer and existing icon/vault. 20 host tests passed; static APK/DEX verification passed. Device acceptance pending. Live provider account capture not implemented; no credential handling or browser-session transfer. Existing requirements retained; no Gradle, SDK install, LFS or paid Actions.

## 2026-11-25 — README and MIT licensing
- User requested a redesigned README and full MIT open-source license. Added root README with original CRT banner adapted from project robot icon, feature matrix, accurate current limits, workflows, direct build instructions, verification, roadmap and source links. Added unmodified MIT license text naming Background Gremlin Group (2026). Repository visibility remains private; no public release or visibility change authorized. Existing technical README and application code unchanged. User reported later passkey option was not offered; live authentication still pending. No builds or paid actions performed.

## README presentation update
- Applied professional section headings, 18 Shields.io badges, native Mermaid application and build flowcharts, and linked repository structure directly to main. Removed redundant checklist presentation; authoritative requirements and acceptance documentation remain intact. Closed superseded documentation PR. No application changes or CI execution.

## Sponsorship documentation
- Added optional Monero support to the README and a dedicated support page, with a repository-hosted custom funding URL. Regenerated the supplied QR as a scalable vector; the encoded payment URI matches the original, and the committed SVG blob hash matches the locally scanned SVG. No payment processing, application changes or CI execution.
