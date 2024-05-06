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
                      </dd><#t>
                    </#list>
                </#if>
                <#if meaning.synonyms()?has_content>
                  <dt><h5 style="margin-bottom:0;margin-top:0;color:green">synonyms</h5></dt><#t>
                    <#list meaning.synonyms() as synonym>
                      <dd>${synonym}</dd><#t>
                    </#list>
                </#if>
                <#if meaning.antonyms()?has_content>
                  <dt><h5 style="margin-bottom:0;margin-top:10px;color:red">antonyms</h5></dt><#t>
                    <#list meaning.antonyms() as antonym>
                      <dd>${antonym}</dd><#t>
                    </#list>
                </#if>
            </dl><#t>
          </li><#t>
        </#list>
    </#if>
</ul><#t>