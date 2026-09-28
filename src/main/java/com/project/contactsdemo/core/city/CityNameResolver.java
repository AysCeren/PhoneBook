package com.project.contactsdemo.core.city;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Resolves the city names for a batch of records in the service layer.
 * <p>
 * Each distinct code is looked up once per call (a list of 100 persons born in Ankara makes one lookup,
 * not 100). Codes that can't be resolved keep the code as their name, and the result reports that it is
 * incomplete so callers can avoid caching it.
 */
@Component
@RequiredArgsConstructor
public class CityNameResolver {

    private final CityLookup cityLookup;

    public CityNames resolve(Stream<String> cityCodes) {
        Map<String, Optional<String>> lookups = new HashMap<>();
        cityCodes.filter(Objects::nonNull).distinct()
                .forEach(code -> lookups.put(code, cityLookup.findCityName(code)));

        Map<String, String> namesByCode = new HashMap<>();
        lookups.forEach((code, name) -> namesByCode.put(code, name.orElse(code)));
        boolean complete = lookups.values().stream().allMatch(Optional::isPresent);
        return new CityNames(namesByCode, complete);
    }

    /**
     * @param complete false if at least one name fell back to its code (unknown code or service unavailable)
     */
    public record CityNames(Map<String, String> namesByCode, boolean complete) {
        public String nameFor(String cityCode) {
            return cityCode == null ? null : namesByCode.getOrDefault(cityCode, cityCode);
        }
    }
}
