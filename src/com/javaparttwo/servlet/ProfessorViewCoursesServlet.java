package com.javaparttwo.servlet;

import java.io.IOException;
import javax.annotation.Resource;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sql.DataSource;
import com.javaparttwo.service.AuthService;
import com.javaparttwo.service.ProfessorService;

/**
 * Handles course view requests by professors.
 */
@WebServlet({"/ProfessorViewCoursesServlet", "/instructorcourses"})
public class ProfessorViewCoursesServlet extends HttpServlet {

    /**
     * Java related serial version UID.
     */
    private static final long serialVersionUID = 1L;

    /**
     * An instance of the database connection.
     */
    @Resource(name = "jdbc/javapart3")
    private DataSource ds;

    /**
     * Handles all GET requests.
     */
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        AuthService auth = new AuthService(request.getSession());

        if (!auth.isLoggedIn()) {
            response.sendRedirect("login");
            return;
        }

        if (!auth.hasRole("instructor")) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN);
            return;
        }

        ProfessorService service = new ProfessorService(ds);

        request.setAttribute("courses", service.getCourses(auth.getUser().getUsername()));
        request.getRequestDispatcher("WEB-INF/views/professor/view-courses.jsp").forward(request,
                response);
    }
}
