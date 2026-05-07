"""Render the 512x512 Play Store icon and the 1024x500 feature graphic.

Reproduces the same scales-of-justice mark used in the in-app adaptive icon
(see app/src/main/res/drawable/ic_launcher_foreground.xml). Run from the
repo root:

    python play-store/graphics/render_graphics.py

Outputs:
    play-store/graphics/icon-512.png
    play-store/graphics/feature-1024x500.png
"""
from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path(__file__).resolve().parents[2]
OUT = Path(__file__).resolve().parent
OUT.mkdir(parents=True, exist_ok=True)

COURT_BLUE = (30, 58, 138)
COURT_BLUE_DARK = (30, 41, 59)
COURT_GOLD = (251, 191, 36)
INK = (241, 245, 249)
INK_MUTED = (148, 163, 184)
BG = (11, 18, 32)


def render_icon(size: int = 512) -> Image.Image:
    """Render the Jury Simulator mark at `size` x `size`."""
    img = Image.new("RGB", (size, size), COURT_BLUE)
    overlay = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    od = ImageDraw.Draw(overlay)
    s = size / 108.0
    # Subtle dark wave overlay (matches drawable/ic_launcher_background.xml)
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
        (52, 30, 56, 80),  # pillar
        (30, 80, 78, 84),  # base
        (26, 30, 82, 34),  # crossbeam
        (30, 34, 32, 42),  # left chain
        (76, 34, 78, 42),  # right chain
        (50, 24, 58, 30),  # top knob
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


def load_font(paths: list[str], size: int) -> ImageFont.FreeTypeFont:
    for p in paths:
        try:
            return ImageFont.truetype(p, size)
        except OSError:
            continue
    return ImageFont.load_default()


def render_feature_graphic(width: int = 1024, height: int = 500) -> Image.Image:
    """Render the 1024x500 Play Store feature graphic."""
    base = Image.new("RGB", (width, height), BG)

    # Two soft glows: warm gold tint left, deep blue glow right.
    glow = Image.new("RGBA", (width, height), (0, 0, 0, 0))
    gd = ImageDraw.Draw(glow)
    gd.ellipse((-260, -180, 520, 580), fill=(*COURT_GOLD, 28))
    gd.ellipse((420, -120, 1280, 700), fill=(*COURT_BLUE, 110))
    base = Image.alpha_composite(base.convert("RGBA"), glow).convert("RGB")

    d = ImageDraw.Draw(base)

    # Left: logo + wordmark + tagline
    icon = render_icon(160)
    base.paste(icon, (60, 90))

    title_font = load_font(
        [
            "C:/Windows/Fonts/georgiab.ttf",
            "C:/Windows/Fonts/timesbd.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSerif-Bold.ttf",
        ],
        size=58,
    )
    eyebrow_font = load_font(
        [
            "C:/Windows/Fonts/segoeuib.ttf",
            "C:/Windows/Fonts/arialbd.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans-Bold.ttf",
        ],
        size=16,
    )
    sub_font = load_font(
        [
            "C:/Windows/Fonts/segoeui.ttf",
            "C:/Windows/Fonts/arial.ttf",
            "/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf",
        ],
        size=20,
    )

    # Right side: phone mockup first so we know its left edge.
    mockup_left = width
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
        mockup_left = x

    d = ImageDraw.Draw(base)
    text_x = 250
    text_max_w = max(420, mockup_left - text_x - 40)
    d.text((text_x, 110), "TRIAL  ·  VERDICT  ·  REPEAT", font=eyebrow_font, fill=COURT_GOLD)
    d.text((text_x, 138), "Jury Simulator", font=title_font, fill=INK)
    d.text((text_x, 220), "Decide justice from your phone.", font=sub_font, fill=INK_MUTED)
    d.text(
        (text_x, 252),
        "Free · Android · Runs on-device",
        font=sub_font,
        fill=INK_MUTED,
    )

    return base


def main() -> None:
    icon_512 = render_icon(512)
    icon_512.save(OUT / "icon-512.png", "PNG")
    print(f"Wrote {OUT / 'icon-512.png'}")

    feature = render_feature_graphic()
    feature.save(OUT / "feature-1024x500.png", "PNG")
    print(f"Wrote {OUT / 'feature-1024x500.png'}")


if __name__ == "__main__":
    main()
