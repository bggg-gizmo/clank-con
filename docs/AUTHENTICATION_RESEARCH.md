# Authentication and retrieval investigation
Recorded 2026-11-24. This is a research decision record, not a live compatibility report.

## User context and direction
The user primarily uses native Android AI apps, but also accesses ChatGPT through DuckDuckGo Android. No login method was specified. Do not ask the same browser-use question again or require a change to the user's daily AI workflow. The user directed continued investigation and implementation without further debate. Preserve all approved requirements. Icon remains undecided.

## Candidate selection
Investigate Android system WebView first: DuckDuckGo's Android browser uses android.webkit.WebView. This is evidence of engine viability, not proof that Clank-Con's own login environment will work. No browser engine download is necessary for this candidate. A browser screen must display the genuine URL and TLS errors, permit required legitimate login redirects and popups, and never log or expose passwords, cookies, tokens, MFA or page content during sign-in. Never spoof a browser or bypass provider restrictions. No public sharing links. No use of another app's private session storage.
For independent persistent profiles, use AndroidX WebKit's ProfileStore and WebViewCompat.setProfile where MULTI_PROFILE is supported. Check support at runtime before creating a session; never silently substitute the common default profile. Android's setDataDirectorySuffix is a process-wide pre-initialization setting, not a switchable per-WebView setting; a distinct process architecture would need separate verification. Persistent and temporary sessions are both proposed. Temporary profile data must be removed only after all associated views are closed and deletion succeeds; keep archived conversations independent of session deletion.
Google prohibits OAuth in embedded user agents; external authentication cannot simply transplant cookies back into WebView. A full-browser GeckoView candidate would require pinned AARs and transitive dependencies, manifests, resources, JNI libraries, license inventory and ABI validation with the direct build pipeline. It is not guaranteed to solve provider OAuth restrictions. Custom Tabs alone do not provide DOM access. An external extension would be an additional installation and cannot be silently made a requirement of the APK.
If login is unsupported, present the real reason and allow explicitly user-provided files or text as fallback. Do not claim that a URL handed from another app contains its private conversation. Official archive import is optional, not the intended sole workflow. OpenAI consumer terms restrict automatic/programmatic extraction; do not enable prohibited automation or circumvent restrictions. Review each provider's permitted integration options separately.

## Provider gate
All eight providers remain requirements; none has verified live Android authentication or retrieval. The following are official site entry points, not undocumented API endpoints.
| Provider | Entry | Established evidence | Outstanding |
|---|---|---|---|
| ChatGPT | https://chatgpt.com/ | User reports DuckDuckGo access; official data export documented | App login, permissible capture, completeness, branches, projects, attachments |
| Kimi | https://www.kimi.com/ | Browser exporter support reported; no live verification | Login, permissible capture, account export details, all completeness categories |
| Claude | https://claude.ai/ | Browser exporters report support | Login, permissible capture, projects, branches, attachment completeness |
| Gemini | https://gemini.google.com/ | Google Takeout documents chats, uploads and generated media | Embedded login restrictions; permitted capture and actual archive completeness |
| Grok | https://grok.com/ | Official settings/data controls download; private chats not visible in history | Login, account scopes, permitted capture and completeness |
| DeepSeek | https://chat.deepseek.com/ | Browser exporters report support | Login, permissible capture and account export details |
| Perplexity | https://www.perplexity.ai/ | Browser exporters report thread support | Login, permissible capture and account export details |
| Microsoft Copilot | https://copilot.microsoft.com/ | Microsoft privacy dashboard documents activity export | Login, consumer versus work account scope, permitted capture and completeness |

No claim is made that existing browser extensions are audited, licensed for reuse, or equivalent to a standalone APK. Do not import third-party code without license and security review.

## Acceptance protocol
1. Test installed APK on Android using a controlled account, starting from a clean profile. Record engine/version and sign-in method without credentials.
2. Verify official origin, login completion, MFA if requested, cancellation, session expiry, reconnect, app restart, and logout. Reject TLS errors.
3. Connect two separately authorized accounts to the same provider. Verify no cross-account cookies, account identity or archive records. Test temporary session cleanup and retained local archives.
4. Check a known short and long conversation against its original visible message count and content. Distinguish user-visible thread from full branch graph and account inventory. Test archived/project views and media only where provider supports the relevant feature.
5. Record source, count, available IDs, timestamp provenance and completeness status; missing metadata remains unknown. Never infer completeness merely from a successful login or a nonempty export.
6. Test offline export, encrypted vault storage, search, deduplication and backup/restore independently. Retain synthetic fixtures only in Git.
7. Do not declare provider support or APK release acceptance without actual results.

## Sources
- DuckDuckGo Android source: https://github.com/duckduckgo/Android/blob/develop/app/src/main/java/com/duckduckgo/app/browser/BrowserChromeClient.kt
- Android profile APIs: https://developer.android.com/reference/androidx/webkit/ProfileStore and https://developer.android.com/reference/androidx/webkit/WebViewCompat
- Android WebView process isolation: https://developer.android.com/reference/android/webkit/WebView
- Google embedded OAuth policy: https://developers.googleblog.com/upcoming-security-changes-to-googles-oauth-20-authorization-endpoint-in-embedded-webviews/
- Mozilla engine and extensions: https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/geckoview-quick-start.html and https://firefox-source-docs.mozilla.org/mobile/android/geckoview/consumer/web-extensions.html
- Browser exporter examples: https://github.com/Covai-Labs/ace and https://addons.mozilla.org/en-US/firefox/addon/open-ai-exporter/
- OpenAI terms: https://openai.com/policies/terms-of-use/
- Gemini export: https://support.google.com/gemini/answer/16920332
- Grok consumer FAQ: https://x.ai/legal/faq
- Copilot history: https://support.microsoft.com/en-gb/privacy/microsoft-copilot/activity-history
