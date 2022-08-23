package org.networkcalculus.dnc.ethernet.tsn;

import org.networkcalculus.dnc.AnalysisConfig.Multiplexing;
import org.networkcalculus.dnc.ethernet.tsn.results.AnalysesResultPaperAccess2018;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 *
 */
public class ExecutionConfig {
	
	public enum PrioritizingOrder {
		/**
		 * As done by Luxi Zhao et al.
		 */
		LOWER_VALUE_HIGHER_PRIORITY,

		/**
		 * As declared in the IEEE 802.1Q
		 */
		HIGHER_VALUE_HIGHER_PRIORITY,
	}
	
	public enum PLCAModeling {
		/**
		 * if running 'Single PLCA-WRR Server'
		 * It relies on the fluid flow model and assume a NC server in PHY for reflecting medium access for the PLCA mechanism.  
		 */
		SINGLE_PLCA_SERVER_MODELING,

		/**
		 * if running 'No PLCA-WRR Server' modeling.
		 * It relies on the packetized model and does not assume any NC server in PHY.
		 * Instead, the PLCA behavior is captured in a term called 'd_i_PLCA' in the TASWindow class. 
		 */
		NO_PLCA_SERVER_MODELING,
	}
	
	public enum GenerateTASWindowCharts {
		/**
		 * 
		 */
		NO,
		
		/**
		 * 
		 */
		GENERATE_INTERFACES_CHARTS_ONLY,
		
		/**
		 * 
		 */
		GENERATE_WINDOW_COLLISIONS_CHARTS_ONLY,
		
		/**
		 * 
		 */
		ALL,
	}
	
	public enum GenerateNCCurvesCharts {
		/**
		 * 
		 */
		NO,
		
		/**
		 * 
		 */
		GENERATE_ARRIVAL_CURVES_CHARTS_ONLY,
		
		/**
		 * 
		 */
		GENERATE_SERVICE_CURVES_CHARTS_ONLY,
		
		/**
		 * 
		 */
		ALL,
	}
	
	public enum GeneratePortGuaranteedWindowsFiles{
		/**
		 * 
		 */
		NO,
		
		/**
		 * 
		 */
		ALL
	}
	
	public enum GenerateTASWindowsExcelSheet {
		/**
		 * Do not to generate any Excel sheet with already computed values.
		 */
		NO,
		
		/**
		 * Generate all Excel sheets with all computed attribute values of TASWindow.
		 */
		ALL,
	}
	
	public enum ValidateSchedulingForFrameSize {
		/**
		 * Do not validate if there is an ST frame that does not fit within its related TAS guaranteed windows along its whole path.
		 */
		NO,
		
		/**
		 * Check all ST frames fits within TAS windows in each Ethernet interface along their whole frame's path.
		 * Print on standard err if there are ST frames that do not fit in its related TAS window.
		 */
		YES_PRINT_IF_INVALID,
		
		/**
		 * Assure all ST frames fits within TAS windows in each Ethernet interface along their whole frame's path.
		 * Throw an exception if there are ST frames that do not fit in its related TAS window.
		 */
		YES_THROW_ERROR_IF_INVAID,
	}
	
	public enum SaveServerGraph {
		/**
		 * 
		 */
		NO,
		
		/**
		 * 
		 */
		YES,
	}
	

	public final AnalysesResultPaperAccess2018 analysesResult;

	/**
	 * 
	 */
	public final GenerateTASWindowsExcelSheet generateTASWindowsExcelSheet;

	/**
	 * 
	 */
	public final GenerateTASWindowCharts generateTASWindowsCharts;
	
	/**
	 * 
	 */
	public final GenerateNCCurvesCharts generateNCCurvesCharts;
	
	/**
	 * 
	 */
	public final GeneratePortGuaranteedWindowsFiles generatePortGuaranteedWinFiles;

	/**
	 * PrioritizingOrder.LOWER_VALUE_HIGHER_PRIORITY 	for using the same way as Zhao et al.
	 * PrioritizingOrder.HIGHER_VALUE_HIGHER_PRIORITY 	for using the correct way accordingly to standard IEEE 802.1Q
	 */
	public final PrioritizingOrder prioritizingOrder;

	/**
	 * Multiplexing.ARBITRARY, or
	 * Multiplexing.FIFO
	 */
	public final Multiplexing multiplexing;

	/**
	 * 
	 */
	public final ValidateSchedulingForFrameSize validateSchedulingForFrameSize;

	/**
	 * 
	 */
	public final SaveServerGraph saveServerGraph;

	/**
	 * true 	-> if running both 'Single PLCA-WRR Server' or 'Separated PLCA-WRR Server' (fluid flow model)
	 * false	-> if running 'No PLCA-WRR Server' (packetized model)
	 */
	public final PLCAModeling plcaModeling;

	
	
	public ExecutionConfig(AnalysesResultPaperAccess2018 analysesResult, 
			GenerateTASWindowsExcelSheet generateTASWindowsExcelSheet,
			GenerateTASWindowCharts generateTASWindowsCharts,
			GenerateNCCurvesCharts generateNCCurvesCharts, 
			GeneratePortGuaranteedWindowsFiles generatePortGuaranteedWinFiles,
			PrioritizingOrder prioritizingOrder, 
			PLCAModeling plcaModeling,
			ValidateSchedulingForFrameSize validateSchedulingForFrameSize,
			SaveServerGraph saveServerGraph,
			Multiplexing multiplexing) 
	{
		super();
		this.analysesResult = analysesResult;
		this.generateTASWindowsExcelSheet = generateTASWindowsExcelSheet;
		this.generateTASWindowsCharts = generateTASWindowsCharts;
		this.generateNCCurvesCharts = generateNCCurvesCharts;
		this.generatePortGuaranteedWinFiles = generatePortGuaranteedWinFiles;
		this.prioritizingOrder = prioritizingOrder;
		this.multiplexing = multiplexing;
		this.validateSchedulingForFrameSize = validateSchedulingForFrameSize;
		this.saveServerGraph = saveServerGraph;
		this.plcaModeling = plcaModeling;
	}

	
}
