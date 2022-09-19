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

import org.apache.commons.lang.StringEscapeUtils;

import Beans.Album;
import Beans.Image;
import Beans.User;
import Dao.AlbumDAO;
import Dao.CommentDAO;
import Dao.ImageDAO;
import Utils.ConnectionHandler;
@WebServlet("/CreateComment")
@MultipartConfig

public class CreateComment extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;

	public CreateComment() {
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
		Integer pictureId = null;
		String commentText = null;
		
		try {
			pictureId = Integer.parseInt(request.getParameter("pictureid"));
			commentText = StringEscapeUtils.escapeJava(request.getParameter("comment"));
			
			if(commentText == null || commentText.trim().equals("") || commentText.isEmpty()) {
				response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
				response.getWriter().println("Incorrect or missing param values");
				return;
			}
		}catch(NullPointerException | NumberFormatException e){
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}
		
		ImageDAO imageDao = new ImageDAO(connection);
		AlbumDAO albumDao = new AlbumDAO(connection);
		
		try {
			
			List<Image> images = new ArrayList<>();
			
			for(Album album : albumDao.findAllAlbums()) {
				images.addAll(imageDao.findAllImagesOfAlbum(album.getId()));
			}
			List<Integer> ids = new ArrayList<>();
			
			for(Image image1: images) {
				ids.add(image1.getId());
			}
			
			if(!ids.contains(pictureId)) {
				response.sendError(HttpServletResponse.SC_BAD_REQUEST, "This picture doesn't exist");
				return;
			}
		}catch(SQLException e){
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to create comment due to a server error");
			return;
		}
		

		CommentDAO commentDao = new CommentDAO(connection);
		
		try {
			commentDao.createComment(commentText, pictureId, user.getId());
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to create comment due to a server error");
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
