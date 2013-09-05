//
//  Edmonds.java
//  
//
//  Created by Andre Altmann on 1/17/13.
//
//  Abstract Class for Edmonds' Algorithm
//  
package com.altmann;

public abstract class Edmonds {

    public abstract AdjacencyList getMinBranching(Node _root, AdjacencyList _edges);
    public abstract AdjacencyList getMaxBranching(Node _root, AdjacencyList _edges);

}

