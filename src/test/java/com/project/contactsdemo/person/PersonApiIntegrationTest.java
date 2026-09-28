package com.project.contactsdemo.person;

import com.project.contactsdemo.support.IntegrationTest;
import com.project.contactsdemo.support.Json;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static com.project.contactsdemo.support.TestData.contact;
import static com.project.contactsdemo.support.TestData.person;
import static org.assertj.core.api.Assertions.assertThat;

/** Documents the current person endpoints (baseline for the upgrade and the API redesign). */
class PersonApiIntegrationTest extends IntegrationTest {

    @Test
    void savePersonReturns201WithTheCityName() {
        ResponseEntity<String> response = post("/api/savePerson", person("Ayse"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Json.of(response).<Integer>read("$.errorStatus")).isZero();
        assertThat(Json.of(response).<Object>read("$.body.id")).isNotNull();
        assertThat(Json.of(response).<String>read("$.body.birthCity")).isEqualTo("Ankara");
        assertThat(Json.of(response).<String>read("$.body.birthDate")).isEqualTo("15-03-1995");
    }

    @Test
    void savePersonRejectsAnInvalidEmailWithTheFieldName() {
        Map<String, Object> person = person("Ayse");
        person.put("email", "not-an-email");

        ResponseEntity<String> response = post("/api/savePerson", person);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Json.of(response).<String>read("$.errorMessage")).startsWith("email: ");
    }

    @Test
    void savePersonRejectsAMissingBirthDate() {
        Map<String, Object> person = person("Ayse");
        person.remove("birthDate");

        ResponseEntity<String> response = post("/api/savePerson", person);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Json.of(response).<String>read("$.errorMessage")).isEqualTo("Birthdate cannot be empty");
    }

    @Test
    void getAllPersonReturns404WhenThereAreNone() {
        assertThat(get("/api/getAllPerson").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllPersonShowsNewlySavedPersonsDespiteTheCache() {
        post("/api/savePerson", person("Ayse"));
        assertThat(firstNames(get("/api/getAllPerson"))).containsExactly("Ayse"); //now cached

        post("/api/savePerson", person("Deniz")); //must evict the cached list

        assertThat(firstNames(get("/api/getAllPerson"))).containsExactlyInAnyOrder("Ayse", "Deniz");
    }

    @Test
    void getAllContactsOfPersonReturnsOnlyActiveContacts() {
        long personId = savePerson("Ayse");
        long keptId = saveContact(personId, "Mehmet Kaya", "+905321112233");
        long deletedId = saveContact(personId, "Ali Demir", "+905321112244");
        put("/api/deleteContact/" + deletedId, null);

        ResponseEntity<String> response = get("/api/getAllContactsOfPerson/" + personId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Json.of(response).<List<Integer>>read("$.body[*].id")).containsExactly((int) keptId);
    }

    @Test
    void getAllContactsOfPersonReturns404ForAnUnknownPersonAnd400ForANonNumericId() {
        assertThat(get("/api/getAllContactsOfPerson/999999").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(get("/api/getAllContactsOfPerson/abc").getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getAllPersonWithContactsIncludesContactsAndASummary() {
        long personId = savePerson("Ayse");
        saveContact(personId, "Mehmet Kaya", "+905321112233");

        ResponseEntity<String> response = get("/api/getAllPersonWithContacts");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(Json.of(response).<String>read("$.body[0].birthCity")).isEqualTo("Ankara");
        assertThat(Json.of(response).<String>read("$.body[0].message")).isEqualTo("1 contacts for Ayse Coban");
    }

    @Test
    void cityLookupEndpointResolvesKnownCodesAndReturns404ForUnknownOnes() {
        assertThat(Json.of(get("/api/restTemplateControl/6")).<String>read("$.body")).isEqualTo("Ankara");
        assertThat(get("/api/restTemplateControl/99").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    private long savePerson(String firstName) {
        return Json.of(post("/api/savePerson", person(firstName))).<Number>read("$.body.id").longValue();
    }

    private long saveContact(long personId, String name, String phoneNumber) {
        return Json.of(post("/api/saveContact", contact(personId, name, phoneNumber))).<Number>read("$.body.id").longValue();
    }

    private static List<String> firstNames(ResponseEntity<String> response) {
        return Json.of(response).read("$.body[*].firstName");
    }
}
