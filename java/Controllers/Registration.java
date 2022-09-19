package Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;

import Beans.User;
import Dao.UserDAO;
import Utils.ConnectionHandler;

@WebServlet("/Registration")
@MultipartConfig
public class Registration extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public Registration() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
	
		
		String username = null;
		String password = null;
		String repeatedPassword = null;
		String name = null;
		String surname = null;
		String email = null;
	
		username = StringEscapeUtils.escapeJava(request.getParameter("registrationUsername"));
		password = StringEscapeUtils.escapeJava(request.getParameter("registrationPassword"));
		repeatedPassword = StringEscapeUtils.escapeJava(request.getParameter("repeatedPassword"));
		name = StringEscapeUtils.escapeJava(request.getParameter("name"));
		surname = StringEscapeUtils.escapeJava(request.getParameter("surname"));
		email = StringEscapeUtils.escapeJava(request.getParameter("email"));
		
		if(username == null || username.isEmpty() || username.trim().equals("") || password == null || password.isEmpty() || password.trim().equals("")|| 
			name == null || name.isEmpty() || name.trim().equals("") || surname == null || surname.isEmpty() || surname.trim().equals("") ||
			repeatedPassword == null || repeatedPassword.isEmpty() || repeatedPassword.trim().equals("") || email == null || email.isEmpty() ||
			email.trim().equals("")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Missing parameters");
			return;
		}
		
		UserDAO userDao = new UserDAO(connection);
		User checkUser = null;
		
		try {
			checkUser = userDao.checkUsername(username, email);
		}catch(SQLException ex) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal server error, retry later");
			return;
		}
		
		Pattern p = Pattern.compile("^[a-zA-Z0-9_+&*-]+(?:\\."+
                "[a-zA-Z0-9_+&*-]+)*@" +
                "(?:[a-zA-Z0-9-]+\\.)+[a-z" +
                "A-Z]{2,3}$");
		Matcher m = p.matcher(email);
		boolean matchFound = m.matches();
		
		if(checkUser == null && password.equals(repeatedPassword) && matchFound) {
			try {
				userDao.registerNewUser(username, password, name, surname, email);
			}catch(SQLException e) {
				response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
				response.getWriter().println("Not possible to register new user");
				return;
			}
		}
		else {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Wrong input data");
			return;
		}
		
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
	}
	
	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		}catch(SQLException ex) {
			ex.printStackTrace();
		}
	}


}