package com.project.contactsdemo.core.city;

import java.util.Optional;

/**
 * Resolves a city code (e.g. "6") to its name (e.g. "Ankara").
 * <p>
 * Implementations: {@link HttpCityLookup} calls the external city service (default);
 * {@link StubCityLookup} answers from a fixed table under the {@code local} and {@code test} profiles.
 */
public interface CityLookup {

    /**
     * @return the city name, or empty when the code is unknown or the name can't be resolved right now
     *         (e.g. the external service is down). Callers decide how to degrade, typically by showing the code.
     */
    Optional<String> findCityName(String cityCode);
}
