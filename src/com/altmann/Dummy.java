package com.altmann;
class Dummy {

    Node myRoot;
    AdjacencyList myEdges;
  
    public Dummy(Node _root, AdjacencyList _edges){
	this.myRoot = _root;
	this.myEdges = _edges;
    }

    public AdjacencyList getEdges(){
	return(this.myEdges);
    }
    public Node getRoot(){
	return(this.myRoot);
    }
	
}