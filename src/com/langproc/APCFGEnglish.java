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
		parser.addRule("noun[PN] -> professor[PN] | math[PN] | student[PN] | car[PN]");
		parser.addRule("NP[PN] -> noun[PN]");
		parser.addRule("NP[PN] -> def_art noun[PN]");
		
		parser.addRule("S -> NP[PN] VP[PN]");
		parser.addRule("FULLS -> START S END");
		parser.addRule("VP[PN] -> VP[PN] DNP");
		parser.addRule("VP[PN] -> VP[PN] NP");
		parser.addRule("OBJECT[PN] -> NP[PN]");
		parser.addRule("DNP[PN] -> prep NP[PN]");
		parser.addRule("professor[p3n1] -> <Professor>[r]");
		parser.addRule("math[p3 n1 n*] -> <math>[r]");		
		parser.addRule("prep -> <to>[r]");
		parser.addRule("student[p3n*] -> <students>[r]");
		parser.addRule("def_art -> <the>[r]");
		parser.addRule("car[p3n1] -> <car>[r]");
		
		parser.addRule("to_prep -> to[r]");
		
		
		parser.addRule("VP[PN] 1.1 -> teach[PN] <knowledge_domain> to_prep <can_learn>");
		
		parser.addRule("<knowledge_domain>[PN] -> <discipline_subject>[PN]");
		parser.addRule("<discipline_subject>[PN] -> science[PN]");
		parser.addRule("science[PN] -> math[PN]");

		parser.addRule("<can_learn>[PN] -> <person_individual>[PN]");
		parser.addRule("<person_individual>[PN] -> enrollee[PN]");
		parser.addRule("enrollee[PN] -> student[PN]");
		
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
