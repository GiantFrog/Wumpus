public class WumpusDriver
{
	public static void main (String[] args)
	{
		WumpusWorld world = new WumpusWorld(4);
		while (!world.agentHasDied() && !world.agentHasWon())
			world.makeAction();
		
		//print the results of the game
		System.out.print("After making " + world.getActions() + " actions, ");
		if (world.diedToWumpus())
			System.out.println("you were killed by an angry wumpus.");
		if (world.diedToPit())
			System.out.println("you fell out of the wumpus' lair to your death.");
		else if (world.agentHasWon())
			System.out.println("you made it out alive with half your weight in solid gold!");
		System.out.println("\nFINAL SCORE: " + world.getPoints());
	}
	//TODO main menu with level size chooser
	//TODO write prolog rules
}