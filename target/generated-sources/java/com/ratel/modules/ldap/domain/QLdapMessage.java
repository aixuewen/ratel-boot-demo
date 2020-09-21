package com.ratel.modules.ldap.domain;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.Generated;
import com.querydsl.core.types.Path;


/**
 * QLdapMessage is a Querydsl query type for LdapMessage
 */
@Generated("com.querydsl.codegen.EntitySerializer")
public class QLdapMessage extends EntityPathBase<LdapMessage> {

    private static final long serialVersionUID = 1773634375L;

    public static final QLdapMessage ldapMessage = new QLdapMessage("ldapMessage");

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

    public final StringPath id = createString("id");

    public final StringPath message = createString("message");

    public final StringPath method = createString("method");

    public final StringPath table = createString("table");

    public final StringPath type = createString("type");

    //inherited
    public final DateTimePath<java.util.Date> updateTime = _super.updateTime;

    //inherited
    public final StringPath updateUserId = _super.updateUserId;

    //inherited
    public final StringPath updateUserName = _super.updateUserName;

    public QLdapMessage(String variable) {
        super(LdapMessage.class, forVariable(variable));
    }

    public QLdapMessage(Path<? extends LdapMessage> path) {
        super(path.getType(), path.getMetadata());
    }

    public QLdapMessage(PathMetadata metadata) {
        super(LdapMessage.class, metadata);
    }

}

