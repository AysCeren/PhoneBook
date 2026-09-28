package com.project.contactsdemo.person.service;

import com.project.contactsdemo.core.dto.GenericDTO;
import com.project.contactsdemo.person.dto.PersonRequestDTO;
import com.project.contactsdemo.person.dto.PersonResponseDTO;
import com.project.contactsdemo.person.entity.Person;
import com.project.contactsdemo.person.mapper.PersonMapper;
import com.project.contactsdemo.person.repository.PersonRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonSaveService {
    private final PersonMapper personMapper;
    private final PersonRepository personRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GenericDTO<PersonResponseDTO> savePerson(PersonRequestDTO savePersonRequestDto) {
        Person person = personMapper.fromPersonRequestDTOToPersonEntity(savePersonRequestDto);
        this.personRepository.save(person);
        PersonResponseDTO personResponseDTO = personMapper.fromPersonToPersonResponseDto(person);
        GenericDTO<PersonResponseDTO> genericDTO = new GenericDTO<>(0,null);
        genericDTO.setBody(personResponseDTO);
        return genericDTO;
    }
}
