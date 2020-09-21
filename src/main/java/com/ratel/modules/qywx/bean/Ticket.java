package com.ratel.modules.qywx.bean;

import lombok.Data;

import java.util.Date;

@Data
public class Ticket implements java.io.Serializable, Cloneable{
	 /**
	 * 
	 */
	private static final long serialVersionUID = -2815881227505962159L;
	
	private String errcode;
    private String errmsg;
    private String ticket;
    private String expires_in;
    private Date date=new Date();
    
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
    
    
	    
}
