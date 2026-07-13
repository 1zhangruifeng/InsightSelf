package com.example.insightself.util

import com.example.insightself.data.model.ZodiacNatalDto
import com.example.insightself.ui.components.ZodiacInterpretation

object ZodiacSignUtils {
    private val englishToKey = mapOf(
        "Aries" to "Aries",
        "Taurus" to "Taurus",
        "Gemini" to "Gemini",
        "Cancer" to "Cancer",
        "Leo" to "Leo",
        "Virgo" to "Virgo",
        "Libra" to "Libra",
        "Scorpio" to "Scorpio",
        "Sagittarius" to "Sagittarius",
        "Capricorn" to "Capricorn",
        "Aquarius" to "Aquarius",
        "Pisces" to "Pisces",
        "白羊座" to "Aries",
        "金牛座" to "Taurus",
        "双子座" to "Gemini",
        "巨蟹座" to "Cancer",
        "狮子座" to "Leo",
        "处女座" to "Virgo",
        "天秤座" to "Libra",
        "天蝎座" to "Scorpio",
        "射手座" to "Sagittarius",
        "摩羯座" to "Capricorn",
        "水瓶座" to "Aquarius",
        "双鱼座" to "Pisces"
    )

    fun signKey(sign: String?): String? {
        if (sign.isNullOrBlank()) return null
        return englishToKey[sign] ?: descriptions.keys.find { it.equals(sign, ignoreCase = true) }
    }

    fun displaySign(sign: String?, isChinese: Boolean, prefix: String? = null): String {
        if (sign.isNullOrBlank()) return if (isChinese) "未知" else "Unknown"
        val key = englishToKey[sign] ?: sign
        val label = if (isChinese) chineseName(key) else key
        return if (prefix.isNullOrBlank()) label else "$prefix$label"
    }

    fun interpretationFromNatal(natal: ZodiacNatalDto, isChinese: Boolean): ZodiacInterpretation {
        val sun = natal.sunSign
        val moon = natal.planets?.get("Moon")?.sign
        val rising = natal.ascendantSign
        return ZodiacInterpretation(
            sunSign = placementLabel("sun", sun, isChinese),
            sunSignKey = signKey(sun),
            sunDescription = placementDescription("sun", sun, isChinese),
            moonSign = placementLabel("moon", moon, isChinese),
            moonSignKey = signKey(moon),
            moonDescription = placementDescription("moon", moon, isChinese),
            risingSign = placementLabel("rising", rising, isChinese),
            risingSignKey = signKey(rising),
            risingDescription = placementDescription("rising", rising, isChinese)
        )
    }

    private fun placementLabel(placement: String, sign: String?, isChinese: Boolean): String {
        val prefix = when (placement) {
            "sun" -> if (isChinese) "太阳" else "Sun "
            "moon" -> if (isChinese) "月亮" else "Moon "
            "rising" -> if (isChinese) "上升" else "Rising "
            else -> ""
        }
        return displaySign(sign, isChinese, prefix)
    }

    private fun placementDescription(placement: String, sign: String?, isChinese: Boolean): String {
        val key = englishToKey[sign] ?: sign ?: return if (isChinese) "暂无解读。" else "No interpretation available."
        val copy = descriptions[key] ?: return if (isChinese) "暂无解读。" else "No interpretation available."
        return when (placement) {
            "sun" -> if (isChinese) copy.sunZh else copy.sunEn
            "moon" -> if (isChinese) copy.moonZh else copy.moonEn
            "rising" -> if (isChinese) copy.risingZh else copy.risingEn
            else -> if (isChinese) copy.sunZh else copy.sunEn
        }
    }

    private fun chineseName(key: String): String = when (key) {
        "Aries" -> "白羊座"
        "Taurus" -> "金牛座"
        "Gemini" -> "双子座"
        "Cancer" -> "巨蟹座"
        "Leo" -> "狮子座"
        "Virgo" -> "处女座"
        "Libra" -> "天秤座"
        "Scorpio" -> "天蝎座"
        "Sagittarius" -> "射手座"
        "Capricorn" -> "摩羯座"
        "Aquarius" -> "水瓶座"
        "Pisces" -> "双鱼座"
        else -> key
    }

    private data class SignCopy(
        val sunZh: String,
        val sunEn: String,
        val moonZh: String,
        val moonEn: String,
        val risingZh: String,
        val risingEn: String
    )

    private val descriptions = mapOf(
        "Aries" to SignCopy(
            "太阳白羊带来直接、行动导向的自我表达。你习惯先动起来，再在过程中校准方向。",
            "Sun in Aries brings direct, action-oriented self-expression. You tend to move first and refine direction along the way.",
            "月亮白羊需要快速被回应的情绪空间。烦躁往往来自节奏被拖慢，而不是事情本身。",
            "Moon in Aries needs emotional space that responds quickly. Frustration often comes from slowed pace, not the issue itself.",
            "上升白羊给人的第一印象是有冲劲、敢开头。你更适合用短跑式推进建立信任。",
            "Rising Aries projects momentum and initiative. Trust builds faster through short, decisive starts."
        ),
        "Taurus" to SignCopy(
            "太阳金牛重视稳定、感官与可持续节奏。你更擅长把价值慢慢累积成可见成果。",
            "Sun in Taurus values stability, sensory grounding, and sustainable pace. You build visible results steadily.",
            "月亮金牛需要可预期的安全感。熟悉的环境、固定的节律会让你更容易放松。",
            "Moon in Taurus needs predictable safety. Familiar settings and steady rhythms help you settle.",
            "上升金牛显得踏实、可靠。别人会把你视为能守住底线、把事情落地的人。",
            "Rising Taurus appears grounded and reliable. Others see you as someone who holds boundaries and delivers."
        ),
        "Gemini" to SignCopy(
            "太阳双子带来敏捷表达与好奇心。你擅长连接信息、人物与观点，在变化里找灵感。",
            "Sun in Gemini brings agile expression and curiosity. You connect ideas, people, and perspectives with ease.",
            "月亮双子需要被听懂、被回应。情绪会通过交流与书写更快理顺。",
            "Moon in Gemini needs to feel heard. Feelings sort faster through conversation and writing.",
            "上升双子显得灵活、好聊。你给人的第一印象是反应快、适应力强。",
            "Rising Gemini appears flexible and conversational. People read you as quick-minded and adaptable."
        ),
        "Cancer" to SignCopy(
            "太阳巨蟹以感受与归属驱动选择。你擅长照顾氛围，也重视关系里的安全感。",
            "Sun in Cancer is guided by feeling and belonging. You nurture atmosphere and value relational safety.",
            "月亮巨蟹情绪细腻，对熟悉的人极度保护。需要先感到安全，才会真正敞开。",
            "Moon in Cancer is sensitive and protective toward trusted people. Safety must come before openness.",
            "上升巨蟹温和、有照顾感。别人容易把你当成可以依靠的情绪锚点。",
            "Rising Cancer feels gentle and caring. Others may treat you as an emotional anchor."
        ),
        "Leo" to SignCopy(
            "太阳狮子重视自我表达与被看见。你在真诚发光时，最能带动团队士气。",
            "Sun in Leo cares about expression and being seen. You lift group morale when you shine authentically.",
            "月亮狮子需要被肯定的情绪反馈。被忽视会比被批评更伤人。",
            "Moon in Leo needs affirming emotional feedback. Being overlooked can hurt more than criticism.",
            "上升狮子自信、有存在感。你天然适合站在台前承担鼓舞角色。",
            "Rising Leo carries confidence and presence. You naturally fit roles that inspire from the front."
        ),
        "Virgo" to SignCopy(
            "太阳处女擅长分析、优化与把复杂问题拆小。你更相信可执行的改进，而不是空泛口号。",
            "Sun in Virgo excels at analysis, refinement, and breaking complexity into steps. You trust workable improvement.",
            "月亮处女通过整理与解决问题安抚情绪。失控感常来自细节没对齐。",
            "Moon in Virgo calms itself by organizing and fixing. Unease often comes from misaligned details.",
            "上升处女细致、有条理。你给人的第一印象是可靠、注重实际。",
            "Rising Virgo appears meticulous and structured. People read you as practical and dependable."
        ),
        "Libra" to SignCopy(
            "太阳天秤重视关系平衡与审美协调。你擅长在不同立场之间找到可合作的中间地带。",
            "Sun in Libra values relational balance and harmony. You find cooperative middle ground between positions.",
            "月亮天秤需要和谐的情绪环境。冲突过久会让你身心俱疲。",
            "Moon in Libra needs harmonious emotional climate. Prolonged conflict can drain you quickly.",
            "上升天秤礼貌、得体。你擅长用氛围感降低对抗成本。",
            "Rising Libra appears courteous and balanced. You lower confrontation cost through tone and grace."
        ),
        "Scorpio" to SignCopy(
            "太阳天蝎追求深度、真实与转化。你不满足于表面答案，更在意核心动机。",
            "Sun in Scorpio seeks depth, truth, and transformation. Surface answers matter less than core motives.",
            "月亮天蝎情绪浓烈，需要真实的联结。信任一旦建立，忠诚度极高。",
            "Moon in Scorpio feels intensely and needs authentic bonds. Loyalty runs deep once trust is earned.",
            "上升天蝎神秘、有穿透力。别人会感到你看得比别人更深。",
            "Rising Scorpio feels magnetic and penetrating. Others sense you see beneath the surface."
        ),
        "Sagittarius" to SignCopy(
            "太阳射手以探索、信念与远景驱动。你需要空间去理解更大的意义。",
            "Sun in Sagittarius is driven by exploration, belief, and horizon. You need space to find larger meaning.",
            "月亮射手情绪需要自由与希望感。被限制时，你会迅速感到窒息。",
            "Moon in Sagittarius needs freedom and hope to feel well. Restriction can feel suffocating fast.",
            "上升射手开朗、直率。你给人的第一印象是乐观、敢冒险。",
            "Rising Sagittarius appears open and candid. People read you as optimistic and adventurous."
        ),
        "Capricorn" to SignCopy(
            "太阳摩羯重视责任、结构与长期成果。你擅长在约束里建立可持续路径。",
            "Sun in Capricorn values responsibility, structure, and long-term outcomes. You build sustainable paths under pressure.",
            "月亮摩羯习惯先稳住再表达情绪。安全感来自可控的进展与清晰目标。",
            "Moon in Capricorn stabilizes before expressing feelings. Safety comes from progress and clear goals.",
            "上升摩羯沉稳、有分寸。别人会把你视为能扛事的人。",
            "Rising Capricorn appears composed and measured. Others see you as someone who can carry weight."
        ),
        "Aquarius" to SignCopy(
            "太阳水瓶重视独立、理念与未来感。你更关注系统改进，而不是短期情绪。",
            "Sun in Aquarius values independence, ideas, and future orientation. You focus on systems more than momentary mood.",
            "月亮水瓶需要情绪上的空间与尊重。被理解的方式比被安慰更重要。",
            "Moon in Aquarius needs emotional space and respect. Being understood matters more than being soothed.",
            "上升水瓶独特、理性。你给人的第一印象是有自己的节奏与观点。",
            "Rising Aquarius appears distinctive and rational. People sense your own rhythm and perspective."
        ),
        "Pisces" to SignCopy(
            "太阳双鱼敏感、富想象力。你擅长共情，也容易吸收环境情绪。",
            "Sun in Pisces is sensitive and imaginative. You empathize easily and absorb surrounding moods.",
            "月亮双鱼情绪像潮水，需要温柔边界。艺术、音乐或独处能帮你回神。",
            "Moon in Pisces ebbs and flows emotionally and needs gentle boundaries. Art, music, or solitude restores you.",
            "上升双鱼柔和、有包容感。别人容易向你倾诉。",
            "Rising Pisces feels soft and receptive. Others may confide in you naturally."
        )
    )
}
