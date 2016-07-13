package edu.uchicago.cs.java.finalproject.mvc.controller;

import edu.uchicago.cs.java.finalproject.mvc.model.*;
import edu.uchicago.cs.java.finalproject.mvc.view.GamePanel;
import edu.uchicago.cs.java.finalproject.sounds.Sound;

import javax.sound.sampled.Clip;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Random;

// ===============================================
// == This Game class is the CONTROLLER
// ===============================================

public class Game implements Runnable, KeyListener {

	// ===============================================
	// FIELDS
	// ===============================================

	public static final Dimension DIM = new Dimension(1100, 675); //the dimension of the game.
	private GamePanel gmpPanel;
	public static Random R = new Random();
	public final static int ANI_DELAY = 45; // milliseconds between screen
											// updates (animation)
	private Thread thrAnim;
	private int nLevel = 1;
	private int nTick = 0;

	private boolean bMuted = true;
	

	private final int PAUSE = 80, // p key
			QUIT = 81, // q key
			LEFT = 37, // rotate left; left arrow
			RIGHT = 39, // rotate right; right arrow
			UP = 38, // thrust; up arrow
			START = 83, // s key
			FIRE = 32, // space key
			MUTE = 77, // m-key mute
            DOWN = 40,

	// for possible future use
	// HYPER = 68, 					// d key
	SHIELD = 65, 				// a key arrow
	// NUM_ENTER = 10, 				// hyp
	 SPECIAL = 70; 					// fire special weapon;  F key

	private Clip clpThrust;
	private Clip clpMusicBackground;

	private static final int SPAWN_NEW_SHIP_FLOATER = 1200;



	// ===============================================
	// ==CONSTRUCTOR
	// ===============================================

	public Game() {

		gmpPanel = new GamePanel(DIM);
		gmpPanel.addKeyListener(this);
		clpThrust = Sound.clipForLoopFactory("whitenoise.wav");
		//clpMusicBackground = Sound.clipForLoopFactory("music-background.wav");
        clpMusicBackground = Sound.clipForLoopFactory("Beastie_Boys_-_Sabotage.wav");

	}

	// ===============================================
	// ==METHODS
	// ===============================================

	public static void main(String args[]) {
		EventQueue.invokeLater(new Runnable() { // uses the Event dispatch thread from Java 5 (refactored)
					public void run() {
						try {
							Game game = new Game(); // construct itself
							game.fireUpAnimThread();

						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				});
	}

	private void fireUpAnimThread() { // called initially
		if (thrAnim == null) {
			thrAnim = new Thread(this); // pass the thread a runnable object (this)
			thrAnim.start();
		}
	}

	// implements runnable - must have run method
	public void run() {

		// lower this thread's priority; let the "main" aka 'Event Dispatch'
		// thread do what it needs to do first
		thrAnim.setPriority(Thread.MIN_PRIORITY);

		// and get the current time
		long lStartTime = System.currentTimeMillis();

		// this thread animates the scene
		while (Thread.currentThread() == thrAnim) {
			tick();
			spawnNewShipFloater();
			gmpPanel.update(gmpPanel.getGraphics()); // update takes the graphics context we must 
														// surround the sleep() in a try/catch block
														// this simply controls delay time between 
														// the frames of the animation

			//this might be a good place to check for collisions
			checkCollisions();
			//this might be a god place to check if the level is clear (no more foes)
			//if the level is clear then spawn some big asteroids -- the number of asteroids 
			//should increase with the level. 
			checkNewLevel();

			try {
				// The total amount of time is guaranteed to be at least ANI_DELAY long.  If processing (update) 
				// between frames takes longer than ANI_DELAY, then the difference between lStartTime - 
				// System.currentTimeMillis() will be negative, then zero will be the sleep time
				lStartTime += ANI_DELAY;
				Thread.sleep(Math.max(0,
						lStartTime - System.currentTimeMillis()));
			} catch (InterruptedException e) {
				// just skip this frame -- no big deal
				continue;
			}
		} // end while
	} // end run

	private void checkCollisions() {



		Point pntFriendCenter, pntFoeCenter;
		int nFriendRadiux, nFoeRadiux;

		for (Movable movFriend : Cc.getInstance().getMovFriends()) {
			for (Movable movFoe : Cc.getInstance().getMovFoes()) {

				pntFriendCenter = movFriend.getCenter();
				pntFoeCenter = movFoe.getCenter();
				nFriendRadiux = movFriend.getRadius();
				nFoeRadiux = movFoe.getRadius();

				//detect collision
				if (pntFriendCenter.distance(pntFoeCenter) < (nFriendRadiux + nFoeRadiux)) {

					//falcon
					if ((movFriend instanceof Falcon) ){
                       //if the falcon is hit, decrease Falcons



						if (!Cc.getInstance().getFalcon().getProtected() && !(Cc.getInstance().getFalcon().getbShield())){
                           // Cc.getInstance().decFalcons();
							Cc.getInstance().getOpsList().enqueue(movFriend, CollisionOp.Operation.REMOVE);
							Cc.getInstance().spawnFalcon(false);

						}


					}
					//not the falcon
					else {
                        //if there is a collision other than w the falcon increase score
                        Cc.getInstance().incrScore();
						Cc.getInstance().getOpsList().enqueue(movFriend, CollisionOp.Operation.REMOVE);
					}//end else
					//kill the foe and if asteroid, then spawn new asteroids
					killFoe(movFoe);
					Sound.playSound("kapow.wav");

				}//end if
			}//end inner for
		}//end outer for


		//check for collisions between falcon and floaters
		if (Cc.getInstance().getFalcon() != null){
			Point pntFalCenter = Cc.getInstance().getFalcon().getCenter();
			int nFalRadiux = Cc.getInstance().getFalcon().getRadius();
			Point pntFloaterCenter;
			int nFloaterRadiux;

			for (Movable movFloater : Cc.getInstance().getMovFloaters()) {
				pntFloaterCenter = movFloater.getCenter();
				nFloaterRadiux = movFloater.getRadius();

				//detect collision
				if (pntFalCenter.distance(pntFloaterCenter) < (nFalRadiux + nFloaterRadiux)) {
                 /*
                    if(Cc.getInstance().getScore() >= 10) {
                        Cc.getInstance().incrLevel();
                    }
                    */
                    Cc.getInstance().incrFalcons();
                    Cc.getInstance().getOpsList().enqueue(movFloater, CollisionOp.Operation.REMOVE);
					Sound.playSound("pacman_eatghost.wav");

				}//end if
			}//end inner for
		}//end if not null



		//we are dequeuing the opsList and performing operations in serial to avoid mutating the movable arraylists while iterating them above
		while(!Cc.getInstance().getOpsList().isEmpty()){
			CollisionOp cop =  Cc.getInstance().getOpsList().dequeue();
			Movable mov = cop.getMovable();
			CollisionOp.Operation operation = cop.getOperation();

			switch (mov.getTeam()){
				case FOE:
					if (operation == CollisionOp.Operation.ADD){
						Cc.getInstance().getMovFoes().add(mov);
					} else {
						Cc.getInstance().getMovFoes().remove(mov);
					}

					break;
				case FRIEND:
					if (operation == CollisionOp.Operation.ADD){
						Cc.getInstance().getMovFriends().add(mov);
					} else {
						Cc.getInstance().getMovFriends().remove(mov);
					}
					break;

				case FLOATER:
					if (operation == CollisionOp.Operation.ADD){
						Cc.getInstance().getMovFloaters().add(mov);
					} else {
						Cc.getInstance().getMovFloaters().remove(mov);
					}
					break;

				case DEBRIS:
					if (operation == CollisionOp.Operation.ADD){
						Cc.getInstance().getMovDebris().add(mov);
					} else {
						Cc.getInstance().getMovDebris().remove(mov);
					}
					break;


			}

		}
		//a request to the JVM is made every frame to garbage collect, however, the JVM will choose when and how to do this
		System.gc();

	}//end method

	private void killFoe(Movable movFoe) {

        Cc.getInstance().getOpsList().enqueue(new Debris((Sprite) movFoe), CollisionOp.Operation.ADD);



        if (movFoe instanceof Asteroid){

			//we know this is an Asteroid, so we can cast without threat of ClassCastException
			Asteroid astExploded = (Asteroid)movFoe;
			//big asteroid 
			if(astExploded.getSize() == 0){
				//spawn two medium Asteroids
				Cc.getInstance().getOpsList().enqueue(new Asteroid(astExploded), CollisionOp.Operation.ADD);
				Cc.getInstance().getOpsList().enqueue(new Asteroid(astExploded), CollisionOp.Operation.ADD);

			} 
			//medium size aseroid exploded
			else if(astExploded.getSize() == 1){
				//spawn three small Asteroids
				Cc.getInstance().getOpsList().enqueue(new Asteroid(astExploded), CollisionOp.Operation.ADD);
				Cc.getInstance().getOpsList().enqueue(new Asteroid(astExploded), CollisionOp.Operation.ADD);
				Cc.getInstance().getOpsList().enqueue(new Asteroid(astExploded), CollisionOp.Operation.ADD);

			}

		} 

		//remove the original Foe
		Cc.getInstance().getOpsList().enqueue(movFoe, CollisionOp.Operation.REMOVE);

	}

	//some methods for timing events in the game,
	//such as the appearance of UFOs, floaters (power-ups), etc. 
	public void tick() {
		if (nTick == Integer.MAX_VALUE)
			nTick = 0;
		else
			nTick++;
	}

	public int getTick()  {
		return nTick;
	}

	private void spawnNewShipFloater() {
		//make the appearance of power-up dependent upon ticks and levels
		//the higher the level the more frequent the appearance
       // if(nTick % 100 ==0) {
     if (nTick % (SPAWN_NEW_SHIP_FLOATER - nLevel * 7) == 0) {

		//	Cc.getInstance().getMovFloaters().enqueue(new NewShipFloater());
			Cc.getInstance().getOpsList().enqueue(new NewShipFloater(), CollisionOp.Operation.ADD);
		}
	}

	// Called when user presses 's'
	private void startGame() {
		Cc.getInstance().clearAll();
		Cc.getInstance().initGame();
		Cc.getInstance().setLevel(0);
		Cc.getInstance().setPlaying(true);
		Cc.getInstance().setPaused(false);
		//if (!bMuted)
		   // clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
	}

	//this method spawns new asteroids
	private void spawnAsteroids(int nNum) {
		for (int nC = 0; nC < nNum; nC++) {
			//Asteroids with size of zero are big
			Cc.getInstance().getOpsList().enqueue(new Asteroid(0), CollisionOp.Operation.ADD);

		}
	}
	
	
	private boolean isLevelClear(){
		//if there are no more Asteroids on the screen
		boolean bAsteroidFree = true;
		for (Movable movFoe : Cc.getInstance().getMovFoes()) {
			if (movFoe instanceof Asteroid){
				bAsteroidFree = false;
				break;
			}
		}
		
		return bAsteroidFree;

		
	}
	
	private void checkNewLevel(){
		
		if (isLevelClear() ){
			if (Cc.getInstance().getFalcon() !=null)
				Cc.getInstance().getFalcon().setProtected(true);
			
			spawnAsteroids(Cc.getInstance().getLevel() + 2);
			Cc.getInstance().setLevel(Cc.getInstance().getLevel() + 1);

		}
	}


	

	// Varargs for stopping looping-music-clips
	private static void stopLoopingSounds(Clip... clpClips) {
		for (Clip clp : clpClips) {
			clp.stop();
		}
	}

	// ===============================================
	// KEYLISTENER METHODS
	// ===============================================

	@Override
	public void keyPressed(KeyEvent e) {

		Falcon fal = Cc.getInstance().getFalcon();
		int nKey = e.getKeyCode();

		if (nKey == START && !Cc.getInstance().isPlaying())
			startGame();

		if (fal != null) {

			switch (nKey) {
			case PAUSE:
				Cc.getInstance().setPaused(!Cc.getInstance().isPaused());
				if (Cc.getInstance().isPaused())
					stopLoopingSounds(clpMusicBackground, clpThrust);
				else
					clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
				break;
			case QUIT:
				System.exit(0);
				break;
			case UP:
				fal.thrustOn();
				if (!Cc.getInstance().isPaused())
					clpThrust.loop(Clip.LOOP_CONTINUOUSLY);
				break;
			case LEFT:
				fal.rotateLeft();
				break;
			case RIGHT:
				fal.rotateRight();
				break;

            case DOWN:
                fal.reverseThrust();
            //lets thrust backwards


			// possible future use
			// case KILL:
			// case SHIELD:
			// case NUM_ENTER:

			default:
				break;
			}
		}
	}

	@Override
	public void keyReleased(KeyEvent e) {
		Falcon fal = Cc.getInstance().getFalcon();
		int nKey = e.getKeyCode();


		if (fal != null) {
			switch (nKey) {
			case FIRE:
				Cc.getInstance().getOpsList().enqueue(new Bullet(fal), CollisionOp.Operation.ADD);
				Sound.playSound("laser.wav");
				break;
				
			//special is a special weapon, current it just fires the cruise missile. 
			case SPECIAL:
				Cc.getInstance().getOpsList().enqueue(new Cruise(fal), CollisionOp.Operation.ADD);

                //Sound.playSound("laser.wav");
				break;
				
			case LEFT:
				fal.stopRotating();
				break;
			case RIGHT:
				fal.stopRotating();
				break;
			case UP:
				fal.thrustOff();
				clpThrust.stop();
				break;

            case DOWN:
                fal.reverseOff();
                break;

			case MUTE:
				if (!bMuted){
					stopLoopingSounds(clpMusicBackground);
					bMuted = !bMuted;
				} 
				else {
					clpMusicBackground.loop(Clip.LOOP_CONTINUOUSLY);
					bMuted = !bMuted;
				}
				break;

                case SHIELD:
                    Cc.getInstance().getFalcon().setbShield(true);
				
			default:
				break;
			}
		}
	}


	@Override
	// Just need it b/c of KeyListener implementation
	public void keyTyped(KeyEvent e) {
	}

}


