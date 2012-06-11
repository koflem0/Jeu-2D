import java.awt.*;

public class Sprite {
	
	private Animation a;
	private float x, y, vx, vy;
	
	//Constructor
	public Sprite(){}
	
	//change la position selon la vitesse 
	public synchronized void update(long timePassed){
		float nextX = vx * timePassed;
		if(nextX <= 80 && nextX >= -80)
			setX(x + nextX);
		float nextY = vy * timePassed;
		if(nextY <= 80 && nextY >= -80)
			setY(y + nextY);
		a.update(timePassed);
	}
	
	//retourne la position
	public int getX(){return Math.round(x);}
	public int getY() {return Math.round(y);}
		
	
	//change la position
	public void setX(float x){this.x = x;}
	public void setY(float y){this.y = y;}
	
	
	//retourne la taille du sprite
	public int getWidth(){if(a != null)return a.getImage().getWidth(null); else return 0;}
	public int getHeight(){if(a != null) return a.getImage().getHeight(null); else return 0;}
	
	
	//retourne la vitesse du sprite
	public float getXVelocity(){return vx;}
	public float getYVelocity(){return vy;}
	
	
	//change la vitesse du sprite
	public void setXVelocity(float vx){this.vx = vx;}
	public void setYVelocity(float vy){this.vy = vy;}
	
	
	//retourne l'image actuelle du sprite
	public Image getImage(){return a.getImage();}
	
	
	//change/retourne l'animation
	public void setAnimation(Animation a){this.a = a;}
	
	public Animation getAnimation(){return a;}
}
