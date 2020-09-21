package com.ratel.modules.ldap.domain;

import lombok.Data;
import org.springframework.ldap.odm.annotations.Attribute;
import org.springframework.ldap.odm.annotations.Entry;
import org.springframework.ldap.odm.annotations.Id;

import javax.naming.Name;

@Data
@Entry(
        base = "ou=User",
        objectClasses = {"baseUser"})
public class User {

    @Id
    private Name uid;

    @Attribute(name = "ujkqchushiDepId")
    private String ujkqchushiDepId;

    @Attribute(name = "ujkqchushiDepName")
    private String ujkqchushiDepName;

    @Attribute(name = "ujkqchushiDepNumber")
    private String ujkqchushiDepNumber;

    @Attribute(name = "ujkqid")
    private String ujkqid;

    @Attribute(name = "ujkqorderId")
    private String ujkqorderId;

    @Attribute(name = "ujkqrealName")
    private String ujkqrealName;

    @Attribute(name = "ujkquserName")
    private String ujkquserName;

    @Attribute(name = "ujkquserNumber")
    private String ujkquserNumber;

    @Attribute(name = "userPassword")
    private byte[] userPassword;

}
