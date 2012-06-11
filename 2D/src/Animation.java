import java.awt.*;
import java.util.*;

public class Animation {

	private ArrayList<OneScene> scenes;
	private int sceneIndex;
	private long movieTime, totalTime;
	

	public Animation() {
		scenes = new ArrayList<OneScene>();
		totalTime = 0;
		start();
	}
	
	//ajoute une image à l'animation
	public synchronized void addScene(Image i, int t){
		totalTime += t;
		scenes.add(new OneScene(i, totalTime));
	}
	
	//recommence l'animation
	public synchronized void start(){
		movieTime = 0;
		sceneIndex = 0;
	}
	
	//change l'image selon le temps écoulé
	public synchronized void update(long timePassed){
		if(scenes.size() > 1){
			movieTime += timePassed;
			if(movieTime >= totalTime) start();
			
			while(movieTime > getScene(sceneIndex).endTime) sceneIndex++;
		}
	}
	
	//retourne l'image actuelle
	public synchronized Image getImage() {
		if(scenes.size()==0) return null;
		else return getScene(sceneIndex).pic;
	}
	
	
	//retourne une scène (une image accompagnée d'un temps)
	private OneScene getScene(int x){
		return(OneScene)scenes.get(x);
	}
	
	//////////////classe interne, une scène est un objet composé d'une image et de son temps durant l'animation ////////////
	private class OneScene{
		Image pic;
		long endTime;
		
		public OneScene(Image pic, long endTime){
			this.pic = pic;
			this.endTime = endTime;
		}
	}
	
}
