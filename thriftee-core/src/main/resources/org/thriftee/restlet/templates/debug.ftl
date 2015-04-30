<#include "_header.ftl">

<h2>${title}</h2>

<pre>
Root URI: ${restlet.reference!"<null>"}
Root Ref: ${restlet.rootRef!"<null>"}
Base Ref: ${restlet.reference.baseRef!"<null>"}
Leftover: ${restlet.reference.remainingPart!"<null>"}
</pre>

<#include "_footer.ftl">
