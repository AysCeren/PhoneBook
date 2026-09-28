package com.project.contactsdemo.contact.service;

import com.project.contactsdemo.core.cache.CacheNames;
import com.project.contactsdemo.core.cache.CacheService;
import com.project.contactsdemo.contact.dto.ContactRequestDTO;
import com.project.contactsdemo.contact.dto.ContactResponseDTO;
import com.project.contactsdemo.core.dto.GenericDTO;
import com.project.contactsdemo.contact.entity.Contact;
import com.project.contactsdemo.contact.mapper.ContactMapper;
import com.project.contactsdemo.contact.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ContactSaveService {
    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    private final CacheService cacheService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GenericDTO<ContactResponseDTO> saveContact(ContactRequestDTO saveContactRequestDto) {
        //öncelikle gelen contact'ın personId'sine bakalım
        Contact contact = contactMapper.fromContactRequestDTOToContactEntity(saveContactRequestDto);
        this.contactRepository.save(contact);
        cacheService.clearAfterCommit(CacheNames.CONTACT_RESPONSE_ALL, CacheNames.PERSON_WITH_CONTACTS); //cached contact lists are now outdated
        GenericDTO<ContactResponseDTO> genericDTO = new GenericDTO<>(0, null);
        //Rest Template ile iletişim kurulan yer. Circuit Breaker'ın burada olması gerekir.
        genericDTO.setBody(contactMapper.fromContactEntityToContactResponseDTO(contact));
        return genericDTO;
    }
}
