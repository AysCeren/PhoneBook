package com.project.contactsdemo.person.service;


import com.project.contactsdemo.core.dto.GenericDTO;
import com.project.contactsdemo.core.exception.NoDataFoundException;
import com.project.contactsdemo.core.mapperhelpermethods.BirthCityConverter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PersonGetWithIDService {

    private final BirthCityConverter birthCityConverter;

    @Transactional(propagation = Propagation.REQUIRED)
    public GenericDTO<String> getPersonWithId(String ilKodu) throws NoDataFoundException {
        GenericDTO<String> gDTO = new GenericDTO<>(0, "null");
        gDTO.setBody(birthCityConverter.birthCityName(ilKodu));
        return gDTO;
    }
}
