import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.ImageIcon;

	public class Map {

		private Spot[] spots = new Spot[5];
		private int Xlimit, Ylimit;
		private Wall[] walls = new Wall[25];
		private Platform[] platforms = new Platform[25];
		private Monster[] monsters = new Monster[25];
		private Image background;
		private Ladder[] ladders = new Ladder[25];
		private Rectangle[] water = new Rectangle[5];
		public Point spawnPoint = new Point(50,50), spawnCamera = new Point(0,0);

		public Map(int number) {
			switch (number) {
			case 0:
				spawnPoint = new Point(5,715-200);
				Xlimit = 1500;
				Ylimit = 910;
				walls[4] = new Wall(0,715,1500,10);
				walls[5] = new Wall(1195,615,305,295);
				walls[6] = new Wall(1363,525,137,380);
				platforms[0] = new Platform(490,530,170);
				background = newImage("/map0.jpg");
				spots[0] = new Spot(new Point(1390,320), new Point(5,335), 1, new Point(0,0));
				monsters[0] = new Monster(Main.COBRA, new Point(100,715-60));
				monsters[1] = new Monster(Main.COBRA, new Point(900,715-60));
				monsters[2] = new Monster(Main.COBRA, new Point(570,525-60));
				monsters[3] = new Monster(Main.COBRA, new Point(1230,600-60));
				break;
			case 1:
				spawnPoint = new Point(5,335);
				Xlimit = 3000;
				Ylimit = 1820;
				walls[4] = new Wall(0,Ylimit-40,Xlimit,40);
				platforms[0] = new Platform(0,550, 515);
				platforms[1] = new Platform(780,550,1330-780);
				platforms[2] = new Platform(1600,550,2190-1600);
				platforms[3] = new Platform(2450,550,Xlimit-2450);
				platforms[4] = new Platform(515,1455,775-515);
				ladders[0] = new Ladder(0,platforms[0],460,1260);
				spots[0] = new Spot(new Point(0,340), new Point(1500-125,525-200), 0, new Point(220,0));
				spots[1] = new Spot(new Point(2450+450,550-200), new Point(5,5000-240),2,new Point(0,5000-910));
				background = newImage("/map1.jpg");
				monsters[0] = new Monster(Main.COBRA, new Point(850,540-60));
				monsters[1] = new Monster(Main.COBRA, new Point(1100,540-60));
				monsters[2] = new Monster(Main.BIGCOBRA, new Point(1750,540-60));
				monsters[3] = new Monster(Main.COBRA, new Point(2000,540-60));
				monsters[4] = new Monster(Main.BIGCOBRA, new Point(2600,Ylimit-100));
				monsters[5] = new Monster(Main.BIGCOBRA, new Point(1600,Ylimit-100));
				monsters[6] = new Monster(Main.BIGCOBRA, new Point(400,Ylimit-100));
				monsters[7] = new Monster(Main.VERYBIGCOBRA, new Point(1800,Ylimit-100));
				monsters[8] = new Monster(Main.VERYBIGCOBRA, new Point(600,Ylimit-100));
				break;
			case 2:
				Xlimit = 2000;
				Ylimit = 5000;
				
				spawnPoint = new Point(5,5000-240);
				spawnCamera = new Point(0,5000-910);
				
				spots[0] = new Spot(new Point(0,5000-240), new Point(2450+430,550-200), 1, new Point(3000-1280,0));
				walls[4] = new Wall(0,Ylimit-40,Xlimit,40);
				walls[5] = new Wall(1580,4012,2000-1580,4035-4012);
				walls[6] = new Wall(103,2483,201-103,2658-2483);
				walls[7] = new Wall(215,2568,253-215,2656-2568);
				walls[8] = new Wall(400,2357,605-400,2812-2380);
				platforms[0] = new Platform(765,4835, 1052-765);
				platforms[1] = new Platform(927,4075, 1167-927);
				platforms[2] = new Platform(765,3785, 1345-765);
				platforms[3] = new Platform(1465,3700, 1660-1465);
				platforms[4] = new Platform(1782,3700, 2000-1782);
				platforms[5] = new Platform(765,3785, 1345-765);
				platforms[6] = new Platform(790,3313, 1289-777);
				platforms[7] = new Platform(277,3315, 668-277);
				platforms[8] = new Platform(256,2633, 668-277);
				platforms[9] = new Platform(220,2353, 180);
				platforms[10] = new Platform(1150,2436, 1333-1150);
				
				monsters[0] = new Monster(Main.VERYBIGCOBRA, new Point(600,Ylimit-100));
				monsters[7] = new Monster(Main.BIGCOBRA, new Point(1000,Ylimit-100));
				monsters[8] = new Monster(Main.COBRA, new Point(1200,Ylimit-100));
				monsters[9] = new Monster(Main.COBRA, new Point(1400,Ylimit-100));
				monsters[1] = new Monster(Main.COC, new Point(800,3730));
				monsters[2] = new Monster(Main.COC, new Point(800, 3250));
				monsters[3] = new Monster(Main.VERYBIGCOBRA, new Point(300, 3250));
				monsters[4] = new Monster(Main.BIGCOBRA, new Point(550, 3250));
				monsters[5] = new Monster(Main.COC, new Point(400, 2290));
				
				background = newImage("/map2.jpg");
				ladders[0] = new Ladder(987,platforms[1],100,4415-4020);
				ladders[1] = new Ladder(945,4120,125,540);
				ladders[2] = new Ladder(1200,platforms[2],1337-1220,4100-3870);
				ladders[3] = new Ladder(1825,platforms[3],0,4012-3860);
				ladders[4] = new Ladder(815,platforms[6],971-815,3773-3341);
				ladders[5] = new Ladder(502,2817,0,3185-2830);
				ladders[6] = new Ladder(337,platforms[8],0,2800-2600);
				ladders[7] = new Ladder(804,2350,1110-804,2600-2350);
				spots[1] = new Spot(new Point(Xlimit-115,4009-200), new Point(25,830-200),3, new Point(0,0));
				break;
			case 3:
				
				spawnPoint = new Point(5,Ylimit-265);
				spawnCamera = new Point(0,0);
				Xlimit = 3000;
				Ylimit = 910;
				spots[0] = new Spot(new Point(25,Ylimit-260), new Point(2000-125,4009-200), 2, new Point(2000-1280,3985-710));
				spots[1] = new Spot(new Point(2880,630), new Point(25,700), 4, new Point(0,1000-910));
				platforms[0] = new Platform(880,343,1185-880);
				platforms[1] = new Platform(1297,393,1650-1297);
				platforms[2] = new Platform(1757,335,1893-1757);
				platforms[3] = new Platform(1975,407,50);
				ladders[0] = new Ladder(941,351,1050-941,825-291);
				ladders[1] = new Ladder(2580,430,5,833-430);
				
				monsters[0] = new Monster(Main.COC, new Point(400,Ylimit-120));
				monsters[1] = new Monster(Main.COC, new Point(600,Ylimit-120));
				monsters[2] = new Monster(Main.COC, new Point(800,Ylimit-120));
				monsters[3] = new Monster(Main.COC, new Point(1000,Ylimit-120));
				monsters[4] = new Monster(Main.COC, new Point(1200,Ylimit-120));
				monsters[5] = new Monster(Main.COC, new Point(1400,Ylimit-120));
				monsters[6] = new Monster(Main.COC, new Point(1600,Ylimit-120));
				monsters[7] = new Monster(Main.COC, new Point(1800,Ylimit-120));
				
				background = newImage("/map3.jpg");
				walls[4] = new Wall(0,Ylimit-60,Xlimit,40);
				walls[5] = new Wall(2100,440,2571-2100,819-440);
				break;
			case 4:
				spawnPoint = new Point(5,Ylimit-235);
				spawnCamera = new Point(0,0);
				Xlimit = 4000;
				Ylimit = 1000;
				background = newImage("/map4.png");
				walls[4] = new Wall(0,Ylimit-30,Xlimit,40);
				spots[0] = new Spot(new Point(25,Ylimit-230), new Point(2880,630), 3, new Point(3000-1280,0));
				
			}
			limitWalls();
		}

		public Ladder[] getLadders() {
			return ladders;
		}
		
		public Rectangle[] getWater() {
			return water;
		}

		// quatre murs limites
		public void limitWalls() {
			walls[0] = new Wall(-5, 0, 10, Ylimit);
			walls[1] = new Wall(Xlimit - 5, 0, 10, Ylimit);
			walls[2] = new Wall(0, Ylimit - 5, Xlimit, 10);
			walls[3] = new Wall(0, -5, Xlimit, 10);
		}
		
		public Image getBackground(){return background;}
		
		// retourne les murs et les platformes de la map
		public Wall[] getWalls() {
			return walls;
		}

		public Platform[] getPlatforms() {
			return platforms;
		}

		// retourne un spot de téléportation
		public Rectangle getSpot(int i) {
			return spots[i].getArea();
		}

		// retourne tous les spots de téléportation
		public Spot[] getSpots() {
			return spots;
		}
		
		public Image newImage(String source) {
			Image image = new ImageIcon(getClass().getResource(source)).getImage();
			return new ImageIcon(getClass().getResource(source)).getImage();
		}

		// retourne la prochaine map
		public int getNextMap(int i) {
			return spots[i].getNextMap();
		}

		// returns the character's starting spot on the map
		public Point getStart(int i) {
			return spots[i].getSpawn();
		}

		// retourne les limites de la map
		public int getXLimit() {
			return Xlimit;
		}

		public int getYLimit() {
			return Ylimit + 50;
		}

		// retourne ou la caméra devrait être à la prochaine map
		public Point getXY(int i) {
			return spots[i].getNextXY();
		}

		// retourne les monstres de la map
		public Monster[] getMonsters() {
			return monsters;
		}

	}
