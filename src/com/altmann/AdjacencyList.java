//
//  AdjacencyList.java
//  
//
//  Created by Andre Altmann on 1/14/13.
//  Source Code taken from
//  http://algowiki.net/wiki/index.php?title=Adjacency_list
//
package com.altmann;
import java.util.Map;
import java.util.HashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.Collection;

public class AdjacencyList {

   private Map<Node, ArrayList<Edge>> adjacencies = new HashMap<Node, ArrayList<Edge>>();

   public Edge addEdge(Node source, Node target, double weight) {
       ArrayList<Edge> list;
       if(!adjacencies.containsKey(source)) {
           list = new ArrayList<Edge>();
           adjacencies.put(source, list);
       } else {
           list = adjacencies.get(source);
       }
       Edge result = new Edge(source, target, weight);
       list.add(result);
       return result;
   }

    public Edge addEdge(Edge _edge){
       ArrayList<Edge> list;
       if(!adjacencies.containsKey(_edge.from)) {
           list = new ArrayList<Edge>();
           adjacencies.put(_edge.from, list);
       } else {
           list = adjacencies.get(_edge.from);
       }
       list.add(_edge);
       return _edge;

    }

   public ArrayList<Edge> getAdjacent(Node source) {
       return adjacencies.get(source);
   }

   public void reverseEdge(Edge e) {
       adjacencies.get(e.from).remove(e);
       addEdge(e.to, e.from, e.weight);
   }

   public void reverseGraph() {
       adjacencies = getReversedList().adjacencies;
   }

   public AdjacencyList getReversedList() {
       AdjacencyList newlist = new AdjacencyList();
       for(List<Edge> edges : adjacencies.values()) {
           for(Edge e : edges) {
               newlist.addEdge(e.to, e.from, e.weight);
           }
       }
       return newlist;
   }

public Map<Node, Collection<Edge>> getAllInEdges(){
	Map<Node, Collection<Edge>> result = new HashMap<Node, Collection<Edge> >();

	for (Node n : getSourceNodeSet()){
		for(Edge e: getAdjacent(n)){
			Collection<Edge> tmp;
			if(result.containsKey(e.to)){
				tmp = result.get(e.to);
			} else {
				tmp = new ArrayList<Edge>();
				result.put(e.to, tmp);
			}
			tmp.add(e);
		}
	}
	return result;
}

   public Set<Node> getSourceNodeSet() {
       return adjacencies.keySet();
   }

   public Collection<Edge> getAllEdges() {
       List<Edge> edges = new ArrayList<Edge>();
       for(List<Edge> e : adjacencies.values()) {
           edges.addAll(e);
       }
       return edges;
   }
}

