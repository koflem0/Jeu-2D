import java.awt.Point;
import java.awt.Rectangle;


public class Tooltip {
	
	private Item item;
	private Rectangle area;
	private String[] stats = new String[5];
	
	public Tooltip(Item item, Point position){
		this.item = item;
		setArea(position);
		
		if(item.getRarity()!=Item.COMMON) getStats();
		
	}
	
	public void setArea(Point position){area = new Rectangle(position.x+10, position.y, 160, 165);}
	public Rectangle getArea(){return area;}
	
	private void getStats(){
		int number = 0;
		
		if(item.getEnhancedD() != 0){
			
			stats[0] = item.getEnhancedD() + "% enhanced ";
			
			if(item.getSlot() == Item.WEAPON) stats[0] += " damage";
			else stats[0] += " defense";
			number++;
		}
		
		for(int i = 0; i < 7; i++){
			if(item.getStat(i)!=0) {
				stats[number]="+";
				stats[number]+=item.getStat(i);
				switch(i){
				case Main.ALLSTATS: stats[number]+=" to all stats"; break;
				case Main.SPIRIT: stats[number]+=" Spirit"; break;
				case Main.POW: stats[number]+=" Power"; break;
				case Main.AGI: stats[number]+=" Agility"; break;
				case Main.VIT: stats[number]+=" Vitality"; break;
				case Main.CRITDMG: stats[number]+="% Crit damage"; break;
				case Main.CRIT: stats[number]+="% Crit chance"; break;
				case Main.MASTERY: stats[number]+=" Mastery"; break;
				}
				number++;
			}
		}
	}
	public Item getItem(){return item;}
	
	public String getInfo(int line){
		String info ="";
		if(item.getSlot() == Item.WEAPON) {
			switch(line){
			case 1 : info+="Weapon"; break;
			case 2 : info+=item.getD() + " damage";break;
			case 8: info+="Required level : " + item.getLevel();break;
			default : if(stats[line-3] !=null) info = stats[line-3];
			}
			
		} else if(item.getSlot() != Item.RING && item.getSlot() !=Item.AMULET){
			switch(line){
			
			case 1:
			info+="Armor - ";
			switch(item.getSlot()){
				case Item.TORSO: info+="torso"; break;
				case Item.BOOTS: info+="boots"; break;
				case Item.HELM: info+="helm"; break;
				case Item.GLOVES: info+="gloves"; break;
				case Item.PANTS: info+= "pants"; break;
			} break;
			
			case 2: info+=item.getD()+" defense"; break;
			case 8: info+="Required level : " + item.getLevel();break;
			default : if(stats[line-3] !=null) info = stats[line-3];
			}
		} else {
			
			switch(line){
			
			case 1:
			switch(item.getSlot()){
				case Item.RING: info+="Ring"; break;
				case Item.AMULET: info+="Amulet"; break;
			} break;
			
			case 2: break;
			case 8: info+="Level requis : " + item.getLevel();break;
			default : if(stats[line-3] !=null) info = stats[line-3];
			}
		}
		return info;
	}
}
