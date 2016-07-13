package edu.uchicago.cs.java.finalproject.mvc.model;

import edu.uchicago.cs.java.finalproject.mvc.controller.Game;

import java.awt.*;
import java.util.ArrayList;

public class NewShipFloater extends Sprite {


	private int nSpin;

	public NewShipFloater() {

		super();
		setTeam(Team.FLOATER);
		ArrayList<Point> pntCs = new ArrayList<Point>();
		// top of ship

//        pntCs.add(new Point(0,3));
 //       pntCs.add(new Point(0,4));

		pntCs.add(new Point(5, 5));
		pntCs.add(new Point(4,0));
		pntCs.add(new Point(5, -5));
		pntCs.add(new Point(0,-4));
		pntCs.add(new Point(-5, -5));
		pntCs.add(new Point(-4,0));
		pntCs.add(new Point(-5, 5));
		pntCs.add(new Point(0,4));

		assignPolarPoints(pntCs);

		setExpire(250);
		setRadius(50);
		setColor(Color.BLUE);


		int nX = Game.R.nextInt(10);
		int nY = Game.R.nextInt(10);
		int nS = Game.R.nextInt(5);
		
		//set random DeltaX
		if (nX % 2 == 0)
			setDeltaX(nX);
		else
			setDeltaX(-nX);

		//set rnadom DeltaY
		if (nY % 2 == 0)
			setDeltaX(nY);
		else
			setDeltaX(-nY);
		
		//set random spin
		if (nS % 2 == 0)
			setSpin(nS);
		else
			setSpin(-nS);

		//random point on the screen
		setCenter(new Point(Game.R.nextInt(Game.DIM.width),
				Game.R.nextInt(Game.DIM.height)));

		//random orientation 
		 setOrientation(Game.R.nextInt(360));

	}

	public void move() {
		super.move();
		setOrientation(getOrientation() + getSpin());

		//adding expire functionality
		if (getExpire() == 0)
			Cc.getInstance().getOpsList().enqueue(this, CollisionOp.Operation.REMOVE);
		else
			setExpire(getExpire() - 1);


	}

	public int getSpin() {
		return this.nSpin;
	}

	public void setSpin(int nSpin) {
		this.nSpin = nSpin;
	}




	@Override
	public void draw(Graphics g) {
		super.draw(g);
		//fill this polygon (with whatever color it has)
		g.fillPolygon(getXcoords(), getYcoords(), dDegrees.length);
		//now draw a white border
		g.setColor(Color.WHITE);
		g.drawPolygon(getXcoords(), getYcoords(), dDegrees.length);
	}

}
