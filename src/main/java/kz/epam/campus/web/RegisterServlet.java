package kz.epam.campus.web;

import kz.epam.campus.config.MessageHelper;
import kz.epam.campus.model.Role;
import kz.epam.campus.model.User;
import kz.epam.campus.services.UserService;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Locale;

@WebServlet("/register")
public class RegisterServlet extends HttpServlet {

    private UserService userService;

    @Override
    public void init() throws ServletException {
        userService = WebApplicationContextUtils
                .getWebApplicationContext(getServletContext())
                .getBean(UserService.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String name = req.getParameter("name");
        String email = req.getParameter("email");
        String password = req.getParameter("password");

        Locale locale = MessageHelper.resolveLocale(req);

        if (name == null || name.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.trim().isEmpty()) {
            req.setAttribute("error", MessageHelper.msg(getServletContext(), locale, "register.error.empty_fields"));
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            return;
        }

        name = name.trim();
        email = email.trim();
        password = password.trim();

        if (userService.emailExists(email)) {
            req.setAttribute("error", MessageHelper.msg(getServletContext(), locale, "register.error.duplicate_email"));
            req.getRequestDispatcher("/WEB-INF/views/register.jsp").forward(req, resp);
            return;
        }

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(password);
        user.setPosition(Role.USER);
        user.setActive(true);

        userService.register(user);

        resp.sendRedirect("/login?registered");
    }
}