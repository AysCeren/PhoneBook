package com.project.contactsdemo.person.mapper;

import com.project.contactsdemo.person.dto.PersonWithContactsDTO;
import com.project.contactsdemo.person.entity.Person;
import com.project.contactsdemo.person.dto.PersonRequestDTO;
import com.project.contactsdemo.person.dto.PersonResponseDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// birthCity is copied as the city code; the services replace it with the city name (see CityNameResolver).
// The lookup is an HTTP call, so it stays out of the mapper: mapping remains fast, pure and easy to test.
@Mapper
public interface PersonMapper {
    //This method takes requestDTO and maps them to Person Entity
    //Note: We do not need to use Mapping, because names are the same.
    @Mapping(target = "birthDate", source = "birthDate")
    List<Person> fromPersonRequestDTOToPersonEntity(List<PersonRequestDTO> requestDto);
    @Mapping (target = "birthDate", source = "birthDate",  qualifiedByName = "stringToLocalDate")
    Person fromPersonRequestDTOToPersonEntity(PersonRequestDTO requestDto);
    //This method takes Person Entity and maps them to Person ResponseDTO
    @Mapping (target = "birthDate", source = "birthDate")
    List<PersonResponseDTO> fromPersonToPersonResponseDto(List<Person> person);
    @Mapping (target = "birthDate", source = "birthDate", qualifiedByName = "LocalDateToString")
    PersonResponseDTO fromPersonToPersonResponseDto(Person person);
    PersonWithContactsDTO fromPersonToPersonResponseForContactDTO(Person person);

    @Named("stringToLocalDate")
    default LocalDate stringToLocalDate(String birthDate) { //it will automatically be used by "fromPersonRequestDTOToPersonEntity"
        return birthDate != null ? LocalDate.parse(birthDate, DateTimeFormatter.ofPattern("dd-MM-yyyy")) : null;
    }
    @Named("LocalDateToString")
    default String LocalDateToString(LocalDate birthDate) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        String text = birthDate.format(formatter);
        return text;
    }
}
