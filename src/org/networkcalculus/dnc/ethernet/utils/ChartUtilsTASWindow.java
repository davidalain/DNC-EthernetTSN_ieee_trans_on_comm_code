package org.networkcalculus.dnc.ethernet.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.knowm.xchart.BitmapEncoder;
import org.knowm.xchart.XYChart;
import org.knowm.xchart.XYSeries;
import org.knowm.xchart.style.markers.SeriesMarkers;
import org.networkcalculus.dnc.ethernet.tsn.ExecutionConfig;
import org.networkcalculus.dnc.ethernet.tsn.entry.InterfaceInfoEntry;
import org.networkcalculus.dnc.ethernet.tsn.model.TASWindow;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 *
 */
public class ChartUtilsTASWindow {

	//	private static final int TOTAL_HEIGHT = 10;
	//	private static final int HIGH_LEVEL_HEIGHT = 9;
	//	private static final int LOW_LEVEL_HEIGHT = 1;
	//	private static final int CURVE_HEIGHT = HIGH_LEVEL_HEIGHT - LOW_LEVEL_HEIGHT;

	private static final double TOTAL_HEIGHT = 1.0;
	private static final double HIGH_LEVEL_HEIGHT_TAS_WINDOW = 0.95;
	private static final double LOW_LEVEL_HEIGHT_TAS_WINDOW = 0.001;

	//	private static final double HIGH_LEVEL_HEIGHT_TAS_GUARANTEED_WINDOW = 0.94;
	private static final double LOW_LEVEL_HEIGHT_TAS_GUARANTEED_WINDOW = LOW_LEVEL_HEIGHT_TAS_WINDOW - 0.002;

	private static final double CURVE_HEIGHT = HIGH_LEVEL_HEIGHT_TAS_WINDOW - LOW_LEVEL_HEIGHT_TAS_WINDOW;

	private static final double STAIR_STEP_LENGTH = 1e-10;

	//	public static void saveTASWindowsChart(String filepath, String name, Map<InterfaceInfo,Map<Integer,List<TASWindow>>> map, ExecutionConfig.PrioritizingOrder prioritizingOrder) throws IOException {
	//
	//		int arraySize = 0;
	//		int arrayIndex = 0;
	//
	//		for(Entry<InterfaceInfo,Map<Integer,List<TASWindow>>> entry : map.entrySet()) {
	//			final Map<Integer, List<TASWindow>> m = entry.getValue();
	//
	//			if(m != null && m.values() != null)
	//				arraySize += m.values().size();
	//		}
	//
	//		final double[][] xDataAll = new double[2 * arraySize][];
	//		final double[][] yDataAll = new double[2 * arraySize][];
	//		final String[] names = new String[2 * arraySize];
	//
	//		final List<InterfaceInfo> interfaces = new LinkedList<>(map.keySet());
	//
	//		//Sort by name
	//		interfaces.sort((InterfaceInfo o1, InterfaceInfo o2) -> o1.toStringInfo().compareTo(o2.toStringInfo()));
	//
	//		for(InterfaceInfo interfaceInfo : interfaces) {
	//
	//			final Map<Integer,List<TASWindow>> mapPriorityListTASWindow = map.get(interfaceInfo);
	//
	//			if(mapPriorityListTASWindow == null)
	//				continue;
	//
	//			final List<Integer> priorities = new LinkedList<>(mapPriorityListTASWindow.keySet());
	//
	//			switch (prioritizingOrder) {
	//			case LOWER_VALUE_HIGHER_PRIORITY: {
	//				//Sort ascending by priority
	//				priorities.sort((Integer o1, Integer o2) -> Integer.compare(o1, o2));
	//				break;
	//			}
	//			case HIGHER_VALUE_HIGHER_PRIORITY: {
	//				//Sort descending by priority
	//				priorities.sort((Integer o1, Integer o2) -> -Integer.compare(o1, o2));
	//				break;
	//			}
	//			default:
	//				throw new IllegalArgumentException("Unexpected value: " + prioritizingOrder);
	//			}
	//
	//			for(Integer priority : priorities) {
	//
	//				final List<TASWindow> tasWindowList = mapPriorityListTASWindow.get(priority);
	//
	//				for(TASWindow tasWindow : tasWindowList) {
	//					putDataWindow(tasWindow, arrayIndex, interfaceInfo, xDataAll, yDataAll, names);
	//					arrayIndex++;
	//
	//					putDataGuaranteedWindow(tasWindow, arrayIndex, interfaceInfo, xDataAll, yDataAll, names);
	//					arrayIndex++;
	//				}
	//
	//			}
	//
	//		}
	//
	//		final XYChart chart = buildChart(
	//				"Time-Aware Shaper - Priority Queues Window Scheduling" + (name!=null?" ("+name+")":""), 
	//				"time (us)", 
	//				"Priority Queues Gate States", 
	//				names, xDataAll, yDataAll);
	//
	//		// or save it in high-res
	////		BitmapEncoder.saveJPGWithQuality(chart, filepath, 1.0f);
	//		BitmapEncoder.saveBitmapWithDPI(chart, filepath, BitmapFormat.PNG, 300);
	//	}

	public static void saveTASWindowsChart(String filepath, String name, InterfaceInfoEntry interfaceInfo, Map<Integer,List<TASWindow>> map, ExecutionConfig.PrioritizingOrder prioritizing) throws IOException {

		int arraySize = 0;
		int arrayIndex = 0;
		long T_GCL = map.entrySet().iterator().next().getValue().get(0).T_GCL; //All T_GCL values for TAS Windows are equals for a single interface.

		for(Entry<Integer,List<TASWindow>> entry : map.entrySet()) {
			arraySize += entry.getValue().size();
		}

		final double[][] xDataAll = new double[2 * arraySize][];
		final double[][] yDataAll = new double[2 * arraySize][];
		final String[] names = new String[2 * arraySize];

		final List<Integer> priorities = new LinkedList<>(map.keySet());

		final int orderingFactor;

		switch (prioritizing) {
		case LOWER_VALUE_HIGHER_PRIORITY: { 	orderingFactor = 1; 	break; }	//Sort ascending by priority
		case HIGHER_VALUE_HIGHER_PRIORITY: {	orderingFactor = -1;	break; }	//Sort descending by priority
		default:
			throw new IllegalArgumentException("Unexpected value: " + prioritizing);
		}

		priorities.sort((Integer o1, Integer o2) -> orderingFactor * Integer.compare(o1, o2));

		for(Integer priority : priorities) {

			final List<TASWindow> tasWindowList = map.get(priority);

			for(TASWindow tasWindow : tasWindowList) {
				putDataWindow(tasWindow, arrayIndex, interfaceInfo, xDataAll, yDataAll, names, prioritizing);
				arrayIndex++;

				putDataGuaranteedWindow(tasWindow, arrayIndex, interfaceInfo, xDataAll, yDataAll, names, prioritizing);
				arrayIndex++;
			}

		}

		final XYChart chart = buildChart(
				"Time-Aware Shaper - Priority Queues Window Scheduling" + (name!=null?" ("+name+")":""), 
				"time (us)", 
				"Priority Queues Gate States", 
				names, xDataAll, yDataAll,
				T_GCL);

		// or save it in high-res
		BitmapEncoder.saveJPGWithQuality(chart, filepath + ".jpg", 1.0f);
		//BitmapEncoder.saveBitmapWithDPI(chart, filepath + ".png", BitmapFormat.PNG, 300);
	}

	private static void putDataGuaranteedWindow(TASWindow tasWindow, int arrayIndex, InterfaceInfoEntry interfaceInfo, final double[][] xDataAll, final double[][] yDataAll, final String[] names, ExecutionConfig.PrioritizingOrder prioritizing) {

		double[] xData = new double[6];
		double[] yData = new double[6];

		final int orderingFactor;

		switch (prioritizing) {
		case LOWER_VALUE_HIGHER_PRIORITY: { 	orderingFactor = -1; 	break; }
		case HIGHER_VALUE_HIGHER_PRIORITY: {	orderingFactor = 1;		break; }
		default:
			throw new IllegalArgumentException("Unexpected value: " + prioritizing);
		}

		final double baseLevel = orderingFactor * (((tasWindow.priorityPm) * TOTAL_HEIGHT)) + LOW_LEVEL_HEIGHT_TAS_GUARANTEED_WINDOW;

		xData[0] = tasWindow.index * tasWindow.T_Pm;
		xData[1] = tasWindow.t_Bi_Pm - STAIR_STEP_LENGTH;
		xData[2] = tasWindow.t_Bi_Pm;
		xData[3] = tasWindow.t_Ei_Pm;
		xData[4] = tasWindow.t_Ei_Pm + STAIR_STEP_LENGTH;
		xData[5] = ((tasWindow.index + 1) * tasWindow.T_Pm);

		yData[0] = baseLevel;
		yData[1] = baseLevel;
		yData[2] = baseLevel + CURVE_HEIGHT;
		yData[3] = baseLevel + CURVE_HEIGHT;	
		yData[4] = baseLevel;
		yData[5] = baseLevel;

		names[arrayIndex] = interfaceInfo.toStringInfoEth() + 
				" (P_m="+tasWindow.priorityPm +", i="+tasWindow.index+", \\overline{L}^i_{P_m}="+tasWindow.L_bar_i_Pm+", T_{P_m}="+tasWindow.T_Pm+")    .";

		xDataAll[arrayIndex] = xData;
		yDataAll[arrayIndex] = yData;

	}

	private static void putDataWindow(TASWindow tasWindow, int arrayIndex, InterfaceInfoEntry interfaceInfo, final double[][] xDataAll, final double[][] yDataAll, final String[] names, ExecutionConfig.PrioritizingOrder prioritizing) {

		double[] xData = new double[6];
		double[] yData = new double[6];

		final int orderingFactor;

		switch (prioritizing) {
		case LOWER_VALUE_HIGHER_PRIORITY: { 	orderingFactor = -1; 	break; }
		case HIGHER_VALUE_HIGHER_PRIORITY: {	orderingFactor = 1;		break; }
		default:
			throw new IllegalArgumentException("Unexpected value: " + prioritizing);
		}

		final double baseLevel = orderingFactor * (((tasWindow.priorityPm) * TOTAL_HEIGHT)) + LOW_LEVEL_HEIGHT_TAS_GUARANTEED_WINDOW;

		xData[0] = tasWindow.index * tasWindow.T_Pm;
		xData[1] = tasWindow.t_oi_Pm - STAIR_STEP_LENGTH;
		xData[2] = tasWindow.t_oi_Pm;
		xData[3] = tasWindow.t_ci_Pm;
		xData[4] = tasWindow.t_ci_Pm + STAIR_STEP_LENGTH;
		xData[5] = ((tasWindow.index + 1) * tasWindow.T_Pm);

		yData[0] = baseLevel;
		yData[1] = baseLevel;
		yData[2] = baseLevel + CURVE_HEIGHT;
		yData[3] = baseLevel + CURVE_HEIGHT;	
		yData[4] = baseLevel;
		yData[5] = baseLevel;

		names[arrayIndex] = interfaceInfo.toStringInfoEth() + 
				" (P_m="+tasWindow.priorityPm +", i="+tasWindow.index+", L^i_{P_m}="+(tasWindow.t_ci_Pm - tasWindow.t_oi_Pm)+", T_{P_m}="+tasWindow.T_Pm+")    .";

		xDataAll[arrayIndex] = xData;
		yDataAll[arrayIndex] = yData;

	}

	private static XYChart buildChart(String chartTitle, String xTitle, String yTitle, String[] seriesNames, double[][] xData, double[][] yData, long T_GCL) {

		//XYChart chart = new XYChart(1280, 720);
		//XYChart chart = new XYChart(1920, 1080);
		final double widthRef = 1920.0 * 2.0; 	//pixels
		final double heightRef = 100.0; 			//pixels
		final double T_GCL_Ref = 250.0;

		final int width = (int)((T_GCL * widthRef)/T_GCL_Ref);
		final int height = (int)(heightRef * seriesNames.length);

		// Create Chart
		final XYChart chart = new XYChart(width, height);

		// Customize Chart
		chart.setTitle(chartTitle);
		chart.setXAxisTitle(xTitle);
		chart.setYAxisTitle(yTitle);
		chart.setInfoContent(Arrays.asList(seriesNames));

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
