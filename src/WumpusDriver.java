public class WumpusDriver
{
	public static void main (String[] args)
	{
		WumpusWorld world = new WumpusWorld(4);
		while (!world.agentHasDied() && !world.agentHasWon())
			world.makeAction();
		if (world.agentHasDied())
			System.out.println("ded.");
		else if (world.agentHasWon())
			System.out.println("By God, he's done it!");
	}
	//TODO implement goTo w/ maze algorithm using safe spaces
	//TODO main menu with level size chooser
	//TODO write prolog rules
}