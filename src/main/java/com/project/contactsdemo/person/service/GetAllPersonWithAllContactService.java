package com.project.contactsdemo.person.service;

import com.project.contactsdemo.core.dto.GenericDTO;
import com.project.contactsdemo.core.exception.NoDataFoundException;
import com.project.contactsdemo.person.dto.PersonWithContactsDTO;
import com.project.contactsdemo.person.entity.Person;
import com.project.contactsdemo.person.mapper.PersonMapper;
import com.project.contactsdemo.person.repository.PersonRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
@Service
@RequiredArgsConstructor
public class GetAllPersonWithAllContactService {
    private final PersonRepository personRepository;
    private final PersonMapper personMapper;

    @CircuitBreaker(name = "exampleService")
    @Transactional(propagation = Propagation.REQUIRED)
    // Note: Transactional annotation will not work on private methods due to the Spring AOP Proxy Mechanism
    public GenericDTO<List<PersonWithContactsDTO>> getAllPersonWithContacts(){
        List<Person> personListWithContacts = personRepository.findAllWithContacts();
        if (personListWithContacts.isEmpty()) {
            throw new NoDataFoundException("No contacts found");
        }else{
            GenericDTO<List<PersonWithContactsDTO>> genericDTO = new GenericDTO<>(0,null);
            genericDTO.setBody(personListWithContacts.stream()
                    .map(person -> {
                        PersonWithContactsDTO response = personMapper.fromPersonToPersonResponseForContactDTO(person);
                        response.setMessage(response.getContacts().size() + " contacts for " + person.getFirstName() + " " + person.getLastName());
                        return response;
                    })
                    .collect(Collectors.toList()));
            return genericDTO;
        }
    }
}