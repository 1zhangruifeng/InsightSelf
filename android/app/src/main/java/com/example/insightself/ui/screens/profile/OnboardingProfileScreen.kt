package com.example.insightself.ui.screens.profile

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.insightself.R
import com.example.insightself.data.local.LanguageManager
import com.example.insightself.data.local.RegionCatalog
import com.example.insightself.data.model.ProfileDto
import com.example.insightself.ui.components.AppTextField
import com.example.insightself.ui.components.AppTopBar
import com.example.insightself.ui.components.BirthDateTimeFields
import com.example.insightself.ui.components.AppLocaleContent
import com.example.insightself.ui.components.BirthRegionPicker
import com.example.insightself.ui.components.InsightCard
import com.example.insightself.ui.components.LanguageToggleRow
import com.example.insightself.ui.components.PrimaryActionButton
import com.example.insightself.ui.components.ProfileRequiredFieldsLegend
import com.example.insightself.ui.components.ProfileFieldRequirements
import com.example.insightself.ui.components.RequiredFieldLabel
import com.example.insightself.ui.components.normalizeBirthTimeForApi
import com.example.insightself.ui.screens.auth.ScreenBackground
import com.example.insightself.ui.theme.InsightCardStrong
import com.example.insightself.ui.theme.InsightMuted
import com.example.insightself.ui.theme.InsightPrimary
import com.example.insightself.ui.theme.InsightShapes
import com.example.insightself.ui.theme.InsightSpacing
import com.example.insightself.ui.theme.InsightStroke
import com.example.insightself.ui.theme.InsightText
import com.example.insightself.util.SampleNameGenerator
import com.example.insightself.viewmodel.ProfileViewModel
import com.example.insightself.viewmodel.UiState

@Composable
fun OnboardingProfileScreen(
    profileViewModel: ProfileViewModel,
    onComplete: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val regionRoot = remember { RegionCatalog.load(context) }
    val defaultProvince = regionRoot.provinces.firstOrNull()
    val defaultCity = defaultProvince?.cities?.firstOrNull()
    val defaultDistrict = defaultCity?.districts?.firstOrNull()

    var nickname by remember { mutableStateOf("") }
    var gender by remember { mutableStateOf("Prefer not to say") }
    var birthDate by remember { mutableStateOf("") }
    var birthTime by remember { mutableStateOf("") }
    var selectedProvince by remember { mutableStateOf(defaultProvince?.name.orEmpty()) }
    var selectedCity by remember { mutableStateOf(defaultCity?.name.orEmpty()) }
    var selectedDistrict by remember { mutableStateOf(defaultDistrict?.name.orEmpty()) }
    var calendarType by remember { mutableStateOf("SOLAR") }
    var preference by remember { mutableStateOf("BALANCED") }
    var aiEnabled by remember { mutableStateOf(false) }
    var localError by remember { mutableStateOf<String?>(null) }
    // Default English on first open; survives language toggle without Activity recreate.
    var currentLanguage by rememberSaveable { mutableStateOf("en") }

    val profileState by profileViewModel.profileState.collectAsState()
    val loading = profileState is UiState.Loading
    val nicknamePlaceholder = remember(currentLanguage) {
        SampleNameGenerator.nicknamePlaceholder(currentLanguage == "zh")
    }

    fun switchLanguage(languageCode: String) {
        if (currentLanguage == languageCode) return
        scope.launch {
            LanguageManager.saveLanguage(context, languageCode)
            currentLanguage = languageCode
        }
    }

    LaunchedEffect(profileState) {
        if (profileState is UiState.Success) {
            LanguageManager.saveLanguage(context, currentLanguage)
            profileViewModel.resetProfileState()
            onComplete()
        }
    }

    AppLocaleContent(languageCode = currentLanguage) {
        ScreenBackground {
            Column(modifier = Modifier.fillMaxSize()) {
                AppTopBar(
                    title = stringResource(R.string.create_your_profile),
                    subtitle = stringResource(R.string.profile_subtitle),
                    onBack = onBackToLogin,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = InsightSpacing.ScreenHorizontal)
                        .padding(top = 24.dp)
                )
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = InsightSpacing.ScreenHorizontal)
                        .padding(bottom = 24.dp),
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    InsightCard {
                LanguageToggleRow(
                    currentLanguage = currentLanguage,
                    onSelectLanguage = ::switchLanguage,
                    enabled = !loading
                )

                ProfileRequiredFieldsLegend(isChinese = currentLanguage == "zh")

                AppTextField(
                    value = nickname,
                    onValueChange = { nickname = it },
                    label = stringResource(R.string.nickname),
                    labelRequirement = ProfileFieldRequirements.nickname,
                    placeholder = nicknamePlaceholder,
                    isChinese = currentLanguage == "zh"
                )

                BirthDateTimeFields(
                    birthDate = birthDate,
                    birthTime = birthTime,
                    onBirthDateChange = { birthDate = it },
                    onBirthTimeChange = { birthTime = it },
                    isChinese = currentLanguage == "zh"
                )

                BirthRegionPicker(
                    provinces = regionRoot.provinces,
                    selectedProvince = selectedProvince,
                    selectedCity = selectedCity,
                    selectedDistrict = selectedDistrict,
                    onSelectionChange = { province, city, district ->
                        selectedProvince = province
                        selectedCity = city
                        selectedDistrict = district
                    },
                    isChinese = currentLanguage == "zh"
                )

                GenderOptionGroup(
                    selectedValue = gender,
                    onSelected = { gender = it }
                )

                CalendarTypeOptionGroup(
                    selectedValue = calendarType,
                    onSelected = { calendarType = it },
                    isChinese = currentLanguage == "zh"
                )

                PreferenceOptionGroup(
                    selectedValue = preference,
                    onSelected = { preference = it }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(InsightCardStrong, RoundedCornerShape(InsightShapes.ControlRadius))
                        .border(BorderStroke(1.dp, InsightStroke), RoundedCornerShape(InsightShapes.ControlRadius))
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = stringResource(R.string.ai_enabled), style = MaterialTheme.typography.titleMedium)
                        Text(
                            text = stringResource(R.string.ai_description),
                            color = InsightMuted,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                    Switch(checked = aiEnabled, onCheckedChange = { aiEnabled = it })
                }

                val errorText = localError ?: (profileState as? UiState.Error)?.message
                if (errorText != null) {
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

                    PrimaryActionButton(
                        text = stringResource(R.string.continue_btn),
                        onClick = {
                    val birthPlace = RegionCatalog.formatBirthPlace(
                        selectedProvince,
                        selectedCity,
                        selectedDistrict
                    )
                    val fullBirthTime = normalizeBirthTimeForApi(birthTime)
                    localError = validateProfile(
                        nickname,
                        birthDate,
                        fullBirthTime,
                        birthPlace,
                        calendarType,
                        preference,
                        currentLanguage == "zh"
                    )
                    if (localError == null) {
                        profileViewModel.createProfile(
                            ProfileDto(
                                nickname = nickname.trim(),
                                gender = gender,
                                birthDate = birthDate.trim(),
                                birthTime = fullBirthTime,
                                birthPlace = birthPlace,
                                calendarType = calendarType,
                                preference = preference,
                                aiEnabled = aiEnabled,
                                language = currentLanguage
                            )
                        )
                    }
                        },
                        enabled = !loading,
                        loading = loading
                    )
                    Text(
                        text = stringResource(R.string.ai_info),
                        color = InsightMuted,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }
    }
}

@Composable
private fun GenderOptionGroup(
    selectedValue: String,
    onSelected: (String) -> Unit
) {
    val preferNotText = stringResource(R.string.gender_prefer_not)
    val femaleText = stringResource(R.string.gender_female)
    val maleText = stringResource(R.string.gender_male)

    val options = listOf(preferNotText, femaleText, maleText)
    val selectedDisplayText = when (selectedValue) {
        "Female" -> femaleText
        "Male" -> maleText
        else -> preferNotText
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = stringResource(R.string.gender), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { option ->
                val isSelected = option == selectedDisplayText
                Text(
                    text = option,
                    modifier = Modifier
                        .background(
                            color = if (isSelected) InsightPrimary.copy(alpha = 0.12f) else InsightCardStrong,
                            shape = RoundedCornerShape(InsightShapes.PillRadius)
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                if (isSelected) InsightPrimary.copy(alpha = 0.42f) else InsightStroke
                            ),
                            RoundedCornerShape(InsightShapes.PillRadius)
                        )
                        .clickable {
                            val newValue = when (option) {
                                femaleText -> "Female"
                                maleText -> "Male"
                                else -> "Prefer not to say"
                            }
                            onSelected(newValue)
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    color = if (isSelected) InsightPrimary else InsightText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun CalendarTypeOptionGroup(
    selectedValue: String,
    onSelected: (String) -> Unit,
    isChinese: Boolean
) {
    val solarText = stringResource(R.string.calendar_solar)
    val lunarText = stringResource(R.string.calendar_lunar)

    val options = listOf(solarText, lunarText)
    val selectedDisplayText = when (selectedValue) {
        "LUNAR" -> lunarText
        else -> solarText
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        RequiredFieldLabel(
            text = stringResource(R.string.calendar_type),
            requirement = ProfileFieldRequirements.calendarType,
            isChinese = isChinese
        )
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { option ->
                val isSelected = option == selectedDisplayText
                Text(
                    text = option,
                    modifier = Modifier
                        .background(
                            color = if (isSelected) InsightPrimary.copy(alpha = 0.12f) else InsightCardStrong,
                            shape = RoundedCornerShape(InsightShapes.PillRadius)
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                if (isSelected) InsightPrimary.copy(alpha = 0.42f) else InsightStroke
                            ),
                            RoundedCornerShape(InsightShapes.PillRadius)
                        )
                        .clickable {
                            val newValue = when (option) {
                                lunarText -> "LUNAR"
                                else -> "SOLAR"
                            }
                            onSelected(newValue)
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    color = if (isSelected) InsightPrimary else InsightText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun PreferenceOptionGroup(
    selectedValue: String,
    onSelected: (String) -> Unit
) {
    val easternText = stringResource(R.string.preference_eastern)
    val westernText = stringResource(R.string.preference_western)
    val balancedText = stringResource(R.string.preference_balanced)

    val options = listOf(easternText, westernText, balancedText)
    val selectedDisplayText = when (selectedValue) {
        "EASTERN" -> easternText
        "WESTERN" -> westernText
        else -> balancedText
    }

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(text = stringResource(R.string.preference_lens), style = MaterialTheme.typography.titleMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            options.forEach { option ->
                val isSelected = option == selectedDisplayText
                Text(
                    text = option,
                    modifier = Modifier
                        .background(
                            color = if (isSelected) InsightPrimary.copy(alpha = 0.12f) else InsightCardStrong,
                            shape = RoundedCornerShape(InsightShapes.PillRadius)
                        )
                        .border(
                            BorderStroke(
                                1.dp,
                                if (isSelected) InsightPrimary.copy(alpha = 0.42f) else InsightStroke
                            ),
                            RoundedCornerShape(InsightShapes.PillRadius)
                        )
                        .clickable {
                            val newValue = when (option) {
                                easternText -> "EASTERN"
                                westernText -> "WESTERN"
                                else -> "BALANCED"
                            }
                            onSelected(newValue)
                        }
                        .padding(horizontal = 14.dp, vertical = 10.dp),
                    color = if (isSelected) InsightPrimary else InsightText,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

private fun validateProfile(
    nickname: String,
    birthDate: String,
    birthTime: String,
    birthPlace: String,
    calendarType: String,
    preference: String,
    isChinese: Boolean
): String? {
    val dateRegex = Regex("""\d{4}-\d{2}-\d{2}""")
    val timeRegex = Regex("""\d{2}:\d{2}:\d{2}""")
    return when {
        nickname.trim().isBlank() -> if (isChinese) "请填写昵称。" else "Nickname is required."
        !dateRegex.matches(birthDate.trim()) -> if (isChinese) "请选择出生日期。" else "Birth date must use yyyy-MM-dd."
        !timeRegex.matches(birthTime.trim()) -> if (isChinese) "请选择完整的出生时间（时:分）。" else "Birth time must use HH:mm."
        birthPlace.split("|").size < 3 -> if (isChinese) "请选择完整的出生地点（省/市/区）。" else "Please select province, city, and district."
        calendarType !in listOf("SOLAR", "LUNAR") -> if (isChinese) "历法类型无效。" else "Calendar type must be SOLAR or LUNAR."
        preference !in listOf("EASTERN", "WESTERN", "BALANCED") -> if (isChinese) "偏好类型无效。" else "Preference must be EASTERN, WESTERN, or BALANCED."
        else -> null
    }
}
