<#macro cappedList items color><#t>
  <span style="color:${color}"><i><#t>
    <#list items as item><#t>
      <#if item_index lt 5><#t>
        ${item}<#if item_index lt items?size - 1 && item_index lt 4>, </#if><#t>
      </#if><#t>
    </#list><#t>
  </i></span><#t>
  <#if items?size gt 5><#t>
    <details style="display:block; margin-top:2px"><#t>
      <summary style="display:inline; color:${color}; cursor:pointer">more ${items?size - 5}</summary><#t>
      <span style="color:${color}"><i><#t>
        <#list items as item><#t>
          <#if item_index gte 5><#t>
            ${item}<#if item_index lt items?size - 1>, </#if><#t>
          </#if><#t>
        </#list><#t>
      </i></span><#t>
    </details><#t>
  </#if><#t>
</#macro>
<#if ankiData.transcription?? && ankiData.transcription?has_content>
  <p style="margin:0 0 6px 0"><#t>
    transcription: ${ankiData.transcription}<#t>
  </p><#t>
</#if>
<ul style="margin:0; padding-left:18px"><#t>
    <#if ankiData.meanings?? && ankiData.meanings?has_content>
        <#list ankiData.meanings as meaning>
          <li style="margin:0 0 12px 0"><span style="color:yellow">${meaning.type()}</span><#t>
            <div style="margin:4px 0 0 0"><#t>
                <#if meaning.definitions()?has_content>
                  <div style="color:blue; font-size:13px; font-weight:bold">definitions</div><#t>
                    <#list meaning.definitions() as definition>
                      <div style="margin:0 0 12px 10px"><#t>
                        <div>* ${definition.info()}</div><#t>
                          <#if definition.example()?? && definition.example()?has_content>
                            <div style="margin:3px 0 0 18px; font-size:13px; color:#555"><#t>
                              <b>example</b>: <span><i>${definition.example()}</i></span><#t>
                            </div><#t>
                          </#if>
                          <#if definition.synonyms()?? && definition.synonyms()?has_content>
                            <div style="margin:3px 0 0 18px; font-size:13px; color:#666"><#t>
                              <b>synonyms</b>: <@cappedList definition.synonyms() "#666"/><#t>
                            </div><#t>
                          </#if>
                          <#if definition.antonyms()?? && definition.antonyms()?has_content>
                            <div style="margin:3px 0 0 18px; font-size:13px; color:#8b0000"><#t>
                              <b>antonyms</b>: <@cappedList definition.antonyms() "#8b0000"/><#t>
                            </div><#t>
                          </#if>
                      </div><#t>
                    </#list>
                </#if>
                <#if meaning.synonyms()?has_content>
                  <p style="margin:6px 0 0 0; font-size:13px"><#t>
                    <b>synonyms</b>: <@cappedList meaning.synonyms() "#2e7d32"/><#t>
                  </p><#t>
                </#if>
                <#if meaning.antonyms()?has_content>
                  <p style="margin:6px 0 0 0; font-size:13px"><#t>
                    <b>antonyms</b>: <@cappedList meaning.antonyms() "#c62828"/><#t>
                  </p><#t>
                </#if>
            </div><#t>
          </li><#t>
        </#list>
    </#if>
</ul><#t>
<#if ankiData.examples?? && ankiData.examples?has_content>
  <details open style="margin:14px 0 0 0; border-top:1px solid #ccc; padding-top:8px"><#t>
    <summary style="font-size:14px; font-weight:bold; cursor:pointer">examples:</summary><#t>
    <ul style="margin:4px 0 0 0; padding-left:18px"><#t>
      <#list ankiData.examples as example>
        <li style="margin:2px 0; font-size:13px; color:#555"><i>${example}</i></li><#t>
      </#list>
    </ul><#t>
  </details><#t>
</#if>
