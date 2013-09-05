//
//  Edge.java
//  
//
//  Created by Andre Altmann on 1/14/13.
//  Source code taken from and adapted to facilitate double weights
//  http://algowiki.net/wiki/index.php?title=Edge
//
package com.altmann;

public class Edge /*implements Comparable<Edge>*/ {
   
  final Node from, to; 
  final double weight;
   
   public Edge(final Node argFrom, final Node argTo, final double argWeight){
       from = argFrom;
       to = argTo;
       weight = argWeight;
   }
   
   public double getWeight() {return weight;}
   public Node getSource() {return from;}
   public Node getDest() {return to;}
   
   
   //not even used in Edmonds' alg
//   public int compareTo(final Edge argEdge){
//       //return weight - argEdge.weight;
//
//       double res = weight - argEdge.weight;
//       return (int)(res * 100);
//   }
   
   public String toString()
   {
	   return "" + from.name + "->" + to.name + " w=" + weight; 
   }
   
}
