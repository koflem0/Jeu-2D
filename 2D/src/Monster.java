import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.util.Random;

import javax.sound.sampled.Clip;
import javax.swing.ImageIcon;


public class Monster extends Sprite {

		public static final int DMG = 0, SPD = 1, DEF = 2; 
		
		long cantMoveTime;
		private int atk, def, mastery;

		int life;

		private int maxLife;

		private int exp;

		private int lvl;

		private int dropchance = 13;

		private int rarechance = 8;

		private int dropamount = 1;

		int avoid;
		private float spd, allStatsMultiplier = 1;
		private float[] statMultipliers = {1,1,1,1,1,1,1,1,1,1};
		private boolean facingLeft = true;

		boolean canMove = true;

		private boolean alive = false;

		boolean isAggro = false;

		boolean elite = false;
		private Image[] monstreD = new Image[8],monstreG = new Image[5];
		private Image monstreHitD, monstreHitL;
		private Animation hitLeft, hitRight, left, right;
		Clip hitSound;

		private Clip dieSound;
		private Point spawnPoint;
		private long timer, deathTimer = 200, regen = 0;

		long aggroTimer = 5000;
		public String name, eliteType = "";

		public Monster(int i, Point spawn) {
			getAnimations(i);
			switch (i) {
			case Main.COBRA:
				atk = 12;
				def = 2;
				mastery = 50;
				spd = -0.240f;
				maxLife = 13;
				timer = 12000;
				exp = 4;
				lvl = 1;
				avoid = 7;
				name = "Cobra";
				break;
			case Main.BIGCOBRA:
				atk = 22;
				def = 5;
				mastery = 65;
				spd = -0.35f;
				maxLife = 25;
				timer = 30000;
				exp = 9;
				lvl = 3;
				dropchance = 20;
				dropamount = 1;
				avoid = 12;
				name = "Big Cobra";
				break;
			case Main.VERYBIGCOBRA:
				atk = 35;
				def = 8;
				mastery = 50;
				spd = -0.40f;
				maxLife = 41;
				timer = 30000;
				exp = 14;
				lvl = 4;
				dropamount = 1;
				avoid = 20;
				name ="VBig Cobra";
				break;
			case Main.COC:
				atk = 28;
				def = 15;
				mastery = 70;
				spd = -0.37f;
				maxLife = 62;
				timer = 24000;
				exp = 21;
				lvl = 6;
				dropchance = 24;
				dropamount = 1;
				rarechance = 14;
				avoid = 12;
				name ="Beetle";
				break;
			}
			this.spawnPoint = spawn;
		}

		// initialise le monstre
		public void init() {
			randomElite();
			life = getMaxLife();
			alive = true;
			setXVelocity(spd);
			setX((float) spawnPoint.getX());
			setY((float) spawnPoint.getY());
		}

		public void randomElite(){
			Random rand = new Random();
			if(1 > rand.nextInt(10)){
				elite = true;
				allStatsMultiplier = 1.3f;
				
				switch(rand.nextInt(3)){
				case DEF : statMultipliers[DEF] = 1.3f; eliteType = "DEF"; break;
				case DMG : statMultipliers[DMG] = 1.3f; eliteType = "DMG"; break;
				case SPD : statMultipliers[SPD] = 1.3f; eliteType = "SPD"; break;
				}
			} else {
				elite = false;
				allStatsMultiplier = 1;
				for(int i = 0; i < 10; i++){
					statMultipliers[i] = 1;
				}
			}
		}
		
		public void jump(){
			setYVelocity(-1);
		}
		
		// change le point ou le monstre apparait
		public void setSpawn(Point Spawn) {
			spawnPoint = Spawn;
		}
		
		public int getExp() {
			return (int)(exp*allStatsMultiplier);
		}
		
		// retourne la vie du monstre
		public int getLife() {
			return life;
		}

		public int getMaxLife() {
			return (int)(maxLife * allStatsMultiplier * statMultipliers[DEF]);
		}

		public int getLevel() {
			if(elite) return lvl + 2;
			return lvl;
		}

		// change les coordonées du monstre
		public void setX(float x) {
				super.setX(x);
		}

		public void setY(float y) {
				super.setY(y);
		}

		// modifie la position et la vitesse du monstre, le fais réapparaitre
		public void update(long timePassed) {
			if (alive) {
				regen+= timePassed;
				if(regen >= 40000/maxLife+200){
					if(life < maxLife)
					life++;
					regen=0;
				}
				if (getXVelocity() == getSpeed() || getXVelocity() == -getSpeed())
					if ((isFacingLeft() && getXVelocity() > 0)
							|| (!(isFacingLeft()) && getXVelocity() < 0))
						setXVelocity(-getXVelocity());
				super.update(timePassed);
				if (cantMoveTime >= 0) {
					cantMoveTime -= timePassed;
				}
				if (cantMoveTime <= 0)
					canMove = true;
				
				if(isAggro){
				if(aggroTimer>=0){
					aggroTimer-=timePassed;
				}
				if(aggroTimer<=0) isAggro = false;
				}
				
			} else {
				deathTimer -= timePassed;
				if (deathTimer <= 0) {
					init();
				}
			}

		}

		// retourne si le monstre est en vie
		public boolean isAlive() {
			return alive;
		}

		// retourne si le monstre peut bouger
		public boolean canMove() {
			return canMove;
		}


		// fais mourir le monstre
		void die() {
			if(dieSound != null) dieSound.start();
			alive = false; isAggro = false;
			deathTimer = timer;
		}
		
		int getdropamount(){
			if(elite) return dropamount+1;
			return dropamount;
		}
		
		int getdropchance(){
			return (int)(dropchance*allStatsMultiplier);
		}
		
		int getrarechance(){
			return (int)(rarechance*allStatsMultiplier);
		}

		// load les animations du monstre
		private void getAnimations(int i) {
			hitLeft = new Animation();
			hitRight = new Animation();
			right = new Animation();
			left = new Animation();
			loadpics(i);
			switch (i) {
			case Main.COBRA:
			case Main.BIGCOBRA:
			case Main.VERYBIGCOBRA:
				right.addScene(monstreD[1], 220);
				right.addScene(monstreD[2], 220);
				left.addScene(monstreG[1], 220);
				left.addScene(monstreG[2], 220);
				hitLeft.addScene(monstreG[1], 200);
				hitRight.addScene(monstreD[1],200);
				break;
			case Main.COC:
				right.addScene(monstreD[1],110);
				right.addScene(monstreD[2],110);
				right.addScene(monstreD[3],110);
				right.addScene(monstreD[4],110);
				left.addScene(monstreG[1],110);
				left.addScene(monstreG[2],110);
				left.addScene(monstreG[3],110);
				left.addScene(monstreG[4],110);
				hitLeft.addScene(monstreG[1], 200);
				hitRight.addScene(monstreD[1],200);
				break;
			}
		}

		// retourne l'espace ou la prochaine platforme devrait être
		public Rectangle getNextFloor() {
			if (facingLeft)
				return new Rectangle(getX() - 25, getY() + getHeight() - 5, 20,
						15);
			return new Rectangle(getX() + getWidth() + 5, getY() + getHeight()
					- 5, 20, 15);
		}

		// retourne le coté du monstre
		public Rectangle getSide() {
			if (getXVelocity() < 0)
				return new Rectangle(getX() - 10, getY() + 10, 20,
						getHeight() - 25);
			return new Rectangle(getX() + getWidth() - 10, getY() + 10, 20,
					getHeight() - 25);
		}

		// load les images du monstre
		public void loadpics(int i) {
			switch (i) {
			case Main.COBRA:
				monstreD[1] = newImage("/cobra1D.png");
				monstreD[2] = newImage("/cobra2D.png");
				monstreG[1] = newImage("/cobra1G.png");
				monstreG[2] = newImage("/cobra2G.png");
				break;
			case Main.BIGCOBRA:
				monstreD[1] = newImage("/bigcobra1D.png");
				monstreD[2] = newImage("/bigcobra2D.png");
				monstreG[1] = newImage("/bigcobra1G.png");
				monstreG[2] = newImage("/bigcobra2G.png");
				break;
			case Main.VERYBIGCOBRA:
				monstreD[1] = newImage("/verybigcobra1D.png");
				monstreD[2] = newImage("/verybigcobra2D.png");
				monstreG[1] = newImage("/verybigcobra1G.png");
				monstreG[2] = newImage("/verybigcobra2G.png");
				break;
			case Main.COC:
				monstreD[1] = newImage("/coc1D.png");
				monstreD[2] = newImage("/coc2D.png");
				monstreD[3] = newImage("/coc3D.png");
				monstreD[4] = newImage("/coc4D.png");
				monstreG[1] = newImage("/coc1G.png");
				monstreG[2] = newImage("/coc2G.png");
				monstreG[3] = newImage("/coc3G.png");
				monstreG[4] = newImage("/coc4G.png");
				break;
			}
		}

		// retourne l'animation du monstre
		public Animation getAnimation(boolean left) {
			if (left){
				if(!canMove) return hitLeft;
				return this.left;
			}
			else{
				if(!canMove) return hitRight;
				return right;
			}
		}
		
		public Image newImage(String source) {
			return new ImageIcon(getClass().getResource(source)).getImage();
		}

		// retourne si le monstre "regarde" a gauche
		public boolean isFacingLeft() {
			return facingLeft;
		}

		public void setFacingLeft(boolean facingLeft) {
			this.facingLeft = facingLeft;
		}

		// retourne toute la surface du monstre
		public Rectangle getArea() {
			return new Rectangle(getX(), getY(), getWidth(), getHeight());
		}

		// retourne la défence du monstre
		public int getDefense() {
			return (int)(def * statMultipliers[DEF] * allStatsMultiplier);
		}

		// frappe un personnage
		public synchronized int hit(Main.Character c) {
			int dmg = getDamage(c);

			if (!c.isInvincible()) {
				c.damageChar(dmg);
				if (dmg >= (c.getMaxLife() * 5 / 100)) {
					float vx = 0.3f;
					if ((getX() + getWidth() / 2) > (c.getX() + c.getWidth() / 2))
						vx = -vx;
					c.setXVelocity(vx);
					c.setYVelocity(-0.5f);
					c.canMove(false);
				}
				c.setInvincible(1000);
			}
			return dmg;
		}

		// retourne les dégats si le monstre frappe un personnage
		public int getDamage(Main.Character c) {
			Random rand = new Random();
			int dmast = rand.nextInt(100 - getMastery()) + getMastery();

			int dmg = getAtk();
			dmg = dmg * dmast / 100;
			dmg = (int) (dmg * (1 - c.getDefense()
					/ (c.getDefense() + 22 * Math.pow(1.1, getLevel()))));
			if (dmg <= 0)
				dmg = 1;
			return dmg;
		}

		private int getAtk(){
			return (int)(atk * allStatsMultiplier * statMultipliers[DMG]);
		}
		
		private int getMastery(){
			int mast = (int)(mastery * allStatsMultiplier * statMultipliers[DMG]);
			if(mast >= 100) return 99;
			return mast;
		}

		// fais tomber le monstre
		public void fall(long timePassed) {
			if (getYVelocity() < 0.8f)
				setYVelocity(getYVelocity() + 0.005f * timePassed);
		}

		// retourne la vitesse de base du monstre
		public float getSpeed() {
			return spd*allStatsMultiplier*statMultipliers[SPD];
		}

		// retourne la base du monstre
		public Rectangle getBase() {
			return new Rectangle(getX() + 10, getY() + getHeight() - 15,
					getWidth() - 20, 20);
		}

	}
