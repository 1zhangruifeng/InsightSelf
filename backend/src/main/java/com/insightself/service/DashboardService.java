package com.insightself.service;

import com.insightself.common.WellnessSafetyNotice;
import com.insightself.common.LanguageSupport;
import com.insightself.domain.AiReport;
import com.insightself.domain.AssessmentResult;
import com.insightself.domain.BaziResult;
import com.insightself.domain.UserProfile;
import com.insightself.domain.ZodiacResult;
import com.insightself.dto.AiReportResponse;
import com.insightself.dto.DashboardResponse;
import com.insightself.repository.AiReportRepository;
import com.insightself.repository.AssessmentResultRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DashboardService {
    private final ProfileService profileService;
    private final BaziService baziService;
    private final ZodiacService zodiacService;
    private final AssessmentResultRepository assessmentResultRepository;
    private final AiReportRepository aiReportRepository;

    public DashboardService(ProfileService profileService, BaziService baziService, ZodiacService zodiacService,
                            AssessmentResultRepository assessmentResultRepository, AiReportRepository aiReportRepository) {
        this.profileService = profileService;
        this.baziService = baziService;
        this.zodiacService = zodiacService;
        this.assessmentResultRepository = assessmentResultRepository;
        this.aiReportRepository = aiReportRepository;
    }

    public DashboardResponse load(Long userId) {
        UserProfile profile = profileService.get(userId);
        BaziResult bazi = baziService.latestOrGenerate(userId);
        ZodiacResult zodiac = zodiacService.daily(userId);
        List<AssessmentResult> assessments = assessmentResultRepository.findByUserIdOrderByCreatedAtDesc(userId);
        AiReportResponse latestReport = aiReportRepository.findTopByUserIdOrderByCreatedAtDesc(userId)
                .map(report -> AiReportResponse.from(report, profile.getLanguage()))
                .orElse(null);
        String summary = integratedSummary(profile, bazi, zodiac, assessments);
        return new DashboardResponse(
                profile,
                bazi,
                zodiac,
                assessments,
                latestReport,
                summary,
                WellnessSafetyNotice.forLanguage(profile.getLanguage())
        );
    }

    private String integratedSummary(UserProfile profile, BaziResult bazi, ZodiacResult zodiac, List<AssessmentResult> assessments) {
        boolean isChinese = LanguageSupport.isChinese(profile.getLanguage());
        if (isChinese) {
            return getChineseSummary(profile, bazi, zodiac, assessments);
        } else {
            return getEnglishSummary(profile, bazi, zodiac, assessments);
        }
    }

    private String getEnglishSummary(UserProfile profile, BaziResult bazi, ZodiacResult zodiac, List<AssessmentResult> assessments) {
        String assessmentPart;
        if (assessments.isEmpty()) {
            assessmentPart = "No assessment has been completed yet, so the dashboard keeps this part open.";
        } else {
            AssessmentResult latest = assessments.get(0);
            String typeEnglish = getAssessmentTypeEnglish(latest.getType());
            String resultEnglish = getAssessmentResultEnglish(latest.getResultLabel());
            assessmentPart = "The latest assessment is " + typeEnglish + " with result " + resultEnglish + ".";
        }
        return "For " + profile.getNickname() + ", today's self-reflection combines " + zodiac.getZodiacSign()
                + " daily indicators with a calendar-based Bazi element balance led by "
                + strongestElement(bazi) + ". " + assessmentPart
                + " Treat this as a gentle planning summary, not a diagnosis or prediction.";
    }

    private String getChineseSummary(UserProfile profile, BaziResult bazi, ZodiacResult zodiac, List<AssessmentResult> assessments) {
        String assessmentPart;
        if (assessments.isEmpty()) {
            assessmentPart = "暂无完成的测评，主页将保留此区域开放。";
        } else {
            AssessmentResult latest = assessments.get(0);
            String typeChinese = getAssessmentTypeChinese(latest.getType());
            String resultChinese = getAssessmentResultChinese(latest.getResultLabel());
            assessmentPart = "最新测评是" + typeChinese + "，结果为" + resultChinese + "。";
        }
        
        String zodiacChinese = getZodiacSignChinese(zodiac.getZodiacSign());
        String strongestElement = getStrongestElementChinese(bazi);
        
        return "对于" + profile.getNickname() + "，今日的自我反思结合了" + zodiacChinese
                + "的每日星象指标和以" + strongestElement + "为主的八字五行结构。"
                + assessmentPart + "请将此视为温和的计划总结，而非诊断或预测。";
    }

    private String strongestElement(BaziResult bazi) {
        return bazi.getElementScores().entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("balanced elements");
    }

    private String getStrongestElementChinese(BaziResult bazi) {
        String element = bazi.getElementScores().entrySet().stream()
                .max(java.util.Map.Entry.comparingByValue())
                .map(java.util.Map.Entry::getKey)
                .orElse("平衡元素");
        switch (element.toLowerCase()) {
            case "wood": return "木";
            case "fire": return "火";
            case "earth": return "土";
            case "metal": return "金";
            case "water": return "水";
            default: return element;
        }
    }

    private String getZodiacSignChinese(String sign) {
        if (sign == null || sign.isBlank()) return "未知";
        switch (sign) {
            case "Aries": return "白羊座";
            case "Taurus": return "金牛座";
            case "Gemini": return "双子座";
            case "Cancer": return "巨蟹座";
            case "Leo": return "狮子座";
            case "Virgo": return "处女座";
            case "Libra": return "天秤座";
            case "Scorpio": return "天蝎座";
            case "Sagittarius": return "射手座";
            case "Capricorn": return "摩羯座";
            case "Aquarius": return "水瓶座";
            case "Pisces": return "双鱼座";
            default: return sign;
        }
    }

    // ==================== 测评类型映射 ====================
    
    private String getAssessmentTypeChinese(String type) {
        if (type == null) return "测评";
        switch (type) {
            case "BFI10": return "IPIP 大五人格 20 题";
            case "MBTI": return "MBTI 风格偏好";
            case "ATTACHMENT": return "依恋风格";
            case "CAREER": return "O*NET 职业兴趣";
            case "WHO5": return "WHO-5 幸福感";
            case "RSES": return "Rosenberg 自尊";
            default: return type;
        }
    }

    private String getAssessmentTypeEnglish(String type) {
        if (type == null) return "Assessment";
        switch (type) {
            case "BFI10": return "IPIP Big Five-20";
            case "MBTI": return "MBTI-style Preferences";
            case "ATTACHMENT": return "Attachment";
            case "CAREER": return "O*NET Career Interests";
            case "WHO5": return "WHO-5 Well-being";
            case "RSES": return "Rosenberg Self-Esteem";
            default: return type;
        }
    }

    // ==================== 测评结果映射 ====================
    
    private String getAssessmentResultChinese(String resultLabel) {
        if (resultLabel == null) return "结果";
        switch (resultLabel) {
            // 依恋风格结果（英文 -> 中文）
            case "Secure": return "安全型";
            case "Anxious-leaning": return "焦虑型倾向";
            case "Avoidant-leaning": return "回避型倾向";
            // Legacy MBTI result
            case "INTJ": return "建筑师型";
            case "INTP": return "逻辑学家型";
            case "ENTJ": return "指挥官型";
            case "ENTP": return "辩论家型";
            case "INFJ": return "提倡者型";
            case "INFP": return "调停者型";
            case "ENFJ": return "主人公型";
            case "ENFP": return "竞选者型";
            case "ISTJ": return "物流师型";
            case "ISFJ": return "守护者型";
            case "ESTJ": return "总经理型";
            case "ESFJ": return "执政官型";
            case "ISTP": return "鉴赏家型";
            case "ISFP": return "探险家型";
            case "ESTP": return "企业家型";
            case "ESFP": return "表演者型";
            // IPIP Big Five-20 result
            case "Extraversion": return "外向性";
            case "Agreeableness": return "宜人性";
            case "Conscientiousness": return "尽责性";
            case "Neuroticism": return "神经质";
            case "Openness": return "开放性";
            case "Realistic": return "现实型";
            case "Investigative": return "研究型";
            case "Artistic": return "艺术型";
            case "Social": return "社会型";
            case "Enterprising": return "企业型";
            case "Conventional": return "事务型";
            case "Higher well-being": return "较高幸福感";
            case "Moderate well-being": return "中等幸福感";
            case "Lower well-being": return "较低幸福感";
            case "Higher self-esteem": return "较高自尊";
            case "Moderate self-esteem": return "中等自尊";
            case "Lower self-esteem": return "较低自尊";
            default: return resultLabel;
        }
    }

    private String getAssessmentResultEnglish(String resultLabel) {
        if (resultLabel == null) return "Result";
        // 中文 -> 英文映射
        switch (resultLabel) {
            case "安全型": return "Secure";
            case "焦虑型倾向": return "Anxious-leaning";
            case "回避型倾向": return "Avoidant-leaning";
            case "外向性": return "Extraversion";
            case "宜人性": return "Agreeableness";
            case "尽责性": return "Conscientiousness";
            case "神经质": return "Neuroticism";
            case "开放性": return "Openness";
            default: return resultLabel;
        }
    }

}
