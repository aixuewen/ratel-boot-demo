package com.ratel.modules.ldap.service;

import com.ratel.modules.jsth.utils.UUIDUtils;
import com.ratel.modules.ldap.domain.*;
import com.ratel.modules.ldap.mapper.LdapMapper;
import com.ratel.modules.system.domain.SysDept;
import com.ratel.modules.system.domain.SysUser;
import com.ratel.modules.system.repository.SysDeptRepository;
import com.ratel.modules.system.repository.SysUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
@Order(value=999)
@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class LdapService  {

    private static final Logger log = LoggerFactory.getLogger(LdapService.class);

    @Autowired
    private UserService userService;

    @Autowired
    private DeptService  deptService;

    @Autowired
    private LdapMapper ldapMapper;

    @Value("${wx.uploadDeptIds}")
    private String uploadDeptIds;

    @Autowired
    private LdapMessageService ldapMessageService;


    @Autowired
    private SysUserRepository sysUserRepository;

    @Autowired
    private SysDeptRepository sysDeptRepository;

    @Transactional(rollbackFor = Exception.class)
    public void saveLdapUser() {
        List<User> users = userService.findAll();
        log.info("users: {}", users);
        List<LdapUser> userList=new ArrayList<LdapUser>();
       for(int i=0;i<users.size();i++) {
            User d=users.get(i);
            LdapUser sd=new LdapUser();
            sd.setId(UUIDUtils.getUUID());
            sd.setUid(d.getUid().get(1).split("=")[1]);
            sd.setUjkqchushiDepId(d.getUjkqchushiDepId());
            sd.setUjkqchushiDepName(d.getUjkqchushiDepName());
            sd.setUjkqchushiDepNumber(d.getUjkqchushiDepNumber());
            sd.setUjkqid(d.getUjkqid());
            sd.setUjkqorderId(d.getUjkqorderId());
            sd.setUjkqrealName(d.getUjkqrealName());
            sd.setUjkquserName(d.getUjkquserName());
            sd.setUjkquserNumber(d.getUjkquserNumber());
            sd.setUserPassword(new String(d.getUserPassword()));
            sd.setUjkqid(d.getUjkqid());

            userList.add(sd);
       }
        ldapMapper.deleteAllUser();
        ldapMapper.replaceIntoUser(userList);
        //往sysUser表中存数据

        List<SysUser> userSysList=new ArrayList<SysUser>();

        //ldapMapper.deleteAllSysUsersRoles();
        ldapMapper.deleteAllSysUser();
        //向sys_dept 表中存入 不存在的用户表中的部门id
        ldapMapper.replaceIntoSysUserSysDept();
        //ldapMapper.replaceIntoSysUser(userSysList);
        for(int i=0;i<users.size();i++) {
            User user=users.get(i);
            SysUser su=new SysUser();
            //su.setId(UUIDUtils.getUUID());
            su.setId(user.getUjkqid());
            su.setUsername(user.getUjkquserName());
            //su.setEmail(user.getUjkqid()+"@qq.com");
            su.setEmail("");
            su.setPhone("");

            su.setEnable("1");
            su.setEnabled(true);

            su.setPassword(new String(user.getUserPassword()));
            su.setSex(null);
            su.setType("ldap");
            su.setStatus("0");
            su.setSort(Long.valueOf(user.getUjkqorderId()));
            su.setDeptId(String.valueOf(user.getUjkqchushiDepId()));


            su.setNickName(user.getUjkqrealName());

            //userSysList.add(su);

            //sysUserRepository.save(su);
            ldapMapper.intoUser(su);
        }
        /**
         * 添加默认密码
         */
        //ldapMapper.updatePassword(password);
        /**
         * 添加默认权限
         */
        List<SysUser> ulist=ldapMapper.findNotRoleUser();
        if(ulist!=null&&ulist.size()>0) {
            //ldapMapper.saveDefaultRole(ulist);
        }else {
            log.debug("-------------没有需要设置默认权限的用户");
        }


        saveError("LdapUser,SysUser","getLdapUser","成功插入"+userList.size()+"条记录","成功");

    }

    @Transactional(rollbackFor = Exception.class)
    public void saveLdapDept() {
        List<Dept> depts = deptService.findAll();

        log.info("depts: {}", depts);

        List<LdapDepartment> deptList=new ArrayList<LdapDepartment>();
        for(int i=0;i<depts.size();i++) {
            Dept d=depts.get(i);
            LdapDepartment sd=new LdapDepartment();

            sd.setId(UUIDUtils.getUUID());
            sd.setUid(d.getUid().get(1).split("=")[1]);

            sd.setDjkqdepName(d.getDjkqdepName());
            sd.setDjkqdepNumber(d.getDjkqdepNumber());
            sd.setDjkqId(d.getDjkqId());
            sd.setDjkqorderId(d.getDjkqorderId());
            sd.setDjkqparentDepId(d.getDjkqparentDepId());



            deptList.add(sd);
        }
        ldapMapper.deleteAllDept();
        ldapMapper.replaceIntoDept(deptList);
       // saveError("LdapDept","getLdapDept","成功插入"+deptList.size()+"条记录","成功");


        //往sysdept表中存数据
        List<SysDept> deptSysList=new ArrayList<SysDept>();

        ldapMapper.deleteAllSysDept();

        for(int i=0;i<depts.size();i++) {
            Dept d=depts.get(i);
            SysDept sd=new SysDept();
            sd.setId(d.getUid().get(1).split("=")[1]);
            sd.setName(d.getDjkqdepName());
            sd.setPid(d.getDjkqparentDepId());
            //sd.setDesPid("0");
            sd.setEnable("1");
            sd.setEnabled(true);
            sd.setSort(Long.parseLong(d.getDjkqorderId()));
            sd.setType("ldap");
            sd.setStatus("0");
            //sd.setDescription(String.valueOf(times));
            if("root".equals(d.getUid().get(1).split("=")[1])){
                sd.setPid((uploadDeptIds));//uploadDeptIds
            }
            //deptSysList.add(sd);
            //sysDeptRepository.save(sd);
            ldapMapper.intoDept(sd);
        }
        //ldapMapper.deleteAllSysUsersRoles();
        //ldapMapper.deleteSysDeptIds();

        //接下来更新原有跟节点

        ldapMapper.updateSysDeptPid(uploadDeptIds);
        //ldapMapper.replaceIntoSysDept(deptSysList);
        saveError("LdapDept,SysDept","getLdapDept","成功插入"+deptList.size()+"条记录","成功");

    }
    @Transactional(rollbackFor = Exception.class)
    public void saveError(String tableName,String method,String mes,String type) {
        LdapMessage le = new LdapMessage();
        le.setId(UUIDUtils.getUUID());
        le.setMethod(method);
        le.setTable(tableName);
        le.setMessage(mes);
        le.setType(type);
        ldapMessageService.save(le);
    }

}
