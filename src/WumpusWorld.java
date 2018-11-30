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
	}
	//TODO implement goTo w/ maze algorithm using safe spaces
	//TODO main menu with level size chooser
	//TODO random level gen (1 wumpus, 1 gold, (1,1) is always clear, 20% chance of pit on other tiles)
	//TODO write prolog rules
}