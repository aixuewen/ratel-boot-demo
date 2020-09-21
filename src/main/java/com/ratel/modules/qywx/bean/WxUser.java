package com.ratel.modules.qywx.bean;

import lombok.Data;

import java.util.List;

@Data
public class WxUser {
	
	    private String userid;

	    private String name;

	    private String position;

	    private String mobile;

	    private String gender;

	    private String email;

	    private String errcode;
	    
		private String errmsg;
		
		private List<Integer> department;
		
		private List<Integer> order;
		
		private Integer status;

		private Integer enable;

		private String alias;

		
		
		
	
}
