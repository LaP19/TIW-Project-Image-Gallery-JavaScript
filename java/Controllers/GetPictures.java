package Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;

import Beans.Album;
import Beans.Image;
import Dao.AlbumDAO;
import Dao.ImageDAO;
import Utils.ConnectionHandler;


@WebServlet("/GetPictures")
@MultipartConfig
public class GetPictures extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetPictures() {
		super();
	}

	public void init() throws ServletException {
		connection = ConnectionHandler.getConnection(getServletContext());
	}

	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		
		String loginpath = getServletContext().getContextPath() + "/index.html";
		HttpSession session = request.getSession();
		if (session.isNew() || session.getAttribute("user") == null) {
			System.out.println("Not authenticated!");
			response.sendRedirect(loginpath);
			return;
		}
		
		Integer albumId = null;
		
		try {
			albumId = Integer.parseInt(request.getParameter("albumId"));
		} catch (NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect param values");
			return;
		}

		ImageDAO imageDAO = new ImageDAO(connection);
		List<Image> images = new ArrayList<>();
		AlbumDAO albumDao = new AlbumDAO(connection);
		
		try {
			List<Integer> ids = new ArrayList<>();
			
			for(Album album : albumDao.findAllAlbums()) {
				ids.add(album.getId());
			}

			if(!ids.contains(albumId)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "This album doesn't exist");
				return;
			}
			
			images = imageDAO.findAllImagesOfAlbum(albumId);
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover pictures due to an internal server error");
			return;
		}

		// Redirect to the Home page and add missions to the parameters
		String json = new Gson().toJson(images);
		response.setContentType("application/json");
		response.setCharacterEncoding("UTF-8");
		response.getWriter().write(json);
		
	}

	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			throws ServletException, IOException {
		doGet(request, response);
	}

	public void destroy() {
		try {
			ConnectionHandler.closeConnection(connection);
		} catch (SQLException e) {
			e.printStackTrace();
		}
	}

}

