import asyncio
import glob
import os
import re
import sys
from playwright.async_api import async_playwright
from PIL import Image

OUT_DIR = "/Users/sargon/.gemini/antigravity/brain/3ef0a392-e328-48be-860c-cefa718f80ae/galeria_completa"
WEB_DIR = os.path.join(OUT_DIR, "web")
COMP_DIR = os.path.join(OUT_DIR, "comparacion")

os.makedirs(WEB_DIR, exist_ok=True)
os.makedirs(COMP_DIR, exist_ok=True)

# Collect all official screens
pattern = "docs/design/RosaFiesta-diseno/pantallas/*.png"
files = sorted(glob.glob(pattern))

screens = []
ignore_files = {"AdminBar.png", "Foto.png", "Icon.png", "Illo.png", "Logo.png", "Main.png", "Rosa.png", "TabBar.png"}

for f in files:
    base = os.path.basename(f)
    if base in ignore_files:
        continue
    m = re.match(r"^([a-zA-Z0-9]+)-", base)
    if m:
        raw_code = m.group(1)
        if raw_code.startswith("a"):
            code = "A" + raw_code[1:]
        else:
            code = raw_code
        screens.append((code, base, f))

print(f"Total screens to process: {len(screens)}")

async def process_all():
    async with async_playwright() as p:
        browser = await p.chromium.launch(headless=True)
        try:
            # Context for mobile screens
            mobile_context = await browser.new_context(viewport={"width": 390, "height": 844}, device_scale_factor=2)
            mobile_page = await mobile_context.new_page()

            # Context for desktop screen (A39)
            desktop_context = await browser.new_context(viewport={"width": 1440, "height": 900}, device_scale_factor=1)
            desktop_page = await desktop_context.new_page()

            count = 0
            for code, base, ref_path in screens:
                count += 1
                is_desktop = (code == "A39")
                page = desktop_page if is_desktop else mobile_page
                url = f"http://localhost:8090/?screen={code}"

                try:
                    await page.goto(url, timeout=10000)
                    # Allow Compose Wasm to render
                    await page.wait_for_timeout(600)
                    
                    web_path = os.path.join(WEB_DIR, f"{code}.png")
                    await page.screenshot(path=web_path)

                    # Generate side-by-side comparison
                    ref = Image.open(ref_path)
                    web = Image.open(web_path)

                    if is_desktop:
                        target_h = 900
                    else:
                        target_h = 844

                    ref_w = int(ref.width * (target_h / ref.height))
                    web_w = int(web.width * (target_h / web.height))

                    ref_resized = ref.resize((ref_w, target_h), Image.Resampling.LANCZOS)
                    web_resized = web.resize((web_w, target_h), Image.Resampling.LANCZOS)

                    comp = Image.new("RGB", (ref_w + web_w + 20, target_h), (240, 240, 240))
                    comp.paste(ref_resized, (0, 0))
                    comp.paste(web_resized, (ref_w + 20, 0))

                    comp_path = os.path.join(COMP_DIR, f"{code}-comparacion.png")
                    comp.save(comp_path)

                    if count % 10 == 0 or count == len(screens):
                        print(f"[{count}/{len(screens)}] Processed {code} -> {comp_path}")
                except Exception as e:
                    print(f"Error on screen {code}: {e}")

            await mobile_context.close()
            await desktop_context.close()
        finally:
            await browser.close()

if __name__ == "__main__":
    asyncio.run(process_all())
