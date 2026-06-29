<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="kz.epam.campus.config.MessageHelper" %>
<%@ page import="java.util.Locale" %>
<%
    Locale locale = kz.epam.campus.config.MessageHelper.resolveLocale(request);
    String langPrefix = MessageHelper.langQueryPrefix(request);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="/css/style.css" />
    <title><%= MessageHelper.msg(getServletContext(), locale, "login.title") %></title>
</head>
<body>

<h1><%= MessageHelper.msg(getServletContext(), locale, "login.heading") %></h1>

<p>
    <a href="<%= langPrefix %>lang=en">EN</a> |
    <a href="<%= langPrefix %>lang=ru">RU</a> |
    <a href="<%= langPrefix %>lang=kz">KZ</a>
</p>

<% if (request.getParameter("error") != null) { %>
    <p style="color: red;"><%= MessageHelper.msg(getServletContext(), locale, "login.error") %></p>
<% } %>

<% if (request.getParameter("logout") != null) { %>
    <p style="color: green;"><%= MessageHelper.msg(getServletContext(), locale, "login.logout_message") %></p>
<% } %>

<form method="POST" action="/login">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

    <label for="username"><%= MessageHelper.msg(getServletContext(), locale, "login.email_label") %></label>
    <input type="email" id="username" name="username" required autofocus />

    <label for="password"><%= MessageHelper.msg(getServletContext(), locale, "login.password_label") %></label>
    <input type="password" id="password" name="password" required />

    <button type="submit"><%= MessageHelper.msg(getServletContext(), locale, "login.heading") %></button>
</form>

<p><%= MessageHelper.msg(getServletContext(), locale, "login.no_account") %>
   <a href="/register"><%= MessageHelper.msg(getServletContext(), locale, "login.register_link") %></a></p>

</body>
</html>
