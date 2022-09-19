package Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
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
import Beans.Comment;
import Beans.Image;
import Beans.User;
import Dao.AlbumDAO;
import Dao.CommentDAO;
import Dao.ImageDAO;
import Dao.UserDAO;
import Utils.ConnectionHandler;


@WebServlet("/GetPictureDetails")
@MultipartConfig
public class GetPictureDetails extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetPictureDetails() {
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
		
		Integer pictureId = null;
		try {
			pictureId = Integer.parseInt(request.getParameter("pictureid"));
		} catch (NumberFormatException | NullPointerException e) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect param values");
			return;
		}
		
		UserDAO userDao = new UserDAO(connection);
		CommentDAO commentDao = new CommentDAO(connection);
		HashMap<String, List<String>> comments = new HashMap<>();
		AlbumDAO albumDao = new AlbumDAO(connection);
		ImageDAO imageDao = new ImageDAO(connection);
		
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
			
			List<User> users = new ArrayList<>();
			
			users = userDao.getAllUsers();
			
			
			//Gets all the comments of the users
			List<Comment> allComments = commentDao.getAllImageComments(pictureId); 
			
			for(User user1 : users) {
				List<String> commentsOfUser = new ArrayList<>();
				for(Comment comment : allComments) {
					if(comment.getCreatorId() == user1.getId()) {
						commentsOfUser.add(comment.getText());
					}
				}
				
				if(commentsOfUser.size() != 0) {
					comments.put(user1.getUsername(), commentsOfUser);
				}
			}
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover picture details due to internal server error");
			return;
		}

		// Redirect to the Home page and add missions to the parameters
		String json = new Gson().toJson(comments);
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
