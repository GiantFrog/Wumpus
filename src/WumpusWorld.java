import org.jpl7.*;

public class WumpusWorld
{
	private Room[][] map;
	private int agentX, agentY, agentDirection, moves, points;
	
	public WumpusWorld (int mapSize)
	{
		//load up the rules in wumpus_writs.pl using swi-prolog's JPL.
		//You may need to add JPL as a library for this to work. Get it from jpl7.org
		Query loadWrits = new Query("consult", new Term[] {new Atom("wumpus_writs.pl")});
		System.out.println( "Wumpus Writs have been loaded" + (loadWrits.hasSolution() ? " successfully!" : "... Not at all. They failed to load."));
		
		points = moves = 0;
		agentX = agentY = agentDirection = 1;
		map = generateMap(mapSize);
		sendSenses(agentX, agentY);
	}
	
	//returns a random map of the specified dimensions
	//Every map contains a wumpus, a golden treasure, and some bottomless pits.
	private Room[][] generateMap (int size)
	{
		map = new Room[size+2][size+2];	//we add 2 to account for walls
		
		//initialize all the rooms as empty
		for (int a = 0; a < size+2; a++)
		{
			for (int b = 0; b < size+2; b++)
				map[a][b] = new Room();
		}
		
		//set walls along the border
		for (int a = 0; a < size+2; a++)
		{
			map[0][a].setWall();
			map[size+1][a].setWall();
		}
		for (int a = 1; a < size+2; a++)
		{
			map[a][0].setWall();
			map[a][size+1].setWall();
		}
		
		//add a wumpus and a gold to any tile that isn't 1, 1
		boolean goldAdded = false;
		while (true)
		{
			int j = (int)(Math.random()*size) + 1;
			int k = (int)(Math.random()*size) + 1;
			if (j != 1 || k != 1)	//if we rolled 1,1 just start the loop over
			{
				if (goldAdded)
				{
					System.out.println("The Wumpus is in " + j + ", " + k);
					map[j][k].setWumpus();
					map[j][k].setStinky();
					map[j+1][k].setStinky();
					map[j-1][k].setStinky();
					map[j][k+1].setStinky();
					map[j][k-1].setStinky();
					break;
				}
				else
				{
					System.out.println("The gold is in " + j + ", " + k);
					map[j][k].setGold();
					map[j][j].setGlittery();
					goldAdded = true;
				}
			}
		}
		
		//add pits to non-starting spaces without gold or a wall. 20% chance for a pit per space.
		boolean atOneOne = true;
		for (int a = 1; a <= size; a++)
		{
			for (int b = 1; b <= size; b++)
			{
				if (!atOneOne && !map[a][b].isGold() && !map[a][b].isWall() && (int)(Math.random()*5) == 0)
				{
					System.out.println("A pit is in " + a + ", " + b);
					map[a][b].setPit();
					map[a][b].setDrafty();
					map[a+1][b].setDrafty();
					map[a-1][b].setDrafty();
					map[a][b+1].setDrafty();
					map[a][b-1].setDrafty();
				}
				else
					atOneOne = false;
			}
		}
		
		//this helps prolog recognize room adjacency
		for (int a = 1; a < size+2; a++)
		{
			new Query("assert(nextTo(" + a + "," + (a-1) + "))").hasSolution();
			new Query("assert(nextTo(" + (a-1) + "," + a + "))").hasSolution();
		}
		return map;
	}
	
	//informs the agent about the room it is currently in.
	//returns false if the agent tried to walk into a wall.
	private boolean sendSenses (int a, int b)
	{
		if (map[a][b].isStinky())
			new Query("assert(stinky(" + a + "," + b + "))").hasSolution();
		if (map[a][b].isDrafty())
			new Query("assert(drafty(" + a + "," + b + "))").hasSolution();
		if (map[a][b].isGlittery())
			new Query("assert(glittery(" + a + "," + b + "))").hasSolution();
		
		if (map[a][b].isWall())
		{
			new Query("assert(wall(" + a + "," + b + "))").hasSolution();
			return false;
		}
		else	//only count a space as explored if there's no wall
		{
			new Query("assert(explored(" + a + "," + b + "))").hasSolution();
			return true;
		}
	}
	
	//moves the agent in the direction they are facing.
	//no movement will happen if the agent tries to walk into a wall.
	private void forward()
	{
		moves++;
		points--;
		switch (agentDirection)
		{
			case 0:		//north
				if (sendSenses(agentX, agentY+1))
					agentY++;
				break;
			case 1:		//east
				if (sendSenses(agentX+1, agentY))
					agentX++;
				break;
			case 2:		//south
				if (sendSenses(agentX, agentY-1))
					agentY--;
				break;
			default:	//west
				if (sendSenses(agentX-1, agentY))
					agentX--;
		}
	}
	
	private void turnRight()
	{
		moves++;
		agentDirection++;
		agentDirection %= 4;
	}
	private void turnLeft()
	{
		moves++;
		agentDirection--;
		if (agentDirection < 0)
			agentDirection = 3;
	}
}