<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="kz.epam.campus.config.MessageHelper" %>
<%@ page import="java.util.Locale" %>
<%
    Locale locale = MessageHelper.resolveLocale(request);
    String langPrefix = MessageHelper.langQueryPrefix(request);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="/css/style.css" />
    <title><%= MessageHelper.msg(getServletContext(), locale, "register.title") %></title>
</head>
<body>

<h1><%= MessageHelper.msg(getServletContext(), locale, "register.heading") %></h1>

<p>
    <a href="<%= langPrefix %>lang=en">EN</a> |
    <a href="<%= langPrefix %>lang=ru">RU</a> |
    <a href="<%= langPrefix %>lang=kz">KZ</a>
</p>

<% if (request.getAttribute("error") != null) { %>
    <p style="color: red;"><%= request.getAttribute("error") %></p>
<% } %>

<form method="POST" action="/register">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />

    <label for="name"><%= MessageHelper.msg(getServletContext(), locale, "register.name_label") %></label>
    <input type="text" id="name" name="name" required autofocus />

    <label for="email"><%= MessageHelper.msg(getServletContext(), locale, "register.email_label") %></label>
    <input type="email" id="email" name="email" required />

    <label for="password"><%= MessageHelper.msg(getServletContext(), locale, "register.password_label") %></label>
    <input type="password" id="password" name="password" required />

    <button type="submit"><%= MessageHelper.msg(getServletContext(), locale, "register.button") %></button>
</form>

<p><%= MessageHelper.msg(getServletContext(), locale, "register.has_account") %>
   <a href="/login"><%= MessageHelper.msg(getServletContext(), locale, "register.login_link") %></a></p>

</body>
</html>
