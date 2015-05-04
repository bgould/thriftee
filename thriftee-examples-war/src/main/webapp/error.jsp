<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.StringWriter"%>
<%@ page session="false" %><%@page import="thriftee.shaded.org.apache.commons.lang.StringEscapeUtils"%>
<pre>
<% out.println(StringEscapeUtils.escapeHtml(pageContext.getErrorData().toString())); %>
<% out.println(StringEscapeUtils.escapeHtml(pageContext.getErrorData().getRequestURI())); %>
<% 
	if (pageContext.getErrorData().getThrowable() != null) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		pageContext.getErrorData().getThrowable().printStackTrace(pw);
		pw.flush();
		out.println(StringEscapeUtils.escapeHtml(sw.toString()));	
	}
	
%>
</pre>