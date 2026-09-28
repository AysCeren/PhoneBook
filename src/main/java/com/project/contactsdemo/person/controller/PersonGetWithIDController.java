package com.project.contactsdemo.person.controller;


import com.project.contactsdemo.core.dto.GenericDTO;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import com.project.contactsdemo.person.service.PersonGetWithIDService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping({"/api"})
@RequiredArgsConstructor
public class PersonGetWithIDController {
    private final PersonGetWithIDService personGetWithIDService;

    @GetMapping(path = "/restTemplateControl/{ilKodu}")
    public ResponseEntity<GenericDTO<String>> getPersonWithId(@Valid @PathVariable("ilKodu") @Parameter(name = "ilKodu", example = "6") String ilKodu) {
        GenericDTO<String> gDTO = personGetWithIDService.getPersonWithId(ilKodu);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(gDTO);
    }
}
