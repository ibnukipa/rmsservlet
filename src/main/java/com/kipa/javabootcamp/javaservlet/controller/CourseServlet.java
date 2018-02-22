package com.kipa.javabootcamp.javaservlet.controller;

import com.kipa.javabootcamp.javaservlet.common.Breadcrumb;
import com.kipa.javabootcamp.javaservlet.common.Constanta;
import com.kipa.javabootcamp.javaservlet.common.Message;
import com.kipa.javabootcamp.javaservlet.common.Page;
import com.kipa.javabootcamp.javaservlet.dao.CourseDao;
import com.kipa.javabootcamp.javaservlet.dao.EmployeeDao;
import com.kipa.javabootcamp.javaservlet.model.Course;
import com.kipa.javabootcamp.javaservlet.model.Employee;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@WebServlet({"/course", "/course/create", "/course/update", "/course/delete"})
public class CourseServlet extends AbstractServlet {
    private CourseDao courseDao = new CourseDao();
    private EmployeeDao employeeDao = new EmployeeDao();

    private Course convertRequestToCourse(HttpServletRequest request) throws Exception {
        Course course = new Course();
        course.setCode(request.getParameter("course_code"));
        course.setType(request.getParameter("course_type"));
        course.setName(request.getParameter("course_name"));
        course.setDescription(request.getParameter("course_description"));
        course.setStartDate(new SimpleDateFormat("MMMMM dd, yyyy h:mm").parse(request.getParameter("course_startDate")));
        course.setEndDate(new SimpleDateFormat("MMMMM dd, yyyy h:mm").parse(request.getParameter("course_endDate")));
        course.setPlace(request.getParameter("course_place"));
        return course;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        try {
            String action = request.getServletPath();
            if(action.equals("/course/create")) {
                postCreateCourse(request, response);
            } else if(action.equals("/course/update")) {
                postUpdateCourse(request, response);
            } else if(action.equals("/course/delete")) {
                postDeleteCourse(request, response);
            } else {
                handleNotFound(request, response);
            }
        } catch (Exception ex) {
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
        throws ServletException, IOException {
        try {
            String action = request.getServletPath();
            if(action.equals("/course/create")) {
                getCreateCourse(request, response);
            } else if(action.equals("/course/update")) {
                getUpdateCourse(request, response);
            } else if(action.equals("/course/delete")) {
                handleNotFound(request, response);
            } else {
                if(request.getParameter("id") != null && Integer.parseInt(request.getParameter("id")) > 0) {
                    getCourse(request, response);
                } else {
                    getCourses(request, response);
                }
            }
        } catch (SQLException ex) {
            throw new ServletException(ex);
        }
    }

    private void getCourses(HttpServletRequest request, HttpServletResponse response)
        throws SQLException, IOException, ServletException {
        List<Course> courses = courseDao.getCourses();

        request.setAttribute("breadcrumbs", new ArrayList<Breadcrumb>(){{
            add(new Breadcrumb("Home", "/", "home"));
            add(new Breadcrumb("Courses", "/course", "bookmark"));
        }});
        request.setAttribute("page", new Page("Courses | ".concat(Constanta._APP_NAME)) {{setPath("course");}});
        request.setAttribute("courses", courses);
        forward(request, response);
    }

    private void getCourse(HttpServletRequest request, HttpServletResponse response)
        throws SQLException, IOException, ServletException {
        Integer courseId = Integer.parseInt(request.getParameter("id"));
        final Course course = courseDao.getCourseById(courseId);
        if(course == null) {
            handleNotFound(request, response);
        } else {
            request.setAttribute("breadcrumbs", new ArrayList<Breadcrumb>(){{
                add(new Breadcrumb("Home", "/", "home"));
                add(new Breadcrumb("Courses", "/course", "bookmark"));
                add(new Breadcrumb(course.getName(), null, "star"));
            }});
            request.setAttribute("page", new Page(course.getName().concat(" | ".concat(Constanta._APP_NAME))) {{setPath("course/detail");}});
            request.setAttribute("course", course);
            forward(request, response);
        }
    }

    private void getCreateCourse(HttpServletRequest request, HttpServletResponse response)
        throws IOException, ServletException {
        request.setAttribute("breadcrumbs", new ArrayList<Breadcrumb>(){{
            add(new Breadcrumb("Home", "/", "home"));
            add(new Breadcrumb("Course", "/course", "bookmark"));
            add(new Breadcrumb("Create", null, "bookmark"));
        }});
        request.setAttribute("page", new Page("Create Course | " + Constanta._APP_NAME) {{setPath("course/form");}});
        request.setAttribute("employees", employeeDao.getEmployees());
        forward(request, response);
    }

    private void postCreateCourse(HttpServletRequest request, HttpServletResponse response)
        throws IOException, Exception {
        Course course = this.convertRequestToCourse(request);
        Employee courseBy = employeeDao.getEmployeeById(Integer.parseInt(request.getParameter("course_by")));
        course.setCourseBy(courseBy);
        courseDao.create(course);
        request.setAttribute("message", new Message(
            "Course "+ course.getName() +" has been CREATED",
            "#"+course.getCode()+" - "+course.getName(),
            "success",
            "mini",
            "checkmark"));
        response.sendRedirect("/course");
    }

    private void getUpdateCourse(HttpServletRequest request, HttpServletResponse response)
        throws SQLException, IOException, ServletException {
        Integer courseId = Integer.parseInt(request.getParameter("id"));
        final Course course = courseDao.getCourseById(courseId);

        if(course == null) {
            handleNotFound(request, response);
        } else {
            request.setAttribute("breadcrumbs", new ArrayList<Breadcrumb>(){{
                add(new Breadcrumb("Home", "/", "home"));
                add(new Breadcrumb("Course", "/course", "bookmark"));
                add(new Breadcrumb(course.getName().concat(" ("+ course.getCode() +")"), null, "bookmark"));
            }});
            request.setAttribute("page", new Page(course.getName()+ " | " + Constanta._APP_NAME) {{setPath("course/form");}});
            request.setAttribute("course", course);
            request.setAttribute("employees", employeeDao.getEmployees());
            forward(request, response);
        }
    }

    private void postUpdateCourse(HttpServletRequest request, HttpServletResponse response)
        throws SQLException, IOException, ServletException, Exception {
        Course course = this.convertRequestToCourse(request);
        course.setId(Integer.parseInt(request.getParameter("course_id")));
        Employee courseBy = employeeDao.getEmployeeById(Integer.parseInt(request.getParameter("course_by")));
        course.setCourseBy(courseBy);
        courseDao.update(course);

        request.setAttribute("message", new Message(
                "Course "+ course.getName() +" has been UPDATED",
                "#"+course.getCode()+" - "+course.getName(),
                "success",
                "mini",
                "checkmark"));
        getCourses(request, response);
    }

    private void postDeleteCourse(HttpServletRequest request, HttpServletResponse response)
        throws SQLException, IOException, ServletException {
        Integer courseId = Integer.parseInt(request.getParameter("id"));
        Course course = courseDao.getCourseById(courseId);

        if (course == null) {
            handleNotFound(request, response);
        } else {
            courseDao.delete(course);
            request.setAttribute("message", new Message(
                    "Course "+ course.getName() +" has been DELETED",
                    "#"+course.getCode()+" - "+course.getName(),
                    "success",
                    "mini",
                    "checkmark"));
            getCourses(request, response);
        }
    }
}
