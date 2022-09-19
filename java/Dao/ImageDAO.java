package Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Beans.Image;

public class ImageDAO {
	
	private Connection connection;
	
	public ImageDAO(Connection connection) {
		this.connection = connection;
	}
	
	public List<Image> findAllImagesOfAlbum(int albumId) throws SQLException{
		
		String query = "SELECT * FROM image WHERE idAlbum = ? ORDER BY date DESC";
		
		List<Image> images = new ArrayList<Image>();
		
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1, albumId);
			try(ResultSet result = pstatement.executeQuery()){
				while(result.next()) {
					Image image = new Image();
					image.setTitle(result.getString("title"));
					image.setDate(result.getDate("date"));
					image.setDescription(result.getString("description"));
					image.setPath(result.getString("path"));
					image.setId(result.getInt("id"));
					image.setOwner(result.getInt("idOwner"));
					image.setAlbum(result.getInt("idAlbum"));
					images.add(image);
				}
			}
		}
		
		return images;
	}
	
	public Image findImageById(int imageId) throws SQLException{
		String query = "SELECT * FROM image WHERE id = ?";
		
		Image image = null;
		
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1, imageId);
			try(ResultSet result = pstatement.executeQuery();){
				if (result.next()) {
					image = new Image();
					image.setId(result.getInt("id"));
					image.setPath(result.getString("path"));
					image.setDate(result.getDate("date"));
					image.setDescription(result.getString("description"));		
					image.setTitle(result.getString("title"));
					image.setOwner(result.getInt("idOwner"));
					image.setAlbum(result.getInt("idAlbum"));
				}
			}
		}
		
		return image;
	}
	
	public List<Image> findAllImagesOfUser(int userId) throws SQLException{
		
		String query = "SELECT * FROM image WHERE (idOwner = ? AND idAlbum IS NULL) ORDER BY date DESC";
		
		List<Image> images = new ArrayList<Image>();
		
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1, userId);
			try(ResultSet result = pstatement.executeQuery()){
				while(result.next()) {
					Image image = new Image();
					image.setTitle(result.getString("title"));
					image.setDate(result.getDate("date"));
					image.setDescription(result.getString("description"));
					image.setPath(result.getString("path"));
					image.setId(result.getInt("id"));
					image.setOwner(result.getInt("idOwner"));
					image.setAlbum(null);
					images.add(image);
				}
			}
		}
		
		return images;
	}

	public void addImageToAlbum(int albumId, int imageId) throws SQLException{
		String query = "UPDATE image SET idAlbum = ? WHERE id = ?";
		
		try(PreparedStatement pstatement = connection.prepareStatement(query);){

			pstatement.setInt(1, albumId);
			pstatement.setInt(2, imageId);
			pstatement.executeUpdate();
		}
		
	}
	

}
