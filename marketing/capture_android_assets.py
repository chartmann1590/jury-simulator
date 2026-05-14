import os
import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET


SERIAL = "37220DLJG001ML"
OUT_DIR = "H:/Jury-Sim/marketing/assets/raw"


def run(cmd):
    return subprocess.run(cmd, check=True, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)


def dump_ui(filename="window_dump.xml"):
    run(["adb", "-s", SERIAL, "shell", "uiautomator", "dump", "/sdcard/window_dump.xml"])
    run(["adb", "-s", SERIAL, "pull", "/sdcard/window_dump.xml", f"{OUT_DIR}/{filename}"])
    return ET.parse(f"{OUT_DIR}/{filename}").getroot()


def screenshot(name):
    p = subprocess.run(
        ["adb", "-s", SERIAL, "exec-out", "screencap", "-p"],
        check=True,
        stdout=subprocess.PIPE,
    )
    with open(f"{OUT_DIR}/{name}.png", "wb") as f:
        f.write(p.stdout)


def tap_text(*candidates):
    root = dump_ui()
    for candidate in candidates:
        for n in root.iter("node"):
            text = (n.attrib.get("text") or "").strip()
            if text == candidate:
                b = n.attrib.get("bounds", "")
                m = re.match(r"\[(\d+),(\d+)\]\[(\d+),(\d+)\]", b)
                if not m:
                    continue
                x = (int(m.group(1)) + int(m.group(3))) // 2
                y = (int(m.group(2)) + int(m.group(4))) // 2
                run(["adb", "-s", SERIAL, "shell", "input", "tap", str(x), str(y)])
                time.sleep(2)
                return candidate
    return None


def tap_if_exists(*candidates):
    try:
        return tap_text(*candidates)
    except Exception:
        return None


def print_texts(limit=80):
    root = dump_ui("window_dump_debug.xml")
    texts = []
    for n in root.iter("node"):
        t = (n.attrib.get("text") or "").strip()
        if t:
            texts.append(t)
    print("\n".join(texts[:limit]))


def ensure_out_dir():
    os.makedirs(OUT_DIR, exist_ok=True)


def main():
    ensure_out_dir()

    run(["adb", "-s", SERIAL, "shell", "am", "start", "-n", "com.charles.jurysim/.MainActivity"])
    time.sleep(3)

    # Dismiss Android compatibility warning if present.
    tap_if_exists("OK")
    tap_if_exists("Don't Show Again")
    tap_if_exists("OK")

    screenshot("shot1-home")

    # Navigate through app flow and capture incremental screens.
    tap_text("New Case")
    screenshot("shot2-new-case")

    # Try to progress through likely onboarding/setup/model paths.
    for label in ("Continue", "Next", "Start", "Save", "Done", "Connect"):
        if tap_if_exists(label):
            screenshot(f"shot3-{label.lower()}")
            break

    # Capture whatever simulation screen is currently visible.
    screenshot("shot4-current")

    # Try likely trial progression actions.
    for label in ("Continue", "Next Phase", "Proceed", "Deliberation", "Vote", "Submit Vote"):
        if tap_if_exists(label):
            time.sleep(2)
            screenshot(f"shot5-{label.lower().replace(' ', '-')}")
            break

    screenshot("shot6-current")
    print_texts()


if __name__ == "__main__":
    try:
        main()
    except Exception as e:
        print(f"capture failed: {e}", file=sys.stderr)
        sys.exit(1)
