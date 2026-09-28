package com.project.contactsdemo.person.service;

import com.project.contactsdemo.core.dto.GenericDTO;
import com.project.contactsdemo.person.dto.PersonResponseDTO;
import com.project.contactsdemo.person.entity.Person;
import com.project.contactsdemo.core.exception.NoDataFoundException;
import com.project.contactsdemo.person.mapper.PersonMapper;
import com.project.contactsdemo.person.repository.PersonRepository;
import com.project.contactsdemo.core.cache.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PersonGetAllService {

    private final PersonMapper personMapper;
    private final PersonRepository personRepository;
    private final CacheService cacheService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GenericDTO<List<PersonResponseDTO>> getAllPerson() throws NoDataFoundException {
        String key = "personResponseAll";
        String mapName = "personResponseAll";
        List<PersonResponseDTO> fromCache = (List<PersonResponseDTO>) cacheService.getFromCache(key,mapName);
        if((fromCache != null && fromCache.size() != 0)) {
            GenericDTO<List<PersonResponseDTO>> genericDTO = new GenericDTO<>(0,null);
            genericDTO.setBody(fromCache);
            return genericDTO;
        }
        List<Person> personList = new ArrayList<>(personRepository.findAll());
        if (personList.isEmpty()) {
            throw new NoDataFoundException("There is no person found");
        }
        List<PersonResponseDTO> allPerson= personList.stream()
                .map(personMapper::fromPersonToPersonResponseDto)
                .collect(Collectors.toList());
        GenericDTO<List<PersonResponseDTO>> genericDTO = new GenericDTO<>(0,null);
        genericDTO.setBody(allPerson);
        cacheService.saveToCache(allPerson, key, mapName);
        return genericDTO;
    }
}