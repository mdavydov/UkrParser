// MainApp for signtutor
// Created by Oleh Skotar (Lviv Polytechnic National Univerity)
// Edited by Maksym Davydov (Lviv Polytechnic National Univerity)
// This source code is made available under
// Creative Commons Attribution 4.0 International (CC BY 4.0) license

package com.signtutor;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

import org.dts.spell.dictionary.OpenOfficeSpellDictionary;

import com.langproc.Morphology;
import com.langproc.Sentence;
import com.langproc.UkrainianISpellMorphology;
import com.langproc.WordHypotheses;


public class MainApp extends Application
{

	private String path;
	private Stage primaryStage;
	private AnchorPane rootLayout;

	private ArrayList<String> videos;

	@Override
	public void start(Stage primaryStage)
	{
		this.primaryStage = primaryStage;
		this.primaryStage.setTitle("Sign Translator");
		initRootLayout();

		videos = new ArrayList<>();
	}

	static Morphology m_morphology;
	static OpenOfficeSpellDictionary m_dict;
	
	public String translate(String text)
	{
		videos.clear();

		StringBuilder result = new StringBuilder();
		String[] words = text.split(" ");
		String trword;
		String line;
		boolean found;
		
		
		try
		{
			Sentence ss = m_morphology.parseSentenceMorphemes(text);

			BufferedReader br;

			for (WordHypotheses word : ss)
			{
				FileInputStream fis = new FileInputStream("resources/data/words.txt");
				br = new BufferedReader(
						   new InputStreamReader(
				                      fis, "UTF8"));

				String to_search = word.getHypothesis(0).getBaseBaseForm();
				
				found = false;

				while ((line = br.readLine()) != null)
				{

					String[] trwords = line.split("[ \t]+");

					if (trwords.length >= 3
							&& to_search.equalsIgnoreCase(trwords[0]))
					{
						result.append(trwords[1] + " ");
						String video_path = "resources/video/"
								+ trwords[2].trim();
						videos.add(video_path);
						System.out.println(video_path
								+ " was added for playback");
						found = true;
					}
				}
				br.close();
				fis.close();

				if (!found)
				{
					String word_wr = word.getHypothesis(0).getWordAsWritten();

					System.out.println("Word " + word_wr
							+ " was not found in the dictionary");
					for (int i = 0; i < word_wr.length(); ++i)
					{
						String video_path = "resources/video/"
								+ java.lang.Character.toUpperCase(word_wr
										.charAt(i)) + ".mp4";
						result.append(word_wr.charAt(i));
						result.append(i + 1 == word_wr.length() ? ' ' : '-');
						videos.add(video_path);
						System.out.println(video_path
								+ " was added for playback");
					}
				}
			}

		}
		catch (IOException ioe)
		{
			System.err.println("Error reading words.txt" + ioe);
		}

		return result.toString();
	}

	public ArrayList<String> getVideo()
	{
		return videos;
	}

	public String senTranslate(String text)
	{
		StringBuilder result = new StringBuilder();
		// String[] words = text.split(" ");
		String trword;
		String line;
		boolean found;

		videos = new ArrayList<>();

		try
		{
			FileReader fr;
			BufferedReader br;

			fr = new FileReader("resources/data/Sentences.txt");
			br = new BufferedReader(fr);

			found = false;

			while ((line = br.readLine()) != null)
			{
				String[] trwords = line.split("-");

				if (text.equals(trwords[0]) || text.equals(trwords[1]))
				{
					result.append(trwords[1]);

					found = true;
				}
			}
			br.close();

			// if(!found) result.append(text + " ");

			String[] words = result.toString().split(" ");
			for (String word : words)
				videos.add(findVideoForWord(word));

		}
		catch (IOException ioe)
		{
			System.err.println("Reading words.txt" + ioe);
		}

		return result.toString();
	}

	public String findVideoForWord(String word)
	{
		String result = "";
		String trword;
		String line;
		boolean found;

		try
		{
			FileReader fr;
			BufferedReader br;

			fr = new FileReader("resources/data/VideoPath.txt");
			br = new BufferedReader(fr);

			found = false;

			while ((line = br.readLine()) != null)
			{
				String[] trwords = line.split(" ");

				if (word.equals(trwords[0]))
				{
					result = trwords[1];
					found = true;
				}
			}
			br.close();

		}
		catch (IOException ioe)
		{
			System.err.println("Reading words.txt" + ioe);
		}

		return result;

	}

	public String buildTree(String text)
	{
		return null;
	}

	public void initRootLayout()
	{
		try
		{
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(MainApp.class.getResource("TestOverview.fxml"));
			rootLayout = (AnchorPane) loader.load();

			Scene scene = new Scene(rootLayout);
			primaryStage.setScene(scene);
			primaryStage.show();

			Controller controller = loader.getController();
			controller.setMaindApp(this);
		}
		catch (IOException ioe)
		{
			System.err.println(ioe);
		}
	}

	public Stage getPrimaryStage()
	{
		return primaryStage;
	}

	public static void main(String[] args)
	{
		
		try
		{
			m_dict = new OpenOfficeSpellDictionary("uk_UA");
			m_morphology = new UkrainianISpellMorphology(m_dict);
		}
		catch(java.lang.Exception e) {}

		launch(args);
	}
}
