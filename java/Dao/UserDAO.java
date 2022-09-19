package Dao;


import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Beans.User;
import Utils.TransformJson;

public class UserDAO {
	private Connection connection;

	public UserDAO(Connection connection) {
		this.connection = connection;
	}

	public User checkCredentials(String username, String password) throws SQLException {
		String query = "SELECT  id, username, email, name, surname FROM user WHERE ((username = ? AND password =?) OR (email = ? AND password = ?))";
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, password);
			pstatement.setString(3, username);
			pstatement.setString(4, password);
			try (ResultSet result = pstatement.executeQuery();) {
				if (!result.isBeforeFirst())
					return null;
				else {
					result.next();
					User user = new User();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));
					user.setName(result.getString("name"));
					user.setSurname(result.getString("surname"));
					user.setEmail(result.getString ("email"));
					return user;
				}
			}
		}
	}
	
	public User checkUsername(String username, String email) throws SQLException{
		
		String query = "SELECT * FROM user WHERE (username = ? OR email = ?)";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, email);
			try(ResultSet result = pstatement.executeQuery();){
				if (!result.isBeforeFirst())
					return null;
				else {
					result.next();
					User user = new User();
					user.setUsername(result.getString("username"));
					user.setEmail(result.getString("email"));
					return user;
				}
			}
		}
	}
	
	public void registerNewUser(String username, String password, String name, String surname, String email) throws SQLException{
		
		String query = "INSERT INTO user (username, password, name, surname, email) VALUES (?,?,?,?, ?)";
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			pstatement.setString(1, username);
			pstatement.setString(2, password);
			pstatement.setString(3, name);
			pstatement.setString(4, surname);
			pstatement.setString(5,  email);
			pstatement.executeUpdate();
		}
		
	}
	
	public List<User> getAllUsers() throws SQLException{
		
		String query = "SELECT * FROM user";
		
		List<User> users = new ArrayList<>();
		
		try (PreparedStatement pstatement = connection.prepareStatement(query);) {
			try(ResultSet result = pstatement.executeQuery();){
				while(result.next()) {
					User user = new User();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));
					users.add(user);
				}
			}
		}
		
		return users;
		
	}
	
	public User findUserById(int id) throws SQLException{
		
		String query = "SELECT * FROM user WHERE id = ?";
		
		User user = null;
		
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1, id);
			try(ResultSet result = pstatement.executeQuery();){
				if (result.next()) {
					user = new User();
					user.setId(result.getInt("id"));
					user.setUsername(result.getString("username"));				
				}
			}
		}
		
		return user;
	}
	
	public void updateSorting(int userId, String sorting) throws SQLException {
		
		String query = "UPDATE user SET albumOrder = ? WHERE id = ?";
		
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setString(1, sorting);
			pstatement.setInt(2, userId);
			pstatement.executeUpdate();
		}
	}
	
	public ArrayList<Integer> getSorting(int userId) throws SQLException{
		
		String query = "SELECT albumOrder FROM user WHERE id = ?";
		
		ArrayList<Integer> orderedList = null;
		String jsonString = null;
		
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1, userId);
			try(ResultSet result = pstatement.executeQuery();){
				if (result.next()) {
					jsonString = result.getString("albumOrder");
				}	
				
				if(jsonString != null) {
					orderedList = TransformJson.transformJson(jsonString);	
				}
				
			}
		}
		
		return orderedList;
	}
}

