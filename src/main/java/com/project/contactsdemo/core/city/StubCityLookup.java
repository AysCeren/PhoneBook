package com.project.contactsdemo.core.city;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Offline city lookup for local development and tests, so they don't depend on the external service.
 * <p>
 * Codes follow Turkey's license plate numbering (1 = Adana ... 81 = Düzce). The real service's coding
 * has not been checked against this table.
 */
@Component
@Profile({"local", "test"})
public class StubCityLookup implements CityLookup {

    private static final List<String> CITIES_BY_PLATE_CODE = List.of(
            "Adana", "Adıyaman", "Afyonkarahisar", "Ağrı", "Amasya", "Ankara", "Antalya", "Artvin", "Aydın", "Balıkesir",
            "Bilecik", "Bingöl", "Bitlis", "Bolu", "Burdur", "Bursa", "Çanakkale", "Çankırı", "Çorum", "Denizli",
            "Diyarbakır", "Edirne", "Elazığ", "Erzincan", "Erzurum", "Eskişehir", "Gaziantep", "Giresun", "Gümüşhane", "Hakkari",
            "Hatay", "Isparta", "Mersin", "İstanbul", "İzmir", "Kars", "Kastamonu", "Kayseri", "Kırklareli", "Kırşehir",
            "Kocaeli", "Konya", "Kütahya", "Malatya", "Manisa", "Kahramanmaraş", "Mardin", "Muğla", "Muş", "Nevşehir",
            "Niğde", "Ordu", "Rize", "Sakarya", "Samsun", "Siirt", "Sinop", "Sivas", "Tekirdağ", "Tokat",
            "Trabzon", "Tunceli", "Şanlıurfa", "Uşak", "Van", "Yozgat", "Zonguldak", "Aksaray", "Bayburt", "Karaman",
            "Kırıkkale", "Batman", "Şırnak", "Bartın", "Ardahan", "Iğdır", "Yalova", "Karabük", "Kilis", "Osmaniye",
            "Düzce");

    private static final Map<String, String> CITY_NAMES = IntStream.range(0, CITIES_BY_PLATE_CODE.size())
            .boxed()
            .collect(Collectors.toUnmodifiableMap(i -> String.valueOf(i + 1), CITIES_BY_PLATE_CODE::get));

    @Override
    public Optional<String> findCityName(String cityCode) {
        return Optional.ofNullable(cityCode)
                .map(code -> code.trim().replaceFirst("^0+(?=\\d)", "")) //"06" and "6" are the same city
                .map(CITY_NAMES::get);
    }
}
