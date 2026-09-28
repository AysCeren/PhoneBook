package com.project.contactsdemo.contact.controller;

import com.project.contactsdemo.contact.dto.ContactResponseDTO;
import com.project.contactsdemo.contact.service.GetAllContactService;
import com.project.contactsdemo.core.dto.GenericDTO;
import com.project.contactsdemo.core.ratelimitedannotation.RateLimited;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Validated
@RequestMapping({"/api"})
@RequiredArgsConstructor
public class GetAllContactController {

    private final GetAllContactService getAllContactService;

    @Operation( summary= "Get all contact", description = "Get the all contacts from the contact table")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully get all contact"),
            @ApiResponse(responseCode = "404", description = "Not found - No such contact to update with the given id"),
            @ApiResponse(responseCode = "500", description = "Internal Server Error")
    })
    @RequestMapping(method = {RequestMethod.GET}, path = {"/getAllContact"})
    @RateLimited(service = "GET_ALL_CONTACT")
    public ResponseEntity<GenericDTO<List<ContactResponseDTO>>> getAllContact() {
        GenericDTO<List<ContactResponseDTO>> genericDTO = getAllContactService.getAllContactDTO();
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(genericDTO);
    }
}
