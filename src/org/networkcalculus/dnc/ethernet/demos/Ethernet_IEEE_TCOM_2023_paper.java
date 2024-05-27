package org.networkcalculus.dnc.ethernet.demos;

import java.util.List;

import org.networkcalculus.dnc.AnalysisConfig;
import org.networkcalculus.dnc.ethernet.EthernetNetwork;
import org.networkcalculus.dnc.ethernet.tsn.ExecutionConfig;
import org.networkcalculus.dnc.ethernet.tsn.TASWindowsBuilder;
import org.networkcalculus.dnc.ethernet.tsn.data.DatasetReader;
import org.networkcalculus.dnc.ethernet.tsn.results.AnalysesResultPaperAccess2018;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 * 
 */
public class Ethernet_IEEE_TCOM_2023_paper {

	public static void main(String[] args) throws Exception {

		// Take time before execute all analysis to calculate the execution time
		long timeIniMs = System.currentTimeMillis();

		final ExecutionConfig executionConfig = new ExecutionConfig(
				/**
				 * Object used to store all analysis results and compare them to baseline work.
				 */
				new AnalysesResultPaperAccess2018(),

				/**
				 * Generate Excel sheets with all computed attribute values of TASWindow.
				 * 
				 * Available options:
				 * 		NO,
				 * 		ALL
				 */
				ExecutionConfig.GenerateTASWindowsExcelSheet.ALL,

				/**
				 * Generate TASWindow Charts.
				 * 
				 * Available options:
				 * 		NO,
				 * 		GENERATE_INTERFACES_CHARTS_ONLY,
				 * 		GENERATE_WINDOW_COLLISIONS_CHARTS_ONLY,
				 * 		ALL
				 */
				ExecutionConfig.GenerateTASWindowCharts.ALL,
				
				/**
				 * Generate Network Calculus Curves Charts (Arrival and Service Curves).
				 * 
				 * Available options:
				 * 		NO,
				 * 		GENERATE_ARRIVAL_CURVES_CHARTS_ONLY,
				 * 		GENERATE_SERVICE_CURVES_CHARTS_ONLY,
				 * 		ALL
				 */
				ExecutionConfig.GenerateNCCurvesCharts.ALL, 
				
				/**
				 * Generate PortGuaranteedWindows files. It is used to compare the computed values with those disclosed by Luxi Zhao to us.
				 * 
				 * Available options:
				 * 		NO,
				 * 		ALL 	
				 */
				ExecutionConfig.GeneratePortGuaranteedWindowsFiles.NO,
				
				/**
				 * Option to choose the prioritizing order.
				 * 
				 * Available options:
				 * 		LOWER_VALUE_HIGHER_PRIORITY: (As done by Luxi Zhao et al.)
				 * 		HIGHER_VALUE_HIGHER_PRIORITY: (As declared in the IEEE 802.1Q)
				 */
				ExecutionConfig.PrioritizingOrder.LOWER_VALUE_HIGHER_PRIORITY,
				
				/**
				 * Choose the PLCA modeling.
				 * 
				 * Available options:
				 * 		SINGLE_PLCA_SERVER_MODELING,
				 * 		SEPARATED_PLCA_SERVER_MODELING,
				 */
				ExecutionConfig.PLCAModeling.SINGLE_PLCA_SERVER_MODELING,
				
				/**
				 * Check if ST frame length fits within TAS windows in each Ethernet interface along their whole frame's path.
				 * 
				 * Available options:
				 * 		NO,
				 * 		YES_PRINT_IF_INVALID,
				 * 		YES_THROW_ERROR_IF_INVAID
				 */
				ExecutionConfig.ValidateSchedulingForFrameSize.YES_PRINT_IF_INVALID,
				
				/**
				 * Save the generated ServerGraph into DNC tool to compute bounds.
				 * 
				 * Available options:
				 * 		NO,
				 * 		YES
				 */
				ExecutionConfig.SaveServerGraph.YES,
				
				/**
				 * Choose Network Calculus multiplexing method on analysis.
				 * 
				 * Available options:
				 * 		ARBITRARY,
				 * 		FIFO
				 */
				AnalysisConfig.Multiplexing.FIFO
				);

		/**
		 * Directories with the dataset files
		 */
		final String[] datasetRootPaths = {
				"dataset/synthetic test cases (Access 2018 Luxi)_mod_DavidAlain_1000BASE-TX",
				"dataset/synthetic test cases (Access 2018 Luxi)_mod_DavidAlain_1000BASE-T1S",
		};

		for (final String datasetRootPath : datasetRootPaths) {

			final List<String> pathList = DatasetReader.listDatasetFilesPaths(datasetRootPath);

			for (String path : pathList) {

				try {

					System.out.println("Dataset case path: " + path);

					/**
					 * Read dataset files and build all required objects 
					 */
					final DatasetReader datasetReader = new DatasetReader(path);
					final TASWindowsBuilder networkConfigBuilder = new TASWindowsBuilder(datasetReader, executionConfig);
					final EthernetNetwork ethernetNetwork = new EthernetNetwork(networkConfigBuilder);

					// Run analysis, store results in properly object in ExecutionConfig object, and print results
					ethernetNetwork.performAnalysis();

					//Save curves accordingly to chosen options in ExecutionConfig object
					ethernetNetwork.saveCurves();

				} catch (Exception e) {
					Thread.sleep(1000);
					e.printStackTrace();
					Thread.sleep(1000);
				}

			}

		}

		// Calculate the execution time
		long timeEndMs = System.currentTimeMillis();
		System.out.println("Required time to compute bounds: "
				+ (timeEndMs - timeIniMs) / 1000.0 + " seconds "
				+ "(= " + ((timeEndMs - timeIniMs) / 1000.0 / 60.0) + " minutes)");

		if (executionConfig.analysesResult != null) {
			
			//Build Excel sheet with all analyses results
			executionConfig.analysesResult.buildSheetWriteXLSXFile();
			
			//Build CSV files for plotting bar and heatmap graphs used in the paper
			executionConfig.analysesResult.buildSheetWriteCSVFileForSeabornWithReplacementMap();
		}

	}

}
