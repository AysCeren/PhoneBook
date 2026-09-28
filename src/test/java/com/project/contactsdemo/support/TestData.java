package com.project.contactsdemo.support;

import java.util.HashMap;
import java.util.Map;

/** Valid request bodies, as a client would send them. Tests copy and change single fields. */
public final class TestData {

    private TestData() {
    }

    public static Map<String, Object> person(String firstName) {
        Map<String, Object> person = new HashMap<>();
        person.put("firstName", firstName);
        person.put("lastName", "Coban");
        person.put("birthDate", "15-03-1995");
        person.put("birthCity", "6"); //Ankara in StubCityLookup
        person.put("gender", "FEMALE");
        person.put("phoneNumber", "+905522568471");
        person.put("email", "ayse@example.com");
        return person;
    }

    public static Map<String, Object> contact(long personId, String name, String phoneNumber) {
        Map<String, Object> contact = new HashMap<>();
        contact.put("personId", personId);
        contact.put("name", name);
        contact.put("phoneNumber", phoneNumber);
        return contact;
    }
}
