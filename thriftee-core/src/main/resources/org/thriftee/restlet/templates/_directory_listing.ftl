<h2>${directory.title}</h2>
<hr />
<ul>
  <#list directory.files.entrySet() as file>
  <li><a href="${directory.baseRef}/${file.key}">${file.value}</a></li>
  </#list>
</ul>
<hr />
<#list directory.downloads.entrySet() as download>
  <a href="${download.key}">${download.value}</a>
</#list>  
<em>${directory.serverLine!"ThriftEE"}</em>
