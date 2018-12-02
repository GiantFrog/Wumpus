public class Node
{
	private Node parent;
	private int x, y, cost;
	
	public Node (int x, int y, int cost, Node parent)
	{
		this.x = x;
		this.y = y;
		this.cost = cost;
		this.parent = parent;
	}
	
	public Node getParent ()
	{
		return parent;
	}
	public int getX ()
	{
		return x;
	}
	public int getY ()
	{
		return y;
	}
	//this cost is only distance, NOT including distance to the goal!
	public int getCost ()
	{
		return cost;
	}
	
	@Override
	public boolean equals (Object a)
	{
		try
		{
			Node b = (Node)a;
			return (b.getX() == this.getX() && b.getY() == this.getY());
		}
		catch (Exception shucks)
		{
			return false;
		}
	}
}
