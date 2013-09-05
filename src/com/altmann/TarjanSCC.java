//
//  SCC.java
//  
//
//  Created by Andre Altmann on 1/17/13.
//
//  Tarjan's algorithm for finding SCCs in a graph
//  implementation based on Wikipedia description of the algorithm:
//  en.wikipedia.org/wiki/Tarjan's_strongly_connected_components_algorithm
package com.altmann;
import java.util.*;

public class TarjanSCC extends SCC {

    int index;
    Stack<Node> S;
    AdjacencyList edges;
    List<Collection<Node>> mySCC;

    public TarjanSCC(){
	this.index = 0;
	this.S = new Stack<Node>();
	this.mySCC = new ArrayList<Collection<Node> >();
    }
	
    public List<Collection<Node>> runSCCsearch(AdjacencyList _edges){	
	this.edges = _edges;
	this.index = 0;
	this.mySCC = new ArrayList<Collection<Node> >();

	//empty stack
	while (! this.S.empty())
	    S.pop();

	//created Set of Nodes
	Set<Node> mynodes = new HashSet<Node>();
	for(Iterator<Edge> e = edges.getAllEdges().iterator(); e.hasNext(); ){
	    Edge mye = e.next();
	    mynodes.add(mye.from);
	    mye.from.index = -1;
	    mye.from.lowlink = -1;

	    mynodes.add(mye.to);
	    mye.to.index = -1;
	    mye.to.lowlink = -1;
	}

	for(Iterator<Node> n = mynodes.iterator(); n.hasNext();){
	    Node v = n.next();
	    if (v.index < 0)
		this.strongConnect(v);
	}

	return this.mySCC;
	       
    }

    public void strongConnect(Node v){
	v.index = index;
	v.lowlink = index++;
	S.push(v);

	List<Edge> adjacent = this.edges.getAdjacent(v);
	if (adjacent != null)
		for(Iterator<Edge> e = adjacent.iterator(); e.hasNext();){
		    Edge mye = e.next();
		    Node w = mye.to;
		    if (w.index < 0){
			this.strongConnect(w);
			v.lowlink = Math.min(v.lowlink, w.lowlink);
		    } else {
			if (S.search(w) > 0){
			    v.lowlink = Math.min(v.lowlink, w.index);
			}
		    }	
		}
	
	if (v.lowlink == v.index){
	    Collection<Node> nscc = new HashSet<Node>();
	    Node w;
	    do {
		w = S.pop();
		nscc.add(w);
	    } while (w != v);
	    mySCC.add(nscc);       
	}
    }

}

