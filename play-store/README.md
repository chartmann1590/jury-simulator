# Play Store Submission Package — Jury Simulator

Everything Google Play Console asks for, in one folder, in the order it asks for it. Plug each file into the matching field.

## Folder map

```
play-store/
├── app-title.txt                  → Play Console field: "App name"
├── short-description.txt          → "Short description" (≤ 80 chars)
├── full-description.txt           → "Full description" (≤ 4000 chars)
├── release-notes-en-US.txt        → "Release notes" for the current build
├── category.txt                   → "App category" + tags
├── privacy-policy-url.txt         → "Privacy Policy" URL
├── website-url.txt                → "Website" URL
├── contact-email.txt              → "Email" (developer contact)
├── promo-video-link.txt           → "Promo video" YouTube URL (instructions)
├── content-rating-answers.md      → "Content rating" questionnaire answers
├── data-safety-answers.md         → "Data safety" form answers
├── README.md                      → this file
└── graphics/
    ├── icon-512.png               → "App icon" (512 × 512, PNG, no alpha)
    ├── feature-1024x500.png       → "Feature graphic" (1024 × 500, PNG)
    ├── render_graphics.py         → reproducible source for every PNG above
    └── screenshots/
        ├── phone/                 → "Phone" screenshots — 6 portrait PNGs
        │   ├── 01-home.png … 06-additional.png
        ├── tablet-7in/            → "7-inch tablet" screenshots — 6 PNGs at 1200×1920
        │   ├── 01-home.png … 06-additional.png
        └── tablet-10in/           → "10-inch tablet" screenshots — 6 PNGs at 1600×2560
            ├── 01-home.png … 06-additional.png
```

## Asset specs (for sanity)

| Asset | Required size | What's in this folder |
|---|---|---|
| App icon | 512 × 512 PNG, 32-bit, no alpha | `graphics/icon-512.png` |
| Feature graphic | 1024 × 500 PNG, no alpha | `graphics/feature-1024x500.png` |
| Phone screenshots | 320–3840 px each side, 16:9 to 9:16 | 6 PNGs in `graphics/screenshots/phone/` (native phone capture) |
| 7-inch tablet screenshots | same range, ≥ 1080 px on the long edge recommended | 6 PNGs at 1200 × 1920 in `graphics/screenshots/tablet-7in/` |
| 10-inch tablet screenshots | same range, ≥ 1080 px on the long edge recommended | 6 PNGs at 1600 × 2560 in `graphics/screenshots/tablet-10in/` |

The tablet screenshots are **marketing-style** renders: the actual phone capture is bezeled at the top with a title/caption beneath. They're built from the same six source images so the messaging stays consistent across all three buckets. If/when you add tablet-specific UI to the app, capture native tablet screenshots and drop them in to replace these.

## What CI gives you

Every push to `master` produces a signed AAB + APK and tags a GitHub Release. Latest build is always at <https://github.com/chartmann1590/jury-simulator/releases/latest>. Upload that AAB to Play Console.

## Manual submission steps

1. Pay the one-time Play Console developer fee ($25) at <https://play.google.com/console>.
2. Create the app shell — copy `app-title.txt` into **App name**, choose **Game**, **Free**.
3. **Main store listing**:
   - Short description ← `short-description.txt`
   - Full description ← `full-description.txt`
   - App icon ← `graphics/icon-512.png`
   - Feature graphic ← `graphics/feature-1024x500.png`
   - Phone screenshots ← all 6 PNGs from `graphics/screenshots/phone/`
   - 7-inch tablet screenshots ← all 6 from `graphics/screenshots/tablet-7in/`
   - 10-inch tablet screenshots ← all 6 from `graphics/screenshots/tablet-10in/`
4. Upload `marketing/assets/jurysim-promo.mp4` to YouTube, then paste the URL into the **Promo video** field. Update `promo-video-link.txt` with the final URL.
5. **Privacy** ← URL from `privacy-policy-url.txt`.
6. **Policy → App content**:
   - Content rating — answers from `content-rating-answers.md`
   - Data safety — answers from `data-safety-answers.md`
   - Target audience — 13+
   - Ads — Yes (AdMob)
7. **Production → Create new release** — upload the latest AAB from GitHub Releases. Paste `release-notes-en-US.txt` as the release notes.
8. Submit for review.

## Updating an existing listing

- Bump notes in `release-notes-en-US.txt`.
- Upload the new AAB (CI built it on master push).
- Privacy URL doesn't change — the page itself updates when `site/privacy.html` is edited and pushed.

## Regenerating graphics

If you change the app icon, feature copy, or screenshot captions, edit `graphics/render_graphics.py` and run:

```bash
python play-store/graphics/render_graphics.py
```

This rewrites `icon-512.png`, `feature-1024x500.png`, and all 18 screenshot PNGs (6 × 3 device classes) from the source images in `marketing/assets/`.
