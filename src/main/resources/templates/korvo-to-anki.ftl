#separator:pipe
#html:true
#deck column:1
#tags column:6
<#list ankiDataList as ankiData>
  korvo-to-anki::${ankiData.book}|${ankiData.word}|${ankiData.translations?join(", ")}|${ankiData.myExample!""}|${ankiData.meanings}|${ankiData.book?replace(" ","_")}
</#list>


