package com.ratel.modules.ldap.repository;

import com.ratel.modules.ldap.domain.Dept;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeptRepository extends LdapRepository<Dept> {
    @Override
    List<Dept> findAll();
}
