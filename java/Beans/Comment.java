package Beans;

public class Comment {
	
	private int id;
	private String text;
	private int idUser;
	private int idImage;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}
	
	public int getCreatorId() {
		return idUser;
	}
	
	public void setCreatorId(int id) {
		this.idUser = id;
	}
	
	public int getImageId() {
		return idImage;
	}
	
	public void setImageId(int id) {
		this.idImage = id;
	}
	
	

}
