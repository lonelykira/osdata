      type: JVxeTypes.popupDict,
      fieldConfig: '${col.dictTable},${col.dictText},${col.dictField}',
      multi: ${col.extendParams.popupMulti?c},
    <#if col.readonly=='Y'>
      disabled: true,
    </#if>
