package Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import Dao.ImageDAO;
import Utils.ConnectionHandler;

@WebServlet("/AddPictureToAlbum")
@MultipartConfig

public class AddPictureToAlbum extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;

	public AddPictureToAlbum() {
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
		
		ImageDAO imageDao = new ImageDAO(connection);
		Integer albumId = null;
		Integer pictureId = null;
		
		try {
			albumId = Integer.parseInt(request.getParameter("albumid"));
			pictureId = Integer.parseInt(request.getParameter("pictureid"));
		}catch(NullPointerException | NumberFormatException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}
		
		try {
			imageDao.addImageToAlbum(albumId, pictureId);
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to add picture due to an internal server error");
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
