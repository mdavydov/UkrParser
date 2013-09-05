package com.altmann;
import java.util.*;

public class runEdmonds {

    public static void main(String[] argv){

	if (argv.length != 2){
	    System.err.println("USAGE: runEdmonds <input matrix> <min|max>");
	    System.exit(0);
	}

	String fname = new String(argv[0]);
	String minmax = new String(argv[1]);
	boolean doMax = true;
	if (minmax.equals("min")){
	    doMax = false;
	} else {
	    if (!minmax.equals("max"))
		System.err.println("Unsopported option: " + minmax); 
	}
	
	MatrixIO myIO = new MatrixIO();
	Dummy myInput = myIO.readGraphMatrix(fname);
	AdjacencyList myEdges = myInput.getEdges();
	Node root = myInput.getRoot();
	
	//run Edomonds'
	//Edmonds myed = new Edmonds_AlgoWiki();
	Edmonds myed = new Edmonds_Andre();	
	AdjacencyList rBranch;
	if (doMax){
	    rBranch = myed.getMaxBranching(root, myEdges);
	} else {
	    rBranch = myed.getMinBranching(root, myEdges);
	}
	myIO.printGraphMatrix(rBranch);


    }

}
