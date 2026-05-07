# Play Console — Data Safety Form

These answers must match `site/privacy.html` exactly. If you change one, change the other.

## Data collection and security

**Does your app collect or share any of the required user data types?**
- Yes (because of AdMob).

**Is all of the user data collected by your app encrypted in transit?**
- Yes (HTTPS only, network_security_config disallows cleartext).

**Do you provide a way for users to request that their data be deleted?**
- Not applicable — the app does not store user data on a server. Local case history can be removed by uninstalling the app or clearing app data in Android settings.

## Data types collected

### Personal info
- None.

### Financial info
- None.

### Health and fitness
- None.

### Messages
- None.

### Photos and videos
- None.

### Audio files
- None.

### Files and docs
- None.

### Calendar
- None.

### Contacts
- None.

### App activity
- None collected by us.

### Web browsing
- None.

### App info and performance
- None collected by us.

### Device or other IDs
- **Advertising ID** — Collected by Google AdMob, not by us. Purpose: advertising or marketing. Required to use the app: No (resettable in Android settings).

## Data shared with third parties

| Data type | Shared with | Purpose | Required |
|---|---|---|---|
| Advertising ID | Google AdMob | Advertising or marketing | Optional (user can reset/opt out at OS level) |
| Approximate location (country, derived from IP) | Google AdMob | Advertising or marketing | Optional |
| Device or other IDs (model, OS, language) | Google AdMob | Advertising or marketing | Optional |

Note: AdMob's data handling is governed by Google's Ads & Privacy Policy. We do not directly collect or transmit any of this — Google's SDK does, when an ad is requested.

## Security practices

- **Data encrypted in transit:** Yes
- **Data deletion option:** App data can be wiped via Android Settings → Apps → Jury Simulator → Storage → Clear data, or by uninstall. There is no server-side data to delete because we don't run one.
- **Independent security review:** No
- **Committed to Play Families Policy:** No (app is rated 13+; not designed for children).
