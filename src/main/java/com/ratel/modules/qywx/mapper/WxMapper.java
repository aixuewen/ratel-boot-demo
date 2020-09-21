package com.ratel.modules.qywx.mapper;

import com.ratel.modules.system.domain.SysDept;
import com.ratel.modules.system.domain.SysUser;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public interface WxMapper {
	
	public void replaceIntoDept(List<SysDept> department);

	public void intoDept(SysDept department);

	
	public void deleteAllDept();
	
	public void replaceIntoUser(List<SysUser> userlist);

	public void intoUser(SysUser userlist);

	public void deleteAllUser();
	
	public void updatePassword(String password);
	
	public List<SysUser> findNotRoleUser();
	
	public void saveDefaultRole(List<SysUser> userlist);
	
	public void setViewDept(List<String> detpIds);

	public Map findById(String id);
	
}
