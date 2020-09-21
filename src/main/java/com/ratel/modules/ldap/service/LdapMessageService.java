package com.ratel.modules.ldap.service;

import com.ratel.framework.service.BaseService;
import com.ratel.modules.ldap.domain.LdapMessage;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(propagation = Propagation.SUPPORTS, readOnly = true, rollbackFor = Exception.class)
public class LdapMessageService extends BaseService<LdapMessage, Long> {

}
