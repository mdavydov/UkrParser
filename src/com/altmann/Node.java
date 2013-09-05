//
//  Node.java
//  
//
//  Created by Andre Altmann on 1/14/13.
//  source code taken from:
//  http://algowiki.net/wiki/index.php?title=Node 
//
package com.altmann;

public class Node implements Comparable<Node> {
   
   public final int name;
   boolean visited = false;   // used for Kosaraju's algorithm and Edmonds's algorithm
   int lowlink = -1;          // used for Tarjan's algorithm
   int index = -1;            // used for Tarjan's algorithm
   
   public Node(final int argName) {
       name = argName;
   }
   
    //not even used in Edmonds' alg
   public int compareTo(final Node argNode) {
       return argNode == this ? 0 : -1;
   }
}
