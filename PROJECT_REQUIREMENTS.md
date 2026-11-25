# Clank-Con — product requirements

## Approved scope
- Application name: Clank-Con.
- Providers: ChatGPT, Kimi, Claude, Gemini, Grok, DeepSeek, Perplexity, Microsoft Copilot.
- Exports: TXT (UTF-8), PDF, JSON, Markdown, HTML, DOCX, ZIP.
- Retrieval: individual conversations, selected conversations, entire available history, archived conversations, project conversations, alternative response branches, attachments, generated images, conversation metadata.
- Storage: direct exports and an encrypted searchable local vault, with backup and restoration.
- UI: retro terminal CRT. User-friendly robust page-based workflows, dropdowns and navigation rather than monolithic walls.
- Authentication: undecided. Icon: undecided.

## Functional and data contracts
The goal is retrieval of existing personal conversations, not merely conversion of a previously downloaded account export. The app must preserve all available original content rather than summarize it. Preserve message order, roles, Unicode, code, formatting, IDs, timestamps, branching relationships and attachments where exposed. Preserve raw source records when available; never invent missing metadata. Distinguish visible thread, selected branch, complete branch graph and account history. Report missing content and failed retrieval explicitly. Provide local search, filters, duplicate detection, incremental imports/backups, export selection and integrity manifests. No provider may be marked complete without provider-specific acceptance evidence. No chat mutation or deletion on provider services.

## Proposed workflow (not a finalized UI)
Home / provider selection -> source connection or import -> conversation selection -> capture/review and completeness report -> save to vault or export -> native Android destination picker. Separate vault/search, backup/restore and settings pages. Offer format selection via dropdown or multi-selection, not an oversized dashboard. Actual UI requires implementation and review.

## Research: authentication
External browser/custom tabs reuse browser state but do not grant arbitrary DOM/script access. Android WebView has its own session and Google OAuth disallows embedded user agents. Browser extensions demonstrate capture in a browser but do not establish a standalone APK solution. Ordinary Android sharing can pass a URL or selected content, not automatically an entire authenticated thread. Provider-supported exports remain optional import sources, not the sole intended workflow. OpenAI consumer terms restrict programmatic extraction; provider-specific permitted routes and limitations must be recorded. No session token extraction, login bypass or silent sharing. All provider live capture and full-history completeness remain unverified.

References:
- https://developer.android.com/develop/ui/views/layout/webapps/in-app-browsing-embedded-web
- https://chromium.googlesource.com/chromium/src/+/main/docs/security/custom-tabs-faq.md
- https://developers.googleblog.com/upcoming-security-changes-to-googles-oauth-20-authorization-endpoint-in-embedded-webviews/
- https://github.com/GrantTotinov/AIExporter
- https://help.openai.com/en/articles/7260999-exporting-your-chatgpt-history-and-data
- https://openai.com/policies/row-terms-of-use/
- https://support.google.com/gemini/answer/16920332

## Acceptance gates
Verify supplied toolchain availability and integrity; build directly with javac/AAPT2/D8/zipalign/apksigner. Verify manifest, expected DEX classes, DEX integrity, resources, APK ZIP integrity, alignment, signature, SHA-256, installation and launch on supported Android. Test all formats, long conversations, Unicode, branches, metadata, attachment preservation, import completeness, duplicate handling, backup/restore, storage privacy and errors. Device and live-account tests must be separately labeled; synthetic fixtures alone are not provider acceptance.
