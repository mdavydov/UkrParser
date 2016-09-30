/*******************************************************************************
 * UkrParser
 * Copyright (c) 2013-2014 Maksym Davydov
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/

// Affix probabilistic context free grammar parser

package com.langproc;

import sun.reflect.generics.reflectiveObjects.NotImplementedException;

// P -> abc (0,75) | Abc

// (adv_place) дійти до зеленої гори  

// (adv_plaсe) до (кого/чого?)

// G,C = Gender and count that should be the same!!!
// NP[GC] -> NP[GC] ADV[GC]

class AssociatedRule
{
	// token index in the rule
	int m_token_index;
	ProductionRule m_rule;
	RequiredToken m_required_token;
	
	AssociatedRule(	int token_index, ProductionRule rule, RequiredToken required_token )
	{
		m_token_index = token_index;
		m_rule = rule;
		m_required_token = required_token;
	}
}

class AttributeSpecials
{
	float m_probability=1.0f;
	boolean m_generate_all_permutations=false;
};

class TokenSpecials
{
	boolean m_is_required=true;
	boolean m_should_consume=true;  // if true rule should consumes the symbol. If not can be optional
	boolean m_non_consumable=false; // if true means that rule can't consume the symbol. Not compatible with m_should_consume=true
};
// terminal or non-terminal token
class Token
{
	// grammar name
	String m_name;
	// possible grammar attributes
	// for instance, if this entity can have gender but it is not specified
	// it should have attribute WT.GENDER_MASK
	// if the entity is only masculine it should have WT.MALE
	// NOT USED???
	//WordTags m_attributes;

	java.util.List<AssociatedRule> m_associated_multi_term_rules = new java.util.ArrayList<AssociatedRule>();
	java.util.List<AssociatedRule> m_associated_one_term_rules = new java.util.ArrayList<AssociatedRule>();
	
	Token(String name) { m_name = name; }
}

class RequiredToken
{
	// token that should be present
	Token m_token;
	// semantic token that can be used in semantic parsing.
	Token m_semantic_token;
	// m_is_head = true if this token denoted semantics of the production
	boolean m_is_head;
	// attributes that are required to satisfy the rule
	WordTags m_required_attributes;
	// attributes that are inherited by rule production
	WordTags m_inherited_attributes;
	// attributes that should be uniform across the rule
	WordTags m_uniform_attributes;
	// the assumption level about presence of this token. 1.0 means should always be present 
	TokenSpecials m_token_specials;
	
	RequiredToken(Token token, Token semantic_token, WordTags required_attributes,
			WordTags inherited_attributes, WordTags uniform_attributes, TokenSpecials ta)
	{
		m_token = token;
		m_is_head = semantic_token!=null && semantic_token.m_name.equals("*");
		m_semantic_token = m_is_head?null : semantic_token;
		m_required_attributes = required_attributes;
		m_inherited_attributes = inherited_attributes;
		m_uniform_attributes = uniform_attributes;
		m_token_specials = ta;
	}
	boolean isRequired() { return m_token_specials.m_is_required; }
	boolean shouldConsume() { return m_token_specials.m_should_consume; }
	boolean canConsume() { return !m_token_specials.m_non_consumable; }
}

class ProductionRule
{
	Token m_result;			// result token like "Participle phrase"
	Token m_semantic_result;// semantic result (null means that it is taken from the "head" marked as "*")
	WordTags m_defined_attributes;	 // attributes that are defined by the rule
	WordTags m_inherited_unified_attributes;
	float m_probability;
	// required sub-tokens
	java.util.Vector<RequiredToken> m_subtokens;
	
	ProductionRule(Token result, Token semantic_result,  WordTags def_attr, WordTags inherited_unified_attributes, float probability,
			java.util.Vector<RequiredToken> subtokens, boolean gen_all_permutations)
	{
		m_result = result;
		m_semantic_result = semantic_result;
		m_defined_attributes = def_attr;
		m_inherited_unified_attributes = inherited_unified_attributes;
		m_probability = probability;
//		if (m_probability>1)
//		{
//			System.out.println("Pr = " + m_probability);
//		}
		m_subtokens = subtokens;
		
		addToIndex();
		if (gen_all_permutations)
		{
			createAllPermutations(0);
		}
	}
	
	void createAllPermutations(int index_to_start)
	{
		int s = m_subtokens.size();
		if (index_to_start<s-1) createAllPermutations(index_to_start+1);
		for(int i=index_to_start+1;i<s;++i)
		{
			java.util.Vector<RequiredToken> new_subtokens = new java.util.Vector<RequiredToken>(m_subtokens);
			RequiredToken t1 = new_subtokens.get(i);
			new_subtokens.set(i, new_subtokens.get(index_to_start) );
			new_subtokens.set(index_to_start, t1);
			ProductionRule new_pr_rule = new ProductionRule(m_result, m_semantic_result, m_defined_attributes,
					m_inherited_unified_attributes, m_probability, new_subtokens, false);
			new_pr_rule.createAllPermutations(index_to_start+1); 
		}
	}
	
	public String toString()
	{
		StringBuffer sb = new StringBuffer();
		sb.append(m_result.m_name);
		sb.append(m_semantic_result==null?"":"("+m_semantic_result.m_name+")");
		sb.append(" " + m_probability + " -> ");
		for(RequiredToken rt : m_subtokens)
		{
			sb.append(rt.m_token.m_name);
			sb.append(rt.m_semantic_token==null?"":"("+rt.m_semantic_token.m_name+")");
			sb.append(" ");
		}
		return sb.toString();
	}
	
	boolean canBeOneTermRuleWithTokenAt(int pos)
	{
		for(int i = 0; i < m_subtokens.size();++i)
		{
			RequiredToken rt = m_subtokens.get(i);
			if (i!=pos && rt.isRequired() && rt.shouldConsume()) return false;
		}
		return true;
	}
	
	public void addToIndex()
	{
		//System.out.println("Add to index " + this);
		

		if (m_subtokens.size()>1)
		{
			for(int pos = 0; pos < m_subtokens.size();++pos)
			{
				RequiredToken rt = m_subtokens.get(pos);
				rt.m_token.m_associated_multi_term_rules.add(new AssociatedRule(pos, this, rt) );
			}
		}
		
		for(int pos = 0; pos < m_subtokens.size();++pos)
		{
			RequiredToken rt = m_subtokens.get(pos);
			if (canBeOneTermRuleWithTokenAt(pos))
			{
				rt.m_token.m_associated_one_term_rules.add(new AssociatedRule(pos, this, rt) );
			}
		}		
	}

	public boolean hasOptionalConsumables()
	{
		for(int i = 0; i < m_subtokens.size();++i)
		{
			RequiredToken rt = m_subtokens.get(i);
			if (!rt.shouldConsume()) return true;
		}
		return false;
	}
}

class ParsedToken
{
	// linked language Token
	Token m_token;
	// linked semantic token
	Token m_semantic_token;
	// attributes obtained from the rule and extracted from sub-tokens
	WordTags m_attributes = new WordTags();
	// (attributes that should be uniform among specific children
	WordTags m_uniform_attributes = new WordTags();
	// subjective probability of presence
	float m_probabilty=1.0f;
	
	ProductionRule m_production_rule;
	
	String m_token_text;
	
	int m_num_subtokens=0;
	
	// the set of sub-tokens that constitute the most probable ParsedToken
	private java.util.Vector<ParsedToken> m_subtokens = new java.util.Vector<ParsedToken>();
	
	ParsedToken() {}
	ParsedToken(Token token, Token semantic_token, WordTags attributes, float probabilty, String token_text)
	{
		if (token==null) throw new java.lang.NullPointerException();
		m_token = token;
		m_semantic_token = semantic_token;
		m_attributes.m_tags = attributes.m_tags;
		m_probabilty=probabilty;
		m_token_text = token_text;
	}
	@SuppressWarnings("unchecked")
	ParsedToken(ParsedToken pc)
	{
		m_token = pc.m_token;
		m_semantic_token = pc.m_semantic_token;
		m_attributes.m_tags = pc.m_attributes.m_tags;
		m_uniform_attributes.m_tags = pc.m_uniform_attributes.m_tags;
		m_probabilty=pc.m_probabilty;
		m_subtokens=(java.util.Vector<ParsedToken>)pc.m_subtokens.clone();
		m_production_rule = pc.m_production_rule;
	}
	
	@SuppressWarnings("unchecked")
	ParsedToken copyFrom(ParsedToken pc)
	{
		m_token = pc.m_token;
		m_semantic_token = pc.m_semantic_token;
		m_attributes.m_tags = pc.m_attributes.m_tags;
		m_uniform_attributes.m_tags = pc.m_uniform_attributes.m_tags;
		m_probabilty=pc.m_probabilty;
		m_subtokens=(java.util.Vector<ParsedToken>)pc.m_subtokens.clone();
		m_production_rule = pc.m_production_rule;
		return this;
	}
	
	boolean isMultiTerm() {return m_num_subtokens>1;}
	
	void resetWithRule(ProductionRule production_rule)
	{
		m_production_rule = production_rule;
		m_num_subtokens = 0;
		int size = production_rule.m_subtokens.size();
		m_subtokens.setSize(size);
		for(int i=0;i<size;++i) m_subtokens.set(i, null);
		
		m_probabilty = production_rule.m_probability;
		m_token = production_rule.m_result;
		m_semantic_token = production_rule.m_semantic_result;
		m_attributes.copy(production_rule.m_defined_attributes);
		m_uniform_attributes.m_tags = ~(long)0;
	}
	
	void setSubtoken(int index, ParsedToken subtoken)
	{
		++m_num_subtokens;
		
		boolean is_single=m_production_rule.m_subtokens.size()==1;
		RequiredToken rt = m_production_rule.m_subtokens.get(index);
		
		if (rt.m_semantic_token!=null && rt.m_semantic_token!=subtoken.m_semantic_token)
		{
			assert(false);
		}
		
		if ( (rt.m_is_head || is_single) && m_production_rule.m_semantic_result==null)
		{
			// NO DOUBLE TOKEN SET_UP YET!!!
			assert(m_semantic_token==null);
			// use this token semantic attribute in the parser
			m_semantic_token = subtoken.m_semantic_token;
		}
		
		WordTags subtoken_tags = new WordTags( subtoken.m_attributes );
		subtoken_tags.limitInCategories(rt.m_required_attributes);
		subtoken_tags.limitInCategories(rt.m_uniform_attributes, m_uniform_attributes);
		
		m_uniform_attributes.limitInCategories(rt.m_uniform_attributes, subtoken_tags);

		m_subtokens.set(index, subtoken);
		m_probabilty *= subtoken.m_probabilty;
		
		m_attributes.m_tags |= (subtoken_tags.m_tags & rt.m_inherited_attributes.m_tags);
		m_attributes.limitInCategories(m_production_rule.m_inherited_unified_attributes, m_uniform_attributes );
	}
	void removeSubtoken(int index, long restore_tags,
			long restore_uniform_tags,
			float restore_probability )
	{
		--m_num_subtokens;
		
		boolean is_single=m_production_rule.m_subtokens.size()==1;
		RequiredToken rt = m_production_rule.m_subtokens.get(index);
		
		if ( (rt.m_is_head || is_single) && m_production_rule.m_semantic_result==null)
		{
			m_semantic_token = null;
		}
		
		m_subtokens.set( index, null );
		m_attributes.m_tags = restore_tags;
		m_uniform_attributes.m_tags = restore_uniform_tags;
		m_probabilty = restore_probability;
	}
	
	public String toString()
	{
		return toString(null, null, null);
	}
	
	public String toString(WordTags req_tokens, WordTags to_unify, WordTags unif_res)
	{
		WordTags wt;
		
		if (req_tokens!=null)
		{
			wt = new WordTags(m_attributes);
			wt.limitInCategories(req_tokens);
			wt.limitInCategories(to_unify, unif_res);
		}
		else
		{
			wt = m_attributes;
		}
		
		StringBuffer res = new StringBuffer();

		if (m_token_text!=null)
		{
			res.append("<" + m_token_text + ">");
		}
		if (m_token.m_name!=null)
		{
			res.append(m_token.m_name);
		}
		
		if (m_semantic_token!=null && m_semantic_token!=m_token)
		{
			res.append("(" + m_semantic_token.m_name + ")");
		}
		
		res.append(" (");
		//res.append(m_attributes.toString());
		res.append(wt.toString());
		res.append(") [");
		res.append(m_probabilty);
		res.append("] ");
		if (m_subtokens.size()>0)
		{
			res.append("-> { ");
			
			int rule_ind = 0;
			for( ParsedToken pt : m_subtokens )
			{
				if (pt==null)
				{
					res.append("<> ");
				}
				else if (m_production_rule==null)
				{
					res.append(pt.toString());
				}
				else
				{
					RequiredToken rt = m_production_rule.m_subtokens.get(rule_ind);
					res.append(pt.toString(rt.m_required_attributes, rt.m_uniform_attributes, m_uniform_attributes));
				}
				++rule_ind;
			}
			res.append(" } ");
		}

		return res.toString();
	}
	
	String toTikzTree(boolean show_attr)
	{
		int level_d = show_attr ? 100 : 50;
		int depth = getDepth();
		return "\\hspace{1em}\n\\resizebox{\\columnwidth}{!}{\n\\begin{tikzpicture}[sibling distance=15pt,level distance="+level_d+"pt]\n" +
		"\\tikzset{frontier/.style={distance from root="+(depth*level_d-level_d/2)+"pt}}\n" +
		"\\Tree\n" + 
		toTikzTree(null,null,null, show_attr, 1) + "\n" + 
		"\\end{tikzpicture}\n}\n";
	}
	
	String toTeXString(String s)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<s.length();++i)
		{
			char c = s.charAt(i);
			if (c=='_') sb.append("\\_");
			else sb.append(c);
		}
		return sb.toString();
	}
	
	String toTikzTree(WordTags req_tokens, WordTags to_unify, WordTags unif_res, boolean show_attr, int num_parent_children)
	{
		StringBuffer res = new StringBuffer();

		int child_num=0;
		for( ParsedToken pt : m_subtokens ) if (pt!=null) ++child_num;
		
/*		if (child_num==1 && num_parent_children==1)
		{
			int rule_ind1 = 0;
			for( ParsedToken pt : m_subtokens )
			{
				if (pt!=null)
				{
					RequiredToken rt = m_production_rule.m_subtokens.get(rule_ind1);
					return pt.toTikzTree(rt.m_required_attributes, rt.m_uniform_attributes, m_uniform_attributes, show_attr, 1);
				}
				++rule_ind1;
			}
		}*/
		
		WordTags wt;
		if (req_tokens!=null)
		{
			wt = new WordTags(m_attributes);
			wt.limitInCategories(req_tokens);
			wt.limitInCategories(to_unify, unif_res);
		}
		else
		{
			wt = m_attributes;
		}

		boolean add_attributes = show_attr || m_token_text==null || !m_token.m_name.equals(m_token_text);
		if (add_attributes)
		{
			res.append("[.{\\pbox[b]{1cm}{ "); // \\centering
			if (m_token_text==null || !m_token.m_name.equals(m_token_text))
			{
				res.append(toTeXString(m_token.m_name));
			}
			if (m_semantic_token!=null && m_token!=m_semantic_token)
			{
				res.append(" \\\\ (" + toTeXString(m_semantic_token.m_name) + ")");
			}
			res.append("}");
			if (show_attr)
			{
				res.append(" (");
				res.append(wt.toString());
				res.append(") ");
				res.append(m_probabilty);
			}
			res.append("} ");
		}
		if (m_subtokens.size()>=1)
		{
			int rule_ind = 0;
			for( ParsedToken pt : m_subtokens )
			{
				if (pt!=null && !pt.m_token.m_name.equals("START") && !pt.m_token.m_name.equals("END"))
				{
					res.append(" \\edge node[auto=left]{");
					res.append(pt.m_probabilty);
					res.append("}; ");
					RequiredToken rt = m_production_rule.m_subtokens.get(rule_ind);
					res.append(pt==null?"<>":pt.toTikzTree(rt.m_required_attributes, rt.m_uniform_attributes, m_uniform_attributes, show_attr, child_num));
				}
				++rule_ind;
			}
		}
		else
		{
			res.append(" {");
//			if (m_token.m_name!=null)
//			{
//				res.append(m_token.m_name);
//			}
			if (m_token_text!=null)
			{
				//res.append("<" + m_token_text + ">");
				res.append(toTeXString(m_token_text));
			}
			res.append("} ");
		}
		if (add_attributes) res.append(" ]");
		return res.toString();
	}
	
	int getDepth()
	{
		int max = 0;
		for( ParsedToken pt : m_subtokens )
		{
			int d = pt==null? 0 : pt.getDepth();
			if (d>max) max=d;
		}
		return max+1;
	}
}

// Probabilistic context free grammar (PCFG) parser
public class APCFGParser
{
	int m_total_height;
	java.util.List<ParsedToken> m_parse_piramide[][];
	java.util.HashMap<String, Token> m_tokens = new java.util.HashMap<String, Token>();
	
	Token getTokenByName(String s)
	{
		if (s==null) return null;
		Token t = m_tokens.get(s);
		if (t==null)
		{
			t = new Token(s);
			m_tokens.put(s,t);
		}
		return t;
	}
	
	boolean readORSign(StringBuffer buf)
	{
		while(buf.length()>0 && Character.isWhitespace(buf.charAt(0)))
		{
			buf.deleteCharAt(0);
		}
		
		if (buf.length()==0) return false;
		if (buf.charAt(0)=='|')
		{
			buf.deleteCharAt(0);
			return true;
		}
		return false;
	}
	
	String readTokenString(StringBuffer buf)
	{
		// read one token from the string and advance string pointer p
		while(buf.length()>0 && Character.isWhitespace(buf.charAt(0)))
		{
			buf.deleteCharAt(0);
		}
		
		if (buf.length()==0) return null;
		
		if (buf.charAt(0)=='<')
		{
			buf.deleteCharAt(0);
			// read until '>'
			StringBuffer result = new StringBuffer(16);			
			while(buf.length()>0 && buf.charAt(0)!='>')
			{
				result.append(buf.charAt(0));
				buf.deleteCharAt(0);
			}
			if (buf.length()>0) buf.deleteCharAt(0);
			return result.toString();
		}
				
		if (!Character.isLetter(buf.charAt(0))) return null;
			
		StringBuffer result = new StringBuffer(16);
		
		while(buf.length()>0 && ( Character.isLetter(buf.charAt(0)) || buf.charAt(0)=='_') )
		{
			result.append(buf.charAt(0));
			buf.deleteCharAt(0);
		}
		return result.toString(); 
	}
	
	String readTokenSemanticAttribute(StringBuffer buf)
	{
		while(buf.length()>0 && Character.isWhitespace(buf.charAt(0)))
		{
			buf.deleteCharAt(0);
		}
		if (buf.length()==0) return null;
		
		if (buf.charAt(0)=='(')
		{
			buf.deleteCharAt(0);
			// read until '>'
			StringBuffer result = new StringBuffer(16);			
			while(buf.length()>0 && buf.charAt(0)!=')')
			{
				result.append(buf.charAt(0));
				buf.deleteCharAt(0);
			}
			if (buf.length()>0) buf.deleteCharAt(0);
			return result.toString();
		}
		return null; // no semantic attribute was found
	}

	TokenSpecials readTokenSpecials(StringBuffer buf)
	{
		TokenSpecials ts = new TokenSpecials();
		// read one token from the string and advance string pointer p
		while(buf.length()>0 && Character.isWhitespace(buf.charAt(0)))
		{
			buf.deleteCharAt(0);
		}
		
		for(;;)
		{
			if (buf.length()==0) return ts;
			if (buf.charAt(0)=='?') { ts.m_is_required = false; buf.deleteCharAt(0); continue; }
			if (buf.charAt(0)=='!')
			{
				if (ts.m_should_consume==true) // single ! is for optional consume
				{
					ts.m_should_consume = false;
				}
				else
				{
					ts.m_non_consumable = true; // !! is for non consumable
				}
				buf.deleteCharAt(0);
				continue;
			}
			return ts;
		}
	}

	
	

	
	void readAttributeSpecials(StringBuffer inbuf, AttributeSpecials attr_sp)
	{
		while(inbuf.length()>0 && Character.isWhitespace(inbuf.charAt(0)))
		{
			inbuf.deleteCharAt(0);
		}
		if (inbuf.length()==0) return;
		if (inbuf.charAt(0)=='*')
		{
			attr_sp.m_generate_all_permutations = true;
			inbuf.deleteCharAt(0);
		}
		
		try
		{
			attr_sp.m_probability = Float.valueOf(inbuf.toString());
//			if (attr_sp.m_probability > 1)
//			{
//				System.out.println("Pr = " + attr_sp.m_probability);
//			}
		}
		catch(NumberFormatException e) {}
	}
	


	void addRule(String rule)
	{
		int rule_symbol = rule.indexOf("->");
		if (rule_symbol==-1)
		{
			System.out.println("Bad rule:" + rule);
			System.out.println("Should be: LeftPart -> Right part");
		}
		
		StringBuffer leftPart = new StringBuffer( rule.substring(0, rule_symbol) );
		StringBuffer rightPart = new StringBuffer( rule.substring(rule_symbol+2) );
		
		String leftTokenS = readTokenString(leftPart);
		String leftTokenSemAttr = readTokenSemanticAttribute(leftPart);
		//System.out.println("Left part: " + leftTokenS);
		WordTags left_sp = new WordTags();
		WordTags left_un = new WordTags();
		WordTags.readAttributeString(leftPart, left_sp, left_un);
		
		AttributeSpecials sp_attr = new AttributeSpecials();
		readAttributeSpecials(leftPart, sp_attr);
		
		
		Token result = getTokenByName(leftTokenS);
		Token semantic_result = getTokenByName(leftTokenSemAttr);
		WordTags defined_attributes = left_sp; // no tags yet
		
		for(;;)
		{
			java.util.Vector<RequiredToken> subtokens = new java.util.Vector<RequiredToken>();
			
			for(;;)
			{
				String rightTokenS = readTokenString(rightPart);
				String rightTokenSemAttr = readTokenSemanticAttribute(rightPart);
				if (rightTokenS==null) break;
				
				WordTags token_sp = new WordTags();
				WordTags token_un = new WordTags();
				WordTags.readAttributeString(rightPart, token_sp, token_un);

				TokenSpecials ts = readTokenSpecials(rightPart);
				
				//System.out.println("Right part: " + rightTokenS);
				Token req_token =  getTokenByName(rightTokenS);
				
				subtokens.add( new RequiredToken(req_token, getTokenByName(rightTokenSemAttr), token_sp,
						new WordTags(token_un.m_tags), token_un, ts) );
			}
			
			if (subtokens.size()>0)
			{
//				if (sp_attr.m_probability>1)
//				{
//					System.out.println("Pr2 = " + sp_attr.m_probability);
//				}
				// the rule is added to index in each token that can be produced by this rule
				new ProductionRule(result, semantic_result, defined_attributes, left_un, sp_attr.m_probability, subtokens, sp_attr.m_generate_all_permutations);
				
			}
				
			
			if (!readORSign(rightPart)) break;
		}
	}

	boolean addPossibleTokenCopy(int pos_left, int pos_after_right, ParsedToken partially_parsed_token, boolean only_multiterm)
	{
		if (only_multiterm && !partially_parsed_token.isMultiTerm()) return false;
		int height = pos_after_right - pos_left - 1;
		java.util.List<ParsedToken> tokens = m_parse_piramide[height][pos_left];
		
		for(ParsedToken p : tokens)
		{
			if (p.m_token == partially_parsed_token.m_token &&
				p.m_semantic_token == partially_parsed_token.m_semantic_token &&
				p.m_attributes.equals(partially_parsed_token.m_attributes))
			{
				if (p.m_probabilty < partially_parsed_token.m_probabilty)
				{
					p.copyFrom(partially_parsed_token);
					if (LangProcSettings.DEBUG_OUTPUT)
					{
						System.out.println("Rep (h=" + height + ", p=" + pos_left + ") " + p.toString());
					}
					return true;
				}
				else
				{
					//System.out.println("Fnd (h=" + height + ", p=" + pos_left + ") " + partially_parsed_token.toString());
					// this one is worser that one in the tree
					return false;
				}
			}
		}
		
		// not found
		ParsedToken pt = new ParsedToken(partially_parsed_token);
		tokens.add( pt );
		if (LangProcSettings.DEBUG_OUTPUT)
		{
			System.out.println("Add (h=" + height + ", p=" + pos_left + ") " + pt.toString());
		}
		return true;
	}
	
	// match all right parts that can be produced by the rule of height <= apply_height
	// assume that left part is fully parsed
	void tryMatchRight(ProductionRule rule, ParsedToken partially_parsed_token,
			int apply_height,
			int pos_left,
			int rule_token_after_right, int pos_after_right)
	{
		if (rule_token_after_right==rule.m_subtokens.size())
		{
			// finished parsing the rule. Write only if goes to upper level of pyramid
			if (pos_after_right - pos_left - 1 > apply_height)
			{
				addPossibleTokenCopy(pos_left, pos_after_right, partially_parsed_token, true);
			}
			return;
		}

		int required_tokens_on_the_right = 0;
		for(int i=rule_token_after_right;i<rule.m_subtokens.size();++i)
		{
			if (rule.m_subtokens.get(i).isRequired()) ++required_tokens_on_the_right;
		}
		
		// can't satisfy if too little tokens left
		if (m_total_height - pos_after_right < required_tokens_on_the_right) return;
		
		if (pos_after_right==m_total_height)
		{
			// all parts that are left where optional. Add current token
			// TODO: adjust the probability of partial token
			if (pos_after_right - pos_left - 1 > apply_height)
			{
				addPossibleTokenCopy(pos_left, pos_after_right, partially_parsed_token, true);
			}
			return;
		}

		// go through all possible starts
		RequiredToken req_token = rule.m_subtokens.get(rule_token_after_right);
		Token search_token = req_token.m_token;
		Token search_semantic_token = req_token.m_semantic_token;
		// save the parsing state
		long saved_tags = partially_parsed_token.m_attributes.m_tags;
		long saved_uniform_tags = partially_parsed_token.m_uniform_attributes.m_tags;
		float saved_prob = partially_parsed_token.m_probabilty;
		
		
		long required_tags = req_token.m_required_attributes.m_tags;
		long required_categories = req_token.m_required_attributes.getCategories();
		
		long required_to_be_uniform =  req_token.m_uniform_attributes.m_tags;

		if (!req_token.isRequired())
		{
			// add parse option where next token is omitted
			tryMatchRight(rule, partially_parsed_token, apply_height, pos_left, rule_token_after_right+1, pos_after_right);
		}
		// now next token should be present
		if (required_tokens_on_the_right==0) required_tokens_on_the_right=1;

		int max_height = m_total_height - pos_after_right - required_tokens_on_the_right;
		if (max_height>apply_height) max_height = apply_height;
		// try match the token on the right
		for(int i=0;i<=max_height;++i)
		{
			
			java.util.List<ParsedToken> next_token_list=null;
			try
			{
				next_token_list = m_parse_piramide[i][pos_after_right];
			}
			catch(java.lang.ArrayIndexOutOfBoundsException e)
			{
				System.err.println("ArrayIndexOutOfBoundsException");
			}
			if (next_token_list==null) break;
			for(ParsedToken t: next_token_list)
			{
				// reference should be unique!!!
				if (t.m_token==search_token &&
					(search_semantic_token==null || t.m_semantic_token==search_semantic_token) &&
					t.m_attributes.hasCommonTagsInAllCategories(required_tags, required_categories) &&
					t.m_attributes.hasCommonTagsInAllCategories(
							saved_uniform_tags&(~required_categories | required_tags), required_to_be_uniform) )
				{
					partially_parsed_token.setSubtoken(rule_token_after_right, t);
					
					if (!req_token.shouldConsume())
					{
						tryMatchRight(rule, partially_parsed_token,
								apply_height, pos_left, rule_token_after_right+1, pos_after_right );
					}
					if (req_token.canConsume())
					{
						tryMatchRight(rule, partially_parsed_token,
							apply_height, pos_left, rule_token_after_right+1, pos_after_right+(i+1) );
					}
					
					partially_parsed_token.removeSubtoken(rule_token_after_right, saved_tags, saved_uniform_tags, saved_prob);
				}
			}
		}	
	}
	
	// match all left parts that can be produced by the rule of height < apply_height (i.e. left part should be smaller!!!)
	// assume that left part is fully parsed
	void tryExpandLeft(ProductionRule rule, ParsedToken partially_parsed_token,
			int apply_height,
			int rule_token_left, int pos_left,
			int rule_token_after_right, int pos_after_right)
	{
		if (rule_token_left==0)
		{
			// go to right-side
			tryMatchRight(rule, partially_parsed_token, apply_height, pos_left, rule_token_after_right, pos_after_right);
			return;
		}

		int required_tokens_on_the_left = 0;
		for(int i=0; i<rule_token_left;++i)
		{
			if (rule.m_subtokens.get(i).isRequired()) ++required_tokens_on_the_left;
		}
		
		// can't satisfy if too little tokens left
		if (pos_left < required_tokens_on_the_left) return;
		
		if (pos_left==0)
		{
			// all parts that are left are optional. Continue to the right
			tryMatchRight(rule, partially_parsed_token, apply_height, pos_left, rule_token_after_right, pos_after_right);
			return;
		}

		// go through all possible matches
		RequiredToken req_token = rule.m_subtokens.get(rule_token_left-1);
		Token search_token = req_token.m_token;
		Token search_semantic_token=req_token.m_semantic_token;
		// save the parsing state
		long saved_tags = partially_parsed_token.m_attributes.m_tags;
		long saved_uniform_tags = partially_parsed_token.m_uniform_attributes.m_tags;
		float saved_prob = partially_parsed_token.m_probabilty;
		
		
		long required_tags = req_token.m_required_attributes.m_tags;
		long required_categories = req_token.m_required_attributes.getCategories();
		
		long required_to_be_uniform =  req_token.m_uniform_attributes.m_tags;
		
		if (!req_token.isRequired())
		{
			// add parse option where next token is omitted
			// TODO: add skip penalty
			tryExpandLeft(rule, partially_parsed_token, apply_height, rule_token_left-1, pos_left, rule_token_after_right, pos_after_right);
		}
		
		// here we check the case when token on the left is not ommitted. So we can set required tokens to at least 1
		if (required_tokens_on_the_left==0) required_tokens_on_the_left=1;

		int max_height = pos_left - required_tokens_on_the_left;
		if (max_height>=apply_height) max_height = apply_height-1;
		// try match the token on the right
		for(int i=0;i<=max_height;++i)
		{
			java.util.List<ParsedToken> next_token_list = null;
			
			try
			{
				next_token_list = m_parse_piramide[i][pos_left-i-1];
			}
			catch(java.lang.ArrayIndexOutOfBoundsException e)
			{
				System.err.println("ArrayIndexOutOfBoundsException");
			}
			if (next_token_list==null) break;
			for(ParsedToken t: next_token_list)
			{
				// reference should be unique!!!
				if (t.m_token==search_token &&
					(search_semantic_token==null || t.m_semantic_token==search_semantic_token) &&
					t.m_attributes.hasCommonTagsInAllCategories(required_tags, required_categories) &&
					t.m_attributes.hasCommonTagsInAllCategories(
							saved_uniform_tags&(~required_categories | required_tags), required_to_be_uniform) )
				{
					partially_parsed_token.setSubtoken(rule_token_left-1, t);
					
					if (!req_token.shouldConsume())
					{
						tryExpandLeft(rule, partially_parsed_token,
								apply_height, rule_token_left-1, pos_left, rule_token_after_right, pos_after_right );
					}
					if (req_token.canConsume())
					{
						tryExpandLeft(rule, partially_parsed_token,
							apply_height, rule_token_left-1, pos_left-i-1, rule_token_after_right, pos_after_right );
					}
					
					partially_parsed_token.removeSubtoken(rule_token_left-1, saved_tags, saved_uniform_tags, saved_prob);
				}
			}
		}	
	}
	
	void fireAllOneTermRules(int apply_height, int start_pos)
	{
		java.util.List<ParsedToken> start_list = m_parse_piramide[apply_height][start_pos];
		ParsedToken reusable_token = new ParsedToken();
		// run all 1-term rules first!!!
		
		boolean changed = false;
		do
		{
			changed = false; 
			for(ParsedToken token: start_list)
			{
				// for all possible rules
				for( AssociatedRule ar : token.m_token.m_associated_one_term_rules )
				{
					Token req_sem_token = ar.m_required_token.m_semantic_token;
					if (token.m_attributes.hasRequiredTags(ar.m_required_token.m_required_attributes) &&
						(req_sem_token==null || req_sem_token==token.m_semantic_token))
					{
						 reusable_token.resetWithRule(ar.m_rule);
						 reusable_token.m_probabilty = ar.m_rule.m_probability;
						 reusable_token.m_token = ar.m_rule.m_result;
						 reusable_token.m_attributes.copy( ar.m_rule.m_defined_attributes );
						 reusable_token.m_attributes.limitInCategories(ar.m_required_token.m_required_attributes);
						 
						 // by default set that all attributes are possible
						 reusable_token.m_uniform_attributes.m_tags = ~(long)0;
						 
						 //System.out.println("One-term rule (" + ar.m_rule + ") at h=" + apply_height + " p=" + start_pos);
						 reusable_token.setSubtoken(ar.m_token_index, token);
						 
						 if (ar.m_rule.hasOptionalConsumables())
						 {
							 throw new NotImplementedException();
						 }
						 
						 if (addPossibleTokenCopy(start_pos, start_pos+apply_height+1, reusable_token, false))
						 {
							 changed = true;
						 }
					}
				}
				if (changed) break;
			}
		} while (changed);
	}
	
	void fireAllMultiTermRules(int apply_height, int start_pos)
	{
		java.util.List<ParsedToken> start_list = m_parse_piramide[apply_height][start_pos];
		ParsedToken reusable_token = new ParsedToken();
		// run all 1-term rules first!!!
		for(ParsedToken token: start_list)
		{
			// for all possible rules
			for( AssociatedRule ar : token.m_token.m_associated_multi_term_rules )
			{
				Token req_sem_token = ar.m_required_token.m_semantic_token;
				if (  (req_sem_token==null || req_sem_token==token.m_semantic_token) &&
						token.m_attributes.hasRequiredTags(ar.m_required_token.m_required_attributes))
				{
					 reusable_token.resetWithRule(ar.m_rule);
					 reusable_token.setSubtoken(ar.m_token_index, token);
					 
					 //System.out.println("Try rule (" + ar.m_rule + ") at h=" + apply_height + " p=" + start_pos);
					 
					 if (!ar.m_required_token.shouldConsume())
					 {
						 tryExpandLeft(ar.m_rule, reusable_token,
									apply_height, ar.m_token_index, start_pos,
									ar.m_token_index+1, start_pos+1);
					 }
					 if (ar.m_required_token.canConsume())
					 {
						 tryExpandLeft(ar.m_rule, reusable_token,
							apply_height, ar.m_token_index, start_pos,
							ar.m_token_index+1, start_pos+apply_height+1);
					 }
				}
			}
		}
	
	}
	
	@SuppressWarnings("unchecked")
	public java.util.List<ParsedToken> parse(java.util.Vector< java.util.List<ParsedToken> > tokens)
	{
		m_total_height = tokens.size();
		m_parse_piramide = new java.util.List[m_total_height][];
		for(int i=0;i<m_total_height;++i)
		{
			m_parse_piramide[i] = new java.util.List[m_total_height-i];
			for(int j=0;j<m_total_height-i;++j)
			{
				m_parse_piramide[i][j] = new java.util.ArrayList<ParsedToken>();
			}
		}
		for(int i=0;i<m_total_height;++i) m_parse_piramide[0][i] = tokens.get(i);
		
		
		for(int apply_height=0;apply_height<m_total_height;++apply_height)
		{
			for(int start_pos=0; start_pos < m_total_height-apply_height;++start_pos)
			{
				fireAllOneTermRules(apply_height, start_pos);
			}
			for(int start_pos=0; start_pos < m_total_height-apply_height;++start_pos)
			{
				fireAllMultiTermRules(apply_height, start_pos);
			}
		}
		return m_parse_piramide[m_total_height-1][0];
	}
		
	public java.util.List<ParsedToken> parse(String s)
	{
		StringBuffer readS = new StringBuffer(s);
		java.util.Vector< java.util.List<ParsedToken> > tokens = new java.util.Vector< java.util.List<ParsedToken> > ();
		
		for(;;)
		{
			String tokenS = readTokenString(readS);
			if (tokenS==null) break;
			
			WordTags token_sp = new WordTags();
			WordTags token_un = new WordTags();
			WordTags.readAttributeString(readS, token_sp, token_un);
			
			//System.out.println("Parse string part: " + tokenS);
			Token req_token = getTokenByName(tokenS);
			java.util.List<ParsedToken> ptl = new java.util.ArrayList<ParsedToken>();
			ParsedToken pt = new ParsedToken(req_token, null, token_sp, 1.0f, "<" + req_token.m_name + ">");
			assert(false); //DON'T KNOW WHAT semantic attribute to add there
			ptl.add(pt);
			tokens.add( ptl );
			System.out.println(pt.toString());
		}
		
		return parse(tokens);
	}
	
//	public static void main_(String[] args)
//	{
//		System.out.println("PCFG Parser test!!!");
//		PCFGParser parser = new PCFGParser();
//		//parser.addRule("DNP[NCG] 0.8 -> noun[NCG c2c3c4c5c6c7] adj[NCG]");
//		parser.addRule("DNP[NCG] -> adj[NCG c2c3c4c5c6c7] noun[NCG]");
//		
//		parser.addRule("NP[NCG p3] 0.8 -> noun[NCG c1] adj[NCG]");
//		parser.addRule("NP[NCG p3] -> adj[NCG c1] noun[NCG]");
//		parser.addRule("NP[NCGP] -> pronoun[NCGP c1]");
//		
//		parser.addRule("TARGET -> u DNP[c4]");
//		parser.addRule("S -> NP[PN] VP[PN]");
//		
//		parser.addRule("VP[PN] -> verb[PN] TARGET?");
//		
//		parser.addRule("A 0.6 -> B adj");
//		parser.addRule("A 0.7 -> noun D");
//		parser.addRule("D -> C");
//		
//		
//		java.util.List<ParsedToken> res = parser.parse("pronoun[p1 s c1] verb[p1 s] u adj[m s c4] noun[m s c4]");
//		
//		if (res==null)
//		{
//			System.out.println("No results");
//			//throw new java.lang.IndexOutOfBoundsException();
//		}
//		else
//		{
//			for( ParsedToken root : res)
//			{
//				System.out.println(root.toTikzTree(false));
//			}
//		}
//	}
}



















