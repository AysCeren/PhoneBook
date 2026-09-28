package com.project.contactsdemo.core.city;

import com.project.contactsdemo.core.dto.CityResponseDTO;
import com.project.contactsdemo.core.properties.CityServiceProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Optional;

/** Looks cities up in the external city service. Active in every profile except {@code local} and {@code test}. */
@Slf4j
@Component
@Profile("!local & !test")
public class HttpCityLookup implements CityLookup {

    private final RestTemplate restTemplate;
    private final UriComponents uriComponents;

    public HttpCityLookup(RestTemplate restTemplate, CityServiceProperties cityServiceProperties) {
        this.restTemplate = restTemplate;
        //URI Builder: the base URL comes from app.city-service.base-url
        this.uriComponents = UriComponentsBuilder
                .fromUriString(cityServiceProperties.baseUrl())
                .queryParam("ilKodu", "{ilKodu}")
                .encode()
                .build();
    }

    @Override
    @CircuitBreaker(name = "cityService", fallbackMethod = "cityNameUnavailable")
    public Optional<String> findCityName(String cityCode) {
        CityResponseDTO cityResponseDTO = restTemplate.getForObject(uriComponents.expand(cityCode).toUri(), CityResponseDTO.class); //neye döneceğini burada class formatında belirtiriz.
        return Optional.ofNullable(cityResponseDTO).map(CityResponseDTO::getIlAdi);
    }

    /**
     * Circuit-breaker fallback: called instead of throwing when the call failed (timeout, error response,
     * unreachable host) or the breaker is open. Same parameters as the protected method, plus the cause.
     */
    private Optional<String> cityNameUnavailable(String cityCode, Throwable cause) {
        log.warn("City name for code {} unavailable: {}", cityCode, cause.toString());
        return Optional.empty();
    }
}
