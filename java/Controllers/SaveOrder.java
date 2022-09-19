package Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import Beans.User;
import Dao.UserDAO;
import Utils.ConnectionHandler;
import Utils.TransformJson;

@WebServlet("/SaveOrder")
@MultipartConfig

public class SaveOrder extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;

	public SaveOrder() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			System.out.println("Not authenticated!");
			response.sendRedirect(loginpath);
			return;
		}
		
		User user = (User) session.getAttribute("user");
		
		Gson gson = new Gson();
		
		//Gets the data from the request
		String order = request.getReader().readLine();
		
		if(order == null || order.length() < 1 || order.isEmpty() || order.trim().equals("")) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect param values");
			return;
		}
		
		ArrayList<Integer> sortedArray = TransformJson.transformJson(order);

		//Re-convert the arrayList of integer in the String to upload
		String updatedSorting = gson.toJson(sortedArray);
		
		UserDAO userDao = new UserDAO(connection);
		
		try {
			
			userDao.updateSorting(user.getId(), updatedSorting);
		}catch(SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);//Code 500
			response.getWriter().println("Internal server error, retry later");
			return;
		}	

		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");

	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}
}