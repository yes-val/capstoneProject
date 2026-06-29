package kz.epam.campus.web;

import kz.epam.campus.model.Equipment;
import kz.epam.campus.services.EquipmentService;
import kz.epam.campus.services.security.SecurityService;
import org.springframework.web.context.support.WebApplicationContextUtils;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet("/admin/equipment/*")
public class AdminEquipmentServlet extends HttpServlet {

    private EquipmentService equipmentService;
    private SecurityService securityService;

    @Override
    public void init() throws ServletException {
        var ctx = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
        equipmentService = ctx.getBean(EquipmentService.class);
        securityService = ctx.getBean(SecurityService.class);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!securityService.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return;
        }
        List<Equipment> equipmentList = equipmentService.getActiveEquipment();
        req.setAttribute("equipmentList", equipmentList);
        req.getRequestDispatcher("/WEB-INF/views/admin-equipment.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!securityService.isAdmin()) {
            resp.sendError(HttpServletResponse.SC_FORBIDDEN, "Admin access required.");
            return;
        }

        String path = req.getPathInfo();

        if ("/deactivate".equals(path)) {
            handleDeactivate(req, resp);
        } else {
            handleCreate(req, resp);
        }
    }

    private void handleCreate(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String name = req.getParameter("name");
        String description = req.getParameter("description");

        if (name == null || name.trim().isEmpty()) {
            req.setAttribute("error", "Equipment name is required.");
            req.setAttribute("equipmentList", equipmentService.getActiveEquipment());
            req.getRequestDispatcher("/WEB-INF/views/admin-equipment.jsp").forward(req, resp);
            return;
        }

        Equipment equipment = new Equipment();
        equipment.setName(name.trim());
        equipment.setDescription(description == null ? "" : description.trim());
        equipmentService.createEquipment(equipment);

        resp.sendRedirect("/admin/equipment");
    }

    private void handleDeactivate(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        int equipmentId = Integer.parseInt(req.getParameter("equipmentId"));
        equipmentService.deactivateEquipment(equipmentId);
        resp.sendRedirect("/admin/equipment");
    }
}
