package com.langproc;

public interface Grammar
{
	public abstract String processSentence(Morphology morphology, Sentence s, boolean use_word_weighting);
}
