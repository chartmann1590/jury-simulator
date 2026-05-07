# Play Store Submission Package — Jury Simulator

This folder contains everything needed to submit Jury Simulator to the Google Play Store, in the order Play Console asks for it.

## Files in this folder

| File | What Play Console calls it |
|---|---|
| `app-title.txt` | App name (≤30 chars) |
| `short-description.txt` | Short description (≤80 chars) |
| `full-description.txt` | Full description (≤4000 chars) |
| `release-notes-en-US.txt` | Release notes for the current version (≤500 chars) |
| `category.txt` | Category + tags |
| `privacy-policy-url.txt` | Privacy policy URL |
| `website-url.txt` | Website URL |
| `contact-email.txt` | Developer contact email |
| `promo-video-link.txt` | YouTube URL placeholder + upload instructions |
| `content-rating-answers.md` | Pre-filled answers for the IARC content-rating questionnaire |
| `data-safety-answers.md` | Pre-filled answers for the Data Safety form |
| `graphics/icon-512.png` | App icon (512×512 PNG, no transparency) |
| `graphics/feature-1024x500.png` | Feature graphic (1024×500 PNG) |
| `graphics/screenshots/` | Phone screenshots (six PNGs) |

## What CI gives you

The signed AAB you upload to Play Console is produced automatically on every push to `master`. Find it under **GitHub → Releases → latest** as `jury-simulator-vX.Y.Z.aab`.

## Manual submission steps

1. Pay the one-time Play Console developer fee ($25) at <https://play.google.com/console>.
2. Create the app shell in Play Console — copy `app-title.txt` into the **App name** field, choose **Game** type, **Free**.
3. Fill **Main store listing** with `short-description.txt`, `full-description.txt`, and the 1024×500 feature graphic + 512×512 icon.
4. Upload all six screenshots from `graphics/screenshots/` under **Phone screenshots**.
5. Upload `marketing/assets/jurysim-promo.mp4` to YouTube, then paste its URL into the **Promo video** field. Update `promo-video-link.txt` with the final URL.
6. Set **Privacy policy** to the URL in `privacy-policy-url.txt`.
7. Under **Policy → App content**, complete:
   - Content rating — use answers from `content-rating-answers.md`
   - Data safety — use answers from `data-safety-answers.md`
   - Target audience — 13+
   - Ads — Yes (AdMob)
8. Under **Production → Create new release**, upload the latest AAB from GitHub Releases. Paste `release-notes-en-US.txt` as release notes.
9. Submit for review.

## Updating an existing listing

For subsequent releases:

- Bump version notes in `release-notes-en-US.txt`.
- Upload the new AAB (CI builds one on every master push).
- The privacy policy URL stays the same — the page itself updates automatically when `site/privacy.html` is changed and pushed.
