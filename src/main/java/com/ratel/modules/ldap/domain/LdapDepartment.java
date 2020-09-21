package com.ratel.modules.ldap.domain;

import com.ratel.framework.domain.BaseUuidEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


@Data
@Entity
@Table(name = "ldap_user")
public class LdapDepartment extends BaseUuidEntity {


	@Column(name="uid")
	private String uid;

	@Column(name = "djkqdep_name")
	private String djkqdepName;

	@Column(name = "djkqdep_number")
	private String djkqdepNumber;

	@Column(name = "djkq_id")
	private String djkqId;

	@Column(name = "djkqorder_id")
	private String djkqorderId;

	@Column(name = "djkqparent_dep_id")
	private String djkqparentDepId;
	@Column
	private String ext1;
	@Column
	private String ext2;
	@Column
	private String ext3;
	
}
