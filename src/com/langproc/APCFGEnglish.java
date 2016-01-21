package com.langproc;

public class APCFGEnglish implements Grammar {

	APCFGParser parser = null;
	
	public APCFGEnglish()
	{
		initParser();
	}
	
	Token getTokenByGrammar(APCFGParser parser, TaggedWord tw)
	{
		if (tw.m_tags.hasSomeTags(WT.NOUN)) return parser.getTokenByName("noun");
		if (tw.m_tags.hasSomeTags(WT.VERB)) return parser.getTokenByName("verb");
		if (tw.m_tags.hasSomeTags(WT.ADV)) return parser.getTokenByName("adv");
		if (tw.m_tags.hasSomeTags(WT.ADJ)) return parser.getTokenByName("adj");
		if (tw.m_tags.hasSomeTags(WT.PRONOUN)) return parser.getTokenByName("pronoun");
		if (tw.m_tags.hasSomeTags(WT.NEGATION)) return parser.getTokenByName("neg");
		if (tw.m_tags.hasSomeTags(WT.COMMA)) return parser.getTokenByName(tw.m_word);
		if (tw.m_tags.hasSomeTags(WT.CONJ)) return parser.getTokenByName("conj");
		if (tw.m_tags.hasSomeTags(WT.NUMERAL)) return parser.getTokenByName("num");
		if (tw.m_tags.hasSomeTags(WT.PARTICLE)) return parser.getTokenByName("particle");
		if (tw.m_tags.hasSomeTags(WT.ADVPART)) return parser.getTokenByName("advp");
		if (tw.m_tags.hasSomeTags(WT.PREPOSITION)) return parser.getTokenByName(tw.m_word);
		if (tw.m_tags.hasSomeTags(WT.SENTENCE_END)) return parser.getTokenByName(tw.m_word);
		if (tw.m_tags.hasSomeTags(WT.HELPWORD)) return parser.getTokenByName("help");
		return null; // no grammar name can be found
		// return parser.getTokenByName(tw.m_word);
	}
	
	public void initParser()
	{	
		parser = new APCFGParser();
		// attributed noun "Лис Микита"
		
//		parser.addRule("IVP -> <не>? verb[i]");
//		return;
		
		parser.addRule("teach[p3n1] -> <teaches>[r]");
		parser.addRule("VP[PN] -> teach[PN]");
		parser.addRule("noun[PN] -> professor[PN] | math[PN] | student[PN] | car[PN] | book[PN] | sonata[PN]");
		parser.addRule("NP[PN] -> noun[PN]");
		parser.addRule("NP[PN] -> art noun[PN]");
		
		parser.addRule("DS -> NP[PN] VP[PN]");
		parser.addRule("S -> START DS END");
		parser.addRule("VP[PNT] -> VP[PNT] DNP");
		parser.addRule("VP[PNT] -> VP[PNT] NP");
		parser.addRule("VP[PNT] -> verb[PNT]");
		parser.addRule("OBJECT[PN] -> NP[PN]");
		parser.addRule("DNP[PN] -> prep? NP[PN]");
		
		parser.addRule("professor[p3n1] -> <Professor>[r]");
		parser.addRule("math[p3 n1 n*] -> <math>[r]");	
		parser.addRule("book[p3 n1] -> book[r]");
		
		parser.addRule("prep -> prep_to | prep_on");
		
		parser.addRule("prep_to -> <to>[r]");
		parser.addRule("prep_on -> <on>[r]");
		parser.addRule("student[p3n*] -> <students>[r]");
		parser.addRule("art -> def_art | undef_art");
		parser.addRule("def_art -> <the>[r]");
		parser.addRule("undef_art -> <a>[r]");
		parser.addRule("car[p3n1] -> <car>[r]");
		
		parser.addRule("play[p3n1] -> plays[r]");
		parser.addRule("play[p1p2n1] -> play[r]");
		parser.addRule("play[p1p2p3n*] -> play[r]");
		
		parser.addRule("play_perform[PN] 1.1 -> play[PN] musical_composition prep_on musical_instrument");
		parser.addRule("verb[PNT] -> play_perform[PNT]");
		parser.addRule("verb[PNT] -> play[PNT]");
		
		parser.addRule("musical_instrument[PN] -> piano[PN]");
		parser.addRule("musical_instrument[PN] -> piano[PN]");
		
		parser.addRule("piano[p3n1] -> the piano[r]");
		parser.addRule("piano[p3n*] -> pianos[r]");		
		
		parser.addRule("musical_composition[PN] -> sonata[PN]");
		parser.addRule("sonata[p3n1] -> sonata[r]");
		
		parser.addRule("NP[p3n1] -> The? boy[r]");
			
		parser.addRule("VP[PN] 1.1 -> teach[PN] <knowledge_domain> prep_to <can_learn>");
		
		parser.addRule("<knowledge_domain>[PN] -> <discipline_subject>[PN]");
		parser.addRule("<discipline_subject>[PN] -> science[PN]");
		parser.addRule("science[PN] -> math[PN]");

		parser.addRule("<can_learn>[PN] -> <person_individual>[PN]");
		parser.addRule("<person_individual>[PN] -> enrollee[PN]");
		parser.addRule("enrollee[PN] -> student[PN]");
		
		parser.addRule("posessive_pronoun[n1n*] -> My[r]");
		
		parser.addRule("father[p1p2p3n1] -> father[r]");
		parser.addRule("noun[PN] -> father[PN]");
		
		parser.addRule("NP[PN] -> posessive_pronoun[N] noun[PN]");		
			
		parser.addRule("verb[PNT] -> buy[PNT]");
		parser.addRule("buy[p1p2p3n1n*tp] -> bought[r]");
		
		parser.addRule("noun[p1p2p3n*] 0.9 -> several[r]"); // rarely used
		
		parser.addRule("num_adj[p1p2p3n*] -> several[r]");
		
		parser.addRule("NP[PN] -> num_adj[PN] NP[PN]");
		parser.addRule("DNP -> preposition_place DNP");
		
		parser.addRule("candy[p1p2p3n*] -> candies[r]");
		parser.addRule("noun[PN] -> candy[PN]");
		
		parser.addRule("preposition_place -> at[t]");
		
		parser.addRule("shop[p1p2p3n1] -> shop[r]");
		parser.addRule("noun[PN] -> shop[PN]");
		
		parser.addRule("END -> <.>");	
	}

	public String processSentence(Morphology morphology, Sentence s, boolean use_word_weighting)
	{
		java.util.Vector<java.util.List<ParsedToken>> tokens = new java.util.Vector<java.util.List<ParsedToken>>();

		java.util.List<ParsedToken> ptl_start = new java.util.ArrayList<ParsedToken>();
		ParsedToken pt_start = new ParsedToken(parser.getTokenByName("START"), new WordTags(), 1.0f, "");
		if (LangProcSettings.DEBUG_OUTPUT)
		{
			System.out.println("Add token " + pt_start);
		}
		ptl_start.add(pt_start);
		tokens.add(ptl_start);

		for (WordHypotheses sw : s)
		{
			java.util.List<ParsedToken> ptl = new java.util.ArrayList<ParsedToken>();
			int n = sw.numHypotheses();
			for (int i = 0; i < n; ++i)
			{
				TaggedWord tw = sw.getHypothesis(i);
				float w = 0.5f; // base weight for the word
				if (use_word_weighting)
				{
					int weight = morphology.getWordStatisticalWeight(tw.m_word, tw.m_base_word);
					w += weight * 0.1f;
				}

				WordTags token_sp = tw.m_tags;
				
				// fix modality
				if (token_sp.hasAllTags(WT.VERB))// && !token_sp.hasAllTags(WT.MODAL))
				{
					// treat all verbs as possible non-modal
					token_sp.setTags(WT.NON_MODAL);
				}
				
				Token req_token = getTokenByGrammar(parser, tw);
				if (req_token != null)
				{
					ParsedToken pt = new ParsedToken(req_token, token_sp, 1.0f, tw.m_word_as_was_written);
					if (LangProcSettings.DEBUG_OUTPUT)
					{
						System.out.println("Add token " + pt);
					}
					ptl.add(pt);
				}

				Token byname_token = parser.getTokenByName(tw.m_word);
				WordTags token_raw = new WordTags(token_sp);
				token_raw.setTags(WT.RAW);
				ParsedToken pt1 = new ParsedToken(byname_token, token_raw, 1.0f, tw.m_word_as_was_written);
				if (LangProcSettings.DEBUG_OUTPUT)
				{
					System.out.println("Add token " + pt1);
				}
				ptl.add(pt1);
				
				if (!tw.m_base_word.equals(tw.m_word))
				{
					Token bybaseform_token = parser.getTokenByName(tw.m_base_word);
					ParsedToken pt2 = new ParsedToken(bybaseform_token, token_sp, 1.0f, tw.m_word_as_was_written);
					if (LangProcSettings.DEBUG_OUTPUT)
					{
						System.out.println("Add token " + pt2);
					}
					ptl.add(pt2);
				}
			}
			tokens.add(ptl);
		}
		System.out.println();

		java.util.List<ParsedToken> res = parser.parse(tokens);

		if (res == null || res.size() == 0)
		{
			LangProcOutput.println("No results");
			//System.exit(0);
			return null;
		}
		else
		{
			for (ParsedToken root : res)
			{
				LangProcOutput.println();
				LangProcOutput.println(root.toTikzTree(false));
				LangProcOutput.flush();
			}
		}

		return "";
	}

}
