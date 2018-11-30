public class Room
{
	private boolean wall, stinky, drafty, glittery, wumpus, pit, gold;
	
	public Room()
	{
		wall = false;
		stinky = false;
		drafty = false;
		glittery = false;
		wumpus = false;
		pit = false;
		gold = false;
	}
	
	public boolean isWall ()
	{
		return wall;
	}
	
	public boolean isStinky ()
	{
		return stinky;
	}
	
	public boolean isDrafty ()
	{
		return drafty;
	}
	
	public boolean isGlittery ()
	{
		return glittery;
	}
	
	public boolean isWumpus ()
	{
		return wumpus;
	}
	
	public boolean isPit ()
	{
		return pit;
	}
	
	public boolean isGold ()
	{
		return gold;
	}
	
	public void setWall ()
	{
		wall = true;
	}
	
	public void setStinky ()
	{
		stinky = true;;
	}
	
	public void setDrafty ()
	{
		drafty = true;
	}
	
	public void setGlittery ()
	{
		glittery = true;
	}
	
	public void setWumpus ()
	{
		wumpus = true;
	}
	
	public void setPit ()
	{
		pit = true;
	}
	
	public void setGold ()
	{
		gold = true;
	}
}
