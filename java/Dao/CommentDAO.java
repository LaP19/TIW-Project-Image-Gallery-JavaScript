package Dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import Beans.Comment;

public class CommentDAO {
	
	private Connection connection;
	
	public CommentDAO(Connection connection) {
		this.connection = connection;
	}
	
	public void createComment(String text, int imageId, int commentCreator) throws SQLException{
		
		String query = "INSERT INTO comment (text, idImage, idUser) VALUES (?, ?, ?)";
		
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			
			pstatement.setString(1, text);
			pstatement.setInt(2, imageId);
			pstatement.setInt(3, commentCreator);
			pstatement.executeUpdate();
		}
		
	}
	
	public List<Comment> getAllImageComments(int imageId) throws SQLException{
		
		String query = "SELECT * FROM comment WHERE idImage = ?";
		
		List<Comment> comments = new ArrayList<Comment>();
		
		try(PreparedStatement pstatement = connection.prepareStatement(query);){
			pstatement.setInt(1, imageId);
			try(ResultSet result = pstatement.executeQuery();){
				while(result.next()) {
					Comment comment = new Comment();
					comment.setCreatorId(result.getInt("idUser"));
					comment.setText(result.getString("text"));
					comment.setImageId(result.getInt("idImage"));
					comments.add(comment);
				}
			}
		}
		return comments;
	}
	

}
