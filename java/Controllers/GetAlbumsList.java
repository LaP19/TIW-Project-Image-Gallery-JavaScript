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
import com.google.gson.GsonBuilder;

import Beans.Album;
import Beans.User;
import Dao.AlbumDAO;
import Dao.UserDAO;
import Utils.ConnectionHandler;

/**
 * Servlet implementation class GetAlbumsList
 */
@WebServlet("/GetAlbumsList")
@MultipartConfig
public class GetAlbumsList extends HttpServlet {
	private static final long serialVersionUID = 1L;
	private Connection connection = null;

	public GetAlbumsList() {
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

		AlbumDAO albumDAO = new AlbumDAO(connection);
		List<Album> albums = new ArrayList<Album>();
		UserDAO userDao = new UserDAO(connection);
		ArrayList<Integer> sorting = new ArrayList<Integer>();
		Gson gson = new Gson();

		try {
			
			sorting = userDao.getSorting(user.getId());
			
			//If the order has been personalized send the order that comes up in the sorting array
			if(sorting != null){
				List<Album> containedAlbums = albumDAO.findAllAlbumsOfCreator(user.getId());
				List<Integer> ids = new ArrayList<>();
				
				//create the list of ids of the albums of the user
				for(int i = 0; i < containedAlbums.size(); i++) {
					ids.add(containedAlbums.get(i).getId());
				}
				
				//If an album has been added but it's not in the sorting put it at the bottom
				for(int i = 0; i < ids.size(); i++) {
					if(!sorting.contains(ids.get(i))) {
						sorting.add(ids.get(i));
					}
				}
				
				userDao.updateSorting(user.getId(), gson.toJson(sorting));
				
				for(int i = 0; i < sorting.size(); i++) {
					
					//if an albums has been deleted from the db I have to update the sorting and delete its id
					if(!ids.contains(sorting.get(i))) {
						sorting.remove(i);
						userDao.updateSorting(user.getId(), gson.toJson(sorting));
					}else {
						//if the album is still contained in the db add it to the list
						albums.add(albumDAO.findAlbumById(sorting.get(i)));
					}
				}
			}
			
			//If the order hasn't been personalized yet send the default order
			else {
				albums = albumDAO.findAllAlbumsOfCreator(user.getId());
			}


		} catch (SQLException e) {
			e.printStackTrace();
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Not possible to recover albums");
			return;
		}

		// Redirect to the Home page and add albums to the parameters
		
		Gson gsonBuilder = new GsonBuilder()
				   .setDateFormat("yyyy MMM dd").create();
		String json = gsonBuilder.toJson(albums);
		
		
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
