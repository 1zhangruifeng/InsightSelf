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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.insightself.MainActivity
import com.example.insightself.R
import com.example.insightself.data.local.LanguageManager
import com.example.insightself.data.local.RegionCatalog
import com.example.insightself.data.local.RegionNameTranslator
import com.example.insightself.data.local.UserSessionDataStore
import com.example.insightself.data.model.ProfileDto
import com.example.insightself.data.repository.ProfileRepository
import com.example.insightself.ui.components.AppTextField
import com.example.insightself.ui.components.AppTopBar
import com.example.insightself.ui.components.BirthDateTimeFields
import com.example.insightself.ui.components.BirthRegionPicker
import com.example.insightself.ui.components.LanguageToggleRow
import com.example.insightself.ui.components.InfoRow
import com.example.insightself.ui.components.InsightCard
import com.example.insightself.ui.components.PrimaryActionButton
import com.example.insightself.ui.components.ProfileFieldRequirements
import com.example.insightself.ui.components.ProfileRequiredFieldsLegend
import com.example.insightself.ui.components.RequiredFieldLabel
import com.example.insightself.ui.components.SecondaryActionButton
import com.example.insightself.ui.components.birthTimeHourMinute
import com.example.insightself.ui.components.normalizeBirthTimeForApi
import com.example.insightself.util.SampleNameGenerator
import com.example.insightself.ui.screens.auth.ScreenBackground
import com.example.insightself.ui.theme.InsightCardStrong
import com.example.insightself.ui.theme.InsightMuted
import com.example.insightself.ui.theme.InsightPrimary
import com.example.insightself.ui.theme.InsightShapes
import com.example.insightself.ui.theme.InsightSpacing
import com.example.insightself.ui.theme.InsightStroke
import com.example.insightself.ui.theme.InsightText
import com.example.insightself.viewmodel.LanguageViewModel
import com.example.insightself.viewmodel.ProfileUpdateViewModel
import com.example.insightself.viewmodel.AuthViewModel
import com.example.insightself.viewmodel.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    languageViewModel: LanguageViewModel,
    authViewModel: AuthViewModel,
    onDemoLoaded: () -> Unit,
    onLogout: () -> Unit
) {
    val context = LocalContext.current
    val appContext = context.applicationContext
    val scope = rememberCoroutineScope()
    val regionRoot = remember { RegionCatalog.load(context) }
    val currentLanguage by languageViewModel.currentLanguage.collectAsState()
    val isChinese = currentLanguage == "zh"

    val profileUpdateViewModel: ProfileUpdateViewModel = viewModel(
        factory = object : androidx.lifecycle.ViewModelProvider.Factory {
            override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
                @Suppress("UNCHECKED_CAST")
                return ProfileUpdateViewModel(
                    profileRepository = ProfileRepository(),
                    sessionDataStore = UserSessionDataStore(appContext)
                ) as T
            }
        }
    )

    val updateState by profileUpdateViewModel.updateState.collectAsState()
    val passwordChangeState by profileUpdateViewModel.passwordChangeState.collectAsState()
    val currentProfile by profileUpdateViewModel.currentProfile.collectAsState()
    val authState by authViewModel.authState.collectAsState()
    val isLoading = updateState is UiState.Loading
    val isDemoLoading = authState is UiState.Loading

    // 编辑模式状态
    var isEditing by remember { mutableStateOf(false) }
    var showLogoutDialog by remember { mutableStateOf(false) }
    var showChangePasswordDialog by remember { mutableStateOf(false) }
    var isLanguageUpdating by remember { mutableStateOf(false) }
    var editError by remember { mutableStateOf<String?>(null) }

    // 修改密码表单
    var oldPassword by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var isChangingPassword by remember { mutableStateOf(false) }

    // 编辑表单数据
    var editNickname by remember { mutableStateOf("") }
    var editGender by remember { mutableStateOf("") }
    var editBirthDate by remember { mutableStateOf("") }
    var editBirthTime by remember { mutableStateOf("") }
    var selectedProvince by remember { mutableStateOf(regionRoot.provinces.firstOrNull()?.name.orEmpty()) }
    var selectedCity by remember { mutableStateOf(regionRoot.provinces.firstOrNull()?.cities?.firstOrNull()?.name.orEmpty()) }
    var selectedDistrict by remember {
        mutableStateOf(
            regionRoot.provinces.firstOrNull()?.cities?.firstOrNull()?.districts?.firstOrNull()?.name.orEmpty()
        )
    }
    var editCalendarType by remember { mutableStateOf("SOLAR") }
    var editPreference by remember { mutableStateOf("BALANCED") }
    val nicknamePlaceholder = remember(isChinese) {
        SampleNameGenerator.nicknamePlaceholder(isChinese)
    }

    LaunchedEffect(Unit) {
        profileUpdateViewModel.loadProfile()
    }

    fun applyProfileToEditForm(profile: ProfileDto) {
        editNickname = profile.nickname
        editGender = profile.gender ?: "Prefer not to say"
        editBirthDate = profile.birthDate
        editBirthTime = birthTimeHourMinute(profile.birthTime)
        RegionCatalog.parseBirthPlace(profile.birthPlace)?.let { (province, city, district) ->
            selectedProvince = province
            selectedCity = city
            selectedDistrict = district
        }
        editCalendarType = profile.calendarType
        editPreference = profile.preference
    }

    // 加载数据到编辑表单
    LaunchedEffect(currentProfile) {
        currentProfile?.let { profile ->
            if (!isEditing) {
                applyProfileToEditForm(profile)
            }
            val profileLang = LanguageManager.normalizeLanguage(profile.language)
            val appLang = LanguageManager.currentLanguageBlocking(appContext)
            if (profileLang != appLang) {
                LanguageManager.saveLanguage(appContext, profileLang)
            }
        }
    }

    // 处理资料保存成功
    LaunchedEffect(updateState) {
        if (updateState is UiState.Success && isEditing) {
            isEditing = false
            profileUpdateViewModel.loadProfile()
        }
    }

    LaunchedEffect(authState) {
        val state = authState
        if (state is UiState.Success) {
            authViewModel.resetAuthState()
            android.widget.Toast.makeText(
                context,
                if (isChinese) "HKICT 演示账号已载入" else "HKICT demo account loaded",
                android.widget.Toast.LENGTH_SHORT
            ).show()
            onDemoLoaded()
        }
    }

    fun switchLanguage(languageCode: String) {
        if (currentLanguage == languageCode) return
        scope.launch {
            isLanguageUpdating = true
            LanguageManager.saveLanguage(appContext, languageCode)
            languageViewModel.updateLanguage(languageCode)
            profileUpdateViewModel.updateLanguage(languageCode) {
                isLanguageUpdating = false
                (context as? MainActivity)?.restartForLanguageChange()
            }
        }
    }

    // 监听密码修改状态
    LaunchedEffect(passwordChangeState) {
        when (passwordChangeState) {
            is UiState.Loading -> {
                isChangingPassword = true
            }
            is UiState.Success -> {
                isChangingPassword = false
                showChangePasswordDialog = false
                oldPassword = ""
                newPassword = ""
                confirmPassword = ""
                passwordError = null
                // 密码修改成功后提示
                android.widget.Toast.makeText(
                    context,
                    if (isChinese) "密码修改成功" else "Password changed successfully",
                    android.widget.Toast.LENGTH_SHORT
                ).show()
                // 重置密码修改状态
                profileUpdateViewModel.resetPasswordChangeState()
            }
            is UiState.Error -> {
                isChangingPassword = false
                passwordError = (passwordChangeState as UiState.Error).message
            }
            else -> {}
        }
    }

    fun getPreferenceDisplay(preference: String?): String {
        if (!isChinese || preference == null) return preference ?: ""
        return when (preference) {
            "EASTERN" -> "偏中式"
            "WESTERN" -> "偏西式"
            "BALANCED" -> "平衡"
            else -> preference
        }
    }

    fun getPreferenceValue(display: String): String {
        return when (display) {
            "偏中式" -> "EASTERN"
            "偏西式" -> "WESTERN"
            "平衡" -> "BALANCED"
            else -> display
        }
    }

    fun getCalendarTypeDisplay(calendarType: String?): String {
        if (!isChinese || calendarType == null) return calendarType ?: ""
        return when (calendarType) {
            "SOLAR" -> "阳历"
            "LUNAR" -> "阴历"
            else -> calendarType
        }
    }

    fun getCalendarTypeValue(display: String): String {
        return when (display) {
            "阳历" -> "SOLAR"
            "阴历" -> "LUNAR"
            else -> display
        }
    }

    fun getGenderDisplay(gender: String?): String {
        if (!isChinese || gender == null) return gender ?: ""
        return when (gender) {
            "Female" -> "女"
            "Male" -> "男"
            "Prefer not to say" -> "不愿透露"
            else -> gender
        }
    }

    fun getGenderValue(display: String): String {
        return when (display) {
            "女" -> "Female"
            "男" -> "Male"
            "不愿透露" -> "Prefer not to say"
            else -> display
        }
    }

    fun validatePassword(): Boolean {
        passwordError = null
        if (oldPassword.isBlank()) {
            passwordError = if (isChinese) "请输入原密码" else "Please enter old password"
            return false
        }
        if (newPassword.isBlank()) {
            passwordError = if (isChinese) "请输入新密码" else "Please enter new password"
            return false
        }
        if (newPassword.length < 4) {
            passwordError = if (isChinese) "密码长度至少4位" else "Password must be at least 4 characters"
            return false
        }
        if (newPassword != confirmPassword) {
            passwordError = if (isChinese) "两次输入的密码不一致" else "Passwords do not match"
            return false
        }
        if (oldPassword == newPassword) {
            passwordError = if (isChinese) "新密码不能与原密码相同" else "New password cannot be the same as old password"
            return false
        }
        return true
    }

    // 退出登录对话框
    if (showLogoutDialog) {
        AlertDialog(
            onDismissRequest = { showLogoutDialog = false },
            title = { Text(if (isChinese) "确定退出登录？" else "Logout?") },
            text = { Text(if (isChinese) "本地会话将被清除并返回登录页。" else "Local session will be cleared and you will return to login page.") },
            confirmButton = {
                TextButton(onClick = { onLogout() }) {
                    Text(if (isChinese) "退出" else "Logout", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showLogoutDialog = false }) {
                    Text(if (isChinese) "取消" else "Cancel")
                }
            }
        )
    }

    // 修改密码对话框
    if (showChangePasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!isChangingPassword) {
                    showChangePasswordDialog = false
                    passwordError = null
                    oldPassword = ""
                    newPassword = ""
                    confirmPassword = ""
                    profileUpdateViewModel.resetPasswordChangeState()
                }
            },
            title = { Text(if (isChinese) "修改密码" else "Change Password") },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = oldPassword,
                        onValueChange = { oldPassword = it },
                        label = { Text(if (isChinese) "原密码" else "Old Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isChangingPassword
                    )
                    OutlinedTextField(
                        value = newPassword,
                        onValueChange = { newPassword = it },
                        label = { Text(if (isChinese) "新密码" else "New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isChangingPassword
                    )
                    OutlinedTextField(
                        value = confirmPassword,
                        onValueChange = { confirmPassword = it },
                        label = { Text(if (isChinese) "确认新密码" else "Confirm New Password") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        enabled = !isChangingPassword
                    )
                    if (passwordError != null) {
                        Text(
                            text = passwordError!!,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                    if (isChangingPassword) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(24.dp),
                                strokeWidth = 2.dp
                            )
                            Text(
                                text = if (isChinese) "修改中..." else "Changing...",
                                modifier = Modifier.padding(start = 8.dp),
                                color = InsightMuted
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (validatePassword()) {
                            profileUpdateViewModel.changePassword(oldPassword, newPassword)
                        }
                    },
                    enabled = !isChangingPassword
                ) {
                    Text(if (isChinese) "确认" else "Confirm")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        if (!isChangingPassword) {
                            showChangePasswordDialog = false
                            passwordError = null
                            oldPassword = ""
                            newPassword = ""
                            confirmPassword = ""
                            profileUpdateViewModel.resetPasswordChangeState()
                        }
                    },
                    enabled = !isChangingPassword
                ) {
                    Text(if (isChinese) "取消" else "Cancel")
                }
            }
        )
    }

    ScreenBackground {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = InsightSpacing.ScreenHorizontal, vertical = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            AppTopBar(
                title = if (isEditing) (if (isChinese) "编辑资料" else "Edit Profile") else stringResource(R.string.nav_profile),
                subtitle = null,
                actionText = if (!isEditing && currentProfile != null) (if (isChinese) "编辑" else "Edit") else null,
                onAction = if (!isEditing && currentProfile != null) {
                    {
                        currentProfile?.let { applyProfileToEditForm(it) }
                        isEditing = true
                    }
                } else null
            )

            // 编辑模式
            if (isEditing && currentProfile != null) {
                // 编辑表单卡片
                InsightCard {
                    Text(
                        text = if (isChinese) "个人档案" else "Profile Info",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )

                    ProfileRequiredFieldsLegend(isChinese = isChinese)

                    AppTextField(
                        value = editNickname,
                        onValueChange = { editNickname = it },
                        label = stringResource(R.string.nickname),
                        labelRequirement = ProfileFieldRequirements.nickname,
                        placeholder = nicknamePlaceholder,
                        isChinese = isChinese
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = stringResource(R.string.gender),
                        style = MaterialTheme.typography.bodySmall,
                        color = InsightMuted
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val genderOptions = if (isChinese) listOf("女", "男", "不愿透露") else listOf("Female", "Male", "Prefer not to say")
                        genderOptions.forEach { option ->
                            val displayGender = if (isChinese) option else option
                            val isSelected = getGenderDisplay(editGender) == displayGender
                            Button(
                                onClick = { editGender = getGenderValue(displayGender) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(InsightShapes.PillRadius),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) InsightPrimary else InsightCardStrong,
                                    contentColor = if (isSelected) Color.White else InsightText
                                )
                            ) {
                                Text(displayGender)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    BirthDateTimeFields(
                        birthDate = editBirthDate,
                        birthTime = editBirthTime,
                        onBirthDateChange = { editBirthDate = it },
                        onBirthTimeChange = { editBirthTime = it },
                        isChinese = isChinese
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
                        isChinese = isChinese
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    RequiredFieldLabel(
                        text = stringResource(R.string.calendar_type),
                        requirement = ProfileFieldRequirements.calendarType,
                        isChinese = isChinese,
                        style = MaterialTheme.typography.bodySmall
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val solarText = stringResource(R.string.calendar_solar)
                        val lunarText = stringResource(R.string.calendar_lunar)
                        val calendarOptions = listOf(solarText, lunarText)
                        calendarOptions.forEach { option ->
                            val isSelected = when (editCalendarType) {
                                "LUNAR" -> option == lunarText
                                else -> option == solarText
                            }
                            Button(
                                onClick = {
                                    editCalendarType = if (option == lunarText) "LUNAR" else "SOLAR"
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(InsightShapes.PillRadius),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) InsightPrimary else InsightCardStrong,
                                    contentColor = if (isSelected) Color.White else InsightText
                                )
                            ) {
                                Text(option)
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    // 文化偏好
                    Text(
                        text = stringResource(R.string.preference_lens),
                        style = MaterialTheme.typography.bodySmall,
                        color = InsightMuted
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val preferenceOptions = if (isChinese) listOf("偏中式", "偏西式", "平衡") else listOf("EASTERN", "WESTERN", "BALANCED")
                        preferenceOptions.forEach { option ->
                            val isSelected = getPreferenceDisplay(editPreference) == option
                            Button(
                                onClick = { editPreference = getPreferenceValue(option) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(InsightShapes.PillRadius),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) InsightPrimary else InsightCardStrong,
                                    contentColor = if (isSelected) Color.White else InsightText
                                )
                            ) {
                                Text(option)
                            }
                        }
                    }

                    editError?.let { message ->
                        Text(
                            text = message,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                    }
                }

                // 编辑按钮
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryActionButton(
                        text = if (isChinese) "取消" else "Cancel",
                        onClick = {
                            isEditing = false
                            currentProfile?.let { applyProfileToEditForm(it) }
                        },
                        modifier = Modifier.weight(1f)
                    )
                    PrimaryActionButton(
                        text = if (isChinese) "保存" else "Save",
                        onClick = {
                            val birthPlace = RegionCatalog.formatBirthPlace(
                                selectedProvince,
                                selectedCity,
                                selectedDistrict
                            )
                            val fullBirthTime = normalizeBirthTimeForApi(editBirthTime)
                            val validationError = validateProfileEdit(
                                editNickname,
                                editBirthDate,
                                fullBirthTime,
                                birthPlace,
                                isChinese
                            )
                            if (validationError != null) {
                                editError = validationError
                                return@PrimaryActionButton
                            }
                            editError = null
                            val updatedProfile = currentProfile?.copy(
                                nickname = editNickname.trim(),
                                gender = editGender,
                                birthDate = editBirthDate.trim(),
                                birthTime = fullBirthTime,
                                birthPlace = birthPlace,
                                calendarType = editCalendarType,
                                preference = editPreference
                            )
                            updatedProfile?.let { profileUpdateViewModel.updateProfile(it) }
                        },
                        loading = isLoading,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
            // 查看模式
            else if (currentProfile != null && !isEditing) {
                // 个人信息卡片
                InsightCard {
                    Text(
                        text = if (isChinese) "个人档案" else "Profile Info",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(bottom = 12.dp)
                    )
                    InfoRow(
                        label = stringResource(R.string.nickname),
                        value = currentProfile?.nickname ?: ""
                    )
                    InfoRow(
                        label = stringResource(R.string.gender),
                        value = getGenderDisplay(currentProfile?.gender)
                    )
                    InfoRow(
                        label = stringResource(R.string.birth_date),
                        value = currentProfile?.birthDate ?: ""
                    )
                    InfoRow(
                        label = stringResource(R.string.birth_time),
                        value = currentProfile?.birthTime
                            ?.let { birthTimeHourMinute(it).ifBlank { it } }
                            ?.takeIf { it.isNotBlank() }
                            ?: (if (isChinese) "未设置" else "Not set")
                    )
                    InfoRow(
                        label = stringResource(R.string.birthplace),
                        value = currentProfile?.birthPlace
                            ?.let { place ->
                                RegionCatalog.parseBirthPlace(place)?.let { (province, city, district) ->
                                    listOf(province, city, district).joinToString(" / ") { part ->
                                        RegionNameTranslator.displayName(appContext, part, isChinese)
                                    }
                                } ?: place.replace("|", " / ")
                            }
                            ?.takeIf { it.isNotBlank() }
                            ?: (if (isChinese) "未设置" else "Not set")
                    )
                    InfoRow(
                        label = stringResource(R.string.calendar_type),
                        value = getCalendarTypeDisplay(currentProfile?.calendarType)
                    )
                    InfoRow(
                        label = stringResource(R.string.preference_lens),
                        value = getPreferenceDisplay(currentProfile?.preference)
                    )
                }
            } else if (isLoading) {
                InsightCard {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(16.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = if (isChinese) "加载中..." else "Loading...",
                            modifier = Modifier.padding(start = 8.dp),
                            color = InsightMuted
                        )
                    }
                }
            } else {
                InsightCard {
                    Text(
                        text = if (isChinese) "暂无个人信息，请先完成个人档案创建" else "No profile info, please complete your profile first",
                        style = MaterialTheme.typography.bodyMedium,
                        color = InsightMuted,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }

            // 修改密码卡片
            InsightCard {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showChangePasswordDialog = true }
                        .padding(vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (isChinese) "修改密码" else "Change Password",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = InsightText
                    )
                    Icon(
                        painter = painterResource(id = R.drawable.ic_arrow_forward),
                        contentDescription = null,
                        tint = InsightMuted
                    )
                }
            }

            // 语言设置卡片
            InsightCard {
                LanguageToggleRow(
                    currentLanguage = currentLanguage,
                    onSelectLanguage = ::switchLanguage,
                    enabled = !isLoading
                )

                if (isLoading) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
                        horizontalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(24.dp),
                            strokeWidth = 2.dp
                        )
                        Text(
                            text = stringResource(R.string.updating_language),
                            modifier = Modifier.padding(start = 8.dp),
                            color = InsightMuted
                        )
                    }
                }

                if (updateState is UiState.Error && !isLanguageUpdating) {
                    Text(
                        text = (updateState as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }

            // 其他设置卡片
            InsightCard {
                Text(
                    text = stringResource(R.string.other_settings),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                text = "${stringResource(R.string.version)} 2.0.0",
                    color = InsightMuted,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            InsightCard {
                Text(
                    text = if (isChinese) "隐私与信任" else "Privacy & Trust",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = if (isChinese)
                        "比赛原型已完成会话安全、用户归属校验和 AI 来源标记。以下控制项列入市场化版本。"
                    else
                        "The prototype includes session security, user ownership checks, and AI source labels. These controls are planned for the market-ready version.",
                    color = InsightMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                InfoRow(
                    label = if (isChinese) "数据导出" else "Data export",
                    value = if (isChinese) "路线图" else "Roadmap"
                )
                InfoRow(
                    label = if (isChinese) "删除账号" else "Delete account",
                    value = if (isChinese) "路线图" else "Roadmap"
                )
                InfoRow(
                    label = if (isChinese) "报告保留" else "Report retention",
                    value = if (isChinese) "可配置" else "Configurable"
                )
            }

            InsightCard {
                Text(
                    text = if (isChinese) "比赛演示" else "Competition Demo",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
                Text(
                    text = if (isChinese)
                        "载入预置香港学生档案、测评、八字、星盘提示和综合报告。"
                    else
                        "Load the seeded Hong Kong student profile, assessments, Bazi, astrology prompt, and integrated report.",
                    color = InsightMuted,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(bottom = 12.dp)
                )
                if (authState is UiState.Error) {
                    Text(
                        text = (authState as UiState.Error).message,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                SecondaryActionButton(
                    text = if (isDemoLoading) {
                        if (isChinese) "载入中..." else "Loading..."
                    } else {
                        if (isChinese) "载入 HKICT 演示账号" else "Use HKICT demo"
                    },
                    onClick = {
                        if (!isDemoLoading) {
                            authViewModel.seedDemo()
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 退出登录按钮
            PrimaryActionButton(
                text = stringResource(R.string.logout),
                onClick = { showLogoutDialog = true },
                enabled = !isLoading
            )
            Spacer(modifier = Modifier.height(96.dp))
        }
    }
}

private fun validateProfileEdit(
    nickname: String,
    birthDate: String,
    birthTime: String,
    birthPlace: String,
    isChinese: Boolean
): String? {
    val dateRegex = Regex("""\d{4}-\d{2}-\d{2}""")
    val timeRegex = Regex("""\d{2}:\d{2}:\d{2}""")
    return when {
        nickname.trim().isBlank() -> if (isChinese) "请填写昵称。" else "Nickname is required."
        !dateRegex.matches(birthDate.trim()) -> if (isChinese) "请选择出生日期。" else "Birth date must use yyyy-MM-dd."
        !timeRegex.matches(birthTime.trim()) -> if (isChinese) "请选择完整的出生时间。" else "Birth time must use HH:mm."
        birthPlace.split("|").size < 3 -> if (isChinese) "请选择完整的出生地点。" else "Please select province, city, and district."
        else -> null
    }
}
