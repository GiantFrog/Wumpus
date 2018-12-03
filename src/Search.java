import org.jpl7.Query;
import org.jpl7.Term;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

//A* search to pathfind between safe rooms. Note that the final destination does not need to be confirmed safe.
public class Search
{
	private ArrayList<Node> frontier, closed;
	private int startX, startY, endX, endY;
	
	public Search (int startX, int startY, int endX, int endY)
	{
		this.startX = startX;
		this.startY = startY;
		this.endX = endX;
		this.endY = endY;
		frontier = new ArrayList<>();
		closed = new ArrayList<>();
	}
	
	//note that we solve backwards!
	//this allows us to skip checking the safety of the final space by immediately adding it to the frontier.
	//it also returns our current room as a node whose parent is our next stop along the path!
	public Node solve()
	{
		frontier.add(new Node (endX, endY, 0, null));
		while (true)
		{
			if (frontier.isEmpty())
				return null;
			
			//find the lowest cost node on the frontier
			Node best = null;
			int lowestCost = Integer.MAX_VALUE;
			for (Node room : frontier)
			{
				int cost = room.getCost() + (Math.abs(room.getX() - startX) + Math.abs(room.getY() - startY));
				if (cost < lowestCost)
				{
					lowestCost = cost;
					best = room;
				}
			}
			//stick the node on the closed list
			frontier.remove(best);
			closed.add(best);
			
			//if the node we selected is our solution, we're done here!
			if (best.getX() == startX && best.getY() == startY)
				return best;
			
			//ask prolog for all the safe, adjacent rooms and add them to the frontier
			Query safeAdjQuery = new Query("safeAndAdj([" + best.getX() + "," + best.getY() + "],[X2,Y2])");
			Map<String, Term> safeRoom = new HashMap<>();
			while (safeAdjQuery.hasMoreSolutions())
			{
				safeRoom = safeAdjQuery.nextSolution();
				Node safeNode = new Node(safeRoom.get("X2").intValue(), safeRoom.get("Y2").intValue(), best.getCost()+1, best);
				if (frontier.contains(safeNode))
				{
					//let's see if this path is any cheaper than what we already have.
					if (frontier.get(frontier.indexOf(safeNode)).getCost() < safeNode.getCost())
					{	//remove() uses .equals(), which we have overwritten for Node to mean the same X and Y.
						frontier.remove(safeNode);
						frontier.add(safeNode);
					}
				}
				//it is not on the frontier if we get here, and if it is also not on the closed set, add it to the frontier
				else if (!closed.contains(safeNode))
					frontier.add(safeNode);
			}
		}
	}
}
