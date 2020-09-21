package com.ratel.modules.qywx.bean;

import lombok.Data;

import java.util.List;

@Data
public class UserList extends Wx {

	private List<WxUser> userlist;
	
}
