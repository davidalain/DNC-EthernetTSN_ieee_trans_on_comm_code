package org.networkcalculus.dnc.ethernet.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.networkcalculus.dnc.ethernet.tsn.entry.InterfaceInfoEntry;
import org.networkcalculus.dnc.ethernet.tsn.model.TASWindow;

/**
 * Class for generating PortGuaranteedWin.txt files to compare to Luxi Zhao's results.
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 */
public class PortGuaranteedWindowsExporter {

	private String dirpath;

	public PortGuaranteedWindowsExporter(final String resultDirPath){

		this.dirpath = resultDirPath;
	}

	public final void writeFileAsZhaosResults(Map<InterfaceInfoEntry,Map<Integer, List<TASWindow>>> mapInterfaceInfoPriorityTasWindowDataList) {

		try {

			/**
			 * ES1.0	->	SW1.0:
			 * SW1.3	->	SW2.0:
			 * SW2.1	->	ES4.0:
			 * ES2.0	->	SW1.1:
			 * SW2.2	->	ES5.0:
			 * ES3.0	->	SW1.2:
			 * SW2.3	->	ES6.0:
			 * SW1.1	->	ES2.0:
			 * SW1.2	->	ES3.0:
			 * SW1.0	->	ES1.0:
			 */

			final StringBuilder sb = new StringBuilder();

			sb.append("#link, priority: start time, end time").append("\r\n");

			List<InterfaceInfoEntry> interfaceInfolist = new LinkedList<>(mapInterfaceInfoPriorityTasWindowDataList.keySet());
			Collections.sort(interfaceInfolist, new Comparator<InterfaceInfoEntry>() {

				final List<String> interfacesSequence = Arrays.asList(new String[] {
						"ES1.0",
						"SW1.3",
						"SW2.1",
						"ES2.0",
						"SW2.2",
						"ES3.0",
						"SW2.3",
						"SW1.1",
						"SW1.2",
						"SW1.0"
				});

				@Override
				public int compare(InterfaceInfoEntry o1, InterfaceInfoEntry o2) {
					return Integer.compare(
							interfacesSequence.indexOf(o1.toStringInfo()),
							interfacesSequence.indexOf(o2.toStringInfo())
							);
				}
			});

			final Map<String, String> mapInterfaceLink = new HashMap<>();
			mapInterfaceLink.put("ES1.0","ES1->SW1");
			mapInterfaceLink.put("SW1.3","SW1->SW2");
			mapInterfaceLink.put("SW2.1","SW2->ES4");
			mapInterfaceLink.put("ES2.0","ES2->SW1");
			mapInterfaceLink.put("SW2.2","SW2->ES5");
			mapInterfaceLink.put("ES3.0","ES3->SW1");
			mapInterfaceLink.put("SW2.3","SW2->ES6");
			mapInterfaceLink.put("SW1.1","SW1->ES2");
			mapInterfaceLink.put("SW1.2","SW1->ES3");
			mapInterfaceLink.put("SW1.0","SW1->ES1");

			final Map<String, String> mapCasePath = new HashMap<>();
			mapCasePath.put("1-1", "TC1 - random open windows (change overlapped situations)\\1-1");
			mapCasePath.put("1-2", "TC1 - random open windows (change overlapped situations)\\1-2");
			mapCasePath.put("1-3", "TC1 - random open windows (change overlapped situations)\\1-3");
			mapCasePath.put("1-4", "TC1 - random open windows (change overlapped situations)\\1-4");

			mapCasePath.put("2-1", "TC2 - random open windows (change length)\\2-1");
			mapCasePath.put("2-2", "TC2 - random open windows (change length)\\2-2");
			mapCasePath.put("2-3", "TC2 - random open windows (change length)\\2-3");

			mapCasePath.put("3-1", "TC3 - random open windows (change period)\\3-1");
			mapCasePath.put("3-2", "TC3 - random open windows (change period)\\3-2");
			mapCasePath.put("3-3", "TC3 - random open windows (change period)\\3-3");

			mapCasePath.put("4-1", "TC4 - random flows (change priority)\\4-1");
			mapCasePath.put("4-2", "TC4 - random flows (change priority)\\4-2");
			mapCasePath.put("4-3", "TC4 - random flows (change priority)\\4-3");


			for(InterfaceInfoEntry entry : interfaceInfolist) {

				sb.append(mapInterfaceLink.get(entry.toStringInfo())).append(":").append("\r\n");

				Map<Integer, List<TASWindow>> map = mapInterfaceInfoPriorityTasWindowDataList.get(entry);

				for(int i = 0 ; i < 8 ; i++) {

					sb.append("Priority ").append(i).append(": ");

					final List<TASWindow> tasWindowList = map.get(i);

					if(tasWindowList != null) {

						for(TASWindow window : tasWindowList) {

							if ((window.t_Bi_Pm == Math.floor(window.t_Bi_Pm)) && !Double.isInfinite(window.t_Bi_Pm)) {
								sb.append(((int) ((double) window.t_Bi_Pm)));
							} else {
								sb.append(window.t_Bi_Pm);
							}

							sb.append(", ");


							if ((window.t_Ei_Pm == Math.floor(window.t_Ei_Pm)) && !Double.isInfinite(window.t_Ei_Pm)) {
								sb.append(((int) ((double) window.t_Ei_Pm)));
							} else {
								sb.append(window.t_Ei_Pm);
							}

							sb.append("; ");
						}

					}

					sb.append("\r\n");
				}

				sb.append("\r\n");
			}

			final String[] parts = dirpath.split("_");
			final String testCase = parts[parts.length - 2];
			final String dir = mapCasePath.get(testCase);
			final String networkType = parts[9];

			if(dir == null)
				throw new InvalidParameterException("testCase="+testCase+", " + "dirpath="+ dirpath);

			final String resutsPath = "results/davidalain_results_compare_to_zhao_"+networkType+"/" + dir;
			
			final File dirFile = new File(resutsPath);
			if(!dirFile.exists())
				dirFile.mkdirs();

			final String filepath = resutsPath + "/PortGuaranteedWin.txt";
			final FileOutputStream fileOut = new FileOutputStream(filepath);
			fileOut.write(sb.toString().getBytes());
			fileOut.close();


		}catch (Exception e) {
			e.printStackTrace();
		}

	}

}
