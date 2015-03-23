package com.langproc;

public interface Morphology
{
	public Sentence parseSentenceMorphemes(String txt);
	public void setWordStatisticsCounter(WordStatisticsCounter wsc);
	public int getWordStatisticalWeight(String word, String base_form);
}
