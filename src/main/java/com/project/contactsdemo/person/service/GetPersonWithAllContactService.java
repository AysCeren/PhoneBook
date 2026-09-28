package com.project.contactsdemo.person.service;

import com.project.contactsdemo.contact.dto.ContactResponseDTO;
import com.project.contactsdemo.contact.mapper.ContactMapper;
import com.project.contactsdemo.core.dto.GenericDTO;
import com.project.contactsdemo.core.exception.NoDataFoundException;
import com.project.contactsdemo.person.entity.Person;
import com.project.contactsdemo.person.repository.PersonRepository;
import com.project.contactsdemo.core.cache.CacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GetPersonWithAllContactService {
    private final PersonRepository personRepository;
    private final ContactMapper contactMapper;
    private final CacheService cacheService;
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GenericDTO<List<ContactResponseDTO>> getAllContactsOfPerson(Long personId) {
        String key = personId.toString();
        String map = "personWithContacts";
        List<ContactResponseDTO> fromCache = (List<ContactResponseDTO>) cacheService.getFromCache(key, map);
        if ((fromCache != null && fromCache.size() != 0)) {
            GenericDTO<List<ContactResponseDTO>> genericDTO = new GenericDTO<>(0, null);
            genericDTO.setBody(fromCache);
            return genericDTO;
        }
        Optional<Person> person = (personRepository.findById(personId));
        if (person.isPresent()) {
            GenericDTO<List<ContactResponseDTO>> genericDTO = new GenericDTO<>(0, null);
            List<ContactResponseDTO> contactOfPerson = person.get().getContacts().stream().filter(contact -> contact.getStatus() == 1).map(contactMapper::fromContactEntityToContactResponseDTO).collect(Collectors.toList());
            genericDTO.setBody(contactOfPerson);
            cacheService.saveToCache(contactOfPerson, key, map);
            return genericDTO;
        } else
            throw new NoDataFoundException("There is no person found");
    }
}
