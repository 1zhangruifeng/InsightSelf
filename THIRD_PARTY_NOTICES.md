# Third Party Notices

InsightSelf is licensed as AGPL-3.0-or-later because the backend now vendors Swiss Ephemeris source and data files.

## Swiss Ephemeris

- Use in this project: Western astrology natal chart, daily transit chart, and relationship matching calculations.
- Vendored Java source: `backend/src/main/java/swisseph/`.
- Bundled data files: `backend/src/main/resources/ephe/sepl_18.se1` and `backend/src/main/resources/ephe/semo_18.se1`.
- Upstream sources used during integration:
  - Java port source: https://github.com/kim-dam-petersen/swisseph-module
  - Ephemeris data files: https://github.com/aloistr/swisseph
  - Swiss Ephemeris licensing information: https://www.astro.com/swisseph/swephinfo_e.htm
- Licensing note: Swiss Ephemeris is available under open-source copyleft terms or a professional/commercial license from Astrodienst. This repository uses the open-source copyleft route and therefore changed the project license to AGPL-3.0-or-later.
- Local modifications:
  - Converted vendored Java source files to UTF-8 so they compile with the project compiler settings.
  - Preserved explicit Moshier ephemeris flags in the Java port, although the application currently uses bundled Swiss ephemeris data files for valid planetary coordinates.

## 6tail lunar-java

- Use in this project: Bazi calendar conversion and four-pillar calculation.
- Maven coordinate: `cn.6tail:lunar:1.7.7`.
- Upstream project: https://github.com/6tail/lunar-java

## Questionnaire Content

- `BFI10` API code now maps to an IPIP-style Big Five 20-item app form. IPIP item families are public-domain resources, but the Chinese translations in this project are app-local and are not normed.
- `MBTI` is retained as a 16-item MBTI-style preference module for familiar communication reflection. It is not the official MBTI instrument and should not be presented as one.
- `ATTACHMENT` now uses an ECR-RS-style 9-item general attachment form with avoidance/anxiety scoring on a 1-5 app scale. The Chinese translations are app-local and are not normed.
- `CAREER` now uses the official O*NET Mini Interest Profiler 30-item RIASEC item set (`ONET-MINI-IP-30-v1`). The Chinese translations are app-local and are not normed.
- `WHO5` uses the WHO-5 Well-Being Index item set with the original 0-5 response scale. The Chinese translations are app-local and are not normed.
- `RSES` uses the Rosenberg Self-Esteem Scale 10-item format with a 4-point agreement scale. The Chinese translations are app-local and are not normed.
- Reference pages used for questionnaire design:
  - IPIP: https://ipip.ori.org/
  - ECR-RS overview/scoring resources: https://labs.psych.ucsb.edu/fraley/rc/measures/ecrr.htm
  - O*NET Interest Profiler manual and Mini-IP materials: https://www.onetcenter.org/IP.html
  - WHO-5 Well-Being Index: https://www.psykiatri-regionh.dk/who-5/who-5-questionnaires/Pages/default.aspx
  - Rosenberg Self-Esteem Scale overview: https://socy.umd.edu/about-us/rosenberg-self-esteem-scale
