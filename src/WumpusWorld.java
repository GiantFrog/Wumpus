import org.jpl7.*;

public class WumpusWorld
{
	public static void main (String[] args)
	{
		/*
		Query testQuery = new Query("consult", new Term[] {new Atom("test.pl")});
		System.out.println( "consult " + (testQuery.hasSolution() ? "succeeded" : "failed"));
		Query q = new Query("child_of", new Term[] {new Atom("joe"), new Atom("ralf")});
		System.out.println("child of joe and ralf is " + (q.hasSolution() ? "provable" : "not provable"));
		
		Query q3 =
				new Query(
						"descendent_of",
						new Term[] {new Atom("steve"),new Atom("ralf")}
				);
		System.out.println(
				"descendent_of(joe,ralf) is " +
						( q3.hasSolution() ? "provable" : "not provable" )
		);
		
		Variable X = new Variable("X");
		Query q4 =
				new Query(
						"descendent_of",
						new Term[] {X,new Atom("ralf")}
				);
		
		java.util.Map<String,Term> solution;
		
		solution = q4.oneSolution();
		
		System.out.println( "first solution of descendent_of(X, ralf)");
		System.out.println( "X = " + solution.get("X"));
		*/
		//load up the rules in wumpus_writs.pl using swi-prolog's JPL.
		//You may need to add JPL as a library for this to work. Get it from jpl7.org
		Query loadWrits = new Query("consult", new Term[] {new Atom("wumpus_writs.pl")});
		generateMap(4);
	}
	//TODO implement goTo w/ maze algorithm using safe spaces
	//TODO main menu with level size chooser
	//TODO random level gen (1 wumpus, 1 gold, (1,1) is always clear, 20% chance of pit on other tiles)
	//TODO write prolog rules
	
	//returns a random map of the specified dimensions
	public static Room[][] generateMap (int size)
	{
		Room[][] map = new Room[size+2][size+2];	//we add 2 to account for walls
		
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
			map[size+2][a].setWall();
		}
		for (int a = 1; a <= size+2; a++)
		{
			map[a][0].setWall();
			map[a][size+2].setWall();
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
		return map;
	}
}