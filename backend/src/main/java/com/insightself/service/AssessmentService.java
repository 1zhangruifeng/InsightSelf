package com.insightself.service;

import com.insightself.common.ApiException;
import com.insightself.common.LanguageSupport;
import com.insightself.domain.AssessmentQuestion;
import com.insightself.domain.AssessmentResult;
import com.insightself.domain.UserProfile;
import com.insightself.dto.AssessmentQuestionDto;
import com.insightself.dto.AssessmentSubmitRequest;
import com.insightself.repository.AssessmentQuestionRepository;
import com.insightself.repository.AssessmentResultRepository;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AssessmentService {
    public static final List<String> TYPES = List.of("BFI10", "MBTI", "CAREER", "WHO5", "RSES", "ATTACHMENT");
    private static final String BIG_FIVE_VERSION = "IPIP-BIG5-20-v1";
    private static final String MBTI_VERSION = "MBTI-STYLE-16-v1";
    private static final String ATTACHMENT_VERSION = "ECR-RS-GENERAL-9-v1";
    private static final String CAREER_VERSION = "ONET-MINI-IP-30-v1";
    private static final String WHO5_VERSION = "WHO-5-WELLBEING-5-v1";
    private static final String RSES_VERSION = "ROSENBERG-SELF-ESTEEM-10-v1";

    private static final String IPIP_SOURCE = "IPIP public-domain item family; app-local zh translation is not normed";
    private static final String MBTI_SOURCE = "MBTI-style preference items; official MBTI is proprietary and not bundled";
    private static final String ECR_RS_SOURCE = "ECR-RS general attachment item family; app-local zh translation is not normed";
    private static final String CAREER_SOURCE = "O*NET Mini Interest Profiler 30-item RIASEC item set; app-local zh translation is not normed";
    private static final String WHO5_SOURCE = "WHO-5 Well-Being Index item set; app-local zh translation is not normed";
    private static final String RSES_SOURCE = "Rosenberg Self-Esteem Scale 10-item public-domain item set; app-local zh translation is not normed";

    private final AssessmentQuestionRepository questionRepository;
    private final AssessmentResultRepository resultRepository;
    private final UserService userService;
    private final ProfileService profileService;

    public AssessmentService(AssessmentQuestionRepository questionRepository,
                             AssessmentResultRepository resultRepository,
                             UserService userService,
                             ProfileService profileService) {
        this.questionRepository = questionRepository;
        this.resultRepository = resultRepository;
        this.userService = userService;
        this.profileService = profileService;
    }

    public List<String> types() {
        return TYPES;
    }

    public List<AssessmentTypeInfo> typesWithLanguage(Long userId) {
        UserProfile profile = profileService.get(userId);
        boolean isChinese = profile != null && LanguageSupport.isChinese(profile.getLanguage());

        return TYPES.stream().map(type -> new AssessmentTypeInfo(
                type,
                getDisplayName(type, isChinese),
                getDescription(type, isChinese)
        )).collect(Collectors.toList());
    }

    private String getDisplayName(String type, boolean isChinese) {
        if (!isChinese) {
            return switch (type) {
                case "BFI10" -> "IPIP Big Five-20";
                case "MBTI" -> "MBTI-style Preferences";
                case "ATTACHMENT" -> "ECR-RS General Attachment";
                case "CAREER" -> "O*NET Mini Interest Profiler";
                case "WHO5" -> "WHO-5 Well-being";
                case "RSES" -> "Rosenberg Self-Esteem";
                default -> type;
            };
        }
        return switch (type) {
            case "BFI10" -> "IPIP 大五人格 20 题";
            case "MBTI" -> "MBTI 风格偏好";
            case "ATTACHMENT" -> "ECR-RS 泛关系依恋问卷";
            case "CAREER" -> "O*NET 迷你职业兴趣测评";
            case "WHO5" -> "WHO-5 幸福感指数";
            case "RSES" -> "Rosenberg 自尊量表";
            default -> type;
        };
    }

    private String getDescription(String type, boolean isChinese) {
        if (!isChinese) {
            return switch (type) {
                case "BFI10" -> "A 20-item Big Five reflection based on public-domain IPIP-style item families.";
                case "MBTI" -> "A familiar preference-style reflection module; not the official proprietary MBTI instrument.";
                case "ATTACHMENT" -> "A 9-item dimensional attachment screen using avoidance and anxiety scoring.";
                case "CAREER" -> "The official 30-item O*NET Mini Interest Profiler for RIASEC career interests.";
                case "WHO5" -> "A 5-item well-being index using the original 0-5 response scale.";
                case "RSES" -> "A 10-item self-esteem scale using the original 4-point agreement format.";
                default -> "A structured self-reflection module.";
            };
        }
        return switch (type) {
            case "BFI10" -> "基于 IPIP 公共领域题项家族的大五人格 20 题反思。";
            case "MBTI" -> "常见偏好风格反思模块；不是官方专有 MBTI 量表。";
            case "ATTACHMENT" -> "使用回避与焦虑两个维度计分的 9 题依恋筛查。";
            case "CAREER" -> "官方 O*NET 30 题迷你职业兴趣测评，输出 RIASEC 六维兴趣。";
            case "WHO5" -> "5 题幸福感指数，使用原始 0-5 频率量尺。";
            case "RSES" -> "10 题自尊量表，使用原始 4 点同意量尺。";
            default -> "结构化自我反思模块。";
        };
    }

    public List<AssessmentQuestion> questions(String type) {
        String normalized = normalizeType(type);
        return questionRepository.findByTypeOrderByDisplayOrderAsc(normalized);
    }

    public List<AssessmentQuestionDto> questionsWithLanguage(String type, Long userId) {
        UserProfile profile = profileService.get(userId);
        boolean isChinese = profile != null && LanguageSupport.isChinese(profile.getLanguage());

        String normalized = normalizeType(type);
        List<AssessmentQuestion> questions = questionRepository.findByTypeOrderByDisplayOrderAsc(normalized);

        return questions.stream().map(q -> {
            AssessmentQuestionDto dto = new AssessmentQuestionDto();
            dto.setId(q.getId());
            dto.setQuestionText(isChinese && q.getQuestionTextZh() != null && !q.getQuestionTextZh().isBlank()
                    ? q.getQuestionTextZh()
                    : q.getQuestionText());
            dto.setDimension(q.getDimension());
            dto.setReverseScore(q.isReverseScore());
            dto.setInstrumentVersion(q.getInstrumentVersion());
            dto.setSourceNote(q.getSourceNote());
            return dto;
        }).collect(Collectors.toList());
    }

    @Transactional
    public AssessmentResult submit(String type, AssessmentSubmitRequest request) {
        String normalized = normalizeType(type);
        if (request == null || request.userId() == null || request.answers() == null || request.answers().isEmpty()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "userId and answers are required");
        }
        userService.requireUser(request.userId());

        // Assessments can be completed before the birth profile. The profile is only used
        // for language preference here; profile-gated modules remain Dashboard/Bazi/Zodiac/report.
        boolean isChinese = profileService.findOptional(request.userId())
                .map(UserProfile::getLanguage)
                .filter(LanguageSupport::isChinese)
                .isPresent();

        List<AssessmentQuestion> questionList = questionRepository.findByTypeOrderByDisplayOrderAsc(normalized);
        Map<Long, AssessmentQuestion> questions = questionList.stream()
                .collect(Collectors.toMap(AssessmentQuestion::getId, q -> q));

        if (request.answers().size() != questions.size()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "all questions for the assessment type must be answered once");
        }

        Map<String, List<Integer>> grouped = new LinkedHashMap<>();
        Set<Long> seen = new HashSet<>();
        for (AssessmentSubmitRequest.Answer answer : request.answers()) {
            if (answer == null || answer.questionId() == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "questionId is required for every answer");
            }
            if (!seen.add(answer.questionId())) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "duplicate answer for question");
            }
            int minScore = minScore(normalized);
            int maxScore = maxScore(normalized);
            if (answer.score() < minScore || answer.score() > maxScore) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "scores must be from " + minScore + " to " + maxScore);
            }
            AssessmentQuestion question = questions.get(answer.questionId());
            if (question == null) {
                throw new ApiException(HttpStatus.BAD_REQUEST, "question does not belong to assessment type");
            }
            int score = question.isReverseScore() ? minScore + maxScore - answer.score() : answer.score();
            grouped.computeIfAbsent(question.getDimension(), ignored -> new ArrayList<>()).add(score);
        }

        ScoredAssessment scored = switch (normalized) {
            case "MBTI" -> scoreMbti(grouped, isChinese);
            case "ATTACHMENT" -> scoreAttachment(grouped, isChinese);
            case "CAREER" -> scoreCareerAnchors(grouped, isChinese);
            case "WHO5" -> scoreWho5(grouped, isChinese);
            case "RSES" -> scoreRses(grouped, isChinese);
            default -> scoreBigFive(grouped, isChinese);
        };

        AssessmentResult result = new AssessmentResult();
        result.setUserId(request.userId());
        result.setType(normalized);
        result.setInstrumentVersion(instrumentVersion(normalized));
        result.setResultLabel(scored.label());
        result.setResultJson(AssessmentResult.resultJson(scored.scores(), scored.details()));
        result.setSummary(scored.summary());
        return resultRepository.save(result);
    }

    public List<AssessmentResult> results(Long userId) {
        userService.requireUser(userId);
        return resultRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Bean
    ApplicationRunner seedAssessmentQuestions() {
        return args -> seedQuestions();
    }

    @Transactional
    public void seedQuestions() {
        // Existing installations may already contain the earlier demo seeds. Replace by count
        // so an upgraded app receives the full instruments without manual DB cleanup.
        replaceQuestionsIfNeeded("BFI10", bigFiveQuestions());
        replaceQuestionsIfNeeded("MBTI", mbtiQuestions());
        replaceQuestionsIfNeeded("ATTACHMENT", attachmentQuestions());
        replaceQuestionsIfNeeded("CAREER", careerAnchorQuestions());
        replaceQuestionsIfNeeded("WHO5", who5Questions());
        replaceQuestionsIfNeeded("RSES", rsesQuestions());
    }

    private void replaceQuestionsIfNeeded(String type, List<AssessmentQuestion> questions) {
        if (questionRepository.countByType(type) == questions.size()) {
            return;
        }
        questionRepository.deleteByType(type);
        questionRepository.saveAll(questions);
    }

    private List<AssessmentQuestion> bigFiveQuestions() {
        return List.of(
                question("BFI10", BIG_FIVE_VERSION, "IPIP-EXT-1", "Am the life of the party.", "我在聚会或小组中常常很活跃。", "Extraversion", false, 1, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-AGR-1", "Am interested in people.", "我对他人的经历和想法感兴趣。", "Agreeableness", false, 2, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-CON-1", "Am always prepared.", "我通常会提前准备。", "Conscientiousness", false, 3, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-NEU-1", "Get stressed out easily.", "我很容易感到压力。", "Neuroticism", false, 4, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-OPN-1", "Have a vivid imagination.", "我有丰富的想象力。", "Openness", false, 5, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-EXT-2", "Feel comfortable around people.", "我和人相处时通常很自在。", "Extraversion", false, 6, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-AGR-2", "Sympathize with others' feelings.", "我会体会他人的感受。", "Agreeableness", false, 7, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-CON-2", "Pay attention to details.", "我会注意细节。", "Conscientiousness", false, 8, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-NEU-2", "Worry about things.", "我会为事情担心。", "Neuroticism", false, 9, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-OPN-2", "Have excellent ideas.", "我经常有不错的新想法。", "Openness", false, 10, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-EXT-R1", "Keep in the background.", "我倾向于待在背景中。", "Extraversion", true, 11, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-AGR-R1", "Insult people.", "我有时会贬低或冒犯他人。", "Agreeableness", true, 12, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-CON-R1", "Leave my belongings around.", "我常把东西随手乱放。", "Conscientiousness", true, 13, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-NEU-R1", "Am relaxed most of the time.", "我大多数时候都比较放松。", "Neuroticism", true, 14, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-OPN-R1", "Have difficulty understanding abstract ideas.", "我理解抽象想法时会比较吃力。", "Openness", true, 15, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-EXT-R2", "Don't talk a lot.", "我话不多。", "Extraversion", true, 16, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-AGR-R2", "Feel little concern for others.", "我较少关心他人的处境。", "Agreeableness", true, 17, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-CON-R2", "Make a mess of things.", "我容易把事情弄得混乱。", "Conscientiousness", true, 18, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-NEU-R2", "Seldom feel blue.", "我很少感到低落。", "Neuroticism", true, 19, IPIP_SOURCE),
                question("BFI10", BIG_FIVE_VERSION, "IPIP-OPN-R2", "Do not have a good imagination.", "我的想象力不强。", "Openness", true, 20, IPIP_SOURCE)
        );
    }

    private List<AssessmentQuestion> mbtiQuestions() {
        return List.of(
                question("MBTI", MBTI_VERSION, "MBTI-E-1", "I gain energy from active social settings.", "我从活跃的社交场合中获得能量。", "E/I:E", false, 1, MBTI_SOURCE),
                question("MBTI", MBTI_VERSION, "MBTI-I-1", "I prefer quiet time before responding.", "我喜欢先安静思考再回应。", "E/I:I", false, 2, MBTI_SOURCE),
                question("MBTI", MBTI_VERSION, "MBTI-S-1", "I focus first on concrete facts.", "我首先关注具体事实。", "S/N:S", false, 3, MBTI_SOURCE),
                question("MBTI", MBTI_VERSION, "MBTI-N-1", "I enjoy exploring future possibilities.", "我喜欢探索未来的可能性。", "S/N:N", false, 4, MBTI_SOURCE),
                question("MBTI", MBTI_VERSION, "MBTI-T-1", "I make decisions by comparing principles and logic.", "我通过比较原则和逻辑做决定。", "T/F:T", false, 5, MBTI_SOURCE),
                question("MBTI", MBTI_VERSION, "MBTI-F-1", "I make decisions by considering personal impact.", "我通过考虑个人影响做决定。", "T/F:F", false, 6, MBTI_SOURCE),
                question("MBTI", MBTI_VERSION, "MBTI-J-1", "I like plans to be settled early.", "我喜欢尽早确定计划。", "J/P:J", false, 7, MBTI_SOURCE),
                question("MBTI", MBTI_VERSION, "MBTI-P-1", "I prefer keeping options open.", "我更喜欢保持选择开放。", "J/P:P", false, 8, MBTI_SOURCE),
                question("MBTI", MBTI_VERSION, "MBTI-E-2", "I often start conversations when a group is quiet.", "当小组沉默时，我常会主动开启话题。", "E/I:E", false, 9, MBTI_SOURCE),
                question("MBTI", MBTI_VERSION, "MBTI-I-2", "I recharge best after time alone.", "独处最能帮我恢复能量。", "E/I:I", false, 10, MBTI_SOURCE),
                question("MBTI", MBTI_VERSION, "MBTI-S-2", "I trust practical experience more than speculation.", "比起推测，我更信任实际经验。", "S/N:S", false, 11, MBTI_SOURCE),
                question("MBTI", MBTI_VERSION, "MBTI-N-2", "I notice patterns and meanings behind events.", "我会注意事件背后的模式和意义。", "S/N:N", false, 12, MBTI_SOURCE),
                question("MBTI", MBTI_VERSION, "MBTI-T-2", "I can debate ideas directly without taking it personally.", "我能直接讨论观点而不把它当作私人攻击。", "T/F:T", false, 13, MBTI_SOURCE),
                question("MBTI", MBTI_VERSION, "MBTI-F-2", "I try to preserve harmony when people disagree.", "当大家意见不同时，我会努力维持和谐。", "T/F:F", false, 14, MBTI_SOURCE),
                question("MBTI", MBTI_VERSION, "MBTI-J-2", "Clear schedules help me relax.", "清晰的日程会让我更放松。", "J/P:J", false, 15, MBTI_SOURCE),
                question("MBTI", MBTI_VERSION, "MBTI-P-2", "I improvise well when plans change.", "计划变化时，我能很好地随机应变。", "J/P:P", false, 16, MBTI_SOURCE)
        );
    }

    private List<AssessmentQuestion> careerAnchorQuestions() {
        return List.of(
                question("CAREER", CAREER_VERSION, "ONET-R-1", "Build kitchen cabinets.", "制作厨房橱柜。", "Realistic", false, 1, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-I-1", "Develop a new medicine.", "研发一种新药。", "Investigative", false, 2, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-A-1", "Write books or plays.", "写书或剧本。", "Artistic", false, 3, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-S-1", "Teach an individual an exercise routine.", "教别人一套锻炼动作。", "Social", false, 4, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-E-1", "Buy and sell stocks and bonds.", "买卖股票和债券。", "Enterprising", false, 5, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-C-1", "Develop a spreadsheet using computer software.", "使用电脑软件制作电子表格。", "Conventional", false, 6, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-R-2", "Lay brick or tile.", "铺砖或瓷砖。", "Realistic", false, 7, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-I-2", "Study ways to reduce water pollution.", "研究减少水污染的方法。", "Investigative", false, 8, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-A-2", "Play a musical instrument.", "演奏乐器。", "Artistic", false, 9, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-S-2", "Help people with personal or emotional problems.", "帮助别人处理个人或情绪问题。", "Social", false, 10, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-E-2", "Manage a retail store.", "管理一家零售店。", "Enterprising", false, 11, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-C-2", "Proofread records or forms.", "校对记录或表格。", "Conventional", false, 12, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-R-3", "Repair household appliances.", "维修家用电器。", "Realistic", false, 13, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-I-3", "Conduct chemical experiments.", "进行化学实验。", "Investigative", false, 14, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-A-3", "Compose or arrange music.", "作曲或编曲。", "Artistic", false, 15, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-S-3", "Give career guidance to people.", "为他人提供职业指导。", "Social", false, 16, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-E-3", "Operate a beauty salon or barber shop.", "经营美容院或理发店。", "Enterprising", false, 17, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-C-3", "Operate a calculator.", "操作计算器。", "Conventional", false, 18, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-R-4", "Assemble electronic parts.", "组装电子零件。", "Realistic", false, 19, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-I-4", "Study the movement of planets.", "研究行星运动。", "Investigative", false, 20, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-A-4", "Draw pictures.", "画画。", "Artistic", false, 21, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-S-4", "Do volunteer work at a non-profit organization.", "在非营利组织做志愿服务。", "Social", false, 22, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-E-4", "Start your own business.", "创办自己的事业。", "Enterprising", false, 23, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-C-4", "Keep shipping and receiving records.", "维护收发货记录。", "Conventional", false, 24, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-R-5", "Test the quality of parts before shipment.", "发货前测试零件质量。", "Realistic", false, 25, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-I-5", "Examine blood samples using a microscope.", "用显微镜检查血液样本。", "Investigative", false, 26, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-A-5", "Create special effects for movies.", "为电影制作特效。", "Artistic", false, 27, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-S-5", "Teach children how to read.", "教儿童阅读。", "Social", false, 28, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-E-5", "Sell merchandise at a department store.", "在百货商店销售商品。", "Enterprising", false, 29, CAREER_SOURCE),
                question("CAREER", CAREER_VERSION, "ONET-C-5", "Stamp, sort, and distribute mail for an organization.", "为机构盖章、分拣并分发邮件。", "Conventional", false, 30, CAREER_SOURCE)
        );
    }

    private List<AssessmentQuestion> attachmentQuestions() {
        return List.of(
                question("ATTACHMENT", ATTACHMENT_VERSION, "ECRRS-GEN-1", "It helps to turn to people in times of need.", "需要帮助时，向他人求助是有帮助的。", "Avoidance", true, 1, ECR_RS_SOURCE),
                question("ATTACHMENT", ATTACHMENT_VERSION, "ECRRS-GEN-2", "I usually discuss my problems and concerns with others.", "我通常会和他人讨论自己的问题与担忧。", "Avoidance", true, 2, ECR_RS_SOURCE),
                question("ATTACHMENT", ATTACHMENT_VERSION, "ECRRS-GEN-3", "I talk things over with people.", "我会和他人把事情谈开。", "Avoidance", true, 3, ECR_RS_SOURCE),
                question("ATTACHMENT", ATTACHMENT_VERSION, "ECRRS-GEN-4", "I find it easy to depend on others.", "我觉得依靠他人并不困难。", "Avoidance", true, 4, ECR_RS_SOURCE),
                question("ATTACHMENT", ATTACHMENT_VERSION, "ECRRS-GEN-5", "I don't feel comfortable opening up to others.", "向他人敞开心扉会让我不舒服。", "Avoidance", false, 5, ECR_RS_SOURCE),
                question("ATTACHMENT", ATTACHMENT_VERSION, "ECRRS-GEN-6", "I prefer not to show others how I feel deep down.", "我倾向于不让他人看到我内心深处的感受。", "Avoidance", false, 6, ECR_RS_SOURCE),
                question("ATTACHMENT", ATTACHMENT_VERSION, "ECRRS-GEN-7", "I often worry that other people do not really care for me.", "我经常担心别人其实并不在乎我。", "Anxiety", false, 7, ECR_RS_SOURCE),
                question("ATTACHMENT", ATTACHMENT_VERSION, "ECRRS-GEN-8", "I'm afraid that other people may abandon me.", "我害怕别人会离开我。", "Anxiety", false, 8, ECR_RS_SOURCE),
                question("ATTACHMENT", ATTACHMENT_VERSION, "ECRRS-GEN-9", "I worry that others won't care about me as much as I care about them.", "我担心别人不会像我在乎他们那样在乎我。", "Anxiety", false, 9, ECR_RS_SOURCE)
        );
    }

    private List<AssessmentQuestion> who5Questions() {
        return List.of(
                question("WHO5", WHO5_VERSION, "WHO5-1", "I have felt cheerful and in good spirits.", "我感到快乐、心情愉快。", "Well-being", false, 1, WHO5_SOURCE),
                question("WHO5", WHO5_VERSION, "WHO5-2", "I have felt calm and relaxed.", "我感到平静和放松。", "Well-being", false, 2, WHO5_SOURCE),
                question("WHO5", WHO5_VERSION, "WHO5-3", "I have felt active and vigorous.", "我感到充满活力、精力充沛。", "Well-being", false, 3, WHO5_SOURCE),
                question("WHO5", WHO5_VERSION, "WHO5-4", "I woke up feeling fresh and rested.", "我醒来时感到清新、休息充分。", "Well-being", false, 4, WHO5_SOURCE),
                question("WHO5", WHO5_VERSION, "WHO5-5", "My daily life has been filled with things that interest me.", "我的日常生活充满令我感兴趣的事情。", "Well-being", false, 5, WHO5_SOURCE)
        );
    }

    private List<AssessmentQuestion> rsesQuestions() {
        return List.of(
                question("RSES", RSES_VERSION, "RSES-1", "On the whole, I am satisfied with myself.", "总体来说，我对自己感到满意。", "Self-esteem", false, 1, RSES_SOURCE),
                question("RSES", RSES_VERSION, "RSES-2", "At times I think I am no good at all.", "有时我觉得自己一无是处。", "Self-esteem", true, 2, RSES_SOURCE),
                question("RSES", RSES_VERSION, "RSES-3", "I feel that I have a number of good qualities.", "我觉得自己有不少优点。", "Self-esteem", false, 3, RSES_SOURCE),
                question("RSES", RSES_VERSION, "RSES-4", "I am able to do things as well as most other people.", "我能把事情做得和大多数人一样好。", "Self-esteem", false, 4, RSES_SOURCE),
                question("RSES", RSES_VERSION, "RSES-5", "I feel I do not have much to be proud of.", "我觉得自己没有太多值得骄傲的地方。", "Self-esteem", true, 5, RSES_SOURCE),
                question("RSES", RSES_VERSION, "RSES-6", "I certainly feel useless at times.", "我有时确实觉得自己没用。", "Self-esteem", true, 6, RSES_SOURCE),
                question("RSES", RSES_VERSION, "RSES-7", "I feel that I am a person of worth, at least on an equal plane with others.", "我觉得自己是有价值的人，至少和别人一样有价值。", "Self-esteem", false, 7, RSES_SOURCE),
                question("RSES", RSES_VERSION, "RSES-8", "I wish I could have more respect for myself.", "我希望自己能更尊重自己。", "Self-esteem", true, 8, RSES_SOURCE),
                question("RSES", RSES_VERSION, "RSES-9", "All in all, I am inclined to feel that I am a failure.", "总的来说，我倾向于觉得自己是失败的。", "Self-esteem", true, 9, RSES_SOURCE),
                question("RSES", RSES_VERSION, "RSES-10", "I take a positive attitude toward myself.", "我对自己持积极态度。", "Self-esteem", false, 10, RSES_SOURCE)
        );
    }

    private AssessmentQuestion question(String type,
                                        String version,
                                        String itemKey,
                                        String text,
                                        String textZh,
                                        String dimension,
                                        boolean reverse,
                                        int order,
                                        String sourceNote) {
        return new AssessmentQuestion(type, version, itemKey, text, textZh, dimension, reverse, order, sourceNote);
    }

    private String normalizeType(String type) {
        if (type == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "assessment type is required");
        }
        String normalized = type.trim().toUpperCase();
        if (!TYPES.contains(normalized)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "unsupported assessment type");
        }
        return normalized;
    }

    private ScoredAssessment scoreBigFive(Map<String, List<Integer>> grouped, boolean isChinese) {
        Map<String, Double> scores = average(grouped);
        String strongest = scores.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElseThrow();
        String label = isChinese ? mapDimensionToChinese(strongest) : strongest;
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("instrument", BIG_FIVE_VERSION);
        details.put("sourceNote", IPIP_SOURCE);
        details.put("interpretation", "Higher averages indicate stronger self-reported endorsement on this 1-5 app scale; no population norm is applied.");
        String summary = isChinese
                ? "您的大五人格最高维度是" + label + "。分数反映本次自评中相对更强的习惯倾向，不等同于常模诊断。"
                : "Your strongest Big Five dimension in this run is " + label + ". Scores are relative self-ratings on this app scale, not normed diagnoses.";
        return new ScoredAssessment(label, scores, details, summary);
    }

    private ScoredAssessment scoreMbti(Map<String, List<Integer>> grouped, boolean isChinese) {
        Map<String, Double> scores = average(grouped);
        String type = pick(scores, "E/I:E", "E/I:I", "E", "I")
                + pick(scores, "S/N:S", "S/N:N", "S", "N")
                + pick(scores, "T/F:T", "T/F:F", "T", "F")
                + pick(scores, "J/P:J", "J/P:P", "J", "P");
        String label = isChinese ? mapMBTIToChinese(type) : type;

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("instrument", MBTI_VERSION);
        details.put("sourceNote", MBTI_SOURCE);
        details.put("preferenceMargins", Map.of(
                "E/I", margin(scores, "E/I:E", "E/I:I"),
                "S/N", margin(scores, "S/N:S", "S/N:N"),
                "T/F", margin(scores, "T/F:T", "T/F:F"),
                "J/P", margin(scores, "J/P:J", "J/P:P")
        ));
        details.put("interpretation", "This is an MBTI-style preference shorthand for reflection. The official MBTI instrument is proprietary and not bundled.");
        String summary = isChinese
                ? "您的 MBTI 风格偏好结果为" + label + "。它用于沟通偏好反思，不是官方 MBTI 量表或人格诊断。"
                : "Your MBTI-style preference result is " + label + ". It supports communication reflection and is not the official MBTI instrument or a diagnosis.";
        return new ScoredAssessment(label, scores, details, summary);
    }

    private ScoredAssessment scoreAttachment(Map<String, List<Integer>> grouped, boolean isChinese) {
        Map<String, Double> scores = average(grouped);
        double avoidance = scores.getOrDefault("Avoidance", 0.0);
        double anxiety = scores.getOrDefault("Anxiety", 0.0);
        String label;
        if (avoidance < 3.0 && anxiety < 3.0) {
            label = isChinese ? "安全型倾向" : "Secure-leaning";
        } else if (avoidance >= 3.0 && anxiety >= 3.0) {
            label = isChinese ? "恐惧-回避型倾向" : "Fearful-avoidant leaning";
        } else if (anxiety >= avoidance) {
            label = isChinese ? "焦虑型倾向" : "Anxious-leaning";
        } else {
            label = isChinese ? "疏离-回避型倾向" : "Dismissive-avoidant leaning";
        }

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("instrument", ATTACHMENT_VERSION);
        details.put("sourceNote", ECR_RS_SOURCE);
        details.put("thresholdNote", "The app uses 1-5 response options, so 3.0 is treated as the local midpoint for high/low classification.");
        details.put("highestDimension", scores.entrySet().stream()
                .max(Comparator.comparingDouble(Map.Entry::getValue))
                .map(Map.Entry::getKey)
                .orElseThrow());
        String summary = isChinese
                ? "您的依恋结果为" + label + "。请同时查看回避与焦虑两个维度，而不是只看单一标签。"
                : "Your attachment result is " + label + ". Read avoidance and anxiety together rather than relying only on the label.";
        return new ScoredAssessment(label, scores, details, summary);
    }

    private ScoredAssessment scoreCareerAnchors(Map<String, List<Integer>> grouped, boolean isChinese) {
        Map<String, Double> scores = average(grouped);
        List<Map.Entry<String, Double>> ranked = scores.entrySet().stream()
                .sorted(Map.Entry.<String, Double>comparingByValue().reversed())
                .toList();
        String primaryKey = ranked.get(0).getKey();
        String secondaryKey = ranked.size() > 1 ? ranked.get(1).getKey() : primaryKey;
        String primaryLabel = careerAnchorLabel(primaryKey, isChinese);
        String secondaryLabel = careerAnchorLabel(secondaryKey, isChinese);

        Map<String, Object> details = new LinkedHashMap<>();
        details.put("instrument", CAREER_VERSION);
        details.put("sourceNote", CAREER_SOURCE);
        details.put("primaryInterest", primaryKey);
        details.put("secondaryInterest", secondaryKey);
        details.put("interpretation", "Scores reflect self-reported O*NET RIASEC career interests on a 1-5 app scale.");
        String summary = isChinese
                ? "您的主职业兴趣是" + primaryLabel + "，次职业兴趣是" + secondaryLabel
                + "。请结合 RIASEC 六个维度的相对高低一起阅读，而不是只看单一标签。"
                : "Your primary O*NET interest area is " + primaryLabel + " and your secondary interest area is " + secondaryLabel
                + ". Read the relative highs and lows across all six RIASEC dimensions rather than relying on one label alone.";
        return new ScoredAssessment(primaryLabel, scores, details, summary);
    }

    private ScoredAssessment scoreWho5(Map<String, List<Integer>> grouped, boolean isChinese) {
        List<Integer> values = grouped.getOrDefault("Well-being", List.of());
        int raw = values.stream().mapToInt(Integer::intValue).sum();
        int percentage = raw * 4;
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("Well-being", (double) percentage);
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("instrument", WHO5_VERSION);
        details.put("sourceNote", WHO5_SOURCE);
        details.put("rawScore", raw);
        details.put("percentageScore", percentage);
        details.put("interpretation", "WHO-5 raw scores range from 0 to 25 and are multiplied by 4 for a 0-100 well-being score.");
        String label = percentage >= 72 ? (isChinese ? "较高幸福感" : "Higher well-being")
                : percentage >= 52 ? (isChinese ? "中等幸福感" : "Moderate well-being")
                : (isChinese ? "较低幸福感" : "Lower well-being");
        String summary = isChinese
                ? "您的 WHO-5 幸福感分数为 " + percentage + "/100。请将其作为近期状态观察，不作为临床判断。"
                : "Your WHO-5 well-being score is " + percentage + "/100. Treat it as a recent-state reflection, not a clinical judgement.";
        return new ScoredAssessment(label, scores, details, summary);
    }

    private ScoredAssessment scoreRses(Map<String, List<Integer>> grouped, boolean isChinese) {
        List<Integer> values = grouped.getOrDefault("Self-esteem", List.of());
        int raw = values.stream().mapToInt(Integer::intValue).sum();
        Map<String, Double> scores = new LinkedHashMap<>();
        scores.put("Self-esteem", (double) raw);
        Map<String, Object> details = new LinkedHashMap<>();
        details.put("instrument", RSES_VERSION);
        details.put("sourceNote", RSES_SOURCE);
        details.put("rawScore", raw);
        details.put("scoreRange", "10-40");
        details.put("interpretation", "Higher scores reflect stronger self-reported global self-esteem on the 10-40 scale.");
        String label = raw >= 31 ? (isChinese ? "较高自尊" : "Higher self-esteem")
                : raw >= 21 ? (isChinese ? "中等自尊" : "Moderate self-esteem")
                : (isChinese ? "较低自尊" : "Lower self-esteem");
        String summary = isChinese
                ? "您的 Rosenberg 自尊分数为 " + raw + "/40。请把它作为自我接纳状态的反思提示。"
                : "Your Rosenberg self-esteem score is " + raw + "/40. Use it as a reflection prompt about self-acceptance.";
        return new ScoredAssessment(label, scores, details, summary);
    }

    private String careerAnchorLabel(String key, boolean isChinese) {
        if (!isChinese) {
            return switch (key) {
                case "Realistic" -> "Realistic";
                case "Investigative" -> "Investigative";
                case "Artistic" -> "Artistic";
                case "Social" -> "Social";
                case "Enterprising" -> "Enterprising";
                case "Conventional" -> "Conventional";
                default -> key;
            };
        }
        return switch (key) {
            case "Realistic" -> "现实型";
            case "Investigative" -> "研究型";
            case "Artistic" -> "艺术型";
            case "Social" -> "社会型";
            case "Enterprising" -> "企业型";
            case "Conventional" -> "事务型";
            default -> key;
        };
    }

    private String instrumentVersion(String type) {
        return switch (type) {
            case "MBTI" -> MBTI_VERSION;
            case "ATTACHMENT" -> ATTACHMENT_VERSION;
            case "CAREER" -> CAREER_VERSION;
            case "WHO5" -> WHO5_VERSION;
            case "RSES" -> RSES_VERSION;
            default -> BIG_FIVE_VERSION;
        };
    }

    private int minScore(String type) {
        return "WHO5".equals(type) ? 0 : 1;
    }

    private int maxScore(String type) {
        return switch (type) {
            case "RSES" -> 4;
            default -> 5;
        };
    }

    private Map<String, Double> average(Map<String, List<Integer>> grouped) {
        Map<String, Double> scores = new LinkedHashMap<>();
        grouped.forEach((dimension, values) -> scores.put(dimension,
                values.stream().mapToInt(Integer::intValue).average().orElseThrow()));
        return scores;
    }

    private String pick(Map<String, Double> scores, String leftKey, String rightKey, String left, String right) {
        double leftVal = scores.getOrDefault(leftKey, 0.0);
        double rightVal = scores.getOrDefault(rightKey, 0.0);
        return leftVal >= rightVal ? left : right;
    }

    private double margin(Map<String, Double> scores, String leftKey, String rightKey) {
        double value = scores.getOrDefault(leftKey, 0.0) - scores.getOrDefault(rightKey, 0.0);
        return Math.round(value * 100.0) / 100.0;
    }

    private String mapMBTIToChinese(String mbti) {
        return switch (mbti) {
            case "INTJ" -> "建筑师型 (INTJ)";
            case "INTP" -> "逻辑学家型 (INTP)";
            case "ENTJ" -> "指挥官型 (ENTJ)";
            case "ENTP" -> "辩论家型 (ENTP)";
            case "INFJ" -> "提倡者型 (INFJ)";
            case "INFP" -> "调停者型 (INFP)";
            case "ENFJ" -> "主人公型 (ENFJ)";
            case "ENFP" -> "竞选者型 (ENFP)";
            case "ISTJ" -> "物流师型 (ISTJ)";
            case "ISFJ" -> "守护者型 (ISFJ)";
            case "ESTJ" -> "总经理型 (ESTJ)";
            case "ESFJ" -> "执政官型 (ESFJ)";
            case "ISTP" -> "鉴赏家型 (ISTP)";
            case "ISFP" -> "探险家型 (ISFP)";
            case "ESTP" -> "企业家型 (ESTP)";
            case "ESFP" -> "表演者型 (ESFP)";
            default -> mbti;
        };
    }

    private String mapDimensionToChinese(String dimension) {
        return switch (dimension) {
            case "Extraversion" -> "外向性";
            case "Agreeableness" -> "宜人性";
            case "Conscientiousness" -> "尽责性";
            case "Neuroticism" -> "神经质";
            case "Openness" -> "开放性";
            default -> dimension;
        };
    }

    private record ScoredAssessment(String label, Map<String, Double> scores, Map<String, Object> details, String summary) {
    }

    public static class AssessmentTypeInfo {
        private final String type;
        private final String displayName;
        private final String description;

        public AssessmentTypeInfo(String type, String displayName, String description) {
            this.type = type;
            this.displayName = displayName;
            this.description = description;
        }

        public String getType() {
            return type;
        }

        public String getDisplayName() {
            return displayName;
        }

        public String getDescription() {
            return description;
        }
    }
}
