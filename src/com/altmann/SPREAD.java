//
//  SPREAD.java
//  
//
//  Created by Andre Altmann on 3/6/13.
//
//  reads an Event Matrix, and runs a bootstrap analysis
//  
//  

package com.altmann;
import java.util.*;
import java.io.*;

public class SPREAD {

    //runtime variables
    static boolean doMax = true, bootSupp = false;
    static String matfname = "", opref = "out";
    static int nboot = 100;
    static double[][] bootSupport;
    
    static String adjf, treef, bootf, cmapf, labmapf, adjcol="red";

	public static void help(String _exname){
		
		System.err.println("USAGE: " + _exname + " command options");
		System.err.println("single");
		System.err.println("	-i <input file>");
		System.err.println("	-o <output prefix>      default: " + opref);
		System.err.println("	-min			computes minimum branching instead of maximum, default: " + !doMax);
		
		System.err.println("bootstrap");
		System.err.println("	-i <input file>");
		System.err.println("	-o <output prefix>      default: " + opref);
		System.err.println("	-n <int>		number of bootstrap samples, default: " + nboot);
		System.err.println("	-bsup			compute the bootstrap support for connections (sloooow), default: " + bootSupp);
		System.err.println("	-min			computes minimum branching instead of maximum, default: " + !doMax);


		System.err.println("plot");
		System.err.println("	-t <branching>		output of 'single' or 'bootstrap'");
		System.err.println("	-a <adjacency matrix>	optional");
		System.err.println("	-b <bootstrapsupport>	optional");
		System.err.println("	-c <color map>		optional");
		System.err.println("	-l <label map>		optional");
      
		
	}

    public static void plot(){
	MatrixIO mio = new MatrixIO();
	double[][] adj = null, tree = null;
	Map<Integer, String> labmap = new HashMap<Integer, String>();
	
	if (bootf != null)
	    bootSupport = mio.readMatrix(bootf);
	if (adjf != null)
	    adj = mio.readMatrix(adjf);
	if (treef != null)
	    tree = mio.readMatrix(treef);

	//option to load label mapping
	labmap.put(0,"root");
	if (labmapf != null){
	    try {
		BufferedReader in = new BufferedReader(new FileReader(labmapf));
		String line = in.readLine();		
		String[] tok;
		while (line != null){
		    tok  = line.split(" ");
		    labmap.put(Integer.parseInt(tok[0]), '"' + tok[1] + '"');
		    line = in.readLine();		    
		}
	    } catch (IOException e){
		System.err.println("Some IOException!");
	    }
	} else {	    
	    for(int i=1;i <= tree.length; ++i)
		labmap.put(i, "\"" + i + "\"");
	}
	String head="digraph pathway {\n node [ style = filled ];";
	System.out.println(head);

	boolean bootCut = false;//true;
	double bc=0.5;

	String edge, attrib;
	for(int from = 0; from < tree.length; ++from)
	    for(int to = 0; to < tree[0].length; ++to)
		if ( !Double.isNaN(tree[from][to] )){
		    //we have an edge
		    edge = labmap.get(from) + " -> " + labmap.get(to+1);
		    attrib ="";
		    if (adj != null && from > 0 && adj[from-1][to] > 0)
			attrib = "color = " + adjcol;
		    if (bootSupport != null){
			if (! "".equals(attrib))
			    attrib += ", ";
			double pw = 10 * bootSupport[from][to];
			attrib += "penwidth=" + pw;		       
		    }
		    if (! "".equals(attrib))
			attrib = " [" + attrib + "]";
		    if (!bootCut || bootSupport == null || bootSupport[from][to] >= bc)
			System.out.println(edge + attrib + ";");
		}

	//color them nodes!?
	if (cmapf != null){
	    Map<String, String> ltoc = new HashMap<String, String>();
	    ltoc.put("occipital_lobe", "red");
	    ltoc.put("frontal_lobe", "blue");
	    ltoc.put("temporal_lobe", "orange");
	    ltoc.put("parietal_lobe","green");
	    ltoc.put("cerebellum", "brown");
	    ltoc.put("subcortical", "yellow");


	    try {
		BufferedReader in = new BufferedReader(new FileReader(cmapf));
		String line = in.readLine();		
		String[] tok;
		String node;
		while (line != null){
		    tok  = line.split(" ");
		    //
		    String xxx = "\"" + tok[0] + "\"";
		    if (labmap.containsValue(xxx)){
			    node = xxx + " [color = " + ltoc.get(tok[1]) + "];";
			    System.out.println(node);
			}
		    line = in.readLine();		    
		}
	    } catch (IOException e){
		System.err.println("Some IOException!");
	    }

	}

	System.out.println("}");

    }

	public static AdjacencyList singleRun(Dummy myInput){

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
	    //myIO.printGraphMatrix(rBranch);
	    return rBranch;

	}

    public static Dummy prepareBootstrapRun(Eventmatrix emat){
	double[] singleProbs = new double[emat.getNE()];
	double[][] pairProbs = new double[emat.getNE()][emat.getNE()];
	bootSupport =  new double[emat.getNE()+1][emat.getNE()];

	//init
	for(int i=0; i<singleProbs.length;++i){
	    singleProbs[i] = 0.0;
	    bootSupport[emat.getNE()][i] = 0.0;
	    for(int j=0; j < emat.getNE(); ++j){
		pairProbs[i][j] = 0.0;
		bootSupport[i][j] = 0.0;
	    }
	}
	for(int b=0; b<nboot; ++b){
	    System.err.println("Calculating probabilities on Bootstrap sample: " + b);
	    Eventmatrix bmat = emat.generateBootstrap();
	    int denom = bmat.getNS()*nboot;

	    Vector<Integer> singles = bmat.computeColSum();
	    for(int i=0; i < singleProbs.length; ++i){
		singleProbs[i]+=(double)singles.get(i)/denom;
		for(int j=i+1; j<pairProbs[0].length; ++j)
		    pairProbs[i][j] += (double)bmat.computeJointOccur(i,j)/denom;		
	    }
	    if(bootSupp){
		System.err.println("Calculating branching on Bootstrap sample: " + b);
		Dummy binput = bmat.probToGraph();
		AdjacencyList bootBranch = singleRun(binput);
		MatrixIO myIO = new MatrixIO();
		double[][] tmp = myIO.adlistToMatrix(bootBranch);
		double[] zzz;
		for(int i=0; i < tmp.length; ++i){
		    zzz = tmp[i];
		    for(int j=0;j<zzz.length;++j)
			if(!Double.isNaN(zzz[j]))
			    bootSupport[i][j] += 1.0/nboot;
		
		}
		binput = null;
		bootBranch = null;
		zzz=null;
	    }
	    bmat = null;
	}
	if(bootSupp){
	    Dummy bootInput = emat.probToGraph(bootSupport);
	    MatrixIO mio = new MatrixIO();
	    mio.printGraphMatrix(singleRun(bootInput), opref + ".bs_support_tree");
	}

	//now we have the bootstrapped probabilities
	//we print them
	double[][] probs = new double[emat.getNE() + 1][emat.getNE()];
	probs[0] = singleProbs;
	for(int i=0; i<emat.getNE();)
	    probs[i+1] = pairProbs[i++];
	MatrixIO mio = new MatrixIO();
	mio.print2DArray(probs, opref + ".bs_prob");

	//and can compute the probability matrix

	double[][] pmat = new double[emat.getNE()+1][emat.getNE()];
	for(int i=0;i<emat.getNE();++i){
	    double Pu = singleProbs[i];
	    double lPu = Math.log(Pu);
	    for(int j=i+1;j<emat.getNE();++j){
		double Pv = singleProbs[j];
		double lPv = Math.log(Pv);
		double Puv = pairProbs[i][j];
		if (Puv == 0.0)
		    Puv = 0.0001;
		double lPuv = Math.log(Puv);

		double tmp = lPuv - Math.log(Pu + Pv);
		pmat[i+1][j] = tmp - lPv;
		pmat[j+1][i] = tmp - lPu;			
	    }
	    pmat[i+1][i] = Double.NaN;
	    pmat[0][i] = lPu;	    
	}

	Dummy result = emat.probToGraph(pmat);
	return (result);
    }

    public static void parseParams(String args[]){
	int nparm=args.length;
	int cparm=1;
	
	while (cparm < nparm){
	    String flag = new String(args[cparm++]);
	    switch(flag){
	    case "-min":
		doMax=false;
		break;
	    case "-bsup":
		bootSupp = true;
		break;
	    case "-i":
		matfname = new String(args[cparm++]);
		break;
	    case "-o":
		opref = new String(args[cparm++]);
		break;
	    case "-n":
		String tmp = new String(args[cparm++]);
		nboot = Integer.parseInt(tmp);
		break;
	    case "-t":
		treef = new String(args[cparm++]);
		break;
	    case "-a":
		adjf = new String(args[cparm++]);
		break;
	    case "-b":
		bootf = new String(args[cparm++]);
		break;
	    case "-c":
		cmapf = new String(args[cparm++]);
		break;
	    case "-l":
		labmapf = new String(args[cparm++]);
		break;
	    default:
		System.err.println("Unknown option: '" + flag + "'!");
		help("SPREAD");
		System.exit(0);
		break;
	    }
	}
	
    }

	public static void main(String argv[]){
		
	    if (argv.length == 0){
		help("SPREAD");
		System.exit(0);
	    }
		String cmd = new String(argv[0]);
		parseParams(argv);
		Eventmatrix myevent = new Eventmatrix();
		Dummy myInput;
		MatrixIO mio = new MatrixIO();
		AdjacencyList mybranch;

		switch(cmd){
		case "single":
		    myevent.readMatrix(matfname);
		    //double[][] probmat = myevent.computeProb(true);
		    //myInput = myevent.probToGraph(probmat);
		    //mio.print2DArray(probmat, opref + ".prob");
		    double[][] probmat = myevent.computeTrueCondProb(true);
		    mio.print2DArray(probmat, opref + ".prob");
		    myInput = myevent.probToGraph();
		    mybranch = singleRun(myInput);
		    mio.printGraphMatrix(mybranch, opref + ".spantree");
		    break;
		case "bootstrap":
		    myevent.readMatrix(matfname);
		    myInput = prepareBootstrapRun(myevent);
		    mybranch = singleRun(myInput);
		    mio.printGraphMatrix(mybranch, opref + ".bs_spantree");
		    if (bootSupp)
			mio.print2DArray(bootSupport, opref + ".bs_support");
		    break;
		case "plot":
		    plot();
		    break;
		default:
		    System.err.println("Unknown command: '" + cmd + "'!");
		    help("SPREAD");
		    System.exit(0);
		    break;
		}
	}

}

