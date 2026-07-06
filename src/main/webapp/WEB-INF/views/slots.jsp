<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="java.time.LocalDate" %>
<%@ page import="kz.epam.campus.model.Equipment" %>
<%@ page import="kz.epam.campus.model.Slot" %>
<%@ page import="kz.epam.campus.config.MessageHelper" %>
<%
    Locale locale = MessageHelper.resolveLocale(request);
    String langPrefix = MessageHelper.langQueryPrefix(request);
    Equipment equipment = (Equipment) request.getAttribute("equipment");
    LocalDate selectedDate = (LocalDate) request.getAttribute("selectedDate");
    List<Slot> availableSlots = (List<Slot>) request.getAttribute("availableSlots");
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="/css/style.css" />
    <title><%= MessageHelper.msg(getServletContext(), locale, "slots.title") %></title>
</head>
<body>

<h1><%= equipment.getName() %></h1>
<p><%= equipment.getDescription() %></p>

<p>
    <a href="<%= langPrefix %>lang=en">EN</a> |
    <a href="<%= langPrefix %>lang=ru">RU</a> |
    <a href="<%= langPrefix %>lang=kz">KZ</a>
</p>

<% if (request.getAttribute("error") != null) { %>
    <p style="color: red;"><%= request.getAttribute("error") %></p>
<% } %>

<form method="GET" action="/bookings">
    <input type="hidden" name="equipmentId" value="<%= equipment.getEquipmentId() %>" />
    <label for="date"><%= MessageHelper.msg(getServletContext(), locale, "slots.date_label") %></label>
    <input type="date" id="date" name="date" value="<%= selectedDate %>" />
    <button type="submit"><%= MessageHelper.msg(getServletContext(), locale, "slots.view_button") %></button>
</form>

<h2><%= MessageHelper.msg(getServletContext(), locale, "slots.heading_prefix") + " " + selectedDate %></h2>

<% if (availableSlots == null || availableSlots.isEmpty()) { %>
    <p><%= MessageHelper.msg(getServletContext(), locale, "slots.empty") %></p>
<% } else { %>
    <ul>
    <% for (Slot slot : availableSlots) { %>
        <li>
            <%= slot.getTimeStart() %>–<%= slot.getTimeEnd() %>
            <form method="POST" action="/bookings" style="display:inline;">
                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                <input type="hidden" name="equipmentId" value="<%= equipment.getEquipmentId() %>" />
                <input type="hidden" name="slotId" value="<%= slot.getSlotId() %>" />
                <input type="hidden" name="date" value="<%= selectedDate %>" />
                <button type="submit"><%= MessageHelper.msg(getServletContext(), locale, "slots.book_button") %></button>
            </form>
        </li>
    <% } %>
    </ul>
<% } %>

<p><a href="/bookings"><%= MessageHelper.msg(getServletContext(), locale, "slots.back_link") %></a></p>

</body>
</html>
