import org.jpl7.*;

import java.lang.Integer;
import java.util.ArrayList;
import java.util.Map;

public class WumpusWorld
{
	private Room[][] map;
	private int startingX, startingY, agentX, agentY, agentDirection, actions, points, arrows;
	private boolean hasGold;
	
	public WumpusWorld (int mapSize)
	{
		//load up the rules in wumpus_writs.pl using swi-prolog's JPL.
		//You may need to add JPL as a library for this to work. Get it from jpl7.org
		Query loadWrits = new Query("consult", new Term[] {new Atom("wumpus_writs.pl")});
		System.out.println( "Wumpus Writs have been loaded" + (loadWrits.hasSolution() ? " successfully!" : "... Not at all. They failed to load."));
		
		hasGold = false;
		arrows = agentDirection = startingX = startingY = 1;
		points = actions = 0;
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
					map[j][k].setGlittery();
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
		//prolog already knows (0,0) is a wall, but we want to tell it the other three corners are as well.
		//this way we never try to explore these unreachable spaces.
		new Query("assert(wall(" + 0 + "," + (size+1) + "))").hasSolution();
		new Query("assert(wall(" + (size+1) + "," + 0 + "))").hasSolution();
		new Query("assert(wall(" + (size+1) + "," + (size+1) + "))").hasSolution();
		
		new Query("assert(pit(" + startingX + "," + startingY + "):- false)");
		new Query("assert(wumpus(" + startingX + "," + startingY + "):- false)");
		
		return map;
	}
	
	//informs the agent about the room it is currently in.
	//returns false if the agent tried to walk into a wall.
	private boolean sendSenses (int a, int b)
	{
		System.out.println("You head to " + a + ", " + b + ".");
		if (map[a][b].isStinky())
		{
			new Query("assert(stinky(" + a + "," + b + "))").hasSolution();
			System.out.println("You smell the unmistakable stench of the wumpus.");
		}
		else new Query("assert(stinky(" + a + "," + b + "):- false)").hasSolution();
		if (map[a][b].isDrafty())
		{
			new Query("assert(drafty(" + a + "," + b + "))").hasSolution();
			System.out.println("You feel a draft and hear the gentle howl of the empty sky below you.");
		}
		else new Query("assert(drafty(" + a + "," + b + "):- false)").hasSolution();
		if (map[a][b].isGlittery())
		{
			new Query("assert(glittery(" + a + "," + b + "))").hasSolution();
			System.out.println("The walls reflect with the glitter of gold! The treasure must be in here, somewhere...");
		}
		else new Query("assert(glittery(" + a + "," + b + "):- false)").hasSolution();
		
		//send flavor text before death if the agent enters a lethal room
		boolean wumpusIsDead = new Query("scream").hasSolution();
		if (map[a][b].isWumpus())
		{
			if (wumpusIsDead)
				System.out.println("Most of the room is filled by the wumpus, which is still truly terrifying even after being slain by your arrow!");
			else
				System.out.println("Most of the room is filled by the wumpus, which devours you well before you get a chance to examine it!");
		}
		if (map[a][b].isPit())
			System.out.println("You suddenly find yourself falling through the open sky! You have plenty of time to contemplate your career choices before you hit the ground.");
		
		//send a bump if we hit a wall
		if (map[a][b].isWall())
		{
			new Query("assert(wall(" + a + "," + b + "))").hasSolution();
			System.out.println("...but fall backwards after walking straight into a wall. D:");
			return false;
		}
		else	//only count a space as explored if there's no wall, so we don't try to pathfind through it
		{
			new Query("assert(explored(" + a + "," + b + "))").hasSolution();
			new Query("assert(wall(" + a + "," + b + "):- false)").hasSolution();
			return true;
		}
	}
	
	//moves the agent in the direction they are facing.
	//no movement will happen if the agent tries to walk into a wall.
	public void forward()
	{
		actions++;
		switch (agentDirection)
		{
			case 0:		//north
				if (sendSenses(agentX, agentY+1))
				{
					agentY++;
					points--;
				}
				break;
			case 1:		//east
				if (sendSenses(agentX+1, agentY))
				{
					agentX++;
					points--;
				}
				break;
			case 2:		//south
				if (sendSenses(agentX, agentY-1))
				{
					agentY--;
					points--;
				}
				break;
			default:	//west
				if (sendSenses(agentX-1, agentY))
				{
					agentX--;
					points--;
				}
		}
	}
	public void turnRight()
	{
		actions++;
		agentDirection++;
		agentDirection %= 4;
	}
	public void turnLeft()
	{
		actions++;
		agentDirection--;
		if (agentDirection < 0)
			agentDirection = 3;
	}
	public void turnToFace (int direction)
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
	}
	//uses turning and forward moves to go directly to an adjacent square. does NOT check for safety!
	public void moveAjd (int direction)
	{
		turnToFace(direction);
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
		actions++;
		if (map[agentX][agentY].isGold())
		{
			new Query("retract(glittery(" + agentX + "," + agentY + "))").hasSolution();
			hasGold = true;
			System.out.println("You find the treasure and load up on as many gold bars as you can carry!");
		}
		else System.out.println("You search the room for treasure, but find nothing.");
	}
	//fires an arrow straight forward
	public void shoot()
	{
		actions++;
		points -= 10;
		if (arrows > 0)
		{
			System.out.println("You draw back your bow and fire off an arrow into the darkness.");
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
	//moves the agent to a safe place and turns them to face the room they want to shoot at, then fires!
	public boolean shoot (int x, int y)
	{
		ArrayList<int[]> safeShootingRooms = new ArrayList<>();
		Query roomQuery = new Query("safe(" + x + ",Y)");
		while (roomQuery.hasMoreSolutions())
		{
			Map<String,Term> safeRoom = roomQuery.nextSolution();
			safeShootingRooms.add(new int[] {x, safeRoom.get("Y").intValue()});
		}
		roomQuery = new Query("safe(X," + y + ")");
		while (roomQuery.hasMoreSolutions())
		{
			Map<String,Term> safeRoom = roomQuery.nextSolution();
			safeShootingRooms.add(new int[] {safeRoom.get("X").intValue(), y});
		}
		//we couldn't find any rooms to move to and shoot from
		if (safeShootingRooms.isEmpty())
			return false;
		else
		{
			//finds and moves us to the closest safe room
			findClosestRoom(safeShootingRooms);
			if (y > agentY)	//need to face north
				turnToFace(0);
			else if (x > agentX)		//east
				turnToFace(1);
			else if (y < agentY)		//south
				turnToFace(2);
			else if (x < agentX)		//west
				turnToFace(3);
			
			shoot();
			return true;
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
			System.out.println("You hear a terrible scream. Your arrow must have hit its mark!");
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
		//if there's glitter here, the best action is always to take the gold and run!
		if (new Query("glittery(" + agentX + "," + agentY + ")").hasSolution())
		{
			grabGold();
			goTo(startingX, startingY);
		}
		else	//query for all safe, unexplored rooms. pick the closest one and move to it.
		{
			ArrayList<int[]> unexploredCoords = new ArrayList<>();
			
			//grab all the safe rooms
			Query safe = new Query("safe(X,Y)");
			while (safe.hasMoreSolutions())
			{
				Map<String,Term> safeRoom = safe.nextSolution();
				unexploredCoords.add(new int[] {safeRoom.get("X").intValue(), safeRoom.get("Y").intValue()});
			}
			//filter out any that are explored or walls
			Query explored = new Query("explored(X,Y)");
			while (explored.hasMoreSolutions())
			{
				Map<String,Term> exploredRoom = explored.nextSolution();
				unexploredCoords.removeIf(coord -> coord[0] == exploredRoom.get("X").intValue() && exploredRoom.get("Y").intValue() == coord[1]);
			}
			Query wall = new Query("wall(X,Y)");
			while (wall.hasMoreSolutions())
			{
				Map<String,Term> wallRoom = wall.nextSolution();
				unexploredCoords.removeIf(coord -> coord[0] == wallRoom.get("X").intValue() && wallRoom.get("Y").intValue() == coord[1]);
			}
			
			if (unexploredCoords.isEmpty())	//there's no safe place to go to!
			{
				System.out.println("There are no safe spaces left!!");
				
				//if we have an arrow, we don't know where the wumpus is, and there's a stinky space somewhere, shoot at the stink
				Query stinky = new Query("stinky(X,Y)");
				if (arrows > 0 && !new Query("wumpus(X,Y)").hasSolution() && stinky.hasMoreSolutions())
				{
					int x, y;
					Map<String,Term> stinkyRoom = stinky.nextSolution();
					x = stinkyRoom.get("X").intValue();
					y = stinkyRoom.get("Y").intValue();
					shoot(x, y);
				}
				else	//we aren't shooting
				{
					//we'll just grab every unexplored space since none of them are safe
					Query unexplored = new Query("unexplored(X,Y)");
					while (unexplored.hasMoreSolutions())
					{
						Map<String, Term> unexploredRoom = unexplored.nextSolution();
						unexploredCoords.add(new int[]{unexploredRoom.get("X").intValue(), unexploredRoom.get("Y").intValue()});
					}
					//we don't want to visit one with a confirmed wumpus or pit!
					Query dangerous = new Query("dangerous(X,Y)");
					ArrayList<int[]> dangerousRooms = new ArrayList<>();
					while (dangerous.hasMoreSolutions())
					{
						Map<String, Term> dangerousRoom = dangerous.nextSolution();
						dangerousRooms.add(new int[] {dangerousRoom.get("X").intValue(), dangerousRoom.get("Y").intValue()});
						unexploredCoords.removeIf(coord -> coord[0] == dangerousRoom.get("X").intValue() && dangerousRoom.get("Y").intValue() == coord[1]);
					}
					if (unexploredCoords.isEmpty())    //every unexplored space is dangerous
					{
						//if we know where the wumpus is, we can shoot it. Maybe it's blocking the path or on the gold?
						Query wumpusQuery = new Query("wumpus(X,Y)");
						Map<String,Term> wumpusLoc;
						if (arrows > 0 && wumpusQuery.hasMoreSolutions())
						{
							wumpusLoc = wumpusQuery.nextSolution();
							shoot(wumpusLoc.get("X").intValue(), wumpusLoc.get("Y").intValue());
						}
						
						//if we ever end up here, we can't win...
						//and seeing as we can't leave without our gold, we commit suicide as quickly as possible so as to minimize point loss
						else
							findClosestRoom(dangerousRooms);
					} else    //we have some risky, but unknown places to try
						findClosestRoom(unexploredCoords);
				}
			}
			else	//there are some safe places to explore!
				findClosestRoom(unexploredCoords);
		}
		if (agentHasWon())
			points += 1000;
	}
	
	//locates the closest room to the agent and goes to it
	private void findClosestRoom (ArrayList<int[]> possibleRooms)
	{
		while (true)
		{
			int bestRoomX = -1, bestRoomY = -1, distance, bestDistance = Integer.MAX_VALUE, index = -1;
			//measure how far away each option we have is
			for (int[] coord : possibleRooms)
			{
				distance = Math.abs(coord[0] - agentX) + Math.abs(coord[1] - agentY);
				if (distance < bestDistance)
				{
					bestDistance = distance;
					bestRoomX = coord[0];
					bestRoomY = coord[1];
					index = possibleRooms.indexOf(coord);
				}
			}
			//then go to the closest one!
			if (goTo(bestRoomX, bestRoomY))
				break;
			else
			{	//if we couldn't find a path there, remove it from the list and find the next closest one, then go there.
				System.out.println("No safe path could be found: " + agentX + ", " + agentY + " -> " + bestRoomX + ", " + bestRoomY);
				possibleRooms.remove(index);
			}
		}
	}
	
	//returns true if the agent has gold and is in the starting room
	public boolean agentHasWon()
	{
		return (hasGold && agentX == startingX && agentY == startingY);
	}
	//returns true if the agent is standing on a pit or is in a room with the wumpus and has never heard a scream.
	public boolean agentHasDied()
	{
		return (diedToPit() || diedToWumpus());
	}
	public boolean diedToPit()
	{
		return map[agentX][agentY].isPit();
	}
	public boolean diedToWumpus()
	{
		if (map[agentX][agentY].isWumpus())
		{	//if the wumpus is still alive, the agent just died to it.
			boolean wumpusIsDead = new Query("scream").hasSolution();
			return !wumpusIsDead;
		}
		else return false;
	}
	public int getActions ()
	{
		return actions;
	}
	public int getPoints()
	{
		return points;
	}
}