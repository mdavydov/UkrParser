package com.langproc;

public class APCFGUkrSL
{
	APCFGParser parser = null;
	
	public APCFGUkrSL()
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
		if (tw.m_tags.hasSomeTags(WT.PREPOS)) return parser.getTokenByName(tw.m_word);
		if (tw.m_tags.hasSomeTags(WT.SENTENCE_END)) return parser.getTokenByName(tw.m_word);
		if (tw.m_tags.hasSomeTags(WT.HELPWORD)) return parser.getTokenByName("help");
		return null; // no grammar name can be found
		// return parser.getTokenByName(tw.m_word);
	}
	
	public void initParser()
	{	
		parser = new APCFGParser();
		
		parser.addRule("QS *-> <скільки> PLACE? DNP[c2]");

		parser.addRule("V -> у | в");
		parser.addRule("Z -> з | із | зі");
		parser.addRule("GENCOMMA -> <,> | <.> | <:> | <?> | <!> | START");

		parser.addRule("AN -> noun noun[u]?");
		// noun group (adjectives, etc)
		parser.addRule("ADJG -> adj");
		parser.addRule("ADJQ -> adj[q]"); // якою? котрою? ... question with adjective attributes
		parser.addRule("ADJG -> ADJG adj");

		parser.addRule("NG -> AN ADJG?");
		//parser.addRule("NG[NCG] -> adj[NCG]? pronoun[NCG] ADJG[NCG]?");
		parser.addRule("NG[n*] -> NG NG");
		parser.addRule("DNP -> NG");
		parser.addRule("NP -> NG");
		parser.addRule("NP -> pronoun");
		//parser.addRule("NP[NCGP] -> NP <чи> NP[NCGP]");

		parser.addRule("TARGET -> V DNP | <до> DNP | <додому> | <туди> | <сюди>");
		parser.addRule("NAME -> noun[u]");
		parser.addRule("ADDRESS -> DNP");
		parser.addRule("PLACE -> V DNP | <тут> | <там>");
		parser.addRule("ADDITIONAL -> Z DNP");
		parser.addRule("TIME -> <зараз> | <потім>");
		parser.addRule("FROM -> Z DNP");
		parser.addRule("OBJECT -> DNP");

		parser.addRule("VP -> verb[i]");
		parser.addRule("VP 1.1-> verb[m+] VP");
		//parser.addRule("VP[p1p2p3p-N] -> ADJG[N]");
		parser.addRule("VP -> <не> VP");
		
		parser.addRule("VP *-> VP ADDRESS");
		parser.addRule("VP *-> VP PLACE");
		parser.addRule("VP *-> VP ADDITIONAL");
		parser.addRule("VP *-> VP OBJECT");
		parser.addRule("VP *-> VP FROM");
		parser.addRule("VP *-> VP TARGET");
		parser.addRule("VP *-> VP TIME");
		
		parser.addRule("noun[c3 n*] -> <дітям>[r]");
		parser.addRule("<дитина>[c3 n*] -> <дітям>[r]");
		parser.addRule("<дитина>[c1 n1] -> <дитина>[r]");
		parser.addRule("noun[c2 c4 gm n1] -> <коня>[r]");
		
		parser.addRule("<людина>[NCG] -> <дитина>[NCG]");
		parser.addRule("<слухач>[NCG] -> <людина>[NCG]");
		parser.addRule("<слухач>[NCG] -> <діти>[NCG]");
		
		parser.addRule("<історія-розповідь>[NCG] -> <казка>[NCG]");
		parser.addRule("<історія-розповідь>[NCG] -> <оповідання>[NCG]");
		parser.addRule("<історія-розповідь>[NCG] -> <пригода>[NCG]");
		
		parser.addRule("<розповідати-синсет>[PNM] * 1.1-> <розповідати>[PNM] <історія-розповідь> <слухач>?");
		parser.addRule("VP[PNM] -> <розповідати-синсет>[PNM]");
		
		parser.addRule("<вчити-навчати>[p1 n1 m-] -> <вчу>");
		
		parser.addRule("<школяр>[c4 c2 n* gm gf gn] -> <школярів>");
		parser.addRule("<людина>[NCG] -> <школяр>[NCG]");
		parser.addRule("<може-вчитись>[NCG] -> <людина>[NCG]");

		parser.addRule("<наука>[NCG] -> <математика>[NCG]");
		parser.addRule("<містить-знання>[NCG] -> <наука>[NCG]");
		
		
		parser.addRule("<вчити-навчати>[PNM] * 1.1-> <вчити-навчати>[PNM] <може-вчитись>[c4]");
		parser.addRule("<вчити-навчати>[PNM] * 1.1-> <вчити-навчати>[PNM] <містить-знання>[c3]");
		parser.addRule("VP[PNM] -> <вчити-навчати>[PNM]");

		parser.addRule("VP[p1p2p3 N] -> <є> adj[N c4c5]"); // TARGET?
		parser.addRule("VP[p1p2p3 N] -> <є> DNP[N c4c5]"); // TARGET?

		parser.addRule("S[n1] -> <ви> <є> DNP[n1 c4c5]"); // TARGET?
		// parser.addRule("VP[PN] -> PLACE verb[PN] OBJECT ADDITIONAL"); //
		// TARGET?
		// parser.addRule("VP[PN] -> verb[PN] ADDRESS PLACE"); // TARGET?
		// parser.addRule("S *-> NP[NP] VP[NP]");
		parser.addRule("S *->VP NP"); // Я зробив завдання
		parser.addRule("S -> VP[p-]"); // Зроблено завдання
		parser.addRule("S -> IVP"); // Робити завдання
		parser.addRule("S -> verb[NPm+] NP[NP] IVP");

		parser.addRule("S *-> S ADDRESS");
		parser.addRule("S *-> S PLACE");
		parser.addRule("S *-> S ADDITIONAL");
		parser.addRule("S *-> S OBJECT");
		parser.addRule("S *-> S FROM");
		parser.addRule("S *-> S TARGET");
		parser.addRule("S *-> S TIME");
		parser.addRule("S *-> S NAME");
		
		// parser.addRule("EEEE -> <є> adj");

		parser.addRule("AKSTOSAY[p2 n1] -> <розкажи> | <скажи> | <повідом> | <повтори> | <розкажіть>");

		parser.addRule("QS -> S");
		parser.addRule("QS -> <чи> S");
//		parser.addRule("QS -> <якого> DNP[n1 c2] DNP? S?");
//		parser.addRule("QS -> <яких> DNP[n* c2] DNP? S?");
//		parser.addRule("QS -> <якою> DNP[n* gf c2] DNP? S?");
		parser.addRule("QS -> pronoun DNP DNP? S?");
		parser.addRule("QS -> ADJQ DNP DNP? S?");
		
		parser.addRule("QS -> <чи> QS");
		parser.addRule("QS *-> adv S");
		parser.addRule("QS *-> adv IVP");
		parser.addRule("QS *-> pronoun[q] NP");
		parser.addRule("QS -> pronoun[q] S");

		// parser.addRule("QS -> <скільки> DNP[c2] PLACE?");
		
		parser.addRule("END -> <.>");
		parser.addRule("FULLS -> START S END");
		parser.addRule("SHORTS -> START NP <.>");
		parser.addRule("FULLQ -> START NP <?>");
		parser.addRule("FULLQ -> START VP <?>");
		parser.addRule("FULLQ -> START QS <?>");
		parser.addRule("FULLQ -> START AKSTOSAY QS <?>");
		parser.addRule("FULLQ -> START AKSTOSAY DNP[c3]? <,>? QS <.>");
	}

	public String processSentenceWithAPCFG(Morphology morphology, Sentence s, boolean use_word_weighting)
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

		for (SentenceWord sw : s)
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
