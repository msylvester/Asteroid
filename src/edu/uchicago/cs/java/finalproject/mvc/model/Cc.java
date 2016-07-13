package edu.uchicago.cs.java.finalproject.mvc.model;

import edu.uchicago.cs.java.finalproject.sounds.Sound;

import java.util.ArrayList;
import java.util.List;


public class Cc {

	private  int nNumFalcon;
	private  int nLevel =1;
	private  long lScore=0;
	private  Falcon falShip;
	private  boolean bPlaying;
	private  boolean bPaused;
	
	// These ArrayLists with capacities set
	private List<Movable> movDebris = new ArrayList<Movable>(300);
    public static  List<Movable> movFriends = new ArrayList<Movable>(100);
    //private List<Movable> movFriends = new ArrayList<Movable>(100);
	private List<Movable> movFoes = new ArrayList<Movable>(200);
	private List<Movable> movFloaters = new ArrayList<Movable>(50);

	private GameOpsList opsList = new GameOpsList();

	//added by Dmitriy
	private static Cc instance = null;

	// Constructor made private - static Utility class only
	private Cc() {}


	public static Cc getInstance(){
		if (instance == null){
			instance = new Cc();
		}
		return instance;
	}


	public  void initGame(){
		setLevel(1);
		setScore(0);
		setNumFalcons(3);
		spawnFalcon(true);
	}
	
	// The parameter is true if this is for the beginning of the game, otherwise false
	// When you spawn a new falcon, you need to decrement its number
	public  void spawnFalcon(boolean bFirst) {
		if (getNumFalcons() != 0) {
			falShip = new Falcon();
			//movFriends.enqueue(falShip);
			opsList.enqueue(falShip, CollisionOp.Operation.ADD);
			if (!bFirst)
			    setNumFalcons(getNumFalcons() - 1);
		}
		
		Sound.playSound("shipspawn.wav");

	}

	public GameOpsList getOpsList() {
		return opsList;
	}

	public void setOpsList(GameOpsList opsList) {
		this.opsList = opsList;
	}

	public  void clearAll(){
		movDebris.clear();
		movFriends.clear();
		movFoes.clear();
		movFloaters.clear();
	}

	public  boolean isPlaying() {
		return bPlaying;
	}

	public  void setPlaying(boolean bPlaying) {
		this.bPlaying = bPlaying;
	}

	public  boolean isPaused() {
		return bPaused;
	}

	public  void setPaused(boolean bPaused) {
		this.bPaused = bPaused;
	}
	
	public  boolean isGameOver() {		//if the number of falcons is zero, then game over
		if (getNumFalcons() == 0) {
			return true;
		}
		return false;
	}

	public  int getLevel() {return nLevel; }

    public   long getScore() {
		return lScore;
	}

	public  void setScore(long lParam) {lScore +=lParam;}

   // public void incrLevel() {nLevel++;}

    public void resetLevel() {nLevel =1;}

    public void incrScore() {lScore++;}

    public void resetScore() {lScore =0;}


	public  void setLevel(int n) {nLevel = n;}

    public  int getNumFalcons() {return nNumFalcon;}

	public  void setNumFalcons(int nParam) {
		nNumFalcon = nParam;
	}

    public void incrFalcons() {nNumFalcon++;}

    public void decFalcons() {nNumFalcon--;}
	
	public  Falcon getFalcon(){return falShip;}
	
	public  void setFalcon(Falcon falParam){
		falShip = falParam;
	}

	public  List<Movable> getMovDebris() {
		return movDebris;
	}

    public  List<Movable> getMovFriends() {
		return movFriends;
	}

    public  List<Movable> getMovFoes() {
		return movFoes;
	}

    public  List<Movable> getMovFloaters() {
		return movFloaters;
	}


}
