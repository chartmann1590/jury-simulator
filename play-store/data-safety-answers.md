# Play Console — Data Safety Form

These answers must match `site/privacy.html`. If you change one, change the other.

Jury Simulator itself collects **nothing** — the developer runs no server
and the app has no analytics SDK. Every "collected/shared" entry below
comes from the **Google Mobile Ads SDK (AdMob)**, per Google's official
SDK data disclosure (https://developers.google.com/admob/android/privacy),
which lists the following as collected automatically by the SDK:
IP address, user product interactions, diagnostic information, and
device/account identifiers (Android advertising ID, app set ID).

The one-time AI model download is a direct HTTPS GET from the device to
Hugging Face; no user data is sent by the app, and the foreground
service (dataSync) that runs it transfers no personal data.

## Data collection and security

**Does your app collect or share any of the required user data types?**
- Yes (solely because of the AdMob SDK, per Google's disclosure table).

**Is all of the user data collected by your app encrypted in transit?**
- Yes (HTTPS only; network_security_config disallows cleartext).

**Do you provide a way for users to request that their data be deleted?**
- Not applicable — the developer stores no user data on any server.
  Local app data (case history, juror profile) can be removed by
  clearing app data in Android settings or uninstalling. Advertising
  data held by Google is governed by Google's policies; the advertising
  ID can be reset or deleted in Android settings.

## Data types

### Location
- **Approximate location** — Collected by the Google Mobile Ads SDK
  (estimated from IP address). Shared with Google / advertising
  partners via the SDK. Purposes: Advertising or marketing, Analytics,
  Fraud prevention, security, and compliance. Ephemeral: processed
  ephemerally where applicable per Google's disclosure. Required: No.

### App activity
- **App interactions** — Collected by the Google Mobile Ads SDK (app
  launches, taps, ad impressions/views). Shared via the SDK.
  Purposes: Advertising or marketing, Analytics. Required: No.
- Other user-generated content / other actions: None.

### App info and performance
- **Crash logs** — Collected by the Google Mobile Ads SDK. Shared via
  the SDK. Purposes: Analytics, Fraud prevention, security, and
  compliance. Required: No.
- **Diagnostics** — Collected by the Google Mobile Ads SDK (launch
  time, hang rate, energy usage, performance data). Shared via the SDK.
  Purposes: Analytics, Fraud prevention, security, and compliance.
  Required: No.
- Other app performance data: None beyond the above.

### Device or other IDs
- **Device or other IDs** — Advertising ID and app set ID, collected by
  the Google Mobile Ads SDK. Shared via the SDK. Purposes: Advertising
  or marketing, Analytics, Fraud prevention, security, and compliance.
  Required: No (advertising ID is resettable/deletable in Android
  settings).

### Everything else — None
- Personal info: None. (The juror profile — name, occupation,
  political leaning, and similar fields the user types — is stored
  ONLY on the device and is never transmitted.)
- Financial info: None. Health and fitness: None. Messages: None
  (juror chats are on-device AI conversations, never transmitted).
- Photos and videos: None. Audio files: None. Files and docs: None.
- Calendar: None. Contacts: None. Web browsing: None.

## Data shared with third parties

All sharing below is performed by the Google Mobile Ads SDK itself; the
app code does not transmit any of this data.

| Data type | Shared with | Purpose |
|---|---|---|
| Approximate location (IP-derived) | Google AdMob / advertising partners | Advertising, analytics, fraud prevention |
| App interactions | Google AdMob | Advertising, analytics |
| Crash logs, diagnostics | Google AdMob | Analytics, fraud prevention, security |
| Advertising ID, app set ID | Google AdMob / advertising partners | Advertising, analytics, fraud prevention |

## Security practices

- **Data encrypted in transit:** Yes
- **Data deletion:** No server-side data exists to delete (developer
  runs no server). Local data: Android Settings → Apps → Jury
  Simulator → Storage → Clear data, or uninstall. Android backup of
  app data is disabled (allowBackup=false).
- **Independent security review:** No
- **Committed to Play Families Policy:** No (app is rated Teen; not
  designed for children).
- **Ads consent:** Where required, the app obtains consent via Google's
  User Messaging Platform before the Mobile Ads SDK is initialized.
