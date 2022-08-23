/*
 * This file is part of the Deterministic Network Calculator (DNC).
 *
 * Copyright (C) 2011 - 2018 Steffen Bondorf
 * Copyright (C) 2017 - 2018 The DiscoDNC contributors
 * Copyright (C) 2019+ The DNC contributors
 *
 * http://networkcalculus.org
 *
 *
 * The Deterministic Network Calculator (DNC) is free software;
 * you can redistribute it and/or modify it under the terms of the 
 * GNU Lesser General Public License as published by the Free Software Foundation; 
 * either version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 */

package org.networkcalculus.dnc.ethernet.utils;

import java.io.IOException;
import java.security.InvalidParameterException;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.QuickChart;
import org.knowm.xchart.SwingWrapper;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.networkcalculus.dnc.curves.Curve;
import org.networkcalculus.dnc.curves.LinearSegment;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 *
 */
public class ChartUtilNCCurve {

	private static final int WIDTH = 1280;
	private static final int HEIGHT = 720;

	public ChartUtilNCCurve() {
	}

	// TODO: Can this plot curves with discontinuities like staircase functions?
	public void plotCurve(Curve c, String name){

		int seg_count = c.getSegmentCount();

		double[] xData = new double[seg_count+1];
		double[] yData = new double[seg_count+1];

		// How much of the last segment should be shown?
		// First attempt: minimum of
		// - as long as the longest segment or
		// - somewhere in the order of an affine curve's rate

		LinearSegment seg = c.getSegment(0);
		xData[0] = seg.getX().doubleValue();
		yData[0] = seg.getY().doubleValue();

		// The first segment starts in x=0, we do not know its length yet
		double prev_x = 0, max_seg_length = 0;
		for( int i = 1; i < c.getSegmentCount(); i++ ) {
			seg = c.getSegment(i);
			xData[i] = seg.getX().doubleValue();
			yData[i] = seg.getY().doubleValue();

			if( xData[i] - prev_x > max_seg_length ) {
				max_seg_length = xData[i] - prev_x;
			}
			prev_x = seg.getX().doubleValue();
		}

		// only one segment and potentially a spot in the origin
		if( prev_x == 0 ) {
			max_seg_length = Math.max(seg.getY().doubleValue(), seg.getGrad().doubleValue());        			
		}

		xData[seg_count] = max_seg_length * 1.5; //some random scaling by 1.5
		yData[seg_count] = seg.getY().doubleValue() + (max_seg_length * seg.getGrad().doubleValue()); 

		// Create Chart
		XYChart chart = QuickChart.getChart(name, "time (s)", "data (b)", "f(x)", xData, yData);

		// Show it
		new SwingWrapper<XYChart>(chart).displayChart();

	}

	public void saveCurve(Curve c, String name, String filepath) throws IOException {

		int seg_count = c.getSegmentCount();

		double[] xData = new double[seg_count+1];
		double[] yData = new double[seg_count+1];

		// How much of the last segment should be shown?
		// First attempt: minimum of
		// - as long as the longest segment or
		// - somewhere in the order of an affine curve's rate

		LinearSegment seg = c.getSegment(0);
		xData[0] = seg.getX().doubleValue();
		yData[0] = seg.getY().doubleValue();

		// The first segment starts in x=0, we do not know its length yet
		double prev_x = 0, max_seg_length = 0;
		for( int i = 1; i < c.getSegmentCount(); i++ ) {
			seg = c.getSegment(i);
			xData[i] = seg.getX().doubleValue();
			yData[i] = seg.getY().doubleValue();

			if( xData[i] - prev_x > max_seg_length ) {
				max_seg_length = xData[i] - prev_x;
			}
			prev_x = seg.getX().doubleValue();
		}

		// only one segment and potentially a spot in the origin
		if( prev_x == 0 ) {
			max_seg_length = Math.max(seg.getY().doubleValue(), seg.getGrad().doubleValue());        			
		}

		xData[seg_count] = max_seg_length * 1.5; //some random scaling by 1.5
		yData[seg_count] = seg.getY().doubleValue() + (max_seg_length * seg.getGrad().doubleValue()); 

		// Create Chart
		XYChart chart = QuickChart.getChart(name, "time (s)", "data (b)", "f(x)", xData, yData);

		BitmapEncoder.saveJPGWithQuality(chart, filepath + ".jpg", 1.0f);

	}

	public void saveCurve(Curve c, String name, String curveName, String filepath) throws IOException {

		int seg_count = c.getSegmentCount();

		double[] xData = new double[seg_count+1];
		double[] yData = new double[seg_count+1];

		// How much of the last segment should be shown?
		// First attempt: minimum of
		// - as long as the longest segment or
		// - somewhere in the order of an affine curve's rate

		LinearSegment seg = c.getSegment(0);
		xData[0] = seg.getX().doubleValue();
		yData[0] = seg.getY().doubleValue();

		// The first segment starts in x=0, we do not know its length yet
		double prev_x = 0, max_seg_length = 0;
		for( int i = 1; i < c.getSegmentCount(); i++ ) {
			seg = c.getSegment(i);
			xData[i] = seg.getX().doubleValue();
			yData[i] = seg.getY().doubleValue();

			if( xData[i] - prev_x > max_seg_length ) {
				max_seg_length = xData[i] - prev_x;
			}
			prev_x = seg.getX().doubleValue();
		}

		// only one segment and potentially a spot in the origin
		if( prev_x == 0 ) {
			max_seg_length = Math.max(seg.getY().doubleValue(), seg.getGrad().doubleValue());        			
		}

		xData[seg_count] = max_seg_length * 1.5; //some random scaling by 1.5
		yData[seg_count] = seg.getY().doubleValue() + (max_seg_length * seg.getGrad().doubleValue()); 

		// Create Chart
		XYChart chart = QuickChart.getChart(name, "time (s)", "data (b)", curveName, xData, yData);

		BitmapEncoder.saveJPGWithQuality(chart, filepath + ".jpg", 1.0f);

	}

	// TODO: Can this plot curves with discontinuities like staircase functions?
	public void plotCurve(Curve[] curves, String[] seriesNames, String chartTitle){

		if(curves.length != seriesNames.length)
			throw new InvalidParameterException("lengths from curves and seriesNames does not match");

		double[][] xDataAll = new double[curves.length][];
		double[][] yDataAll = new double[curves.length][];

		for(int j = 0 ; j < curves.length ; j++) {

			Curve c = curves[j];

			int seg_count = c.getSegmentCount();

			double[] xData = new double[seg_count+1];
			double[] yData = new double[seg_count+1];

			// How much of the last segment should be shown?
			// First attempt: minimum of
			// - as long as the longest segment or
			// - somewhere in the order of an affine curve's rate

			LinearSegment seg = c.getSegment(0);
			xData[0] = seg.getX().doubleValue();
			yData[0] = seg.getY().doubleValue();

			// The first segment starts in x=0, we do not know its length yet
			double prev_x = 0, max_seg_length = 0;
			for( int i = 1; i < c.getSegmentCount(); i++ ) {
				seg = c.getSegment(i);
				xData[i] = seg.getX().doubleValue();
				yData[i] = seg.getY().doubleValue();

				if(xData[i] - prev_x > max_seg_length ) {
					max_seg_length = xData[i] - prev_x;
				}
				prev_x = seg.getX().doubleValue();
			}

			// only one segment and potentially a spot in the origin
			if( prev_x == 0 ) {
				max_seg_length = Math.max(seg.getY().doubleValue(), seg.getGrad().doubleValue());        			
			}

			xData[seg_count] = max_seg_length * 1.5; //some random scaling by 1.5
			yData[seg_count] = seg.getY().doubleValue() + (max_seg_length * seg.getGrad().doubleValue()); 

			xDataAll[j] = xData;
			yDataAll[j] = yData;
		}

		XYChart chart = buildChart(chartTitle, "time (seconds)", "data (bits)", seriesNames, xDataAll, yDataAll);

		// Show it
		new SwingWrapper<XYChart>(chart).displayChart();
	}


	private static XYChart buildChart(String chartTitle, String xTitle, String yTitle, String[] seriesNames, double[][] xData, double[][] yData) {

		// Create Chart
		XYChart chart = new XYChart(WIDTH, HEIGHT);

		// Customize Chart
		chart.setTitle(chartTitle);
		chart.setXAxisTitle(xTitle);
		chart.setYAxisTitle(yTitle);

		// Series
		for (int i = 0; i < yData.length; i++) {
			XYSeries series;
			if (seriesNames != null) {
				series = chart.addSeries(seriesNames[i], xData[i], yData[i]);
			} else {
				chart.getStyler().setLegendVisible(false);
				series = chart.addSeries(" " + i, xData[i], yData[i]);
			}
			series.setMarker(SeriesMarkers.NONE);
		}
		return chart;
	}

}