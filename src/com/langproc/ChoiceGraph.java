/*******************************************************************************
 * UkrParser
 * Copyright (c) 2013
 * Maksym Davydov
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/

package com.langproc;

import java.util.Collection;
import java.util.Vector; 
import java.util.HashMap;

import com.altmann.AdjacencyList;
import com.altmann.Edmonds;
import com.altmann.Edmonds_Andre;
import com.altmann.Node;


class Vertex
{
	public int m_group_id;
	public int m_vectex_id;
	double m_weight;
	Object m_obj;
	
	Vertex(Object obj, int group_id, int vectex_id, double weight)
	{
		m_obj = obj;
		m_group_id = group_id;
		m_vectex_id = vectex_id;
		m_weight = weight;
	}
	
	public String toString() { return "" + m_vectex_id + " [" + m_group_id + "] " + m_weight + " " + m_obj; }
	
}

class DirectedEdge
{
	Object m_obj;
	double m_weight;
	
	DirectedEdge(Object obj, double weight)
	{
		m_obj = obj;
		m_weight = weight;
	}
	
	public String toString()
	{
		java.text.DecimalFormatSymbols dfs = new java.text.DecimalFormatSymbols(new java.util.Locale("en"));
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.###", dfs);

		return "" + df.format(m_weight) + " " + m_obj;
	}
}

/*************************************************************************************
 * 
 * Subtree
 *
 */

class Subtree implements java.lang.Comparable<Subtree>
{
	double m_total_weight; // weight of the subtree including weight of vertexes, directed edges and incoming edge
	int m_num_vertexes;
	int m_root_vectrex_id;
	int m_root_group_id;
	boolean[] m_covered_groups;
	Vector<Subtree> m_subtrees = new Vector<Subtree>();
	
	public double getBalancedWeight() { return m_total_weight + m_num_vertexes * 10.0f; }
	
	public Subtree(ChoiceGraph g, int vertex_id)
	{
		Vertex v = g.vertexById(vertex_id);
		m_total_weight = v.m_weight;
		m_num_vertexes = 1;
		m_root_vectrex_id = vertex_id;
		m_root_group_id = v.m_group_id;
		m_covered_groups = new boolean[g.getNumGroups()];
		m_covered_groups[v.m_group_id] = true;
		checkConsistency();
	}
	Subtree(Subtree s)
	{
		m_total_weight = s.m_total_weight;
		m_num_vertexes = s.m_num_vertexes;
		m_root_vectrex_id = s.m_root_vectrex_id;
		m_root_group_id = s.m_root_group_id;
		m_covered_groups = s.m_covered_groups.clone();
		for(Subtree s1 : s.m_subtrees)
		{
			m_subtrees.addElement(new Subtree(s1));
		}
		checkConsistency();
	}
	
	// update weight in case of manually constructed Subtree structure or change in ChoiceGraph weights
	double updateWeight(ChoiceGraph g, double incoming_edge_weight)
	{
		double total = incoming_edge_weight;
		total += g.vertexById(m_root_vectrex_id).m_weight;
		int num_vertexes = 1;
		
		for(Subtree s : m_subtrees)
		{
			total += s.updateWeight(g, g.getEdge(m_root_vectrex_id, s.m_root_vectrex_id).m_weight );
			num_vertexes += s.m_num_vertexes;
			
		}
		m_total_weight = total;
		m_num_vertexes = num_vertexes;
		return m_total_weight;
	}
	void updateCoveredGroups(boolean covered_parent[])
	{
		for(Subtree s : m_subtrees)
		{
			s.updateCoveredGroups(m_covered_groups);
		}
		if (covered_parent!=null)
		{
			for(int i=0;i<m_covered_groups.length;++i)
			{
				if (m_covered_groups[i]) covered_parent[i]=true;
			}
		}
	}
	
	void fillVertexChoices(ChoiceGraph cg, int vertex_choices[])
	{
		for(Subtree s : m_subtrees)
		{
			s.fillVertexChoices(cg, vertex_choices);
		}
		vertex_choices[m_root_group_id] = cg.getVertexChoiceIndexInGroup(m_root_vectrex_id);
	}
	
	int[] getVertexChoices(ChoiceGraph cg)
	{
		int choices[] = new int[cg.getNumGroups()];
		fillVertexChoices(cg, choices);
		return choices;
	}
	
	void updateCoveredGroups() { updateCoveredGroups(null); }
	
	public int compareTo(Subtree o)
	{
		Subtree other = (Subtree)o;
		if (m_total_weight == other.m_total_weight) return 0;
		return m_total_weight < other.m_total_weight ? -1 : +1;
	}
	
	void checkConsistency()
	{
		for(int i=0;i<m_covered_groups.length;++i)
		{
			if (m_covered_groups[i])
			{
				Subtree s = findSubtreeByGroupId(i);
				if (s==null) throw new java.lang.IndexOutOfBoundsException("no subtree with covered group");
				if (numVertexesWithGroup(i)!=1)
				{
					throw new java.lang.IndexOutOfBoundsException("num groups != 1"); 
				}
			}
			else
			{
				Subtree s = findSubtreeByGroupId(i);
				if (s!=null) throw new java.lang.IndexOutOfBoundsException("has Subtree with uncovered group");
				if (numVertexesWithGroup(i)!=0) throw new java.lang.IndexOutOfBoundsException("num groups != 0"); 
			}		
		}
	}
	
	int numVertexesWithGroup(int group_id)
	{
		int n = m_root_group_id==group_id ? 1 : 0;
		for(Subtree s : m_subtrees)
		{
			n+=s.numVertexesWithGroup(group_id);
		}
		return n;
	}
	
	Subtree findSubtreeByGroupId(int group_id)
	{
		if (!m_covered_groups[group_id]) return null;
		if (m_root_group_id==group_id) return this;
		for(Subtree s : m_subtrees)
		{
			Subtree r = s.findSubtreeByGroupId(group_id);
			if (r!=null) return r;
		}
		return null;
	}
	
	boolean removeSubtree(Subtree s)
	{
		if (s==this) return true;
		for(Subtree sub:m_subtrees)
		{
			if (sub.removeSubtree(s))
			{
				m_total_weight -= s.m_total_weight;
				m_num_vertexes -= s.m_num_vertexes;
				int nc = m_covered_groups.length;
				for(int i=0;i<nc;++i)
				{
					if (s.m_covered_groups[i]) m_covered_groups[i]=false;
				}
				if (sub==s)
				{
					m_subtrees.remove(s);
				}
				return true;
			}
		}
		return false;
	}
	
	double getRemovalWeightDirect(Subtree other)
	{
		if (other.m_covered_groups[m_root_group_id])
		{
			return m_total_weight;
		}
		
		double total = 0;
		
		for(Subtree s : m_subtrees)
		{
			total+=s.getRemovalWeight(other);
		}
		return total;
	}
	
	double getRemovalWeight(Subtree other)
	{
		if (other.m_covered_groups[m_root_group_id])
		{
			return Math.min(m_total_weight, other.getRemovalWeightDirect(this));
		}
		
		double total = 0;
		
		for(Subtree s : m_subtrees)
		{
			total+=s.getRemovalWeight(other);
		}
		return total;
	}
	
	// also verify if we gain by adding other to some subnode???
	boolean canAttachDirectSubtree(ChoiceGraph g, Subtree other)
	{
		DirectedEdge de = g.getEdge(m_root_vectrex_id, other.m_root_vectrex_id );
		if (de == null) return false;
		return true;
	}
	public boolean isBetterThan(Subtree other)
	{
		int nc = m_covered_groups.length;
		if (m_total_weight <= other.m_total_weight) return false;
		if (m_num_vertexes < other.m_num_vertexes) return false;
		for(int i=0;i<nc;++i)
		{
			if (!m_covered_groups[i] && other.m_covered_groups[i]) return false;
		}
		return true;
	}
	
	void fillSubtrees(Vector<Subtree> v)
	{
		v.addElement(this);
		for(Subtree s : m_subtrees)
		{
			s.fillSubtrees(v);
		}
	}
	
	Vector<Subtree> getAllSubtreesSorted()
	{
		Vector<Subtree> my = new Vector<Subtree>(m_num_vertexes);
		fillSubtrees(my);
		java.util.Collections.sort(my);
		return my;
	}
	
	// add subtree connected to the top vertex
	boolean addSubtree(ChoiceGraph g, Subtree other)
	{	
		checkConsistency();
		other.checkConsistency();
		DirectedEdge de = g.getEdge(m_root_vectrex_id, other.m_root_vectrex_id );
		if (de == null) return false;
		
		//LangProcOutput.println("Adding subtree 1");
		//print(g);
		//LangProcOutput.println("Adding subtree 2");
		//other.print(g);
		
		other.m_total_weight += de.m_weight;
		
		Vector<Subtree> all_my = getAllSubtreesSorted();
		Vector<Subtree> all_other = other.getAllSubtreesSorted();

		// remove overlapping groups starting from low-weight subtrees
		int my_remove = 0;
		int other_remove = 0;
		
		for(;;)
		{
			Subtree my_s = all_my.get(my_remove);
			Subtree other_s = all_other.get(other_remove);
			if (my_s.m_total_weight <= other_s.m_total_weight)
			{
				if (other.m_covered_groups[my_s.m_root_group_id])
				{
					if (my_s==this) return false;
					removeSubtree(my_s);
				}
				if (++my_remove >= all_my.size()) break;

			}
			else
			{
				if (m_covered_groups[other_s.m_root_group_id])
				{
					if (other_s==other) return false;
					other.removeSubtree(other_s);
				}
				if (++other_remove >= all_other.size()) break;
			}
		}
		
		m_subtrees.addElement(other);
		m_total_weight += other.m_total_weight;
		m_num_vertexes += other.m_num_vertexes;
		int nc = m_covered_groups.length;
		for(int i=0;i<nc;++i)
		{
			if (other.m_covered_groups[i]) m_covered_groups[i] = true;
		}
		checkConsistency();

		return true;
	}
	
	void print(ChoiceGraph g, int shift, int prev_root)
	{
		for(int i=0;i<shift;++i) LangProcOutput.print("  ");
		
		if (prev_root != -1)
		{
			DirectedEdge de = g.getEdge(prev_root, m_root_vectrex_id);
			LangProcOutput.print( "(->" + de + "->) ");
		}
		Vertex v = g.vertexById(m_root_vectrex_id);
		LangProcOutput.println("" + v + " (" + m_num_vertexes + "," +  m_total_weight + ")");
		for(Subtree s : m_subtrees)
		{
			s.print(g, shift+1, m_root_vectrex_id);
		}
	}
	
	public void print(ChoiceGraph g) { print(g,0, -1); }
	
	public void print_qtree(ChoiceGraph g, int prev_root)
	{	
		Vertex v = g.vertexById(m_root_vectrex_id);
		boolean close2 = false;

		if (prev_root != -1)
		{
			DirectedEdge de = g.getEdge(prev_root, m_root_vectrex_id);
			if (de.m_weight <= LangProcSettings.OPTIONAL_WEIGHT) return;
			java.text.DecimalFormat df = new java.text.DecimalFormat("#.###");			
			LangProcOutput.print( "[." + de.m_obj + " \\edge node[auto=left]{" + df.format(de.m_weight) + " }; ");
			if (m_subtrees.size()==0)
			{
				LangProcOutput.print(v.m_obj +  "-" + (float)v.m_weight );
			}
			else
			{
				LangProcOutput.print("[." + v.m_obj +  "-" + (float)v.m_weight + " ");
				close2 = true;
			}
		}
		else
		{
			LangProcOutput.print("[." + v.m_obj + "-" + (float)v.m_weight + " ");
		}
		for(Subtree s : m_subtrees)
		{
			s.print_qtree(g, m_root_vectrex_id);
		}
		LangProcOutput.print(close2?" ] ]":" ]");
	}
	public void print_qtree(ChoiceGraph g) { print_qtree(g, -1); }
}

/*************************************************************************************
 * 
 * ChoiceGraph
 *
 */

public class ChoiceGraph
{
	private int m_max_vertexes;
	private int m_max_groups;
	
	private int m_num_groups;
	private int m_num_vertexes;
	
	private Vector<Vector<DirectedEdge>> m_edges;
	private Vector<Vector<Integer>> m_group_vertices;
	private Vector<Vertex> m_vertexes;
	private int[] m_vertex2group;
	private HashMap<Object, Vertex> m_object2vertex_map = new HashMap<Object, Vertex>();
	
	public ChoiceGraph(int max_groups, int max_vertexes)
	{
		m_max_groups = max_groups;
		m_max_vertexes = max_vertexes;
		m_edges = new Vector<Vector<DirectedEdge>>(max_vertexes);
		m_edges.setSize(max_vertexes);
		
		for( int i=0;i<m_edges.size(); ++i)
		{
			Vector<DirectedEdge> vve = new Vector<DirectedEdge>(max_vertexes);
			vve.setSize(max_vertexes);
			m_edges.set(i,  vve );
		}
		m_group_vertices = new Vector<Vector<Integer>>(m_max_groups);
		m_group_vertices.setSize(m_max_groups);
		for( int i=0;i<m_group_vertices.size(); ++i)
		{
			m_group_vertices.set(i,  new Vector<Integer>() );	
		}
		
		m_vertexes = new Vector<Vertex>(m_max_vertexes);
		m_vertex2group = new int[m_max_vertexes];
		m_num_groups = 0;
		m_num_vertexes = 0;
	}
	public Vertex vertexById(int vert_id) { return m_vertexes.get(vert_id); }
	public double getVertexWeight(int vert_id) { return m_vertexes.get(vert_id).m_weight; }
	public int getVertexGroup(int vert_id) { return m_vertex2group[vert_id]; }
	public int getVertexChoiceIndexInGroup(int vert_id)
	{
		// search when we know that these vertexes go sequentially
		return vert_id - getVertexIdByGroupAndChoice( getVertexGroup(vert_id), 0);
//		int ci = getVertexGroup(vert_id);
//		Vector<Integer> group_verts = m_group_vertices.get(ci);
//		for(int i=0; i<group_verts.size(); ++i )
//		{
 //			if (vert_id == group_verts.get(i)) return i;
//		}
//		return -1;
	}
	double getMaxVertexWeightInGroup(int group_id)
	{
		int n = getNumVerticesInGroup(group_id);
		double max = getVertexIdByGroupAndChoice(group_id, 0);
		for(int i=1; i<n;++i)
		{
			double w = getVertexWeight(getVertexIdByGroupAndChoice(group_id, i));
			if (w>max) max = w;
		}
		return max;
	}
	
	double getMaxEdgeWeightBetweenGroups(int group_id1, int group_id2)
	{
		int n1 = getNumVerticesInGroup(group_id1);
		int n2 = getNumVerticesInGroup(group_id2);
		double max = 0.0;
		for(int i=0; i<n1;++i) 	for(int j=0; j<n2;++j)
		{
			double w = getEdgeWeight(getVertexIdByGroupAndChoice(group_id1, i),
									 getVertexIdByGroupAndChoice(group_id2, j)  );
			if (w>max) max = w;
		}
		return max;
	}
	
	public int getVertexIdByGroupAndChoice(int group_id, int choice_id)
	{
		return m_group_vertices.get(group_id).get(choice_id);
	}
	public int getNumVerticesInGroup(int group_id)
	{
		return m_group_vertices.get(group_id).size();
	}
	private Vector<Integer> getGroupVertexes(int group_id)
	{
		return m_group_vertices.get(group_id);
	}
	
	public int getNumVertexes() { return m_num_vertexes; }
	public int getNumGroups() { return m_num_groups; }

	public DirectedEdge getEdge(int v1, int v2) { return m_edges.get(v1).get(v2);}
	public double getEdgeWeight(int v1, int v2)
	{
		DirectedEdge e = getEdge(v1,v2);
		return e==null ? 0.0 : e.m_weight;
	}
	
	public void addVertex(Object vert_o, double weight, boolean new_group)
	{
		if (LangProcSettings.DEBUG_OUTPUT)
		{
			LangProcOutput.println("Add vertex (" + (float)weight + ") " + vert_o);
		}
		
		if (new_group && m_num_groups >= m_max_groups) throw new java.lang.IndexOutOfBoundsException("No more groups");
		if (m_num_vertexes >= m_max_vertexes) throw new java.lang.IndexOutOfBoundsException("No more vertexes");
		if (!new_group && m_num_groups==0) throw new java.lang.IndexOutOfBoundsException("No group is allocated yet. Use new_group = true!!!");
		
		int group_id = new_group? m_num_groups++ : m_num_groups-1;
		int vert_id = m_num_vertexes++;
		
		Vertex v = new Vertex(vert_o, group_id, vert_id, weight);
		m_vertexes.addElement(v);
		
		m_group_vertices.get(group_id).addElement(vert_id);		
		m_vertex2group[vert_id] = group_id;
		m_object2vertex_map.put(vert_o, v);
	}
	
	public void addEdge(Object edge_o, double weight, int v_id1, int v_id2)
	{
		//java.text.DecimalFormatSymbols dfs = new java.text.DecimalFormatSymbols(new java.util.Locale("en"));
		//java.text.DecimalFormat df = new java.text.DecimalFormat("#.###", dfs);
		//LangProcOutput.println("(" + v_id1 + ") edge node [right] {" + df.format(weight) + "} ("+ v_id2 +")");
		assert(v_id1!=v_id2);
		
		assert( getVertexGroup(v_id1)!=getVertexGroup(v_id2) );
		
		DirectedEdge edge_old = m_edges.get(v_id1).get(v_id2);
		if (edge_old!=null && edge_old.m_weight >= weight) return;
		
		DirectedEdge e = new DirectedEdge(edge_o, weight);
		m_edges.get(v_id1).set(v_id2, e);
	}
	
	public void addEdge(Object edge_o, double weight, Object vert_o1, Object vert_o2)
	{
		if (weight==0.0) return;
		
		if (LangProcSettings.DEBUG_OUTPUT)
		{
			LangProcOutput.println("Add edge (" + (float)weight + ") " + edge_o + "(" + vert_o1 + "->" + vert_o2+")");
		}
		int v_id1 = m_object2vertex_map.get(vert_o1).m_vectex_id;
		int v_id2 = m_object2vertex_map.get(vert_o2).m_vectex_id;
		
		addEdge(edge_o, weight, v_id1, v_id2);
	}
	
	public boolean hasEdge(int v_id1, int v_id2)
	{
		return m_edges.get(v_id1).get(v_id2)!=null;
	}
	
	public boolean addToVectorIfGood(Vector<Subtree> v, Subtree s)
	{
		final int hypo_limit = 3;
		
		double min_weight = Double.MAX_VALUE;
		int min_index = 0;

		for(int i=0; i<v.size();++i)
		{
			Subtree sv = v.get(i);
			
			if (s.isBetterThan(sv))
			{
				v.set(i, s);
				return true;
			}
			
			if (sv.m_total_weight < min_weight)
			{
				min_weight = sv.m_total_weight;
				min_index = i;
			}
			if (sv.isBetterThan(s)) return false;
		}
		
		// there is no supersession
		if (v.size()<hypo_limit)
		{
			v.addElement(s);
			return true;
		}
		else if (min_weight < s.m_total_weight)
		{
			v.set(min_index, s);
			return true;
		}
		return false;
	}
	
	void dumpBiggest(Vector< Vector<Subtree> > trees)
	{
		for(Vector<Subtree> vt : trees)
		{
			java.util.Collections.sort(vt);
			vt.get(vt.size()-1).print(this);
			LangProcOutput.println("------------------");
		}
	}
	
	AdjacencyList getMaxBranchingByChoices(int choice_indexes[])
	{
		Node root = new Node(-1);
		Node[] nodes = new Node[m_num_groups];
	
		AdjacencyList myEdges = new AdjacencyList();
		
		for(int i=0;i<choice_indexes.length;++i)
		{
			int v_i = getVertexIdByGroupAndChoice(i,choice_indexes[i]);
			nodes[i] = new Node(v_i);
			myEdges.addEdge(root, nodes[i], getVertexWeight(v_i) );
		}
		
		for(int i=0;i<m_num_groups;++i) for(int j=0;j<m_num_groups;++j)
		{
			int v_i = getVertexIdByGroupAndChoice(i, choice_indexes[i]);
			int v_j = getVertexIdByGroupAndChoice(j, choice_indexes[j]);
			
			DirectedEdge e = m_edges.get(v_i).get(v_j);
			if ( e!=null)
			{
				//LangProcOutput.println("" + v_i + "->" + v_j + " w=" + e.m_weight + " vw=" + getVertexWeight(v_j));
				myEdges.addEdge(nodes[i], nodes[j], 1000 + e.m_weight + getVertexWeight(v_j) );
			}
		}
		
		Edmonds myed = new Edmonds_Andre();
		AdjacencyList rBranch;
	    rBranch = myed.getMaxBranching(root, myEdges);
	    //dumpBranching(rBranch);
	    return rBranch;
	}
	
	// max branching when we take maximum by every hipothesis edge
	AdjacencyList getMaxBranchingByMerge()
	{
		Node root = new Node(-1);
		Node[] nodes = new Node[m_num_groups];
	
		AdjacencyList myEdges = new AdjacencyList();
		
		for(int i=0;i<m_num_groups;++i)
		{
			nodes[i] = new Node(m_num_groups);
			myEdges.addEdge(root, nodes[i], getMaxVertexWeightInGroup(i) );
		}
		
		for(int i=0;i<m_num_groups;++i) for(int j=0;j<m_num_groups;++j)
		{
			double edge_w = getMaxEdgeWeightBetweenGroups(i,j);
			if (edge_w>0.0)
			{
				myEdges.addEdge(nodes[i], nodes[j], 1000 + edge_w + getMaxVertexWeightInGroup(j) );
			}
		}
		
		Edmonds myed = new Edmonds_Andre();	
		AdjacencyList rBranch;
	    rBranch = myed.getMaxBranching(root, myEdges);
	    //dumpBranching(rBranch);
	    return rBranch;
	}
	
	void dumpBranching(AdjacencyList branching)
	{
		LangProcOutput.println("dumpBranching");
	    for( com.altmann.Edge e : branching.getAllEdges())
	    {
	    	LangProcOutput.println(e);
	    }
	}
	
	boolean isAcceptable(AdjacencyList branching)
	{
		int num_roots = 0;
	    for( com.altmann.Edge e : branching.getAllEdges())
	    {
	    	if (e.getSource().name==-1) ++num_roots;
	    }
	    return num_roots==1;
	}
	
	Subtree createSubTreeFromBranching(AdjacencyList branching)
	{
		Subtree tree_root = null;
	    Subtree tree_nodes[] = new Subtree[m_num_groups];

		
	    for( com.altmann.Edge e : branching.getAllEdges())
	    {
	    	int vi = e.getDest().name;
	    	tree_nodes[m_vertex2group[vi]] = new Subtree(this, vi);
	    }
	    
	    for( com.altmann.Edge e : branching.getAllEdges())
	    {
	    	//LangProcOutput.println(e);
	    	if (e.getSource().name==-1)
	    	{
	    		tree_root = tree_nodes[m_vertex2group[e.getDest().name]];
	    	}
	    	else
	    	{
	    		int v1 = e.getSource().name;
	    		int v2 = e.getDest().name;
	    		tree_nodes[m_vertex2group[v1]].updateCoveredGroups();
	    		tree_nodes[m_vertex2group[v2]].updateCoveredGroups();
	    		tree_nodes[m_vertex2group[v1]].addSubtree(this, tree_nodes[m_vertex2group[v2]]);
	    	}
	    }
	    
	    tree_root.updateWeight(this, 0.0);
	    
	    //LangProcOutput.println("Total = " + max_total);
	    //tree_root.print(this);
	    //LangProcOutput.println("Total weight = " + tree_root.m_total_weight );
		return tree_root;
	}
	
	int[] calculateGroupParentsFromAdjacencyList(AdjacencyList branching)
	{
		int group_parents[] = new int[m_num_groups];
		
	    for( com.altmann.Edge e : branching.getAllEdges())
	    {
	    	int v1 = e.getSource().name;
	    	int v2 = e.getDest().name;
	    	
	    	//LangProcOutput.println("" + v1 + "->" + v2);
	    	
	    	int c1 = v1==-1?-1:m_vertex2group[v1];
	    	int c2 = m_vertex2group[v2];
	    	
	    	group_parents[c2] = c1;
	    }
	    return group_parents;
	}
	int getRootGroupFromParents(int[] group_parents)
	{
		for(int i=0;i<group_parents.length;++i)
		{
			if (group_parents[i]==-1) return i;
		}
		return -1;
	}
	int[] calculateGroupRanks(int group_parents[])
	{
		int group_rank[] = new int[m_num_groups];
		for(int i=0;i<m_num_groups;++i)
		{
			for(int j=0;j<m_num_groups;++j)
			{
				if (group_parents[j]!=-1)
				{
					if (group_rank[group_parents[j]] < group_rank[j]+1)
						group_rank[group_parents[j]]=group_rank[j]+1;
				}
			}		
		}
		return group_rank;
	}
	public void printSubtreeFromRootVertex(int root_vi, int best_connection[][], double subtree_weight[])
	{
		// best_connection[vertex_id][child_group] -> (child_vertex || -1)
		// subtree_weight[vertex_id]->weight
		
		LangProcOutput.println( "Subtree (" + root_vi +  ") w = " + subtree_weight[root_vi] );
		for(int i=0;i<m_num_groups;++i)
		{
			if (best_connection[root_vi][i]!=-1)
			{
				LangProcOutput.print("Connect " + root_vi +" -> ");
				printSubtreeFromRootVertex(best_connection[root_vi][i], best_connection, subtree_weight );
			}
		}
	}
	
	public void fillVertexChoice(int root_vi, int best_connection[][], int vertex_choice[])
	{
		int gr = getVertexGroup(root_vi);
		vertex_choice[ gr ] = getVertexChoiceIndexInGroup( root_vi );
		for(int i=0;i<m_num_groups;++i)
		{
			if (best_connection[root_vi][i]!=-1)
			{
				fillVertexChoice(best_connection[root_vi][i], best_connection, vertex_choice );
			}
		}
	}
	
	public double getBranchingWeight(AdjacencyList branching)
	{
		double total = 0;
	    for( com.altmann.Edge e : branching.getAllEdges())
	    {
	    	//LangProcOutput.println(e);
	    	total += e.getWeight();
	    }
	    return total;
	}
	
	public int[] optimizeVertexSelection(AdjacencyList branching)
	{
		// dynamic programming
		// alloc max_weight for each possible subtree root
		double subtree_weight[] = new double[m_num_vertexes];

		// connection from a vertex to the best subtree (subtree group_id->connected vertex_id)
		int best_connection[][] = new int[m_num_vertexes][m_num_groups];
		
		int group_parents[] = calculateGroupParentsFromAdjacencyList(branching);
		int group_rank[] = calculateGroupRanks(group_parents);
		int root_group = getRootGroupFromParents(group_parents);
//		LangProcOutput.println("Root = " + root_group);
//		for(int i=0;i<m_num_groups;++i) LangProcOutput.print(" " + group_parents[i]);
//		LangProcOutput.println();
//		for(int i=0;i<m_num_groups;++i) LangProcOutput.print(" " + group_rank[i]);
//		LangProcOutput.println();
		
		for(int rank=0; rank<m_num_groups; ++rank)
		{
			for(int group=0;group<m_num_groups;++group)
			{
				if (group_rank[group]==rank)
				{
					// for all groups from the lowest rank to highest recalculate possible outcome
					// recalculate group selection based on weights
					Vector<Integer> group_verts = getGroupVertexes(group);
					int num_ci = group_verts.size();
					for(int ci=0; ci < num_ci; ++ci) // for all choice vertexes
					{
						int vi = group_verts.get(ci);
						// set initial weight to vertex weight only
						subtree_weight[vi] = getVertexWeight(vi);
						
						// go through all possible children groups and add best subtrees to the weight 
						for(int child_group=0; child_group<m_num_groups; ++child_group)
						{
							// clean connection array
							best_connection[vi][child_group] = -1;
							// if not connected in the given tree continue
							if (group_parents[child_group]!=group) continue;
							
							double max_child_weight = 0;
								
							//LangProcOutput.println("Optimize " + vi + "(" + group + ")" + " -> " + child_group);
							int num_cci = getNumVerticesInGroup(child_group);
							for(int cci=0; cci < num_cci; ++cci)
							{
								// get possible child vertex 
								int cvi = getVertexIdByGroupAndChoice(child_group, cci);
								
								DirectedEdge e = m_edges.get(vi).get(cvi);
								if (e==null)
								{
									//LangProcOutput.println("Vertex " + vi + " and " + cvi + " are not connected");
									continue;
								}
								
								double weight = e.m_weight + subtree_weight[cvi];
								
								if (weight > max_child_weight)
								{
									max_child_weight = weight;
									best_connection[vi][child_group] = cvi;
								}
							}
							
							// what to do if we can't connect at all???
							subtree_weight[vi] += max_child_weight;
						}
					}
					
					if (group == root_group)
					{
						int best_ci = 0;
						double best_weight = subtree_weight[group_verts.get(0)];
						
//						LangProcOutput.println("best_weight = " + best_weight);
//						
//						for(int vi = 0; vi<m_num_vertexes; ++vi)
//						{
//							LangProcOutput.print(subtree_weight[vi] + " ");
//						}
//						LangProcOutput.println();
//						LangProcOutput.println("Possible connection matrix:");
//						
//						for(int vi = 0; vi<m_num_vertexes; ++vi)
//						{
//							for(int chi = 0; chi<m_num_groups; ++chi)
//							{
//								LangProcOutput.print(best_connection[vi][chi] + " ");
//							}
//							LangProcOutput.println();
//						}
						
						for(int ci=1; ci < num_ci; ++ci) // for all other vertexes in the group
						{
							if (subtree_weight[group_verts.get(ci)] > best_weight)
							{
								best_weight = subtree_weight[group_verts.get(ci)];
								best_ci = ci;
							}
						}
						
						int best_vi = getVertexIdByGroupAndChoice(group, best_ci);
						//printSubtreeFromRootVertex(best_vi, best_connection, subtree_weight);
						int vertex_choice[] = new int[m_num_groups];
						fillVertexChoice(best_vi, best_connection, vertex_choice);
						return vertex_choice;
					}
				}
			}
		}
		return null;
	}
	
	public Subtree ExhaustiveEdmondSearch()
	{
		if (m_num_groups == 0)
		{
			LangProcOutput.println("No groups. Return!!!");
			return null;
		}
		
		int choice_indexes[] = new int[m_num_groups];
		for(int i=0;i<choice_indexes.length;++i) choice_indexes[i] = 0;
			
		boolean finished = false;
		
		double max_total = 0;
		AdjacencyList maxBranch=null;
		int max_indexes[] = new int[m_num_groups];
		
		// estimate maximum :-)
//		AdjacencyList rBranch1 = getMaxBranchingByMerge();
//		double total_est = 0.0;
//		for( com.altmann.Edge e : rBranch1.getAllEdges() )
//		{
//			total_est += e.getWeight();
//		}
//		LangProcOutput.println("Estimated max weight = " + total_est);
		
		
		for(;;)
		{
			AdjacencyList rBranch = getMaxBranchingByChoices(choice_indexes);
			
			if (isAcceptable(rBranch))
			{
			    double total = getBranchingWeight(rBranch);
			    //LangProcOutput.println("Total = " + total);
			    
			    if (total > max_total)
			    {
			    	max_total = total;
			    	maxBranch = rBranch;
			    	System.arraycopy( choice_indexes, 0, max_indexes, 0, choice_indexes.length );
			    }
			}

		    // update choices to check the next case
			for(int i=0;i<choice_indexes.length;++i)
			{
				if ( choice_indexes[i]+1 == getNumVerticesInGroup(i) )
				{
					choice_indexes[i] = 0;
					if (i==choice_indexes.length-1)
					{
						finished = true;
					}
				}
				else
				{
					choice_indexes[i]+=1;
					break;
				}
			}

			//for(int i=0;i<indexes.length;++i) LangProcOutput.print(" " + indexes[i]);
			//LangProcOutput.println();
			
			if (finished) break;
		}
		

		// calculate maximum tree from obtained maximum branching
	    
		if (maxBranch==null) return null;
		
	    Subtree st = createSubTreeFromBranching(maxBranch);
//	    LangProcOutput.println("Total weight = " + st.m_total_weight);
	    return st;
	}
	
	public Subtree randomizedEdmondSearch(int num_iter, boolean use_optimize, boolean use_bayesian_stochastic)
	{
		int choice_indexes[] = new int[m_num_groups];
		java.util.Random random = new java.util.Random();
					
		double max_total = 0;
		AdjacencyList maxBranch=null;
		int max_indexes[] = new int[m_num_groups];
		
		float total_max_decisions[] = new float[getNumVertexes()];
		float total_vertex_weight_histogr[] = new float[getNumVertexes()];
		
		for(int j=0;j<getNumVertexes();++j)
		{
			total_max_decisions[j]=1.0f;
		}
		
		
		for(int num=0;num<num_iter;++num)
		{
			for(int i=0;i<m_num_groups;++i)
			{
				if (use_bayesian_stochastic)
				{
					int n_vert_in_gr = getNumVerticesInGroup(i);
					float total_v_weight = 0;
					for(int j=0; j<n_vert_in_gr;++j)
					{
						float vw = 1.0f + total_max_decisions[ getVertexIdByGroupAndChoice(i, j) ];
						total_v_weight += vw;
						total_vertex_weight_histogr[j] = total_v_weight;
					}
					float rand = (float)(random.nextDouble() * total_v_weight);
					
					int choice = 0;
					for(choice=0; choice<n_vert_in_gr;++choice)
					{
						if (total_vertex_weight_histogr[choice] >= rand ) break;
					}
					choice_indexes[i] = choice;
				}
				else
				{
					choice_indexes[i] = random.nextInt(getNumVerticesInGroup(i));
				}
			}
			
			AdjacencyList rBranch = getMaxBranchingByChoices(choice_indexes);
			
			if (isAcceptable(rBranch))
			{
			    double total = getBranchingWeight(rBranch);
			    //LangProcOutput.println("Accepted total = " + total);

			    // optimise until we stuck in local optimum
			    while (use_optimize)
			    {
				    int new_choices[] = optimizeVertexSelection(rBranch);
				    AdjacencyList rBranch1 = getMaxBranchingByChoices(new_choices);
				    if (isAcceptable(rBranch1))
				    {
				    	double new_weight = getBranchingWeight(rBranch1);
				    	if (new_weight>total)
				    	{
				    		//LangProcOutput.println("Improve " + total + "->" + new_weight);
				    		rBranch = rBranch1;
				    		total = new_weight;
				    	}
				    	else
				    	{
				    		break;
				    	}
				    }
				    else
				    {
				    	break;
				    }
			    }
			    
			    if (total > max_total)
			    {
			    	max_total = total;
			    	maxBranch = rBranch;
			    	System.arraycopy( choice_indexes, 0, max_indexes, 0, choice_indexes.length );
			    	
					if (use_bayesian_stochastic)
					{
//						int nv = getNumVertexes();
//						for(int j=0;j<nv;++j)
//						{
//							total_max_decisions[j]*=0.9f;
//						}

						
						for(int i=0;i<m_num_groups;++i)
						{
							total_max_decisions[ getVertexIdByGroupAndChoice(i, choice_indexes[i]) ]+=1.0f;
						}
					}
			    }
			    else
			    {
					if (use_bayesian_stochastic)
					{
						for(int i=0;i<m_num_groups;++i)
						{
							total_max_decisions[ getVertexIdByGroupAndChoice(i, choice_indexes[i]) ]*=0.9f;
						}
					}
			    }
			}
		}
	    
		if (maxBranch==null)
		{
			//LangProcOutput.println("----------- branching was not found ");
			return null;
		}
	    //LangProcOutput.println("-------------- not need just test ");
	    
	    return createSubTreeFromBranching(maxBranch);
	}

	
	public Subtree growingTreesSearch()
	{
		if (m_num_vertexes==0) return null;
		// create trees starting from each vertex
		Vector< Vector<Subtree> > trees = new Vector< Vector<Subtree> >(m_num_vertexes);	
		for(int i=0;i<m_num_vertexes;++i) trees.addElement( new Vector<Subtree>() );

		Subtree max_weighted_tree = new Subtree(this, 0);
		
		// fill single-vertex subtrees
		for(int i=0;i<m_num_vertexes;++i)
		{
			Subtree ns = new Subtree(this, i);
			trees.get(i).addElement(ns);
			if (ns.getBalancedWeight() > max_weighted_tree.getBalancedWeight()) max_weighted_tree = ns;
		}
		
		
		// grow trees from each vertex by adding neighbour
		int num_changes = 0;
		int total_tests = 0;
		int subtree_aditions = 0;
		do
		{
			num_changes = 0;
			for(int c=0;c<m_num_vertexes;++c)
			{
				// process hypotheses. Note, than trees.get(c) can be extended with new hypotheses dynamically
				for(int i = 0; i < trees.get(c).size(); ++i)
				{
					Subtree sc = trees.get(c).get(i);
					for(int j=0;j<m_num_vertexes;++j)
					{
						if (j==c) continue;
						for( Subtree sj : trees.get(j) )
						{
							++total_tests;
							if (sc.canAttachDirectSubtree(this, sj))
							{
								sc.checkConsistency();
								sj.checkConsistency();
								Subtree sc_copy = new Subtree(sc);
								Subtree sj_copy = new Subtree(sj);
								if (sc_copy.addSubtree(this, sj_copy))
								{
									sc_copy.checkConsistency();
									if (sc_copy.getBalancedWeight() > max_weighted_tree.getBalancedWeight())
									{
										max_weighted_tree = sc_copy;
									}
									++subtree_aditions;
									if ( addToVectorIfGood(trees.get(c), sc_copy) ) ++num_changes;
								}
							}
						}
					}
				}
			}
			//LangProcOutput.println("num_changes = " + num_changes +
					//", total_tests = " + total_tests + ", subtree_aditions = " + subtree_aditions);

		} while (num_changes>0);
		
		
		//dumpBiggest(trees);
		
		//LangProcOutput.println("0 to 9 ---------------------");
		
		//Subtree sc_copy1 = new Subtree(trees.get(9).get(0));
		//Subtree sj_copy1 = new Subtree(trees.get(0).get(0));
		//if (sc_copy1.addSubtree(this, sj_copy1))
		//{
		//	sc_copy1.print(this);
		//}

		
		return max_weighted_tree;
	}
	
	public double getComplexity()
	{
		double compl = 1;
		for(int gi=0;gi<m_num_groups;++gi)
		{
			compl *= getNumVerticesInGroup(gi);
		}
		return compl;
	}
	
	public void printComplexity()
	{
		LangProcOutput.println("Num groups = " + m_num_groups);
		LangProcOutput.print("Complexity ");
		double compl = 1;
		for(int gi=0;gi<m_num_groups;++gi)
		{
			LangProcOutput.print( (gi>0?"*":"") + getNumVerticesInGroup(gi) );
			compl *= getNumVerticesInGroup(gi);
		}
		LangProcOutput.println(" = " + compl);
	}
	
	public void printComplexityShort()
	{
		//LangProcOutput.println("Num groups = " + m_num_groups);
		//LangProcOutput.print("Complexity ");
		double compl = 1;
		for(int gi=0;gi<m_num_groups;++gi)
		{
			//LangProcOutput.print( (gi>0?"*":"") + getNumVerticesInGroup(gi) );
			compl *= getNumVerticesInGroup(gi);
		}
		LangProcOutput.print("" + compl);
	}
	
	public void print()
	{
		//LangProcOutput.println(m_vertexes);
		LangProcOutput.println(m_group_vertices);
		//LangProcOutput.println(m_edges);
		LangProcOutput.println(m_object2vertex_map);
		LangProcOutput.println(m_vertex2group);
		
		for(int i=0;i<m_num_vertexes;++i)
		{
			for(int j=0;j<m_num_vertexes;++j)
			{
				char c='?';
				if (getVertexGroup(i)==getVertexGroup(j)) c='*';
				else if ( !hasEdge(i,j) ) c='.';
				else if (getEdgeWeight(i,j)>0.9) c='9';
				else if (getEdgeWeight(i,j)>0.8) c='8';
				else if (getEdgeWeight(i,j)>0.7) c='7';
				else if (getEdgeWeight(i,j)>0.6) c='6';
				else if (getEdgeWeight(i,j)>0.5) c='5';
				else if (getEdgeWeight(i,j)>0.4) c='4';
				else if (getEdgeWeight(i,j)>0.3) c='3';
				else if (getEdgeWeight(i,j)>0.2) c='2';
				else if (getEdgeWeight(i,j)>0.1) c='1';
				else if (getEdgeWeight(i,j)>=0.0) c='0';
				
				LangProcOutput.print(c);
				LangProcOutput.print(' ');
			}
			LangProcOutput.println();
		}
		printComplexity();
	}
	
	void print_dependencies()
	{
		LangProcOutput.println("\\begin{dependency}");
		LangProcOutput.println("\\begin{deptext}[column sep=1cm]");
		
		for(int i=0;i<m_num_vertexes;++i)
		{
			if (i!=0) LangProcOutput.print(" \\& ");

			try
			{
				TaggedWord tw = (TaggedWord)m_vertexes.get(i).m_obj;
				LangProcOutput.print(tw.m_word + " ");
			}
			catch(java.lang.Exception e)
			{
				LangProcOutput.print(m_vertexes.get(i).m_obj);
			}
		}
		LangProcOutput.println("\\\\");
		
		for(int i=0;i<m_num_vertexes;++i)
		{
			if (i!=0) LangProcOutput.print(" \\& ");

			try
			{
				TaggedWord tw = (TaggedWord)m_vertexes.get(i).m_obj;
				LangProcOutput.print("(" + tw.m_base_word + ") ");
			}
			catch(java.lang.Exception e)
			{
			}
		}
		LangProcOutput.println("\\\\");
		
		for(int i=0;i<m_num_vertexes;++i)
		{
			if (i!=0) LangProcOutput.print(" \\& ");
			LangProcOutput.print( (float)m_vertexes.get(i).m_weight);
		}
		LangProcOutput.println("\\\\");
		
		// My \& dog \& also \& likes \& eating \& sausage \\
		LangProcOutput.println("\\end{deptext}");
		
		for(int i=0;i<m_num_vertexes;++i) for(int j=0;j<m_num_vertexes;++j)
		{
			if (m_edges.get(i).get(j)!=null
					&& m_edges.get(i).get(j).m_weight>LangProcSettings.OPTIONAL_WEIGHT
					)
			{
				LangProcOutput.println("\\depedge{"+(i+1)+"}{"+(j+1)+"}{" + m_edges.get(i).get(j) + "}");
			}
		}
		
		LangProcOutput.println("\\end{dependency}");
	}
	
	
	static public void testOnce()
	{
		try
		{
			final int num_verts = 30;
			final int num_edges = num_verts * num_verts / 2; // edge coverage
			final int avg_grouping = 3;	// average 3 vertex in group
			final int max_grouping = 5;
			ChoiceGraph cg = new ChoiceGraph(num_verts, num_verts);
			
			int group_id = -1;
			int num_in_group = 0;
			java.util.Random random = new java.util.Random();
			
			for(int i = 0; i<num_verts; ++i)
			{
				boolean new_group = num_in_group >= max_grouping-1 ? true : random.nextInt(avg_grouping)==0;
				if (new_group || i==0)
				{
					++group_id;
					num_in_group = 0;
				}
				else
				{
					++num_in_group;
				}
				cg.addVertex("v" + i + "(" + group_id + ")",
						random.nextDouble(),
						new_group || i==0);
			}
			
			for(int j = 0; j<num_edges; ++j)
			{
				int v1 = random.nextInt(num_verts);
				int v2 = random.nextInt(num_verts);
				
				while (cg.getVertexGroup(v1)==cg.getVertexGroup(v2) ||
						cg.hasEdge(v1,v2) )
				{
					v1 = random.nextInt(num_verts);
					v2 = random.nextInt(num_verts);
				}
				
				cg.addEdge("e"+v1+"_"+v2+"", random.nextDouble(), v1,v2);
			}
			
//			cg.addVertex("v1_1", 1.0f, true);
//			cg.addVertex("v1_2", 0.8f, false);
//			cg.addVertex("v2_1", 0.6f, true);
//			cg.addVertex("v2_2", 0.9f, false);
//			cg.addVertex("v3_1", 1.0f, true);
//			cg.addVertex("v3_2", 0.5f, false);
//			cg.addVertex("v4_1", 0.2f, true);
//			cg.addVertex("v4_2", 0.3f, false);
//			cg.addVertex("v5_1", 0.6f, true);
//			cg.addVertex("v5_2", 0.9f, false);
//			
//			cg.addEdge("e_1_2_1", 1.0f, "v1_1", "v2_1");
//			cg.addEdge("e_1_3_1", 0.4f, "v1_1", "v3_1");
//			cg.addEdge("e_3_4_1", 1.0f, "v3_1", "v4_1");
//			cg.addEdge("e_3_5_1", 0.7f, "v3_1", "v5_1");
//			cg.addEdge("e_1_2_2", 1.0f, "v1_2", "v2_2");
//			cg.addEdge("e_1_3_2", 0.4f, "v1_2", "v3_2");
//			cg.addEdge("e_3_4_2", 1.0f, "v3_2", "v4_2");
//			cg.addEdge("e_3_5_2", 0.7f, "v3_2", "v5_2");
//			
//			cg.addEdge("e_1_2_2", 1.0f, "v1_1", "v2_2");
//			cg.addEdge("e_1_3_2", 0.4f, "v1_2", "v3_1");
//			cg.addEdge("e_3_4_2", 1.0f, "v3_1", "v4_2");
//			cg.addEdge("e_3_5_2", 0.7f, "v3_2", "v5_1");
			
			cg.printComplexity();
			
			LangProcOutput.println("\nExhaustive Edmond Search");
			long t3_0 = System.nanoTime();
			Subtree st3 = cg.ExhaustiveEdmondSearch();
			long t3_1 = System.nanoTime();
			LangProcOutput.println( "Time3 = " + (t3_1-t3_0)/1000 + " mikro-sec" );
			LangProcOutput.println("w3 = " + st3.m_total_weight);
			
			double optimal = st3.m_total_weight;
			long opt_time = t3_1-t3_0;
			
			LangProcOutput.println("\nRandomized Edmond search SIMPLE");
			long t1_0 = System.nanoTime();
			Subtree st1 = cg.randomizedEdmondSearch(1000, false, false);
			long t1_1 = System.nanoTime();
			LangProcOutput.println( "Time% = " + (float)(100.0)*(t1_1-t1_0)/opt_time + "%" );
			LangProcOutput.println("w% = " + (float)( 100.0 * (1.0 - st1.m_total_weight/optimal)));
			
			LangProcOutput.println("\nRandomized Edmond search OPTIMIZE");
			long t1o_0 = System.nanoTime();
			Subtree st1o = cg.randomizedEdmondSearch(1000, true, false);
			long t1o_1 = System.nanoTime();
			LangProcOutput.println( "Time% = " + (float)(100.0)*(t1o_1-t1o_0)/opt_time + "%" );
			LangProcOutput.println("w% = " + (float)( 100.0 * (1.0 - st1o.m_total_weight/optimal)));
			
			LangProcOutput.println("\nGrowing trees search");
			
			long t2_0 = System.nanoTime();
			Subtree st2 = cg.growingTreesSearch();
			long t2_1 = System.nanoTime();
			LangProcOutput.println( "Time% = " + (float)(100.0)*(t2_1-t2_0)/opt_time + "%" );
			LangProcOutput.println("w% = " + (float)( 100.0 * (1.0 - st2.m_total_weight/optimal)));
		}
		catch(java.lang.Exception e)
		{
			e.printStackTrace();
		}
	}
	
	static public void testTable(double arc_coverage, double pow)
	{
		try
		{
			final int num_verts = 35;
			final int num_arcs = (int)(arc_coverage * num_verts * num_verts); // arc coverage
			//final int avg_grouping = 3;	// average 3 vertex in group
			final int max_grouping = 4; 
			ChoiceGraph cg = new ChoiceGraph(num_verts, num_verts);
			
			int group_id = -1;
			int num_in_group = 0;
			java.util.Random random = new java.util.Random();
			
			for(int i = 0; i<num_verts; ++i)
			{
				// now groupping is fixed to perform strict time computations
				boolean new_group = num_in_group >= max_grouping-1 ? true : false/*random.nextInt(avg_grouping)==0*/;
				if (new_group || i==0)
				{
					++group_id;
					num_in_group = 0;
				}
				else
				{
					++num_in_group;
				}
				double rand = random.nextDouble();
				while(rand==0.0 || rand==1.0) rand = random.nextDouble();
				
				if (pow!=0.0)
				{
					rand = java.lang.Math.pow( -java.lang.Math.log(rand), pow ); 
				}
				
				cg.addVertex("v" + i + "(" + group_id + ")", rand, new_group || i==0);
			}
			
			for(int j = 0; j<num_arcs; ++j)
			{
				int v1 = random.nextInt(num_verts);
				int v2 = random.nextInt(num_verts);
				
				while (cg.getVertexGroup(v1)==cg.getVertexGroup(v2) ||
						cg.hasEdge(v1,v2) )
				{
					v1 = random.nextInt(num_verts);
					v2 = random.nextInt(num_verts);
				}
				
				double rand = random.nextDouble();
				while(rand==0.0 || rand==1.0) rand = random.nextDouble();
				
				if (pow!=0.0)
				{
					rand = java.lang.Math.pow( -java.lang.Math.log(rand), pow ); 
				}
				cg.addEdge("e"+v1+"_"+v2+"", rand, v1,v2);
			}
					
			cg.printComplexityShort();
			
			//LangProcOutput.println("\nExhaustive Edmond Search");
			long t3_0 = System.nanoTime();
			Subtree st3 = cg.ExhaustiveEdmondSearch();
			long t3_1 = System.nanoTime();
			LangProcOutput.print(" " + (t3_1-t3_0)/1000 );
			double optimal = 1.0;
			if (st3==null)
			{
				LangProcOutput.print(" 0.0");
			}
			else
			{
				LangProcOutput.print(" " + st3.m_total_weight);
				optimal = st3.m_total_weight;
			}
			
			long opt_time = t3_1-t3_0;
			
			//LangProcOutput.println("\nRandomized Edmond search SIMPLE");
			long t1_0 = System.nanoTime();
			Subtree st1 = cg.randomizedEdmondSearch(400, false, false);
			long t1_1 = System.nanoTime();
			LangProcOutput.print(" " + (float)(100.0)*(t1_1-t1_0)/opt_time );
			if (st1==null)
			{
				LangProcOutput.print(st3==null?" 0.0":" 100.0");
			}
			else
			{
				LangProcOutput.print(" " + (float)( 100.0 * (1.0 - st1.m_total_weight/optimal)));
			}
			
			//LangProcOutput.println("\nRandomized Edmond search OPTIMIZE");
			long t1o_0 = System.nanoTime();
			Subtree st1o = cg.randomizedEdmondSearch(100, true, false);
			long t1o_1 = System.nanoTime();
			LangProcOutput.print(" " + (float)(100.0)*(t1o_1-t1o_0)/opt_time );
			if (st1o==null)
			{
				LangProcOutput.print(st3==null?" 0.0":" 100.0");
			}
			else
			{
				LangProcOutput.print(" " + (float)( 100.0 * (1.0 - st1o.m_total_weight/optimal)));
			}
			
			//LangProcOutput.println("\nGrowing trees search");
			
//			long t2_0 = System.nanoTime();
//			Subtree st2 = cg.growingTreesSearch();
//			long t2_1 = System.nanoTime();
//			
//			LangProcOutput.print( " " + (float)(100.0)*(t2_1-t2_0)/opt_time );
//
//			if (st2==null)
//			{
//				LangProcOutput.print(st3==null?" 0.0":" 100.0");
//			}
//			else
//			{
//				LangProcOutput.print(" " + (float)( 100.0 * (1.0 - st2.m_total_weight/optimal)));
//			}
			LangProcOutput.println();
		}
		catch(java.lang.Exception e)
		{
			e.printStackTrace();
		}
	}
	
	
	static public void testMeanPerformance(int num_verts, int max_grouping, double arc_coverage, double pow)
	{
		long sum_time = 0;
		long sum_time_alg[] = {0,0,0,0};
		float sum_sqr_dev_alg[] = {0,0,0,0};
		float num_optimal[] = {0,0,0,0};
		
		final int num_random_searches = num_verts * num_verts;
		
		final int num_trials = 100;
		for(int n = 0; n<num_trials; ++n)
		{
			try
			{
				//final int num_verts = 35;
				final int num_arcs = (int)(arc_coverage * num_verts * num_verts); // arc coverage
				//final int avg_grouping = 3;	// average 3 vertex in group
				//final int max_grouping = 4; 
				ChoiceGraph cg = new ChoiceGraph(num_verts, num_verts);
				
				int group_id = -1;
				int num_in_group = 0;
				java.util.Random random = new java.util.Random();
				
				for(int i = 0; i<num_verts; ++i)
				{
					// now groupping is fixed to perform strict time computations
					boolean new_group = num_in_group >= max_grouping-1 ? true : false/*random.nextInt(avg_grouping)==0*/;
					if (new_group || i==0)
					{
						++group_id;
						num_in_group = 0;
					}
					else
					{
						++num_in_group;
					}
					double rand = random.nextDouble();
					while(rand==0.0 || rand==1.0) rand = random.nextDouble();
					
					if (pow!=0.0)
					{
						rand = java.lang.Math.pow( -java.lang.Math.log(rand), pow ); 
					}
					
					cg.addVertex("v" + i + "(" + group_id + ")", rand, new_group || i==0);
				}
				
				for(int j = 0; j<num_arcs; ++j)
				{
					int v1 = random.nextInt(num_verts);
					int v2 = random.nextInt(num_verts);
					
					while (cg.getVertexGroup(v1)==cg.getVertexGroup(v2) ||
							cg.hasEdge(v1,v2) )
					{
						v1 = random.nextInt(num_verts);
						v2 = random.nextInt(num_verts);
					}
					
					double rand = random.nextDouble();
					while(rand==0.0 || rand==1.0) rand = random.nextDouble();
					
					if (pow!=0.0)
					{
						rand = java.lang.Math.pow( -java.lang.Math.log(rand), pow ); 
					}
					cg.addEdge("e"+v1+"_"+v2+"", rand, v1,v2);
				}
						
				//cg.printComplexityShort();
				
				//LangProcOutput.println("\nExhaustive Edmond Search");
				long t1 = System.nanoTime();
				Subtree st = cg.ExhaustiveEdmondSearch();
				long t2 = System.nanoTime();
				//LangProcOutput.print(" " + (t3_1-t3_0)/1000 );
				float optimal = 1.0f;
				if (st==null)
				{
					--n;
					continue;
					//LangProcOutput.print(" 0.0");
				}
				else
				{
					//LangProcOutput.print(" " + st3.m_total_weight);
					optimal = (float)st.m_total_weight;
				}
				
				long opt_time = t2-t1;
				
				sum_time += opt_time;
				
				//LangProcOutput.println("\nRandomized Edmond search SIMPLE");
				t1 = System.nanoTime();
				st = cg.randomizedEdmondSearch(num_random_searches*4, false, false);
				t2 = System.nanoTime();
				//LangProcOutput.print(" " + (float)(100.0)*(t1_1-t1_0)/opt_time );
				
				sum_time_alg[0] += (t2 - t1);
				
				float dev = (float)(st==null ? 1.0 : (optimal-st.m_total_weight)/optimal);
				sum_sqr_dev_alg[0] += dev * dev;
				if (dev < 1E-6) num_optimal[0] +=1.0f;

				//LangProcOutput.println("\nRandomized Edmond search Bayesian");
				t1 = System.nanoTime();
				st = cg.randomizedEdmondSearch(num_random_searches*4, false, true);
				t2 = System.nanoTime();
				//LangProcOutput.print(" " + (float)(100.0)*(t1_1-t1_0)/opt_time );
				
				sum_time_alg[1] += (t2 - t1);
				
				dev = (float)(st==null ? 1.0 : (optimal-st.m_total_weight)/optimal);
				sum_sqr_dev_alg[1] += dev * dev;
				if (dev < 1E-6) num_optimal[1] += 1.0f;

					
				//LangProcOutput.println("\nRandomized Edmond search OPTIMIZE");
				t1 = System.nanoTime();
				st = cg.randomizedEdmondSearch(num_random_searches, true, false);
				t2 = System.nanoTime();
				
				//LangProcOutput.print(" " + (float)(100.0)*(t1o_1-t1o_0)/opt_time );
				sum_time_alg[2] += (t2 - t1);
				
				dev = (float)(st==null ? 1.0 : (optimal - st.m_total_weight)/optimal);
				sum_sqr_dev_alg[2] += dev * dev;
				if (dev < 1E-6) num_optimal[2] += 1.0f;
				
				
				//LangProcOutput.println("\nRandomized Edmond search OPTIMIZE");
				t1 = System.nanoTime();
				st = cg.randomizedEdmondSearch(num_random_searches, true, true);
				t2 = System.nanoTime();
				
				//LangProcOutput.print(" " + (float)(100.0)*(t1o_1-t1o_0)/opt_time );
				sum_time_alg[3] += (t2 - t1);
				
				dev = (float)(st==null ? 1.0 : (optimal - st.m_total_weight)/optimal);
				sum_sqr_dev_alg[3] += dev * dev;
				if (dev < 1E-6) num_optimal[3] += 1.0f;
				
				//LangProcOutput.println();
			}
			catch(java.lang.Exception e)
			{
				e.printStackTrace();
			}
		}
		
		LangProcOutput.print("" + num_verts + " " + (float)sum_time / 1.0e9 );
		
		for(int i=0;i<4;++i)
		{
			LangProcOutput.print(
					" " + (float)((float)sum_time_alg[i]/sum_time) +
					" " + (float)java.lang.Math.sqrt(sum_sqr_dev_alg[i]/num_trials) +
					" " + (float)java.lang.Math.sqrt(num_optimal[i]/num_trials ) );
		}
		
		LangProcOutput.println();
		
	}
	
	static public void selectTheBest(int num_verts, int max_grouping, double arc_coverage, double pow)
	{
		float sum_sqr_dev_alg1 = 0;
		float sum_sqr_dev_alg2 = 0;
		
		final int num_random_searches = num_verts * num_verts;
		
		final int num_trials = 100;
		for(int n = 0; n<num_trials; ++n)
		{
			try
			{
				//final int num_verts = 35;
				final int num_arcs = (int)(arc_coverage * num_verts * num_verts); // arc coverage
				//final int avg_grouping = 3;	// average 3 vertex in group
				//final int max_grouping = 4; 
				ChoiceGraph cg = new ChoiceGraph(num_verts, num_verts);
				
				int group_id = -1;
				int num_in_group = 0;
				java.util.Random random = new java.util.Random();
				
				for(int i = 0; i<num_verts; ++i)
				{
					// now groupping is fixed to perform strict time computations
					boolean new_group = num_in_group >= max_grouping-1 ? true : false/*random.nextInt(avg_grouping)==0*/;
					if (new_group || i==0)
					{
						++group_id;
						num_in_group = 0;
					}
					else
					{
						++num_in_group;
					}
					double rand = random.nextDouble();
					while(rand==0.0 || rand==1.0) rand = random.nextDouble();
					
					if (pow!=0.0)
					{
						rand = java.lang.Math.pow( -java.lang.Math.log(rand), pow ); 
					}
					
					cg.addVertex("v" + i + "(" + group_id + ")", rand, new_group || i==0);
				}
				
				for(int j = 0; j<num_arcs; ++j)
				{
					int v1 = random.nextInt(num_verts);
					int v2 = random.nextInt(num_verts);
					
					while (cg.getVertexGroup(v1)==cg.getVertexGroup(v2) ||
							cg.hasEdge(v1,v2) )
					{
						v1 = random.nextInt(num_verts);
						v2 = random.nextInt(num_verts);
					}
					
					double rand = random.nextDouble();
					while(rand==0.0 || rand==1.0) rand = random.nextDouble();
					
					if (pow!=0.0)
					{
						rand = java.lang.Math.pow( -java.lang.Math.log(rand), pow ); 
					}
					cg.addEdge("e"+v1+"_"+v2+"", rand, v1,v2);
				}
						

				//Subtree st1 = cg.randomizedEdmondSearch(num_random_searches*4, false, false);
				Subtree st1 = cg.randomizedEdmondSearch(num_random_searches, true, false);
				Subtree st1o = cg.randomizedEdmondSearch(num_random_searches, true, true);
				
				if (st1==null || st1o==null)
				{
					--n; continue;
				}
				
				float diff = (float)(st1.m_total_weight - st1o.m_total_weight);
				//LangProcOutput.println("" + st1.m_total_weight + " - " + st1o.m_total_weight + "\t\t" + diff);

				sum_sqr_dev_alg1 += diff<-1e-6? diff*diff : 0.0;
				sum_sqr_dev_alg2 += diff>+1e-6? diff*diff : 0.0;
				
//				sum_sqr_dev_alg1 += diff<=-1e-6? 1.0f : 1.0f;
//				sum_sqr_dev_alg2 += diff>=1e-6? 0.0f : 1.0f;
				
			}
			catch(java.lang.Exception e)
			{
				e.printStackTrace();
			}
		}
		
		LangProcOutput.println("" + num_verts + 
				" " + (float)java.lang.Math.sqrt(sum_sqr_dev_alg1/num_trials) +
				" " + (float)java.lang.Math.sqrt(sum_sqr_dev_alg2/num_trials)
				);
		
	}

	
	static public void test()
	{
		LangProcOutput.println("#Verts Base_time(sec) time1_frac time2_frac SQV1 SQV2");

		double arc_coverage = 0.4;  double pow = 1.0;
		//double arc_coverage = 0.4;  double pow = 0.0;
		//double arc_coverage = 0.4;  double pow = 0.0;
		
		LangProcOutput.println("Testing for " + arc_coverage + ";pow=" + pow);
		
		testMeanPerformance( 8, 4, arc_coverage, pow);
		testMeanPerformance(12, 4, arc_coverage, pow);
		testMeanPerformance(16, 4, arc_coverage, pow);
		testMeanPerformance(20, 4, arc_coverage, pow);
		testMeanPerformance(24, 4, arc_coverage, pow);
		testMeanPerformance(28, 4, arc_coverage, pow);
		testMeanPerformance(32, 4, arc_coverage, pow);
		testMeanPerformance(36, 4, arc_coverage, pow);
		
//		selectTheBest( 8, 4, arc_coverage, pow);
//		selectTheBest(12, 4, arc_coverage, pow);
//		selectTheBest(16, 4, arc_coverage, pow);
//		selectTheBest(20, 4, arc_coverage, pow);
//		selectTheBest(24, 4, arc_coverage, pow);
//		selectTheBest(28, 4, arc_coverage, pow);
//		System.out.flush();
//		selectTheBest(36, 4, arc_coverage, pow);
//		System.out.flush();
//		selectTheBest(40, 4, arc_coverage, pow);
//		System.out.flush();
//		testMeanPerformance(36, 4, arc_coverage, pow);
		
//		LangProcOutput.println("# Complexity Base_time(msk) Maximal_weight Alg1_time(%) Alg1_dev(%) Alg2_time(%) Alg2_dev(%) Alg3_time(%) Alg3_dev(%");
//		double arc_coverage = 0.4;
//		double pow = 1.0;
//		//LangProcOutput.println("Arc coverage=;" + arc_coverage + ";distribition=;" + pow);
//		for(int i=0;i<200;++i)
//		{
//			testTable(arc_coverage, pow);
//		}
	}
}


