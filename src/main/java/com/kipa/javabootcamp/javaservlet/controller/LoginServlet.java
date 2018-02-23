package com.kipa.javabootcamp.javaservlet.controller;

import com.kipa.javabootcamp.javaservlet.common.Breadcrumb;
import com.kipa.javabootcamp.javaservlet.common.Constanta;
import com.kipa.javabootcamp.javaservlet.common.Message;
import com.kipa.javabootcamp.javaservlet.common.Page;
import com.kipa.javabootcamp.javaservlet.dao.EmployeeDao;
import com.kipa.javabootcamp.javaservlet.model.Employee;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;

@WebServlet({"/login", "/logout", "/account"})
public class LoginServlet extends AbstractServlet {
    private EmployeeDao employeeDao;

    public void init() {
        employeeDao = new EmployeeDao();
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        try {
            String action = request.getServletPath();
            switch (action) {
                case "/login":
                    getLogin(request, response);
                    break;
                case "/account":
                    getAccount(request, response);
                    break;
                default:
                    handleNotFound(request, response);
                    break;
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException {
        try {
            String action = request.getServletPath();
            switch (action) {
                case "/login":
                    postLogin(request, response);
                    break;
                case "/logout":
                    postLogout(request, response);
                    break;
                default:
                    handleNotFound(request, response);
                    break;
            }
        } catch (Exception e) {
            throw new ServletException(e);
        }
    }

    private void getAccount(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        Employee employee = (Employee) request.getSession().getAttribute("user");

        request.setAttribute("breadcrumbs", new ArrayList<Breadcrumb>(){{
            add(new Breadcrumb("Home", "/", "home"));
            add(new Breadcrumb(employee.getName().concat(" ("+ employee.getCode() +")"), null, "user"));
        }});
        request.setAttribute("page", new Page(employee.getName().concat(" | ".concat(Constanta._APP_NAME))) {{setPath("employee/detail");}});
        request.setAttribute("employee", employee);
        forward(request, response);
    }

    private void getLogin(HttpServletRequest request, HttpServletResponse response)
        throws  ServletException, IOException {
        request.setAttribute("breadcrumbs", new ArrayList<Breadcrumb>(){{
            add(new Breadcrumb("Home", "/", "home"));
            add(new Breadcrumb("Login", "/login", "lock"));
        }});
        request.setAttribute("page", new Page("Login | ".concat(Constanta._APP_NAME)) {{setPath("login");}});
        forward(request, response);
    }

    private  void postLogout(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
        HttpSession session = request.getSession();
        session.invalidate();
        request.setAttribute("message", new Message(
            "You have been logged out",
            "See you soon",
            "info",
            "mini",
            "info"));
        response.sendRedirect("login");
    }

    private void postLogin(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        String username = request.getParameter("username");
        String password = request.getParameter("password");
        Employee employee = employeeDao.authenticate(username, password);
        if (employee != null) {
            request.getSession().setAttribute("user", employee);
            response.sendRedirect("/");
        } else {
            request.setAttribute("message", new Message(
                "Sorry, your credentials doesn't match or exist",
                "Please check the credentials",
                "error",
                "mini",
                "warning"));
            getLogin(request, response);
        }
    }
}
