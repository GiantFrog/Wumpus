import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Stack;

public class BFS
{
	private Queue<int[]> frontier;
	private Stack<Integer> result;
	private ArrayList<int[]> closed;
	
	public BFS ()
	{
		frontier = new LinkedList<>();
		result = new Stack<>();
	}
	
	public Stack<Integer> solve (int startX, int startY, int endX, int endY)
	{
		//TODO make this thing work and return a stack of coordinates to visit
		frontier.add(new int[] {endX, endY});
		int[] current;
		while (true)
		{
			current = frontier.poll();
			if (current == null)
				return null;
			
			if (current[0] == startX && current[1] == startY)
				return result;
		}
	}
}
