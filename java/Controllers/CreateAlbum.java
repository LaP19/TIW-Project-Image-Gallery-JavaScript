package Controllers;

import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import java.time.LocalDate;
import java.time.ZoneId;

import org.apache.commons.lang.StringEscapeUtils;

import com.google.gson.Gson;

import Beans.Album;
import Beans.User;
import Dao.AlbumDAO;
import Dao.UserDAO;
import Utils.ConnectionHandler;

@WebServlet("/CreateAlbum")
@MultipartConfig

public class CreateAlbum extends HttpServlet {
	private static final long serialVersionUID = 1L;

	private Connection connection = null;

	public CreateAlbum() {
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
		String albumTitle = null;
		LocalDate localDate = null;
		Date date = null;
		ZoneId defaultZoneId = ZoneId.systemDefault();

		albumTitle = StringEscapeUtils.escapeJava(request.getParameter("title"));
		localDate = LocalDate.now();
        date = Date.from(localDate.atStartOfDay(defaultZoneId).toInstant());
        
		if(albumTitle == null || albumTitle.trim().equals("") || date == null || albumTitle.isEmpty()) {
			response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
			response.getWriter().println("Incorrect or missing param values");
			return;
		}

		AlbumDAO albumDAO = new AlbumDAO(connection);
		UserDAO userDAO = new UserDAO(connection);
		List<Album> allAlbums = new ArrayList<Album>();
		ArrayList<Integer> sorting = new ArrayList<>();
		ArrayList<Integer> newSorting = new ArrayList<>();
		Gson gson = new Gson();
		
		try {
			albumDAO.createAlbum(albumTitle, date, user.getId());
			allAlbums = albumDAO.findAllAlbumsOfCreator(user.getId());
			
			sorting = userDAO.getSorting(user.getId());
			
			//If the albums have already been sorted add the new one to the bottom
			if(sorting != null) {
				newSorting.addAll(sorting);
				
				for(int i = 0; i < allAlbums.size(); i++) {
					if(!newSorting.contains(allAlbums.get(i).getId())) {
						newSorting.add(allAlbums.get(i).getId());
					}
				}
			
				//Re-convert the arrayList of integer in the String to upload
				String updatedSorting = gson.toJson(newSorting);
				
				userDAO.updateSorting(user.getId(), updatedSorting);
			}
		} catch (SQLException e) {
			response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
			response.getWriter().println("Internal server error, try again later.");
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
