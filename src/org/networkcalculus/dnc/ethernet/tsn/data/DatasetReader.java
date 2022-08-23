package org.networkcalculus.dnc.ethernet.tsn.data;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import org.networkcalculus.dnc.ethernet.EthernetPhyStandard;
import org.networkcalculus.dnc.ethernet.tsn.TASWindowsBuilder;
import org.networkcalculus.dnc.ethernet.tsn.entry.InterfaceInfoEntry;
import org.networkcalculus.dnc.ethernet.tsn.entry.STMessageEntry;
import org.networkcalculus.dnc.ethernet.tsn.entry.TASGateScheduleEntry2018;
import org.networkcalculus.dnc.ethernet.tsn.entry.VirtualLinkEntry;
import org.networkcalculus.dnc.ethernet.utils.DataPrinterUtil;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 *
 */
public class DatasetReader {

	private final String dirpath;

	public DatasetReader(String dirpath) {
		this.dirpath = dirpath;
	}

	
	public static final List<String> listDatasetFilesPaths(String datasetRootPath){
		
		final File f = new File(datasetRootPath);

		if(!f.exists())
			throw new InvalidParameterException("path '" + datasetRootPath + "' does not exists");

		final List<String> pathList = new LinkedList<String>();

		final FileFilter filterByDirectory = new FileFilter() {
			@Override
			public boolean accept(File pathname) {
				return pathname.isDirectory();
			}
		};

		for(File ff1 : f.listFiles(filterByDirectory)) {
			for(File ff2 : ff1.listFiles(filterByDirectory)) {
				for(File ff3 : ff2.listFiles(filterByDirectory)) {
					pathList.add(ff3.getPath());
				}
			}
		}
		
		return pathList;
	}
	
	/**
	 * 
	 * @param filepath
	 * @return
	 */
	private static final List<String> readFile(String filepath){

		final List<String> lines = new LinkedList<String>();

		final Path path = Path.of(filepath);

		try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) 
		{
			stream.forEach(s -> lines.add(s));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}

		return lines;
	}

	/**
	 * 	Format:
	 * 		R id, size(byte), deadline, <virtual link id>, type [TT, RC], priority, [period | rate] (us), [offset | ] [packed | fragmented]
	 * 
	 * 	Examples:
	 * 		tt1, 400, 58972.0, vl1, TT, 7, 250, 0.0
	 * 		tt2, 400, 114419.0, vl2, TT, 3, 250, 0.0
	 * 		tt3, 400, 56935.0, vl3, TT, 1, 250, 0.0
	 * 		tt4, 400, 170198.0, vl4, TT, 6, 250, 0.0
	 * 		tt5, 400, 34481.0, vl5, TT, 1, 250, 0.0
	 * 		tt6, 400, 11709.0, vl6, TT, 7, 250, 0.0
	 * 		tt7, 400, 352023.0, vl5, TT, 7, 250, 0.0
	 * 		tt8, 400, 23165.0, vl8, TT, 6, 250, 0.0
	 * 		tt9, 400, 35879.0, vl9, TT, 5, 250, 0.0
	 * 		tt10, 400, 8908.0, vl10, TT, 4, 250, 0.0
	 * 
	 * @param filepath
	 */
	public List<STMessageEntry> readScheduledMessageFile(){

		final String filepath = dirpath + "/msg.txt";
		final List<STMessageEntry> messages = new LinkedList<STMessageEntry>();

		final List<String> lines = readFile(filepath);

		for(String entryFromFile : lines) {
			if(!entryFromFile.startsWith("#"))
				messages.add(STMessageEntry.parse(entryFromFile));
		}

		return messages;

	}

	/**
	 * Example:
	 * 
	 * ES1.0,SW1.0
	 * 40	60	250	7
	 * 60	80	250	6
	 * 80	100	250	4
	 * 100	120	250	2
	 * 
	 * ES2.0,SW1.0
	 * 70	90	250	7
	 * 85	105	250	5
	 * 40	60	250	3
	 * 105	125	250	2
	 * 55	75	250	1
	 * 
	 * 
	 * @return	Map<String = Link Info,List<TASGateScheduleEntry>>
	 */
	public Map<InterfaceInfoEntry, Map<Integer,List<TASGateScheduleEntry2018>>> readGateSchedulingFile(){

		final String filepath = dirpath + "/historySCHED1.txt";

		//final Map<InterfaceInfo,List<TASGateScheduleEntry>> scheduling = new HashMap<InterfaceInfo,List<TASGateScheduleEntry>>();
		final Map<InterfaceInfoEntry, Map<Integer,List<TASGateScheduleEntry2018>>> mapInterfaceInfoPriorityListTASGateScheduleEntry = new HashMap<InterfaceInfoEntry, Map<Integer,List<TASGateScheduleEntry2018>>>();

		final List<String> lines = readFile(filepath);

		InterfaceInfoEntry interfaceInfo = null;
		for(String line : lines) {

			if(line.startsWith("#"))
				break;

			if(line.replaceAll("\\s+","").length() == 0) //It is a blank line
				continue;

			if(line.startsWith(";")) //Do not read lines which starts with ';'
				continue;

			if(line.contains(",")) { //it is a InterfaceInfo (e.g. "SW2.1,ES6.0")
				interfaceInfo = InterfaceInfoEntry.parse(line.replaceAll("\\s+",""));
				continue;
			}
			
			/*
			 * It creates a tasGateScheduleEntry and adds it to the map
			 */
			final TASGateScheduleEntry2018 tasGateScheduleEntry = TASGateScheduleEntry2018.parse(line);

			final Map<Integer,List<TASGateScheduleEntry2018>> mapList = mapInterfaceInfoPriorityListTASGateScheduleEntry.getOrDefault(interfaceInfo, new HashMap<Integer,List<TASGateScheduleEntry2018>>());
			final List<TASGateScheduleEntry2018> list = mapList.getOrDefault(tasGateScheduleEntry.priority, new LinkedList<TASGateScheduleEntry2018>());
			list.add(tasGateScheduleEntry);
			mapList.put(tasGateScheduleEntry.priority, list);
			mapInterfaceInfoPriorityListTASGateScheduleEntry.put(interfaceInfo, mapList);
			
		}

		/**
		 * Sanity check
		 * 
		 * Assert all TASGateScheduleEntry instances from the same priority and interface have equals periodLength
		 */
		for(Entry<InterfaceInfoEntry,Map<Integer,List<TASGateScheduleEntry2018>>> entry : mapInterfaceInfoPriorityListTASGateScheduleEntry.entrySet()) {

			for(Entry<Integer,List<TASGateScheduleEntry2018>> entry2 : entry.getValue().entrySet()) {

				final List<TASGateScheduleEntry2018> tasGateScheduleEntryList = entry2.getValue();
				final Set<Long> periodLengthSet = new HashSet<Long>();

				tasGateScheduleEntryList.forEach(tasGate -> { periodLengthSet.add(tasGate.periodLength); });

				if(periodLengthSet.size() != 1)
					throw new InvalidParameterException("All TASGateScheduleEntry instances from the same interface and priority must have the same periodLength value");	
			}
		}

		return mapInterfaceInfoPriorityListTASGateScheduleEntry;
	}

	public List<VirtualLinkEntry> readVirtualLinksFile(){

		final String filepath = dirpath + "/vls.txt";
		final List<VirtualLinkEntry> virtualLinks = new LinkedList<VirtualLinkEntry>();

		final List<String> lines = readFile(filepath);

		for(String line : lines) {
			if(!line.startsWith("#"))
				virtualLinks.add(VirtualLinkEntry.parse(line)); 
		}

		/**
		 * Sanity Check
		 */
		virtualLinks.forEach(vl -> {
			for(int i = 0 ; i < vl.route.size() - 1; i++) {
				if(
						!(Objects.equals(vl.route.get(i).getSecond().deviceName, vl.route.get(i + 1).getFirst().deviceName)) 
						||
						(Objects.equals(vl.route.get(i).getSecond(), vl.route.get(i + 1).getFirst())))
					throw new InvalidParameterException("Invalid virtual link route path:\n" + DataPrinterUtil.toString(vl));
			}
		});

		return virtualLinks;

	}

	/**
	 * 	Examples:
	 * 		ES1.0=1000BASE-TX
	 * 		ES1.0=10BASE-T1S;1
	 * 		ES1.0=10BASE-T1S;0:1,1:1,2:1,3:1,4:1,5:1,6:1,7:1
	 *
	 * @param filepath
	 */
	public Map<InterfaceInfoEntry, EthernetPhyStandard> readInterfacesTypesFile(){

		final String filepath = dirpath + "/interfaces.txt";
		final Map<InterfaceInfoEntry, EthernetPhyStandard> mapInterfaceInfoPhyStandard = new HashMap<InterfaceInfoEntry, EthernetPhyStandard>();

		final List<String> lines = readFile(filepath);

		for(String line : lines) {
			if(line.startsWith("#"))
				continue;
			if(line.replaceAll("\\s+","").length() == 0) //It is a blank line
				continue;

			final String[] partsKeyValue = line.split("=");
			final String[] partsValue = partsKeyValue[1].split(";");

			final InterfaceInfoEntry interfaceInfo = InterfaceInfoEntry.parse(partsKeyValue[0]);
			final EthernetPhyStandard ethernetPhyStandard = EthernetPhyStandard.findByName(partsValue[0]);

			if(ethernetPhyStandard == null)
				throw new InvalidParameterException("Invalid Interface standard: " + partsValue[0]);
			mapInterfaceInfoPhyStandard.put(interfaceInfo, ethernetPhyStandard);

		}

		return mapInterfaceInfoPhyStandard;
	}

	/**
	 * 	Examples:
	 * 		ES1.0=1000BASE-TX
	 * 		ES1.0=10BASE-T1S;1
	 * 		ES1.0=10BASE-T1S;0:1,1:1,2:1,3:1,4:1,5:1,6:1,7:1
	 *
	 * @param filepath
	 */
	public Map<InterfaceInfoEntry, Map<Integer,Integer>> readInterfacesPLCAWeightsWRRFile(){

		final String filepath = dirpath + "/interfaces.txt";
		final Map<InterfaceInfoEntry, Map<Integer,Integer>> mapInterfaceInfoPriorityPLCAWeightWRR = new HashMap<InterfaceInfoEntry, Map<Integer,Integer>>();

		final List<String> lines = readFile(filepath);

		for(String line : lines) {
			if(line.startsWith("#"))
				continue;
			if(line.replaceAll("\\s+","").length() == 0) //It is a blank line
				continue;

			final String[] partsKeyValue = line.split("=");
			final String[] partsValue = partsKeyValue[1].split(";");

			final InterfaceInfoEntry interfaceInfo = InterfaceInfoEntry.parse(partsKeyValue[0]);

			final Map<Integer,Integer> mapPriorityPlcaWRR = new HashMap<Integer, Integer>();

			//If it has information related to PLCA
			if(partsValue.length == 2) {

				//It is "separated PLCA server"
				if(partsValue[1].contains(",")) {
					final String[] partsPlcaWRR = partsValue[1].split(",");

					for(String pairPrioWRR: partsPlcaWRR) {

						final String[] pairPrioWRRArray = pairPrioWRR.split(":");
						final int prio = Integer.parseInt(pairPrioWRRArray[0]);
						final int plcaWRR = Integer.parseInt(pairPrioWRRArray[1]);

						mapPriorityPlcaWRR.put(prio, plcaWRR);
					}

				} 
				//It is is "one PLCA server"
				else {

					final int plcaWRR = Integer.parseInt(partsValue[1]);
					mapPriorityPlcaWRR.put(TASWindowsBuilder.KEY_PHY_NO_PRIORITY, plcaWRR);
				}
			}

			mapInterfaceInfoPriorityPLCAWeightWRR.put(interfaceInfo, mapPriorityPlcaWRR);

		}

		return mapInterfaceInfoPriorityPLCAWeightWRR;
	}

	/**
	 * 
	 * @param entryList
	 * 
	 * @return	Map<Integer = Priority,List<TASGateScheduleEntry>>
	 */
//	private final Map<Integer,List<TASGateScheduleEntry>> mapByPriority(List<TASGateScheduleEntry> entryList){
//
//		final Map<Integer,List<TASGateScheduleEntry>> mapPriorityGateScheduleList = new HashMap<Integer, List<TASGateScheduleEntry>>();
//
//		for(TASGateScheduleEntry entry : entryList) {
//
//			final int priority = entry.priority;
//			final List<TASGateScheduleEntry> list = mapPriorityGateScheduleList.getOrDefault(priority, new LinkedList<TASGateScheduleEntry>());
//
//			list.add(entry);
//			mapPriorityGateScheduleList.put(priority, list);
//		}
//
//		return mapPriorityGateScheduleList;
//	}

	public final String getDirpath() {
		return dirpath;
	}

	public final String getResultAbsolutePath() {

		final String strAbsolutePath = "results/" + dirpath.replace("/", "_").replace("\\", "_").replace(" ", "_");
		final File fileDirectory = new File(strAbsolutePath);

		if(!fileDirectory.exists()) {
			fileDirectory.mkdirs();	
		}

		return strAbsolutePath;
	}
	

}
