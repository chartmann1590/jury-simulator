# Play Console — IARC Content Rating Questionnaire

Use these answers in Play Console under **Policy → App content →
Content ratings**. They describe the app AS IT NOW SHIPS (post
AI-safety fix). Do not submit answers from memory or from older
versions of this file — the previous questionnaire described content
the app could actually exceed, which is what drew the May/June 2026
Content Ratings rejections.

## How content works in this app (read first)

Every case, witness, juror, and line of dialogue is generated at
runtime by an on-device AI model (Gemma 4 E4B via LiteRT-LM). The app
constrains that generation in code:

- **Charge allowlist** — cases can only be built around: theft, fraud,
  assault (non-graphic), DUI (driving under the influence), drug
  possession, burglary, vandalism. Sexual offences, offences against
  children, and graphic-violence cases are excluded by construction.
- **System constraint block** on every generation prompt:
  courtroom-appropriate language only; no profanity or slurs; no
  graphic descriptions of injuries, violence, or sexual content;
  off-topic requests redirected back to the trial.
- **Sampler limits and a hard output-length cap** in the engine.
- **Output filter** — AI text is screened before display; slurs are
  replaced with an in-character fallback line and strong profanity is
  masked.
- **Report mechanism** — users can long-press any AI message and
  choose "Report this content" (recorded locally, with an optional
  pre-filled email to the developer).

## Violence
- **Does the app contain violence?** Yes — text-only references.
  Trials reference assault and other non-graphic offences as the
  charge being tried, in courtroom dialogue. No depicted violence, no
  animated combat, no graphic imagery, no injury detail (the
  constraint block forbids it and the filter screens output).
- **Realistic violence?** No.
- **Blood, gore, dismemberment?** No.

## Sexual content
- **Sexual content / nudity?** No. Sexual offences are excluded from
  the case allowlist, and the constraint block forbids sexual detail.

## Profanity
- **Strong language?** No — generation is constrained to
  courtroom-appropriate language, and any stray strong profanity in
  model output is masked by the output filter. Slurs are replaced
  outright.
- **Crude humor?** No.

## Drugs / alcohol / tobacco
- **References to drug use?** Yes — drug possession is one of the
  allowable charges; references are procedural (the offence being
  tried), text-only, never glorified or instructional.
- **Alcohol references?** Yes — DUI is one of the allowable charges;
  same procedural treatment.
- **Tobacco references?** No.

## Gambling
- **Real-money gambling?** No.
- **Simulated gambling?** No.

## Digital purchases
- **In-app purchases / digital goods?** None. The app has no billing,
  no subscriptions, no premium tier, no virtual currency. It is free
  with ads only.

## Advertising
- **Contains ads?** Yes — Google AdMob: a banner ad and occasional
  interstitial ads between case phases (not forced on every case
  start; frequency-capped). Where required, consent is obtained via
  Google's User Messaging Platform before ads initialize.
- **Ads are not** gambling, alcohol, or adult ads targeted by the app;
  ad content is served by AdMob under its own policies.

## AI-generated content
- **Does the app generate content with AI?** Yes — all cases and
  dialogue are generated on-device at runtime (no cloud generation).
- **Can users steer generation with free text?** Yes — users type
  questions and chat messages to AI jurors/witnesses. Generation is
  constrained as described above (allowlist, system constraints,
  output filter), and users can report any AI message in-app.

## User interaction / user-generated content
- **Users can interact with other users / share content?** No — fully
  single-player; no online multiplayer; no chat with other humans;
  nothing is published or shared.
- **User-generated content?** No public UGC. Text the user types goes
  only to the on-device AI and stays on the device.
- **Shares user location?** No (the app does not access device
  location; AdMob may estimate approximate location from IP — see
  Data Safety).

## Accounts / personal data
- **Accounts?** None — no sign-up, no sign-in, no email collection.
- **Personal data collected by the app?** None transmitted anywhere.
  The app offers an optional local juror profile (which can include a
  political-leaning field the user sets); it is stored only on the
  device, never leaves it, and is excluded from Android backups.

## Mature themes
- **Crime themes?** Yes — the game is about criminal trials for the
  allowlisted offences above. Treated procedurally, not glorified.
- **Horror?** No.
- **Discrimination / hate?** No — juror "bias" in-game means
  case-relevant leanings (e.g. trusting police testimony), and the
  constraint block + filter prohibit slurs and hate content.

## Recommended rating outcome
- **IARC**: Teen / 12+ in most regions; PEGI 12 (non-graphic crime
  references, infrequent mild themes, in-app advertising).
- **Target age**: 13+
