package com.langproc;

import java.util.Random;
import java.util.Vector;

public class Sentence
{
	public Vector<SentenceWord> m_words = new Vector<SentenceWord>();

	public int numWords()
	{
		return m_words.size();
	}

	public int numHypotheses()
	{
		int num = 0;
		for (SentenceWord w : m_words)
		{
			num += w.numHypotheses();
		}
		return num;
	}

	public void addWord(SentenceWord w)
	{
		m_words.addElement(w);
	}

	SentenceWord sentenceWordAt(int index)
	{
		return m_words.elementAt(index);
	}

	boolean hasWordsWithAllTagsBetween(int index_from, int index_to, long tags)
	{
		if (Math.abs(index_from - index_to) <= 1) return false;

		if (index_to >= m_words.size()) index_to = m_words.size() - 1;
		if (index_to < 0) index_to = 0;
		if (index_from >= m_words.size()) index_from = m_words.size() - 1;
		if (index_from < 0) index_from = 0;

		int dir = index_to >= index_from ? +1 : -1;
		index_to += dir;

		for (int i = index_from + dir; i != index_to - dir; i += dir)
		{
			SentenceWord sw = m_words.elementAt(i);
			if (sw.hasHypothesisWithAllTags(tags)) return true;
		}
		return false;
	}

	boolean hasWordsWithSomeTagsBetween(int index_from, int index_to, long tags)
	{
		if (Math.abs(index_from - index_to) <= 1) return false;

		if (index_to >= m_words.size()) index_to = m_words.size() - 1;
		if (index_to < 0) index_to = 0;
		if (index_from >= m_words.size()) index_from = m_words.size() - 1;
		if (index_from < 0) index_from = 0;

		int dir = index_to >= index_from ? +1 : -1;
		index_to += dir;

		for (int i = index_from + dir; i != index_to - dir; i += dir)
		{
			SentenceWord sw = m_words.elementAt(i);
			if (sw.hasHypothesisWithSomeTags(tags)) return true;
		}
		return false;
	}

	boolean hasWordsWithSomeTagsBetween(int index_from, int index_to,
			long tags_group1, long tags_group2)
	{
		if (Math.abs(index_from - index_to) <= 1) return false;

		if (index_to >= m_words.size()) index_to = m_words.size() - 1;
		if (index_to < 0) index_to = 0;
		if (index_from >= m_words.size()) index_from = m_words.size() - 1;
		if (index_from < 0) index_from = 0;

		int dir = index_to >= index_from ? +1 : -1;
		index_to += dir;

		for (int i = index_from + dir; i != index_to - dir; i += dir)
		{
			SentenceWord sw = m_words.elementAt(i);
			if (sw.hasHypothesisWithSomeTags(tags_group1, tags_group2)) return true;
		}
		return false;
	}

	double linkPrefClose(int sp1, int sp2)
	{
		if (sp2 > sp1)
		{
			return Math.exp((1.0 - Math.abs(sp1 - sp2)) * 0.5);
		}
		else
		{
			return Math.exp((0.5 - Math.abs(sp1 - sp2)) * 0.5);
		}
	}

	double linkPrefSeq(int sp1, int sp2)
	{
		if (sp2 > sp1)
		{
			return Math.exp((1.0 - Math.abs(sp1 - sp2)) * 0.5);
		}
		else
		{
			return Math.exp((0.5 - Math.abs(sp1 - sp2)) * 0.5);
		}
	}

	double linkPrefTogether(int sp1, int sp2)
	{
		if (sp2 == sp1 + 1)
		{
			return 1.5 * Math.exp((1.0 - Math.abs(sp1 - sp2)) * 0.5);
		}
		else if (sp2 > sp1)
		{
			return Math.exp((1.0 - Math.abs(sp1 - sp2)) * 0.5);
		}
		else
		{
			return Math.exp((0.5 - Math.abs(sp1 - sp2)) * 0.5);
		}
	}

	double linkHardSeq(int sp1, int sp2)
	{
		if (sp2 > sp1)
		{
			return Math.exp((1.0 - Math.abs(sp1 - sp2)) * 0.5);
		}
		return 0.0;
	}

	double linkGovernmentPrepos(int sp1, int sp2)
	{
		if (sp2 > sp1)
		{
			return 1.0 + Math.exp((1.0 - Math.abs(sp1 - sp2)) * 0.5);
		}
		return 0.0;
	}

	double linkNonGovernmentPrepos(int sp1, int sp2)
	{
		if (sp2 > sp1)
		{
			return Math.exp((1.0 - Math.abs(sp1 - sp2)) * 0.5) / 10;
		}
		return 0.0;
	}

	void addPossibleRelation(ChoiceGraph cg, LangProc langproc, TaggedWord w1,
			TaggedWord w2)
	{
		WordTags t1 = w1.getTags();
		WordTags t2 = w2.getTags();

		int sp1 = w1.getSentencePos();
		int sp2 = w2.getSentencePos();

		if (sp1 == sp2) return;

		double dk = Math.exp((1.0 - Math.abs(sp1 - sp2)) * 0.5);

		if (sp2 > sp1)
		{
			if (t2.hasSomeTags(WT.SENTENCE_END))
			{
				if (t1.hasSomeTags(WT.VERB | WT.QUESTION)
						&& !t1.hasSomeTags(WT.INFINITIVE))
				{
					if (w2.m_word.equals("?")) cg.addEdge("QUESTION", 0.05, w2, w1);
					else if (w2.m_word.equals("!")) cg.addEdge("EXCLAMATION", 0.05, w2, w1);
					else if (w2.m_word.equals(".")) cg.addEdge("ROOT", 0.05, w2, w1);
					else if (w2.m_word.equals(";")) cg.addEdge("ROOT", 0.05, w2, w1);
				}
				else
				{
					cg.addEdge("OPTIONAL", LangProcSettings.OPTIONAL_WEIGHT,
							w2, w1);
				}
			}

			if (t1.hasAllTags(WT.PRONOUN | WT.INDICATIVE)
					&& t2.hasAllTags(WT.PRONOUN | WT.QUESTION)
					&& sp2 == sp1 + 2
					&& hasWordsWithSomeTagsBetween(sp1, sp2, WT.COMMA))
			{ // noun-to-noun
				// relations
				cg.addEdge("INDICATES", 1.0, w1, w2);
			}

			if (t1.hasAllTags(WT.PRONOUN | WT.QUESTION)
					&& t2.hasAllTags(WT.VERB))
			{ // noun-to-noun relations
				cg.addEdge("QUESTION", linkHardSeq(sp1, sp2), w1, w2);
			}

			if (t1.hasSomeTags(WT.ANY_NOUN) && t2.hasSomeTags(WT.ANY_NOUN))
			{ // noun-to-noun
				// relations
				if (t1.sameCasus(t2))
				{
					// we should redo this to make it better and more universal
					if ((hasWordsWithAllTagsBetween(sp1, sp2, WT.COMMA) || hasWordsWithAllTagsBetween(
							sp1, sp2, WT.CONJ)))
					{
						cg.addEdge("HOMOG-NOUN", dk, w1, w2);
					}
					else if (sp2 == sp1 + 1 && t1.sameCount(t2)
							&& t1.hasTag(WT.NOUN))
					{
						cg.addEdge("APPOSITION", 1.1, w1, w2);
					}
				}

				if (t2.hasTag(WT.CASUS2)
						&& t2.hasSomeTags(WT.NOUN)
						&& !hasWordsWithAllTagsBetween(sp1, sp2, t1.getCasus()
								| WT.ANY_NOUN)
								&& !hasWordsWithAllTagsBetween(sp1, sp2, WT.COMMA)
								&& !hasWordsWithAllTagsBetween(sp1, sp2, WT.CONJ))
				{
					if (w2.hasSomeTags(WT.NOUN) && !w1.hasAllTags(WT.NUMERAL))
					{
						cg.addEdge("BELONG-TO", linkPrefSeq(sp1, sp2), w1, w2);
					}
				}
			}

			if (t1.hasTag(WT.VERB) && t2.hasTag(WT.PARTICLE))
			{
				if (m_words.get(sp2 - 1).hasHypothesisWithAllTags(WT.COMMA))
				{
					if (w2.m_word.equals("����"))
					{
						cg.addEdge("RESULT", 10, w2, w1);
					}
					if (w2.m_word.equals("�������"))
					{
						cg.addEdge("REASON", 10, w2, w1);
					}
				}
			}

			if (sp1 >= 2 && t1.hasTag(WT.PARTICLE) && t2.hasTag(WT.VERB))
			{
				if (m_words.get(sp1 - 1).hasHypothesisWithAllTags(WT.COMMA))
				{
					if (w1.m_word.equals("����"))
					{
						cg.addEdge("REASON", 10, w1, w2);
					}
					if (w1.m_word.equals("�������"))
					{
						cg.addEdge("RESULT", 10, w1, w2);
					}
				}
			}

			if (t1.hasTag(WT.VERB) && t2.hasTag(WT.VERB))
			{ // verb-to-verb
				// relations
				if (t1.samePerson(t2) && t1.sameCount(t2))
				{
					cg.addEdge("HOMOG-VERB", dk, w1, w2);
				}
			}

			if (t1.hasSomeTags(WT.ADV) && t2.hasSomeTags(WT.ADV))
			{ // noun-to-noun
				// relations
				// we should
				// redo this
				// to make
				// it better
				// and more
				// universal
				if ((hasWordsWithAllTagsBetween(sp1, sp2, WT.COMMA) || hasWordsWithAllTagsBetween(
						sp1, sp2, WT.CONJ)))
				{
					cg.addEdge("HOMOG-ADV", dk, w1, w2);
				}
			}
			if (t1.hasSomeTags(WT.ADJ) && t2.hasSomeTags(WT.ADJ))
			{ // noun-to-noun
				// relations
				if (t1.sameCasus(t2) && t1.sameCount(t2) && t1.sameGender(t2))
				{
					// we should redo this to make it better and more universal
					if ((hasWordsWithAllTagsBetween(sp1, sp2, WT.COMMA) || hasWordsWithAllTagsBetween(
							sp1, sp2, WT.CONJ)))
					{
						cg.addEdge("HOMOG-ADJ", dk, w1, w2);
					}
				}
			}
		}

		if (t1.hasTag(WT.NUMERAL) && t2.hasTag(WT.NOUN))
		{ // verb-to-verb
			// relations
			if (t1.hasTag(WT.CASUS1)
					&& langproc.m_countable_req_nom.containsKey(w1.m_word))
			{
				if (t2.hasAllTags(WT.CASUS1 | WT.PLURAL))
				{
					cg.addEdge("COUNT", linkPrefSeq(sp1, sp2), w1, w2);
				}
				else if (t2.hasAllTags(WT.CASUS2 | WT.PLURAL))
				{
					cg.addEdge("COUNT-BAD", linkPrefSeq(sp1, sp2) / 2, w1, w2);
				}
			}
			else if (t1.hasTag(WT.CASUS1))
			{
				if (t2.hasAllTags(WT.CASUS2 | WT.PLURAL))
				{
					cg.addEdge("COUNT", linkPrefSeq(sp1, sp2), w1, w2);
				}
			}
			else if (t1.sameCount(t2) && t1.sameCasus(t1))
			{
				cg.addEdge("COUNT", linkPrefSeq(sp1, sp2), w1, w2);
			}
		}

		if (t1.hasTag(WT.STATE) && t2.hasTag(WT.VERB))
		{
			if (w2.m_word.equals("����"))
			{
				cg.addEdge("TIME-MOD", linkPrefSeq(sp1, sp2), w1, w2);
			}
			else if (w2.m_word.equals("����"))
			{
				cg.addEdge("TIME-MOD", linkPrefSeq(sp1, sp2), w1, w2);
			}
		}

		if (t1.hasTag(WT.VERB) && t2.hasSomeTags(WT.PREPOS))
		{ // verb-to-verb
			// relations
			cg.addEdge("ADV|OBJ", linkPrefSeq(sp1, sp2), w1, w2);
		}

		if (t1.hasTag(WT.MODAL) && t2.hasTag(WT.INFINITIVE))
		{ // verb-to-verb
			// relations
			cg.addEdge("MOD-VERB", linkPrefSeq(sp1, sp2), w1, w2);
		}

		if (t1.hasTag(WT.VERB) && t2.hasSomeTags(WT.ANY_NOUN))
		{ // verb-to-verb
			// relations
			if (t1.samePerson(t2) && t1.sameCount(t2) && t1.sameGender(t2)
					&& t2.hasTag(WT.CASUS1))
			{
				cg.addEdge("SUBJECT", linkPrefSeq(sp2, sp1), w1, w2);
			}

			if (t2.hasTag(WT.CASUS4))
			{
				cg.addEdge("OBJECT", linkPrefSeq(sp1, sp2), w1, w2);
			}

			if (t2.hasSomeTags(WT.CASUS2 | WT.CASUS3 | WT.CASUS5 | WT.CASUS6))
			{
				cg.addEdge("IND-OBJ", linkPrefSeq(sp1, sp2), w1, w2);
			}
		}

		if (t1.hasTag(WT.VERB) && t2.hasTag(WT.ADJ))
		{ // verb-to-adjective relations
			if (t2.hasTag(WT.CASUS5)) // "������� �������"
			{
				cg.addEdge("ADVERBIAL", linkPrefSeq(sp1, sp2), w1, w2);
			}

			if (t2.hasTag(WT.CASUS4))
			{
				cg.addEdge("OBJECT", linkPrefSeq(sp1, sp2) * 0.25f, w1, w2);
			}

			if (t2.hasSomeTags(WT.CASUS2 | WT.CASUS3 | WT.CASUS5 | WT.CASUS6))
			{
				cg.addEdge("IND-OBJ", linkPrefSeq(sp1, sp2), w1, w2);
			}
		}

		if (t1.hasTag(WT.ADVPART) && t2.hasSomeTags(WT.ANY_NOUN))
		{ // verb-to-verb
			// relations
			if (t2.hasTag(WT.CASUS4))
			{
				cg.addEdge("OBJECT", linkPrefSeq(sp1, sp2), w1, w2);
			}

			if (t2.hasSomeTags(WT.CASUS2 | WT.CASUS3 | WT.CASUS5 | WT.CASUS6))
			{
				cg.addEdge("IND-OBJ", linkPrefSeq(sp1, sp2), w1, w2);
			}
		}

		if (t1.hasSomeTags(WT.ANY_NOUN) && t2.hasTag(WT.ADJ))
		{
			if (t1.sameCount(t2) && t1.sameGender(t2) && t1.sameCasus(t2))
			{
				cg.addEdge("ADJ", linkPrefTogether(sp2, sp1), w1, w2);
			}
		}

		if (t1.hasTag(WT.PREPOS) && t2.hasSomeTags(WT.ANY_NOUN))
		{
			WordTags req_tags = langproc.m_prepositions.get(w1.m_base_word);

			if (t2.hasSomeTags(req_tags)
					&& !hasWordsWithSomeTagsBetween(sp1, sp2, WT.ANY_NOUN,
							req_tags.m_tags))
			{
				cg.addEdge("PREPOS", linkGovernmentPrepos(sp1, sp2), w1, w2);
			}
			else
			{
				// less link with word that does not meet requires casus
				cg.addEdge("PREPOS", linkNonGovernmentPrepos(sp1, sp2), w1, w2);
			}
		}

		if (t1.hasSomeTags(WT.ANY_VERB) && t2.hasTag(WT.ADV))
		{
			cg.addEdge("ADV", linkPrefSeq(sp1, sp2), w1, w2);
		}

		if (t1.hasTag(WT.VERB) && t2.hasTag(WT.ADVPART))
		{
			cg.addEdge("V-ADVPART", linkPrefClose(sp1, sp2), w1, w2);
		}

		if (t2.hasTag(WT.NEGATION))
		{
			if (sp1 == sp2 + 1)
			{
				cg.addEdge("NEG", 1, w1, w2);
			}
		}
	}

	public int getNumCorrectionChoices()
	{
		int total = 1;

		for (SentenceWord sw : m_words)
		{
			int n = sw.numHypotheses();

			boolean different = false;

			if (n > 0)
			{
				String case1 = sw.getHypothesis(0).m_word;
				for (int i = 1; i < n; ++i)
				{
					TaggedWord tw = sw.getHypothesis(i);
					if (!tw.m_word.equals(case1)) different = true;
				}
			}
			total *= n == 0 || !different ? 1 : n;
		}
		// System.out.println("Total = " + total);
		return total;
	}

	public String tryFixUsingRandom(LangProc langproc, boolean use_word_weighting)
	{
		int vertex_choices[] = new int[numWords()];

		Random randomGen = new Random();

		int i = 0;
		for (SentenceWord sw : m_words)
		{
			int n = sw.numHypotheses();
			// TODO: reevaluate word weight. Now all are 1.0f :-)

			int max_weight = 0;
			int best_val = 0;

			if (use_word_weighting)
			{
				for (int j = 0; j < n; ++j)
				{
					TaggedWord tw = sw.getHypothesis(j);
			
					int weight = langproc.getWordStatisticalWeight(tw.m_word, tw.m_base_word);

					// if (num_base_use!=null &&
					// num_base_use.getMeanInterval()>200) weight=0;

					if (weight > max_weight)
					{
						max_weight = weight;
						best_val = j;
					}
				}
			}

			vertex_choices[i] = max_weight > 0 ? best_val : (n > 0 ? randomGen.nextInt(n) : 0);
			++i;
		}

		StringBuffer sb = new StringBuffer(128);

		for (i = 0; i < vertex_choices.length; ++i)
		{
			if (m_words.get(i).numHypotheses() > 0)
			{
				TaggedWord tw = m_words.get(i).getHypothesis(vertex_choices[i]);
				if (i != 0 && !tw.hasSomeTags(WT.ANY_PUNCT)) sb.append(' ');
				sb.append(tw.m_word);
			}
		}
		sb.setCharAt(0, java.lang.Character.toUpperCase(sb.charAt(0)));

		return sb.toString();

	}

	public String processSentence(LangProc langproc, boolean use_word_weighting)
	{
		ChoiceGraph cg = new ChoiceGraph(numWords(), numHypotheses());

		Vector<TaggedWord> all_words = new Vector<TaggedWord>();

		for (SentenceWord sw : m_words)
		{
			int n = sw.numHypotheses();
			for (int i = 0; i < n; ++i)
			{
				// TODO: reevaluate word weight. Now all are 1.0f :-)
				TaggedWord tw = sw.getHypothesis(i);
				float w = 0.5f; // base weight for the word
				// if (tw.m_base_word.equals("�����")) w = 0.9f;

				if (use_word_weighting)
				{
					int weight = langproc.getWordStatisticalWeight(tw.m_word, tw.m_base_word);
					
					// if (num_base_use!=null &&
					// num_base_use.getMeanInterval()>200) weight=0;

					w += weight * 0.1f;
				}

				cg.addVertex(tw, w, i == 0);
				all_words.addElement(tw);
			}
		}

		// for(int i=0;i<all_words.size();++i)
		// {
		// TaggedWord tw = all_words.get(i);
		// LangProcOutput.println("\\node[main node] ("+ i +") at ("+
		// (135-i*305/all_words.size())+":7cm) {" + tw.m_word + "\\\\"
		// + (new WordTags(tw.m_tags.getPartOfSpeech()).toString() + "};") );
		// }

		// LangProcOutput.println("\\path[every node/.style={font=\\sffamily\\small}]");

		for (TaggedWord w1 : all_words)
			for (TaggedWord w2 : all_words)
			{
				addPossibleRelation(cg, langproc, w1, w2);
			}
		// LangProcOutput.println(";");
		// cg.print();

		// cg.print_dependencies();

		// Subtree st = cg.growingTreesSearch();

		Subtree st = null;

		if (cg.getComplexity() < 1000)
		{
			st = cg.ExhaustiveEdmondSearch();
		}
		else
		{
			st = cg.randomizedEdmondSearch(400, true, false);
		}

		if (st != null && LangProcSettings.TEX_OUTPUT)
		{
			LangProcOutput.print("\n\\hspace{1em}\n");
			LangProcOutput.println("\\resizebox{\\columnwidth}{!}{");
			LangProcOutput.println("\\begin{tikzpicture}[sibling distance=30pt]");
			LangProcOutput.println("\\Tree ");
			st.print_qtree(cg);
			LangProcOutput.println();
			LangProcOutput.println("\\end{tikzpicture}");
			LangProcOutput.println("}");
			LangProcOutput.println("\n\\hspace{1em}\n");
		}
		int vertex_choices[] = new int[numWords()];
		st.fillVertexChoices(cg, vertex_choices);

		StringBuffer sb = new StringBuffer(128);

		for (int i = 0; i < vertex_choices.length; ++i)
		{
			if (m_words.get(i).numHypotheses() != 0)
			{
				TaggedWord tw = m_words.get(i).getHypothesis(vertex_choices[i]);
				if (i != 0 && !tw.hasSomeTags(WT.ANY_PUNCT)) sb.append(' ');
				sb.append(tw.m_word);
			}
		}
		sb.setCharAt(0, java.lang.Character.toUpperCase(sb.charAt(0)));

		return sb.toString();
	}

	public void print()
	{
		for (SentenceWord w : m_words)
		{
			w.print();
			LangProcOutput.println();
			LangProcOutput.println();
		}
	}
}
