package com.ratel.modules.ldap.domain;

import com.ratel.framework.domain.BaseUuidEntity;
import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;


@Data
@Entity
@Table(name = "ldap_message")
public class LdapMessage extends BaseUuidEntity {


	@Column(name="id")
	private String id;

	@Column(name = "mes_method")
	private String method;

	@Column(name = "mes_table")
	private String table;

	@Column(name = "mes_message")
	private String message;

	@Column(name = "mes_type")
	private String type;

	@Column
	private String ext1;
	@Column
	private String ext2;
	@Column
	private String ext3;
	
}
