package edu.uchicago.cs.java.finalproject.mvc.model;

import java.awt.*;

public interface Movable {

	public static enum Team {
		FRIEND, FOE, FLOATER, DEBRIS
	}

	//for the game to move and draw movable objects
	public void move();
	public void draw(Graphics g);

	//for collision detection
	public Point getCenter();
	public int getRadius();
	public Team getTeam();


} //end Movable
