package com.insightself.service;

import com.insightself.common.ApiException;
import com.insightself.domain.UserProfile;
import com.insightself.dto.ProfileRequest;
import com.insightself.repository.UserProfileRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Set;

@Service
public class ProfileService {
    private static final Set<String> CALENDAR_TYPES = Set.of("SOLAR", "LUNAR");
    private static final Set<String> PREFERENCES = Set.of("EASTERN", "WESTERN", "BALANCED");
    private static final Set<String> LANGUAGES = Set.of("en", "zh");

    private final UserProfileRepository profileRepository;
    private final UserService userService;
    private final BirthPlaceResolver birthPlaceResolver;

    public ProfileService(UserProfileRepository profileRepository,
                          UserService userService,
                          BirthPlaceResolver birthPlaceResolver) {
        this.profileRepository = profileRepository;
        this.userService = userService;
        this.birthPlaceResolver = birthPlaceResolver;
    }

    @Transactional
    public UserProfile createOrReplace(Long userId, ProfileRequest request) {
        userService.requireUser(userId);
        validate(request);
        UserProfile profile = profileRepository.findByUserId(userId).orElseGet(UserProfile::new);
        if (profile.getId() == null) {
            profile.setCreatedAt(LocalDateTime.now());
        }
        apply(profile, userId, request);
        return profileRepository.save(profile);
    }

    public UserProfile get(Long userId) {
        userService.requireUser(userId);
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "profile not found"));
    }

    public Optional<UserProfile> findOptional(Long userId) {
        userService.requireUser(userId);
        return profileRepository.findByUserId(userId);
    }

    @Transactional
    public UserProfile update(Long userId, ProfileRequest request) {
        userService.requireUser(userId);
        validate(request);
        UserProfile profile = get(userId);
        apply(profile, userId, request);
        return profileRepository.save(profile);
    }

    private void apply(UserProfile profile, Long userId, ProfileRequest request) {
        BirthPlaceResolver.ResolvedBirthPlace resolved = birthPlaceResolver.resolve(request.birthPlace());
        profile.setUserId(userId);
        profile.setNickname(request.nickname().trim());
        profile.setGender(emptyToNull(request.gender()));
        profile.setBirthDate(request.birthDate());
        profile.setBirthTime(request.birthTime());
        profile.setBirthPlace(resolved.birthPlace());
        profile.setBirthTimezone(resolved.birthTimezone());
        profile.setLatitude(resolved.latitude());
        profile.setLongitude(resolved.longitude());
        profile.setCalendarType(defaulted(request.calendarType(), "SOLAR").toUpperCase());
        profile.setPreference(defaulted(request.preference(), "BALANCED").toUpperCase());
        profile.setAiEnabled(request.aiEnabled());
        // 新增：保存语言偏好
        String language = defaulted(request.language(), "en").toLowerCase();
        if (!LANGUAGES.contains(language)) {
            language = "en";
        }
        profile.setLanguage(language);
        profile.setUpdatedAt(LocalDateTime.now());
    }

    private void validate(ProfileRequest request) {
        if (request == null || blank(request.nickname()) || request.birthDate() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "nickname and birthDate are required");
        }
        String calendarType = defaulted(request.calendarType(), "SOLAR").toUpperCase();
        String preference = defaulted(request.preference(), "BALANCED").toUpperCase();
        if (!CALENDAR_TYPES.contains(calendarType)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "calendarType must be SOLAR or LUNAR");
        }
        if (!PREFERENCES.contains(preference)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "preference must be EASTERN, WESTERN, or BALANCED");
        }
        if (blank(request.birthPlace())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "birthPlace is required");
        }
        if (request.birthTime() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "birthTime is required");
        }
        birthPlaceResolver.resolve(request.birthPlace());
    }

    private boolean blank(String value) {
        return value == null || value.trim().isBlank();
    }

    private String defaulted(String value, String fallback) {
        return blank(value) ? fallback : value.trim();
    }

    private String emptyToNull(String value) {
        return blank(value) ? null : value.trim();
    }
}
