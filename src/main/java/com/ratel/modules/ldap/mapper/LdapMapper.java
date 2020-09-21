package com.ratel.modules.ldap.mapper;

import com.ratel.modules.ldap.domain.LdapDepartment;
import com.ratel.modules.ldap.domain.LdapUser;
import com.ratel.modules.system.domain.SysDept;
import com.ratel.modules.system.domain.SysUser;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public interface LdapMapper {
	
	public void replaceIntoDept(List<LdapDepartment> department);
	
	public void deleteAllDept();
	
	public void replaceIntoUser(List<LdapUser> userlist);

	public void deleteAllUser();

	public void replaceIntoSysDept(List<SysDept> department);

	public void deleteAllSysDept();

	public void deleteSysDeptIds();

	public void replaceIntoSysUser(List<SysUser> userlist);

	public void deleteAllSysUser();

	public void deleteAllSysUsersRoles();
	
	public void updatePassword(String password);

	public void updateSysDeptPid(String pid);

	
	public List<SysUser> findNotRoleUser();
	
	public void saveDefaultRole(List<SysUser> userlist);
	
	public void setViewDept(List<String> detpIds);
	public void replaceIntoSysUserSysDept();


	public void intoUser(SysUser userlist);

	public void intoDept(SysDept department);
}
