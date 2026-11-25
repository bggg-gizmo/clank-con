# Clank-Con — binding engineering contract

Project: Clank-Con. Repository: bggg-gizmo/clank-con. Default branch: main.
User-approved scope recorded 2026-11-24. Read PROJECT_REQUIREMENTS.md and docs/COORDINATION_LOG.md before any change.

## Non-negotiable rules
- Deliver a standalone, installable Android APK, with complete auditable source retained in this private repository.
- No Gradle, Android Studio dependency, new Android SDK installation or construction, sdkmanager, paid GitHub Actions, hosted builds, or Git LFS.
- Use the supplied JDK, platform android.jar and Android build-tools archives directly. Compile resources with AAPT2, Java with javac, convert with D8, package all required DEX and resources, zipalign, sign with apksigner, then verify. Do not claim an APK is installable based solely on packaging.
- Use ordinary Git objects; if artifact transfer limits require multipart bootstraps, preserve every byte and verify SHA-256 after reconstruction. Never commit signing secrets, authentication material, conversation data, or user-specific test fixtures.
- No telemetry, external archival servers, credential harvesting, root requirement or Termux dependency for the installed application. Do not bypass authentication or access controls. No silent publication of chats or creation of public share links.
- Preserve accepted functionality and UI; no regressions or unrequested feature removal. Ask the user about unresolved product decisions rather than silently choosing.
- Keep the coordination log current, preserve test evidence, label unverified capabilities, and never present a mockup or fixture as live functionality. Render actual implemented UI for review when available.
- Verify provider-specific retrieval before declaring support. Full history and branches are target requirements, not claims about existing access.

## Handoff
Authentication architecture and icon are not yet approved. Do not finalize either without user approval. Build-tool archives are named in project context but were not inspected because the execution environment returned ClientError.
