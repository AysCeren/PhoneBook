package com.project.contactsdemo.contact.service;

import com.project.contactsdemo.core.cache.CacheNames;
import com.project.contactsdemo.core.cache.CacheService;
import com.project.contactsdemo.contact.dto.ContactRequestDTO;
import com.project.contactsdemo.contact.dto.ContactResponseDTO;
import com.project.contactsdemo.contact.entity.Contact;
import com.project.contactsdemo.contact.mapper.ContactMapper;
import com.project.contactsdemo.contact.repository.ContactRepository;
import com.project.contactsdemo.core.dto.GenericDTO;
import com.project.contactsdemo.core.exception.NoDataFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UpdateContactService {

    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;
    private final CacheService cacheService;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public GenericDTO<ContactResponseDTO> updateContact(ContactRequestDTO updatedContactRequestDTO, Long contactId) {
        Optional<Contact> updatedContact = contactRepository.findById(contactId);
        if(updatedContact.isPresent()) {
            Contact newOne = contactMapper.fromContactRequestDTOToContactEntity(updatedContactRequestDTO);
            newOne.setId(updatedContact.get().getId());
            contactRepository.save(newOne);
            cacheService.clearAfterCommit(CacheNames.CONTACT_RESPONSE_ALL, CacheNames.PERSON_WITH_CONTACTS); //cached contact lists are now outdated
            GenericDTO<ContactResponseDTO> genericDTO = new GenericDTO<>(0,null);
            genericDTO.setBody(contactMapper.fromContactEntityToContactResponseDTO(newOne));
            return genericDTO;
            //TODO: Burada neden newOne yaparak aldığımızı bulalım.
        }
        else{
            throw new NoDataFoundException("No such a contact to update");
        }
    }
}
