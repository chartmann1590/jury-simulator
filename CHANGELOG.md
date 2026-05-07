# Changelog

All notable changes to Jury Simulator are documented here. Format follows [Keep a Changelog](https://keepachangelog.com/en/1.1.0/). Versions are tagged `v1.0.<run>` where `<run>` is the GitHub Actions run number that produced the release.

## [Unreleased]

### Added
- GitHub Actions CI workflow — lint and unit tests on every push.
- GitHub Actions release workflow — signs and publishes AAB + APK on every push to `master` with auto-incrementing `versionCode`.
- GitHub Pages workflow — deploys the marketing site under `site/` to `chartmann1590.github.io/jury-simulator`.
- Adaptive app icon (scales of justice mark in CourtBlue + CourtGold).
- Customer-facing README with screenshot gallery and Play Store links.
- Marketing site (`site/`) with privacy policy and support pages.
- Play Store submission package (`play-store/`) — descriptions, content rating answers, data safety form, 512×512 icon, 1024×500 feature graphic, six phone screenshots.
- Release signing config in `app/build.gradle.kts` reading from environment (CI) or `keystore.properties` (local).

### Changed
- `versionCode` and `versionName` are now driven by env vars (`VERSION_CODE`, `VERSION_NAME`) with fallback to `1` / `1.0` for local builds.
- App launcher icon switched from the placeholder Android drawable to the new adaptive icon.

## [1.0.0] — Initial development

- Trial flow: voir dire, opening, witnesses, evidence, closing, deliberation, verdict.
- Eleven AI jurors with hidden biases; private one-on-one chats; up to five voting rounds.
- On-device AI via LiteRT-LM (Gemma 4) — replaces the earlier Ollama integration.
- Notebook, case history, customizable juror profile.
- AdMob banner + interstitial.
