package Beans;

import java.util.Date;

public class Image {
	
	private int id;
	private String title;
	private String description;
	private Date date;
	private String path;
	private Integer album;
	private int owner;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public String getPath() {
		return path;
	}
	
	public void setPath(String filePath) {
		this.path = filePath;
	}
	
	public Integer getAlbum() {
		return album;
	}
	
	public void setAlbum(Integer id) {
		this.album = id;
	}
	
	public int getOwner() {
		return owner;
	}
	
	public void setOwner(int owner) {
		this.owner = owner;
	}
}
