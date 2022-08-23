package org.networkcalculus.dnc.ethernet.utils;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.networkcalculus.dnc.ethernet.tsn.entry.InterfaceInfoEntry;
import org.networkcalculus.dnc.ethernet.tsn.model.TASWindow;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 *
 */
public class ExcelSheetTASWindow {

	private static final String FILENAME = "TASWindows.xlsx";
	private static final String SHEETNAME = "Plan1";

	private String dirpath;
	private Workbook workbookOut;
	private Sheet sheet;
	private int rowIndex;
	private int columnIndex;

	public ExcelSheetTASWindow(final String resultDirPath){

		this.dirpath = resultDirPath;

		this.workbookOut = new XSSFWorkbook();
		this.sheet = workbookOut.createSheet(SHEETNAME);
	}

	public final void buildSheetWriteFile(Map<InterfaceInfoEntry,Map<Integer, List<TASWindow>>> mapInterfaceInfoPriorityTasWindowDataList) {

		try {

			int max_o_ji_Pm_length = 0;

			for(Entry<InterfaceInfoEntry,Map<Integer, List<TASWindow>>> entry0 : mapInterfaceInfoPriorityTasWindowDataList.entrySet()) {

				for(Entry<Integer,List<TASWindow>> entry : entry0.getValue().entrySet()) {

					for(TASWindow tasWindow : entry.getValue()) {

						if(max_o_ji_Pm_length < tasWindow.o_ji_Pm.length) {
							max_o_ji_Pm_length = tasWindow.o_ji_Pm.length;
						}

					}
				}
			}

			this.rowIndex=sheet.getPhysicalNumberOfRows();
			this.columnIndex=0;

			if(rowIndex == 0) {

				final Row headerRow = sheet.createRow(rowIndex++);

				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("device");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("interfaceId");

				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("P_m");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("i");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("N_{P_m}");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("T_{P_m}");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("T_{GCL}");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("t^{o,i}_{P_m}");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("t^{c,i}_{P_m}");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("l^{max}_{P_m}");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("l^{min}_{P_m}");
//				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("l^{max}_{P_{m^{+}}}");
//				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("t^{c,i}_{P_{m^{+}}}");
//				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("d^{np,i}_{P_{m^{+}}}");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("d^{np,i}_L");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("t^{np,i}_L");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("d^{gb}_{P_m}");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("t^{gb,i}_{P_m}");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("t^{c,i}_{P_{m^{-}}}");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("t^{o,i}_{P_{m^{-}}}");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("t^{B,i}_H");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("t^{E,i}_H");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("t^{B,i}_{P_m}");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("t^{E,i}_{P_m}");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("\\overline{L}^i_{P_m}");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("\\overline{o}^i_{P_m}");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("d^{np,0}_L");
				headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("S^{i}_{P_m}");
				for(int j = 0 ; j < max_o_ji_Pm_length ; j++) {
					headerRow.createCell(columnIndex++, CellType.STRING).setCellValue("o^{j,i}_{P_m}[j="+j+"]");
				}

			}

			putAll(mapInterfaceInfoPriorityTasWindowDataList);

			writeToFile();

		}catch (IOException e) {
			e.printStackTrace();
		}

	}
	
	private final void putAll(Map<InterfaceInfoEntry,Map<Integer, List<TASWindow>>> mapInterfaceInfoPriorityTasWindowDataList) {
		
		for(Entry<InterfaceInfoEntry,Map<Integer, List<TASWindow>>> entry : mapInterfaceInfoPriorityTasWindowDataList.entrySet()) {
			put(entry);
		}
		
	}

	private final void put(Entry<InterfaceInfoEntry,Map<Integer, List<TASWindow>>> entryMap) {
		
		final InterfaceInfoEntry interfaceInfo = entryMap.getKey();
		final Map<Integer,List<TASWindow>> mapPriorityTasList = entryMap.getValue();

		for(Entry<Integer,List<TASWindow>> entry : mapPriorityTasList.entrySet()) {

			for(TASWindow tasWindow : entry.getValue()) {

				final Row valueRow = sheet.createRow(rowIndex++);
				this.columnIndex = 0;

				valueRow.createCell(columnIndex++, CellType.STRING).setCellValue(interfaceInfo.deviceName);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(interfaceInfo.interfaceId);

				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.priorityPm);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.index);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.N_Pm);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.T_Pm);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.T_GCL);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.t_oi_Pm);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.t_ci_Pm);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.l_max_Pm);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.l_min_Pm);
//				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.l_max_Pm_plus);
//				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.t_ci_Pm_plus);
//				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.d_npi_Pm_plus);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.d_npi_L);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.t_npi_L);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.d_gb_Pm);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.t_gbi_Pm);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.t_ci_Pm_minus);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.t_oi_Pm_minus);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.t_Bi_H);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.t_Ei_H);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.t_Bi_Pm);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.t_Ei_Pm);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.L_bar_i_Pm);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.o_bar_i_Pm);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.d_np0_L);
				valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.S_i_Pm);
				for(int j = 0 ; j < tasWindow.o_ji_Pm.length ; j++) {
					valueRow.createCell(columnIndex++, CellType.NUMERIC).setCellValue(tasWindow.o_ji_Pm[j]);
				}

			}
		}

	}

	private void writeToFile() throws IOException {
		final String filepath = this.dirpath + "/" + FILENAME;
		final FileOutputStream fileOut = new FileOutputStream(filepath);
		this.workbookOut.write(fileOut);
		this.workbookOut.close();
		fileOut.close();
	}

}
