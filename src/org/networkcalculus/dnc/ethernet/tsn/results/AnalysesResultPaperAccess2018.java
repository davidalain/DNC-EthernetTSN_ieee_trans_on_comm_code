package org.networkcalculus.dnc.ethernet.tsn.results;

import java.io.FileOutputStream;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.networkcalculus.dnc.AnalysisConfig.Multiplexing;
import org.networkcalculus.dnc.ethernet.utils.DataPrinterUtil;

/**
 * 
 * @author David Alain do Nascimento (david.nascimento@garanhuns.ifpe.edu.br, dan@cin.ufpe.br)
 *
 */
public class AnalysesResultPaperAccess2018 {

	public enum Key{
		DEADLINE,
		TFA_DELAY_BOUND, 
		SFA_DELAY_BOUND, 
		PMOO_DELAY_BOUND, 
		TMA_DELAY_BOUND
	}

	public enum NetworkCase{
		case_1000BASE_TX("1000BASE-TX"),
		case_1000BASE_T1S("1000BASE-T1S"),
		case_BaselineAccess2018("Baseline");
		//		case_1000BASE_T1S__Single_PLCA_WRR("1000BASE-T1S__Single_PLCA_WRR"),
		//		case_1000BASE_T1S__Separated_PLCA_WRR("1000BASE-T1S__Separated_PLCA_WRR");

		public final String name;

		NetworkCase(String name) {
			this.name = name;
		}

		public final static NetworkCase parse(String nameFrom) {
			for(NetworkCase nc : NetworkCase.values()) {
				if(nc.name.equals(nameFrom))
					return nc;
			}
			throw new InvalidParameterException(nameFrom + " is not a valid name for a NetworkCase");
		}
	}

	private final Map<NetworkCase,	//Network				(possibilities = 1000BASE-TX, 1000BASE-T1S__Single_PLCA_WRR, 1000BASE-T1S__Separated_PLCA_WRR)
	Map<Multiplexing,				//Multiplexing			(possibilities = FIFO, ARBITRARY)
	Map<String,						//Dataset Case			(possibilities = 1-1, 1-2, 1-3, 1-4, 2-1, 2-2, 2-3, 3-1, 3-2, 3-3, 4-1, 4-2, 4-3)
	Map<String,						//Flow Name 			(possibilities = tt1 to tt13)
	Map<Key,						//Key					(possibilities = deadline, TFA, SFA, PMOO, TMA)
	Double							//Value					(in microseconds)
	>>>>> resultMap;


	public static final String DEFAULT_PATH = "results";

	private static final String FILENAME_ANALYSIS_RESULT_XLSX = "AnalysesResult.xlsx";
	private static final String FILENAME_ANALYSES_RESULT_FOR_SEABORN_CSV = "AnalysesResultForPythonSeaborn.csv";
	private static final String SHEETNAME = "Plan1";

	private String dirpath;

	public AnalysesResultPaperAccess2018(String resultOutputDirPath) {

		this.dirpath = resultOutputDirPath;
		this.resultMap = new HashMap<>();

		putBaselineDatasetPaper2018();
	}

	public AnalysesResultPaperAccess2018() {
		this(DEFAULT_PATH);
	}

	private void putBaselineDatasetPaper2018() {

		put(NetworkCase.case_BaselineAccess2018, Multiplexing.ARBITRARY, "1-1", "tt11", Key.TFA_DELAY_BOUND, 1036.6);
		put(NetworkCase.case_BaselineAccess2018, Multiplexing.ARBITRARY, "1-2", "tt11", Key.TFA_DELAY_BOUND, 1287.8);
		put(NetworkCase.case_BaselineAccess2018, Multiplexing.ARBITRARY, "1-3", "tt11", Key.TFA_DELAY_BOUND, 1173.0);
		put(NetworkCase.case_BaselineAccess2018, Multiplexing.ARBITRARY, "1-4", "tt11", Key.TFA_DELAY_BOUND, 2309.6);

		put(NetworkCase.case_BaselineAccess2018, Multiplexing.ARBITRARY, "2-1", "tt11", Key.TFA_DELAY_BOUND, 1791.1);
		put(NetworkCase.case_BaselineAccess2018, Multiplexing.ARBITRARY, "2-2", "tt11", Key.TFA_DELAY_BOUND, 1287.8);
		put(NetworkCase.case_BaselineAccess2018, Multiplexing.ARBITRARY, "2-3", "tt11", Key.TFA_DELAY_BOUND, 744.7);

		put(NetworkCase.case_BaselineAccess2018, Multiplexing.ARBITRARY, "3-1", "tt11", Key.TFA_DELAY_BOUND, 2527.8);
		put(NetworkCase.case_BaselineAccess2018, Multiplexing.ARBITRARY, "3-2", "tt11", Key.TFA_DELAY_BOUND, 1287.8);
		put(NetworkCase.case_BaselineAccess2018, Multiplexing.ARBITRARY, "3-3", "tt11", Key.TFA_DELAY_BOUND, 474.0);

		put(NetworkCase.case_BaselineAccess2018, Multiplexing.ARBITRARY, "4-1", "tt11", Key.TFA_DELAY_BOUND, 1177.9);
		put(NetworkCase.case_BaselineAccess2018, Multiplexing.ARBITRARY, "4-2", "tt11", Key.TFA_DELAY_BOUND, 1287.8);
		put(NetworkCase.case_BaselineAccess2018, Multiplexing.ARBITRARY, "4-3", "tt11", Key.TFA_DELAY_BOUND, 2014.0);

	}

	public void put(NetworkCase networkCase, Multiplexing multiplexing, String datasetCase, String flowName, Key resultKey, Double value) {

		/*
		this.resultMap
		.computeIfAbsent(networkCase, k -> new HashMap<>())
		.computeIfAbsent(multiplexing, k -> new HashMap<>())
		.computeIfAbsent(datasetCase, k -> new HashMap<>())
		.computeIfAbsent(flowName, k -> new HashMap<>())
		.put(resultKey, value);
		 */

		final Map<Multiplexing,Map<String,Map<String,Map<Key,Double>>>> multiplexingMap = this.resultMap.getOrDefault(networkCase, new HashMap<>());
		final Map<String,Map<String,Map<Key,Double>>> datasetCaseMap = multiplexingMap.getOrDefault(multiplexing, new HashMap<>());
		final Map<String,Map<Key,Double>> flowNameMap = datasetCaseMap.getOrDefault(datasetCase, new HashMap<>());
		final Map<Key,Double> resultKeyMap = flowNameMap.getOrDefault(flowName, new HashMap<>());

		resultKeyMap.put(resultKey, value);
		flowNameMap.put(flowName, resultKeyMap);
		datasetCaseMap.put(datasetCase, flowNameMap);
		multiplexingMap.put(multiplexing, datasetCaseMap);
		this.resultMap.put(networkCase, multiplexingMap);
	}

	public Map<Key, Double> get(NetworkCase networkCase, Multiplexing multiplexing, String datasetCase, String flowName) {

		/*
		return this.resultMap
				.computeIfAbsent(networkCase, k -> new HashMap<>())
				.computeIfAbsent(multiplexing, k -> new HashMap<>())
				.computeIfAbsent(datasetCase, k -> new HashMap<>())
				.get(flowName);
		 */

		final Map<Multiplexing,Map<String,Map<String,Map<Key,Double>>>> multiplexingMap = this.resultMap.getOrDefault(networkCase, new HashMap<>());
		final Map<String,Map<String,Map<Key,Double>>> datasetCaseMap = multiplexingMap.getOrDefault(multiplexing, new HashMap<>());
		final Map<String,Map<Key,Double>> flowNameMap = datasetCaseMap.getOrDefault(datasetCase, new HashMap<>());
		final Map<Key,Double> resultKeyMap = flowNameMap.getOrDefault(flowName, new HashMap<>());

		return resultKeyMap;
	}

	public Double get(NetworkCase networkCase, Multiplexing multiplexing, String datasetCase, String flowName, Key resultKey) {

		/*
		return this.resultMap
				.computeIfAbsent(networkCase, k -> new HashMap<>())
				.computeIfAbsent(multiplexing, k -> new HashMap<>())
				.computeIfAbsent(datasetCase, k -> new HashMap<>())
				.computeIfAbsent(flowName, k -> new HashMap<>())
				.get(resultKey);
		 */

		final Map<Multiplexing,Map<String,Map<String,Map<Key,Double>>>> multiplexingMap = this.resultMap.getOrDefault(networkCase, new HashMap<>());
		final Map<String,Map<String,Map<Key,Double>>> datasetCaseMap = multiplexingMap.getOrDefault(multiplexing, new HashMap<>());
		final Map<String,Map<Key,Double>> flowNameMap = datasetCaseMap.getOrDefault(datasetCase, new HashMap<>());
		final Map<Key,Double> resultKeyMap = flowNameMap.getOrDefault(flowName, new HashMap<>());

		return resultKeyMap.get(resultKey);
	}

	public Map<NetworkCase, Map<Multiplexing,Map<String,Map<String,Map<Key,Double>>>>> getFullMap(){
		return new HashMap<>(this.resultMap);
	}

	@Override
	public String toString() {

		final StringBuilder sb = new StringBuilder();

		final int columnWidth15 = 15;
		final int columnWidth25 = 25;
		final int columnWidth20 = 20;
		final int columnWidth35 = 35;

		sb.append(DataPrinterUtil.toStringStaticLength("Network Case ", 		columnWidth35, ' '));
		sb.append(DataPrinterUtil.toStringStaticLength("Used Multiplexing ", 	columnWidth20, ' '));
		sb.append(DataPrinterUtil.toStringStaticLength("Dataset Case", 			columnWidth15, ' '));
		sb.append(DataPrinterUtil.toStringStaticLength("Flow Name", 			columnWidth15, ' '));       
		sb.append(DataPrinterUtil.toStringStaticLength("deadline (us)", 		columnWidth15, ' '));
		sb.append(DataPrinterUtil.toStringStaticLength("delay bound TFA (us)", 	columnWidth25, ' '));
		sb.append(DataPrinterUtil.toStringStaticLength("delay bound SFA (us)", 	columnWidth25, ' '));
		sb.append(DataPrinterUtil.toStringStaticLength("delay bound PMOO (us)", columnWidth25, ' '));
		sb.append(DataPrinterUtil.toStringStaticLength("delay bound TMA (us)", 	columnWidth25, ' '));
		sb.append("\r\n");

		final List<NetworkCase> networkCaseList = new LinkedList<>(this.resultMap.keySet());
		Collections.sort(networkCaseList); //sort by declaration order of items into enum

		for(final NetworkCase networkCase : networkCaseList) {

			final List<Multiplexing> multiplexingList = new LinkedList<>(this.resultMap.get(networkCase).keySet());
			Collections.sort(multiplexingList);  //sort by declaration order of items into enum

			for(final Multiplexing multiplexing : multiplexingList) {

				final List<String> datasetCaseList = new LinkedList<>(this.resultMap.get(networkCase).get(multiplexing).keySet());
				Collections.sort(datasetCaseList);

				for(String datasetCase : datasetCaseList) {

					final List<String> flowNameList = new LinkedList<>(this.resultMap.get(networkCase).get(multiplexing).get(datasetCase).keySet());
					Collections.sort(flowNameList);

					for(String flowName : flowNameList) {

						final List<Key> resultKeyList = new LinkedList<>(this.resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).keySet());
						Collections.sort(resultKeyList); //sort by declaration order of items into enum

						if(!resultKeyList.contains(Key.DEADLINE)) {
							//throw new InvalidParameterException("Key.DEADLINE is required");
						}

						if(resultKeyList.size() < 2) {
							//	throw new InvalidParameterException("You must use at least one analysis method (TFA, SFA, PMOO, or TMA). resultKeyList="+resultKeyList);
						}

						final Double deadline = this.resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).get(Key.DEADLINE);
						final Double delayTFA = this.resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).get(Key.TFA_DELAY_BOUND);
						final Double delaySFA = this.resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).get(Key.SFA_DELAY_BOUND);

						Double delayPMOO = null;
						Double delayTMA = null;

						//if(multiplexing == Multiplexing.ARBITRARY) {
						delayPMOO = this.resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).get(Key.PMOO_DELAY_BOUND);
						delayTMA = this.resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).get(Key.TMA_DELAY_BOUND);				
						//}

						sb.append(DataPrinterUtil.toStringStaticLength(networkCase.name,	columnWidth35, ' '));
						sb.append(DataPrinterUtil.toStringStaticLength(multiplexing,		columnWidth20, ' '));
						sb.append(DataPrinterUtil.toStringStaticLength(datasetCase,			columnWidth15, ' '));
						sb.append(DataPrinterUtil.toStringStaticLength(flowName,			columnWidth15, ' '));
						sb.append(DataPrinterUtil.toStringStaticLength(deadline,			columnWidth15, ' '));

						sb.append(DataPrinterUtil.toStringStaticLength(delayTFA, columnWidth25, ' '));
						sb.append(DataPrinterUtil.toStringStaticLength(delaySFA, columnWidth25, ' '));
						//if(multiplexing == Multiplexing.ARBITRARY) {
						sb.append(DataPrinterUtil.toStringStaticLength(delayPMOO, 	columnWidth25, ' '));
						sb.append(DataPrinterUtil.toStringStaticLength(delayTMA, 	columnWidth25, ' '));
						//}
						sb.append("\r\n");
					}

				}

			}

		}

		return sb.toString();
	}

	public String toStringCSV(final Map<String,String> replacementMap) {

		final StringBuilder sb = new StringBuilder();

		sb
		.append("network").append(";")
		.append("multiplexing").append(";")
		.append("dataset_case").append(";")
		.append("experiment").append(";")
		.append("case").append(";")
		.append("flow_name").append(";")
		.append("type").append(";")
		.append("worst_case_delay").append("\r\n");

		final List<NetworkCase> networkCaseList = new LinkedList<>(this.resultMap.keySet());
		Collections.sort(networkCaseList); //sort by declaration order of items into enum

		for(final NetworkCase networkCase : networkCaseList) {

			final List<Multiplexing> multiplexingList = new LinkedList<>(this.resultMap.get(networkCase).keySet());
			Collections.sort(multiplexingList);  //sort by declaration order of items into enum

			for(final Multiplexing multiplexing : multiplexingList) {

				final List<String> datasetCaseList = new LinkedList<>(this.resultMap.get(networkCase).get(multiplexing).keySet());
				Collections.sort(datasetCaseList);

				for(String datasetCase : datasetCaseList) {

					final List<String> flowNameList = new LinkedList<>(this.resultMap.get(networkCase).get(multiplexing).get(datasetCase).keySet());
					Collections.sort(flowNameList);

					for(String flowName : flowNameList) {

						final List<Key> resultKeyList = new LinkedList<>(this.resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).keySet());
						Collections.sort(resultKeyList); //sort by declaration order of items into enum

						if(!resultKeyList.contains(Key.DEADLINE)) {
							//throw new InvalidParameterException("Key.DEADLINE is required");
						}

						if(resultKeyList.size() < 2) {
							//	throw new InvalidParameterException("You must use at least one analysis method (TFA, SFA, PMOO, or TMA). resultKeyList="+resultKeyList);
						}

						for(Key key : resultKeyList) {

							final Double value = resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).get(key);

							if(replacementMap == null) {

								sb
								.append(networkCase.name).append(";")
								.append(multiplexing).append(";")
								.append(datasetCase + "-" + networkCase.name).append(";")
								.append(datasetCase.split("-")[0]).append(";")
								.append(datasetCase.split("-")[1]).append(";")
								.append(flowName).append(";")
								.append(key).append(";");

								if(value != null) 	sb.append(value);
								else				sb.append("");

								sb.append("\r\n");

							} else {

								sb
								.append(replacementMap.getOrDefault(networkCase.name, networkCase.name)).append(";")
								.append(multiplexing).append(";")
								.append(datasetCase + "-" + replacementMap.getOrDefault(networkCase.name, networkCase.name)).append(";")
								.append(datasetCase.split("-")[0]).append(";")
								.append(datasetCase.split("-")[1]).append(";")
								.append(flowName).append(";")
								.append(key).append(";");

								if(value != null) 	sb.append(value);
								else				sb.append("");

								sb.append("\r\n");

							}

						}

					}

				}

			}

		}

		return sb.toString();
	}

	public final void buildSheetWriteXLSXFile(){

		final XSSFWorkbook workbookOut = new XSSFWorkbook();
		final Sheet sheet = workbookOut.createSheet(SHEETNAME);
		int rowIndex = sheet.getPhysicalNumberOfRows();
		int columnIndex = 0;

		try {

			final Row headerRow = sheet.createRow(rowIndex++);

			headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("Network Case");
			headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("Used Multiplexing");
			headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("Dataset Case");
			headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("Flow Name");
			headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("deadline (us)");
			headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("delay bound TFA (us)");
			headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("delay bound SFA (us)");
			headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("delay bound PMOO (us)");
			headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("delay bound TMA (us)");

			putAllDataIntoSheet(sheet, rowIndex, columnIndex);
			writeToFileXLSX(workbookOut);

		}catch (IOException e) {
			e.printStackTrace();
		}

	}

	public final void buildSheetWriteCSVFileForSeaborn() {

		final XSSFWorkbook workbookOut = new XSSFWorkbook();
		final Sheet sheet = workbookOut.createSheet(SHEETNAME);
		int rowIndex = sheet.getPhysicalNumberOfRows();
		int columnIndex = 0;

		try {

			final Row headerRow = sheet.createRow(rowIndex++);

			headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("network_case");
			headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("multiplexing");
			headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("dataset_case");
			headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("flow_name");
			headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("type");
			headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("value");

			final String textCSV = toStringCSV(null);
			writeToFileCSV(textCSV);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				workbookOut.close();	
			}catch (Exception e) { }
		}

	}

	public final void buildSheetWriteCSVFileForSeabornWithReplacementMap() {

		final Map<String,String> replacementMap = new HashMap<>();

		replacementMap.put(NetworkCase.case_1000BASE_TX.name, "MA1");
		replacementMap.put(NetworkCase.case_1000BASE_T1S.name, "MA2");

		try {

			final String textCSV = toStringCSV(replacementMap);
			writeToFileCSV(textCSV);

		}catch (IOException e) {
			e.printStackTrace();
		}

	}

	private final void putAllDataIntoSheet(Sheet sheet, int rowIndex, int columnIndex) {

		final List<NetworkCase> networkCaseList = new LinkedList<>(this.resultMap.keySet());
		Collections.sort(networkCaseList); //sort by declaration order of items into enum

		for(final NetworkCase networkCase : networkCaseList) {

			final List<Multiplexing> multiplexingList = new LinkedList<>(this.resultMap.get(networkCase).keySet());
			Collections.sort(multiplexingList);  //sort by declaration order of items into enum

			for(final Multiplexing multiplexing : multiplexingList) {

				final List<String> datasetCaseList = new LinkedList<>(this.resultMap.get(networkCase).get(multiplexing).keySet());
				Collections.sort(datasetCaseList);

				for(String datasetCase : datasetCaseList) {

					final List<String> flowNameList = new LinkedList<>(this.resultMap.get(networkCase).get(multiplexing).get(datasetCase).keySet());
					Collections.sort(flowNameList);

					for(String flowName : flowNameList) {

						final List<Key> resultKeyList = new LinkedList<>(this.resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).keySet());
						Collections.sort(resultKeyList); //sort by declaration order of items into enum

						if(!resultKeyList.contains(Key.DEADLINE)) {
							//FIXME
							//throw new InvalidParameterException("Key.DEADLINE is required");
						}

						if(resultKeyList.size() < 2) {
							//FIXME
							//	throw new InvalidParameterException("You must use at least one analysis method (TFA, SFA, PMOO, or TMA). resultKeyList="+resultKeyList);
						}

						final Double deadline = resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).get(Key.DEADLINE);
						final Double delayTFA = resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).get(Key.TFA_DELAY_BOUND);
						final Double delaySFA = resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).get(Key.SFA_DELAY_BOUND);
						final Double delayPMOO = resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).get(Key.PMOO_DELAY_BOUND);
						final Double delayTMA = resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).get(Key.TMA_DELAY_BOUND);

						final Row row = sheet.createRow(rowIndex++);
						columnIndex = 0;

						row.createCell(columnIndex++, CellType.STRING).setCellValue(networkCase.name);
						row.createCell(columnIndex++, CellType.STRING).setCellValue(multiplexing.toString());
						row.createCell(columnIndex++, CellType.STRING).setCellValue(datasetCase);
						row.createCell(columnIndex++, CellType.STRING).setCellValue(flowName);

						if(deadline != null)	row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(deadline);
						else					row.createCell(columnIndex++, CellType.STRING).setCellValue("");

						if(delayTFA != null)	row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(delayTFA);
						else					row.createCell(columnIndex++, CellType.STRING).setCellValue("");

						if(delaySFA != null)	row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(delaySFA);
						else					row.createCell(columnIndex++, CellType.STRING).setCellValue("");

						if(delayPMOO != null)	row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(delayPMOO);
						else					row.createCell(columnIndex++, CellType.STRING).setCellValue("");

						if(delayTMA != null) 	row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(delayTMA);
						else					row.createCell(columnIndex++, CellType.STRING).setCellValue("");

					}

				}

			}

		}

	}

	@SuppressWarnings("unused")
	private final void putAllDataSeaborn(Sheet sheet, int rowIndex, int columnIndex) {

		final List<NetworkCase> networkCaseList = new LinkedList<>(this.resultMap.keySet());
		Collections.sort(networkCaseList); //sort by declaration order of items into enum

		for(final NetworkCase networkCase : networkCaseList) {

			final List<Multiplexing> multiplexingList = new LinkedList<>(this.resultMap.get(networkCase).keySet());
			Collections.sort(multiplexingList);  //sort by declaration order of items into enum

			for(final Multiplexing multiplexing : multiplexingList) {

				final List<String> datasetCaseList = new LinkedList<>(this.resultMap.get(networkCase).get(multiplexing).keySet());
				Collections.sort(datasetCaseList);

				for(String datasetCase : datasetCaseList) {

					final List<String> flowNameList = new LinkedList<>(this.resultMap.get(networkCase).get(multiplexing).get(datasetCase).keySet());
					Collections.sort(flowNameList);

					for(String flowName : flowNameList) {

						final List<Key> resultKeyList = new LinkedList<>(this.resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).keySet());
						Collections.sort(resultKeyList); //sort by declaration order of items into enum

						if(!resultKeyList.contains(Key.DEADLINE)) {
							//FIXME
							//throw new InvalidParameterException("Key.DEADLINE is required");
						}

						if(resultKeyList.size() < 2) {
							//FIXME
							//throw new InvalidParameterException("You must use at least one analysis method (TFA, SFA, PMOO, or TMA). resultKeyList="+resultKeyList);
						}

						for(Key key : resultKeyList) {

							final Double value = resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).get(key);

							final Row row = sheet.createRow(rowIndex++);
							columnIndex = 0;

							row.createCell(columnIndex++, CellType.STRING).setCellValue(networkCase.name);
							row.createCell(columnIndex++, CellType.STRING).setCellValue(multiplexing.toString());
							row.createCell(columnIndex++, CellType.STRING).setCellValue(datasetCase);
							row.createCell(columnIndex++, CellType.STRING).setCellValue(flowName);
							row.createCell(columnIndex++, CellType.STRING).setCellValue(key.toString());

							if(value != null) 	row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(value);
							else				row.createCell(columnIndex++, CellType.STRING).setCellValue("");

						}
					}

				}

			}

		}

	} 

	@SuppressWarnings("unused")
	private final void putAllDataSeaborn(Sheet sheet, int rowIndex, int columnIndex, Map<String, String> replacementMap) {

		final List<NetworkCase> networkCaseList = new LinkedList<>(this.resultMap.keySet());
		Collections.sort(networkCaseList); //sort by declaration order of items into enum

		for(final NetworkCase networkCase : networkCaseList) {

			final List<Multiplexing> multiplexingList = new LinkedList<>(this.resultMap.get(networkCase).keySet());
			Collections.sort(multiplexingList);  //sort by declaration order of items into enum

			for(final Multiplexing multiplexing : multiplexingList) {

				final List<String> datasetCaseList = new LinkedList<>(this.resultMap.get(networkCase).get(multiplexing).keySet());
				Collections.sort(datasetCaseList);

				for(String datasetCase : datasetCaseList) {

					final List<String> flowNameList = new LinkedList<>(this.resultMap.get(networkCase).get(multiplexing).get(datasetCase).keySet());
					Collections.sort(flowNameList);

					for(String flowName : flowNameList) {

						final List<Key> resultKeyList = new LinkedList<>(this.resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).keySet());
						Collections.sort(resultKeyList); //sort by declaration order of items into enum

						if(!resultKeyList.contains(Key.DEADLINE)) {
							//FIXME
							//throw new InvalidParameterException("Key.DEADLINE is required");
						}

						if(resultKeyList.size() < 2) {
							//FIXME
							//throw new InvalidParameterException("You must use at least one analysis method (TFA, SFA, PMOO, or TMA). resultKeyList="+resultKeyList);
						}

						for(Key key : resultKeyList) {

							final Double value = resultMap.get(networkCase).get(multiplexing).get(datasetCase).get(flowName).get(key);

							final Row row = sheet.createRow(rowIndex++);
							columnIndex = 0;

							row.createCell(columnIndex++, CellType.STRING).setCellValue(replacementMap.getOrDefault(networkCase.name, networkCase.name));
							row.createCell(columnIndex++, CellType.STRING).setCellValue(replacementMap.getOrDefault(multiplexing.toString(),multiplexing.toString()));
							row.createCell(columnIndex++, CellType.STRING).setCellValue(replacementMap.getOrDefault(datasetCase,datasetCase));
							row.createCell(columnIndex++, CellType.STRING).setCellValue(replacementMap.getOrDefault(flowName,flowName));
							row.createCell(columnIndex++, CellType.STRING).setCellValue(replacementMap.getOrDefault(key.toString(),key.toString()));

							if(value != null) 	row.createCell(columnIndex++, CellType.NUMERIC).setCellValue(replacementMap.getOrDefault(value.toString(),value.toString()));
							else				row.createCell(columnIndex++, CellType.STRING).setCellValue("");

						}
					}

				}

			}

		}

	} 

	private void writeToFileXLSX(XSSFWorkbook workbookOut) throws IOException {
		final String filepath = this.dirpath + "/" + FILENAME_ANALYSIS_RESULT_XLSX;
		final FileOutputStream fileOut = new FileOutputStream(filepath);
		workbookOut.write(fileOut);
		workbookOut.close();
		fileOut.close();
	}

	private void writeToFileCSV(final String textCSV) throws IOException {
		final String filepath = this.dirpath + "/" + FILENAME_ANALYSES_RESULT_FOR_SEABORN_CSV;
		final FileOutputStream fileOut = new FileOutputStream(filepath);
		fileOut.write(textCSV.getBytes());
		fileOut.close();
	}

}

