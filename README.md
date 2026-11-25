<div align="center">

<img src="docs/assets/clank-con-banner.svg" alt="Clank-Con — Pull chats from clanks. Green CRT terminal and robot taking notes." width="100%">

# CLANK-CON

**Pull chats from clanks. Keep the originals. Own your archive.**

`ANDROID 9+` · `JAVA 17 TOOLCHAIN` · `MIT LICENSE` · `LOCAL FIRST`

[Features](#01--the-arsenal) · [Compatibility](#02--provider-matrix) · [How to use](#03--operator-workflow) · [Build](#05--build-it-yourself) · [Audit](#06--inspect-everything) · [License](#08--license)

**A Background Gremlin Group project**

</div>

---

> **Don't trust us. Audit and inspect our code for yourself.**

Clank-Con is a standalone Android conversation archiver built for people who want their own AI conversations under their own control. It combines a retro phosphor-green terminal interface with local encrypted storage, structured conversation import, branch inspection and seven export formats.

No hosted archive service. No Clank-Con account. No mandatory subscription. No Gradle. No deliberately hidden capture of credentials or automatic publication of conversations.

## System status

| Channel | State |
| :--- | :--- |
| Application | **v0.1.1 — device-test release** |
| Package | `group.backgroundgremlin.clankcon` |
| Android | API 28–36 build configuration (Android 9 minimum; target API 36) |
| Host tests | 20 passed in recorded v0.1.1 build |
| APK / DEX | Direct build, integrity, alignment and signature verified |
| Device acceptance | In progress |
| Live provider history retrieval | Outstanding — not advertised as working |

The current build is a working-code archival implementation under device testing, **not** a finished universal live-history downloader. Authentication, import, retrieval and export are distinct stages. Successful browser sign-in alone is not evidence of live retrieval.

[Read the v0.1.1 verification report](docs/V0.1.1_EXPORTER_REVIEW.md) · [Release acceptance gates](docs/RELEASE_ACCEPTANCE.md)

## 01 / The arsenal

| Module | Function |
| :--- | :--- |
| **SOURCE** | Eight provider destinations, independent persistent WebView profiles A/B, user-initiated popups, system autofill and external browser launch. |
| **IMPORT** | Android share sheet, text-selection action, pasted text, individual or multiple files and supplied archives. |
| **VAULT** | Encrypted originals and metadata, duplicate detection, local search and paged selection. |
| **REVIEW** | Inspect supplied content, original hashes, image previews, ChatGPT graph branches and derived branch records. |
| **EXPORT** | Seven output formats, selected records or entire imported selection, native destination picker and direct-export mode. |
| **BACKUP** | Passphrase-encrypted portable backup, authenticated restoration and conflict handling. |

### Seven export formats

| Format | Purpose |
| :--- | :--- |
| TXT | Readable UTF-8 transcript |
| PDF | Portable document rendering |
| JSON | Metadata and original bytes in Base64 |
| Markdown | Notes, code and portable text workflows |
| HTML | Browser-readable text archive |
| DOCX | Editable word-processing document |
| ZIP | Original files and SHA-256 integrity manifest |

JSON and ZIP preserve original bytes. Other formats render supplied text and metadata; binary assets are not embedded by those writers. Actual Android rendering and interoperability still require device tests. Ordinary exports are plaintext; use the encrypted backup function for protected portability.

## 02 / Provider matrix

Every provider below is a project target and has an official-site browser entry. All support generic, user-supplied file or text import. Structured formats from compatible exporters can be recognized independently of the chosen provider.

| Provider | Official website | Supplied native-format parsing | Live history |
| :--- | :--- | :--- | :--- |
| ChatGPT | [chatgpt.com](https://chatgpt.com/) | Conversation graph JSON / ZIP | Pending |
| Kimi | [kimi.com](https://www.kimi.com/) | Generic import | Pending |
| Claude | [claude.ai](https://claude.ai/) | Generic import | Pending |
| Gemini | [gemini.google.com](https://gemini.google.com/) | Generic import | Pending |
| Grok | [grok.com](https://grok.com/) | Generic import | Pending |
| DeepSeek | [chat.deepseek.com](https://chat.deepseek.com/) | Generic import | Pending |
| Perplexity | [perplexity.ai](https://www.perplexity.ai/) | Generic import | Pending |
| Microsoft Copilot | [copilot.microsoft.com](https://copilot.microsoft.com/) | Generic import | Pending |

### Structured interoperability

- Supplied ChatGPT conversation graphs, including explicit parent links, branch endpoints and source metadata.
- ACE schema v1: original roles, display labels, content, timestamps and source details.
- AI Exporter-compatible role/content JSON arrays: ordered text, model, time, supplied reasoning and citations.
- Generic files and ZIP archive members: original bytes retained without assuming a proprietary schema.

The provider label is not a verified account identity. No format is assumed to contain every server-side message, attachment, branch or project.

## 03 / Operator workflow

```text
   [ SOURCE / SHARE / FILE PICKER ]
                  |
                  v
          [ IMPORT ORIGINAL ]
                  |
                  v
       [ REVIEW + SELECT RECORDS ]
             /          \
            v            v
    [ LOCAL VAULT ]   [ DIRECT EXPORT ]
         |     |             |
         |     +-------------+
         v                   v
 [ ENCRYPTED BACKUP ]   [ 7 FILE FORMATS ]
```

1. Choose a provider or import an existing conversation file.
2. If using DuckDuckGo or another external browser, open a conversation and share its **actual selected content** or a supported file to Clank-Con. A link alone is not the conversation.
3. Review the supplied provider, account label and content.
4. Save it to the encrypted vault, or choose direct export without a retained vault record.
5. Search, select and export records. Use Backup / Restore to move an encrypted archive between installations.

Built-in browser login is separate from external browser login. DuckDuckGo retains its own session; Clank-Con cannot inherit it. External ChatGPT sign-in via passkey was observed in earlier device testing, but the option was not offered in a subsequent test. Independent passkey compatibility and live account retrieval remain unresolved.

## 04 / Privacy and integrity

| Layer | Implementation |
| :--- | :--- |
| Vault key | Android Keystore |
| Encryption | AES-256-GCM, authenticated 64 KiB frames |
| Backup key derivation | PBKDF2-HMAC-SHA256, 600,000 iterations, random salt |
| Stream integrity | Authenticated sequence and terminal marker |
| Original files | Byte preservation and SHA-256 integrity metadata |
| Storage | Local encrypted vault or user-selected export destination |
| Browser boundary | Separate app/profile sessions; no cookie or token transfer |

Clank-Con does not implement a separate vault PIN or biometric lock in the current build. Device security still matters. External websites can make their own network requests; local archiving does not make a provider's website offline.

Known limits: content search and rendering are capped at 16 MiB per original, with an explicit graph-parser limit. Larger originals remain available for intact JSON/ZIP export. ZIP expansion can partially succeed before a failed member; the original remains preserved. Backup restore handles ordinary failures, but process termination during the final merge can leave a partial merge.

See [technical documentation](app/README.md) and [verification notes](docs/V0.1.0_VERIFICATION.md) before relying on the application as your only backup.

## 05 / Build it yourself

**One native APK. Real DEX. No Gradle.**

```text
  ANDROID RESOURCES --------> AAPT2 -----> BINARY RESOURCES
                                              |
  JAVA SOURCE ----> JAVAC ----> D8 -----> classes.dex
                                              |
                                              v
                              PACKAGE -> ZIPALIGN -> APKSIGNER
                                              |
                                              v
                                 VERIFIED SIGNED APK
```

Requirements: an existing extracted JDK 17, Android build-tools r36, an API 36 `android.jar`, Bash, Python 3, ZIP utilities and a locally controlled signing keystore. No SDK installation is performed by the build scripts. The existing tools are supplied separately; they are not redistributed in this repository.

Clone this repository using an account with access:

```bash
git clone https://github.com/bggg-gizmo/clank-con.git
cd clank-con
```

Run the provided scripts with your local toolchain paths. Set the variables below to your actual local paths and signing alias before running the commands; supply signing passwords securely through the environment.

```bash
bash scripts/check-direct-toolchain.sh "$JDK_DIR" "$BUILD_TOOLS_DIR" "$ANDROID_JAR"

JAVA_HOME="$JDK_DIR" bash app/test-host.sh

bash app/build.sh \
  "$JDK_DIR" \
  "$BUILD_TOOLS_DIR" \
  "$ANDROID_JAR" \
  "$PWD/out/Clank-Con.apk" \
  "$KEYSTORE" \
  "$KEY_ALIAS"
```

The build requires `CLANK_KEYSTORE_PASS` and `CLANK_KEY_PASS`. Do not commit your signing credentials. Updates to an installed app require signing continuity; a newly generated signing key is not interchangeable with the existing development key.

The script checks every DEX file, required application classes, APK resources, ZIP integrity, alignment, signing certificate and SHA-256. Host tests do not replace on-device verification.

For the recorded v0.1.1 build:

```text
APK SHA-256
9078411107de845ecf75871d50d099a4ce52cde78cf7109e089b58e851f23d22

DEX: 112,748 bytes
HOST TESTS: 20 / 20 PASS
```

No APK release asset is currently published in this repository. Build from source or obtain the separately distributed test build through the project owner, and verify its hash.

## 06 / Inspect everything

```text
clank-con/
├── app/                       Native application
│   ├── src/                   Java source
│   ├── res/                   Android resources and robot icon
│   ├── tests/                 Host-side regression tests
│   ├── AndroidManifest.xml
│   ├── build.sh
│   └── test-host.sh
├── probes/                    Preserved connection diagnostics
├── scripts/                   Direct toolchain verification
├── verification/              Recorded build/test logs
├── docs/                      Research, acceptance and handoff
├── AGENTS.md                  Engineering contract
├── PROJECT_REQUIREMENTS.md    Complete approved scope
├── README.md
└── LICENSE
```

| Read | Why |
| :--- | :--- |
| [Application documentation](app/README.md) | Exact workflows, local storage and build details |
| [Project requirements](PROJECT_REQUIREMENTS.md) | Complete approved scope |
| [Release acceptance](docs/RELEASE_ACCEPTANCE.md) | Criteria for claiming working provider support |
| [Authentication research](docs/AUTHENTICATION_RESEARCH.md) | Browser and session constraints |
| [DuckDuckGo source review](docs/DUCKDUCKGO_BROWSER_REVIEW.md) | Popup, autofill and passkey compatibility findings |
| [Exporter source review](docs/V0.1.1_EXPORTER_REVIEW.md) | Inspected upstream projects, interoperability and tests |
| [Coordination log](docs/COORDINATION_LOG.md) | Development history and outstanding work |
| [Engineering contract](AGENTS.md) | Rules for contributors and development agents |

No code from the inspected upstream exporters or DuckDuckGo was incorporated. Their implementations and formats informed the research; Clank-Con's implementation is independently authored. Upstream projects and Android build tools retain their respective licenses.

## 07 / Mission checklist

- [x] Standalone native Android APK and direct DEX build
- [x] Retro CRT workflow and robot-taking-notes launcher icon
- [x] Eight provider entry points
- [x] Two persistent browser profile candidates
- [x] Individual and multiple supplied conversation imports
- [x] Encrypted searchable vault and direct exports
- [x] Seven export writers
- [x] Supplied ChatGPT graph and branch inspection
- [x] ACE v1 and role/content JSON interoperability
- [x] Supplied attachment and generated-image file preservation
- [x] Portable encrypted backup and restore
- [ ] Verified independent passkey sign-in and provider account identity
- [ ] Temporary sessions and expanded account management
- [ ] Live individual, selected and entire available history retrieval
- [ ] Live archived and project conversation coverage
- [ ] Live alternative response branches, attachments, generated images and metadata
- [ ] Remaining native provider-specific archive parsers
- [ ] Large-text processing, interrupted-restore hardening and complete device acceptance
- [ ] User-owned production signing continuity

Checked boxes mean implemented, not universally device-tested. Nothing on the outstanding list is being silently dropped.

Contributions are welcome when repository access is available. Use synthetic fixtures, preserve existing features, document verification and never include credentials or private chat data. No paid Actions, Git LFS or Gradle.

## 08 / License

**MIT — Copyright (c) 2026 Background Gremlin Group.**

Use, inspect, modify, redistribute, sublicense and sell copies under the terms of the [full MIT license](LICENSE). Retain the copyright and permission notices. The software comes without warranty.

Clank-Con is an independent project, not affiliated with any of the listed AI providers or browser vendors.

---

<div align="center">

**BACKGROUND GREMLIN GROUP**

`> END OF TRANSMISSION_`

*Your conversations. Your archives. Your device.*

</div>
