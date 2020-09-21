package ${package}.domain;

import lombok.Data;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;

import com.ratel.framework.core.domain.BaseNativeEntity;
import com.ratel.framework.core.domain.BaseUuidEntity;

import javax.persistence.*;
<#if isNotNullColumns??>
import javax.validation.constraints.*;
</#if>
<#if hasDateAnnotation>
import javax.persistence.Entity;
import javax.persistence.Table;
import org.hibernate.annotations.*;
</#if>
<#if hasTimestamp>
import java.sql.Timestamp;
</#if>
<#if hasBigDecimal>
import java.math.BigDecimal;
</#if>
import java.io.Serializable;

/**
* @author ${author}
* @date ${date}
*/
@Entity
@Data
@Table(name="${tableName}")
public class ${className} extends <#if pkColumnType == 'String'>BaseUuidEntity<#else>BaseNativeEntity</#if> {
<#if columns??>
    <#list columns as column>
    <#if column.remark != ''>
        /** ${column.remark} */
    </#if>
    <#if column.changeColumnName != 'id' && column.changeColumnName != 'createUserName' && column.changeColumnName != 'createAccountId'
        && column.changeColumnName != 'createTime' && column.changeColumnName != 'updateTime' && column.changeColumnName != 'updateUserName'
        && column.changeColumnName != 'updateAccountId' && column.changeColumnName != 'systemDeptId' && column.changeColumnName != 'dataDomain'
        && column.changeColumnName != 'systemRemark' && column.changeColumnName != 'systemStatus'>
        @Column(name = "${column.columnName}"<#if column.columnKey = 'UNI'>,unique = true</#if><#if column.istNotNull && column.columnKey != 'PRI'>,nullable = false</#if>)
        <#if column.istNotNull && column.columnKey != 'PRI'>
            <#if column.columnType = 'String'>
                @NotBlank
            <#else>
                @NotNull
            </#if>
        </#if>
        private ${column.columnType} ${column.changeColumnName};
    </#if>

    </#list>
</#if>

    public void copy(${className} source){
        BeanUtil.copyProperties(source,this, CopyOptions.create().setIgnoreNullValue(true));
    }
}
