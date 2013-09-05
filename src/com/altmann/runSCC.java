package com.altmann;
import java.util.*;

public class runSCC {

    public static void main(String[] argv)
    {
	if (argv.length != 1){
	    System.err.println("USAGE: runSCC <input matrix>");
	    System.exit(0);
	}

	String fname = new String(argv[0]);

	MatrixIO myIO = new MatrixIO();
	Dummy myInput = myIO.readGraphMatrix(fname);
	AdjacencyList myEdges = myInput.getEdges();

	SCC mySCC = new TarjanSCC();

	List<Collection<Node>> sccs = mySCC.runSCCsearch(myEdges);
	int i = 0;
	for(Iterator<Collection<Node>> scc = sccs.iterator(); scc.hasNext(); ){
	    Collection<Node> dummy = scc.next();
	    System.out.print("SCC " + ++i + ":");
	    for(Iterator<Node> v = dummy.iterator(); v.hasNext(); )
		System.out.print(" " + v.next().name);
	    System.out.println("");
	}
    }
}
