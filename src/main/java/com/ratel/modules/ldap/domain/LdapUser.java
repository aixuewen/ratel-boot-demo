package com.ratel.modules.ldap.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.ratel.framework.domain.BaseUuidEntity;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;


@Data
@Entity
@Table(name = "ldap_user")
public class LdapUser extends BaseUuidEntity {


	@Column(name="uid")
	private String uid;

	@Column(name = "ujkqchushi_dep_id")
	private String ujkqchushiDepId;

	@Column(name = "ujkqchushi_dep_name")
	private String ujkqchushiDepName;

	@Column(name = "ujkqchushi_dep_number")
	private String ujkqchushiDepNumber;

	@Column(name = "ujkqid")
	private String ujkqid;

	@Column(name = "ujkqorder_id")
	private String ujkqorderId;

	@Column(name = "ujkqreal_name")
	private String ujkqrealName;

	@Column(name = "ujkquser_name")
	private String ujkquserName;

	@Column(name = "ujkquser_number")
	private String ujkquserNumber;

	@Column(name = "user_password")
	private String userPassword;

	@Column
	private String ext1;
	@Column
	private String ext2;
	@Column
	private String ext3;
	
}
