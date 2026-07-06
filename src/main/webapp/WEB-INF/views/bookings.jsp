<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Locale" %>
<%@ page import="kz.epam.campus.model.Equipment" %>
<%@ page import="kz.epam.campus.web.BookingServlet.BookingView" %>
<%@ page import="kz.epam.campus.config.MessageHelper" %>
<%
    Locale locale = MessageHelper.resolveLocale(request);
    String langPrefix = MessageHelper.langQueryPrefix(request);
%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <link rel="stylesheet" href="/css/style.css" />
    <title><%= MessageHelper.msg(getServletContext(), locale, "bookings.title") %></title>
</head>
<body>

<h1><%= MessageHelper.msg(getServletContext(), locale, "bookings.heading") %></h1>

<p>
    <a href="<%= langPrefix %>lang=en">EN</a> |
    <a href="<%= langPrefix %>lang=ru">RU</a> |
    <a href="<%= langPrefix %>lang=kz">KZ</a>
</p>

<form method="POST" action="/logout" style="display:inline;">
    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
    <button type="submit"><%= MessageHelper.msg(getServletContext(), locale, "bookings.logout") %></button>
</form>

<p><a href="/profile"><%= MessageHelper.msg(getServletContext(), locale, "profile.heading") %></a></p>

<% if (request.getParameter("error") != null) { %>
    <p style="color: red;"><%= request.getParameter("error") %></p>
<% } %>

<%
    List<BookingView> myBookings = (List<BookingView>) request.getAttribute("myBookings");
%>

<% if (myBookings == null || myBookings.isEmpty()) { %>
    <p><%= MessageHelper.msg(getServletContext(), locale, "bookings.empty") %></p>
<% } else { %>
    <ul>
    <% for (BookingView bv : myBookings) { %>
        <li>
            <%= bv.equipment.getName() %> —
            <%= bv.slot.getDate() %> <%= bv.slot.getTimeStart() %>–<%= bv.slot.getTimeEnd() %> —
            <%= MessageHelper.msg(getServletContext(), locale, "bookings.status_label") %> <%= bv.booking.getStatus() %>

            <% if (bv.booking.getStatus().name().equals("CONFIRMED")) { %>
                <form method="POST" action="/bookings/cancel" style="display:inline;">
                    <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}" />
                    <input type="hidden" name="bookingId" value="<%= bv.booking.getBookingId() %>" />
                    <button type="submit"><%= MessageHelper.msg(getServletContext(), locale, "bookings.cancel_button") %></button>
                </form>
            <% } %>
        </li>
    <% } %>
    </ul>
<% } %>

<h2><%= MessageHelper.msg(getServletContext(), locale, "bookings.equipment_heading") %></h2>

<%
    List<Equipment> equipmentList = (List<Equipment>) request.getAttribute("equipmentList");
%>

<ul>
<% for (Equipment eq : equipmentList) { %>
    <li>
        <a href="/bookings?equipmentId=<%= eq.getEquipmentId() %>"><%= eq.getName() %></a>
        — <%= eq.getDescription() %>
    </li>
<% } %>
</ul>

</body>
</html>
