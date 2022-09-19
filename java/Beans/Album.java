package Beans;

import java.util.Date;

public class Album {
	
	private int id;
	private String title;
	private Date date;
	private int creator;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id= id;
	}
	
	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public Date getDate() {
		return date;
	}
	
	public void setDate(Date date) {
		this.date = date;
	}
	
	public int getCreator() {
		return creator;
	}
	
	public void setCreator(int idCreator) {
		this.creator = idCreator;
	}

}
