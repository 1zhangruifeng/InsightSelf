# InsightSelf Mockups (HTML + PNG)

This folder contains static mobile UI mockups for the *InsightSelf* app proposal. Pages include **PRD-aligned interaction notes** (expandable sections, FAB + bottom sheet, dialogs, banners).

## Quick start

Open **`index.html`** in a browser for links to all screens.

The HTML mockups can be viewed without Node.js. Node.js/npm are only required when regenerating screenshot artifacts.

```powershell
npm ci
npm run render
```

## Files

| File | Description |
|------|-------------|
| `index.html` | Hub linking all prototypes |
| `login_register.html` | Login / register fields, validation hints, password visibility |
| `onboarding.html` | Post-registration profile (gender, read-only sign, CH/EN, prefs) |
| `home_dashboard.html` | Dashboard cards + **interactive** AI bottom sheet & exit confirm |
| `bazi_detail.html` | Bazi hero quote, missing-hour banner, Five Elements radar, **十神** bars, collapsible sections |
| `zodiac_detail.html` | Sun/Moon/Rising headline, daily board, layered expandable advice |
| `assessment_list.html` | Questionnaire list with status badges (BFI-10, ECR-R, anchors, MBTI-style) |
| `assessment_question.html` | Questionnaire detail (single question screen): progress bar, Likert options, **interactive** back confirmation |
| `assessment_result.html` | Result interpretation screen: dimension bars, expandable explanation, share buttons |
| `profile_center.html` | Profile rows + **interactive** logout confirmation + **interactive** edit mode toggle (view ↔ edit) |

Extended PRD (HTML): `../InsightSelf_PRD.html`.
