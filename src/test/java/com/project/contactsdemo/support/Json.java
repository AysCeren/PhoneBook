package com.project.contactsdemo.support;

import com.jayway.jsonpath.DocumentContext;
import com.jayway.jsonpath.JsonPath;
import org.springframework.http.ResponseEntity;

/** Reads values from a JSON response body, e.g. {@code Json.of(response).read("$.body.id")}. */
public final class Json {

    private Json() {
    }

    public static DocumentContext of(ResponseEntity<String> response) {
        return JsonPath.parse(response.getBody());
    }
}
