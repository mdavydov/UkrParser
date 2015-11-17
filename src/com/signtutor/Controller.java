// MainApp for signtutor
// Created by Oleh Skotar (Lviv Polytechnic National Univerity)
// Edited by Maksym Davydov (Lviv Polytechnic National Univerity)
// This source code is made available under
// Creative Commons Attribution 4.0 International (CC BY 4.0) license

package com.signtutor;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaPlayer.Status;
import javafx.scene.media.MediaView;

public class Controller {

	@FXML
	private TextArea txtRaw;
	@FXML
	private TextArea txtTranslated;
	@FXML
	private MediaView video;

	private MediaPlayer mediaPlayer;

	private MainApp mainApp;

	public Controller() {}

	@FXML
	private void initialize() {

	}

	public void setMaindApp(MainApp mainApp) {
		this.mainApp = mainApp;
	}

	@FXML
	private void handleTranslate() {
		//Translate the text from one text area to another;
		txtTranslated.setText(mainApp.translate(txtRaw.getText()));
	}

	@FXML
	private void handleSenTranslate() {
		txtTranslated.setText(mainApp.senTranslate(txtRaw.getText()));
		try {
			handleShow();
		} catch (InterruptedException e) {

			e.printStackTrace();
		}
	}
	@FXML
	public void handleShow() throws InterruptedException
	{
		List<String> videos = mainApp.getVideo();
		//for(String video : videos)
			//{initPlayer(video);}
		initPlayer(videos,0);
	}
	@FXML
	public void handleTree()
	{
		mainApp.buildTree(txtRaw.getText());
	}



	public void initPlayer(final List<String> videos, final int current) throws InterruptedException
	{
		if (videos.size()==0) return;
		
		boolean prVFinished = false;
		//if(videos.equals("")) return;
		System.out.println(videos);
			if(mediaPlayer != null) {

			//mediaPlayer.stop();
			//mediaPlayer = null;
			}

		Media media = new Media(new File(videos.get(current)).toURI().toString());
			mediaPlayer = new MediaPlayer(media);

		mediaPlayer.setAutoPlay(true);

		mediaPlayer.setOnEndOfMedia(new Runnable() {
			@Override
			public void run() {
				try {
					if(videos.size()-1 != current)
					initPlayer(videos,current+1);
					else return;
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}


		});
		video.setMediaPlayer(mediaPlayer);
	}


	}
