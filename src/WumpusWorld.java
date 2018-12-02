import org.jpl7.*;

import java.lang.Integer;
import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import static java.lang.System.exit;

public class WumpusWorld
{
	private Room[][] map;
	private int startingX, startingY, agentX, agentY, agentDirection, moves, points, arrows;
	private boolean hasGold;
	
	public WumpusWorld (int mapSize)
	{
		//load up the rules in wumpus_writs.pl using swi-prolog's JPL.
		//You may need to add JPL as a library for this to work. Get it from jpl7.org
		Query loadWrits = new Query("consult", new Term[] {new Atom("wumpus_writs.pl")});
		System.out.println( "Wumpus Writs have been loaded" + (loadWrits.hasSolution() ? " successfully!" : "... Not at all. They failed to load."));
		
		hasGold = false;
		arrows = agentDirection = startingX = startingY = 1;
		points = moves = 0;
		agentX = startingX;
		agentY = startingY;
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
		else new Query("assert(stinky(" + a + "," + b + "):- false)").hasSolution();
		if (map[a][b].isDrafty())
			new Query("assert(drafty(" + a + "," + b + "))").hasSolution();
		else new Query("assert(drafty(" + a + "," + b + "):- false)").hasSolution();
		if (map[a][b].isGlittery())
			new Query("assert(glittery(" + a + "," + b + "))").hasSolution();
		else new Query("assert(glittery(" + a + "," + b + "):- false)").hasSolution();
		
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
	public void forward()
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
	public void turnRight()
	{
		moves++;
		agentDirection++;
		agentDirection %= 4;
	}
	public void turnLeft()
	{
		moves++;
		agentDirection--;
		if (agentDirection < 0)
			agentDirection = 3;
	}
	//uses turning and forward moves to go directly to an adjacent square. does NOT check for safety!
	public void moveAjd (int direction)
	{
		//this variable is a little confusing, but it saves some space compared to previous copy/paste spaghetticode
		//think of it as how many times we have to turn left to end up facing the way we want to go!
		int turnControl = agentDirection - direction;
		if (turnControl < 0)
			turnControl += 4;
		
		switch (turnControl)
		{
			case 0:
				break;
			case 1:
				turnLeft();
				break;
			case 2:
				turnLeft();
				turnLeft();
				break;
			case 3:
				turnRight();
				break;
		}
		forward();
	}
	public void moveAjd (int x, int y)
	{
		if (y > agentY)				//move north
			moveAjd(0);
		else if (x > agentX)		//move east
			moveAjd(1);
		else if (y < agentY)		//move south
			moveAjd(2);
		else moveAjd(3);	//move west
	}
	//moves an agent safely to a room. the last move is allowed to be risky.
	//returns false if no safe path could be found.
	public boolean goTo (int x, int y)
	{
		Node path = new Search(agentX, agentY, x, y).solve();
		if (path == null)
			return false;
		while (path.getParent() != null)
		{
			//we start on our staring space, so we can skip trying to move there.
			path = path.getParent();
			moveAjd(path.getX(), path.getY());
		}
		
		if (agentHasDied())
			points -= 1000;
		return true;
	}
	//give the agent the gold and remove it from the map
	public void grabGold()
	{
		moves++;
		if (map[agentX][agentY].isGold())
		{
			new Query("retract(glittery(" + agentX + "," + agentY + "))").hasSolution();
			hasGold = true;
		}
	}
	public void shoot()
	{
		moves++;
		points -= 10;
		if (arrows > 0)
		{
			arrows--;
			switch (agentDirection)
			{
				case 0:		//north
					for (int a = agentY; !map[agentX][a].isWall(); a++)	//go until you hit a wall
					{
						if (sendArrowThrough(agentX, a))
							break; //the arrow stops when it hits the Wumpus
					}
					break;
				case 1:		//east
					for (int a = agentX; !map[a][agentY].isWall(); a++)
					{
						if (sendArrowThrough(a, agentY))
							break;
					}
					break;
				case 2:		//south
					for (int a = agentY; !map[agentX][a].isWall(); a--)
					{
						if (sendArrowThrough(agentX, a))
							break;
					}
					break;
				default:	//west
					for (int a = agentX; !map[a][agentY].isWall(); a--)
					{
						if (sendArrowThrough(a, agentY))
							break;
					}
			}
		}
	}
	//helper function for shoot().
	//Returns true if the wumpus has been hit. We stop the arrow if we hit the wumpus.
	private boolean sendArrowThrough (int x, int y)
	{
		if (map[x][y].isWumpus())
		{
			//tell the agent there is no more risk from the wumpus
			new Query("assert(scream)").hasSolution();
			return true;
		}
		else
		{
			//tell the agent no wumpus can possibly be in this square
			new Query("assert(noWumpus(" + x + "," + y + "))").hasSolution();
			return false;
		}
	}
	
	//looks at what the agent knows about the world, picks the best action to make, and performs it.
	public void makeAction()
	{
		if (new Query("glittery(" + agentX + "," + agentY + ")").hasSolution())
			grabGold();
		else
		{	//query for all safe spaces. pick the closest one and move to it.
			Query safe = new Query("safe(X,Y)");
			Query explored = new Query("explored(X,Y");
			//TODO remove every explored element from safe, then pick from the leftovers.
			Map<String,Term> goodRoom;
			int bestRoomX = -1, bestRoomY = -1, distance, bestDistance = Integer.MAX_VALUE;
			if (safeUnexplored.hasSolution())	//there's a safe place to go to!
			{
				while (safeUnexplored.hasMoreSolutions())
				{
					goodRoom = safeUnexplored.getSolution();
					distance = Math.abs(goodRoom.get("X").intValue() - agentX) + Math.abs(goodRoom.get("Y").intValue() - agentY);
					if (distance < bestDistance)
					{
						bestDistance = distance;
						bestRoomX = goodRoom.get("X").intValue();
						bestRoomY = goodRoom.get("Y").intValue();
					}
				}
				goTo(bestRoomX, bestRoomY);
			}
			else
			{
				//TODO decide how to proceed when there are no space spaces.
				System.out.println("There are no safe spaces left!!");
				forward();
			}
		}
		if (agentHasWon())
			points += 1000;
	}
	
	//returns true if the agent has gold and is in the starting room
	public boolean agentHasWon()
	{
		return (hasGold && agentX == startingX && agentY == startingY);
	}
	//returns true if the agent is standing on a pit or is in a room with the wumpus and has never heard a scream.
	public boolean agentHasDied()
	{
		boolean wumpusIsDead = new Query("scream").hasSolution();
		return (map[agentX][agentY].isPit() || (!wumpusIsDead && map[agentX][agentY].isWumpus()));
	}
	public int getMoves()
	{
		return moves;
	}
	public int getPoints()
	{
		return points;
	}
}