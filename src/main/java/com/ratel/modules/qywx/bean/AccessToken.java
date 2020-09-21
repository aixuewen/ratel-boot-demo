package com.ratel.modules.qywx.bean;

import lombok.Data;

import java.util.Date;

@Data
public class AccessToken implements Cloneable,java.io.Serializable{
	
   /**
	 * 
	 */
	private static final long serialVersionUID = -5581390003045199087L;
   
	private String errcode ;
    private String errmsg ;
    private String access_token ;
    private String expires_in ;
    private Date date=new Date();
  
	@Override
	public Object clone() throws CloneNotSupportedException {
		return super.clone();
	}
	

}
