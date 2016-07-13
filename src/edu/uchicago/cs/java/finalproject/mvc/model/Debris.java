package edu.uchicago.cs.java.finalproject.mvc.model;

import java.awt.*;

/**
 * Created by michael on 11/20/15.
 */
public class Debris implements Movable {

    private Sprite parent;
    private int mExpiry;

    public Debris(Sprite s) {

        this.parent = s;
        mExpiry = 40;

    }

    @Override
    public void move() {

        if (mExpiry == 0){
            Cc.getInstance().getOpsList().enqueue(this, CollisionOp.Operation.REMOVE);
        } else {
            mExpiry--;
        }

    }

    @Override
    public void draw(Graphics g) {

        g.setColor(Color.RED);
        g.fillOval(parent.getCenter().x, parent.getCenter().y, mExpiry, mExpiry);
        g.setColor(Color.YELLOW);



    }

    @Override
    public Point getCenter() {
        return this.parent.getCenter();
    }

    @Override
    public int getRadius() {
        return 10;
    }

    @Override
    public Team getTeam() {
        return Team.DEBRIS;
    }
}
