package com.project.contactsdemo.contact.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@AllArgsConstructor
@NoArgsConstructor

@Data
public class ContactResponseDTO implements Serializable {
    private Long id;
    private String name;
    private String phoneNumber;
    //private Integer status; //gözüküyorsa bir olacağı için kesin göstermek gereksiz
    private Long personId;
}
