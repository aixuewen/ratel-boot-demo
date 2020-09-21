package com.ratel.modules.ldap.repository;

import com.ratel.modules.ldap.domain.User;
import org.springframework.data.ldap.repository.LdapRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends LdapRepository<User> {
    @Override
    List<User> findAll();
}
