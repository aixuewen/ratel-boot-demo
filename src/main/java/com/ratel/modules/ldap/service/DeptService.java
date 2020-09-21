package com.ratel.modules.ldap.service;

import com.ratel.modules.ldap.domain.Dept;
import com.ratel.modules.ldap.repository.DeptRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DeptService {

    private final DeptRepository deptRepository;

    @Autowired
    public DeptService(final DeptRepository deptRepository) {
        this.deptRepository = deptRepository;
    }

    public List<Dept> findAll() {
        return this.deptRepository.findAll();
    }
}
