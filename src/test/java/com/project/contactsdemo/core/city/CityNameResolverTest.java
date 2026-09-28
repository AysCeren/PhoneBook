package com.project.contactsdemo.core.city;

import com.project.contactsdemo.core.city.CityNameResolver.CityNames;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CityNameResolverTest {

    @Test
    void stubResolvesPlateCodesWithOrWithoutLeadingZero() {
        StubCityLookup stub = new StubCityLookup();
        assertThat(stub.findCityName("6")).contains("Ankara");
        assertThat(stub.findCityName("06")).contains("Ankara");
        assertThat(stub.findCityName("81")).contains("Düzce");
        assertThat(stub.findCityName("99")).isEmpty();
    }

    @Test
    void looksUpEachDistinctCodeOnce() {
        List<String> lookedUp = new ArrayList<>();
        CityLookup recording = code -> {
            lookedUp.add(code);
            return Optional.of("City " + code);
        };
        CityNames names = new CityNameResolver(recording).resolve(Stream.of("6", "6", "34", "6"));

        assertThat(lookedUp).containsExactlyInAnyOrder("6", "34");
        assertThat(names.nameFor("34")).isEqualTo("City 34");
        assertThat(names.complete()).isTrue();
    }

    @Test
    void fallsBackToTheCodeAndReportsIncompleteWhenANameIsMissing() {
        CityLookup unavailable = code -> Optional.empty(); //e.g. the city service is down
        CityNames names = new CityNameResolver(unavailable).resolve(Stream.of("6"));

        assertThat(names.nameFor("6")).isEqualTo("6");
        assertThat(names.complete()).isFalse();
    }
}
