//
//  Edmonds_AlgoWiki.java
//  
//
//  Created by Andre Altmann on 1/14/13.
//
//  Implementation of the Chu-Liu-Edmonds Algorithm for finding
//  a maximum/minimum branching in a Graph
//  Source Code taken from:
//  algowiki.net/wiki/index.php?title=Edmonds%27s_algorithm
//
//  WARNING: This implementation is incorrect, see discussion
//  in algowiki.net
//
package com.altmann;

import java.util.ArrayList;
import java.util.Iterator;

public class Edmonds_AlgoWiki extends Edmonds {

   private ArrayList<Node> cycle;

   public AdjacencyList getMinBranching(Node root, AdjacencyList list){
       AdjacencyList reverse = list.getReversedList();
       // remove all edges entering the root
       if(reverse.getAdjacent(root) != null){
           reverse.getAdjacent(root).clear();
       }
       AdjacencyList outEdges = new AdjacencyList();
       // for each node, select the edge entering it with smallest weight
       for(Node n : reverse.getSourceNodeSet()){
           ArrayList<Edge> inEdges = reverse.getAdjacent(n);
           if(inEdges.isEmpty()) continue;
           Edge min = inEdges.get(0);
           for(Edge e : inEdges){
               if(e.weight < min.weight){
                   min = e;
               }
           }
           outEdges.addEdge(min.to, min.from, min.weight);
       }

       // detect cycles
       ArrayList<ArrayList<Node>> cycles = new ArrayList<ArrayList<Node>>();
       cycle = new ArrayList<Node>();
       getCycle(root, outEdges);
       cycles.add(cycle);
       for(Node n : outEdges.getSourceNodeSet()){
           if(!n.visited){
               cycle = new ArrayList<Node>();
               getCycle(n, outEdges);
               cycles.add(cycle);
           }
       }

       // for each cycle formed, modify the path to merge it into another part of the graph
       AdjacencyList outEdgesReverse = outEdges.getReversedList();

       for(ArrayList<Node> x : cycles){
           if(x.contains(root)) continue;
           mergeCycles(x, list, reverse, outEdges, outEdgesReverse);
       }
       return outEdges;
   }

   public AdjacencyList getMaxBranching(Node root, AdjacencyList list){
       //use minBranching algorithm, but first multiply edge weights with -1.0
       AdjacencyList rlist = new AdjacencyList();
       for(Iterator<Edge> e = list.getAllEdges().iterator(); e.hasNext();){
	   Edge k = e.next();
	   rlist.addEdge(k.from, k.to, k.weight * -1.0);
       }
       //System.err.println("reversed edges");

       AdjacencyList tresult = this.getMinBranching(root, rlist);	
       //System.err.println("ran algo");

       //reverse weights for results	
       AdjacencyList result = new AdjacencyList();
       for(Iterator<Edge> e = tresult.getAllEdges().iterator(); e.hasNext();){
	   Edge k = e.next();
	   result.addEdge(k.from, k.to, k.weight * -1.0);
       }	
       //System.err.println("reversed edges");

       return result;		
   }

   private void mergeCycles(ArrayList<Node> cycle, AdjacencyList list, AdjacencyList reverse, AdjacencyList outEdges, AdjacencyList outEdgesReverse){
       ArrayList<Edge> cycleAllInEdges = new ArrayList<Edge>();
       Edge minInternalEdge = null;
       // find the minimum internal edge weight
       for(Node n : cycle){
           for(Edge e : reverse.getAdjacent(n)){
               if(cycle.contains(e.to)){
                   if(minInternalEdge == null || minInternalEdge.weight > e.weight){
                       minInternalEdge = e;
                       continue;
                   }
               }else{
                   cycleAllInEdges.add(e);
               }
           }
       }
       // find the incoming edge with minimum modified cost
       Edge minExternalEdge = null;
       double minModifiedWeight = 0;
       for(Edge e : cycleAllInEdges){
           double w = e.weight - (outEdgesReverse.getAdjacent(e.from).get(0).weight - minInternalEdge.weight);
           if(minExternalEdge == null || minModifiedWeight > w){
               minExternalEdge = e;
               minModifiedWeight = w;
           }
       }
       // add the incoming edge and remove the inner-circuit incoming edge
       Edge removing = outEdgesReverse.getAdjacent(minExternalEdge.from).get(0);
       outEdgesReverse.getAdjacent(minExternalEdge.from).clear();
       outEdgesReverse.addEdge(minExternalEdge.to, minExternalEdge.from, minExternalEdge.weight);
       ArrayList<Edge> adj = outEdges.getAdjacent(removing.to);
       for(Iterator<Edge> i = adj.iterator(); i.hasNext(); ){
           if(i.next().to == removing.from){
               i.remove();
               break;
           }
       }
       outEdges.addEdge(minExternalEdge.to, minExternalEdge.from, minExternalEdge.weight);
   }

   private void getCycle(Node n, AdjacencyList outEdges){
       n.visited = true;
       cycle.add(n);
       if(outEdges.getAdjacent(n) == null) return;
       for(Edge e : outEdges.getAdjacent(n)){
           if(!e.to.visited){
               getCycle(e.to, outEdges);
           }
       }
   }
}

