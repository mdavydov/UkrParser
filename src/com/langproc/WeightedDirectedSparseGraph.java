package com.langproc;

import java.util.Collection;
import java.util.TreeMap;
import java.util.Map;
import java.util.Vector; 
import java.util.HashMap;

import com.altmann.AdjacencyList;
import com.altmann.Edmonds;
import com.altmann.Edmonds_Andre;
import com.altmann.Node;

import com.altmann.*;

class Edge
{
	public int m_from;
	public int m_to; 
	
	public Edge(int from, int to)
	{
		m_from = from;
		m_to = to;
	}
	public String toString()
	{
		return "(" + m_from + "->" + m_to + ")";
	}
}

public class WeightedDirectedSparseGraph
{
	int m_num_vertices;
//	TreeMap<Float, Edge> m_edges = new TreeMap<Float, Edge>();
	
	AdjacencyList m_edges = new AdjacencyList();
	Node m_root = new Node(-1);
	java.util.Vector<Node> m_nodes = new java.util.Vector<Node>();
	
	public WeightedDirectedSparseGraph(int num_vertices)
	{
		m_num_vertices = num_vertices;

		m_nodes.setSize(m_num_vertices);
		for(int i=0;i<m_num_vertices;++i)
		{
			m_nodes.set(i, new Node(i));
			m_edges.addEdge(m_root, m_nodes.get(i), 0.0);
		}
	}
	
	public void addEdge(int v1, int v2, float weight)
	{
		m_edges.addEdge( m_nodes.get(v1), m_nodes.get(v2), weight );
	}
	public void calcMaxBranching()
	{
		Edmonds myed = new Edmonds_Andre();	
		AdjacencyList rBranch;
	    rBranch = myed.getMaxBranching(m_root, m_edges);
	    
	    double total = 0;
	    for( com.altmann.Edge e : rBranch.getAllEdges())
	    {
	    	System.out.println(e);
	    	total += e.getWeight();
	    }
	    
	    System.out.println("Total = " + total);
	}
	
	static public void test()
	{
		WeightedDirectedSparseGraph g = new WeightedDirectedSparseGraph(10);
		
		for(int i=0;i<100;++i) g.addEdge(i%10, i/10, (float)Math.random() );
		
		g.calcMaxBranching();
		

	}
}
