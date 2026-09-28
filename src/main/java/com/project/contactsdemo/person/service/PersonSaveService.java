package com.project.contactsdemo.person.service;

import com.project.contactsdemo.core.cache.CacheNames;
import com.project.contactsdemo.core.cache.CacheService;
import com.project.contactsdemo.core.city.CityNameResolver;
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

import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class PersonSaveService {
    private final PersonMapper personMapper;
    private final PersonRepository personRepository;
    private final CacheService cacheService;
    private final CityNameResolver cityNameResolver;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GenericDTO<PersonResponseDTO> savePerson(PersonRequestDTO savePersonRequestDto) {
        Person person = personMapper.fromPersonRequestDTOToPersonEntity(savePersonRequestDto);
        this.personRepository.save(person);
        cacheService.clearAfterCommit(CacheNames.PERSON_RESPONSE_ALL); //the cached "all persons" list is now outdated
        PersonResponseDTO personResponseDTO = personMapper.fromPersonToPersonResponseDto(person);
        personResponseDTO.setBirthCity(cityNameResolver.resolve(Stream.of(person.getBirthCity())).nameFor(person.getBirthCity()));
        GenericDTO<PersonResponseDTO> genericDTO = new GenericDTO<>(0,null);
        genericDTO.setBody(personResponseDTO);
        return genericDTO;
    }
}
