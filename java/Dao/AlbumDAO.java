package Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import Beans.Album;

public class AlbumDAO {
	
	private Connection connection;
	
	public AlbumDAO(Connection connection) {
		this.connection = connection;
	}
	
	public Album findAlbumById(int id) throws SQLException{
		
		String query = "SELECT * FROM album WHERE id = ?";
		
		Album album = null;
		
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1, id);
			try(ResultSet result = pstatement.executeQuery();){
				if (result.next()) {
					album = new Album();
					album.setId(result.getInt("id"));
					album.setTitle(result.getString("title"));
					album.setDate(result.getDate("date"));
					album.setCreator(result.getInt("idCreator"));				
				}
			}
		}
		
		return album;
	}
	
	public List<Album> findAllAlbumsOfCreator(int creator) throws SQLException{
		
		List<Album> albums = new ArrayList<Album>();
		
		String query = "SELECT * FROM album WHERE idCreator = ? ORDER BY date DESC";
		
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1, creator);
			try(ResultSet result = pstatement.executeQuery();){
				while(result.next()) {
					Album album = new Album();
					album.setId(result.getInt("id"));
					album.setTitle(result.getString("title"));
					album.setDate(result.getDate("date"));
					album.setCreator(result.getInt("idCreator"));
					albums.add(album);
				}
			}
		}
		return albums;
	}
	
	public List<Album> findAllAlbums() throws SQLException{
		
		List<Album> albums = new ArrayList<Album>();
		
		String query = "SELECT * FROM album ORDER BY date DESC";
		
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			try(ResultSet result = pstatement.executeQuery();){
				while(result.next()) {
					Album album = new Album();
					album.setId(result.getInt("id"));
					album.setCreator(result.getInt("idCreator"));
					album.setTitle(result.getString("title"));
					album.setDate(result.getDate("date"));
					albums.add(album);
				}
			}
		}
		return albums;
	}
	
	public void createAlbum(String title, Date date, int creator) throws SQLException{
		String query = "INSERT INTO album (title, date, idCreator) VALUES (?, ?, ?)";
		
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			
			pstatement.setString(1, title);
			pstatement.setDate(2, new java.sql.Date(date.getTime()));
			pstatement.setInt(3, creator);
			pstatement.executeUpdate();
		}
	}
}
