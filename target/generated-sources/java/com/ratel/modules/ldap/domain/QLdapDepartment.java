package com.ratel.modules.ldap.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QLdapDepartment is a Querydsl query type for LdapDepartment
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QLdapDepartment extends EntityPathBase<LdapDepartment> {

    private static final long serialVersionUID = -101976046L;

    public static final QLdapDepartment ldapDepartment = new QLdapDepartment("ldapDepartment");

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

    public final StringPath djkqdepName = createString("djkqdepName");

    public final StringPath djkqdepNumber = createString("djkqdepNumber");

    public final StringPath djkqId = createString("djkqId");

    public final StringPath djkqorderId = createString("djkqorderId");

    public final StringPath djkqparentDepId = createString("djkqparentDepId");

    //inherited
    public final StringPath enable = _super.enable;

    public final StringPath ext1 = createString("ext1");

    public final StringPath ext2 = createString("ext2");

    public final StringPath ext3 = createString("ext3");

    //inherited
    public final StringPath id = _super.id;

    public final StringPath uid = createString("uid");

    //inherited
    public final DateTimePath<java.util.Date> updateTime = _super.updateTime;

    //inherited
    public final StringPath updateUserId = _super.updateUserId;

    //inherited
    public final StringPath updateUserName = _super.updateUserName;

    public QLdapDepartment(String variable) {
        super(LdapDepartment.class, forVariable(variable));
    }

    public QLdapDepartment(Path<? extends LdapDepartment> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLdapDepartment(PathMetadata metadata) {
        super(LdapDepartment.class, metadata);
    }

}

