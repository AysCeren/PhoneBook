package com.project.contactsdemo.contact;

import com.project.contactsdemo.support.IntegrationTest;
import com.project.contactsdemo.support.Json;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static com.project.contactsdemo.support.TestData.contact;
import static com.project.contactsdemo.support.TestData.person;
import static org.assertj.core.api.Assertions.assertThat;

/** Documents the current contact endpoints (baseline for the upgrade and the API redesign). */
class ContactApiIntegrationTest extends IntegrationTest {

    private long personId;

    @BeforeEach
    void savePerson() {
        personId = Json.of(post("/api/savePerson", person("Ayse"))).<Number>read("$.body.id").longValue();
    }

    @Test
    void saveContactReturns201() {
        ResponseEntity<String> response = post("/api/saveContact", contact(personId, "Mehmet Kaya", "+905321112233"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(Json.of(response).<String>read("$.body.name")).isEqualTo("Mehmet Kaya");
        assertThat(Json.of(response).<Number>read("$.body.personId").longValue()).isEqualTo(personId);
    }

    @Test
    void saveContactRejectsAnInvalidPhoneNumber() {
        ResponseEntity<String> response = post("/api/saveContact", contact(personId, "Mehmet Kaya", "12"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(Json.of(response).<String>read("$.errorMessage")).isEqualTo("phoneNumber: Invalid phone number.");
    }

    @Test
    void getAllContactReturns404WhenThereAreNone() {
        assertThat(get("/api/getAllContact").getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllContactShowsNewlySavedContactsDespiteTheCache() {
        saveContact("Mehmet Kaya", "+905321112233");
        assertThat(names(get("/api/getAllContact"))).containsExactly("Mehmet Kaya"); //now cached

        saveContact("Ali Demir", "+905321112244"); //must evict the cached list

        assertThat(names(get("/api/getAllContact"))).containsExactlyInAnyOrder("Mehmet Kaya", "Ali Demir");
    }

    @Test
    void updateContactChangesItsName() {
        long contactId = saveContact("Mehmet Kaya", "+905321112233");

        ResponseEntity<String> response = put("/api/updateContact/" + contactId, contact(personId, "Mehmet Yilmaz", "+905321112233"));

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(names(get("/api/getAllContact"))).containsExactly("Mehmet Yilmaz");
    }

    @Test
    void updateContactReturns404ForAnUnknownId() {
        assertThat(put("/api/updateContact/999999", contact(personId, "Mehmet Kaya", "+905321112233")).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void deleteContactIsASoftDeleteAndCannotBeRepeated() {
        long contactId = saveContact("Mehmet Kaya", "+905321112233");

        assertThat(put("/api/deleteContact/" + contactId, null).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(put("/api/deleteContact/" + contactId, null).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void getAllContactStillListsSoftDeletedContacts() {
        // Current behavior, documented so a change is deliberate: getAllContact has no status filter.
        long contactId = saveContact("Mehmet Kaya", "+905321112233");
        put("/api/deleteContact/" + contactId, null);

        assertThat(names(get("/api/getAllContact"))).containsExactly("Mehmet Kaya");
    }

    private long saveContact(String name, String phoneNumber) {
        Map<String, Object> body = contact(personId, name, phoneNumber);
        return Json.of(post("/api/saveContact", body)).<Number>read("$.body.id").longValue();
    }

    private static List<String> names(ResponseEntity<String> response) {
        return Json.of(response).read("$.body[*].name");
    }
}
