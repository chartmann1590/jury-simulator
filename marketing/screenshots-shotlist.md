# Play Store Screenshots - Capture Plan

Use portrait screenshots at 1080 x 1920 or higher (same aspect ratio for all shots). Capture on a clean device with no notification overlays.

## Shot 1 - Home / Value Proposition
- Screen: Home
- Visible elements: App name, "New Case", "Case History", "Juror Profile", "Settings"
- Overlay text (in marketing editor): "Experience Jury Duty with AI"

## Shot 2 - Jury Selection (Voir Dire)
- Screen: VoirDire
- Visible elements: question prompt and response options/input
- Overlay text: "Answer Voir Dire Questions"

## Shot 3 - Trial in Progress
- Screen: Trial
- Visible elements: phase indicator (opening/witness/evidence/closing), courtroom chat/messages
- Overlay text: "Follow the Trial Phase by Phase"

## Shot 4 - Evidence Review
- Screen: Trial evidence segment
- Visible elements: evidence card/list and context details
- Overlay text: "Examine Evidence and Testimony"

## Shot 5 - Deliberation with Jurors
- Screen: Deliberation
- Visible elements: juror group chat + vote context
- Overlay text: "Deliberate with 11 AI Jurors"

## Shot 6 - Final Verdict
- Screen: Verdict
- Visible elements: verdict result (Guilty / Not Guilty / Mistrial)
- Overlay text: "Cast Your Vote. Decide the Case."

## Optional Shot 7 - Case History
- Screen: History
- Visible elements: prior cases list
- Overlay text: "Track Every Case You Tried"

## Capture Commands (ADB)
Run for each screen:

```bash
adb shell screencap -p /sdcard/shot1.png
adb pull /sdcard/shot1.png ./marketing/assets/shot1.png
```

Repeat with `shot2.png`, `shot3.png`, etc.

## Fast cleanup before upload
- Ensure consistent brightness and system theme
- Crop only if needed; keep UI fully visible
- Add short overlay text in Canva/Figma/CapCut (same font family across all shots)
- Export PNG, no heavy compression artifacts
