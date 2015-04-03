package com.langproc;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

public class SentenceFileReader
{
	private java.io.BufferedReader m_reader;
	StringBuffer m_full_text = new StringBuffer();

	
	public SentenceFileReader(String filename, String encoding)
			throws java.io.FileNotFoundException, UnsupportedEncodingException
	{
		java.io.InputStream ips = new java.io.FileInputStream(filename);
		java.io.InputStreamReader ipsr = new java.io.InputStreamReader(ips, encoding);
		m_reader = new java.io.BufferedReader(ipsr);
	}
	public void close() throws IOException
	{
		m_reader.close();
		m_reader=null;
	}
	public String readSentence() throws IOException
	{
		if (m_reader==null) return null;
		
		for (int i = 0; true; ++i)
		{
			if (i >= m_full_text.length())
			{
				String line = m_reader.readLine();
				if (line==null) return null;
				m_full_text.append(line).append(" ");
			}
			
			char c = m_full_text.charAt(i);
			if ((int) c == 8217) m_full_text.setCharAt(i, '\'');
			if ((int) c == '’') m_full_text.setCharAt(i, '\'');

			if (c == '.' || c == '!' || c == '?' || c == ';')
			{
				String substr = m_full_text.substring(0, i + 1);
				m_full_text.delete(0, i + 1);
				return substr;
			}
		}
	}
}
