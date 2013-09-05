package com.altmann;
import java.io.*;
import java.util.*;

public class MatrixIO {


    public double[][] readMatrix(String _fname){
	double[][] mat;
	try {
	    BufferedReader in = new BufferedReader(new FileReader(_fname));
	    
	    String line = in.readLine();
	    String[] tok = line.split(" ");
	    int nrow=tok.length;
	    int ncol=nrow++;
	    mat = new double[nrow][ncol];

	    int r=0;
	    while (line != null){
		tok = line.split(" ");
		for(int i = 0; i<ncol; ++i)
		    mat[r][i] = Double.parseDouble(tok[i]);
		line = in.readLine();
		++r;
	    }			
	    in.close();
	    return mat;
	} catch (IOException e){
	    System.err.println("Some IOException!");
	}
	return null;
	
    }

    public Dummy readGraphMatrix(String _fname){

	AdjacencyList result = new AdjacencyList();
	Node rootnode = null;
	try {
	    BufferedReader in = new BufferedReader(new FileReader(_fname));
	    int nNodes = 0;

	    String line = in.readLine();
	    String[] tok = line.split(" ");
	    nNodes = tok.length + 1;
	    List<Node> nodes = new ArrayList<Node>(nNodes);
	    for (int i = 0; i < nNodes; ++i){
		Node dummy = new Node(i);
		if (i == 0)
		    rootnode = dummy;
		nodes.add(dummy);
	    }
	    int from=0;
	    while (line != null){
		//now populate the edges
		tok = line.split(" ");
		Node fromnode = nodes.get(from);
	        for(int i = 1; i < nNodes; ++i){
		    if (! "NA".equals(tok[i-1])){
		    	Node to = nodes.get(i);
			double w = Double.parseDouble(tok[i-1]);
		    	result.addEdge(fromnode, to, w);
		    }
		}
		line = in.readLine();
		++from;
	    }
	    in.close();
	} catch (IOException e){
	    System.err.println("Some IOException!");
	}

	//return(result);
	for(Iterator<Edge> e = result.getAllEdges().iterator(); e.hasNext(); ){
	    Edge me = e.next();
	    //System.err.println(me.from.name + "->" + me.to.name + ": " + me.weight);
	}
	Dummy myreturn = new Dummy(rootnode, result);
	return(myreturn);
    }

    public double[][] adlistToMatrix(AdjacencyList _branch){
	int nrow = 0, ncol = 0;
	for (Iterator<Edge> e = _branch.getAllEdges().iterator(); e.hasNext(); ){
	    Edge dummy = e.next();
	    if (dummy.to.name > ncol)
		ncol = dummy.to.name;
	}
	nrow = ncol + 1;
	//init matrix
	double[][] mat = new double[nrow][ncol];
	for(int i = 0; i < nrow; ++i)
	    for(int j = 0; j < ncol; ++j)
		mat[i][j] = Double.NaN;
	//go through branching
	for(Iterator<Edge> e = _branch.getAllEdges().iterator(); e.hasNext(); ){
	    Edge me = e.next();
	    //System.err.println(me.from.name + "->" + me.to.name + ": " + me.weight);
	    mat[me.from.name][me.to.name-1] = me.weight;
	}
	return mat;
    }

    public void print2DArray(double[][] mat, String _fname){
	//String outp = "";
	int nrow = mat.length;
	int ncol = mat[0].length;
	//print the whole thing

	if (! _fname.equals("")){
	    try{
		BufferedWriter myout = new BufferedWriter(new FileWriter(_fname));
		for (int i = 0; i < nrow; ++i){
		    for (int j = 0; j < ncol; ++j)
			myout.write(mat[i][j] + " ");
		    myout.write("\n");           
		}
		myout.close();
	    } catch (Exception e){
		//
	    }
	} else {
	    for (int i = 0; i < nrow; ++i){
		for (int j = 0; j < ncol; ++j)
		    System.out.print(mat[i][j] + " ");
		System.out.println("");           
	    }
	}
    }

    public void print2DArray(double [][] mat){
	print2DArray(mat, "");
    }

    public void printGraphMatrix(AdjacencyList _branch){
	double[][] mat = adlistToMatrix(_branch);
	print2DArray(mat, "");
    }

    //public void writeGraphMatrix(String _fname, AdjacencyList _branch){
    public void printGraphMatrix(AdjacencyList _branch, String _fname){
	double[][] mat = adlistToMatrix(_branch);
	print2DArray(mat, _fname);
    }
}
    



