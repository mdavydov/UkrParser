package com.langproc;

public interface Morphology
{
	public Sentence parseSentenceMorphemes(String txt);
	
	// check whether the word is in dictionary as it is written (i.e. same case) 
	public boolean isInDictionary(String word, boolean correct_errors);
	public void addWordFormsFromDictionary(WordHypotheses wh, String word_as_written, String word_to_search, boolean correct_errors);
	
	public void setWordStatisticsCounter(WordStatisticsCounter wsc);
	public int getWordStatisticalWeight(String word, String base_form);
}
