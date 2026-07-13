import fs from "node:fs";
import { pinyin } from "pinyin-pro";

const root = JSON.parse(
  fs.readFileSync(new URL("../app/src/main/assets/cn_regions.json", import.meta.url), "utf8")
);

const PROVINCE_EN = {
  北京市: "Beijing",
  天津市: "Tianjin",
  河北省: "Hebei",
  山西省: "Shanxi",
  内蒙古自治区: "Inner Mongolia",
  辽宁省: "Liaoning",
  吉林省: "Jilin",
  黑龙江省: "Heilongjiang",
  上海市: "Shanghai",
  江苏省: "Jiangsu",
  浙江省: "Zhejiang",
  安徽省: "Anhui",
  福建省: "Fujian",
  江西省: "Jiangxi",
  山东省: "Shandong",
  河南省: "Henan",
  湖北省: "Hubei",
  湖南省: "Hunan",
  广东省: "Guangdong",
  广西壮族自治区: "Guangxi",
  海南省: "Hainan",
  重庆市: "Chongqing",
  四川省: "Sichuan",
  贵州省: "Guizhou",
  云南省: "Yunnan",
  西藏自治区: "Tibet",
  陕西省: "Shaanxi",
  甘肃省: "Gansu",
  青海省: "Qinghai",
  宁夏回族自治区: "Ningxia",
  新疆维吾尔自治区: "Xinjiang",
  香港特别行政区: "Hong Kong SAR",
  澳门特别行政区: "Macau SAR",
  台湾省: "Taiwan",
  市辖区: "Municipal districts",
};

function titleCasePinyin(zh) {
  return pinyin(zh, { toneType: "none", type: "array" })
    .map((part) => part.charAt(0).toUpperCase() + part.slice(1))
    .join(" ");
}

function toEnglish(zh) {
  if (PROVINCE_EN[zh]) return PROVINCE_EN[zh];
  if (zh.endsWith("自治区")) {
    const base = zh.replace(/壮族自治区|回族自治区|维吾尔自治区|自治区$/, "");
    return `${titleCasePinyin(base)} Autonomous Region`;
  }
  if (zh.endsWith("特别行政区")) {
    return `${titleCasePinyin(zh.replace("特别行政区", ""))} SAR`;
  }
  if (zh.endsWith("地区")) {
    return `${titleCasePinyin(zh.replace("地区", ""))} Prefecture`;
  }
  if (zh.endsWith("盟")) {
    return `${titleCasePinyin(zh.replace("盟", ""))} League`;
  }
  if (zh.endsWith("自治州")) {
    return `${titleCasePinyin(zh.replace("自治州", ""))} Autonomous Prefecture`;
  }
  if (zh.endsWith("自治县")) {
    return `${titleCasePinyin(zh.replace("自治县", ""))} Autonomous County`;
  }
  if (zh.endsWith("新区")) {
    return `${titleCasePinyin(zh.replace("新区", ""))} New District`;
  }
  if (zh.endsWith("区")) {
    return `${titleCasePinyin(zh.replace("区", ""))} District`;
  }
  if (zh.endsWith("县")) {
    return `${titleCasePinyin(zh.replace("县", ""))} County`;
  }
  if (zh.endsWith("市")) {
    return `${titleCasePinyin(zh.replace("市", ""))} City`;
  }
  if (zh.endsWith("省")) {
    return `${titleCasePinyin(zh.replace("省", ""))} Province`;
  }
  return titleCasePinyin(zh);
}

const labels = {};
for (const province of root.provinces) {
  labels[province.name] = toEnglish(province.name);
  for (const city of province.cities) {
    labels[city.name] = toEnglish(city.name);
    for (const district of city.districts) {
      labels[district.name] = toEnglish(district.name);
    }
  }
}

const outPath = new URL("../app/src/main/assets/region_labels_en.json", import.meta.url);
fs.writeFileSync(outPath, JSON.stringify({ version: 1, labels }, null, 0));
console.log(`Wrote ${Object.keys(labels).length} labels to ${outPath.pathname}`);
