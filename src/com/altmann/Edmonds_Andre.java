//
//  Edmonds_Andre.java
//  
//
//  Created by Andre Altmann on 1/17/13.
//
//  Implementation of the Chu-Liu-Edmonds Algorithm for finding
//  a maximum/minimum branching in a Graph
//  implementation based on description of:
//  
//
package com.altmann;
import java.util.*;

public class Edmonds_Andre extends Edmonds {

    List<AdjacencyList> G; //list of graphs
    List<Collection<Node>> C; //list of cycles
    List<AdjacencyList> E; //list of temp solutions
    Map<Integer, AdjacencyList> B; //list of temp branchings
    
    Map<Edge, Edge> OrigEdge; //Maps new edges to contracted cycles to original nodes
	int maxNode; //maximum node in graph
	int index; //iteration of algortihm
	
    public AdjacencyList getMaxBranching(Node _root, AdjacencyList _edges){
	G = new ArrayList<AdjacencyList>();
	C = new ArrayList<Collection<Node>>();
	E = new ArrayList<AdjacencyList>();
	B = new HashMap<Integer, AdjacencyList>();
	
	OrigEdge = new HashMap<Edge, Edge>();
	this.index = 0;
	this.maxNode = 0; 
	for (Edge e : _edges.getAllEdges()){
	    if (e.to.name > this.maxNode)
		this.maxNode = e.to.name;
	    if (e.from.name > this.maxNode)
		this.maxNode = e.from.name;
	}
	
	//for debug
	//MatrixIO xxx = new MatrixIO();

	AdjacencyList reverse = _edges.getReversedList();
       // remove all edges entering the root
       if(reverse.getAdjacent(_root) != null){
           reverse.getAdjacent(_root).clear();
       }
       G.add(reverse.getReversedList()); //G_0

       AdjacencyList outEdges;
       outEdges = getMaxIncoming(G.get(this.index));
       E.add(outEdges); //E_0
       		
       //now, we have either a tree, or we have at least one cycle in the graph
       //detect cycles using StronglyConnectedComponents (Tarjan implementation)
       List<Collection<Node>> sccs = getSCCs(E.get(this.index));
       	while (hasCycle(sccs)){
       		for(Collection<Node> scc : sccs){
		    //debug
		    /*System.out.print("SCC:");
		    for(Iterator<Node> v = scc.iterator(); v.hasNext();)
			System.out.print(" " + v.next().name);
		    System.out.println("");
		    */
			if (scc.size() > 1){
		    //System.out.println("contracting");

				//real cycle
				C.add(scc);
				//cycle has to be resolved, index is incremented
				 Node newnode = contractCycle(scc);
				 //next round
				 break;
			}				
		}
		//System.out.println("Graph G" + this.index);
		//xxx.printGraphMatrix(G.get(this.index));

		//compute potential branching for new graph
		outEdges = this.getMaxIncoming(G.get(this.index));
		E.add(outEdges);
		sccs = getSCCs(E.get(this.index));
       }
       //System.out.println("Amigos no more tears!");
       //all cycles gone! we have a branching!!
       B.put(this.index, E.get(this.index));
		
       //reconstruct, i.e., expand contracted nodes
       while(this.index > 0)
       		reconstruct();
			
    	return B.get(0);
    }

   public AdjacencyList getMinBranching(Node _root, AdjacencyList _edges){
       //use minBranching algorithm, but first multiply edge weights with -1.0
       AdjacencyList rlist = new AdjacencyList();
       for(Iterator<Edge> e = _edges.getAllEdges().iterator(); e.hasNext();){
	   Edge k = e.next();
	   rlist.addEdge(k.from, k.to, k.weight * -1.0);
       }

       AdjacencyList tresult = this.getMaxBranching(_root, rlist);	

       //reverse weights for results	
       AdjacencyList result = new AdjacencyList();
       for(Iterator<Edge> e = tresult.getAllEdges().iterator(); e.hasNext();){
	   Edge k = e.next();
	   result.addEdge(k.from, k.to, k.weight * -1.0);
       }	

       return result;		
   }

	private AdjacencyList getMaxIncoming(AdjacencyList _graph){
		AdjacencyList E = new AdjacencyList();
		Map<Node, Collection<Edge>> reverseAccess = _graph.getAllInEdges();
		
		for(Node n : reverseAccess.keySet()){
			Collection<Edge>  inEdges = reverseAccess.get(n);
			if (inEdges.isEmpty()) continue;
			Edge max=null;
			for (Edge e: inEdges)
				if (max == null || e.weight > max.weight)
					max = e;
			if (max != null)
				E.addEdge(max);			
		}
		return E;
	}

    private AdjacencyList getMaxIncomingOLD(AdjacencyList _graph){
	AdjacencyList E = new AdjacencyList();
	AdjacencyList reverse = _graph.getReversedList();

        for(Node n : reverse.getSourceNodeSet()){
           ArrayList<Edge> inEdges = reverse.getAdjacent(n);
           if(inEdges.isEmpty()) continue;
           Edge max = inEdges.get(0);
           for(Edge e : inEdges)
               if(e.weight > max.weight)
                   max = e;
                         
           //E.addEdge(max.to, max.from, max.weight);
	   E.addEdge(max);
       }
       return E;
    }

    private List<Collection<Node>> getSCCs(AdjacencyList _edges){
		SCC mySCC = new TarjanSCC();
		return mySCC.runSCCsearch(_edges);
    }

    private boolean hasCycle(List<Collection<Node>> _sccs){
		for (Collection<Node> scc : _sccs)
			if (scc.size() > 1)
				return true;
		return false;
    }


    private Node contractCycle(Collection<Node> cycleNodes){
	//contracts all nodes in a cycle to a single new node
	Node result = new Node(++this.maxNode);
	AdjacencyList currentEdges = E.get(this.index);
	AdjacencyList graphEdges = G.get(this.index);
	
	//compute min/max weight within cycle
	List<Edge> cycleEdges = new ArrayList<Edge>();
	Map<Node, Edge> inCycEdge = new HashMap<Node, Edge>();
	Edge extreme = null;
	for( Edge e : currentEdges.getAllEdges()){
	    if (cycleNodes.contains(e.to) && cycleNodes.contains(e.from)){
	    	cycleEdges.add(e);
		inCycEdge.put(e.to, e);
		if (extreme == null || e.weight > extreme.weight)
			extreme = e;		
	    }
	}

	//build new adjacency list
	AdjacencyList _newgraph = new AdjacencyList();
	for(Edge e: G.get(this.index).getAllEdges()){
	    //no end of edge in cycle - just add edge
	    if ( !(cycleNodes.contains(e.to) || cycleNodes.contains(e.from))){
		_newgraph.addEdge(e);
	    } else {
		//incoming end is in cycle
		Edge newedge = null;
		double nweight = 0.0;
		if (cycleNodes.contains(e.to) && !cycleNodes.contains(e.from) ){
		    //we don't need to worry about multiple edges from one source to the new node
		    //since the other function takes care of this and will select the maximum
		    Edge myIn = inCycEdge.get(e.to);
		    //System.out.println(myIn.weight);
		    nweight = e.weight - (myIn.weight - extreme.weight);
		    //nweight = 100;
		    newedge = _newgraph.addEdge(e.from, result, nweight);
		//add edge to mapping
		OrigEdge.put(newedge, e);

		} 
		if (cycleNodes.contains(e.from) && !cycleNodes.contains(e.to) ){
		//outgoing end is in cycle
		    nweight = e.weight;
		    //nweight = 101;
		    newedge = _newgraph.addEdge(result, e.to, nweight);
		//add edge to mapping
		OrigEdge.put(newedge, e);

		}
	    }
	}
	//we have constructed a new graph (G_i+1)
	this.G.add(_newgraph);
	++this.index;				

	return result;		
    }


    private void reconstruct(){
	//B_i (=E_i) is a branching of G_i
	
	AdjacencyList nb = new AdjacencyList();
	//C index is shifted by 1
	Collection<Node> cycleNodes = C.get(this.index-1);

	for(Edge e : B.get(this.index).getAllEdges()){
	    //if no ende was part of cycle, then just add edge
	    if (!(e.to.name == this.maxNode || e.from.name == this.maxNode)){
		//if (!(cycleNodes.contains(e.to) || cycleNodes.contains(e.from))){
		nb.addEdge(e);
	    } else {
		//if (cycleNodes.contains(e.to) && !cycleNodes.contains(e.from)){
		if (e.to.name == this.maxNode && e.from.name != this.maxNode){
		    //incoming edge to contracted node
		    //System.out.println(OrigEdge.size());
		    Edge x = OrigEdge.get(e);
		    nb.addEdge(x);
		    //add remaining edges from cycle stored in E_i-1
		    //, but the one going to x.to
		    for(Edge ce: E.get(this.index-1).getAllEdges()){
			if (cycleNodes.contains(ce.from) && cycleNodes.contains(ce.to) && ce.to != x.to)
			    nb.addEdge(ce);			
		    }
		}
		if (e.to.name != this.maxNode && e.from.name == this.maxNode){
		    //if (cycleNodes.contains(e.from) && !cycleNodes.contains(e.to)){
		    //outgoing edge from contracted node
		    //System.out.println(e.from.name + "->" + e.to.name + ": " + e.weight);
		    Edge x = OrigEdge.get(e);
		    nb.addEdge(x);
		}
	    }
	}
	--this.maxNode;
	B.put(--this.index, nb);
    }
}
