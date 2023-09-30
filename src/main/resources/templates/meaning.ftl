<ul>
    <#if ankiData.meanings?? && ankiData.meanings?has_content>
        <#list ankiData.meanings as meaning>
          <li><span style="color:yellow">${meaning.type()}</span>
            <dl>
                <#if meaning.definitions()?has_content>
                  <dt><h5 style="margin-bottom:0;margin-top:0;color:blue">definitions</h5></dt>
                    <#list meaning.definitions() as definition>
                      <dd style="margin-bottom:10px">
                        <p style="margin-bottom: 0px">
                          * ${definition.info()}
                        </p>
                          <#if definition.example()?? && definition.example()?has_content>
                            <p style="margin-top:1px">
                              <b>example</b>: <span><i>${definition.example()}</i></span>
                            </p>
                          </#if>
                      </dd>
                    </#list>
                </#if>
                <#if meaning.synonyms()?has_content>
                  <dt><h5 style="margin-bottom:0;margin-top:0;color:green">synonyms</h5></dt>
                    <#list meaning.synonyms() as synonym>
                      <dd>${synonym}</dd>
                    </#list>
                </#if>
                <#if meaning.antonyms()?has_content>
                  <dt><h5 style="margin-bottom:0;margin-top:10px;color:red">antonyms</h5></dt>
                    <#list meaning.antonyms() as antonym>
                      <dd>${antonym}</dd>
                    </#list>
                </#if>
            </dl>
          </li>
        </#list>
    </#if>
</ul>