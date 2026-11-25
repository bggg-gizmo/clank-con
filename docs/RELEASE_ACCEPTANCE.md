# Release acceptance contract

Updated 2026-11-25.

## Actual device evidence
The user confirmed the connection diagnostic opened DuckDuckGo, and ChatGPT sign-in succeeded using a passkey. No Clank-Con session appeared after return. This establishes external browser launch and passkey sign-in on the user's device only; it does not establish built-in WebView authentication, session transfer, or provider data retrieval. The user performs physical Android device testing.

## Passkey compatibility
Passkeys, passwords, email-code sign-in, federated sign-in and MFA must be accommodated through genuine provider-supported flows when available. Do not assume every provider offers every method. Android Credential Manager's first-party WebView path requires digital asset association with the relying-party domain. Browser-mode third-party origins require credential-provider approval of the calling package and signing identity. DuckDuckGo's eligibility does not transfer to Clank-Con. Opening an external authentication tab only returns an authorization result if the provider exposes and authorizes a suitable client/redirect integration. A successful provider web login is not that integration. No imitation login screens, credential extraction, cookie copying, identity impersonation, or access-control bypass. No universal history API has been verified. Provider-supported export/import and actual user-transferred content remain distinct from live retrieval. Record unavailable capabilities explicitly.

References:
- https://developer.android.com/identity/passkeys/architecture
- https://developer.android.com/identity/sign-in/privileged-apps
- https://developer.android.com/identity/sign-in/credential-manager-webview

## Release gates
All requirements in PROJECT_REQUIREMENTS.md remain in scope; do not silently substitute import-only support for live retrieval or mark a provider complete based on website launch.
- Authentication: for each provider and applicable account type, test actual available authentication methods, success/cancel/failure, profile isolation, restart, expiry, disconnect, and temporary-session cleanup. Mark unsupported methods with the reason. Never display Connected solely because an external browser was opened.
- Retrieval: distinguish one visible thread, selected threads, entire available history, archived/project conversations, alternative branches, attachments, generated images and metadata. Verify each capability against a known account using permitted interfaces. Record missing content and provenance; do not manufacture metadata.
- Archive: test full original-byte preservation, Unicode, long chats, attachment/image integrity, duplicate behavior, interrupted operations, encrypted storage, search, backup/restore, and all seven export formats (TXT, PDF, JSON, Markdown, HTML, DOCX, ZIP). Test Android runtime rendering and actual file readability. Host-side tests alone do not qualify as device acceptance.
- Upgrades: retain package and signer, increment versionCode, test installation over prior build with existing vault records, settings and sessions; test recovery and data migrations. Never require uninstall as a routine upgrade. Do not publish keys or secrets. Keep the approved robot-taking-notes icon and page-based CRT UI; preserve prior functionality.
- Build: supplied tools only, direct AAPT2/javac/D8/zipalign/apksigner; verify all DEX files, resources, ZIP, alignment, signature, certificate continuity and SHA-256. No Gradle, SDK installation, LFS, paid Actions or hosted build. Exact source/logs committed and checked against build inputs.
- Release reporting: distinguish implemented, host-tested, device-tested, provider-verified and outstanding. No claim that a feature works universally or that a release is complete until evidence supports it.

## Current state
v0.1.0 is the existing device-test release. See docs/V0.1.0_VERIFICATION.md for implemented functionality and outstanding work. No new APK in this pass: passkey success in DuckDuckGo does not establish a working standalone passkey implementation or authorize an external-browser session handoff. Existing code and binary unchanged.
