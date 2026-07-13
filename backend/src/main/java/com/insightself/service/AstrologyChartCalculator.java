package com.insightself.service;

import com.insightself.common.ApiException;
import com.insightself.domain.UserProfile;
import org.springframework.core.io.ClassPathResource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import swisseph.SweConst;
import swisseph.SweDate;
import swisseph.SwissEph;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AstrologyChartCalculator {
    static final List<String> SIGN_NAMES = List.of(
            "Aries", "Taurus", "Gemini", "Cancer", "Leo", "Virgo",
            "Libra", "Scorpio", "Sagittarius", "Capricorn", "Aquarius", "Pisces"
    );
    private static final List<PlanetSpec> PLANETS = List.of(
            new PlanetSpec("Sun", SweConst.SE_SUN),
            new PlanetSpec("Moon", SweConst.SE_MOON),
            new PlanetSpec("Mercury", SweConst.SE_MERCURY),
            new PlanetSpec("Venus", SweConst.SE_VENUS),
            new PlanetSpec("Mars", SweConst.SE_MARS),
            new PlanetSpec("Jupiter", SweConst.SE_JUPITER),
            new PlanetSpec("Saturn", SweConst.SE_SATURN),
            new PlanetSpec("Uranus", SweConst.SE_URANUS),
            new PlanetSpec("Neptune", SweConst.SE_NEPTUNE),
            new PlanetSpec("Pluto", SweConst.SE_PLUTO),
            new PlanetSpec("North Node", SweConst.SE_MEAN_NODE)
    );
    private static final List<String> EPHEMERIS_FILES = List.of("sepl_18.se1", "semo_18.se1");
    private static final int CALC_FLAGS = SweConst.SEFLG_SWIEPH | SweConst.SEFLG_SPEED;
    private static final int HOUSE_SYSTEM_PLACIDUS = 'P';

    private final SwissEph swissEph;

    public AstrologyChartCalculator() {
        this.swissEph = new SwissEph(prepareEphemerisPath().toString());
    }

    public synchronized NatalChart natal(UserProfile profile) {
        requirePreciseBirthData(profile);
        ZonedDateTime localBirth = ZonedDateTime.of(profile.getBirthDate(), profile.getBirthTime(), ZoneId.of(profile.getBirthTimezone()));
        Instant birthInstant = localBirth.toInstant();
        double julianDayUt = julianDayUt(birthInstant);
        Houses houses = houses(julianDayUt, profile.getLatitude(), profile.getLongitude());
        Map<String, PlanetPosition> planets = planetPositions(julianDayUt, houses);
        PlanetPosition sun = planets.get("Sun");
        return new NatalChart(
                "Swiss Ephemeris Java port " + swissEph.swe_java_version(),
                "SWIEPH bundled sepl_18/semo_18",
                birthInstant.toString(),
                julianDayUt,
                sun.sign(),
                signName(houses.ascendant()),
                round(houses.ascendant()),
                signName(houses.mediumCoeli()),
                round(houses.mediumCoeli()),
                houses.cusps(),
                planets
        );
    }

    public synchronized TransitChart transitAt(Instant instant) {
        double julianDayUt = julianDayUt(instant);
        return new TransitChart(
                "Swiss Ephemeris Java port " + swissEph.swe_java_version(),
                "SWIEPH bundled sepl_18/semo_18",
                instant.toString(),
                julianDayUt,
                planetPositions(julianDayUt, null)
        );
    }

    private Path prepareEphemerisPath() {
        // The Java port splits ephemeris paths on ':' and ';', so absolute
        // Windows paths like C:\... are parsed incorrectly. Use a relative
        // runtime directory and copy the bundled files there on startup.
        Path directory = Path.of("ephe");
        try {
            Files.createDirectories(directory);
            for (String fileName : EPHEMERIS_FILES) {
                ClassPathResource resource = new ClassPathResource("ephe/" + fileName);
                Path target = directory.resolve(fileName);
                // Windows keeps these files locked while Swiss Ephemeris is active.
                // If the bundled file is already present and complete, reuse it
                // instead of replacing it under a running local backend or test.
                if (Files.exists(target) && Files.size(target) == resource.contentLength()) {
                    continue;
                }
                try (InputStream inputStream = resource.getInputStream()) {
                    Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
            return directory;
        } catch (IOException ex) {
            throw new IllegalStateException("failed to prepare bundled Swiss Ephemeris data files", ex);
        }
    }

    public synchronized PlanetPosition dateOnlySun(LocalDate date) {
        Instant instant = date.atTime(LocalTime.NOON).atZone(ZoneId.of("UTC")).toInstant();
        double julianDayUt = julianDayUt(instant);
        return planetPosition("Sun", SweConst.SE_SUN, julianDayUt, null);
    }

    private void requirePreciseBirthData(UserProfile profile) {
        if (profile.getBirthDate() == null || profile.getBirthTime() == null || blank(profile.getBirthTimezone())
                || profile.getLatitude() == null || profile.getLongitude() == null) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "birthDate, birthTime, birthTimezone, latitude, and longitude are required for astrology calculation");
        }
    }

    private Map<String, PlanetPosition> planetPositions(double julianDayUt, Houses houses) {
        Map<String, PlanetPosition> positions = new LinkedHashMap<>();
        PLANETS.forEach(planet -> positions.put(planet.name(), planetPosition(planet.name(), planet.planetId(), julianDayUt, houses)));
        return positions;
    }

    private PlanetPosition planetPosition(String name, int planetId, double julianDayUt, Houses houses) {
        double[] values = new double[6];
        StringBuilder error = new StringBuilder();
        int result = swissEph.swe_calc_ut(julianDayUt, planetId, CALC_FLAGS, values, error);
        if (result == SweConst.ERR || Double.isNaN(values[0])) {
            throw new IllegalStateException("Swiss Ephemeris calculation failed for " + name + ": " + error);
        }
        double longitude = normalize(values[0]);
        return new PlanetPosition(
                name,
                round(longitude),
                signName(longitude),
                round(longitude % 30.0),
                values[3] < 0,
                houses == null ? null : houseFor(longitude, houses.cusps())
        );
    }

    private Houses houses(double julianDayUt, double latitude, double longitude) {
        double[] cusps = new double[13];
        double[] ascmc = new double[10];
        StringBuilder error = new StringBuilder();
        int result = swissEph.swe_houses(julianDayUt, 0, latitude, longitude, HOUSE_SYSTEM_PLACIDUS, cusps, ascmc);
        if (result == SweConst.ERR) {
            throw new IllegalStateException("Swiss Ephemeris house calculation failed: " + error);
        }
        List<Double> normalizedCusps = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            normalizedCusps.add(round(normalize(cusps[i])));
        }
        return new Houses(normalizedCusps, normalize(ascmc[SweConst.SE_ASC]), normalize(ascmc[SweConst.SE_MC]));
    }

    private int houseFor(double longitude, List<Double> cusps) {
        for (int i = 0; i < cusps.size(); i++) {
            double start = cusps.get(i);
            double end = cusps.get((i + 1) % cusps.size());
            if (containsCircular(longitude, start, end)) {
                return i + 1;
            }
        }
        throw new IllegalStateException("planet longitude did not fall into a house: " + longitude);
    }

    private boolean containsCircular(double longitude, double start, double end) {
        if (start <= end) {
            return longitude >= start && longitude < end;
        }
        return longitude >= start || longitude < end;
    }

    private double julianDayUt(Instant instant) {
        ZonedDateTime utc = instant.atZone(ZoneId.of("UTC"));
        double hour = utc.getHour() + utc.getMinute() / 60.0 + utc.getSecond() / 3600.0 + utc.getNano() / 3_600_000_000_000.0;
        return SweDate.getJulDay(utc.getYear(), utc.getMonthValue(), utc.getDayOfMonth(), hour, SweDate.SE_GREG_CAL);
    }

    static String signName(double longitude) {
        int index = (int) Math.floor(normalize(longitude) / 30.0);
        return SIGN_NAMES.get(index);
    }

    static double angularDistance(double a, double b) {
        double diff = Math.abs(normalize(a) - normalize(b));
        return diff > 180 ? 360 - diff : diff;
    }

    static double normalize(double longitude) {
        double value = longitude % 360.0;
        return value < 0 ? value + 360.0 : value;
    }

    static double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }

    private boolean blank(String value) {
        return value == null || value.trim().isBlank();
    }

    public record NatalChart(
            String engine,
            String ephemerisMode,
            String birthInstantUtc,
            double julianDayUt,
            String sunSign,
            String ascendantSign,
            double ascendantLongitude,
            String mediumCoeliSign,
            double mediumCoeliLongitude,
            List<Double> houseCusps,
            Map<String, PlanetPosition> planets
    ) {
    }

    public record TransitChart(
            String engine,
            String ephemerisMode,
            String instantUtc,
            double julianDayUt,
            Map<String, PlanetPosition> planets
    ) {
    }

    public record PlanetPosition(
            String name,
            double longitude,
            String sign,
            double degreeInSign,
            boolean retrograde,
            Integer house
    ) {
    }

    private record Houses(List<Double> cusps, double ascendant, double mediumCoeli) {
    }

    private record PlanetSpec(String name, int planetId) {
    }
}
