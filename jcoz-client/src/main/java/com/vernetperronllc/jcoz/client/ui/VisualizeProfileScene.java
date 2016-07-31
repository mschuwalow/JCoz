/*
 * NOTICE
 *
 * Copyright (c) 2016 David C Vernet and Matthew J Perron. All rights reserved.
 *
 * Unless otherwise noted, all of the material in this file is Copyright (c) 2016
 * by David C Vernet and Matthew J Perron. All rights reserved. No part of this file
 * may be reproduced, published, distributed, displayed, performed, copied,
 * stored, modified, transmitted or otherwise used or viewed by anyone other
 * than the authors (David C Vernet and Matthew J Perron),
 * for either public or private use.
 *
 * No part of this file may be modified, changed, exploited, or in any way
 * used for derivative works or offered for sale without the express
 * written permission of the authors.
 *
 * This file has been modified from lightweight-java-profiler
 * (https://github.com/dcapwell/lightweight-java-profiler). See APACHE_LICENSE for
 * a copy of the license that was included with that original work.
 */
package com.vernetperronllc.jcoz.client.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.vernetperronllc.jcoz.client.cli.TargetProcessInterface;
import com.vernetperronllc.jcoz.profile.Experiment;
import com.vernetperronllc.jcoz.profile.LineSpeedup;
import com.vernetperronllc.jcoz.profile.Profile;
import com.vernetperronllc.jcoz.service.JCozException;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class VisualizeProfileScene {

	private static String PROFILE_NAME = "com.vernetperronllc.jcoz.profile.coz";
	
	private static VisualizeProfileScene vpScene = null;

	private final GridPane grid = new GridPane();

	private final Scene scene;

	// Text elements
	private final Text processNameText = new Text();

	// Controls
	private final Button stopProfilingButton = new Button("Stop profiling");
	private final Button experimentsConsoleButton = new Button("Print experiments to console");

	// Visualization    
	private Timeline visualizationUpdateTimeline;
	int chartRow;
	
	// Profile
	Profile profile;

	TargetProcessInterface client;



	/** Disable constructor */
	private VisualizeProfileScene(final Stage stage) {
		// Set layout of grid
		this.grid.setHgap(10);
		this.grid.setVgap(10);
		this.grid.setPadding(new Insets(25, 25, 25, 25));

		final Text scenetitle = new Text("Profiling process");
		scenetitle.setFont(Font.font("Tahoma", FontWeight.NORMAL, 20));
		int currRow = 0;
		this.grid.add(scenetitle, 0, currRow++, 2, 1);

		/*** Text elements ***/
		final Label processNameLabel = new Label("Process name");
		this.grid.add(processNameLabel, 0, currRow);
		this.grid.add(this.processNameText, 1, currRow);
		currRow++;

		/*** Controls ***/
		this.experimentsConsoleButton.setOnAction(new EventHandler<ActionEvent>() { 
			@Override
			public void handle(ActionEvent event) {
				printExperimentsToConsole();
			}
		});
		this.grid.add(this.experimentsConsoleButton, 0, currRow++);
		this.stopProfilingButton.setTooltip(new Tooltip("End profiling and choose a new process"));
		this.stopProfilingButton.setOnAction(new EventHandler<ActionEvent>() { 
			@Override
			public void handle(ActionEvent event) {
				try {
					visualizationUpdateTimeline.stop();
					client.endProfiling();
				} catch (JCozException e) {
					System.err.println("Unable to end profiling");
					e.printStackTrace();
				}
				stage.setScene(PickProcessScene.getPickProcessScene(stage));
			}
		});
		this.grid.add(this.stopProfilingButton, 0, currRow);
		currRow++;

		/*** VISUALIZATION ***/
		currRow = this.setUpVisualizationSection(currRow);
		this.chartRow = currRow;

		this.scene = new Scene(this.grid, 980, 600);
	}
	
	/**
	 * Set up the chart /visualization section of the VisualizeProfileScene
	 * @param currRow The row where the chart should be placed.
	 * @return Next available row after setting scene section.
	 */
	private int setUpVisualizationSection(int currRow) {
		visualizationUpdateTimeline = new Timeline(new KeyFrame(
				Duration.millis(5000),
				new EventHandler<ActionEvent>() { 
					@Override
					public void handle(ActionEvent event) {
						updateGraphVisualization();
					}
				}));
		visualizationUpdateTimeline.setCycleCount(Animation.INDEFINITE);
				
		return currRow;
	}

	public Scene getScene() {
		return this.scene;
	}

	private void setClient(TargetProcessInterface client, String processName) {
		this.profile = new Profile(processName);
		this.processNameText.setText(profile.getProcess());
		this.client = client;
		
		this.grid.add(this.profile.getLineChart(), 0, this.chartRow, 10, 10);
	}

	/**
	 * Update the currently displayed graph visualization.
	 * This is currently called from a timer task in the
	 * scene constructor.
	 */
	private synchronized void updateGraphVisualization() {
		try {
			List<Experiment> experiments = client.getProfilerOutput();
			this.profile.addExperiments(experiments);
			this.profile.renderLineSpeedups();
		} catch (JCozException e) {
			System.err.println("Unable to get profiler experiment outputs");
			e.printStackTrace();
			return;
		}
	}

	/**
	 * Helper function for debugging. Prints all current experiments to the console.
	 */
	private synchronized void printExperimentsToConsole() {
		List<Experiment> experiments = this.profile.getExperiments();
		System.out.println("Printing " + experiments.size() + " experiments...");
		for (Experiment exp : experiments) {
			System.out.println(exp);
		}
	}

	public static Scene getVisualizeProfileScene(
			TargetProcessInterface client, Stage stage, String processName) {
		if (VisualizeProfileScene.vpScene == null) {
			VisualizeProfileScene.vpScene = new VisualizeProfileScene(stage);
		}
		vpScene.setClient(client, processName);
		vpScene.visualizationUpdateTimeline.play();
		return VisualizeProfileScene.vpScene.getScene();
	}
}

