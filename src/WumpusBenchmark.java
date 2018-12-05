public class WumpusBenchmark
{
	public static void main (String[] args)
	{
		benchmark(4);
		//benchmark(5);
		//benchmark(8);
		//benchmark(10);
	}
	
	//runs 100 wumpus worlds of the given size and prints the average results
	//this doesn't work. prolog never resets and just get bogged down by all the rules added every run...
	public static void benchmark (int size)
	{
		System.out.println("Running 100 " + size + "x" + size + " wumpus worlds...");
		int totalActions = 0, totalPoints = 0, totalWins = 0;
		WumpusWorld world;
		
		long startTime = System.currentTimeMillis();
		for (int i = 0; i < 100; i++)
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
		
		System.out.println(totalWins + "% of games were won.");
		System.out.println((endTime - startTime) + "ms to complete 1000 levels.");
		System.out.println(((endTime - startTime)/100) + "ms average per level.");
		System.out.println(totalActions + " total actions made, " + (totalActions/100) + " average.");
		System.out.println(totalPoints + " total score, " + (totalPoints/100) + " average.\n");
	}
}
