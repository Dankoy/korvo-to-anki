<#if ankiData.transcription?? && ankiData.transcription?has_content>
  <p> <#t>
    transcription: ${ankiData.transcription}<#t>
  </p><#t>
</#if>
<ul><#t>
    <#if ankiData.meanings?? && ankiData.meanings?has_content>
        <#list ankiData.meanings as meaning>
          <li><span style="color:yellow">${meaning.type()}</span><#t>
            <dl><#t>
                <#if meaning.definitions()?has_content>
                  <dt><h5 style="margin-bottom:0;margin-top:0;color:blue">definitions</h5></dt><#t>
                    <#list meaning.definitions() as definition>
                      <dd style="margin-bottom:10px"><#t>
                        <p style="margin-bottom: 0px"><#t>
                          * ${definition.info()}<#t>
                        </p><#t>
                          <#if definition.example()?? && definition.example()?has_content>
                            <p style="margin-top:1px"><#t>
                              <b>example</b>: <span><i>${definition.example()}</i></span><#t>
                            </p><#t>
                          </#if>
                          <#if definition.synonyms()?? && definition.synonyms()?has_content>
                            <p style="margin-top:1px"><#t>
                              <b>synonyms</b>: <span><i>${definition.synonyms()?join(", ")}</i></span><#t>
                            </p><#t>
                          </#if>
                          <#if definition.antonyms()?? && definition.antonyms()?has_content>
                            <p style="margin-top:1px"><#t>
                              <b>antonyms</b>: <span><i>${definition.antonyms()?join(", ")}</i></span><#t>
                            </p><#t>
                          </#if>
                      </dd><#t>
                    </#list>
                </#if>
                <#if meaning.synonyms()?has_content>
                  <p style="margin-top:1px"><#t>
                    <b>synonyms</b>: <span style="color:green"><i>${meaning.synonyms()?join(", ")}</i></span><#t>
                  </p><#t>
                </#if>
                <#if meaning.antonyms()?has_content>
                  <p style="margin-top:1px"><#t>
                    <b>antonyms</b>: <span style="color:red"><i>${meaning.antonyms()?join(", ")}</i></span><#t>
                  </p><#t>
                </#if>
            </dl><#t>
          </li><#t>
        </#list>
    </#if>
</ul><#t>
<#if ankiData.examples?? && ankiData.examples?has_content>
  <p> <#t>
    <b>examples:</b><#t>
  </p><#t>
  <ul><#t>
    <#list ankiData.examples as example>
      <li><i>${example}</i></li><#t>
    </#list>
  </ul><#t>
</#if>