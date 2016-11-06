/*
 * Copyright (C) 2013-2016 Benjamin Gould, and others
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.thriftee.examples;

import static javax.servlet.RequestDispatcher.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

@WebServlet(urlPatterns={ "/error.html" })
public class ShimServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;    

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        final String path = request.getServletPath();
        getServletContext().log("ShimServlet: " + path);
        if ("/error.html".equals(path)) {
            err(request, response);
            return;
        }
    }

    private void err(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        final DateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        final String now = format.format(new java.util.Date());
        final String userIp;
        final String userAgent = request.getHeader("user-agent");
        final String servletName;
        final String requestUri;
        final Integer statusCode;
        final String errorMessage;
        final Class<?> exType;
        final String stackTrace;

        boolean isError = request.getAttribute(ERROR_SERVLET_NAME) != null;

        if (isError) {
            servletName = (String) request.getAttribute(ERROR_SERVLET_NAME);
            requestUri = (String) request.getAttribute(ERROR_REQUEST_URI);
            statusCode = (Integer) request.getAttribute(ERROR_STATUS_CODE);
            errorMessage = (String) request.getAttribute(ERROR_MESSAGE);
            exType = (Class<?>) request.getAttribute(ERROR_EXCEPTION_TYPE);
            Throwable t = (Throwable) request.getAttribute(ERROR_EXCEPTION);
            if (t != null) {
                final java.io.StringWriter sw = new java.io.StringWriter();
                final java.io.PrintWriter pw = new java.io.PrintWriter(sw);
                if (t != null) {
                    t.printStackTrace(pw);
                    pw.flush();
                }
                stackTrace = sw.toString();
            } else {
                stackTrace = null;
            }
        } else {
            servletName = getServletConfig().getServletName();
            requestUri = request.getRequestURI();
            statusCode = response.getStatus();
            errorMessage = null;
            exType = null;
            stackTrace = null;
        }
        final String forwardedFor = request.getHeader("x-forwarded-for");
        if (forwardedFor == null || forwardedFor.trim().isEmpty()) {
            userIp = request.getRemoteAddr();
        } else {
            userIp = forwardedFor;
        }

        final PrintWriter out = response.getWriter();
        out.println(headerContent);
        out.println("<ul>");

        out.print("<li>Date/time: ");
        out.print(esc(now));
        out.println("</li>");

        out.print("<li>User agent: ");
        out.print(esc(userAgent));
        out.println("</li>");

        out.print("<li>User IP: ");
        out.print(esc(userIp));
        out.println("</li>");

        out.print("<li>Request UI: <a href=\"" + esc(requestUri) + "\">");
        out.print(esc(requestUri));
        out.println("</a></li>");

        out.print("<li>Servlet Name: ");
        out.print(esc(servletName));
        out.println("</li>");

        out.print("<li>Status code: ");
        out.print(esc(statusCode));
        out.println("</li>");

        out.print("<li>Error Message: ");
        out.print(esc(errorMessage));
        out.println("</li>");

        out.print("<li>Exception Type: ");
        out.print(esc(exType));
        out.println("</li>");

        out.print("<li>Stack Trace: <pre>");
        out.print(esc(stackTrace));
        out.println("</pre></li>");

        out.println("</ul>");
        out.println(footerContent);
    }

    public static String esc(Object obj) {
        return obj == null 
                  ? "" 
                  : StringEscapeUtils.escapeHtml(obj.toString());
    }

    private static final String headerContent = 
        "<!DOCTYPE html>" +
        "<html lang=\"en\">" +
        "<head>" +
        "    <style type=\"text/css\">" +
        "        body {" +
        "            font-family: sans-serif;" +
        "            font-size: medium;" +
        "            padding-top: 40px;" +
        "            padding-bottom: 40px;" +
        "            background-color: #f5f5f5;" +
        "        }" +
        "        .container {" +
        "            font-size: medium;" +
        "            font-family: monospace;" +
        "        }" +
        "        ul {" +
        "            max-width: 1024px;" +
        "            padding: 19px 29px 29px;" +
        "            margin: 0 auto 20px;" +
        "            background-color: #fff;" +
        "            border: 1px solid #e5e5e5;" +
        "            -webkit-border-radius: 5px;" +
        "            -moz-border-radius: 5px;" +
        "            border-radius: 5px;" +
        "            -webkit-box-shadow: 0 1px 2px rgba(0,0,0,.05);" +
        "            -moz-box-shadow: 0 1px 2px rgba(0,0,0,.05);" +
        "            box-shadow: 0 1px 2px rgba(0,0,0,.05);" +
        "        }" +
        "    </style>" +
        "<head>" +
        "<body>" +
        "    <div class=\"container\">";

    private static final String footerContent = 
        "    </div>" +
        "</body>" +
        "</html>";

}
