public class WumpusBenchmark
{
	public static void main (String[] args)
	{
		benchmark(4);
		benchmark(5);
		benchmark(8);
		benchmark(10);
	}
	
	//runs 1000 wumpus worlds of the given size and prints the average results
	public static void benchmark (int size)
	{
		System.out.println("Running 1,000 " + size + "x" + size + " wumpus worlds...");
		int totalActions = 0, totalPoints = 0, totalWins = 0;
		WumpusWorld world;
		
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 1000; i++)
		{
			world = new WumpusWorld(size);
			while (!world.agentHasDied() && !world.agentHasWon())
				world.makeAction();
			if (world.agentHasWon())
				totalWins++;
			totalActions += world.getActions();
			totalPoints += world.getPoints();
		}
		long endTime = System.currentTimeMillis();
		
		System.out.println((totalWins/10) + "% of games were won.");
		System.out.println((endTime - startTime) + "ms to complete 1000 levels.");
		System.out.println(((endTime - startTime)/1000) + "ms average per level.");
		System.out.println(totalActions + " total actions made, " + (totalActions/1000) + " average.");
		System.out.println(totalPoints + " total score, " + (totalPoints/1000) + " average.\n");
	}
}
