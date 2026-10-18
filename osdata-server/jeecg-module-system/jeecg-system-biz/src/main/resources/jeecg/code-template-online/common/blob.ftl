<#if po.fieldDbType=='Blob'>
    @com.baomidou.mybatisplus.annotation.TableField(exist = false)
    private java.lang.String ${po.fieldName}String;

    private byte[] ${po.fieldName};

    public byte[] get${po.fieldName?cap_first}(){
        if(${po.fieldName}String==null){
            return ${po.fieldName};
        }
        try {
            return ${po.fieldName}String.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return ${po.fieldName};
    }

    public void set${po.fieldName?cap_first}(byte[] ${po.fieldName}) {
        this.${po.fieldName} = ${po.fieldName};
        if (this.${po.fieldName}String == null && ${po.fieldName} != null) {
            try {
                this.${po.fieldName}String = new String(${po.fieldName}, "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
    }

    public void set${po.fieldName?cap_first}String(String ${po.fieldName}String) {
        this.${po.fieldName}String = ${po.fieldName}String;
        if (${po.fieldName}String == null) {
            this.${po.fieldName} = null;
            return;
        }
        try {
            this.${po.fieldName} = ${po.fieldName}String.getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    public String get${po.fieldName?cap_first}String(){
        if(${po.fieldName}==null || ${po.fieldName}.length==0){
            return ${po.fieldName}String == null ? "" : ${po.fieldName}String;
        }
        try {
            return new String(${po.fieldName},"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return "";
    }
<#elseif po.classType=='switch'>
    <#assign switch_extend_arr=['Y','N']>
    <#if po.dictField?default("")?contains("[")>
        <#assign switch_extend_arr=po.dictField?eval>
    </#if>
    <#list switch_extend_arr as a>
        <#if a_index == 0>
            <#assign switch_extend_arr1=a>
        <#else>
            <#assign switch_extend_arr2=a>
        </#if>
    </#list>
    @Excel(name = "${po.filedComment}", width = 15,replace = {"是_${switch_extend_arr1}","否_${switch_extend_arr2}"} )
    @Schema(description = "${po.filedComment}")
    private ${po.fieldType} ${po.fieldName};
<#elseif po.classType=='pca'>
    @Excel(name = "${po.filedComment}", width = 15,exportConvert=true,importConvert = true )
    @Schema(description = "${po.filedComment}")
    private ${po.fieldType} ${po.fieldName};

    public String convertis${po.fieldName?cap_first}() {
        return SpringContextUtils.getBean(ProvinceCityArea.class).getText(${po.fieldName});
    }

    public void convertset${po.fieldName?cap_first}(String text) {
        this.${po.fieldName} = SpringContextUtils.getBean(ProvinceCityArea.class).getCode(text);
    }
<#elseif po.classType=='cat_tree'>
    <#assign list_field_dictCode=', dictTable = "sys_category", dicText = "name", dicCode = "id"'>
    @Excel(name = "${po.filedComment}", width = 15${list_field_dictCode})
    @Schema(description = "${po.filedComment}")
    private ${po.fieldType} ${po.fieldName};
<#else>
    @Schema(description = "${po.filedComment}")
  <#if po.fieldDbName == 'del_flag'>
    @TableLogic
  </#if>
    private ${po.fieldType} ${po.fieldName};
</#if>