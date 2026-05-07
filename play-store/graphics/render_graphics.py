"""Render every Play Store graphic from a single source-of-truth.

Outputs (relative to play-store/graphics/):
    icon-512.png                            — 512x512 store icon
    feature-1024x500.png                    — 1024x500 feature graphic
    screenshots/phone/01-home.png .. 06     — copies of marketing/assets/*.png
    screenshots/tablet-7in/01-home.png ..   — 1200x1920 marketing screenshots
    screenshots/tablet-10in/01-home.png ..  — 1600x2560 marketing screenshots

Run from the repo root:

    python play-store/graphics/render_graphics.py
"""
from pathlib import Path
import shutil

from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
OUT = Path(__file__).resolve().parent
SCREENSHOTS_OUT = OUT / "screenshots"

COURT_BLUE = (30, 58, 138)
COURT_BLUE_DARK = (30, 41, 59)
COURT_GOLD = (251, 191, 36)
INK = (241, 245, 249)
INK_MUTED = (148, 163, 184)
BG = (11, 18, 32)

# Source phone screenshots (in repo order matches the trial flow).
PHONE_SHOTS = [
    ("01-home.png", "Take your seat", "Pick up where you left off, or start a new case."),
    ("02-new-case.png", "Every case is fresh", "Charges, defendant, and witnesses generated on the fly."),
    ("03-case-intro.png", "Read the room", "Get the docket before voir dire begins."),
    ("04-voir-dire.png", "Survive voir dire", "Answer the judge and hope you make the cut."),
    ("05-jury-selection.png", "Meet the eleven", "Each juror has a profession, a personality, a hidden bias."),
    ("06-additional.png", "Cast the vote", "Deliberate, vote, and decide the case."),
]


def render_icon(size: int = 512) -> Image.Image:
    """Render the Jury Simulator mark at `size` x `size`."""
    img = Image.new("RGB", (size, size), COURT_BLUE)
    overlay = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    od = ImageDraw.Draw(overlay)
    s = size / 108.0
    od.polygon(
        [
            (0, int(68 * s)),
            (int(108 * s), int(40 * s)),
            (int(108 * s), int(108 * s)),
            (0, int(108 * s)),
        ],
        fill=(*COURT_BLUE_DARK, 140),
    )
    img = Image.alpha_composite(img.convert("RGBA"), overlay)

    d = ImageDraw.Draw(img)
    rects = [
        (52, 30, 56, 80),
        (30, 80, 78, 84),
        (26, 30, 82, 34),
        (30, 34, 32, 42),
        (76, 34, 78, 42),
        (50, 24, 58, 30),
    ]
    for x0, y0, x1, y1 in rects:
        d.rectangle((x0 * s, y0 * s, x1 * s, y1 * s), fill=COURT_GOLD)

    pans = [
        [(22, 42), (40, 42), (37, 50), (25, 50)],
        [(68, 42), (86, 42), (83, 50), (71, 50)],
    ]
    for poly in pans:
        d.polygon([(x * s, y * s) for x, y in poly], fill=COURT_GOLD)

    return img.convert("RGB")


def load_font(paths, size):
    for p in paths:
        try:
            return ImageFont.truetype(p, size)
        except OSError:
            continue
    return ImageFont.load_default()


SERIF_PATHS = [
    "C:/Windows/Fonts/georgiab.ttf",
    "C:/Windows/Fonts/timesbd.ttf",
    "/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf",
]
SANS_BOLD_PATHS = [
    "C:/Windows/Fonts/segoeuib.ttf",
    "C:/Windows/Fonts/arialbd.ttf",
    "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
]
SANS_PATHS = [
    "C:/Windows/Fonts/segoeui.ttf",
    "C:/Windows/Fonts/arial.ttf",
    "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
]


def wrap_text(draw, text, font, max_width):
    """Greedy word-wrap into lines that fit within max_width."""
    words = text.split()
    lines = []
    line = ""
    for w in words:
        candidate = (line + " " + w).strip()
        bbox = draw.textbbox((0, 0), candidate, font=font)
        if bbox[2] - bbox[0] <= max_width or not line:
            line = candidate
        else:
            lines.append(line)
            line = w
    if line:
        lines.append(line)
    return lines


def render_feature_graphic(width: int = 1024, height: int = 500) -> Image.Image:
    base = Image.new("RGB", (width, height), BG)
    glow = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.ellipse((-260, -180, 520, 580), fill=(*COURT_GOLD, 28))
    gd.ellipse((420, -120, 1280, 700), fill=(*COURT_BLUE, 110))
    base = Image.alpha_composite(base.convert("RGBA"), glow).convert("RGB")

    title_font = load_font(SERIF_PATHS, 58)
    eyebrow_font = load_font(SANS_BOLD_PATHS, 16)
    sub_font = load_font(SANS_PATHS, 20)

    icon = render_icon(160)
    base.paste(icon, (60, 90))

    home_path = ROOT / "marketing" / "assets" / "01-home.png"
    if home_path.exists():
        home = Image.open(home_path).convert("RGB")
        target_h = 340
        ratio = target_h / home.height
        new_w = int(home.width * ratio)
        home = home.resize((new_w, target_h), Image.LANCZOS)
        bezel_pad = 8
        bezel = Image.new(
            "RGB",
            (new_w + bezel_pad * 2, target_h + bezel_pad * 2),
            (10, 10, 10),
        )
        bezel.paste(home, (bezel_pad, bezel_pad))
        x = width - bezel.width - 56
        y = (height - bezel.height) // 2
        base.paste(bezel, (x, y))

    d = ImageDraw.Draw(base)
    text_x = 250
    d.text((text_x, 110), "TRIAL  ·  VERDICT  ·  REPEAT", font=eyebrow_font, fill=COURT_GOLD)
    d.text((text_x, 138), "Jury Simulator", font=title_font, fill=INK)
    d.text((text_x, 220), "Decide justice from your phone.", font=sub_font, fill=INK_MUTED)
    d.text((text_x, 252), "Free · Android · Runs on-device", font=sub_font, fill=INK_MUTED)
    return base


def render_marketing_screenshot(
    phone_path: Path,
    title: str,
    caption: str,
    width: int,
    height: int,
) -> Image.Image:
    """Render a tablet-style marketing screenshot (portrait orientation).

    Layout (portrait):
        - Background: dark with court-blue/gold glows
        - Phone shot on top in a bezel, centered horizontally
        - Title + caption below it
        - Brand mark + wordmark at the bottom
    """
    base = Image.new("RGB", (width, height), BG)

    # Atmospheric glows.
    glow = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.ellipse(
        (-int(width * 0.3), -int(height * 0.15), int(width * 0.7), int(height * 0.45)),
        fill=(*COURT_GOLD, 26),
    )
    gd.ellipse(
        (int(width * 0.2), int(height * 0.45), int(width * 1.4), int(height * 1.25)),
        fill=(*COURT_BLUE, 110),
    )
    base = Image.alpha_composite(base.convert("RGBA"), glow).convert("RGB")

    # Phone screenshot in bezel — top portion of canvas.
    phone = Image.open(phone_path).convert("RGB")
    target_h = int(height * 0.62)
    ratio = target_h / phone.height
    new_w = int(phone.width * ratio)
    if new_w > int(width * 0.74):
        new_w = int(width * 0.74)
        ratio = new_w / phone.width
        target_h = int(phone.height * ratio)
        phone = phone.resize((new_w, target_h), Image.LANCZOS)
    else:
        phone = phone.resize((new_w, target_h), Image.LANCZOS)

    bezel_pad = max(8, int(width * 0.012))
    bezel = Image.new(
        "RGB",
        (new_w + bezel_pad * 2, target_h + bezel_pad * 2),
        (8, 8, 8),
    )
    bezel.paste(phone, (bezel_pad, bezel_pad))
    phone_x = (width - bezel.width) // 2
    phone_y = int(height * 0.10)
    base.paste(bezel, (phone_x, phone_y))

    d = ImageDraw.Draw(base)

    # Title + caption block under the phone.
    title_size = max(36, int(height * 0.038))
    caption_size = max(20, int(height * 0.020))
    eyebrow_size = max(14, int(height * 0.013))
    brand_size = max(16, int(height * 0.014))

    title_font = load_font(SERIF_PATHS, title_size)
    caption_font = load_font(SANS_PATHS, caption_size)
    eyebrow_font = load_font(SANS_BOLD_PATHS, eyebrow_size)
    brand_font = load_font(SERIF_PATHS, brand_size)

    text_top = phone_y + bezel.height + int(height * 0.04)
    text_max_w = int(width * 0.84)
    text_x = (width - text_max_w) // 2

    d.text((text_x, text_top), "JURY SIMULATOR", font=eyebrow_font, fill=COURT_GOLD)
    title_y = text_top + int(eyebrow_size * 2.0)
    title_lines = wrap_text(d, title, title_font, text_max_w)
    for i, line in enumerate(title_lines):
        d.text((text_x, title_y + i * int(title_size * 1.15)), line, font=title_font, fill=INK)

    caption_y = title_y + len(title_lines) * int(title_size * 1.15) + int(title_size * 0.5)
    caption_lines = wrap_text(d, caption, caption_font, text_max_w)
    for i, line in enumerate(caption_lines):
        d.text((text_x, caption_y + i * int(caption_size * 1.4)), line, font=caption_font, fill=INK_MUTED)

    # Bottom-anchored small brand mark.
    mark_size = int(height * 0.034)
    mark = render_icon(mark_size)
    mark_y = height - mark_size - int(height * 0.025)
    mark_x = text_x
    base.paste(mark, (mark_x, mark_y))
    d.text(
        (mark_x + mark_size + int(mark_size * 0.35), mark_y + int(mark_size * 0.25)),
        "Jury Simulator",
        font=brand_font,
        fill=INK_MUTED,
    )

    return base


def copy_phone_shots() -> None:
    dest = SCREENSHOTS_OUT / "phone"
    dest.mkdir(parents=True, exist_ok=True)
    for filename, _, _ in PHONE_SHOTS:
        src = ROOT / "marketing" / "assets" / filename
        if src.exists():
            shutil.copy2(src, dest / filename)
    print(f"Wrote {len(PHONE_SHOTS)} phone screenshots to {dest}")


def render_tablet_set(label: str, width: int, height: int) -> None:
    dest = SCREENSHOTS_OUT / label
    dest.mkdir(parents=True, exist_ok=True)
    for filename, title, caption in PHONE_SHOTS:
        src = ROOT / "marketing" / "assets" / filename
        if not src.exists():
            print(f"  skipping {filename}: source not found")
            continue
        img = render_marketing_screenshot(src, title, caption, width, height)
        out_path = dest / filename
        img.save(out_path, "PNG")
    print(f"Wrote {len(PHONE_SHOTS)} {label} screenshots to {dest}")


def main() -> None:
    OUT.mkdir(parents=True, exist_ok=True)
    SCREENSHOTS_OUT.mkdir(parents=True, exist_ok=True)

    icon_512 = render_icon(512)
    icon_512.save(OUT / "icon-512.png", "PNG")
    print(f"Wrote {OUT / 'icon-512.png'}")

    feature = render_feature_graphic()
    feature.save(OUT / "feature-1024x500.png", "PNG")
    print(f"Wrote {OUT / 'feature-1024x500.png'}")

    copy_phone_shots()
    render_tablet_set("tablet-7in", 1200, 1920)
    render_tablet_set("tablet-10in", 1600, 2560)


if __name__ == "__main__":
    main()
