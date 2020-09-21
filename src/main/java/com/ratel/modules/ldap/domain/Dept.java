package com.ratel.modules.ldap.domain;

import lombok.Data;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;

@Data
@Entry(
        base = "ou=Department",
        objectClasses = {"department"})
public class Dept {

    @Id
    private Name uid;

    @Attribute(name = "djkqdepName")
    private String djkqdepName;

    @Attribute(name = "djkqdepNumber")
    private String djkqdepNumber;

    @Attribute(name = "djkqId")
    private String djkqId;

    @Attribute(name = "djkqorderId")
    private String djkqorderId;

    @Attribute(name = "djkqparentDepId")
    private String djkqparentDepId;



}
