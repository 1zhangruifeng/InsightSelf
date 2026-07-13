package com.insightself.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.insightself.common.ApiException;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
@Component
public class BirthPlaceResolver {
    private final JsonNode root;

    public BirthPlaceResolver(ObjectMapper objectMapper) {
        try (InputStream inputStream = new ClassPathResource("regions/cn_regions.json").getInputStream()) {
            this.root = objectMapper.readTree(inputStream);
        } catch (IOException ex) {
            throw new IllegalStateException("failed to load regions/cn_regions.json", ex);
        }
    }

    public ResolvedBirthPlace resolve(String birthPlace) {
        List<String> parts = splitParts(birthPlace);
        if (parts.size() < 3) {
            throw new ApiException(HttpStatus.BAD_REQUEST,
                    "birthPlace must include province, city, and district separated by |");
        }
        String provinceName = parts.get(0);
        String cityName = parts.get(1);
        String districtName = parts.get(2);

        JsonNode province = findChildByName(root.path("provinces"), provinceName);
        if (province == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "unknown province in birthPlace: " + provinceName);
        }
        JsonNode city = findChildByName(province.path("cities"), cityName);
        if (city == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "unknown city in birthPlace: " + cityName);
        }
        JsonNode district = findChildByName(city.path("districts"), districtName);
        if (district == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "unknown district in birthPlace: " + districtName);
        }

        double latitude = district.path("latitude").asDouble(city.path("latitude").asDouble());
        double longitude = district.path("longitude").asDouble(city.path("longitude").asDouble());
        String timezone = province.path("timezone").asText("Asia/Shanghai");
        return new ResolvedBirthPlace(
                String.join("|", provinceName, cityName, districtName),
                timezone,
                latitude,
                longitude
        );
    }

    private List<String> splitParts(String birthPlace) {
        if (birthPlace == null || birthPlace.isBlank()) {
            return List.of();
        }
        String normalized = birthPlace.trim().replace(" / ", "|").replace("/", "|").replace("｜", "|");
        List<String> parts = new ArrayList<>();
        for (String part : normalized.split("\\|")) {
            String trimmed = part.trim();
            if (!trimmed.isBlank()) {
                parts.add(trimmed);
            }
        }
        return parts;
    }

    private JsonNode findChildByName(JsonNode array, String name) {
        if (!array.isArray()) {
            return null;
        }
        String target = normalize(name);
        for (JsonNode child : array) {
            if (normalize(child.path("name").asText("")).equals(target)) {
                return child;
            }
        }
        return null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    public record ResolvedBirthPlace(
            String birthPlace,
            String birthTimezone,
            double latitude,
            double longitude
    ) {
    }
}
