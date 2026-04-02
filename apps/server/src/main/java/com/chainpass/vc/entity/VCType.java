package com.chainpass.vc.entity;

import com.baomidou.mybatisplus.annotation.*;
import java.time.Instant;

/**
 * VC类型定义实体
 */
@TableName("chain_vc_type")
public class VCType {

    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * 类型编码
     */
    private String typeCode;

    /**
     * 类型名称
     */
    private String typeName;

    /**
     * 英文名称
     */
    private String typeNameEn;

    /**
     * JSON Schema定义
     */
    private String schemaJson;

    /**
     * 默认有效期(天)
     */
    private Integer validityDays;

    /**
     * 描述
     */
    private String description;

    /**
     * 图标
     */
    private String icon;

    /**
     * 排序
     */
    private Integer sortOrder;

    /**
     * 状态: 0-启用 1-禁用
     */
    private Integer status;

    // Constructors
    public VCType() {}

    public VCType(Long id, String typeCode, String typeName, String typeNameEn, String schemaJson,
                  Integer validityDays, String description, String icon, Integer sortOrder, Integer status) {
        this.id = id;
        this.typeCode = typeCode;
        this.typeName = typeName;
        this.typeNameEn = typeNameEn;
        this.schemaJson = schemaJson;
        this.validityDays = validityDays;
        this.description = description;
        this.icon = icon;
        this.sortOrder = sortOrder;
        this.status = status;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTypeCode() { return typeCode; }
    public void setTypeCode(String typeCode) { this.typeCode = typeCode; }

    public String getTypeName() { return typeName; }
    public void setTypeName(String typeName) { this.typeName = typeName; }

    public String getTypeNameEn() { return typeNameEn; }
    public void setTypeNameEn(String typeNameEn) { this.typeNameEn = typeNameEn; }

    public String getSchemaJson() { return schemaJson; }
    public void setSchemaJson(String schemaJson) { this.schemaJson = schemaJson; }

    public Integer getValidityDays() { return validityDays; }
    public void setValidityDays(Integer validityDays) { this.validityDays = validityDays; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }

    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }

    public Integer getStatus() { return status; }
    public void setStatus(Integer status) { this.status = status; }

    // Builder
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private String typeCode;
        private String typeName;
        private String typeNameEn;
        private String schemaJson;
        private Integer validityDays;
        private String description;
        private String icon;
        private Integer sortOrder;
        private Integer status;

        public Builder id(Long id) { this.id = id; return this; }
        public Builder typeCode(String typeCode) { this.typeCode = typeCode; return this; }
        public Builder typeName(String typeName) { this.typeName = typeName; return this; }
        public Builder typeNameEn(String typeNameEn) { this.typeNameEn = typeNameEn; return this; }
        public Builder schemaJson(String schemaJson) { this.schemaJson = schemaJson; return this; }
        public Builder validityDays(Integer validityDays) { this.validityDays = validityDays; return this; }
        public Builder description(String description) { this.description = description; return this; }
        public Builder icon(String icon) { this.icon = icon; return this; }
        public Builder sortOrder(Integer sortOrder) { this.sortOrder = sortOrder; return this; }
        public Builder status(Integer status) { this.status = status; return this; }

        public VCType build() {
            return new VCType(id, typeCode, typeName, typeNameEn, schemaJson, validityDays, description, icon, sortOrder, status);
        }
    }
}