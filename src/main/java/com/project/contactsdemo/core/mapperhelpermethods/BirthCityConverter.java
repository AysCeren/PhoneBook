package com.project.contactsdemo.core.mapperhelpermethods;

import com.project.contactsdemo.core.dto.CityResponseDTO;
import com.project.contactsdemo.core.properties.CityServiceProperties;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.mapstruct.Named;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Objects;
@Configuration
public class BirthCityConverter {

    private final RestTemplate restTemplate;
    private final UriComponents uriComponents;

    public BirthCityConverter(RestTemplate restTemplate, CityServiceProperties cityServiceProperties) {
        this.restTemplate = restTemplate;
        //URI Builder: the base URL comes from app.city-service.base-url
        this.uriComponents = UriComponentsBuilder
                .fromUriString(cityServiceProperties.baseUrl())
                .queryParam("ilKodu", "{ilKodu}")
                .encode()
                .build();
    }
    @Named("birthCityName")
    @CircuitBreaker(name = "cityService") //TODO: fallBackMethod'u nereye nasıl tanımlayacağını sor
    public  String birthCityName(String birthCity) {
        URI uri = uriComponents.expand( birthCity).toUri();
        CityResponseDTO cityResponseDTO = restTemplate.getForObject(uri, CityResponseDTO.class); //neye döneceğini burada class formatında belirtiriz.
        return Objects.requireNonNull(cityResponseDTO).getIlAdi();
    }
}
