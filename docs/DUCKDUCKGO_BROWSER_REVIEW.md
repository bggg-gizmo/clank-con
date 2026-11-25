# DuckDuckGo Android browser review — connection gate

2026-11-24. User is physically testing Clank-Con Connection Test 0.0.1-probe and prefers not to retrieve passwords manually. User reports existing ChatGPT browser access via DuckDuckGo Android. Do not infer whether passwords are saved or which authentication method is used.

## Source inspection
Inspected upstream develop files directly using GitHub; the upstream Android repository is Apache-2.0 licensed. No code has been copied. Findings are from source, not device acceptance.

- BrowserChromeClient.kt lines 134–146 and 169–171: user-gesture window creation is forwarded to a new tab using Android WebViewTransport; window close is handled by the browser. Clank-Con diagnostic disables multiple windows. This is a material compatibility difference for websites that require legitimate sign-in popups.
- BrowserWebViewClient.kt lines 690–720 and 916–922: page navigation triggers per-site cookie policy, autofill configuration and login detection; cookie persistence is explicitly managed. Clank-Con diagnostic has fixed third-party cookie blocking and flushes cookies on pause.
- ThirdPartyCookieManager.kt blob 420c2036cf9f0dc2bc2f2d267107546e0ef522e1: upstream has site-specific cross-domain cookie compatibility logic. Do not transplant account cookies, read authentication cookie values, or weaken protections globally. Only consider documented compatibility options, explicit user controls, and provider-authorized login flows.
- WebViewPasskeyInitializer.kt blob a934ce0e233601411fd81e14983b366fbcaa3779: browser WebAuthn capability is checked, and browser-mode support is subject to build eligibility. Source specifically documents Google package/signing certificate allowlisting for origin information. A fork with Clank-Con's identity cannot assume equivalent privileges. Do not impersonate DuckDuckGo or bypass allowlisting.
- DuckDuckGoWebView.kt blob 92740001f8e00fcbe78d704f8b22746ce906b0d6: delegates autofill to Android and tracks completion callbacks. Its separate internal browser password manager is not the same thing as the Android system autofill provider. SystemAutofillServiceSuppressor.kt blob 64e05d8d8aa101b362f1a014aea0bd4355a58a87 can cancel the system autofill flow in contexts where upstream chooses its own behavior. Do not implement a custom password vault or credential-reading bridge for Clank-Con.

## Implications and candidate changes
Keep the tested diagnostic intact until user reports results. For a subsequent diagnostic revision, consider ordinary user-initiated browser popup lifecycle, trusted visible origin on every window, explicit return/cancel, no page-content access on login pages, and normal Android autofill UI (without storing passwords in Clank-Con). Test compatibility against genuine sign-in, never spoof identity, transfer cookies/tokens, override TLS, or bypass provider policies. Built-in WebView remains the smallest dependency-free candidate, not proof of login success. No Gradle, new SDK, LFS or paid builds.

External DuckDuckGo browser launch can preserve the existing browser session if it is still signed in. It cannot grant Clank-Con access to its authenticated private content or password database. Custom Tabs may share browser state if the installed browser supports them, but do not provide arbitrary page access. For external-browser workflows, use explicit user-provided conversation text/files or provider-supported export/integration; a URL alone is not a chat archive. Android system autofill might avoid manual entry if the credential is actually available and the selected provider supports the app/site context. If the official login offers an email code or other supported passwordless method, the user may choose it directly; do not promise availability.

## Sources
- https://github.com/duckduckgo/Android/blob/develop/app/src/main/java/com/duckduckgo/app/browser/BrowserChromeClient.kt
- https://github.com/duckduckgo/Android/blob/develop/app/src/main/java/com/duckduckgo/app/browser/BrowserWebViewClient.kt
- https://github.com/duckduckgo/Android/blob/develop/app/src/main/java/com/duckduckgo/app/browser/cookies/ThirdPartyCookieManager.kt
- https://github.com/duckduckgo/Android/blob/develop/app/src/main/java/com/duckduckgo/app/browser/webauthn/WebViewPasskeyInitializer.kt
- https://github.com/duckduckgo/Android/blob/develop/app/src/main/java/com/duckduckgo/app/browser/DuckDuckGoWebView.kt
- https://github.com/duckduckgo/Android/blob/develop/LICENSE
- https://developer.android.com/develop/ui/views/layout/webapps/in-app-browsing-embedded-web
- https://developer.android.com/identity/sign-in/credential-manager-webview
- https://developer.android.com/identity/autofill
- https://duckduckgo.com/duckduckgo-help-pages/passwords-autofill

## Outstanding
Real device sign-in, session persistence, profile isolation and permitted full-history retrieval remain unverified. No new APK or application code changes in this research step. No source dependency imported, no credentials collected. Existing approved scope remains intact.
