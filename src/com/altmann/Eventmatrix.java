package com.altmann;
import java.util.*;
import java.io.*;

public class Eventmatrix {

    private Vector<Vector<Boolean>> ematrix;
    private Integer nSamples, nEvents;
    
    public Eventmatrix(){
	nSamples = 0;
	nEvents = 0;
    }
    
    //public Eventmatrix(Integer _nSamples, Integer _nEvents, Vector<Vector<Boolean> _emat){
    public Eventmatrix(Vector<Vector<Boolean>> _emat){
	//nSamples = _nSamples;
	//nEvents  = _nEvents;
	
	ematrix = _emat;
	nSamples=ematrix.size();
	if (nSamples > 0)
	    nEvents=ematrix.get(0).size();
    }
    
    public Integer getNS(){ return nSamples;}
    public Integer getNE(){ return nEvents;}
    
    //each row is one Sample, each column one event
    //binary matrix
    public void readMatrix(String _fname){
	ematrix = new Vector<Vector<Boolean>>();
	try {
	    BufferedReader in = new BufferedReader(new FileReader(_fname));
	    
	    String line = in.readLine();
	    String[] tok;
	    
	    while (line != null){
		nSamples += 1;				
		tok = line.split(" ");
		if (nEvents == 0){
		    nEvents = tok.length;
		}
		Vector<Boolean> tmp = new Vector<Boolean>();
		for(int i = 0; i<nEvents; ++i)
		    tmp.add(tok[i].equals("1"));
		ematrix.add(tmp);
		line = in.readLine();
	    }			
	    in.close();
	} catch (IOException e){
	    System.err.println("Some IOException!");
	}
    }
    
    //generate a bootstrap of the original Eventmatrix
    //optional seed??
    public Eventmatrix generateBootstrap(){
	Random randomGen = new Random();
	Vector<Vector<Boolean>> tmp = new Vector<Vector<Boolean>>();
	
	for(int i=0; i<nSamples; ++i){
	    int rnd = randomGen.nextInt(nSamples);
	    tmp.add(ematrix.get(rnd));
	}
	Eventmatrix result = new Eventmatrix(tmp);
	return result;
    }
    
    public Vector<Integer> computeColSum(){
	Vector<Integer> res = new Vector<Integer>();
	for(int j=0; j < nEvents; ++j)
	    res.add(0);
	for(int i=0; i< nSamples;++i){
	    Vector<Boolean> tmp = ematrix.get(i);
	    for(int j=0;j < nEvents; ++j)
		if(tmp.get(j)){
		    //System.out.println(j);
		    res.set(j, res.get(j) + 1);
		}
	}
	return res;
    }

    public Integer computeJointOccur(Integer i, Integer j){
	Integer res = new Integer(0);
	for (int a=0; a<nSamples; ++a){
	    Vector<Boolean> tmp = ematrix.get(a);
	    if (tmp.get(i) && tmp.get(j))
		++res;
	}
	return res;
    }
    
    public double[][] computeTrueCondProb(Boolean addroot){
	int nadd = 0;
	if (addroot)
	    nadd=1;
	double[][] res= new double[nEvents+nadd][nEvents];
	Vector<Integer> cSum = computeColSum();

	for(int i=0; i<nEvents;++i){
	    double Pu = (double)cSum.get(i)/nSamples;
	    for(int j=i+1; j<nEvents; ++j){
		double Pv = (double)cSum.get(j)/nSamples;
		double Puv = (double)computeJointOccur(i,j)/nSamples;
		if (Puv == 0)
		    Puv = 0.0001;
		//from i->j P(j|i)
		res[i+nadd][j] = Puv/Pu;
		//from i->j P(i|j)
		res[j+nadd][i] = Puv/Pv;
	    }
	    res[i+nadd][i] = Double.NaN;
	    if (addroot)
		res[0][i] = Pu;				 						
	}
	return res;

    }

    //compute conditional probabilities
    public double[][] computeProb(Boolean addroot){
	int nadd = 0;
	if (addroot)
	    nadd=1;
	double[][] res= new double[nEvents+nadd][nEvents];
	Vector<Integer> cSum = computeColSum();
	
	for(int i=0; i<nEvents;++i){
	    double Pu = (double)cSum.get(i)/nSamples;
	    double lPu = Math.log(Pu);
	    for(int j=i+1; j<nEvents; ++j){
		double Pv = (double)cSum.get(j)/nSamples;
		double lPv = Math.log(Pv);
		double Puv = (double)computeJointOccur(i,j)/nSamples;
		if (Puv == 0)
		    Puv = 0.0001;
		double lPuv = Math.log(Puv);
		double tmp = lPuv - Math.log(Pu + Pv);
		res[i+nadd][j] = tmp - lPv;
		res[j+nadd][i] = tmp - lPu;			
	    }
	    res[i+nadd][i] = Double.NaN;
	    if (addroot)
		res[0][i] = lPu;				 						
		}
	return res;
    }
    
    public Dummy probToGraph(double[][] probs){
	AdjacencyList result = new AdjacencyList();
	Node rootnode = null;
	int nNodes = probs.length;
	
	List<Node> nodes = new ArrayList<Node>(nNodes);
	for (int i = 0; i < nNodes; ++i){
	    Node dummy = new Node(i);
	    if (i == 0)
		rootnode = dummy;
	    nodes.add(dummy);
	}
	
	for(int from = 0; from < nNodes; ++from){
	    double[] cur = probs[from];
	    //now populate the edges
	    Node fromnode = nodes.get(from);
	    for(int to = 1; to < nNodes; ++to){
		if (!Double.isNaN(cur[to-1])){
		    Node tonode = nodes.get(to);				
		    result.addEdge(fromnode, tonode, cur[to-1]);
		}
	    }
	}
	
	for(Iterator<Edge> e = result.getAllEdges().iterator(); e.hasNext(); ){
	    Edge me = e.next();
	    //System.err.println(me.from.name + "->" + me.to.name + ": " + me.weight);
	}
	Dummy myreturn = new Dummy(rootnode, result);
	return(myreturn);
	
    }
    
    public Dummy probToGraph(){
	double[][] tmp = computeProb(true);
	return probToGraph(tmp);
    }
}
