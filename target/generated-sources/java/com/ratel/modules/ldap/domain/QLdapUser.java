package com.ratel.modules.ldap.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QLdapUser is a Querydsl query type for LdapUser
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QLdapUser extends EntityPathBase<LdapUser> {

    private static final long serialVersionUID = 975909003L;

    public static final QLdapUser ldapUser = new QLdapUser("ldapUser");

    public final com.ratel.framework.domain.QBaseUuidEntity _super = new com.ratel.framework.domain.QBaseUuidEntity(this);

    //inherited
    public final DateTimePath<java.util.Date> createTime = _super.createTime;

    //inherited
    public final StringPath createUserId = _super.createUserId;

    //inherited
    public final StringPath createUserName = _super.createUserName;

    //inherited
    public final StringPath dataDomain = _super.dataDomain;

    //inherited
    public final StringPath deptId = _super.deptId;

    //inherited
    public final StringPath description = _super.description;

    //inherited
    public final StringPath enable = _super.enable;

    public final StringPath ext1 = createString("ext1");

    public final StringPath ext2 = createString("ext2");

    public final StringPath ext3 = createString("ext3");

    //inherited
    public final StringPath id = _super.id;

    public final StringPath uid = createString("uid");

    public final StringPath ujkqchushiDepId = createString("ujkqchushiDepId");

    public final StringPath ujkqchushiDepName = createString("ujkqchushiDepName");

    public final StringPath ujkqchushiDepNumber = createString("ujkqchushiDepNumber");

    public final StringPath ujkqid = createString("ujkqid");

    public final StringPath ujkqorderId = createString("ujkqorderId");

    public final StringPath ujkqrealName = createString("ujkqrealName");

    public final StringPath ujkquserName = createString("ujkquserName");

    public final StringPath ujkquserNumber = createString("ujkquserNumber");

    //inherited
    public final DateTimePath<java.util.Date> updateTime = _super.updateTime;

    //inherited
    public final StringPath updateUserId = _super.updateUserId;

    //inherited
    public final StringPath updateUserName = _super.updateUserName;

    public final StringPath userPassword = createString("userPassword");

    public QLdapUser(String variable) {
        super(LdapUser.class, forVariable(variable));
    }

    public QLdapUser(Path<? extends LdapUser> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLdapUser(PathMetadata metadata) {
        super(LdapUser.class, metadata);
    }

}

