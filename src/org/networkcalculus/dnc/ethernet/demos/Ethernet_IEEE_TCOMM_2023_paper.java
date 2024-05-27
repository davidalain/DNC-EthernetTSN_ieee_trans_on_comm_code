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
public class Ethernet_IEEE_TransOnComm_2022_paper {

	public static void main(String[] args) throws Exception {

		long timeIniMs = System.currentTimeMillis();

		/****************************************************************************************************
		 * 
		 * Read DataSet and build all required maps
		 * 
		 ****************************************************************************************************/

		final ExecutionConfig executionConfig = new ExecutionConfig(
				new AnalysesResultPaperAccess2018(),
				ExecutionConfig.GenerateTASWindowsExcelSheet.ALL,									//generateTASWindowsExcelSheet
				ExecutionConfig.GenerateTASWindowCharts.ALL,										//generateTASWindowsCharts
				ExecutionConfig.GenerateNCCurvesCharts.ALL,											//generateNCCurvesCharts
				ExecutionConfig.GeneratePortGuaranteedWindowsFiles.NO,								//generatePortGuaranteedWinFiles
				ExecutionConfig.PrioritizingOrder.LOWER_VALUE_HIGHER_PRIORITY,						//prioritizingOrder
				ExecutionConfig.PLCAModeling.SINGLE_PLCA_SERVER_MODELING,							//usePlcaServer
				ExecutionConfig.ValidateSchedulingForFrameSize.YES_PRINT_IF_INVALID,				//validateSchedulingForFrameSize
				ExecutionConfig.SaveServerGraph.YES,												//saveServerGraph
				AnalysisConfig.Multiplexing.FIFO													//multiplexing
				);

		final String[] datasetRootPaths = {
				"dataset/synthetic test cases (Access 2018 Luxi)_mod_DavidAlain_1000BASE-TX",
				"dataset/synthetic test cases (Access 2018 Luxi)_mod_DavidAlain_1000BASE-T1S",
		};

		for(final String datasetRootPath : datasetRootPaths) {

			final List<String> pathList = DatasetReader.listDatasetFilesPaths(datasetRootPath);

			//final List<String> pathList = new LinkedList<String>();
			//pathList.add("dataset\\synthetic test cases (Access 2018 Luxi)_mod_DavidAlain_1000BASE-T1S\\TABLE 2 (change overlapped situations)\\1-4\\in");

			for(String path : pathList) {

				try {

					System.out.println();
					System.out.println("==================================================================================");
					System.out.println("----------------------------------------------------------------------------------");
					System.out.println("Dataset case path: " + path);
					System.out.println("----------------------------------------------------------------------------------");

					final DatasetReader datasetReader = new DatasetReader(path);
					final TASWindowsBuilder networkConfigBuilder = new TASWindowsBuilder(datasetReader, executionConfig);
					final EthernetNetwork ethernetNetwork = new EthernetNetwork(networkConfigBuilder);

					//Run analysis and print results
					ethernetNetwork.performAnalysis();

					ethernetNetwork.saveCurves();

				} catch(Exception e) {
					Thread.sleep(1000);
					e.printStackTrace();
					Thread.sleep(1000);
				}

			}

		}

		long timeEndMs = System.currentTimeMillis();
		System.out.println("Required time to compute bounds: " 
				+ (timeEndMs - timeIniMs)/1000.0 + " seconds "
				+ "(= "+((timeEndMs - timeIniMs)/1000.0/60.0)+" minutes)");


		System.out.println();
		System.out.println();
		System.out.println();

		if(executionConfig.analysesResult != null) {
			executionConfig.analysesResult.buildSheetWriteXLSXFile();
			//executionConfig.analysesResult.buildSheetWriteCSVFileForSeaborn();
			executionConfig.analysesResult.buildSheetWriteCSVFileForSeabornWithReplacementMap();

			//executionConfig.analysesResult.print();
		}

		System.out.println();
		System.out.println();
	}


}
