package Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import Beans.Album;
import Beans.User;
import Dao.AlbumDAO;
import Dao.UserDAO;
import Utils.ConnectionHandler;


@WebServlet("/GetOthersAlbums")
@MultipartConfig
public class GetOthersAlbums extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetOthersAlbums() {
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
		
		User user = (User) session.getAttribute("user");
		
		AlbumDAO albumDao = new AlbumDAO(connection);
		LinkedHashMap<List<Object>, String> albums = new LinkedHashMap<List<Object>, String>();
		User owner;
		UserDAO userDao = new UserDAO(connection);
		
		try {
			
			//Fills an hashMap with a list containing all the data of the album and the owner's username
			for(Album album : albumDao.findAllAlbums()) {
				if(album.getCreator() != user.getId()) {
					owner = userDao.findUserById(album.getCreator());
					List<Object> albumData = new ArrayList<>();
					albumData.add(album.getId());
					albumData.add(album.getTitle());
					albumData.add(album.getDate());
					albums.put(albumData, owner.getUsername());
				}
			}
			
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover albums due to an internal server error");
			return;
		}

		Gson gson = new GsonBuilder()
				   .setDateFormat("yyyy MMM dd").create();
		String json = gson.toJson(albums);
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
