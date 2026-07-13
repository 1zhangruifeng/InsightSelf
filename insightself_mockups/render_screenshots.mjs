import { chromium } from "playwright";
import path from "node:path";
import { fileURLToPath } from "node:url";

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);

const pages = [
  { html: "login_register.html", png: "01_login_register.png" },
  { html: "onboarding.html", png: "02_onboarding.png" },
  { html: "home_dashboard.html", png: "03_home_dashboard.png" },
  { html: "bazi_detail.html", png: "04_bazi_detail.png" },
  { html: "zodiac_detail.html", png: "05_zodiac_detail.png" },
  { html: "assessment_list.html", png: "06_assessment_list.png" },
  { html: "profile_center.html", png: "07_profile_center.png" },
];

const viewport = { width: 390, height: 844 };

async function main() {
  // Use locally installed Chrome to avoid downloading browsers.
  const browser = await chromium.launch({ channel: "chrome" });
  const context = await browser.newContext({
    viewport,
    deviceScaleFactor: 2,
  });

  for (const p of pages) {
    const filePath = path.join(__dirname, p.html);
    const url = new URL(`file://${filePath}`);
    const page = await context.newPage();
    await page.goto(url.toString(), { waitUntil: "networkidle" });

    await page.waitForTimeout(150);

    const outPath = path.join(__dirname, p.png);
    await page.screenshot({ path: outPath, fullPage: false });
    await page.close();
  }

  await context.close();
  await browser.close();
}

main().catch((err) => {
  console.error(err);
  process.exitCode = 1;
});

