package com.langproc;

import java.util.regex.Pattern;
import org.dts.spell.finder.CharSequenceWordFinder;
import org.dts.spell.finder.Word;

public abstract class UkrainianMorphologyCommons implements Morphology
{
	final String m_word_pattern = "[АБВГҐДЕЄЖЗІЙИЇКЛМНОПРСТУФХЦЧШЩЬЮЯабвгґдеєжзійиїклмнопрстуфхцчшщьюяЫЪЭЁыъэё0123456789'’-]+|,|\\.|\\?|!|\"|\'|;|:|\\)|\\(|«[^»]*»|\"[^\"]*\"";

	@Override
	public Sentence parseSentenceMorphemes(String txt)
	{
		// sentence should be normal case here!!!
		
		CharSequenceWordFinder wf = new CharSequenceWordFinder(Pattern.compile(m_word_pattern).matcher(txt));
		Sentence ss = new Sentence();
		while (wf.hasNext())
		{
			Word w = wf.next();
			// LangProcOutput.print(w.toString() + " ");
			//System.out.println("Next word " + w.toString());
			// LangProcOutput.print(w.toString());
			// int s = w.toString().length();
			// for(int i= 20; i>s; --i) LangProcOutput.print(" ");
			int word_index = ss.numWords();
			WordHypotheses wh = new WordHypotheses(word_index);
			addWordForms(wh, w.toString(), false);
			ss.addWord(wh);

			// LangProcOutput.print(w.toString() + " ");
			// LangProcOutput.println();
		}
		if (LangProcSettings.SENTENCE_OUTPUT)
		{
			LangProcOutput.println("\n");
			ss.print();
			LangProcOutput.print("\n\\hspace{1em}\n\n");
		}
		return ss;
	}

	private void addWordForms(WordHypotheses wh, String word_as_written, boolean try_error_corrections)
	{
		// LangProcOutput.println("addWordForms " + word);
		if (word_as_written.length()==0) return;
		
		int word_index = wh.getSentencePos();
		
		String lower_word = word_as_written.toLowerCase();
		String upper_word = word_as_written.toUpperCase();
		
		if (word_as_written.equals(".") || word_as_written.equals("?") || word_as_written.equals("!") || word_as_written.equals(";"))
		{
			wh.addHypothesis( new TaggedWord(word_as_written, word_as_written, new WordTags(WT.SENTENCE_END)) );
		}
		else if ( word_as_written.equals(",") )
		{
			wh.addHypothesis( new TaggedWord(word_as_written, word_as_written, new WordTags(WT.COMMA)) );			
		}
		else if ( Character.isDigit( word_as_written.charAt(0) ) )
		{
			// TODO: add better number parser
			wh.addHypothesis( new TaggedWord(word_as_written, word_as_written,
					new WordTags(WT.NUMERAL | WT.ANY_CASUS | WT.PLURAL | WT.ANY_GENDER)) );			
		}
		else if (lower_word.equals(upper_word))
		{
			// sequence without letters. Add as HELPWORD
			wh.addHypothesis( new TaggedWord(word_as_written, word_as_written,
					new WordTags(WT.HELPWORD)) );			
		}
		else if (word_as_written.charAt(0) == '\"' || word_as_written.charAt(0) == '«')
		{
			TaggedWord w = new TaggedWord(word_as_written, word_as_written, "");
			w.addTags(WT.NOUN | WT.PROPERNAME | WT.ANY_GENDER | WT.ANY_COUNT | WT.CASUS1 | WT.CASUS4);
			wh.addHypothesis(w);
		}
		else if (word_as_written.length()>1 &&
			upper_word.equals(word_as_written) &&
			Character.isLetter(word_as_written.charAt(0)) )
		{
			// It should be an Abbreviation. Check dictionary first
			if ( isInDictionary(word_as_written, try_error_corrections) )
			{
				addWordFormsFromDictionary(wh, word_as_written, word_as_written, try_error_corrections);
			}
			else
			{
				// add abbreviation as a new hypothesis
				wh.addHypothesis( new TaggedWord(word_as_written, word_as_written,
						new WordTags(WT.NOUN | WT.ANY_GENDER | WT.ANY_CASUS | WT.ANY_COUNT | WT.ABBREVIATED)) );
			}
		}
		else if ( Character.isUpperCase( word_as_written.charAt(0) ) )
		{
			if (word_index==0)
			{
				// this can be regular word or proper name
				// if it is a known proper name, add it
				// if it is a known word (lower case) also add it
				addWordFormsFromDictionary(wh, word_as_written, word_as_written, try_error_corrections);
				addWordFormsFromDictionary(wh, word_as_written, lower_word, try_error_corrections);
			}
			else
			{
				// words in the middle of sentence are added "as is"  
				addWordFormsFromDictionary(wh, word_as_written, word_as_written, try_error_corrections);				
			}
			 // try unknown proper name if no ideas
			if (wh.numHypotheses() == 0)
			{
				wh.addHypothesis( new TaggedWord(word_as_written, word_as_written,
						new WordTags(WT.NOUN | WT.ANY_CASUS | WT.ANY_GENDER | WT.ANY_COUNT | WT.PROPERNAME) ) );
			}
		}
		else
		{
			addWordFormsFromDictionary(wh, word_as_written, word_as_written, try_error_corrections);
		}
		
		if (wh.numHypotheses() == 0) // if no ideas, try compound words
		{
			int hyphen_ind = word_as_written.indexOf('-');
			if (hyphen_ind != -1)
			{
				String part1 = word_as_written.substring(0, hyphen_ind);
				String part2 = word_as_written.substring(hyphen_ind + 1);
				if (part2.startsWith("пре") && part1.equals(part2.substring(3))) // зелений-презелений
				{
					addWordFormsFromDictionary(wh, word_as_written, part1, false);
				}
				else	// like жовто-блакитний
				{
					addWordFormsFromDictionary(wh, word_as_written, part2, try_error_corrections);
				}
			}

			// no ideas at all. Add word with void tags
			if (wh.numHypotheses() == 0)
			{
				wh.addHypothesis( new TaggedWord(word_as_written, word_as_written, new WordTags() ) );
			}
		}
	}

	@Override
	public void setWordStatisticsCounter(WordStatisticsCounter wsc) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getWordStatisticalWeight(String word, String base_form) {
		// TODO Auto-generated method stub
		return 0;
	}

}
