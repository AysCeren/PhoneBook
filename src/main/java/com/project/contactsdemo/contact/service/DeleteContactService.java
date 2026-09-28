package com.project.contactsdemo.contact.service;

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
public class DeleteContactService {
    private final ContactRepository contactRepository;
    private final ContactMapper contactMapper;

    @Transactional(propagation = Propagation.REQUIRED)
    public GenericDTO<ContactResponseDTO> deleteContact(Long contactId) {

        Optional<Contact> deletedContact = contactRepository.findById(contactId);
        if (deletedContact.isPresent()) {
            if (deletedContact.get().getStatus() == 0) { //already deleted
                throw new NoDataFoundException("No such a contact to delete");
            }
            deletedContact.get().setStatus(0);
            contactRepository.save(deletedContact.get());
            GenericDTO<ContactResponseDTO> genericDTO = new GenericDTO<>(0, null);
            genericDTO.setBody(contactMapper.fromContactEntityToContactResponseDTO(deletedContact.get()));
            return genericDTO;
        } else
            throw new NoDataFoundException("No such a contact to delete");
    }
}
