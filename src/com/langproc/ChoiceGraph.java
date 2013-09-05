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
	public int m_choice_id;
	public int m_vectex_id;
	double m_weight;
	Object m_obj;
	
	Vertex(Object obj, int choice_id, int vectex_id, double weight)
	{
		m_obj = obj;
		m_choice_id = choice_id;
		m_vectex_id = vectex_id;
		m_weight = weight;
	}
	
	public String toString() { return "" + m_vectex_id + " [" + m_choice_id + "] " + m_weight + " " + m_obj; }
	
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
	
	public String toString() { return "" + m_weight + " " + m_obj; }
}

/*************************************************************************************
 * 
 * Subtree
 *
 */

class Subtree implements java.lang.Comparable<Subtree>
{
	double m_total_weight;
	int m_num_vertexes;
	int m_root_vectrex_id;
	int m_root_choice_id;
	boolean[] m_covered_choices;
	Vector<Subtree> m_subtrees = new Vector<Subtree>();
	
	public double getBalancedWeight() { return m_total_weight + m_num_vertexes * 10.0f; }
	
	public Subtree(ChoiceGraph g, int vertex_id)
	{
		Vertex v = g.vertexById(vertex_id);
		m_total_weight = v.m_weight;
		m_num_vertexes = 1;
		m_root_vectrex_id = vertex_id;
		m_root_choice_id = v.m_choice_id;
		m_covered_choices = new boolean[g.getNumChoices()];
		m_covered_choices[v.m_choice_id] = true;
		checkConsistency();
	}
	Subtree(Subtree s)
	{
		m_total_weight = s.m_total_weight;
		m_num_vertexes = s.m_num_vertexes;
		m_root_vectrex_id = s.m_root_vectrex_id;
		m_root_choice_id = s.m_root_choice_id;
		m_covered_choices = s.m_covered_choices.clone();
		for(Subtree s1 : s.m_subtrees)
		{
			m_subtrees.addElement(new Subtree(s1));
		}
		checkConsistency();
	}
	
	public int compareTo(Subtree o)
	{
		Subtree other = (Subtree)o;
		if (m_total_weight == other.m_total_weight) return 0;
		return m_total_weight < other.m_total_weight ? -1 : +1;
	}
	
	void checkConsistency()
	{
		for(int i=0;i<m_covered_choices.length;++i)
		{
			if (m_covered_choices[i])
			{
				Subtree s = findSubtreeByChoiceId(i);
				if (s==null) throw new java.lang.IndexOutOfBoundsException("no subtree with covered choice");
				if (numVertexesWithChoice(i)!=1)
				{
					throw new java.lang.IndexOutOfBoundsException("num choices != 1"); 
				}
			}
			else
			{
				Subtree s = findSubtreeByChoiceId(i);
				if (s!=null) throw new java.lang.IndexOutOfBoundsException("has Subtree with uncovered choice");
				if (numVertexesWithChoice(i)!=0) throw new java.lang.IndexOutOfBoundsException("num choices != 0"); 
			}		
		}
	}
	
	int numVertexesWithChoice(int choice_id)
	{
		int n = m_root_choice_id==choice_id ? 1 : 0;
		for(Subtree s : m_subtrees)
		{
			n+=s.numVertexesWithChoice(choice_id);
		}
		return n;
	}
	
	Subtree findSubtreeByChoiceId(int choice_id)
	{
		if (!m_covered_choices[choice_id]) return null;
		if (m_root_choice_id==choice_id) return this;
		for(Subtree s : m_subtrees)
		{
			Subtree r = s.findSubtreeByChoiceId(choice_id);
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
				int nc = m_covered_choices.length;
				for(int i=0;i<nc;++i)
				{
					if (s.m_covered_choices[i]) m_covered_choices[i]=false;
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
		if (other.m_covered_choices[m_root_choice_id])
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
		if (other.m_covered_choices[m_root_choice_id])
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
		
//		double remove_w = getRemovalWeight(other);
//		
//		if (remove_w < other.m_total_weight + de.m_weight) return true;
//		
//		// some additional check here ...
//		int nc = m_covered_choices.length;
//		boolean is_subcase=true;
//		for(int i=0;i<nc;++i)
//		{
//			if (!m_covered_choices[i] && other.m_covered_choices[i]) is_subcase = false;
//		}
//		
//		if (is_subcase) return false;
//		
//		return true;
	}
	public boolean isBetterThan(Subtree other)
	{
		int nc = m_covered_choices.length;
		if (m_total_weight <= other.m_total_weight) return false;
		if (m_num_vertexes < other.m_num_vertexes) return false;
		for(int i=0;i<nc;++i)
		{
			if (!m_covered_choices[i] && other.m_covered_choices[i]) return false;
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
		
		//System.out.println("Adding subtree 1");
		//print(g);
		//System.out.println("Adding subtree 2");
		//other.print(g);
		
		other.m_total_weight += de.m_weight;
		
		Vector<Subtree> all_my = getAllSubtreesSorted();
		Vector<Subtree> all_other = other.getAllSubtreesSorted();

		// remove overlapping choices starting from low-weight subtrees
		int my_remove = 0;
		int other_remove = 0;
		
		for(;;)
		{
			Subtree my_s = all_my.get(my_remove);
			Subtree other_s = all_other.get(other_remove);
			if (my_s.m_total_weight <= other_s.m_total_weight)
			{
				if (other.m_covered_choices[my_s.m_root_choice_id])
				{
					//print(g);
					//System.out.println("Remove my subtree");
					if (my_s==this) return false;
					//my_s.print(g);
					removeSubtree(my_s);
					//print(g);
					//System.out.println("---------------");
					
					//all_my = getAllSubtreesSorted();
					//all_other = other.getAllSubtreesSorted();
					//my_remove = 0;
					//other_remove = 0;
				}
				//else
				//{
					if (++my_remove >= all_my.size()) break;
				//}
			}
			else
			{
				if (m_covered_choices[other_s.m_root_choice_id])
				{
					//other.print(g);
					//System.out.println("Remove other subtree");
					//other_s.print(g);
					if (other_s==other) return false;
					other.removeSubtree(other_s);
					//other.print(g);
					//System.out.println("---------------");
					
					//all_my = getAllSubtreesSorted();
					//all_other = other.getAllSubtreesSorted();
					//my_remove = 0;
					//other_remove = 0;
				}
				//else
				//{
					if (++other_remove >= all_other.size()) break;
				//}
			}
		}
		
		//System.out.println("Cutted this");
		//print(g);
		//System.out.println("Cutted other");
		//other.print(g);
		
//		int nc = m_covered_choices.length;
//		for(int i=0;i<nc;++i)
//		{
//			if (m_covered_choices[i] && other.m_covered_choices[i])
//			{
//				Subtree s1 = findSubtreeByChoiceId(i);
//				Subtree s2 = other.findSubtreeByChoiceId(i);
//				// TODO: sort by subtree weight and remove from the biggest difference first???
//				if (s1==null || s2==null)
//				{
//					int k=0;
//					++k;
//				}
//				if (s1.m_total_weight < s2.m_total_weight)
//				{
		// bad!!!
//					if (removeSubtree(s1)) return false;
//					//print(g);
//					checkConsistency();
//				}
//				else
//				{
		// bad!!!
//					if (other.removeSubtree(s2)) return false;
//					//other.print(g);
//					other.checkConsistency();
//				}
//			}
//		}
		m_subtrees.addElement(other);
		m_total_weight += other.m_total_weight;
		m_num_vertexes += other.m_num_vertexes;
		int nc = m_covered_choices.length;
		for(int i=0;i<nc;++i)
		{
			if (other.m_covered_choices[i]) m_covered_choices[i] = true;
		}
		checkConsistency();

		return true;
	}
	
	void print(ChoiceGraph g, int shift, int prev_root)
	{
		for(int i=0;i<shift;++i) System.out.print("  ");
		
		if (prev_root != -1)
		{
			DirectedEdge de = g.getEdge(prev_root, m_root_vectrex_id);
			System.out.print( "(->" + de + "->) ");
		}
		Vertex v = g.vertexById(m_root_vectrex_id);
		System.out.println("" + v + " (" + m_num_vertexes + "," +  m_total_weight + ")");
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
			java.text.DecimalFormat df = new java.text.DecimalFormat("#.###");			
			System.out.print( "[." + de.m_obj + " \\edge node[auto=left]{" + df.format(de.m_weight) + " }; ");
			if (m_subtrees.size()==0)
			{
				System.out.print(v.m_obj);
			}
			else
			{
				System.out.print("[." + v.m_obj + " ");
				close2 = true;
			}
		}
		else
		{
			System.out.print("[." + v.m_obj + " ");
		}
		for(Subtree s : m_subtrees)
		{
			s.print_qtree(g, m_root_vectrex_id);
		}
		System.out.print(close2?" ] ]":" ]");
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
	private int m_max_choices;
	
	private int m_used_choices;
	private int m_used_vertexes;
	
	private Vector<Vector<DirectedEdge>> m_edges;
	private Vector<Vector<Integer>> m_choice_vertices;
	private Vector<Vertex> m_vertexes;
	private Vector<Integer> m_vertex2choice;
	private HashMap<Object, Vertex> m_object2vertex_map = new HashMap<Object, Vertex>();
	
	public ChoiceGraph(int max_choices, int max_vertexes)
	{
		m_max_choices = max_choices;
		m_max_vertexes = max_vertexes;
		m_edges = new Vector<Vector<DirectedEdge>>(max_vertexes);
		m_edges.setSize(max_vertexes);
		
		for( int i=0;i<m_edges.size(); ++i)
		{
			Vector<DirectedEdge> vve = new Vector<DirectedEdge>(max_vertexes);
			vve.setSize(max_vertexes);
			m_edges.set(i,  vve );	
		}
		m_choice_vertices = new Vector<Vector<Integer>>(m_max_choices);
		m_choice_vertices.setSize(m_max_choices);
		for( int i=0;i<m_choice_vertices.size(); ++i)
		{
			m_choice_vertices.set(i,  new Vector<Integer>() );	
		}
		
		m_vertexes = new Vector<Vertex>(m_max_vertexes);
		m_vertex2choice = new Vector<Integer>(m_max_vertexes);
		m_vertex2choice.setSize(m_max_vertexes);
		m_used_choices = 0;
		m_used_vertexes = 0;
	}
	public Vertex vertexById(int vert_id) { return m_vertexes.get(vert_id); }
	public int getNumVertexes() { return m_used_vertexes; }
	public int getNumChoices() { return m_used_choices; }
	public DirectedEdge getEdge(int v1, int v2) { return m_edges.get(v1).get(v2);}
	
	public void addVertex(Object vert_o, double weight, boolean new_choice)
	{
		if (new_choice && m_used_choices >= m_max_choices) throw new java.lang.IndexOutOfBoundsException("No more choices");
		if (m_used_vertexes >= m_max_vertexes) throw new java.lang.IndexOutOfBoundsException("No more vertexes");
		if (!new_choice && m_used_choices==0) throw new java.lang.IndexOutOfBoundsException("No choice is allocated yet. Use new_choice = true!!!");
		
		int choice_id = new_choice? m_used_choices++ : m_used_choices-1;
		int vert_id = m_used_vertexes++;
		
		Vertex v = new Vertex(vert_o, choice_id, vert_id, weight);
		m_vertexes.addElement(v);
		
		m_choice_vertices.get(choice_id).addElement(vert_id);		
		m_vertex2choice.set(vert_id, choice_id );
		m_object2vertex_map.put(vert_o, v);
	}
	
	public void addEdge(Object edge_o, double weight, Object vert_o1, Object vert_o2)
	{
		System.out.println("Add edge (" + (float)weight + ") " + edge_o + "(" + vert_o1 + "->" + vert_o2+")");
		int v_id1 = m_object2vertex_map.get(vert_o1).m_vectex_id;
		int v_id2 = m_object2vertex_map.get(vert_o2).m_vectex_id;
		
		java.text.DecimalFormatSymbols dfs = new java.text.DecimalFormatSymbols(new java.util.Locale("en"));
		java.text.DecimalFormat df = new java.text.DecimalFormat("#.###", dfs);
		//System.out.println("(" + v_id1 + ") edge node [right] {" + df.format(weight) + "} ("+ v_id2 +")");
		
		DirectedEdge edge_old = m_edges.get(v_id1).get(v_id2);
		if (edge_old!=null && edge_old.m_weight >= weight) return;
		
		DirectedEdge e = new DirectedEdge(edge_o, weight);
		m_edges.get(v_id1).set(v_id2, e);
	}
	
	public boolean addToVectorIfGood(Vector<Subtree> v, Subtree s)
	{
		final int hypo_limit = 5;
		
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
			System.out.println("------------------");
		}
	}
	
	public Subtree ExhaustiveEdmondSearch()
	{
		int indexes[] = new int[m_used_choices];
		for(int i=0;i<indexes.length;++i) indexes[i] = 0;
			
		// next choice
		boolean finished = false;
		
		double max_total = 0;
		AdjacencyList maxBranch=null;
		int max_indexes[] = new int[m_used_choices];
		
		for(;;)
		{
			// set up edmond graph
			Node root = new Node(-1);
			Node[] nodes = new Node[m_used_choices];
		
			AdjacencyList myEdges = new AdjacencyList();
			
			for(int i=0;i<indexes.length;++i)
			{
				nodes[i] = new Node(i);
				myEdges.addEdge(root, nodes[i], m_vertexes.get(indexes[i]).m_weight);
			}
			
			for(int i=0;i<m_used_choices;++i) for(int j=0;j<m_used_choices;++j)
			{
				int v_i = m_choice_vertices.get(i).get(indexes[i]).intValue();
				int v_j = m_choice_vertices.get(j).get(indexes[j]).intValue();
				
				DirectedEdge e = m_edges.get(v_i).get(v_j);
				if (e!=null)
				{
					//System.out.println("" + v_i + "->" + v_j + " w=" + e.m_weight);
					myEdges.addEdge(nodes[i], nodes[j], 1000 + e.m_weight + m_vertexes.get(indexes[j]).m_weight);
				}
			}
			
			Edmonds myed = new Edmonds_Andre();	
			AdjacencyList rBranch;
		    rBranch = myed.getMaxBranching(root, myEdges);
		    
		    double total = 0;
		    for( com.altmann.Edge e : rBranch.getAllEdges())
		    {
		    	//System.out.println(e);
		    	total += e.getWeight();
		    }
		    //System.out.println("Total = " + total);
		    
		    if (total > max_total)
		    {
		    	max_total = total;
		    	maxBranch = rBranch;
		    	for(int i=0;i<indexes.length;++i) max_indexes[i] = indexes[i];
		    }

					
			for(int i=0;i<indexes.length;++i)
			{
				if ( indexes[i]+1 == m_choice_vertices.get(i).size() )
				{
					indexes[i] = 0;
					if (i==indexes.length-1)
					{
						finished = true;
					}
				}
				else
				{
					indexes[i]+=1;
					break;
				}
			}
			
			for(int i=0;i<indexes.length;++i) System.out.print(" " + indexes[i]);
			System.out.println();
			
			if (finished) break;
		}
		

	    Subtree tree_root = null;
	    Subtree tree_nodes[] = new Subtree[m_used_choices];
	    
	    for(int i=0;i<m_used_choices;++i)
	    {
	    	tree_nodes[i] = new Subtree(this, m_choice_vertices.get(i).get(indexes[i]).intValue());
	    }
	    
	    for( com.altmann.Edge e : maxBranch.getAllEdges())
	    {
	    	System.out.println(e);
	    	if (e.getSource().name==-1)
	    	{
	    		tree_root = tree_nodes[e.getDest().name];
	    	}
	    	else
	    	{
	    		tree_nodes[e.getSource().name].addSubtree(this, tree_nodes[e.getDest().name]);
	    	}
	    }
	    System.out.println("Total = " + max_total);
	    tree_root.print(this);
	    System.out.println("Total weight = " + tree_root.m_total_weight );
		return tree_root;
	}
	
	public Subtree growingTreesSearch()
	{
		if (m_used_vertexes==0) return null;
		// create trees starting from each vertex
		Vector< Vector<Subtree> > trees = new Vector< Vector<Subtree> >(m_used_vertexes);	
		for(int i=0;i<m_used_vertexes;++i) trees.addElement( new Vector<Subtree>() );

		Subtree max_weighted_tree = new Subtree(this, 0);
		
		// fill single-vertex subtrees
		for(int i=0;i<m_used_vertexes;++i)
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
			for(int c=0;c<m_used_vertexes;++c)
			{
				// process hypotheses. Note, than trees.get(c) can be extended with new hypotheses dinamically
				for(int i = 0; i < trees.get(c).size(); ++i)
				{
					Subtree sc = trees.get(c).get(i);
					for(int j=0;j<m_used_vertexes;++j)
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
			//System.out.println("num_changes = " + num_changes +
					//", total_tests = " + total_tests + ", subtree_aditions = " + subtree_aditions);

		} while (num_changes>0);
		
		
		//dumpBiggest(trees);
		
		//System.out.println("0 to 9 ---------------------");
		
		//Subtree sc_copy1 = new Subtree(trees.get(9).get(0));
		//Subtree sj_copy1 = new Subtree(trees.get(0).get(0));
		//if (sc_copy1.addSubtree(this, sj_copy1))
		//{
		//	sc_copy1.print(this);
		//}

		
		return max_weighted_tree;
	}
	
	public void print()
	{
		System.out.println(m_vertexes);
		System.out.println(m_choice_vertices);
		System.out.println(m_edges);
		System.out.println(m_object2vertex_map);
		System.out.println(m_vertex2choice);
	}
	
	
	static public void test()
	{
		try
		{
			ChoiceGraph cg = new ChoiceGraph(8, 20);
			cg.addVertex("v1_1", 1.0f, true);
			cg.addVertex("v1_2", 0.8f, false);
			cg.addVertex("v2_1", 0.6f, true);
			cg.addVertex("v2_2", 0.9f, false);
			cg.addVertex("v3_1", 1.0f, true);
			cg.addVertex("v3_2", 0.5f, false);
			cg.addVertex("v4_1", 0.2f, true);
			cg.addVertex("v4_2", 0.3f, false);
			cg.addVertex("v5_1", 0.6f, true);
			cg.addVertex("v5_2", 0.9f, false);
			
			cg.addEdge("e_1_2_1", 1.0f, "v1_1", "v2_1");
			cg.addEdge("e_1_3_1", 0.4f, "v1_1", "v3_1");
			cg.addEdge("e_3_4_1", 1.0f, "v3_1", "v4_1");
			cg.addEdge("e_3_5_1", 0.7f, "v3_1", "v5_1");
			cg.addEdge("e_1_2_2", 1.0f, "v1_2", "v2_2");
			cg.addEdge("e_1_3_2", 0.4f, "v1_2", "v3_2");
			cg.addEdge("e_3_4_2", 1.0f, "v3_2", "v4_2");
			cg.addEdge("e_3_5_2", 0.7f, "v3_2", "v5_2");
			
			cg.addEdge("e_1_2_2", 1.0f, "v1_1", "v2_2");
			cg.addEdge("e_1_3_2", 0.4f, "v1_2", "v3_1");
			cg.addEdge("e_3_4_2", 1.0f, "v3_1", "v4_2");
			cg.addEdge("e_3_5_2", 0.7f, "v3_2", "v5_1");
			
			cg.print();
			
			Subtree st = cg.growingTreesSearch();
			st.print(cg);
			Subtree st2 = cg.ExhaustiveEdmondSearch();
			st2.print(cg);
		}
		catch(java.lang.Exception e)
		{
			e.printStackTrace();
		}
	}
}


