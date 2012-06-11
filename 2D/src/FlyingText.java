import java.awt.Color;


public class FlyingText extends Sprite {

	
	private String text;
	private Main.Character c = null;
	private Main.Monster m = null;
	private float moveUp = 0;
	private boolean active;
	private Color color = Color.YELLOW;
	
	 public FlyingText(String t, Main.Character c){
		 active = true;
		 text = t;
		 this.c = c;
		 setX(c.getX() + c.getWidth()/3);
		 setY(c.getY() + c.getHeight()/2 - moveUp);
		 setYVelocity(0.1f);
	 }
	 
	 public FlyingText(){
		 active = false;
	 }
	 
	 public FlyingText(String t, Main.Monster m, boolean crit){
		 if(crit) color = Color.RED;
		 active = true;
		 text = t;
		 this.m = m;
		 setX(m.getX() + m.getWidth()/3);
		 setY(m.getY() + m.getHeight()/2 - moveUp);
		 setYVelocity(0.1f);
	 }
	 
	 
	//fais bouger le texte
	 public void update(long timePassed){
			moveUp += timePassed;
		 if(moveUp >= 1000){
			 active = false;
		 }
		 	if(c!=null){
			 setX(c.getX() + c.getWidth()/3);
			 setY(c.getY() + c.getHeight()/2 - moveUp * getYVelocity());
		 } else if(m != null){
			 setX(m.getX() + m.getWidth()/3);
			 setY(m.getY() + m.getHeight()/2 - moveUp * getYVelocity());
		 }
		}
	 public Color getColor(){return color;}
	 
	 public String getText(){
		 return text;
	 }
	 
	 public boolean isActive(){
		 return active;
	 }
	 
	 
}
