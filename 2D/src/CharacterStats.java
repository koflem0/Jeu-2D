import java.io.Serializable;



public class CharacterStats implements Serializable {
		
		private static final long serialVersionUID = 1L;
			public int[] atts = {4,4,4,4};
			public int lvl = 1, exp, skillpts, attpts, mana, life, currentMap = 0, classe;
			public int[] skillLvls = {1,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};
			public int[] passiveSkillLvls = {0,0,0,0,0,0,0,0,0,0};
			public Inventory inventory;
			
			public CharacterStats(Main.Character c){
				inventory = new Inventory(c);
			}
	}
	
