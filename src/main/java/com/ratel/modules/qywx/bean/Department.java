package com.ratel.modules.qywx.bean;

import lombok.Data;

@Data
public class Department implements java.io.Serializable{
	
	//private long id;
	private String  id;

	private String name;
	
	private String name_en;
	
	private String parentid;
	
	private long order;
	
	private String description;
	
	
}
