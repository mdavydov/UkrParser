/*******************************************************************************
 * UkrParser
 * Copyright (c) 2013-2014 Maksym Davydov
 * 
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the GNU Public License v3.0
 * which accompanies this distribution, and is available at
 * http://www.gnu.org/licenses/gpl-3.0.txt
 ******************************************************************************/

// Affix probabilistic context free grammar for Ukrainian language

package com.langproc;

public class APCFGUkrainian  implements Grammar
{
	APCFGParser parser = null;
	
	public APCFGUkrainian()
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
		
		parser.addRule("QS *-> <скільки>(*) PLACE? DNP[c2]");

		parser.addRule("V -> у | в");
		parser.addRule("Z -> з | із | зі");
		parser.addRule("TARGETPRP -> на");
		
		parser.addRule("GENCOMMA -> <,> | <.> | <:> | <?> | <!> | START");

		parser.addRule("AN[NCG] -> noun(*)[NCG] noun[NCGu]?");
		// noun group (adjectives, etc)
		parser.addRule("COMMEDADJG[NCG] -> <,> adj(*)[NCG] GENCOMMA!");
		parser.addRule("COMMEDADJG[NCG] -> COMMEDADJG(*)[NCG] <,> adj[NCG] GENCOMMA!");
		parser.addRule("ADJG[NCG] -> adj[NCG]");
		parser.addRule("ADJQ[NCG] -> adj[NCGq]"); // якою? котрою? ... question with adjective attributes
		parser.addRule("ADJG[NCG] -> COMMEDADJG[NCG]");

		parser.addRule("NG[NCG] -> adj[NCG]? AN[NCG] NG(*)[c2]? ADJG[NCG]?");
		parser.addRule("NG[NCG] -> adj[NCG]? pronoun(*)[NCG] ADJG[NCG]?");
		parser.addRule("NG[C n*] -> NG(*)[C] conj NG[C]");

		parser.addRule("NG[NCG] -> adj[NCG] NG(*)[NCG]");
		parser.addRule("DNP[NCG] -> NG[NCG c2c3c4c5c6c7]");
		parser.addRule("NP[NCG p3] -> NG[NCG c1]");
		parser.addRule("NP[NCGP] -> pronoun[NCGP c1]");
		parser.addRule("NP[NCGP] -> NP(*) <чи> NP[NCGP]");

		parser.addRule("TARGET -> TARGETPRP(*) DNP[c6] | V(*) DNP[c4] | <до>(*) DNP[c2] | <додому> | <туди> | <сюди>");
		parser.addRule("NAME -> noun[NCGu]");
		parser.addRule("ADDRESS -> DNP(*)[c3]");
		parser.addRule("PLACE -> V DNP(*)[c6] | <тут> | <там>");
		parser.addRule("ADDITIONAL -> Z DNP(*)[c5]");
		parser.addRule("TIME -> <зараз> | <потім>");
		parser.addRule("FROM -> Z DNP(*)[c2]");
		parser.addRule("OBJECT -> DNP[c4]");
		
		parser.addRule("ADV -> adv");

		// parser.addRule("VP[PN] *-> verb[PN] ADDRESS? PLACE? ADDITIONAL? OBJECT? FROM? TARGET? TIME?");
		parser.addRule("VP[PNM] -> verb(*)[PNM]");
		parser.addRule("VP[PN m-] 1.1-> verb(*)[PNm+] IVP");
		parser.addRule("VP[p1p2p3p-N] -> ADJG(*)[N]");
		parser.addRule("VP[PNM] -> <не> VP(*)[PNM]");
		parser.addRule("VP[PNM] *-> ADV VP(*)[PNM]");
		
		parser.addRule("VP[PNM] *-> VP(*)[PNM] ADDRESS");
		parser.addRule("VP[PNM] *-> VP(*)[PNM] PLACE");
		parser.addRule("VP[PNM] *-> VP(*)[PNM] ADDITIONAL");
		parser.addRule("VP[PNM] *-> VP(*)[PNM] OBJECT");
		parser.addRule("VP[PNM] *-> VP(*)[PNM] FROM");
		parser.addRule("VP[PNM] *-> VP(*)[PNM] TARGET");
		parser.addRule("VP[PNM] *-> VP(*)[PNM] TIME");
		
		parser.addRule("noun[c3 n*] -> <дітям>[r]");
		parser.addRule("<дитина>[c3 n*] -> <дітям>[r]");
		parser.addRule("<дитина>[c1 n1] -> <дитина>[r]");
		parser.addRule("noun[c2 c4 gm n1] -> <коня>[r]");
		
		parser.addRule("noun[NCG] -> <серце>[NCG]");
		parser.addRule("<серце>[c1c4 n1 gn] -> <серце>[r]");
		
		parser.addRule("<б'ється >[p3 n1] -> <б'ється>[r]");
		
		parser.addRule("adj[c1c4 n1 gn] -> <моє>[r]");
		
		parser.addRule("verb[p2 n1 m+] -> <хочеш>[r]");
		
		parser.addRule("verb[p1 p2 p3 n* m+ m-] -> <прийшли>[r]");
		
		parser.addRule("<серце_б’ється>[p- NC] 1.1-> <серце>[NC] <б'ється >[p3 N]");
		
		parser.addRule("VP[PN] -> <серце_б’ється>[PN]");
		
		parser.addRule("<людина>[NCG] -> <дитина>[NCG]");
		parser.addRule("<слухач>[NCG] -> <людина>[NCG]");
		
		parser.addRule("<історія-розповідь>[NCG] -> <казка>[NCG]");
		parser.addRule("<історія-розповідь>[NCG] -> <оповідання>[NCG]");
		parser.addRule("<історія-розповідь>[NCG] -> <пригода>[NCG]");
		
		parser.addRule("<розповідати-синсет>[PNM] * 1.1-> <розповідати>[PNM] <історія-розповідь>[c4] <слухач>[c3]?");
		parser.addRule("verb[PNM] -> <розповідати-синсет>[PNM]");
		
		parser.addRule("<вчити-навчати>[p1 n1 m-] -> <вчу>");
		
		parser.addRule("<школяр>[c4 c2 n* gm gf gn] -> <школярів>");
		parser.addRule("<людина>[NCG] -> <школяр>[NCG]");
		parser.addRule("<може-вчитись>[NCG] -> <людина>[NCG]");

		parser.addRule("<наука>[NCG] -> <математика>[NCG]");
		parser.addRule("<містить-знання>[NCG] -> <наука>[NCG]");
		
		
		parser.addRule("<вчити-навчати>[PNM] * 1.1-> <вчити-навчати>[PNM] <може-вчитись>[c4]");
		parser.addRule("<вчити-навчати>[PNM] * 1.1-> <вчити-навчати>[PNM] <містить-знання>[c3]");
		parser.addRule("VP[PNM] -> <вчити-навчати>[PNM]");

		// parser.addRule("IVP -> verb[i] ADDRESS? PLACE? ADDITIONAL? OBJECT? FROM? TARGET? TIME?");
		// parser.addRule("IVP -> verb[i] OBJECT? PLACE? NAME");
		parser.addRule("IVP -> <не>? verb[i](*)");
		parser.addRule("IVP *-> IVP(*) ADDRESS");
		parser.addRule("IVP *-> IVP(*) ADV");
		parser.addRule("IVP *-> IVP(*) PLACE");
		parser.addRule("IVP *-> IVP(*) ADDITIONAL");
		parser.addRule("IVP *-> IVP(*) OBJECT");
		parser.addRule("IVP *-> IVP(*) FROM");
		parser.addRule("IVP *-> IVP(*) TARGET");
		parser.addRule("IVP *-> IVP(*) TIME");
		parser.addRule("IVP *-> IVP(*) NAME");


		// parser.addRule("DS *0.8 -> verb[PN] NP[PN] ADDRESS? PLACE? ADDITIONAL? OBJECT? FROM? TARGET?");
		// parser.addRule("IS *-> verb[i] NP[PN] ADDRESS? PLACE? ADDITIONAL? OBJECT? FROM?  TARGET?");

		parser.addRule("VP[p1p2p3 N] -> <є>(*) adj[N c4c5]"); // TARGET?
		parser.addRule("VP[p1p2p3 N] -> <є>(*) DNP[N c4c5]"); // TARGET?

		parser.addRule("DS[n1] -> <ви> <є>(*) DNP[n1 c4c5]"); // TARGET?
		// parser.addRule("VP[PN] -> PLACE verb[PN] OBJECT ADDITIONAL"); //
		// TARGET?
		// parser.addRule("VP[PN] -> verb[PN] ADDRESS PLACE"); // TARGET?
		// parser.addRule("DS *-> NP[NP] VP[NP]");
		parser.addRule("DS *-> VP(*)[NP] NP[NP]"); // Я зробив завдання
		parser.addRule("DS -> VP(*)[p-]"); // Зроблено завдання
		parser.addRule("DS -> IVP(*)"); // Робити завдання
		parser.addRule("DS -> verb(*)[NPm+] NP[NP] IVP");

		parser.addRule("DS *-> DS(*) ADDRESS");
		parser.addRule("DS *-> DS(*) PLACE");
		parser.addRule("DS *-> DS(*) ADDITIONAL");
		parser.addRule("DS *-> DS(*) OBJECT");
		parser.addRule("DS *-> DS(*) FROM");
		parser.addRule("DS *-> DS(*) TARGET");
		parser.addRule("DS *-> DS(*) TIME");
		parser.addRule("DS *-> DS(*) NAME");
		parser.addRule("DS *-> DS(*) adv");
		
		// parser.addRule("EEEE -> <є> adj");

		parser.addRule("AKSTOSAY[p2 n1] -> <розкажи> | <скажи> | <повідом> | <повтори> | <розкажіть>");

		parser.addRule("QS -> DS(*)");
		parser.addRule("QS -> <чи> DS(*)");
//		parser.addRule("QS -> <якого> DNP[n1 c2] DNP? DS?");
//		parser.addRule("QS -> <яких> DNP[n* c2] DNP? DS?");
//		parser.addRule("QS -> <якою> DNP[n* gf c2] DNP? DS?");
		parser.addRule("QS -> pronoun(*)[NGC] DNP[NGC] DNP? DS?");
		parser.addRule("QS -> adj(*)[NGCq] DNP[NGC] DNP? DS?");
		parser.addRule("QS -> ADJQ(*)[NGC] DNP[NGC] DNP? DS?");
		
		parser.addRule("QS -> <чи> QS(*)");
		parser.addRule("QS *-> adv DS(*)");
		parser.addRule("QS *-> adv IVP(*)");
		parser.addRule("QS *-> pronoun[q] NP(*)");
		parser.addRule("QS -> pronoun[q] DS(*)");

		// parser.addRule("QS -> <скільки> DNP[c2] PLACE?");
		
		parser.addRule("END -> <.>");
		parser.addRule("QEND -> <?>");
		parser.addRule("S -> START DS END");
		parser.addRule("S -> START NP <.>");
		parser.addRule("S -> START NP QEND");
		parser.addRule("S -> START VP QEND");
		parser.addRule("S -> START QS QEND");
		parser.addRule("S -> START AKSTOSAY QS QEND");
		parser.addRule("S -> START AKSTOSAY DNP[c3]? <,>? QS QEND");
	}

	public String processSentence(Morphology morphology, Sentence s, boolean use_word_weighting)
	{
		java.util.Vector<java.util.List<ParsedToken>> tokens = new java.util.Vector<java.util.List<ParsedToken>>();

		java.util.List<ParsedToken> ptl_start = new java.util.ArrayList<ParsedToken>();
		ParsedToken pt_start = new ParsedToken(parser.getTokenByName("START"), null, new WordTags(), 1.0f, "");
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
				
				Token grammar_token = getTokenByGrammar(parser, tw);
				Token byname_token = parser.getTokenByName(tw.m_word);
				Token bybaseform_token = parser.getTokenByName(tw.m_base_word);
				if (bybaseform_token==null) bybaseform_token = byname_token;
				
				if (grammar_token != null)
				{
					ParsedToken pt = new ParsedToken(grammar_token, bybaseform_token, token_sp, 1.0f, tw.m_word_as_was_written);
					if (LangProcSettings.DEBUG_OUTPUT)
					{
						System.out.println("Add token " + pt);
					}
					ptl.add(pt);
				}

				WordTags token_raw = new WordTags(token_sp);
				token_raw.setTags(WT.RAW);
				ParsedToken pt1 = new ParsedToken(byname_token, bybaseform_token, token_raw, 1.0f, tw.m_word_as_was_written);
				if (LangProcSettings.DEBUG_OUTPUT)
				{
					System.out.println("Add token " + pt1);
				}
				ptl.add(pt1);
				
				if (!tw.m_base_word.equals(tw.m_word))
				{
					ParsedToken pt2 = new ParsedToken(bybaseform_token, bybaseform_token, token_sp, 1.0f, tw.m_word_as_was_written);
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
