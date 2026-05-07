# Promo Video + Voiceover Pack (30-45s)

Target format: 1080 x 1920 (vertical), 30 fps, 30-45 seconds.

## Storyboard (time-coded)
0:00-0:04
- Visual: App launch and Home screen
- On-screen text: "Take Your Seat on the Jury"

0:04-0:09
- Visual: Start New Case, show case intro
- On-screen text: "Every Case Is AI-Generated"

0:09-0:14
- Visual: Voir dire screen with question/response
- On-screen text: "Pass Through Voir Dire"

0:14-0:22
- Visual: Trial phase transitions (opening, witness, evidence)
- On-screen text: "Hear Testimony. Review Evidence."

0:22-0:30
- Visual: Deliberation chat with multiple jurors
- On-screen text: "Debate with 11 AI Jurors"

0:30-0:36
- Visual: Voting and final verdict reveal
- On-screen text: "Vote. Decide Justice."

0:36-0:42
- Visual: Home + tagline end card
- On-screen text: "Jury Simulator - Trial Verdict"
- CTA text: "Download on Google Play"

## Voiceover Script
"What would you decide if justice depended on your vote?

In Jury Simulator, you experience the full jury process, from voir dire to final verdict.

Question witnesses, review evidence, and deliberate with eleven AI jurors, each with their own personality and perspective.

Every case is different. Every decision matters.

Take your seat. Hear the arguments. Cast your vote.

Jury Simulator - Trial Verdict."

## Recording Notes
- Tone: serious, confident, cinematic
- Pace: 125-140 words per minute
- Voice profile: neutral US English
- Leave 200-300ms pauses between major lines for scene cuts

## B-roll Capture Method
Use screen recording from device/emulator while navigating scripted scenes.

```bash
adb shell screenrecord /sdcard/jurysim-promo.mp4
# Perform the app flow for ~45 seconds, then stop recording (Ctrl+C)
adb pull /sdcard/jurysim-promo.mp4 ./marketing/assets/jurysim-promo.mp4
```

## Quick Edit Recipe (CapCut, Premiere, or DaVinci)
1) Drop recorded app footage on timeline
2) Cut to match storyboard timestamps
3) Add on-screen text captions above
4) Record/import voiceover and align to cuts
5) Add subtle ambient underscore music (low volume)
6) Export H.264 MP4, 1080x1920, 8-12 Mbps
