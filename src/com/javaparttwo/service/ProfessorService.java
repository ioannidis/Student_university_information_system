package com.javaparttwo.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.sql.DataSource;

import com.javaparttwo.model.Course;
import com.javaparttwo.model.CourseStudentGrades;
import com.javaparttwo.model.GradedStudent;
import com.javaparttwo.model.User;

/**
 * Handles all professor database interactions.
 */
public class ProfessorService {

    /**
     * An instance of the database connection.
     */
    private DataSource ds;

    /**
     * Initializes professor service.
     * 
     * @param ds
     *            The data source instance.
     */
    public ProfessorService(DataSource ds) {
	this.ds = ds;
    }

    /**
     * Returns the list of registered professors.
     * 
     * @return The list of professors.
     */
    public List<User> getProfessors() {
	List<User> professors = new ArrayList<>();

	String query = "SELECT * FROM javapart2.users WHERE role_id='instructor'";
	
	// Try-with-resource statement
	// https://docs.oracle.com/javase/tutorial/essential/exceptions/tryResourceClose.html
	
	try (	Connection con 		= ds.getConnection();
		PreparedStatement stmt 	= con.prepareStatement(query);
		ResultSet rs 		= stmt.executeQuery()) {
	    
	    while (rs.next()) {
		professors.add(
			new User(rs.getString("username"),
				null,
				rs.getString("first_name"),
				rs.getString("last_name"),
				rs.getLong("phone_number"),
				rs.getString("email"),
				rs.getString("role_id"),
				rs.getString("department_id")));
	    }
	    
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	
	return professors;
    }
    
    /**
     * Returns the list of registered professors.
     * 
     * @return The list of professors.
     */
    public List<User> getProfessorsByDept(String departmentId) {
		List<User> professors = new ArrayList<>();
	
		Connection con = null;
		ResultSet rs = null;
		PreparedStatement stmt = null;
	
		String query = "SELECT * FROM javapart2.users WHERE role_id='instructor' AND department_id=?";
			
		try {
			con = ds.getConnection();
			
			stmt = con.prepareStatement(query);
			stmt.setString(1, departmentId);
			
			rs = stmt.executeQuery();
			
		    while (rs.next()) {
			professors.add(
				new User(rs.getString("username"),
					null,
					rs.getString("first_name"),
					rs.getString("last_name"),
					rs.getLong("phone_number"),
					rs.getString("email"),
					rs.getString("role_id"),
					rs.getString("department_id")));
		    }
		    
		} catch (SQLException e) {
		    e.printStackTrace();
		} finally {
		    try {
				rs.close();
				stmt.close();
				con.close();
		    } catch (SQLException e) {
		    	e.printStackTrace();
		    }
		}
		
		return professors;
    }
    
    public List<Course> getCourses(String professorUsername) {
	
	List<Course> courses = new ArrayList<>();
	String query = "select * from courses where instructor_username = ?";
	
	try (	Connection con 		= ds.getConnection();
		PreparedStatement stmt 	= con.prepareStatement(query)) {
	    
	    stmt.setString(1, professorUsername);

	    try (ResultSet rs = stmt.executeQuery()) {
		while (rs.next()) {
		    courses.add(new Course(
			rs.getString("id"),
			rs.getString("title"),
			rs.getInt("ects"),
			rs.getInt("teaching_hours"),
			rs.getString("instructor_username"),
			rs.getInt("semester"),
			rs.getString("department_id")));
		    }
	    }
	    
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	
	return courses;
    }
    
    public List<GradedStudent> getGradedStudents(String courseId) {
	List<GradedStudent> gradedStudents = new ArrayList<>();
	
	String query = "select username, first_name, last_name, phone_number, email, role_id, users.department_id, grade " + 
		"from users " + 
		"inner join grades " + 
		"on users.username = grades.student_id " + 
		"inner join courses " + 
		"on grades.course_id = courses.id " + 
		"where grades.course_id = ?";
	
	try (	Connection con 		= ds.getConnection();
		PreparedStatement stmt 	= con.prepareStatement(query)) {
	    
	    stmt.setString(1, courseId);
	    
	    try (ResultSet rs = stmt.executeQuery()) {
		while (rs.next()) {
		    gradedStudents.add(
			    new GradedStudent(
				rs.getString("username"),
				null,
				rs.getString("first_name"),
				rs.getString("last_name"),
				rs.getLong("phone_number"),
				rs.getString("email"),
				rs.getString("role_id"),
				rs.getString("department_id"),
        			rs.getInt("grade")));
		    }
	    }
	    
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	
	return gradedStudents;
    }
    
    public List<User> getPendingStudents(String courseId) {
	List<User> pendingStudents = new ArrayList<>();
	
	String query = "select * " + 
		"from users\r\n" + 
		"where username not in (select student_id from grades where course_id = ?) " + 
		"and role_id='student' " + 
		"and department_id = (select department_id from courses where id = ?)";
	
	try (	Connection con 		= ds.getConnection();
		PreparedStatement stmt 	= con.prepareStatement(query)) {
	    
	    stmt.setString(1, courseId);
	    stmt.setString(2, courseId);
	    
	    try (ResultSet rs = stmt.executeQuery()) {
		while (rs.next()) {
		    pendingStudents.add(
			    new User(
				rs.getString("username"),
				null,
				rs.getString("first_name"),
				rs.getString("last_name"),
				rs.getLong("phone_number"),
				rs.getString("email"),
				rs.getString("role_id"),
				rs.getString("department_id")));
		    }
	    }
	} catch (SQLException e) {
	    e.printStackTrace();
	}
	
	return pendingStudents;
    }
    
    public List<CourseStudentGrades> getCoursesWithStudentAndGrades(CourseService courseService, String professorUsername) {
	List<CourseStudentGrades> courses = new ArrayList<>();
	
	getCourses(professorUsername).forEach(c -> {
	    courses.add(getCourseWithStudentsAndGrades(courseService, c.getCourseId()));
	});
	
	return courses;
    }
    
    public CourseStudentGrades getCourseWithStudentsAndGrades(CourseService courseService, String courseId) {
	Course course = courseService.getCourse(courseId);
	return new CourseStudentGrades(
		course.getCourseId(),
		course.getTitle(),
		course.getEcts(),
		course.getTeachingHours(),
		course.getInstructorUsername(),
		course.getSemester(),
		course.getDepartmentId(),
		getPendingStudents(courseId),
		getGradedStudents(courseId));
    }
}
