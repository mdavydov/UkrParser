package com.langproc;

public class UkrainianGrammarlyMorphology
{
	UkrainianGrammarlyMorphology()
	{
		try
		{
			LangProcOutput.println("Reading file");
			java.io.InputStream ips = new java.io.FileInputStream("tagged.main.txt");
			java.io.InputStreamReader ipsr = new java.io.InputStreamReader(ips, "UTF-8");
			java.io.BufferedReader reader = new java.io.BufferedReader(ipsr);

			StringBuffer full_text = new StringBuffer();
			String line = null;

			int sentence_n = 0;
			int num_all = 0;

			while ((line = reader.readLine()) != null && num_all < 10)
			{
				System.out.println(line);
				++num_all;
			}
			ips.close();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
}
