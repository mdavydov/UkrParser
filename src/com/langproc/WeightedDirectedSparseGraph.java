/*******************************************************************************
 * UkrParser
 * Copyright (c) 2013-2014 Maksym Davydov
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/

package com.langproc;

import java.util.ArrayList;
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
	
	public void calcMaxChainDFS(Node node, int num_in_br, double total_weight, double[] result, double threshold, boolean passed_nodes[] )
	{
		if (num_in_br==m_num_vertices)
		{
			if (result[0] < total_weight) result[0] = total_weight;
			return;
		}
		
		int vi = node.name == -1 ? m_num_vertices : node.name;
		passed_nodes[vi] = true;
		ArrayList<com.altmann.Edge> edge_list = m_edges.getAdjacent(node);
		for( com.altmann.Edge e: edge_list )
		{
			if ( !passed_nodes[e.getDest().name] )
			{
				calcMaxChainDFS(e.getDest(), num_in_br + 1, total_weight + e.getWeight(), result, threshold, passed_nodes);
			}
			//System.out.println(e);
		}
		passed_nodes[vi] = false;
	}
	
	public double calcMaxChainDFS()
	{
		ArrayList<com.altmann.Edge> edge_list = m_edges.getAdjacent(m_root);
		boolean passed_nodes[] = new boolean[m_num_vertices+1];
		double[] result = {0.0};
		calcMaxChainDFS(m_root, 0, 0.0, result, 0.0, passed_nodes );
		
		return result[0];
		
		//System.out.println("Max Branching Weight = " + result[0]);
	}
	
	public void calcMaxBranching()
	{
		long t0 = System.nanoTime();
		
		double res1 = calcMaxChainDFS();
			
		long t1 = System.nanoTime();
		 
		Edmonds myed = new Edmonds_Andre();	
		AdjacencyList rBranch;
	    rBranch = myed.getMaxBranching(m_root, m_edges);
	    
	    double total = 0;
	    for( com.altmann.Edge e : rBranch.getAllEdges())
	    {
	    	System.out.println(e);
	    	total += e.getWeight();
	    }
	    
	    long t2 = System.nanoTime();
	    
	    System.out.println( "Time1 = " + (t1-t0)/1000 + " mikro-sec" );
	    System.out.println( "Time2 = " + (t2-t1)/1000 + " mikro-sec" );
	    
	    System.out.println("Total1 = " + res1);
	    System.out.println("Total2 = " + total);
	}
	
	static public void test()
	{
		WeightedDirectedSparseGraph g = new WeightedDirectedSparseGraph(10);
		
		for(int i=0;i<100;++i) g.addEdge(i%10, i/10, (float)Math.random() );
		
		
		g.calcMaxBranching();
	}
}
