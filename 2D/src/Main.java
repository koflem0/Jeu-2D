import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Random;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;

public class Main extends Core implements KeyListener, MouseListener,
		MouseMotionListener, Serializable {

	private static final long serialVersionUID = 1L;
	public static final int LEFT = -1, RIGHT = 1;
	public static final int POW = 0, AGI = 1, SPIRIT = 2, VIT = 3, CRIT = 4,
			CRITDMG = 5, MASTERY = 6, ALLSTATS = 7, DEFENSE = 19, WATK = 20;
	public static final int MAGE = 2, FIGHTER = 0, ARCHER = 1;
	public static final int COBRA = 0, BIGCOBRA = 1, COC = 2, VERYBIGCOBRA = 3;
	public static final int walkL = 0, walkR = 1, standL = 2, standR = 3, jumpR = 4, jumpL = 5, climb = 6, onLadder = 7, sideClimb = 8,
			wizWalkL = 9, wizWalkR = 10, wizStandL = 11, wizStandR = 12, wizJumpL = 13, wizJumpR = 14;

	public static void main(String[] args) {
		Main m = new Main();
		m.run();
	}

	public Skill[] skills = new Skill[20];
	public PassiveSkill[] passives = new PassiveSkill[10];
	private int counter = 0;
	private float X = 0, Y = 0;
	public boolean down = false, blink = false, playing = false,
			classSelect = false;
	private Character c;
	private Image bg, spotImage;
	private Image[] itemIcons = new Image[8], equipIcons = new Image[8];
	private Animation[] charAnims = new Animation[15];

	private ArrayList<Platform> platforms;
	private ArrayList<Wall> walls;
	private ArrayList<Ladder> ladders;
	private ArrayList<Spot> spots;
	private Rectangle[] water;
	private Map map;
	private ArrayList<Monster> monsters;
	
	private int currentSkill = -1;
	private int activatedSkillKey = -1;
	private int[] SkillKeys = new int[256];
	private Clip clip = null;
	private StatMenu statMenu = new StatMenu();
	private int gameSlot;
	private SaveMenu mainMenu = new SaveMenu();
	private ClassMenu classMenu = new ClassMenu();
	private SkillMenu skillMenu = new SkillMenu();
	private long respawnTimer = 5000;

	private Tooltip tooltip = null;
	private Tooltip equippedTooltip = null;

	private Stash stash;
	
	private boolean isDragging = false;
	private Item draggedItem;
	private int previousItemI = -1, previousItemJ = -1, previousItemP = -1;
	private Point draggedItemLocation;
	
	private ArrayList<FlyingText> damage = new ArrayList<FlyingText>();
	private ArrayList<Projectile> projectiles = new ArrayList<Projectile>();
	private ArrayList<Effect> effects = new ArrayList<Effect>();
	private ArrayList<Drop> drops = new ArrayList<Drop>();

	// initialise la fen�tre
	public void init() {
		super.init();
		Window w = S.getFSWindow();
		w.setFocusTraversalKeysEnabled(false);
		w.addKeyListener(this);
		w.addMouseListener(this);
		w.addMouseMotionListener(this);
	}

	public void mainMenu() {
		clip.stop();
		clip.setFramePosition(0);
		for (SaveButton saveButton : mainMenu.saveButtons) {
			saveButton.refresh();
		}
		playing = false;
	}

	public void start() {
		loadpics();
		loadmusic();
	}

	// arr�te le jeu et la musique
	public void stop() {
		super.stop();
		if (clip != null)
			clip.stop();
	}

	public void save() {

		if (c.stats.inventory.isOpen())
			c.stats.inventory.toggle();
		
		if(!c.isAlive()) c.respawn();
		
		try {
			File rootDir = new File("C:/");
			if(isMac()) rootDir = new File(System.getProperty("user.home")+"/Documents");
			
			File file = new File(rootDir, "jeu");
			if(!file.exists())
			file.mkdirs();
			FileOutputStream saveFile = new FileOutputStream(new File(file, "/Save"
					+ gameSlot + ".sav"));
			ObjectOutputStream save = new ObjectOutputStream(saveFile);
			save.writeObject(c.stats);
			save.close();
			
			FileOutputStream stashFile = new FileOutputStream(new File(file, "/SaveStash.sav"));
			ObjectOutputStream stashSave = new ObjectOutputStream(stashFile);
			stashSave.writeObject(stash);
			stashSave.close();
			
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public static boolean isMac() {
		 
		String os = System.getProperty("os.name").toLowerCase();
		return (!(os.indexOf("win") >= 0));
 
	}

	public Stash loadStash(){
		Stash loadedStash = null;
		try {
			File rootDir = new File("C:/");
			if(isMac()) rootDir = new File(System.getProperty("user.home")+"/Documents");
			
			File file = new File(rootDir, "Jeu");
			FileInputStream saveFile = new FileInputStream(new File(file, "/SaveStash.sav"));
			ObjectInputStream load = new ObjectInputStream(saveFile);
			loadedStash = (Stash) load.readObject();
			load.close();
		} catch (Exception e) {
			loadedStash = null;
		}
		
		return loadedStash;
	}
	
	public void load(int i) {
		start();
		CharacterStats loadedStats = loadStats(i);
		
		stash = loadStash();
		if(stash == null) stash = new Stash();
		
		if (loadedStats != null) {

			initChar(loadedStats.classe);
			c.stats = loadedStats;
			c.loadStats();
			c.stats.inventory.setCharacter(c);
			loadMap(c.stats.currentMap);
			
			c.setX(map.spawnPoint.x);
			c.setY(map.spawnPoint.y);
			X = map.spawnCamera.x;
			Y = map.spawnCamera.y;
			playing = true;
		} else
			classSelect = true;

		gameSlot = i;
	}

	public CharacterStats loadStats(int i) {
		CharacterStats stats = null;
		try {
			File rootDir = new File("C:/");
			if(isMac()) rootDir = new File(System.getProperty("user.home")+"/Documents");
			
			File file = new File(rootDir, "Jeu");
			FileInputStream saveFile = new FileInputStream(new File(file, "/Save"
					+ i + ".sav"));
			ObjectInputStream load = new ObjectInputStream(saveFile);
			stats = (CharacterStats) load.readObject();
			load.close();
		} catch (Exception e) {
			stats = null;
		}
		return stats;
	}
	
	// endommage un monstre et le fait reculer
			public void damageMonster(Monster m, int dmg, float speed) {
				
				boolean crit = false;
				Random rand = new Random();
				if ((rand.nextInt(100) + 1) < c.getCritChance()) {
					dmg = dmg * (c.getCritDamage() + 100) / 100;
					crit = true;
				}

				if (dmg > 0) {
					m.life -= dmg;
					m.isAggro = true; m.aggroTimer = 5000;
				if (m.life <= 0){
					m.die();
					if(m.getLevel() >= c.stats.lvl-5)
						c.exp(m.getExp());
					for (int i = 0; i < m.getdropamount(); i++)
						drop(m);
				} else if(m.hitSound != null) m.hitSound.start();
				}


				boolean created = false;
				for (int i = 0; !created; i++) {
					if(damage.size() <= i){
						damage.add(new FlyingText(dmg, m, crit));
						created = true;
					} else if (!damage.get(i).isActive()) {
							damage.set(i,new FlyingText(dmg, m, crit));
							created = true;
						}
				}
				
				if(dmg > m.getMaxLife() / 50 && m.canMove){
				m.canMove = false;
				m.cantMoveTime = (long) (105 + speed * 1100);
				m.setYVelocity(-(speed * 3));
				if ((c.getX() + c.getWidth() / 2) > (m.getX() + m.getWidth() / 2))
					speed = -speed;
				m.setXVelocity(speed);
				}
			}
			
		private void drop(Monster m) {

			Random rand = new Random();
			int rarity = rand.nextInt(100);

			if ((rand.nextInt(100)) < m.getdropchance()) {

				if (rarity < m.getrarechance())
					rarity = Item.RARE;
				else if (rarity < (int)(m.getrarechance() * 4.5))
					rarity = Item.MAGIC;
				else
					rarity = Item.COMMON;

				int itemChoices = 8;
				if (rarity == Item.COMMON)
						itemChoices = 6;
					
				int dropLvl = rand.nextInt(10);
				if(dropLvl <= 3) dropLvl = m.getLevel(); else if(dropLvl <= 5) dropLvl = m.getLevel()+1; else dropLvl = m.getLevel()-1;
					
				if(dropLvl < 1)dropLvl = 1;
					
				add(new Drop(new Item(dropLvl, rand.nextInt(itemChoices), rarity),
						new Point(m.getX() + rand.nextInt(m.getWidth() >= 50 ? m.getWidth() - 50 : 1),
								m.getY() + m.getHeight() - 50)));
			}
		}
	
	
	public boolean isFacingChar(Monster m){
		if(c.getY()+c.getHeight() > m.getY()){
			if((m.getXVelocity() > 0 && c.getX() > m.getX()) || (m.getXVelocity() < 0 && c.getX() < m.getX())) return true;
		}
		return false;
	}

	// retourne les limites de la map actuelle
	public int getMapXLimit() {
		if (map != null)
			return map.getXLimit();
		else
			return -1;
	}

	public int getMapYLimit() {
		if (map != null)
			return map.getYLimit();
		else
			return -1;
	}

	// retourne les monstres sur la map actuelle
	public ArrayList<Monster> getMonsters() {
		return monsters;
	}

	// load la musique
	public void loadmusic() {
		try {
			AudioInputStream music = AudioSystem.getAudioInputStream(getClass()
					.getResource("sax.wav"));
			clip = AudioSystem.getClip();
			clip.open(music);

			FloatControl gainControl = (FloatControl) clip
					.getControl(FloatControl.Type.MASTER_GAIN);
			gainControl.setValue(-11.0f);
			clip.setLoopPoints(0, -1);
			clip.loop(Clip.LOOP_CONTINUOUSLY);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	// modifie les sorts que le personnage peut utiliser
	public void setSkills(Skill[] skills) {
		this.skills = skills;
	}

	public Monster testProjectile(Projectile projectile) {
		for (Monster monster : monsters) {
			if (monster != null)
				if (monster.isAlive()) {
					if (projectile.getArea().intersects(monster.getArea()))
						return monster;
				}
		}
		return null;
	}

	// load les images, le background et les animations du personnage
	public void loadpics() {

		bg = newImage("/forest1.png");
		spotImage = newImage("/spot.png");
		Image standingR = newImage("/walkright1.png"),wizStandingR = newImage("/bobwalkwizzardR1.png"),
		jumpingR = newImage("/walkright2.png"), wizJumpingR = newImage("/bobwalkwizzardR2.png"),
		standingL = newImage("/walkleft1.png"), wizStandingL = newImage("/bobwalkwizzardL1.png"),
		jumpingL = newImage("/walkleft2.png"), wizJumpingL = newImage("/bobwalkwizzardL2.png"),
		standingladder = newImage("/bobclimb1.png");
		
		charAnims[walkR] = new Animation(); charAnims[wizWalkR] = new Animation();
		charAnims[walkL] = new Animation(); charAnims[wizWalkL] = new Animation();
		charAnims[climb] = new Animation();
		for (int i = 1; i <= 3; i++) {
			charAnims[climb].addScene(newImage("/bobclimb" + i + ".png"), 120);
			charAnims[walkR].addScene(newImage("/walkright" + i + ".png"), 150); 
			charAnims[wizWalkR].addScene(newImage("/bobwalkwizzardR" + i + ".png"), 150);
			charAnims[walkL].addScene(newImage("/walkleft" + i + ".png"), 150); 
			charAnims[wizWalkL].addScene(newImage("/bobwalkwizzardL" + i + ".png"), 150);
			if (i == 2) {
				charAnims[climb].addScene(newImage("/bobclimb" + 1 + ".png"), 120);
				charAnims[walkR].addScene(newImage("/walkright" + 1 + ".png"), 150);
				charAnims[wizWalkR].addScene(newImage("/bobwalkwizzardR" + 1 + ".png"), 150);
				charAnims[walkL].addScene(newImage("/walkleft" + 1 + ".png"), 150);
				charAnims[wizWalkL].addScene(newImage("/bobwalkwizzardL" + 1 + ".png"), 150);
			}
		}
		
		charAnims[sideClimb] = new Animation();
		charAnims[sideClimb].addScene(newImage("/bobclimbL1.png"), 160);
		charAnims[sideClimb].addScene(newImage("/bobclimbL2.png"), 160);
		charAnims[onLadder] = new Animation();
		charAnims[onLadder].addScene(standingladder, 200);
		charAnims[standL] = new Animation();
		charAnims[standL].addScene(standingL, 200);
		charAnims[standR] = new Animation();
		charAnims[standR].addScene(standingR, 200);
		charAnims[wizStandL] = new Animation();
		charAnims[wizStandL].addScene(wizStandingL,200);
		charAnims[wizStandR] = new Animation();
		charAnims[wizStandR].addScene(wizStandingR,200);
		charAnims[jumpR] = new Animation();
		charAnims[jumpR].addScene(jumpingR, 200);
		charAnims[jumpL] = new Animation();
		charAnims[jumpL].addScene(jumpingL, 200);
		charAnims[wizJumpR] = new Animation();
		charAnims[wizJumpR].addScene(wizJumpingR, 200);
		charAnims[wizJumpL] = new Animation();
		charAnims[wizJumpL].addScene(wizJumpingL, 200);
		
		
		itemIcons[Item.TORSO] = newImage("/torso.png");
		itemIcons[Item.BOOTS] = newImage("/boots.png");
		itemIcons[Item.RING] = newImage("/ring.png");
		itemIcons[Item.WEAPON] = newImage("/weapon.png");
		itemIcons[Item.AMULET] = newImage("/amulet.png");
		itemIcons[Item.HELM] = newImage("/helm.png");
		itemIcons[Item.PANTS] = newImage("/pants.png");
		itemIcons[Item.GLOVES] = newImage("/gloves.png");
		
		equipIcons[Item.AMULET] = newImage("/equipAmu.png");
		equipIcons[Item.WEAPON] = newImage("/equipWep.png");
		equipIcons[Item.PANTS] = newImage("/equipLeg.png");
		equipIcons[Item.TORSO] = newImage("/equipTorso.png");
		equipIcons[Item.RING] = newImage("/equipRing.png");
		equipIcons[Item.HELM] = newImage("/equipHelm.png");
		equipIcons[Item.GLOVES] = newImage("/equipGlove.png");
		equipIcons[Item.BOOTS] = newImage("/equipBoot.png");
	}

	// initialise le personnage
	public void initChar(int i) {

		c = new Character(i);
		c.setAnimation(walkR);
		SkillKeys = c.getSkillKeys();
		for(int q = 0; q < skills.length; q++){
			if(skills[q]!=null)
			skills[q].skillStats();
		}

	}

	// dessine les images
	public synchronized void draw(Graphics2D g) {

		if (playing) {
			drawBackGround(g);
			drawSpots(g);
			drawDrops(g);
			drawMonsters(g);
			drawEffects(g);
			if(c.isAlive())
			drawCharacter(g);
			drawProjectiles(g);
			drawDamage(g);
			if (statMenu.isOpen())
				drawStatMenu(g);
			if (skillMenu.isOpen())
				drawSkillMenu(g);
			if (c.stats.inventory.isOpen())
				drawInventory(g);
			if(stash.isOpen())
				drawStash(g);
			if (tooltip != null)
				drawItemTooltip(g);
			if (equippedTooltip != null)
				drawEquippedTooltip(g);
			drawUI(g);
			if (isDragging)
				drawDraggedItem(g);
			
		} else if (classSelect) {
			g.setColor(Color.BLUE);
			g.fillRect(0, 0, S.getWidth(), S.getHeight());
			drawClassMenu(g);
			
		} else {
			g.setColor(Color.BLUE);
			g.fillRect(0, 0, S.getWidth(), S.getHeight());
			drawMainMenu(g);
		}

	}
	
	private void drawStash(Graphics2D g){
		
		g.setColor(Color.GRAY);
		g.fill(stash.getArea());
		
		Item item;
		for(int i = 0; i < 8; i++)
		for (int j = 0; j < 8; j++) {
			item = stash.getItem(stash.getPage(),i, j);
			if (item == null)
				g.setColor(Color.WHITE);
			else {
				switch (item.getRarity()) {
				case Item.COMMON:
					g.setColor(Color.WHITE);
					break;
				case Item.MAGIC:
					g.setColor(Color.CYAN);
					break;
				case Item.RARE:
					g.setColor(Color.YELLOW);
					break;
				}
			}
			g.fill(stash.stashSlots[i][j].getArea());
			if (item != null)
				g.drawImage(itemIcons[item.getSlot()],
						stash.stashSlots[i][j].getArea().x,
						stash.stashSlots[i][j].getArea().y, null);
		}
		
		g.setFont(new Font("Arial", Font.BOLD, 20));
		for(Stash.PageButton pageButton : stash.pageButtons){
			g.setColor(Color.DARK_GRAY);
			if(pageButton.page == stash.getPage()) g.setColor(Color.LIGHT_GRAY);
			g.fill(pageButton.area);
			g.setColor(Color.WHITE);
			g.drawString(""+(pageButton.page+1),pageButton.area.x+45,pageButton.area.y+pageButton.area.height-20);
		}
		
		
		
	}
	
	private void drawBackGround(Graphics2D g){
		Image background = bg;
		if(map.getBackground() != null) {
			background = map.getBackground();
			g.drawImage(background,(int)-X,(int)-Y,null);
		
		} else
		g.drawImage(background, 0, 0, null);
	}

	private void drawDraggedItem(Graphics2D g) {
		g.drawImage(itemIcons[draggedItem.getSlot()],
				(int) draggedItemLocation.getX(),
				(int) draggedItemLocation.getY(), null);
	}

	private void drawDrops(Graphics2D g) {
		for (Drop drop : drops) {
			if (drop != null)
				if (drop.isActive()) {
					g.setFont(new Font("Arial", Font.PLAIN, 12));
					switch(drop.getItem().getRarity()){
					case Item.MAGIC : g.setColor(Color.BLUE); break;
					case Item.RARE : g.setColor(Color.YELLOW); break;
					case Item.COMMON : g.setColor(Color.WHITE); break;
					}
					g.drawString(drop.getItem().getName(), (int)(drop.getArea().getX() - X), (int)(drop.getArea().getY() - Y));
					g.drawImage(itemIcons[drop.getItem().getSlot()],
							(int) (drop.getArea().getX() - X), (int) (drop
									.getArea().getY() - Y), null);
				}
		}
	}

	private void drawEquippedTooltip(Graphics2D g) {

		Tooltip ttip = equippedTooltip;

		g.setColor(Color.WHITE);
		Rectangle r = ttip.getArea();
		g.fillRect((int) r.getX(), (int) r.getY() - 20, (int) r.getWidth(),
				(int) r.getHeight() + 20);
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.PLAIN, 14));
		g.drawString("Currently Equipped : ", (int) r.getX() + 7,
				(int) r.getY() - 5);
		drawTooltip(g, ttip, r);
	}

	private void drawTooltip(Graphics2D g, Tooltip ttip, Rectangle r) {
		g.draw(r);
		g.drawLine((int) r.getX(), (int) r.getY() + 46,
				(int) (r.getX() + r.getWidth()), (int) r.getY() + 46);

		for (int i = 1; i <= 8; i++) {
			g.setColor(Color.BLACK);
			if ((i == 1 && ttip.getItem().getRarity() == Item.MAGIC)
					|| (i == 2 && ttip.getItem().getEnhancedD() != 0))
				g.setColor(Color.BLUE);
			if (i == 1 && ttip.getItem().getRarity() == Item.RARE)
				g.setColor(Color.ORANGE);
			g.drawString(ttip.getInfo(i), (int) r.getX() + 7, (int) r.getY()
					+ 2 + 20 * i);
		}
	}

	private void drawItemTooltip(Graphics2D g) {

		Tooltip ttip = tooltip;

		g.setColor(Color.WHITE);
		Rectangle r = ttip.getArea();
		g.fill(r);
		g.setColor(Color.BLACK);
		g.setFont(new Font("Arial", Font.PLAIN, 14));
		drawTooltip(g, ttip, r);
	}

	private void drawInventory(Graphics2D g) {

		g.setColor(Color.GRAY);
		g.fillRect(Inventory.x, Inventory.y, Inventory.width, Inventory.height);

		Item item;

		for (int i = 0; i < c.stats.inventory.equipSlot.length; i++) {
			item = c.stats.inventory.getEquip(i);
			if (item == null){
				g.setColor(Color.WHITE);
				if(equipIcons[i] != null)
			g.drawImage(equipIcons[i],
					c.stats.inventory.equipSlot[i].getArea().x,
					c.stats.inventory.equipSlot[i].getArea().y, null);
				else g.fill(c.stats.inventory.equipSlot[i].getArea());
			} else {
				switch (item.getRarity()) {
				case Item.COMMON:
					g.setColor(Color.WHITE);
					break;
				case Item.MAGIC:
					g.setColor(Color.CYAN);
					break;
				case Item.RARE:
					g.setColor(Color.YELLOW);
					break;
				}
				g.fill(c.stats.inventory.equipSlot[i].getArea());
				g.drawImage(itemIcons[item.getSlot()],
						c.stats.inventory.equipSlot[i].getArea().x,
						c.stats.inventory.equipSlot[i].getArea().y, null);
			}

			for (int j = 0; j < 8; j++) {
				item = c.stats.inventory.getItem(i, j);
				if (item == null)
					g.setColor(Color.WHITE);
				else {
					switch (item.getRarity()) {
					case Item.COMMON:
						g.setColor(Color.WHITE);
						break;
					case Item.MAGIC:
						g.setColor(Color.CYAN);
						break;
					case Item.RARE:
						g.setColor(Color.YELLOW);
						break;
					}
				}
				g.fill(c.stats.inventory.itemSlot[i][j].getArea());
				if (item != null)
					g.drawImage(itemIcons[item.getSlot()],
							c.stats.inventory.itemSlot[i][j].getArea().x,
							c.stats.inventory.itemSlot[i][j].getArea().y, null);
			}
		}

		g.setFont(new Font("Arial", Font.PLAIN, 16));

		g.drawString("Damage : " + c.getMinDamage() + " - " + c.getMaxDamage(),
				Inventory.x + 20, Inventory.y + 180);
		g.drawString("Defense : " + c.getDefense(), Inventory.x
				+ 20, Inventory.y + 340);
		g.drawString(
				"Damage reduction : "
						+ new DecimalFormat("#.#").format(c
								.getDamageReduction()) + "%", Inventory.x
						+ 20, Inventory.y + 360);

		for (int i = 0; i < 8;i++) {
			if(i != ALLSTATS){
			String info = "";
			switch (i) {
			case SPIRIT:
				info = "Spirit : ";
				break;
			case POW:
				info = "Power : ";
				break;
			case AGI:
				info = "Agility : ";
				break;
			case VIT:
				info = "Vitality : ";
				break;
			case CRIT:
				info = "Crit chance : ";
				break;
			case CRITDMG:
				info = "Crit damage : ";
				break;
			case MASTERY:
				info = "Mastery : ";
				break;
			}
			if (i == CRIT)
				info += new DecimalFormat("#.#").format(c.getStat(i));
			else
				info += (int) c.getStat(i);
			if (i == CRIT || i == CRITDMG || i == MASTERY)
				info += "%";

			g.drawString(info, Inventory.x +20, Inventory.y
					+ 200 + i * 20);
			}
		}
	}

	private void drawSkillMenu(Graphics2D g) {
		g.setColor(Color.GRAY);
		g.fill(skillMenu.getArea());
		for (SkillButton skillButton : skillMenu.skillButtons) {
			g.setColor(Color.WHITE);
			g.fill(skillButton.getArea());
			
			
			if(!skillButton.passive){
				
				g.setFont(new Font("Arial", Font.BOLD, 18));
				switch(c.stats.classe){
				case MAGE : g.setColor(Color.BLUE); break;
				case FIGHTER : g.setColor(Color.RED); break;
				case ARCHER : g.setColor(Color.ORANGE); break;
				}
				
				String manaUsed = "" + skills[skillButton.skill].manaUsed;;
				if(skills[skillButton.skill].manaUsed < 0) {manaUsed = "+"+(-skills[skillButton.skill].manaUsed);}
				g.drawString(manaUsed,
						skillButton.getNamePos().x - 465,
						skillButton.getNamePos().y);
			}
			
			g.setFont(new Font("Arial", Font.PLAIN, 14));
			g.setColor(Color.BLACK);
			String skillName = skillButton.getName();
			g.drawString(skillName, skillButton.getNamePos().x + 23 - 4
					* skillName.length(), skillButton.getNamePos().y);
			String lvl;
			if (skillButton.passive)
				lvl = "" + passives[skillButton.skill].getLvl();
			else
				lvl = "" + skills[skillButton.skill].getLvl();
			g.drawString(lvl, skillButton.getNamePos().x + 15, 
					skillButton.getNamePos().y - 20);
			g.drawString(skillButton.getInfo(),
					skillButton.getNamePos().x - 415,
					skillButton.getNamePos().y - 20);
			g.drawString(skillButton.getNextLevel(),
					skillButton.getNamePos().x - 415,
					skillButton.getNamePos().y + 10);
		}

		g.setColor(Color.WHITE);
		g.drawString("Remaining points : " + c.getSkillPts(), 750, 500);
	}

	private void drawStatMenu(Graphics2D g) {
		g.setFont(new Font("Arial", Font.PLAIN, 14));
		g.setColor(Color.GRAY);
		g.fill(statMenu.getArea());
		for (int i = 0; i < statMenu.statButtons.length; i++) {
			g.setColor(Color.WHITE);
			g.fill(statMenu.statButtons[i].getArea());
			g.setColor(Color.BLACK);
			g.drawString(statMenu.statButtons[i].getText(),
					statMenu.statButtons[i].getTextPosition().x,
					statMenu.statButtons[i].getTextPosition().y);
			g.drawString(statMenu.statButtons[i].getInfo(),
					statMenu.statButtons[i].getTextPosition().x + 50,
					statMenu.statButtons[i].getTextPosition().y - 20);
			//g.drawString(statMenu.statButtons[i].getTotal(),
			//		statMenu.statButtons[i].getTextPosition().x + 50,
			//		statMenu.statButtons[i].getTextPosition().y + 5);
			g.drawString(Integer.toString(c.atts[i]),
					statMenu.statButtons[i].getTextPosition().x + 8,
					statMenu.statButtons[i].getTextPosition().y - 25);
			g.setColor(Color.WHITE);
			g.drawString("Remaining points : " + c.getStatPts(), 390, 480);
		}
	}

	private void drawSpots(Graphics2D g) {
		g.setColor(Color.BLUE);
		for (Spot spot : spots)
			if (spot != null)if(!spot.invisible) {
				g.drawImage(spotImage, (int)(spot.getArea().x-X),(int)(spot.getArea().y-Y), null);
			}
	}

	private void drawMonsters(Graphics2D g) {
		for (Monster monster : monsters)
			if (monster != null)
				if (monster.isAlive()) {
					int f = monster.getLife() * 100;
					f = f / monster.getMaxLife();
					g.drawImage(monster.getImage(), monster.getX() - (int) X,
							monster.getY() - (int) Y, null);
					g.setColor(Color.RED);
					g.fillRect(monster.getX() - (int) X, monster.getY() - 10
							- (int) Y, monster.getWidth(), 10);
					g.setColor(Color.GREEN);
					g.fillRect(monster.getX() - (int) X, monster.getY() - 10
							- (int) Y, f * monster.getWidth() / 100, 10);
					g.setFont(new Font("Arial", Font.PLAIN, 14));
					if(monster.elite) g.setColor(Color.ORANGE);
					else g.setColor(Color.WHITE);
					g.drawString("Lv" + monster.getLevel() + " "+monster.name, monster.getX()-(int)X, monster.getY()-12-(int)Y);
					g.setColor(Color.WHITE);
					if(monster.elite)
					g.drawString(monster.eliteType, monster.getX()-(int)X, monster.getY()-25-(int)Y);
				}
	}

	private void drawCharacter(Graphics2D g) {
		if (c.isInvincible()) {
			counter++;
			if (counter >= 2) {
				blink = !blink;
				counter = 0;
			}
		} else
			blink = true;

		if (blink) {
			if (currentSkill != -1) {
				Skill skill = skills[currentSkill];
				g.drawImage(skill.getImage(), skill.getX() - (int) X,
						skill.getY() - (int) Y, null);
			} else
				g.drawImage(c.getImage(), c.getX() - (int) X, c.getY()
						- (int) Y, null);
		}
	}

	private void drawUI(Graphics2D g) {
		g.setColor(Color.BLACK);
		g.fillRect(0, S.getHeight() - 55, S.getWidth(), 55);
		
		if(c.stats.classe == FIGHTER)
			g.setColor(Color.GRAY);
			else
			g.setColor(Color.RED);
		g.fill(new Rectangle(100, S.getHeight() - 50, 400, 20));
		g.fill(new Rectangle(100, S.getHeight() - 25, 400, 20));
		g.setColor(Color.GREEN);
		g.fill(lifeBar());
		if(c.stats.classe == MAGE)
		g.setColor(Color.BLUE);
		else if(c.stats.classe == ARCHER)
		g.setColor(Color.ORANGE);
		else if(c.stats.classe == FIGHTER)
		g.setColor(Color.RED);
		g.fill(manaBar());
		g.setColor(Color.WHITE);
		g.setFont(new Font("Arial", Font.BOLD, 40));
		g.drawString(Integer.toString(c.stats.lvl), 20, S.getHeight() - 10);
		g.setFont(new Font("Arial", Font.PLAIN, 12));
		g.drawString(
				Integer.toString(c.getLife()) + " / "
						+ Integer.toString(c.getMaxLife()), 260,
				S.getHeight() - 35);
		g.drawString(
				Integer.toString(c.getMana()) + " / "
						+ Integer.toString(c.getMaxMana()), 260,
				S.getHeight() - 10);
		g.setColor(Color.YELLOW);
		g.setFont(new Font("Arial", Font.PLAIN, 26));
		g.drawString(
				"Exp: " + Integer.toString(c.stats.exp) + "/"
						+ Integer.toString(c.expToNextLvl()), 520,
				S.getHeight() - 15);
	}

	private void drawDamage(Graphics2D g) {
		g.setFont(new Font("Arial Black", Font.BOLD, 32));
		for (FlyingText damageText : damage) {
			if (damageText != null)
				if (damageText.isActive()) {
					g.setColor(damageText.getColor());
					g.drawString(damageText.getText(), damageText.getX() - X,
							damageText.getY() - Y);
				}
		}
	}

	private void drawProjectiles(Graphics2D g) {
		for (Projectile projectile : projectiles) {
			if (projectile != null)
				if (projectile.isActive()) {
					Projectile bob = projectile;
					g.drawImage(bob.getImage(), (int) (bob.getX() - X),
							(int) (bob.getY() - Y), null);
				}
		}
	}

	private void drawEffects(Graphics2D g) {
		for (Effect effect : effects) {
			if (effect != null)
				if (effect.isActive()) {
					Effect bob = effect;
					g.drawImage(bob.getImage(), (int) (bob.getX() - X),
							(int) (bob.getY() - Y), null);
				}
		}
	}

	private void drawMainMenu(Graphics2D g) {

		g.setFont(new Font("Arial", Font.PLAIN, 20));
		for (SaveButton saveButton : mainMenu.saveButtons) {
			g.setColor(Color.WHITE);
			g.fill(saveButton.getArea());
			g.setColor(Color.BLACK);
			g.drawString(saveButton.info(), saveButton.infoPos.x,
					saveButton.infoPos.y);
			g.setColor(Color.RED);
			g.fill(saveButton.getDelete());
		}
	}

	private void drawClassMenu(Graphics2D g) {
		g.setFont(new Font("Arial", Font.PLAIN, 30));
		for (ClassButton classButton : classMenu.classButtons) {
			g.setColor(Color.WHITE);
			g.fill(classButton.getArea());
			g.setColor(Color.BLACK);
			g.drawString(classButton.info(), classButton.infoPos.x,
					classButton.infoPos.y);
		}
	}

	// update tous les monstres, le personnage et la map
	public synchronized void update(long timePassed) {

		if (playing) {

			if (!c.isAlive()){
				respawnTimer -= timePassed;
				if(respawnTimer <= 0){
				c.respawn();
				respawnTimer = 5000;
				}
			}
			
			if(c.onLadder!=null){
				Ladder onLadder = null;
				for(Ladder ladder : ladders)if(ladder !=null) if(c.getArea().intersects(ladder)) onLadder = ladder;
				
				c.onLadder = onLadder;
			}
			
			if(c.onLadder==null || !c.canMove)
			c.fall(timePassed);

			for (Monster monster : monsters)
				if (monster != null)
					monster.fall(timePassed);

			test();// collisions
			testProjectiles();

			moveMap(timePassed);

			updateSkill(timePassed);

			for (Monster monster : monsters) {
				if (monster != null) {
					monster.setAnimation(monster.getAnimation(monster
							.isFacingLeft()));
					monster.update(timePassed);
				}
			}

			for (FlyingText damageText : damage) {
				if (damageText != null)
					if (damageText.isActive())
						damageText.update(timePassed);
			}
			
			if(c.isAlive())
			c.update(timePassed);

			for (Projectile projectile : projectiles) {
					if (projectile.isActive())
						projectile.update(timePassed);
			}

			for (Drop drop : drops) {
					if (drop.isActive())
						drop.update(timePassed);
			}

			for (Effect effect : effects) {
					if (effect.isActive())
						effect.update(timePassed);
			}
		}
	}

	public void testProjectiles() {
		for (Projectile projectile : projectiles) {
				if (projectile.isActive()) {
					Monster m = testProjectile(projectile);

					if (m != null) {

						if (projectile.skill.skill == Skill.ExplosiveArrow) {

							Animation explosion = new Animation();
							for (int k = 1; k <= 5; k++)
								explosion.addScene(newImage("/explosion" + k
										+ ".png"), 30);
							add(new Effect(new Point((int) projectile.skill
									.getArea().getX(), (int) projectile.skill
									.getArea().getY()), explosion, 150));
							projectile.skill.hit(projectile.skill, 0);
							try {
								AudioInputStream music = AudioSystem
										.getAudioInputStream(getClass()
												.getResource("boom1.wav"));
								Clip clip = AudioSystem.getClip();
								clip.open(music);
								clip.start();
							} catch (Exception e) {
								e.printStackTrace();
							}

						} else {

							damageMonster(m,c.getDamage(m, projectile.skill
									.getDmgMult(projectile.number)),
									projectile.skill
											.getKBSpeed(projectile.number));
						}

						projectile.delete();

					}
				}
		}
	}

	private void updateSkill(long timePassed) {
		
		if(c.isAlive())
		if (activatedSkillKey != -1) {
			if (currentSkill != -1)
				if (!skills[currentSkill].isActive()) {
					currentSkill = SkillKeys[activatedSkillKey];
					if (skills[currentSkill] != null)
						skills[currentSkill].activate();
					c.setUsingSkill(true);
				}

			if (currentSkill == -1) {
				currentSkill = SkillKeys[activatedSkillKey];
				if (skills[currentSkill] != null)
					skills[currentSkill].activate();
				c.setUsingSkill(true);
			}
		}
		
		if (currentSkill != -1)
			if (skills[currentSkill].isActive() && c.isAlive()) {
				skills[currentSkill].update(timePassed);
				c.setUsingSkill(true);
			} else {
				currentSkill = -1;
				c.setUsingSkill(false);
			}
	}

	// bouge la "cam�ra" selon la position du personnage
	public synchronized void moveMap(long timePassed) {
		if ((X + S.getWidth() < map.getXLimit() && c.getXVelocity() > 0 && c
				.getX() + c.getWidth() - X > S.getWidth() * 2 / 3)
				|| (X > 0 && c.getXVelocity() < 0 && c.getX() - X < S
						.getWidth() / 3)) {
			X += c.getXVelocity() * timePassed;
			if (X + S.getWidth() > map.getXLimit())
				X = map.getXLimit() - S.getWidth();
			if (X < 0)
				X = 0;
		}

		if ((Y + S.getHeight() < map.getYLimit() && c.getYVelocity() > 0 && c
				.getY() + c.getHeight() - Y > S.getHeight() * 2 / 3)
				|| (Y > 0 && c.getYVelocity() < 0 && c.getY() - Y < S
						.getHeight() / 3)) {
			Y += c.getYVelocity() * timePassed;
			if (Y + S.getHeight() > map.getYLimit())
				Y = map.getYLimit() - S.getHeight();
			if (Y < 0)
				Y = 0;
		}
	}

	// teste les collisions
	public void test() {
		
		boolean inWater = false;
		for(Rectangle wateur : water)
			if(wateur != null)
			if(wateur.intersects(c.getArea())) inWater = true;
		
		c.inWater = inWater;
		

		for (Platform platform : platforms) {
				if (c.getBase().intersects(platform.getTop())) {

					if (!c.canMove() && c.getYVelocity() >= 0) {
						c.setXVelocity(0);
						c.canMove(true);
					}
					if (c.getYVelocity() > 0 && !down) {
						c.setYVelocity(0);
						c.setY(platform.getTopY() - c.getHeight());
					} else if(c.getYVelocity() >= 0 && c.onLadder != null){
						boolean touchesLadder = false;
						for(Ladder ladder : ladders)
							if(platform.getTop().intersects(ladder.getTop()) || platform.getTop().intersects(ladder)) touchesLadder = true;
						if(!touchesLadder){
							c.onLadder = null;
							c.setYVelocity(0);
							c.setY(platform.getTopY() - c.getHeight());
						}
					}
				}
		}

		for (Wall wall : walls) {
				if (c.getXVelocity() < 0
						&& c.getLeftSide().intersects(wall.getSide())) {
					c.setX(wall.getX() + wall.getWidth() - 2);
					c.setXVelocity(0);

				} else if (c.getXVelocity() > 0
						&& c.getRightSide().intersects(wall.getSide())) {
					c.setX(wall.getX() - c.getWidth() + 2);
					c.setXVelocity(0);
				}

				if (c.getBase().intersects(wall.getTop())) {

					if (c.getYVelocity() >= 0) {
						c.setYVelocity(0);
						c.setY(wall.getTopY() - c.getHeight());
						c.onLadder = null;
						if (!c.canMove()) {
							c.setXVelocity(0);
							c.canMove(true);
						}
					}
				}

				if (c.getTop().intersects(wall.getBot())) {
					if (c.getYVelocity() < 0) {
						c.setYVelocity(0);
						c.setY(wall.getBotY() + 2);
					}
				}
				for (Projectile projectile : projectiles) {
						if (projectile.isActive())
							if (projectile.getArea().intersects(wall.getArea()))
								projectile.delete();
				}
			
		}

		for (Monster monster : monsters) {
			if (monster != null)
				if (monster.isAlive()) {

					for (Wall wall : walls)
						if (wall != null)
							if (wall.getTop().intersects(monster.getBase())
									&& monster.getYVelocity() > 0) {
								monster.setY(wall.getTopY()
										- monster.getHeight());
								monster.setYVelocity(0);
							}
					for (Platform platform : platforms)
						if (platform != null)
							if (platform.getTop().intersects(monster.getBase())
									&& monster.getYVelocity() > 0) {
								monster.setY(platform.getTopY()
										- monster.getHeight());
								monster.setYVelocity(0);
							}
					turnMonster(monster);
				}
		}

		if (!c.isInvincible())
			for (Monster monster : monsters)
				if (monster != null)
					if (monster.isAlive())
						if (c.getArea().intersects(monster.getArea())) {
							boolean created = false;
							for (int i = 0; !created; i++) {
								if(damage.size() <= i){
									damage.add(new FlyingText(monster.hit(c), c));
									created = true;
								} else if (!damage.get(i).isActive()) {
										damage.set(i,new FlyingText(monster.hit(c), c));
										created = true;
									}
							}
						}

		if (c.getY() > map.getYLimit() - c.getHeight())
			c.setY(map.getYLimit() - c.getHeight());
		if (c.getY() < 5) {
			c.setY(5);
			c.setYVelocity(0);
		}

		if (c.getYVelocity() == 0) {
			if (c.getXVelocity() > 0){
				if(c.onLadder!=null) c.setAnimation(sideClimb);
				else if(c.stats.classe == MAGE) c.setAnimation(wizWalkR);
				else c.setAnimation(walkR);
			} else if (c.getXVelocity() < 0){
				if(c.onLadder!=null) c.setAnimation(sideClimb);
				else if(c.stats.classe == MAGE) c.setAnimation(wizWalkL);
				else c.setAnimation(walkL);
			} else {
				if (c.isFacingLeft()){
					if(c.stats.classe == MAGE) c.setAnimation(wizStandL);
					else c.setAnimation(standL);
				} else {
					if(c.stats.classe == MAGE) c.setAnimation(wizStandR);
					else c.setAnimation(standR);
				}
				if(c.onLadder!=null) c.setAnimation(onLadder);
			}
	
			
		} else if(c.onLadder!=null)c.setAnimation(climb); 
		else if (c.isFacingLeft()){
			if(c.stats.classe == MAGE) c.setAnimation(wizJumpL);
			else c.setAnimation(jumpL);
		} else {
			if(c.stats.classe == MAGE) c.setAnimation(wizJumpR);
			else c.setAnimation(jumpR);
		}

		if (!new Rectangle(0, 0, map.getXLimit(), map.getYLimit()).contains(c
				.getArea())) {
			c.setX(map.spawnPoint.x);
			c.setY(map.spawnPoint.y);
			X = map.spawnCamera.x;
			Y = map.spawnCamera.y;
		}

	}

	// "Intelligence Artificielle" des monstres (les tourne s'il n'y a aucune
	// plateforme plus loin)
	public void turnMonster(Monster m) {
		
		boolean turn, facingwall = false;
		if (m.getYVelocity() == 0)
			turn = true;
		else
			turn = false;
		
		
		for (Wall wall : walls)
			if (wall != null)
				if (wall.getTop().intersects(m.getNextFloor()))
					turn = false;
		for (Platform platform : platforms)
			if (platform != null)
				if (platform.getTop().intersects(m.getNextFloor()))
					turn = false;

		for (Wall wall : walls)
			if (wall != null)
				if (wall.getSide().intersects(m.getSide())){
					turn = true;
					facingwall = true;
				}
			
		if (turn) {
			if (m.canMove()) {
				if(m.isAggro && isFacingChar(m) && !facingwall && m.getYVelocity()==0){ 
					m.jump();
				}else{
				m.setXVelocity(-m.getXVelocity());
				m.setFacingLeft(!m.isFacingLeft());
				}
				
			} else
				m.setXVelocity(0);
		}

		if (m.canMove()
				&& (m.getXVelocity() != m.getSpeed() && m.getXVelocity() != -m
						.getSpeed())) {
			if (c.getX() + c.getWidth() / 2 > m.getX() + m.getWidth() / 2) {
				m.setXVelocity(m.getSpeed());
				m.setFacingLeft(false);
			} else {
				m.setXVelocity(-m.getSpeed());
				m.setFacingLeft(true);
			}
		}

	}

	// load la prochaine map
	public synchronized void loadNextMap(int i) {

		c.setX((int) map.getStart(i).getX());
		c.setY((int) map.getStart(i).getY());

		X = (int) map.getXY(i).getX();
		Y = (int) map.getXY(i).getY();
		if (X != 0)
			X += 1280 - S.getWidth();
		if (Y != 0)
			Y += 960 - S.getHeight();

		platforms = null;
		walls = null;
		loadMap(map.getNextMap(i));

	}

	public synchronized void loadMap(int i) {

		for (Drop drop : drops)
			if (drop != null)
				drop.delete();

		map = new Map(i);
		
		ladders = map.getLadders();
		platforms = map.getPlatforms();
		walls = map.getWalls();
		water = map.getWater();

		monsters = map.getMonsters();
		spots = map.getSpots();

		if (c != null)
			c.stats.currentMap = i;
	}

	public Point getTarget(Rectangle area) {
		for (Monster monster : monsters) {
			if (monster != null)
				if (monster.isAlive())
					if (monster.getArea().intersects(area))
						return new Point(monster.getX() + monster.getWidth()
								/ 2, monster.getY() + monster.getHeight() / 2);
		}
		return null;
	}

	// rajoute ou enleve un projectile
	public void add(Projectile projectile) {
		boolean created = false;
		for (int i = 0; !created; i++) {
			if(projectiles.size() <= i){
				projectiles.add(projectile);
				created = true;
			} else if (!projectiles.get(i).isActive()) {
					projectiles.set(i,projectile);
					created = true;
				}
		}
	}

	public void add(Effect effet) {
		boolean created = false;
		for (int i = 0; !created; i++) {
			if(effects.size() <= i){
				effects.add(effet);
				created = true;
			} else if (!effects.get(i).isActive()) {
					effects.set(i,effet);
					created = true;
				}
		}
	}

	public void add(Drop drop) {
		boolean created = false;
		for (int i = 0; !created; i++) {
			if(drops.size() <= i){
				drops.add(drop);
				created = true;
			} else if (!drops.get(i).isActive()) {
					drops.set(i,drop);
					created = true;
				}
		}
	}

	// retourne les barres de vie et de mana
	public Rectangle lifeBar() {
		int f = c.getLife() * 100;
		f = f / c.getMaxLife();
		return new Rectangle(100, S.getHeight() - 50, 4 * f, 20);
	}

	public Rectangle manaBar() {
		int f = c.getMana() * 100;
		f = f / c.getMaxMana();
		return new Rectangle(100, S.getHeight() - 25, 4 * f, 20);
	}

	// retourne une image � partir de la source
	public Image newImage(String source) {
		return new ImageIcon(getClass().getResource(source)).getImage();
	}

	// clavier
	public void keyPressed(KeyEvent e) {
		int key = e.getKeyCode();

		if (playing) {
			if (currentSkill == -1) {
				if (key == KeyEvent.VK_DOWN){
					c.isPressingClimb(-1);
					down = true;
				}
				if (key == KeyEvent.VK_ALT) {
					for (Platform platform : platforms)
						if (platform != null)
							if (c.getBase().intersects(platform.getTop())
									&& c.getYVelocity() == 0)
								c.jump();
					for (Wall wall : walls)
						if (wall != null)
							if (c.getBase().intersects(wall.getTop())
									&& c.getYVelocity() == 0)
								c.jump();
					if(c.onLadder!=null) c.jump();
					if(c.inWater && c.getYVelocity() > 0) c.jump();
					
				} else if (key == KeyEvent.VK_LEFT) {
					c.move(LEFT);
					c.setFacingLeft(true);
				} else if (key == KeyEvent.VK_RIGHT) {
					c.move(RIGHT);
					c.setFacingLeft(false);
				} else if (key == KeyEvent.VK_UP) {
					boolean teleported = false;
					for (int i = 0; i < spots.size(); i++)
						if (c.getArea().intersects(spots.get(i).getArea())
								&& !teleported) {
							teleported = true;
							loadNextMap(i);
						}
					if(teleported == false){
						c.isPressingClimb(1);
							
					}
				}
			}
			
			if(stash.isOpen())
				if(key >= 49 && key <= 52) stash.setPage(key-49);
			
			
			if(c.isAlive())
			if(c.onLadder==null)
			if(key<256)
			if (activatedSkillKey == -1 && skills[SkillKeys[key]] != null)
				if (skills[SkillKeys[key]].getLvl() > 0)
					activatedSkillKey = key;

			if (key == KeyEvent.VK_M)
				if (clip.isRunning()){
					clip.stop();
					clip.setFramePosition(0);
				}
				else clip.loop(Clip.LOOP_CONTINUOUSLY);
				
			
			if (key == KeyEvent.VK_I || key == KeyEvent.VK_Q) {
				if(statMenu.isOpen())statMenu.toggle();
				if(skillMenu.isOpen())skillMenu.toggle();
				if(key == KeyEvent.VK_I || !c.stats.inventory.isOpen())
				c.stats.inventory.toggle();
				tooltip = null;
				equippedTooltip = null;
				if(key == KeyEvent.VK_Q || stash.isOpen()) stash.toggle();
			}
			
			if (key == KeyEvent.VK_A)
				statMenu.toggle();
			if (key == KeyEvent.VK_S)
				skillMenu.toggle();
			if (key == KeyEvent.VK_X)
				getDrops();
			if (key == KeyEvent.VK_F4)
				save();

			if (key == KeyEvent.VK_ESCAPE) {
				if(stash.isOpen()) {
					stash.toggle();
					c.stats.inventory.toggle();
					tooltip = null;
					equippedTooltip = null;
					}
				
				else if (c.stats.inventory.isOpen()){
					c.stats.inventory.toggle();
					tooltip = null;
					equippedTooltip = null;}
				else if (statMenu.isOpen())
					statMenu.toggle();
				else if (skillMenu.isOpen())
					skillMenu.toggle();
				else {
					save();
					mainMenu();
				}
			}
		} else if (classSelect) {
			if (key == KeyEvent.VK_ESCAPE) {
				classSelect = false;
				clip.stop();
				clip.setFramePosition(0);
			}
		} else {
			if (key == KeyEvent.VK_ESCAPE)
				stop();
		}
		e.consume();

	}

	public void getDrops() {
		for (Drop drop : drops) {
			if (drop != null)
				if (drop.isActive())
					if (drop.getArea().intersects(c.getArea())) {
						if (c.stats.inventory.add(drop.getItem()))
							drop.delete();
						return;
					}
		}
	}

	public void keyReleased(KeyEvent e) {
		if (playing) {
			int key = e.getKeyCode();

			if (activatedSkillKey == key)
				activatedSkillKey = -1;
			if ((key == KeyEvent.VK_LEFT && c.getXVelocity() <= 0)
					|| !c.canMove())
				c.stopMoving();
			if ((key == KeyEvent.VK_RIGHT && c.getXVelocity() >= 0)
					|| !c.canMove())
				c.stopMoving();
			if (key == KeyEvent.VK_DOWN){
				c.isPressingClimb(0);
				down = false;
			}
			if(key == KeyEvent.VK_UP){
				c.isPressingClimb(0);
			}
		}
		e.consume();
	}

	public void keyTyped(KeyEvent e) {
		e.consume();
	}

	@Override
	public void mousePressed(MouseEvent e) {
		if (playing) {
			if (statMenu.isOpen()) {
				for (StatButton statButton : statMenu.statButtons) {
					if (statButton.getArea().contains(e.getLocationOnScreen()))
						statButton.activate();
				}
			}
			if (skillMenu.isOpen()) {
				for (SkillButton skillButton : skillMenu.skillButtons) {
					if (skillButton.getArea().contains(e.getLocationOnScreen()))
						skillButton.activate();
				}
			}
			if(stash.isOpen()){
				for(Stash.PageButton pageButton : stash.pageButtons){
					if(pageButton.area.contains(e.getLocationOnScreen()))
						pageButton.activate();
				}
			}
		} else if (classSelect) {

			for (ClassButton classButton : classMenu.classButtons) {
				if (classButton.getArea().contains(e.getLocationOnScreen()))
					classButton.activate();
			}

		} else {
			for (SaveButton saveButton : mainMenu.saveButtons) {
				if (saveButton.getArea().contains(e.getLocationOnScreen()))
					saveButton.activate();
				if (saveButton.getDelete().contains(e.getLocationOnScreen()))
					saveButton.deleteSave();
			}
		}

	}

	public void mouseDragged(MouseEvent e) {
		if (playing)
			if (!isDragging && c.stats.inventory.isOpen()) {
				for (int i = 0; i < 8; i++) {
					if (c.stats.inventory.equipSlot[i].getArea().contains(
							e.getLocationOnScreen())
							&& c.stats.inventory.getEquip(i) != null) {
						isDragging = true;
						previousItemI = i;
						draggedItem = c.stats.inventory.getEquip(i);
						draggedItemLocation = e.getLocationOnScreen();
						c.stats.inventory.delete(i);
						tooltip = null;
						equippedTooltip = null;
					}
					for (int j = 0; j < 8; j++) {
						if (c.stats.inventory.itemSlot[i][j].getArea()
								.contains(e.getLocationOnScreen())
								&& c.stats.inventory.getItem(i, j) != null) {
							isDragging = true;
							previousItemI = i;
							previousItemJ = j;
							draggedItem = c.stats.inventory.getItem(i, j);
							draggedItemLocation = e.getLocationOnScreen();
							c.stats.inventory.delete(i, j);
							tooltip = null;
							equippedTooltip = null;
						}
						if(stash.isOpen())
						if(stash.stashSlots[i][j].getArea().contains(e.getLocationOnScreen()) && stash.getItem(i, j) != null){
							isDragging = true;
							previousItemI = i;
							previousItemJ = j;
							previousItemP = stash.getPage();
							draggedItem = stash.getItem(i, j);
							draggedItemLocation = e.getLocationOnScreen();
							stash.delete(i, j);
							tooltip = null;
							equippedTooltip = null;
						}
					}
				}
			} else {
				draggedItemLocation = e.getLocationOnScreen();
			}
	}

	@Override
	public void mouseMoved(MouseEvent e) {

		Point mouseLocation = e.getLocationOnScreen();
		Point tooltipLocation = mouseLocation;
		Point equipTooltipLocation = new Point(mouseLocation.x+165, mouseLocation.y);
		if(mouseLocation.x+346 > S.getWidth()){
			tooltipLocation = new Point(mouseLocation.x - 170, mouseLocation.y);
			equipTooltipLocation = new Point(mouseLocation.x - 335, mouseLocation.y);
		}

		if (playing) {
			boolean onItem = false;
			
			Item item;
			if (c.stats.inventory.isOpen()) {
				for (int i = 0; i < 8; i++) {
					for (int j = 0; j < 8; j++) {
						item = c.stats.inventory.getItem(i, j);
						if (c.stats.inventory.itemSlot[i][j].getArea()
								.contains(mouseLocation) && item != null) {
							if (tooltip == null) {
								tooltip = new Tooltip(item, tooltipLocation);
								Item equipped = c.stats.inventory.getEquip(item
										.getSlot());
								if (equipped != null)
									equippedTooltip = new Tooltip(equipped,
											equipTooltipLocation);
							} else if (tooltip.getItem() != c.stats.inventory
									.getItem(i, j)) {
								tooltip = new Tooltip(item, tooltipLocation);
								Item equipped = c.stats.inventory.getEquip(item
										.getSlot());
								if (equipped != null)
									equippedTooltip = new Tooltip(equipped,
											equipTooltipLocation);
							} else {
								tooltip.setArea(tooltipLocation);
								if (equippedTooltip != null)
									equippedTooltip.setArea(
											equipTooltipLocation);
							}
							onItem = true;
						}
						
						if(stash.isOpen()){
							item = stash.getItem(i, j);
							if (stash.stashSlots[i][j].getArea()
									.contains(mouseLocation) && item != null) {
								if (tooltip == null) {
									tooltip = new Tooltip(item, tooltipLocation);
									Item equipped = c.stats.inventory.getEquip(item
											.getSlot());
									if (equipped != null)
										equippedTooltip = new Tooltip(equipped,
												equipTooltipLocation);
								} else if (tooltip.getItem() != stash.getItem(i, j)) {
									
									tooltip = new Tooltip(item, tooltipLocation);
									Item equipped = c.stats.inventory.getEquip(item
											.getSlot());
									if (equipped != null)
										equippedTooltip = new Tooltip(equipped,
												equipTooltipLocation);
								} else {
									tooltip.setArea(tooltipLocation);
									if (equippedTooltip != null)
										equippedTooltip.setArea(equipTooltipLocation);
								}
								onItem = true;
							}
						}
						
					}
					if (c.stats.inventory.equipSlot[i].getArea().contains(
							mouseLocation)
							&& c.stats.inventory.getEquip(i) != null) {
						if (tooltip == null)
							tooltip = new Tooltip(
									c.stats.inventory.getEquip(i),
									tooltipLocation);
						else if (tooltip.getItem() != c.stats.inventory
								.getEquip(i))
							tooltip = new Tooltip(
									c.stats.inventory.getEquip(i),
									tooltipLocation);
						else
							tooltip.setArea(tooltipLocation);
						onItem = true;
					}
				}
			}

			if (!onItem) {
				tooltip = null;
				equippedTooltip = null;
			}
		}
	}

	public void mouseClicked(MouseEvent e) {
		if (playing)
			if (c.stats.inventory.isOpen()) {
				boolean drop = true;
				if (e.getButton() == MouseEvent.BUTTON1)
					drop = false;
				for (ItemSlot slot : c.stats.inventory.equipSlot)
					if (slot.getArea().contains(e.getLocationOnScreen())) {
						slot.activate(drop);
						tooltip = null;
						equippedTooltip = null;
					}
				for (ItemSlot[] row : c.stats.inventory.itemSlot)
					for (ItemSlot slot : row)
						if (slot.getArea().contains(e.getLocationOnScreen())) {
							slot.activate(drop);
							tooltip = null;
							equippedTooltip = null;
						}
			}

	}

	public void mouseEntered(MouseEvent arg0) {
	}

	public void mouseExited(MouseEvent arg0) {
	}

	public void mouseReleased(MouseEvent e) {
		if (playing)
			if (isDragging) {
				isDragging = false;
				boolean onItemSlot = false;
				Item itemInSlot;

				for (int i = 0; i < 8; i++) {
					if (c.stats.inventory.equipSlot[i].getArea().contains(
							e.getLocationOnScreen())) {
						itemInSlot = c.stats.inventory.getEquip(i);
						if (i == draggedItem.getSlot()) {
							onItemSlot = true;
							if (itemInSlot != null){
								if(previousItemP == -1)
								c.stats.inventory.setItem(itemInSlot,
										previousItemI, previousItemJ);
								else stash.setItem(itemInSlot,previousItemP, previousItemI,previousItemJ);
							}
							c.stats.inventory.setEquip(draggedItem, i);
						}
					}
					for (int j = 0; j < 8; j++) {
						if (c.stats.inventory.itemSlot[i][j].getArea()
								.contains(e.getLocationOnScreen())) {
							itemInSlot = c.stats.inventory.getItem(i, j);
							if (itemInSlot != null) {
								if (previousItemJ == -1) {
									if (itemInSlot.getSlot() == previousItemI) {
										c.stats.inventory.setEquip(itemInSlot,
												previousItemI);
										onItemSlot = true;
										c.stats.inventory.setItem(draggedItem,
												i, j);
									}
								} else if(previousItemP == -1){
									c.stats.inventory.setItem(itemInSlot,
											previousItemI, previousItemJ);
									onItemSlot = true;
									c.stats.inventory
											.setItem(draggedItem, i, j);
								} else {
									stash.setItem(itemInSlot,previousItemP,previousItemI,previousItemJ);
									onItemSlot = true;
									c.stats.inventory.setItem(draggedItem,i,j);
								}
							} else {
								onItemSlot = true;
								c.stats.inventory.setItem(draggedItem, i, j);
							}
						}
						
						if(stash.stashSlots[i][j].getArea().contains(e.getLocationOnScreen())){
							itemInSlot = stash.getItem(i, j);
							if (itemInSlot != null) {
								if (previousItemJ == -1) {
									if (itemInSlot.getSlot() == previousItemI) {
										c.stats.inventory.setEquip(itemInSlot,
												previousItemI);
										onItemSlot = true;
										stash.setItem(draggedItem,
												i, j);
									}
								} else if(previousItemP == -1){
									c.stats.inventory.setItem(itemInSlot,
											previousItemI, previousItemJ);
									onItemSlot = true;
									stash.setItem(draggedItem, i, j);
								} else {
									stash.setItem(itemInSlot,previousItemP,previousItemI,previousItemJ);
									onItemSlot = true;
									stash.setItem(draggedItem,i,j);
								}
							} else {
								onItemSlot = true;
								stash.setItem(draggedItem, i, j);
							}
						}
						
					}
				}
				Random rand = new Random();
				if(!stash.isOpen())
				if(!c.stats.inventory.getArea().contains(
						e.getLocationOnScreen())) {
					add(new Drop(draggedItem, new Point(c.getX()
							+ rand.nextInt(120 - 50), c.getY()
							+ c.getHeight() - 50)));
					onItemSlot = true;
				}

				if (!onItemSlot) {
					if (previousItemJ == -1) {
						c.stats.inventory.setEquip(draggedItem, previousItemI);
					} else {
						c.stats.inventory.setItem(draggedItem, previousItemI,
								previousItemJ);
					}
				}

				draggedItem = null;
				previousItemI = -1;
				previousItemJ = -1;
				previousItemP = -1;

			}
	}

	// ------------Classes-----------//

	public class Skill {

		public static final int ATTACK = 1, Arrow = 2, EnergyBall = 3,
				Smash = 4, DoubleArrow = 5, ExplosiveArrow = 6, FireBall = 7,
				MultiHit = 8, Explosion = 9;

		private Animation right = new Animation(), left = new Animation();
		private int skill, maxEnemiesHit, manaUsed = 0;
		private boolean active = false;
		private long skillTime, totalTime = 0;
		private boolean[] hit = new boolean[6];
		private boolean cLeft;
		private int[] hitTime = new int[6];
		private float[] dmgMult = { 1, 1, 1, 1, 1, 1 }, KBSpeed = new float[6];
		private Projectile[] skillProjectiles = new Projectile[8];
		private int shots = 0;
		private Point lastTarget;
		private Effect skillEffect;

		public Skill(int skill) {
			this.skill = skill;
			loadpics(skill);
		}

		public void skillStats() {
			switch (skill) {
			case ATTACK:
				manaUsed = -5 * (c.stats.atts[SPIRIT]+100)/100;
				dmgMult[0] = 1;
				maxEnemiesHit = 1;
				KBSpeed[0] = 0.18f;
				skillTime = 400;
				hitTime[0] = 200;
				break;
			case Smash:
				manaUsed = 15;
				dmgMult[0] = 1.1f + 0.12f * c.stats.skillLvls[skill];
				maxEnemiesHit = 3 + (int)(c.stats.skillLvls[skill]*0.4f);
				KBSpeed[0] = 0.18f;
				skillTime = 400;
				hitTime[0] = 200;
				break;
			case MultiHit:
				manaUsed = -2 * (c.stats.atts[SPIRIT]+100)/100;
				dmgMult[0] = dmgMult[1] = dmgMult[2] = 0.44f + 0.04f * c.stats.skillLvls[skill];
				maxEnemiesHit = 1;
				KBSpeed[0] = KBSpeed[1] = 0.05f;
				KBSpeed[2] = 0.10f;
				skillTime = 500;
				hitTime[0] = 140;
				hitTime[1] = 270;
				hitTime[2] = 400;
				break;
			case Arrow:
				manaUsed = 0;
				dmgMult[0] = 1;
				KBSpeed[0] = 0.10f;
				maxEnemiesHit = 1;
				hitTime[0] = 200;
				skillTime = 400;
				break;
			case DoubleArrow:
				manaUsed = 12;
				dmgMult[0] = dmgMult[1] = 0.74f + 0.06f * c.stats.skillLvls[skill];
				KBSpeed[0] = 0.05f;
				KBSpeed[1] = 0.10f;
				maxEnemiesHit = 1;
				skillTime = 500;
				hitTime[0] = 150;
				hitTime[1] = 300;
				break;
			case ExplosiveArrow:
				manaUsed = 15;
				dmgMult[0] = 0.84f + 0.06f * c.stats.skillLvls[skill];
				KBSpeed[0] = 0.17f;
				maxEnemiesHit = 3 + (int)(c.stats.skillLvls[skill]*0.4f);
				hitTime[0] = 200;
				skillTime = 400;
				break;
			case EnergyBall:
				manaUsed = 0;
				dmgMult[0] = 1;
				KBSpeed[0] = 0.10f;
				maxEnemiesHit = 1;
				hitTime[0] = 200;
				skillTime = 400;
				break;
			case FireBall:
				manaUsed = 8 + (int)(c.stats.skillLvls[skill]*0.4);
				dmgMult[0] = 1.34f + 0.11f * c.stats.skillLvls[skill];
				KBSpeed[0] = 0.13f;
				maxEnemiesHit = 1;
				hitTime[0] = 200;
				skillTime = 400;
				break;
			case Explosion:
				manaUsed = 10 + c.stats.skillLvls[skill];
				dmgMult[0] = 0.83f + 0.07f * c.stats.skillLvls[skill];
				KBSpeed[0] = 0.17f;
				maxEnemiesHit = 3+(int)(c.stats.skillLvls[skill] * 0.5f);
				hitTime[0] = 400;
				skillTime = 600;
				break;
			}
		}

		public int getMaxHits() {
			for(int i = 0; i < hit.length; i++){
				if(hitTime[i]==0) return i;
			}
			return 0;
		}

		public void addLvl() {
			c.stats.skillLvls[skill]++;
			skills[skill].skillStats();
		}

		public void removeLvl() {
			c.stats.skillLvls[skill]--;
			skills[skill].skillStats();
		}

		public int getLvl() {
			return c.stats.skillLvls[skill];
		}

		// load les images du sort
		private void loadpics(int skill) {
			//TODO : animations des skills
			
			Image[] attackL = new Image[10], attackR = new Image[10];
			switch (skill) {
			case Smash:
				attackL[0] = newImage("/smashL.png"); attackL[1] = newImage("/smashL2.png"); attackL[2] = newImage("/smashL3.png"); attackL[3] = newImage("/smashL4.png");
				left.addScene(attackL[0], 100);
				left.addScene(attackL[1], 100);
				left.addScene(attackL[2], 100);
				left.addScene(attackL[3], 150);
				
				attackR[0] = newImage("/smashR.png"); attackR[1] = newImage("/smashR2.png"); attackR[2] = newImage("/smashR3.png"); attackR[3] = newImage("/smashR4.png");
				right.addScene(attackR[0], 100);
				right.addScene(attackR[1], 100);
				right.addScene(attackR[2], 100);
				right.addScene(attackR[3], 150);
				
				break;
			case ATTACK:
				attackL[0] = newImage("/attackL.png"); attackL[1] = newImage("/attackL2.png"); attackL[2] = newImage("/attackL3.png"); attackL[3] = newImage("/attackL4.png");
				left.addScene(attackL[0], 100);
				left.addScene(attackL[1], 100);
				left.addScene(attackL[2], 100);
				left.addScene(attackL[3], 150);
				
				attackR[0] = newImage("/attackR.png"); attackR[1] = newImage("/attackR2.png"); attackR[2] = newImage("/attackR3.png"); attackR[3] = newImage("/attackR4.png");
				right.addScene(attackR[0], 100);
				right.addScene(attackR[1], 100);
				right.addScene(attackR[2], 100);
				right.addScene(attackR[3], 150);
				
				break;
			case MultiHit:
				
				attackL[0] = newImage("/attackL.png"); attackL[1] = newImage("/attackL2.png"); attackL[2] = newImage("/attackL3.png"); attackL[3] = newImage("/attackL4.png");
				left.addScene(attackL[0], 42);
				left.addScene(attackL[1], 42);
				left.addScene(attackL[2], 42);
				left.addScene(attackL[3], 42);
				
				attackR[0] = newImage("/attackR.png"); attackR[1] = newImage("/attackR2.png"); attackR[2] = newImage("/attackR3.png"); attackR[3] = newImage("/attackR4.png");
				right.addScene(attackR[0], 42);
				right.addScene(attackR[1], 42);
				right.addScene(attackR[2], 42);
				right.addScene(attackR[3], 42);
				
				break;
			case DoubleArrow:
			case Arrow:
			case ExplosiveArrow:
				Image walkleft1 = newImage("/walkleft1.png");
				left.addScene(walkleft1, 200);
				
				Image walkright1 = newImage("/walkright1.png");
				right.addScene(walkright1, 200);
				break;
			case FireBall:
			case EnergyBall:
			case Explosion:
				Image wizStandL = newImage("/bobwalkwizzardL1.png");
				left.addScene(wizStandL, 200);
			
				Image wizStandR = newImage("/bobwalkwizzardR1.png");
				right.addScene(wizStandR, 200);
				break;
			}
		}

		// update le sort
		public void update(long timePassed) {
			totalTime += timePassed;
			if(cLeft)
			left.update(timePassed);
			else right.update(timePassed);
			
			updateSkill();
			if (skillTime < totalTime) {
				active = false;
				c.setUsingSkill(false);
				lastTarget = null;
			}
		}

		// retourne si le sort est encore actif
		public boolean isActive() {
			return active;
		}

		// update le sort selon son num�ro
		public void updateSkill() {
			switch (skill) {
			//TODO si on fait un nouveau skill
			default:
				attack();
				break;
				
			case Arrow:
			case DoubleArrow:
			case ExplosiveArrow:
			case EnergyBall:
			case FireBall:
				Projectiles();
				break;
			}

		}

		public void Projectiles() {
			for (int i = 0; i < hit.length; i++) {

				if (hitTime[i] != 0)
					if (totalTime >= hitTime[i] && shots == i) {
						skillProjectiles[i] = null;
						skillProjectiles[i] = new Projectile(
								newAnimation(skill), this, i);
						skillProjectiles[i].setY(getY() + getHeight() / 2);
						if (cLeft){
							skillProjectiles[i].setX(getX() + getWidth() / 3 - skillProjectiles[i].getWidth());
							skillProjectiles[i].setXVelocity(-1);
						} else {
							skillProjectiles[i].setX(getX() + 2 * getWidth() / 3);
							skillProjectiles[i].setXVelocity(1);
						}

						Point target = getTarget(getAimArea());
						if (target == null)
							target = lastTarget;
						if (target != null)
							skillProjectiles[i]
									.setYVelocity((float) ((target.getY() - (skillProjectiles[i]
											.getY() + skillProjectiles[i]
											.getHeight() / 2)) / Math.abs(target
											.getX()
											- (skillProjectiles[i].getX() + skillProjectiles[i]
													.getWidth() / 2))));
						lastTarget = target;
						skillProjectiles[i].activate();
						add(skillProjectiles[i]);
						shots++;
					}
			}
		}

		public Animation newAnimation(int skill) {
			//TODO Les animations de projectiles
			Animation a = new Animation();
			switch (skill) {
			case ExplosiveArrow:
			case Arrow:
			case DoubleArrow:
				Image fleche;
				if (cLeft) {
					fleche = newImage("/flecheG.png");
				} else {
					fleche = newImage("/flecheD.png");
				}
				a.addScene(fleche, 100);
				return a;
			case EnergyBall:
				Image energyBall = newImage("/energyball.png");
				a.addScene(energyBall, 100);
				return a;
			case FireBall:
				Image fireBall = newImage("/fireball.png");
				Image fireBall2 = newImage("/fireball2.png");
				a.addScene(fireBall, 70);
				a.addScene(fireBall2, 70);
				return a;
			}
			return null;
		}
		// retourne le multiplicateur de d�gat du sort
		public float getDmgMult(int hit) {
			return dmgMult[hit];
		}

		// retourne la vitesse de recul du monstre lorsqu'il est frapp� par le
		// sort
		public float getKBSpeed(int hit) {
			return KBSpeed[hit];
		}

		// retourne le nombre de monstres que le sort peut tapper
		public int getMaxEnemiesHit() {
			return maxEnemiesHit;
		}

		// retourne l'animation du sort
		public Animation getAnimation() {
			if(cLeft)
			return left;
			else return right;
		}

		// attaque de base
		public void attack() {

			for (int i = 0; i < hit.length; i++) {
				if (hitTime[i] != 0)
					if ((hitTime[i] <= totalTime) && (hit[i] == false)) {
						hit[i] = true;
						hit(this, i);
					}
			}
		}

		// retourne la zone du sort
		public Rectangle getArea() {
			switch (skill) {
			case Explosion:
				return skillEffect.getArea();
			case ExplosiveArrow:
				if (cLeft)
					return new Rectangle(skillProjectiles[0].getX() - 50,
							skillProjectiles[0].getY() - 50, 200, 100);
				else
					return new Rectangle(skillProjectiles[0].getX() + 50,
							skillProjectiles[0].getY() - 50, 200, 100);
			default:
				if (cLeft)
					return (new Rectangle(getX()+10, getY(), getWidth() - 70,
							getHeight()));
				else
					return new Rectangle(getX() + 60, getY(), getWidth() - 70,
							getHeight());
			}
		}

		public Rectangle getAimArea() {
			switch (skill) {
			default:
				if (cLeft) {
					return new Rectangle(getX() - 450, getY(), 450, getHeight());
				} else {
					return new Rectangle(getX() + getWidth(), getY(), 450,
							getHeight());
				}
			}
		}

		public int getX() {
			if (cLeft)
				return c.getX() - getWidth() + c.getWidth();
			else
				return c.getX();
		}

		public int getY() {
			return c.getY() - c.getHeight() + getHeight();
		}

		public int getHeight() {
			return getImage().getHeight(null);
		}

		public int getWidth() {
			return getImage().getWidth(null);
		}

		// retourne l'image du sort
		public Image getImage() {
			return getAnimation().getImage();
			
		}

		// active le sort si le personnage � assez de mana
		public void activate() {
			left.start(); right.start();
			
			int mana = manaUsed;
			if(mana < 0) mana = 0;
			
			if (mana <= c.getMana()) {
				if (!active) {
					c.setMana(c.getMana() - mana);
					cLeft = c.isFacingLeft();
					totalTime = 0;
					for (int i = 0; i < hit.length; i++)
						hit[i] = false;
					shots = 0; active = true;
					
					skillEffects();
				}
			} else {
				switch(c.stats.classe){
				case FIGHTER : skills[ATTACK].activate(); break;
				case MAGE : skills[EnergyBall].activate(); break;
				case ARCHER : skills[Arrow].activate();break;
				}
			}
		}
		
		private void skillEffects(){
			Animation a = new Animation();
			switch(skill){
			case Explosion:
				for (int j = 1; j <= 10; j++) {
					a.addScene(newImage("/explos" + j + ".png"), 60);
				}
				skillEffect = new Effect(new Point(c.getX() + c.getWidth() / 2
						- 250, c.getY() - 80), a, 500, true);
				add(skillEffect);
				break;
			}
		}

		public void hit(Skill skill, int hit) {
			monsters = getMonsters();
			int hits = 1;

			for (int i = 0; i < monsters.size()
					&& hits <= getMaxEnemiesHit(); i++) {
					if (monsters.get(i).isAlive())
						if (monsters.get(i).getArea().intersects(getArea())) {
							damageMonster(monsters.get(i),
									c.getDamage(monsters.get(i),
										getDmgMult(hit)),
										getKBSpeed(hit));
							if(manaUsed < 0) { 
								if(c.getMana()-manaUsed > c.maxMana) c.setMana(c.maxMana);
								else
								c.setMana(c.getMana()-manaUsed);
								}
							hits++;
						}
			}
		}

	}

	public class PassiveSkill {

		public static final int WandMastery = 0, MopMastery = 1,
				BowMastery = 2;

		public int skill;
		private int[] statBonus = { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,
				0, 0, 0, 0, 0, 0, 0 };

		public PassiveSkill(int i) {
			skill = i;
		}

		public int getLvl() {
			return c.stats.passiveSkillLvls[skill];
		}

		public void skillStats() {
			switch (skill) {
			case WandMastery:
			case MopMastery:
			case BowMastery:
				statBonus[WATK] = (int) Math.floor(getLvl() / 2);
				statBonus[MASTERY] = getLvl() * 4;
			}
		}

		public int getSkillStat(int i) {
			return statBonus[i];
		}

		public void removeLvl() {
			c.stats.passiveSkillLvls[skill]--;
			c.loadStats();
		}

		public void addLvl() {
			c.stats.passiveSkillLvls[skill]++;
			c.loadStats();
		}

	}

	public class Character extends Sprite implements Serializable {

		private static final long serialVersionUID = 3917840299594769183L;
		private int defense, baseMastery = 20, dir, maxLife, maxMana,
				critDamage, wATK, mastery;
		public CharacterStats stats = new CharacterStats(this);
		private float critChance;
		private long manaRegen, lifeRegen;
		public static final int LEFT = -1, RIGHT = 1;
		private boolean invincible = false, canMove = true;
		private float timer = 0, limit = 0;
		private boolean left = false;
		private int[] atts = new int[4];
		private int[] skillKeys = new int[256];
		private boolean usingSkill = false;
		private boolean alive = true;
		public Ladder onLadder = null;
		public int pressingClimb;
		private Clip hitSound, dieSound;
		public boolean inWater;
		public int currentAnimation;
		
		public Character(int classe) {
			super();
			loadStats();
			stats.life = maxLife;
			stats.mana = maxMana;
			stats.classe = classe;
			for (int i = 0; i < skills.length; i++)
				skills[i] = null;
			for (int i = 0; i < passives.length; i++)
				passives[i] = null;

			switch (classe) {
			case MAGE:
				passives[PassiveSkill.WandMastery] = new PassiveSkill(
						PassiveSkill.WandMastery);
				skills[Skill.EnergyBall] = new Skill(Skill.EnergyBall);
				skills[Skill.Explosion] = new Skill(Skill.Explosion);
				skills[Skill.FireBall] = new Skill(Skill.FireBall);
				skillKeys[KeyEvent.VK_CONTROL] = Skill.EnergyBall;
				skillKeys[KeyEvent.VK_SHIFT] = Skill.Explosion;
				skillKeys[KeyEvent.VK_Z] = Skill.FireBall;
				break;

			case FIGHTER:
				passives[PassiveSkill.MopMastery] = new PassiveSkill(
						PassiveSkill.MopMastery);
				skills[Skill.ATTACK] = new Skill(Skill.ATTACK);
				skills[Skill.Smash] = new Skill(Skill.Smash);
				skills[Skill.MultiHit] = new Skill(Skill.MultiHit);
				skillKeys[KeyEvent.VK_CONTROL] = Skill.ATTACK;
				skillKeys[KeyEvent.VK_SHIFT] = Skill.Smash;
				skillKeys[KeyEvent.VK_Z] = Skill.MultiHit;
				break;

			case ARCHER:
				passives[PassiveSkill.BowMastery] = new PassiveSkill(
						PassiveSkill.BowMastery);
				skills[Skill.Arrow] = new Skill(Skill.Arrow);
				skills[Skill.ExplosiveArrow] = new Skill(Skill.ExplosiveArrow);
				skills[Skill.DoubleArrow] = new Skill(Skill.DoubleArrow);
				skillKeys[KeyEvent.VK_CONTROL] = Skill.Arrow;
				skillKeys[KeyEvent.VK_SHIFT] = Skill.ExplosiveArrow;
				skillKeys[KeyEvent.VK_Z] = Skill.DoubleArrow;
				break;
			}
		}
		
		public void setAnimation(int i){
			super.setAnimation(charAnims[i]);
			currentAnimation = i;
		}
		
		public String getResourceName(){
			String info = "";
			switch(c.stats.classe){
			case FIGHTER : info+="fury";break;
			case MAGE : info+="mana";break;
			case ARCHER : info+="precision";break; 
			}
			return info;
		}
		
		public void setClimbing(int i){
			if(canMove)
			if(onLadder!=null)setYVelocity(-i*0.35f);
		}
		
		public void isPressingClimb(int i){
			pressingClimb = i;
		}
		
		public void climb(int i){
			if(canMove()){
			for(Ladder ladder : ladders) if(ladder!=null)
				if(c.getArea().intersects(ladder)) c.onLadder = ladder;
				if(c.onLadder != null) c.setClimbing(i);
			}
		}
		
		public float getStat(int stat) {
			switch (stat) {
			case SPIRIT:
			case POW:
			case AGI:
			case VIT:
				return atts[stat];
			case CRITDMG:
				return critDamage;
			case CRIT:
				return critChance;
			case MASTERY:
				return mastery;
			}
			return 0;
		}

		public void respawn() {
			invincible = false;
			canMove = true;
			stats.life = maxLife;
			stats.mana = maxMana;
			setX(map.spawnPoint.x);
			setY(map.spawnPoint.y);
			X = map.spawnCamera.x;
			Y = map.spawnCamera.y;
			alive = true;
		}

		public void addStat(int stat) {
			stats.atts[stat]++;
			loadStats();
			if (stat == VIT)
				stats.life = maxLife;
			if (stat == SPIRIT)
				stats.mana = maxMana;
		}

		public void loadStats() {

			if (playing)
				for (PassiveSkill passive : passives)
					if (passive != null)
						passive.skillStats();

			for (int i = 0; i < 4; i++) {
				atts[i] = stats.atts[i] + stats.inventory.getStat(i) + stats.inventory.getStat(ALLSTATS);
				for (PassiveSkill passive : passives)
					if (passive != null)
						atts[i] += passive.getSkillStat(i);
			}

			defense = stats.lvl + stats.inventory.getDefense();
						
			maxLife = (5 * stats.lvl + 95)* (atts[VIT]+100)/100;
			if (stats.life > maxLife)
				stats.life = maxLife;
			
			if(stats.classe == MAGE)maxMana = 5 * (stats.lvl-1); else maxMana = 0;
			maxMana += 100;
			maxMana = maxMana * (atts[SPIRIT]+100)/100;
			if (stats.mana > maxMana)
				stats.mana = maxMana;
			
			critChance = 5 + stats.inventory.getStat(CRIT);
			
			critDamage = 50 + stats.inventory.getStat(CRITDMG);
			
			wATK = stats.inventory.getDamage();
			if (wATK == 0) wATK = 2;
			
			mastery = baseMastery + stats.inventory.getStat(MASTERY);

			for (PassiveSkill passive : passives)
				if (passive != null) {
					defense += passive.getSkillStat(DEFENSE);
					critChance += passive.getSkillStat(CRIT);
					critDamage += passive.getSkillStat(CRITDMG);
					wATK += passive.getSkillStat(WATK);
					mastery += passive.getSkillStat(MASTERY);
				}

		}

		public float getCritChance() {
			return critChance;
		}

		public int getCritDamage() {
			return critDamage;
		}

		public void exp(int exp) {
			this.stats.exp += exp;
			if (this.stats.exp >= expToNextLvl())
				lvlup();
		}

		public int expToNextLvl() {
			return (int) (10 * Math.pow(1.4, stats.lvl));

		}

		public void lvlup() {

			try {
				Clip lvlupsound;
				AudioInputStream music = AudioSystem
						.getAudioInputStream(getClass()
								.getResource("steel.wav"));
				lvlupsound = AudioSystem.getClip();
				lvlupsound.open(music);
				lvlupsound.start();
			} catch (Exception e) {
				e.printStackTrace();
			}

			Animation lvlup = new Animation();
			for (int k = 1; k <= 4; k++)
				lvlup.addScene(newImage("/bob_lvlup" + k + ".png"), 140);
			lvlup.addScene(newImage("/bob_lvlup5.png"), 600);
			for (int k = 4; k >= 1; k--)
				lvlup.addScene(newImage("/bob_lvlup" + k + ".png"), 140);

			add(new Effect(new Point((int) getArea().getX() - 40,
					(int) getArea().getY() - 50), lvlup, 1720));

			stats.exp -= expToNextLvl();
			stats.lvl++;
			stats.skillpts += 2;
			stats.attpts += 5;
			loadStats();
			stats.life = maxLife;
			stats.mana = maxMana;
		}

		public int getSkillPts() {
			return stats.skillpts;
		}

		public void removeSkillPt() {
			stats.skillpts--;
		}

		public int getStatPts() {
			return stats.attpts;
		}

		public void removeStatPt() {
			stats.attpts--;
		}

		// retourne si le personnage est en vie
		public boolean isAlive() {
			return alive;
		}

		// retourne si le personnage utilise un sort
		public void setUsingSkill(boolean bool) {
			usingSkill = bool;
		}

		// retourne si le personnage "regarde" � gauche
		public boolean isFacingLeft() {
			return left;
		}

		public void setFacingLeft(boolean left) {
			this.left = left;
		}

		// acc�de ou modifie la mana du personnage
		public int getMana() {
			return stats.mana;
		}

		public int getMaxMana() {
			loadStats();
			return maxMana;
		}

		public void setMana(int mana) {
			stats.mana = mana;
		}

		public Rectangle getBase() {
			return new Rectangle(getX() + 25, getY() + getHeight() - 15,
					getWidth() - 50, 20);
		}

		public Rectangle getTop() {
			return new Rectangle(getX() + 20, getY() - 9, getWidth() - 40, 18);
		}

		public Rectangle getArea() {
			return new Rectangle(getX()+15, getY(), getWidth()-30, getHeight());
		}

		public Rectangle getLeftSide() {
			return new Rectangle(getX() +8, getY() + 13, 4, getHeight() - 24);
		}

		public Rectangle getRightSide() {
			return new Rectangle(getX() + getWidth() - 12, getY() + 13, 4,
					getHeight() - 24);
		}

		// set la direction du mouvement du personnage
		public void move(int dir) {
			this.dir = dir;
		}

		public void stopMoving() {
			this.dir = 0;
		}

		// saute
		public void jump() {
			onLadder = null;
			if(inWater) setYVelocity(-0.9f);
			else setYVelocity(-1.5f);
		}

		// update la mana, l'invincibilit� et le mouvement du personnage
		public void update(long timePassed) {
			
			if(onLadder==null)
			if(pressingClimb != 0) c.climb(pressingClimb);
			
			c.setClimbing(pressingClimb);
			
			if (invincible) {
				timer += timePassed;
				if (timer >= limit) {
					invincible = false;
					timer = 0;
				}
			}
			
			if(!canMove && inWater) canMove = true;
			
			if (canMove())
				if(inWater) setXVelocity(dir*0.29f);
				else setXVelocity(dir * 0.38f);
			
			if (usingSkill && getYVelocity() == 0)
				setXVelocity(0);
			
			if(onLadder != null)
			if(onLadder.fixedX){
				setX((float)(onLadder.getX()+onLadder.getWidth()/2-getWidth()/2));
				setXVelocity(0);
			}
			
			if(stats.classe == MAGE){
			if (stats.mana < maxMana)
				manaRegen += timePassed;
				if (manaRegen >= ((1000 - stats.lvl*5) *100/(100+atts[SPIRIT]))) {
					stats.mana++;
					manaRegen = 0;
				}
			} else if(stats.classe == ARCHER){
				if(stats.mana < maxMana){
					manaRegen += timePassed;
					if((c.getXVelocity()==0 && c.getYVelocity()==0) || !c.canMove())
						manaRegen += 5*timePassed;
				}
				if(manaRegen >= (1200 * 100/(100+atts[SPIRIT]))){
					stats.mana++;
					manaRegen = 0;
				}
			}

			if (stats.life < maxLife)
				lifeRegen += timePassed;
			if (lifeRegen >= (2000 * 100/(100+atts[VIT]))) {
				stats.life++;
				lifeRegen = 0;
			}
			super.update(timePassed);
		}

		// fais tomber le presonnage
		public void fall(long timePassed) {
			if(inWater) {
				if(getYVelocity() < 0.8f)
				setYVelocity(getYVelocity() + (0.0020f * timePassed));
			}
			else if (getYVelocity() < 1) {
				setYVelocity(getYVelocity() + (0.0049f * timePassed));
			}
		}

		// rend le personnage invincible
		public void setInvincible(float time) {
			limit = time;
			timer = 0;
			invincible = true;
		}

		public boolean isInvincible() {
			return invincible;
		}

		// retourne/change si le personnage peut bouger
		public boolean canMove() {
			if(inWater) return true;
			if(onLadder!=null) return true;
			return canMove;
		}

		public void canMove(boolean canMove) {
			this.canMove = canMove;
		}

		// fait des d�gats au personnage
		public void damageChar(int dmg) {
			if(isAlive())
			if (!isInvincible()){
				if (dmg > 0) {
					stats.life -= dmg;
					if(stats.classe == FIGHTER)
					if((stats.mana += 3 * (100 + atts[SPIRIT])/100) > maxMana) stats.mana = maxMana;
				}
			if (stats.life <= 0){
				stats.life = 0;
				alive = false;
				stats.exp -= expToNextLvl()*0.05;
				if(stats.exp < 0) stats.exp = 0;
				if(dieSound != null) dieSound.start();
			} else if(hitSound!= null) hitSound.start();
			onLadder = null;
			}
		}

		// retourne la vie
		public int getLife() {
			return stats.life;
		}

		public int getMaxLife() {
			return maxLife;
		}

		// retourne la d�fence
		public int getDefense() {
			return defense;
		}

		// retourne les d�gats que le personnage fait � un certain monstre
		public int getDamage(Monster m, float dmgMult) {
			Random rand = new Random();
			if(m.avoid > rand.nextInt(100)+atts[AGI]) return 0;
			int dmast = rand.nextInt(100 - mastery) + mastery;

			float dmg = (((wATK * (atts[0] + 100) / 100) * dmgMult));
			dmg = dmg * dmast / 100;
			dmg = (float) (dmg * (1 - m.getDefense()
					/ (m.getDefense() + 22 * Math.pow(1.1, stats.lvl))));
			if (dmg < 1)
				dmg = 1;
			return (int) dmg;
		}

		public double getDamageReduction() {
			return 100 * c.getDefense()
					/ (c.getDefense() + 22 * Math.pow(1.1, stats.lvl));
		}

		public int getMinDamage() {
			float dmg = ((wATK * (atts[0] + 100) / 100));
			dmg = dmg * mastery / 100;
			if (dmg < 1)
				dmg = 1;
			return (int) dmg;
		}

		public int getMaxDamage() {
			return ((wATK * (atts[0] + 100) / 100));
		}

		// retourne les raccourcis clavier des sorts
		public int[] getSkillKeys() {
			return skillKeys;
		}

		public void drop(Item item) {
			Random rand = new Random();
			if (item != null)
				add(new Drop(item, new Point(getX()
						+ rand.nextInt(getWidth() - 50), getY() + getHeight()
						- 50)));
		}

	}

	

	public class Drop {

		private Item item;
		private float x, y;
		private boolean active = true;
		private boolean up = true;
		private long timer = 45000, moveTimer = 0;

		public Drop(Item item, Point position) {
			this.item = item;
			x = position.x;
			y = position.y;
			active = true;
		}

		public void update(long timePassed) {
			moveTimer += timePassed;
			if (moveTimer >= 800) {
				moveTimer = 0;
				up = !up;
			}

			if (up)
				y -= timePassed * 0.02f;
			else
				y += timePassed * 0.02f;

			timer -= timePassed;
			if (timer <= 0)
				delete();
		}

		public boolean isActive() {
			return active;
		}

		public void delete() {
			active = false;
		}

		public Item getItem() {
			return item;
		}

		public Rectangle getArea() {
			return new Rectangle((int) x, (int) y, 50, 50);
		}
	}


	public class StatMenu {

		public StatButton[] statButtons = new StatButton[4];
		private Rectangle area;
		private boolean open = false;

		public StatMenu() {
			area = new Rectangle(100, 100, 450, 400);
			for (int i = 0; i < 4; i++)
				statButtons[i] = new StatButton(new Point(110, 110 + i * 100),
						i);

		}

		public void toggle() {
			open = !open;
			if (c.stats.inventory.isOpen())
				c.stats.inventory.toggle();
			if(stash.isOpen())
				stash.toggle();
		}

		public Rectangle getArea() {
			return area;
		}

		public boolean isOpen() {
			return open;
		}
	}

	public class StatButton {

		private Rectangle area;
		private int stat;
		private String text, info;
		private Point textPosition;

		public StatButton(Point position, int stat) {
			area = new Rectangle(position.x, position.y, 50, 50);
			textPosition = new Point(position.x + 12, position.y + 40);
			this.stat = stat;
			switch (stat) {
			case SPIRIT:
				text = "SPI";
				break;
			case POW:
				text = "POW";
				break;
			case AGI:
				text = "AGI";
				break;
			case VIT:
				text = "VIT";
				break;
			}
		}

		public Rectangle getArea() {
			return area;
		}

		public Point getTextPosition() {
			return textPosition;
		}

		public String getText() {
			return text;
		}

		public String getInfo() {
			switch (stat) {
			case SPIRIT:
				info = "Spirit increases total " + c.getResourceName() + " and " + c.getResourceName()+" regen by 1%";
				break;
			case POW:
				info = "Power increases all damage by 1%";
				break;
			case AGI:
				info = "Agility increases your chance to hit by 1%";
				break;
			case VIT:
				info = "Vitality increases your hp and healing/regen by 1%";
				break;
			}
			return info;
		}

		public void activate() {
			if (c.getStatPts() > 0) {
				c.removeStatPt();
				c.addStat(stat);
			}
		}
	}

	public class SaveMenu {
		public SaveButton[] saveButtons = new SaveButton[4];

		public SaveMenu() {
			for (int i = 0; i < 4; i++) {
				saveButtons[i] = new SaveButton(i);
			}
		}

	}

	public class SaveButton {
		private int slot;
		private Rectangle area, delete;
		private CharacterStats saveStats;
		public Point infoPos;

		public SaveButton(int i) {
			slot = i;
			area = new Rectangle(800, 200 + 125 * i, 400, 100);
			delete = new Rectangle(1200, 200 + 125 * i, 100, 100);
			infoPos = new Point(825, 250 + 125 * i);
			refresh();
		}

		public void refresh() {
			saveStats = loadStats(slot);
		}

		public int getSlot() {
			return slot;
		}

		public Rectangle getArea() {
			return area;
		}

		public Rectangle getDelete() {
			return delete;
		}

		public void activate() {
			load(slot);
		}

		public void deleteSave() {
			File saveFile = new File("C:/Jeu/Save" + slot + ".sav");
			saveFile.delete();
			refresh();
		}

		public String info() {
			if (saveStats != null) {
				String info = "";
				switch (saveStats.classe) {
				case MAGE:
					info += "Mage ";
					break;
				case FIGHTER:
					info += "Fighter ";
					break;
				case ARCHER:
					info += "Archer ";
					break;
				}
				info += "lvl " + saveStats.lvl;
				info += ", " + saveStats.atts[POW] + " POW";
				info += ", " + saveStats.atts[AGI] + " AGI";
				info += ", " + saveStats.atts[SPIRIT] + " SPI";
				info += ", " + saveStats.atts[VIT] + " VIT";
				return info;
			} else
				return "New Game";
		}
	}

	public class ClassMenu {

		public ClassButton[] classButtons = new ClassButton[3];

		public ClassMenu() {
			for (int i = 0; i < 3; i++)
				classButtons[i] = new ClassButton(i);
		}
	}

	public class ClassButton {

		private Rectangle area;
		private int classe;
		public Point infoPos;

		public ClassButton(int i) {
			this.classe = i;
			area = new Rectangle(300 + i * 225, 300, 200, 200);
			infoPos = new Point(360 + i * 225, 420);
		}

		public Rectangle getArea() {
			return area;
		}

		public int getClasse() {
			return classe;
		}

		public String info() {
			switch (classe) {
			case MAGE:
				return "Mage";
			case FIGHTER:
				return "Fighter";
			case ARCHER:
				return "Archer";
			}
			return "";
		}

		public void activate() {
			initChar(classe);
			c.setX(50);
			c.setY(50);
			loadMap(0);
			X = 0;
			Y = 0;
			classSelect = false;
			playing = true;
		}

	}

	public class SkillMenu {

		public SkillButton[] skillButtons = new SkillButton[3];
		private boolean open;
		private int x = 600, y = 100, width = 600, height = 425;

		public SkillMenu() {
			refresh();
		}

		public void refresh() {

			int i = 0;
			for (Skill skill : skills) {
				if (skill != null && skill.skill > 3) {
					skillButtons[i] = new SkillButton(skill.skill, false);
					skillButtons[i].setArea(new Rectangle(x + width - 140, y
							+ 30 + i * 125, 100, 100));
					skillButtons[i].namePos = new Point(x + width - 110, y + 85
							+ i * 125);
					i++;
				}
			}
			for (PassiveSkill passive : passives) {
				if (passive != null) {
					skillButtons[i] = new SkillButton(passive.skill, true);
					skillButtons[i].setArea(new Rectangle(x + width - 140, y
							+ 30 + i * 125, 100, 100));
					skillButtons[i].namePos = new Point(x + width - 110, y + 85
							+ i * 125);
					i++;
				}
			}
		}

		public Rectangle getArea() {
			return new Rectangle(x, y, width, height);
		}

		public boolean isOpen() {
			return open;
		}

		public void toggle() {
			refresh();
			open = !open;
			if (c.stats.inventory.isOpen())
				c.stats.inventory.toggle();
			if(stash.isOpen())
				stash.toggle();
		}

	}

	public class SkillButton {
		private int skill;
		private Rectangle area;
		private Point namePos;
		boolean passive;

		public SkillButton(int i, boolean passive) {
			this.skill = i;
			this.passive = passive;
			if (passive)
				passives[i].skillStats();
			else
				skills[i].skillStats();
		}

		public Point getNamePos() {
			return namePos;
		}

		public void setArea(Rectangle area) {
			this.area = area;
		}

		public Rectangle getArea() {
			return area;
		}

		public int getSkill() {
			return skill;
		}

		public String getName() {
			if (passive) {
				switch (skill) {
				case PassiveSkill.WandMastery:
					return "WandMastery";
				case PassiveSkill.MopMastery:
					return "MopMastery";
				case PassiveSkill.BowMastery:
					return "BowMastery";
				}
			} else
				switch (skill) {
				case Skill.DoubleArrow:
					return "DoubleArrow";
				case Skill.ExplosiveArrow:
					return "ExplosiveArrow";
				case Skill.Explosion:
					return "Explosion";
				case Skill.MultiHit:
					return "MultiHit";
				case Skill.FireBall:
					return "FireBall";
				case Skill.Smash:
					return "Smash";
				}
			return "";
		}

		public String getInfo() {

			String info = "";

			if (passive) {
				if (passives[skill].getLvl() == 0)
					return "Not acquired yet.";
				switch (skill) {
				case PassiveSkill.WandMastery:
				case PassiveSkill.MopMastery:
				case PassiveSkill.BowMastery:
					info += "Increases weapon damage by "
							+ passives[skill].getSkillStat(WATK)
							+ " and weapon mastery by "
							+ passives[skill].getSkillStat(MASTERY);
				}
			} else {
				if (skills[skill].getLvl() == 0)
					return "Not acquired yet.";
				info += "Deals ";
				info += new DecimalFormat("#.##").format(skills[skill]
						.getDmgMult(0)) + " times your damage to ";
				info += skills[skill].getMaxEnemiesHit();
				if (skills[skill].getMaxEnemiesHit() == 1)
					info += " enemy.";
				else
					info += " enemies.";
				info += "Hits " + skills[skill].getMaxHits();
				if (skills[skill].getMaxHits() == 1)
					info += " time.";
				else
					info += " times.";
			}

			return info;
		}

		public String getNextLevel() {

			String info = "";

			if (passive) {
				if (passives[skill].getLvl() >= 10)
					return "Max level";

				passives[skill].addLvl();

				switch (skill) {
				case PassiveSkill.WandMastery:
				case PassiveSkill.MopMastery:
				case PassiveSkill.BowMastery:
					info += "Next level weapon damage "
							+ passives[skill].getSkillStat(WATK)
							+ ", weapon mastery "
							+ passives[skill].getSkillStat(MASTERY);
				}

				passives[skill].removeLvl();
			} else {

				if (skills[skill].getLvl() >= 10)
					return "Max level";

				skills[skill].addLvl();

				info += "Next lvl damage : ";
				info += new DecimalFormat("#.##").format(skills[skill]
						.getDmgMult(0));
				info += ", enemies hit : " + skills[skill].getMaxEnemiesHit();
				info += ", hits : " + skills[skill].getMaxHits();

				skills[skill].removeLvl();
			}

			return info;
		}

		public void activate() {
			if (passive) {
				if (passives[skill].getLvl() < 10) {
					if (c.stats.skillpts > 0) {
						c.stats.skillpts--;
						passives[skill].addLvl();
					}
				}
			} else {

				if (skills[skill].getLvl() < 10) {
					if (c.stats.skillpts > 0) {
						c.stats.skillpts--;
						skills[skill].addLvl();
					}
				}
			}
		}

	}

	public class Projectile extends Sprite {

		private boolean active;
		private int timer = 0;
		public Skill skill;
		public int number = 0;

		public Projectile(Animation a, Skill skill) {
			setAnimation(a);
			active = true;
			this.skill = skill;
		}

		public Projectile(Animation a, Skill skill, int number) {
			this(a, skill);
			this.number = number;
		}

		public Rectangle getArea() {
			return new Rectangle(getX(), getY(), getWidth(), getHeight());
		}

		public boolean isActive() {
			return active;
		}

		public void delete() {
			active = false;
		}

		public void activate() {
			active = true;
		}

		public void update(long timePassed) {
			super.update(timePassed);
			timer += timePassed;
			if (timer >= 400)
				delete();

		}

	}

	public class Effect extends Sprite {

		private long totalTime;
		private boolean active, follow = false;
		private int previousCX, previousCY;

		public Effect(Point p, Animation a, long totalTime) {
			super();

			super.setX((float) p.getX());
			super.setY((float) p.getY());
			super.setAnimation(a);
			this.totalTime = totalTime;
			active = true;

		}

		public Effect(Point p, Animation a, long totalTime, boolean follow) {
			super();
			this.follow = follow;
			super.setX((float) p.getX());
			super.setY((float) p.getY());
			super.setAnimation(a);
			this.totalTime = totalTime;
			active = true;

			if (follow) {
				previousCX = c.getX();
				previousCY = c.getY();
			}

		}

		public void update(long timePassed) {
			super.update(timePassed);
			totalTime -= timePassed;
			if (totalTime <= 0)
				active = false;

			if (follow) {
				setX(getX() + c.getX() - previousCX);
				previousCX = c.getX();
				setY(getY() + c.getY() - previousCY);
				previousCY = c.getY();
			}

		}

		public Rectangle getArea() {
			return new Rectangle(super.getX(), super.getY(), super.getWidth(),
					super.getHeight());
		}

		public boolean isActive() {
			return active;
		}

	}

}
