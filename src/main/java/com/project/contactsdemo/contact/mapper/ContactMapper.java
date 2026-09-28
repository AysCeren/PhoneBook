package com.project.contactsdemo.contact.mapper;

import com.project.contactsdemo.contact.entity.Contact;
import com.project.contactsdemo.contact.dto.ContactRequestDTO;
import com.project.contactsdemo.contact.dto.ContactResponseDTO;
import com.project.contactsdemo.core.mapperhelpermethods.PersonPersonIDMapperMethods;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import java.util.List;


@Mapper(uses = {PersonPersonIDMapperMethods.class})
public interface ContactMapper {
    @Mapping(source = "personId", target="person", qualifiedByName = "mapPersonIdToPerson")
    @Mapping(target = "phoneNo", source = "phoneNumber")
    Contact fromContactRequestDTOToContactEntity(ContactRequestDTO requestDto);
    @Mapping(source = "personId", target="person", qualifiedByName = "mapPersonIdToPerson")
    @Mapping(target = "phoneNumber", source = "phoneNo")
    List<ContactResponseDTO> fromContactEntityToContactResponseDTO(List<Contact> contact);
    @Mapping(source = "person", target="personId", qualifiedByName = "mapPersonToPersonId")
    @Mapping(target = "phoneNumber", source = "phoneNo")
    ContactResponseDTO fromContactEntityToContactResponseDTO(Contact contact);
}
