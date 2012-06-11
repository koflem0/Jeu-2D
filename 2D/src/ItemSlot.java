import java.awt.Rectangle;
import java.io.Serializable;


public class ItemSlot implements Serializable{
		
		private static final long serialVersionUID = -6455029061470075344L;
		private Rectangle area;
		public boolean equip;
		private int i, j, page;
		private Inventory inventory;
		private Stash stash;
		
		public ItemSlot(int i, Inventory inventory){
			this.i = i;
			this.inventory = inventory;
			area = new Rectangle(Inventory.x+220+i*55, Inventory.y+20, 50, 50);
			equip = true;
		}
		
		public ItemSlot(int i, int j, Inventory inventory){
			this.i = i; this.j = j; this.inventory = inventory;
			area = new Rectangle(Inventory.x+220+i*55, Inventory.y+100+j*55,50,50);
			equip = false;
		}
		
		public ItemSlot(int i, int j, Stash stash){
			this.i = i; this.j = j;
			this.stash = stash;
			area = new Rectangle(Stash.x+20+i*55, Stash.y+100+j*55,50,50);
		}
		
		public void activate(boolean drop){
			if(equip){
				inventory.unequip(i);
			} else {
				if(drop) inventory.drop(i, j); else
				inventory.equip(i,j);
			}
		}
		
		public Rectangle getArea(){return area;}
		
	}
	
