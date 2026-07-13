package com.insightself.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightself.common.ApiException;
import com.insightself.common.LanguageSupport;
import com.insightself.domain.BaziResult;
import com.insightself.domain.UserProfile;
import com.insightself.repository.BaziResultRepository;
import com.nlf.calendar.EightChar;
import com.nlf.calendar.Lunar;
import com.nlf.calendar.Solar;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class BaziService {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
    private static final String METHOD = "6tail/lunar-java 1.7.7, exact LiChun/JieQi pillar mode, element-weight-v1";
    private static final List<String> ELEMENT_ORDER = List.of("Wood", "Fire", "Earth", "Metal", "Water");
    private static final Map<String, String> STEM_ELEMENTS = Map.of(
            "甲", "Wood", "乙", "Wood",
            "丙", "Fire", "丁", "Fire",
            "戊", "Earth", "己", "Earth",
            "庚", "Metal", "辛", "Metal",
            "壬", "Water", "癸", "Water"
    );
    private static final Map<String, String> STEM_EN = Map.ofEntries(
            Map.entry("甲", "Jia Wood"), Map.entry("乙", "Yi Wood"),
            Map.entry("丙", "Bing Fire"), Map.entry("丁", "Ding Fire"),
            Map.entry("戊", "Wu Earth"), Map.entry("己", "Ji Earth"),
            Map.entry("庚", "Geng Metal"), Map.entry("辛", "Xin Metal"),
            Map.entry("壬", "Ren Water"), Map.entry("癸", "Gui Water")
    );
    private static final Map<String, String> BRANCH_EN = Map.ofEntries(
            Map.entry("子", "Zi Rat"), Map.entry("丑", "Chou Ox"),
            Map.entry("寅", "Yin Tiger"), Map.entry("卯", "Mao Rabbit"),
            Map.entry("辰", "Chen Dragon"), Map.entry("巳", "Si Snake"),
            Map.entry("午", "Wu Horse"), Map.entry("未", "Wei Goat"),
            Map.entry("申", "Shen Monkey"), Map.entry("酉", "You Rooster"),
            Map.entry("戌", "Xu Dog"), Map.entry("亥", "Hai Pig")
    );

    private final BaziResultRepository baziRepository;
    private final ProfileService profileService;

    public BaziService(BaziResultRepository baziRepository, ProfileService profileService) {
        this.baziRepository = baziRepository;
        this.profileService = profileService;
    }

    public BaziResult generate(Long userId) {
        UserProfile profile = profileService.get(userId);
        requireBirthMoment(profile);
        boolean isChinese = LanguageSupport.isChinese(profile.getLanguage());

        Lunar lunar = lunarFromProfile(profile);
        EightChar eightChar = lunar.getEightChar();
        Map<String, Integer> scores = elementScores(eightChar);
        String strongest = strongest(scores);
        String weakest = weakest(scores);

        BaziResult result = new BaziResult();
        result.setUserId(userId);
        result.setYearPillar(pillar(eightChar.getYearGan(), eightChar.getYearZhi(), isChinese));
        result.setMonthPillar(pillar(eightChar.getMonthGan(), eightChar.getMonthZhi(), isChinese));
        result.setDayPillar(pillar(eightChar.getDayGan(), eightChar.getDayZhi(), isChinese));
        result.setHourPillar(pillar(eightChar.getTimeGan(), eightChar.getTimeZhi(), isChinese));
        result.setWoodScore(scores.get("Wood"));
        result.setFireScore(scores.get("Fire"));
        result.setEarthScore(scores.get("Earth"));
        result.setMetalScore(scores.get("Metal"));
        result.setWaterScore(scores.get("Water"));
        result.setCalculationMethod(METHOD);
        result.setChartJson(chartJson(profile, lunar, eightChar, scores));

        if (isChinese) {
            result.setConclusion("此八字以 6tail/lunar-java 计算四柱：" + eightChar + "。当前结构中" + elementToChinese(strongest) + "相对突出，" + elementToChinese(weakest) + "相对较弱。");
            result.setEvidence("年、月柱采用节气交接的精确模式；五行分数来自天干、地支藏干与月令权重，而不是随机或取模规则。");
            result.setSuggestion("把它作为自我观察框架：增强" + elementToChinese(weakest) + "对应的稳定习惯，同时避免让" + elementToChinese(strongest) + "倾向过度主导决策。");
        } else {
            result.setConclusion("This Bazi chart is calculated with 6tail/lunar-java. The strongest relative element is " + strongest + ", while " + weakest + " is least represented.");
            result.setEvidence("The year and month pillars use exact solar-term transitions. Element scores come from heavenly stems, hidden stems, and month-season weighting.");
            result.setSuggestion("Use this as a reflection model: build routines that support " + weakest + " while keeping the " + strongest + " tendency proportionate.");
        }

        result.setCreatedAt(LocalDateTime.now());
        return baziRepository.save(result);
    }

    public BaziResult latest(Long userId) {
        return baziRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "bazi result not found"));
    }

    public BaziResult latestOrGenerate(Long userId) {
        return baziRepository.findTopByUserIdOrderByCreatedAtDesc(userId).orElseGet(() -> generate(userId));
    }

    private void requireBirthMoment(UserProfile profile) {
        if (profile.getBirthDate() == null || profile.getBirthTime() == null || blank(profile.getBirthTimezone())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "birthDate, birthTime, and birthTimezone are required for Bazi calculation");
        }
    }

    private Lunar lunarFromProfile(UserProfile profile) {
        LocalDate date = profile.getBirthDate();
        LocalTime time = profile.getBirthTime();
        if ("LUNAR".equalsIgnoreCase(profile.getCalendarType())) {
            return Lunar.fromYmdHms(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), time.getHour(), time.getMinute(), time.getSecond());
        }
        return Solar.fromYmdHms(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), time.getHour(), time.getMinute(), time.getSecond()).getLunar();
    }

    private Map<String, Integer> elementScores(EightChar eightChar) {
        Map<String, Double> raw = new LinkedHashMap<>();
        ELEMENT_ORDER.forEach(element -> raw.put(element, 0.0));

        addVisibleStem(raw, eightChar.getYearGan());
        addVisibleStem(raw, eightChar.getMonthGan());
        addVisibleStem(raw, eightChar.getDayGan());
        addVisibleStem(raw, eightChar.getTimeGan());
        addHiddenStems(raw, eightChar.getYearHideGan(), 1.0);
        addHiddenStems(raw, eightChar.getMonthHideGan(), 1.35);
        addHiddenStems(raw, eightChar.getDayHideGan(), 1.0);
        addHiddenStems(raw, eightChar.getTimeHideGan(), 1.0);

        double total = raw.values().stream().mapToDouble(Double::doubleValue).sum();
        Map<String, Integer> scores = new LinkedHashMap<>();
        ELEMENT_ORDER.forEach(element -> scores.put(element, (int) Math.round(raw.get(element) * 100 / total)));
        return scores;
    }

    private void addVisibleStem(Map<String, Double> raw, String stem) {
        addElement(raw, stem, 10.0);
    }

    private void addHiddenStems(Map<String, Double> raw, List<String> hiddenStems, double branchMultiplier) {
        double[] weights = {6.0, 3.0, 1.0};
        for (int i = 0; i < hiddenStems.size(); i++) {
            addElement(raw, hiddenStems.get(i), weights[Math.min(i, weights.length - 1)] * branchMultiplier);
        }
    }

    private void addElement(Map<String, Double> raw, String stem, double weight) {
        String element = STEM_ELEMENTS.get(stem);
        if (element == null) {
            throw new IllegalStateException("unknown heavenly stem: " + stem);
        }
        raw.put(element, raw.get(element) + weight);
    }

    private String pillar(String stem, String branch, boolean isChinese) {
        if (isChinese) {
            return stem + branch;
        }
        return STEM_EN.get(stem) + " - " + BRANCH_EN.get(branch) + " (" + stem + branch + ")";
    }

    private String chartJson(UserProfile profile, Lunar lunar, EightChar eightChar, Map<String, Integer> scores) {
        Map<String, Object> chart = new LinkedHashMap<>();
        chart.put("engine", "6tail/lunar-java");
        chart.put("method", METHOD);
        chart.put("input", Map.of(
                "calendarType", profile.getCalendarType(),
                "birthDate", profile.getBirthDate().toString(),
                "birthTime", profile.getBirthTime().toString(),
                "birthTimezone", profile.getBirthTimezone()
        ));
        chart.put("solar", lunar.getSolar().toYmdHms());
        chart.put("lunar", lunar.toString());
        chart.put("pillars", Map.of(
                "year", eightChar.getYear(),
                "month", eightChar.getMonth(),
                "day", eightChar.getDay(),
                "time", eightChar.getTime()
        ));
        chart.put("hiddenStems", Map.of(
                "year", eightChar.getYearHideGan(),
                "month", eightChar.getMonthHideGan(),
                "day", eightChar.getDayHideGan(),
                "time", eightChar.getTimeHideGan()
        ));
        chart.put("tenGods", Map.of(
                "yearGan", eightChar.getYearShiShenGan(),
                "yearZhi", eightChar.getYearShiShenZhi(),
                "monthGan", eightChar.getMonthShiShenGan(),
                "monthZhi", eightChar.getMonthShiShenZhi(),
                "dayGan", eightChar.getDayShiShenGan(),
                "dayZhi", eightChar.getDayShiShenZhi(),
                "timeGan", eightChar.getTimeShiShenGan(),
                "timeZhi", eightChar.getTimeShiShenZhi()
        ));
        chart.put("naYin", Map.of(
                "year", eightChar.getYearNaYin(),
                "month", eightChar.getMonthNaYin(),
                "day", eightChar.getDayNaYin(),
                "time", eightChar.getTimeNaYin()
        ));
        chart.put("xunKong", Map.of(
                "year", eightChar.getYearXunKong(),
                "month", eightChar.getMonthXunKong(),
                "day", eightChar.getDayXunKong(),
                "time", eightChar.getTimeXunKong()
        ));
        chart.put("elementScores", scores);
        try {
            return OBJECT_MAPPER.writeValueAsString(chart);
        } catch (Exception ex) {
            throw new IllegalStateException("failed to serialize bazi chart", ex);
        }
    }

    private String strongest(Map<String, Integer> scores) {
        return scores.entrySet().stream().max(Map.Entry.comparingByValue()).orElseThrow().getKey();
    }

    private String weakest(Map<String, Integer> scores) {
        return scores.entrySet().stream().min(Map.Entry.comparingByValue()).orElseThrow().getKey();
    }

    private String elementToChinese(String element) {
        return switch (element) {
            case "Wood" -> "木";
            case "Fire" -> "火";
            case "Earth" -> "土";
            case "Metal" -> "金";
            case "Water" -> "水";
            default -> element;
        };
    }

    private boolean blank(String value) {
        return value == null || value.trim().isBlank();
    }
}
