package org.networkcalculus.dnc.ethernet.tsn;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.ServiceCurve;
import org.networkcalculus.dnc.ethernet.EthernetPhyStandard;
import org.networkcalculus.dnc.ethernet.plca.PlcaServerData;
import org.networkcalculus.dnc.ethernet.tsn.data.DatasetReader;
import org.networkcalculus.dnc.ethernet.tsn.data.STFlowData;
import org.networkcalculus.dnc.ethernet.tsn.data.STServerData;
import org.networkcalculus.dnc.ethernet.tsn.entry.InterfaceInfoEntry;
import org.networkcalculus.dnc.ethernet.tsn.entry.STMessageEntry;
import org.networkcalculus.dnc.ethernet.tsn.entry.TASGateScheduleEntry2018;
import org.networkcalculus.dnc.ethernet.tsn.entry.VirtualLinkEntry;
import org.networkcalculus.dnc.ethernet.tsn.model.LinkInfo;
import org.networkcalculus.dnc.ethernet.tsn.model.TASWindow;
import org.networkcalculus.dnc.ethernet.utils.ChartUtilsTASWindow;
import org.networkcalculus.dnc.ethernet.utils.DataPrinterUtil;
import org.networkcalculus.dnc.ethernet.utils.ExcelSheetTASWindow;
import org.networkcalculus.dnc.ethernet.utils.NumberUtil;
import org.networkcalculus.dnc.ethernet.utils.PortGuaranteedWindowsExporter;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 *
 */
public class TASWindowsBuilder {

	//	@ARTICLE{ieee_802.3cg:2019,
	//	  author={{IEEE}},
	//	  journal={IEEE Std 802.3cg-2019}, 
	//	  title={{IEEE Standard for Ethernet - Amendment 5: Physical Layer Specifications and Management Parameters for 10 Mb/s Operation and Associated Power Delivery over a Single Balanced Pair of Conductors}}, 
	//	  year={2020},
	//	  volume={},
	//	  number={},
	//	  doi={10.1109/IEEESTD.2020.8982251}
	//	}

	//	@book{automotive_ethernet_book_kirsten_matheus:2021,
	//		  title={Automotive Ethernet},
	//		  author={Matheus, Kirsten and K{\"o}nigseder, Thomas},
	//		  year={2021},
	//		  place={Cambridge},
	//		  edition={3},
	//		  DOI={10.1017/9781108895248},
	//		  publisher={Cambridge University Press}
	//		}

	/**
	 * Used for replacing the priority value when there is a single PLCA Server,
	 * 	i.e. Single PLCA WRR Server modeling
	 */
	public static final int KEY_PHY_NO_PRIORITY = -1;

	/**
	 * Got from msg.txt
	 */
	private List<STMessageEntry> messages;

	/**
	 * Got from historySCHED1.txt
	 */
	private Map<InterfaceInfoEntry,Map<Integer,List<TASGateScheduleEntry2018>>> mapInterfaceInfoPriorityListTASGateScheduleEntry;

	/**
	 * Got from vls.txt
	 */
	private List<VirtualLinkEntry> virtualLinks;


	private Map<InterfaceInfoEntry,LinkInfo> mapInterfaceInfoLinkInfo;

	private Map<String,List<STFlowData>> mapVirtualLinkSTFlowDataList;

	private Map<STFlowData,VirtualLinkEntry> mapSTFlowDataVirtualLink;

	private Map<InterfaceInfoEntry,Map<Integer,List<STFlowData>>> mapInterfaceInfoPrioritySTFlowDataList;

	/**
	 * Map<InterfaceInfo, Map<Integer = Priority, Pair<Double = MaxMessageSize,Double = MinMessageSize>>>
	 */
	private Map<InterfaceInfoEntry,Map<Integer,Pair<Double,Double>>> mapInterfaceInfoPriorityMaxMinMessageSizeBytes;

	/**
	 * Got from interfaces.txt
	 * File created by David Alain do Nascimento
	 */
	private Map<InterfaceInfoEntry,EthernetPhyStandard> mapInterfaceInfoPhyStandard;

	/**
	 * Map<InterfaceInfo,Map<Integer = Priority, Integer = PLCA Weight WRR>>
	 */
	private Map<InterfaceInfoEntry,Map<Integer,Integer>> mapInterfaceInfoPriorityPLCAWeightWRR;

	private Map<InterfaceInfoEntry,Map<Integer,STServerData>> mapInterfaceInfoPrioritySTServerData;

	private Map<InterfaceInfoEntry,Map<Integer,PlcaServerData>> mapInterfaceInfoPriorityPLCAServerData;

	private Map<InterfaceInfoEntry,Map<Integer, List<TASWindow>>> mapInterfaceInfoPriorityTasWindowDataList;

	private Map<String,STFlowData> mapFlowNameSTFlowData;


	private Map<TASWindow,Map<Integer,List<TASWindow>>> mapTASWindowPriorityTASWindowListCollisions;


	private final DatasetReader datasetReader;
	private final ExecutionConfig executionConfig;

	public TASWindowsBuilder(DatasetReader networkConfigReader, ExecutionConfig executionConfig) {

		this.datasetReader = networkConfigReader;
		this.executionConfig = executionConfig;
	}


	public void build() {

		this.messages = datasetReader.readScheduledMessageFile();
		this.mapInterfaceInfoPriorityListTASGateScheduleEntry = datasetReader.readGateSchedulingFile();
		this.virtualLinks = datasetReader.readVirtualLinksFile();
		this.mapInterfaceInfoPhyStandard = datasetReader.readInterfacesTypesFile();
		this.mapInterfaceInfoPriorityPLCAWeightWRR = datasetReader.readInterfacesPLCAWeightsWRRFile();

		/**
		 * Assert all attributes are non-null
		 */
		Objects.requireNonNull(this.messages);
		Objects.requireNonNull(this.mapInterfaceInfoPriorityListTASGateScheduleEntry);
		Objects.requireNonNull(this.virtualLinks);

		this.buildStep0_mapInterfaceInfoLinkInfo();
		this.buildStep1_mapFlowNameSTFlowData();
		this.buildStep2_mapVirtualLinkSTFlowDataList();
		this.buildStep4_mapSTFlowDataVirtualLink();
		this.buildStep5_mapInterfaceInfoPrioritySTFlowDataList();
		this.buildStep6_mapInterfaceInfoPriorityMaxMinMessageSize();

		this.buildStep7_mapInterfaceInfoPrioritySTServerData();
		this.buildStep8_mapInterfaceInfoPLCAServerData();


		Objects.requireNonNull(this.mapInterfaceInfoLinkInfo);
		Objects.requireNonNull(this.mapFlowNameSTFlowData);
		Objects.requireNonNull(this.mapVirtualLinkSTFlowDataList);
		Objects.requireNonNull(this.mapInterfaceInfoPhyStandard);
		Objects.requireNonNull(this.mapInterfaceInfoPriorityPLCAWeightWRR);
		Objects.requireNonNull(this.mapSTFlowDataVirtualLink);
		Objects.requireNonNull(this.mapInterfaceInfoPrioritySTFlowDataList);
		Objects.requireNonNull(this.mapInterfaceInfoPriorityMaxMinMessageSizeBytes);
		Objects.requireNonNull(this.mapInterfaceInfoPrioritySTServerData);
		Objects.requireNonNull(this.mapInterfaceInfoPriorityPLCAServerData);

		Objects.requireNonNull(this.mapInterfaceInfoPriorityTasWindowDataList);
		Objects.requireNonNull(this.mapTASWindowPriorityTASWindowListCollisions);

	}

	/**
	 * @return the networkConfigReader
	 */
	public final DatasetReader getDatasetReader() {
		return datasetReader;
	}


	/**
	 * @return the executionConfig
	 */
	public final ExecutionConfig getExecutionConfig() {
		return executionConfig;
	}

	public final List<STMessageEntry> getMessages() {
		return messages;
	}
	public final List<VirtualLinkEntry> getVirtualLinks() {
		return virtualLinks;
	}
	public final Map<InterfaceInfoEntry, LinkInfo> getMapInterfaceInfoLinkInfo() {
		return mapInterfaceInfoLinkInfo;
	}
	public final Map<String, List<STFlowData>> getMapVirtualLinkSTFlowDataList() {
		return mapVirtualLinkSTFlowDataList;
	}
	public final Map<STFlowData, VirtualLinkEntry> getMapSTFlowDataVirtualLink() {
		return mapSTFlowDataVirtualLink;
	}
	public final Map<InterfaceInfoEntry, Map<Integer, List<STFlowData>>> getMapInterfaceInfoPrioritySTFlowDataList() {
		return mapInterfaceInfoPrioritySTFlowDataList;
	}
	public final Map<InterfaceInfoEntry, Map<Integer, Pair<Double, Double>>> getMapInterfaceInfoPriorityMaxMinMessageSize() {
		return mapInterfaceInfoPriorityMaxMinMessageSizeBytes;
	}
	public final Map<InterfaceInfoEntry, EthernetPhyStandard> getMapInterfaceInfoPhyStandard() {
		return mapInterfaceInfoPhyStandard;
	}
	public final Map<InterfaceInfoEntry, Map<Integer,Integer>> getMapInterfaceInfoPriorityPLCAWeightWRR() {
		return mapInterfaceInfoPriorityPLCAWeightWRR;
	}
	public final Map<String, STFlowData> getMapFlowNameSTFlowData() {
		return mapFlowNameSTFlowData;
	}
	public final Map<InterfaceInfoEntry, Map<Integer, STServerData>> getMapInterfaceInfoPrioritySTServerData() {
		return mapInterfaceInfoPrioritySTServerData;
	}
	public final Map<InterfaceInfoEntry, Map<Integer, PlcaServerData>> getMapInterfaceInfoPriorityPLCAServerData() {
		return mapInterfaceInfoPriorityPLCAServerData;
	}
	public final Map<InterfaceInfoEntry, Map<Integer, List<TASWindow>>> getMapInterfaceInfoPriorityTasWindowList() {
		return mapInterfaceInfoPriorityTasWindowDataList;
	}

	public final VirtualLinkEntry findVirtualLinkByName(String virtualLinkName) {

		Objects.requireNonNull(this.virtualLinks, "this.virtualLinks cannot be null");

		for(VirtualLinkEntry virtualLinkEntry : this.virtualLinks) {
			if(Objects.equals(virtualLinkEntry.name, virtualLinkName))
				return virtualLinkEntry;
		}

		throw new InvalidParameterException("There no VirtualLink with name: "+virtualLinkName);
	}

	/**************************************************************************************************************************
	 * 
	 * 
	 * 
	 **************************************************************************************************************************/

	private void buildStep0_mapInterfaceInfoLinkInfo() {

		Objects.requireNonNull(this.mapInterfaceInfoPriorityListTASGateScheduleEntry, "this.mapInterfaceInfoPriorityListTASGateScheduleEntry cannot be null");
		Objects.requireNonNull(this.virtualLinks, "this.virtualLinks cannot be null");

		this.mapInterfaceInfoLinkInfo = new HashMap<InterfaceInfoEntry, LinkInfo>();

		for(VirtualLinkEntry entry : this.virtualLinks) {

			for(Pair<InterfaceInfoEntry,InterfaceInfoEntry> pair : entry.route) {

				final InterfaceInfoEntry interfaceInfoA = pair.getFirst();
				final InterfaceInfoEntry interfaceInfoB = pair.getSecond();

				final LinkInfo linkInfoA = mapInterfaceInfoLinkInfo.get(interfaceInfoA);
				final LinkInfo linkInfoB = mapInterfaceInfoLinkInfo.get(interfaceInfoB);

				LinkInfo target = null;

				if(linkInfoA == null && linkInfoB == null) {
					target = new LinkInfo(interfaceInfoA, interfaceInfoB);
				} else {
					Set<InterfaceInfoEntry> targetInterfacesSet = new HashSet<InterfaceInfoEntry>();

					targetInterfacesSet.add(interfaceInfoA);
					targetInterfacesSet.add(interfaceInfoB);

					if(linkInfoA != null)
						targetInterfacesSet.addAll(linkInfoA.getInterfaceInfoSet());

					if(linkInfoB != null)
						targetInterfacesSet.addAll(linkInfoB.getInterfaceInfoSet());

					target = new LinkInfo(targetInterfacesSet);
				}

				this.mapInterfaceInfoLinkInfo.put(interfaceInfoA, target);
				this.mapInterfaceInfoLinkInfo.put(interfaceInfoB, target);

			}
		}

		/**
		 * Sanity check
		 */
		final Set<InterfaceInfoEntry> diff = new HashSet<InterfaceInfoEntry>();

		diff.addAll(this.mapInterfaceInfoPriorityListTASGateScheduleEntry.keySet());
		diff.removeAll(this.mapInterfaceInfoLinkInfo.keySet());

		if(!diff.isEmpty()) {
			DataPrinterUtil.print(diff);
			throw new InvalidParameterException("There are InterfaceInfo instances without link to another one.");
		}

	}

	private void buildStep1_mapFlowNameSTFlowData() {

		Objects.requireNonNull(this.messages, "this.messages cannot be null");

		this.mapFlowNameSTFlowData = new HashMap<String, STFlowData>();

		/**
		 * Correlate STMessageEntry to its proper STFlowData instance
		 */
		for(STMessageEntry message : this.messages) {
			final STFlowData stFlowData = new STFlowData(message);
			final ArrivalCurve arrivalCurve = TASCurveBuilder.getInstance().buildSTArrivalCurve(stFlowData);
			stFlowData.setArrivalCurve(arrivalCurve);

			this.mapFlowNameSTFlowData.put(message.flowName, stFlowData);
		}

	}

	/**
	 * 
	 * @param allMessages
	 * 
	 * @return	Map<String = VirtualLinkName,STFlowData>
	 */
	private void buildStep2_mapVirtualLinkSTFlowDataList(){

		Objects.requireNonNull(this.mapFlowNameSTFlowData, "this.mapFlowNameTSNFlowData cannot be null");

		this.mapVirtualLinkSTFlowDataList = new HashMap<String,List<STFlowData>>();

		/**
		 * Build mapVirtualLinkSTFlowDataList
		 */
		for(Entry<String, STFlowData> entry: this.mapFlowNameSTFlowData.entrySet()) {

			final String virtualLinkName = entry.getValue().getSTMessageEntry().virtualLinkName;

			final List<STFlowData> stFlowDataList = mapVirtualLinkSTFlowDataList.getOrDefault(virtualLinkName, new LinkedList<STFlowData>());

			stFlowDataList.add(entry.getValue());

			this.mapVirtualLinkSTFlowDataList.put(virtualLinkName, stFlowDataList);
		}

	}

	private void buildStep4_mapSTFlowDataVirtualLink(){

		Objects.requireNonNull(this.mapFlowNameSTFlowData, "this.mapFlowNameSTFlowData cannot be null");

		this.mapSTFlowDataVirtualLink = new HashMap<STFlowData, VirtualLinkEntry>();

		for(Entry<String,STFlowData> entry : this.mapFlowNameSTFlowData.entrySet()) {

			final STFlowData stFlowData = entry.getValue();
			final VirtualLinkEntry virtualLinkEntry = this.findVirtualLinkByName(stFlowData.getSTMessageEntry().virtualLinkName);

			this.mapSTFlowDataVirtualLink.put(stFlowData, virtualLinkEntry);
		}

	}

	/**
	 * 
	 * @param mapPriorityStFlowDataList
	 * 
	 * @return	Map<InterfaceInfo, Map<Integer = Priority, List<STFlowData>>>
	 */
	private void buildStep5_mapInterfaceInfoPrioritySTFlowDataList(){

		Objects.requireNonNull(this.mapFlowNameSTFlowData, "this.mapFlowNameSTFlowData cannot be null");

		this.mapInterfaceInfoPrioritySTFlowDataList = new HashMap<InterfaceInfoEntry,Map<Integer,List<STFlowData>>>();

		for(Entry<String,STFlowData> entry : this.mapFlowNameSTFlowData.entrySet()) {

			final STFlowData stFlowData = entry.getValue();
			final int priority = stFlowData.getSTMessageEntry().priority;

			final VirtualLinkEntry virtualLinkEntry = this.findVirtualLinkByName(stFlowData.getSTMessageEntry().virtualLinkName);

			//Iterate by all InterfaceInfo got from virtual link routes
			for(Pair<InterfaceInfoEntry,InterfaceInfoEntry> pair : virtualLinkEntry.route) {

				final InterfaceInfoEntry outputInterfaceInfo = pair.getFirst();
				final Map<Integer,List<STFlowData>> mapPrioritySTFlowDataList = 
						this.mapInterfaceInfoPrioritySTFlowDataList.getOrDefault(outputInterfaceInfo, new HashMap<Integer,List<STFlowData>>());

				final List<STFlowData> stFlowDataListByPriority = mapPrioritySTFlowDataList.getOrDefault(priority, new LinkedList<STFlowData>());
				stFlowDataListByPriority.add(stFlowData);

				mapPrioritySTFlowDataList.put(priority, stFlowDataListByPriority);

				this.mapInterfaceInfoPrioritySTFlowDataList.put(outputInterfaceInfo, mapPrioritySTFlowDataList);
			}

		}

	}

	private void buildStep6_mapInterfaceInfoPriorityMaxMinMessageSize(){

		this.mapInterfaceInfoPriorityMaxMinMessageSizeBytes = new HashMap<InterfaceInfoEntry, Map<Integer,Pair<Double,Double>>>();

		/**
		 * Iterate all flows to get max and min message sizes for each priority.
		 */
		for(Entry<InterfaceInfoEntry,Map<Integer,List<STFlowData>>> entry : this.mapInterfaceInfoPrioritySTFlowDataList.entrySet()) {

			final InterfaceInfoEntry interfaceInfo = entry.getKey();
			final Map<Integer,List<STFlowData>> mapPrioritySTFlowDataList = entry.getValue();

			final Map<Integer,Pair<Double,Double>> mapPriorityMaxMinMessageSizeBytes = 
					this.mapInterfaceInfoPriorityMaxMinMessageSizeBytes.getOrDefault(interfaceInfo, new HashMap<Integer,Pair<Double,Double>>());

			for(Entry<Integer,List<STFlowData>> entry2 : mapPrioritySTFlowDataList.entrySet()) {

				final int priority = entry2.getKey();
				final List<STFlowData> stFlowDataList = entry2.getValue();

				if(stFlowDataList.isEmpty())
					continue;

				final Set<Double> messageMaxSizeBytesSet = new HashSet<>();
				final Set<Double> messageMinSizeBytesSet = new HashSet<>();

				stFlowDataList.forEach(data -> {
					messageMaxSizeBytesSet.add(data.getMessageMaxSizeBytes());
					messageMinSizeBytesSet.add(data.getMessageMinSizeBytes());
				});

				final double l_max_Pm = NumberUtil.max(messageMaxSizeBytesSet);
				final double l_min_Pm = NumberUtil.max(messageMinSizeBytesSet);

				mapPriorityMaxMinMessageSizeBytes.put(priority, new Pair<Double,Double>(l_max_Pm, l_min_Pm));
			}

			this.mapInterfaceInfoPriorityMaxMinMessageSizeBytes.put(interfaceInfo, mapPriorityMaxMinMessageSizeBytes);
		}

	}

	/**
	 * A Service Curve for ST traffic in TSN Priority Queues is composed by a sum of many TDMA (staircase) curves [Zhao, 2020,p.7-8].
	 * We are using a linear approximation (Rate-Latency) of TDMA (staircase) based on [Gollan, 2007, p.4].
	 * 
	 * [Zhao, 2018] Zhao, L., Pop, P., & Craciunas, S. S. (2018). Worst-case latency analysis for IEEE 802.1 Qbv time sensitive networks using network calculus. Ieee Access, 6, 41803-41815.
	 * [Gollan, 2007] Gollan, Nicos, and Jens Schmitt. On the TDMA Design Problem Under Real-Time Constraints in Wireless Sensor Networks. Technical Report 359/07, University of Kaiserslautern, Germany, 2007.
	 * 
	 * @return	Map<EthernetInterface,Map<Integer,STServerData>>
	 * @throws Exception 
	 */
	private void buildStep7_mapInterfaceInfoPrioritySTServerData(){

		//		System.out.println("buildStep7_mapInterfaceInfoPrioritySTServerData()");

		Objects.requireNonNull(this.mapInterfaceInfoPriorityListTASGateScheduleEntry, "this.mapInterfaceInfoPriorityListTASGateScheduleEntry cannot be null");

		this.mapInterfaceInfoPrioritySTServerData = new HashMap<InterfaceInfoEntry, Map<Integer,STServerData>>();
		this.mapInterfaceInfoPriorityTasWindowDataList = new HashMap<InterfaceInfoEntry, Map<Integer, List<TASWindow>>>();

		//		System.out.println("Interfaces vs Priority vs Max Frame Size vs Min Frame Size:");
		//		System.out.println(PrinterUtil.toString(this.mapInterfaceInfoPriorityMaxMinMessageSizeBytes));

		//Iterate for each EthernetInterface of all devices in the whole network
		for(final InterfaceInfoEntry interfaceInfo : this.mapInterfaceInfoPriorityListTASGateScheduleEntry.keySet()) {

			//			System.out.println();
			//			System.out.println("interfaceInfo="+interfaceInfo.toStringInfo());

			this.mapInterfaceInfoPrioritySTServerData.put(interfaceInfo, new HashMap<Integer, STServerData>());

			/**
			 * Calculate all TASWindow from current interface and aggregate them by priority.
			 * 
			 * Note: Zhao's DataSet has only a single TASWindow for each used priority and not all priorities are being used.
			 */
			final Map<Integer,List<TASWindow>> mapPriorityTASWindowTermsList = calculateTASWindowList(interfaceInfo);

			this.mapInterfaceInfoPriorityTasWindowDataList.put(interfaceInfo, mapPriorityTASWindowTermsList);

			/**
			 * Iterate over all List<TASWindow> from each priority and build related ServiceCurve and STServerData
			 */
			for(Entry<Integer,List<TASWindow>> entryPriorityTASWindowTermsList : mapPriorityTASWindowTermsList.entrySet()) {

				final Integer priority = entryPriorityTASWindowTermsList.getKey();
				final List<TASWindow> tasWindowList = entryPriorityTASWindowTermsList.getValue();

				//				System.out.println();
				//				System.out.println("priority="+priority);
				//				System.out.println("buildSTRateLatencyServiceCurve()");

				final ServiceCurve stServiceCurve = TASCurveBuilder.getInstance().buildSTRateLatencyServiceCurve(this.mapInterfaceInfoPhyStandard.get(interfaceInfo), tasWindowList, this.executionConfig.plcaModeling);
				final STServerData stServerData = new STServerData(stServiceCurve, tasWindowList);

				this.mapInterfaceInfoPrioritySTServerData.get(interfaceInfo).put(priority, stServerData);
			}

		}

		if(this.executionConfig.generateTASWindowsExcelSheet == ExecutionConfig.GenerateTASWindowsExcelSheet.ALL) {
			final ExcelSheetTASWindow excelSheetUtil = new ExcelSheetTASWindow(this.datasetReader.getResultAbsolutePath());
			excelSheetUtil.buildSheetWriteFile(mapInterfaceInfoPriorityTasWindowDataList);

			switch(this.executionConfig.generatePortGuaranteedWinFiles) {
			case ALL:
			{
				new PortGuaranteedWindowsExporter(this.datasetReader.getResultAbsolutePath())
				.writeFileAsZhaosResults(mapInterfaceInfoPriorityTasWindowDataList);

			}
			break;
			
			case NO:
			default:
				break;
			}

		}

	}

	private final void buildStep8_mapInterfaceInfoPLCAServerData(){

		Objects.requireNonNull(this.mapInterfaceInfoPrioritySTServerData, "this.mapInterfaceInfoPrioritySTServerData cannot be null");

		this.mapInterfaceInfoPriorityPLCAServerData = new HashMap<InterfaceInfoEntry,Map<Integer,PlcaServerData>>();

		for(Entry<InterfaceInfoEntry, LinkInfo> entry : this.mapInterfaceInfoLinkInfo.entrySet()) {

			final InterfaceInfoEntry interfaceInfo = entry.getKey();
			final EthernetPhyStandard phyStandard = this.mapInterfaceInfoPhyStandard.get(interfaceInfo);

			if(phyStandard == null) {
				System.out.println("------------------ Error ----------------------");
				DataPrinterUtil.print(this.mapInterfaceInfoPhyStandard);
				System.out.println("-----------------------------------------------");
				throw new InvalidParameterException("It is required to have an EthernetPhyStandard for this interface: " + interfaceInfo);
			}

			if((this.mapInterfaceInfoPriorityPLCAWeightWRR.get(interfaceInfo).isEmpty()) && (phyStandard.isDedicatedMediumAccess == false))
				throw new InvalidParameterException("It is required to have a PlcaWeightWRR value for an interface with isDedicatedMediumAccess=false");

			/*
			 * If an interface does not have a plcaWeightWRR stored in map, then it shall not be a shared medium access interface
			 */
			if((this.mapInterfaceInfoPriorityPLCAWeightWRR.get(interfaceInfo).isEmpty()) && (phyStandard.isDedicatedMediumAccess == true))
				continue;


			final Map<Integer,PlcaServerData> mapPriorityPLCAServerData = new HashMap<Integer, PlcaServerData>();
			final Integer weightSinglePlcaWrrServer = this.mapInterfaceInfoPriorityPLCAWeightWRR.get(interfaceInfo).get(TASWindowsBuilder.KEY_PHY_NO_PRIORITY);

			/**
			 * Single PLCA WRR Server modeling
			 */
			if(weightSinglePlcaWrrServer != null) {

				final List<STFlowData> listSTFlowDataAllPriorities = new LinkedList<STFlowData>();

				final Map<Integer, List<STFlowData>> mapPrioritySTFlowDataList = this.mapInterfaceInfoPrioritySTFlowDataList.get(interfaceInfo);
				if(mapPrioritySTFlowDataList != null)
					mapPrioritySTFlowDataList.forEach((k,v) -> listSTFlowDataAllPriorities.addAll(v));

				final PlcaServerData plcaServerData = new PlcaServerData(weightSinglePlcaWrrServer, listSTFlowDataAllPriorities);

				mapPriorityPLCAServerData.put(TASWindowsBuilder.KEY_PHY_NO_PRIORITY, plcaServerData);

			}
			/**
			 * Separated PLCA WRR Server modeling
			 */
			else {

				for(Entry<Integer,Integer> entryPrioPlcaWRR : this.mapInterfaceInfoPriorityPLCAWeightWRR.get(interfaceInfo).entrySet()) {

					final int priority = entryPrioPlcaWRR.getKey();
					final int plcaWeightWRR = entryPrioPlcaWRR.getValue();

					final Map<Integer, List<STFlowData>> mapPrioritySTFlowDataList = this.mapInterfaceInfoPrioritySTFlowDataList.get(interfaceInfo);

					final List<STFlowData> listStFlowData;
					if(mapPrioritySTFlowDataList != null)
						listStFlowData = mapPrioritySTFlowDataList.getOrDefault(priority, new LinkedList<STFlowData>());
					else
						listStFlowData = new LinkedList<STFlowData>();

					final PlcaServerData plcaServerData = new PlcaServerData(plcaWeightWRR, listStFlowData);

					mapPriorityPLCAServerData.put(priority, plcaServerData);
				}

			}

			this.mapInterfaceInfoPriorityPLCAServerData.put(interfaceInfo, mapPriorityPLCAServerData);
		}

	}

	/**
	 * Calculate all required terms. Convert List<TASGateScheduleEntry>> to List<TASWindow> for each priority.
	 * 
	 * @param interfaceInfo
	 * @return
	 */
	public Map<Integer,List<TASWindow>> calculateTASWindowList(InterfaceInfoEntry interfaceInfo){

		final Map<Integer,List<TASWindow>> mapOut = new HashMap<Integer, List<TASWindow>>();

		calculateTASWindowListStep1(interfaceInfo, mapOut);

		/** The following methods with suffix "_Wrong" was implemented based in Zhao's paper equations, but those equations are possibly not completely correct, or
		 * they can lead to a misinterpretation of equations due to the partial definition of some equations**/
		//		calculateTASWindowListStep2_Wrong(interfaceInfo, mapOut);
		//		calculateTASWindowListStep3_Wrong(interfaceInfo, mapOut);
		//		calculateTASWindowListStep4_Wrong(interfaceInfo, mapOut);
		//		calculateTASWindowListStep5_Wrong(interfaceInfo, mapOut);

		/** The following method with suffix "_Correct" is an alternative and correct implementation made by David Alain do Nascimento for the incorrect equations from Zhao's paper. **/
		calculateTASWindowListStep2_3_4_5_Correct(interfaceInfo, mapOut);

		calculateTASWindowListStep6(interfaceInfo, mapOut);
		calculateTASWindowListStep7(interfaceInfo, mapOut);
		calculateTASWindowListStep8(interfaceInfo, mapOut);
		calculateTASWindowListStep9(interfaceInfo, mapOut);

		calculateTASWindowListStep10(interfaceInfo, mapOut);

		return mapOut;
	}

	/**
	 * Step1: Max and Min frame size and other initial values from STFlowData
	 * 
	 * Calculate:
	 * 	t_oi_Pm,
	 * 	t_ci_Pm,
	 * 	l_max_Pm,
	 * 	l_min_Pm,
	 * 	d_gb_Pm (Equation 11), and
	 * 	t_gbi_Pm (Equation 12)
	 * 
	 * 	Note: Equations 11 and 12 do not depends on other values and can be calculated at the beginning of process. 
	 * 
	 * @param interfaceInfo
	 * @param mapPriorityTasWindowList
	 * 
	 * @return
	 */
	private void calculateTASWindowListStep1(final InterfaceInfoEntry interfaceInfo, final Map<Integer, List<TASWindow>> mapPriorityTasWindowList) {

		//System.out.println("calculateTASWindowListStep1()");
		//		System.out.println("interfaceInfo="+interfaceInfo);

		final EthernetPhyStandard phyStandard = this.mapInterfaceInfoPhyStandard.get(interfaceInfo);

		/**
		 * Map < Priority, TASWindow quantity within a sub-cycle T_Pm >
		 * 
		 *  Note: do not confuse with N_Pm. N_Pm is the TASWindow quantity within a T_GCL.
		 */
		final Map<Integer,Integer> mapPriorityTASWindowQuantity = new HashMap<Integer,Integer>();

		/**
		 * Iterate all TASGateScheduleEntry to build related TASWindow.
		 */
		for(Entry<Integer,List<TASGateScheduleEntry2018>> entryTasGateSchedule : this.mapInterfaceInfoPriorityListTASGateScheduleEntry.get(interfaceInfo).entrySet()) {

			/**
			 * All calculations here are related to priority Pm.
			 */
			final int priorityPm = entryTasGateSchedule.getKey();
			/**
			 * Creating a new list to be able for sorting it
			 */
			final List<TASGateScheduleEntry2018> tasGateScheduleEntryList = new LinkedList<TASGateScheduleEntry2018>(entryTasGateSchedule.getValue());

			final List<TASWindow> tasWindowList = new LinkedList<TASWindow>();


			final Map<Integer,Pair<Double,Double>> mapPriorityMaxMinMessageSizeBytes = 
					this.mapInterfaceInfoPriorityMaxMinMessageSizeBytes.getOrDefault(interfaceInfo, 
							new HashMap<Integer, Pair<Double,Double>>()); //Default value when there is no flow with priority Pm
			final Pair<Double,Double> maxMinMessageSizeBytes = 
					mapPriorityMaxMinMessageSizeBytes.getOrDefault(priorityPm, 
							new Pair<Double,Double>(0.0 , 0.0)); //Default value when there is no flow with priority Pm

			final double l_max_Pm = maxMinMessageSizeBytes.getFirst();
			final double l_min_Pm = maxMinMessageSizeBytes.getSecond();

			/**
			 * Sorting by openTime to create the correct index values accordingly to the window position
			 */
			tasGateScheduleEntryList.sort((TASGateScheduleEntry2018 o1, TASGateScheduleEntry2018 o2) -> Double.compare(o1.openTime, o2.openTime));

			int index = 0;
			for(TASGateScheduleEntry2018 tasGateScheduleEntry : tasGateScheduleEntryList) {

				final TASWindow tasWindow = new TASWindow();

				tasWindow.index = index++;
				tasWindow.priorityPm = priorityPm;
				tasWindow.T_Pm = tasGateScheduleEntry.periodLength;			//in microseconds (us)
				tasWindow.t_oi_Pm = tasGateScheduleEntry.openTime;			//in microseconds (us)
				tasWindow.t_ci_Pm = tasGateScheduleEntry.closeTime;			//in microseconds (us)
				tasWindow.l_max_Pm = l_max_Pm;								//in bytes
				tasWindow.l_min_Pm = l_min_Pm;								//in bytes

				/**
				 * Calculate d_gb_Pm (Equation 11)
				 */
				tasWindow.d_gb_Pm = (tasWindow.l_max_Pm * 8.0) / phyStandard.rate_bpus;	//in microseconds (us)

				/**
				 * Calculate t_gbi_Pm (Equation 12)
				 */
				tasWindow.t_gbi_Pm = tasWindow.t_ci_Pm - tasWindow.d_gb_Pm;	//in microseconds (us)

				tasWindowList.add(tasWindow);

				int N_Pm_within_T_Pm = 1 + mapPriorityTASWindowQuantity.getOrDefault(priorityPm, 0);
				mapPriorityTASWindowQuantity.put(priorityPm, N_Pm_within_T_Pm);
			}

			mapPriorityTasWindowList.put(priorityPm, tasWindowList);
		}

		/**
		 * Calculate GCL hyper-period and set it in all TASWindow
		 */
		final long T_CGL = calculateGCLHyperperiod(mapPriorityTasWindowList);

		for(Entry<Integer, List<TASWindow>> entry : mapPriorityTasWindowList.entrySet()) {
			for(TASWindow tasWindow : entry.getValue()) {
				tasWindow.T_GCL = T_CGL; //in microseconds (us)
				tasWindow.N_Pm_within_T_Pm = mapPriorityTASWindowQuantity.get(tasWindow.priorityPm);
				tasWindow.N_Pm = (tasWindow.T_GCL / tasWindow.T_Pm) * tasWindow.N_Pm_within_T_Pm;

				//				System.out.println("interfaceInfo="+interfaceInfo.toStringInfo()
				//				+ ", tasWindow.priorityPm={"+tasWindow.priorityPm+"}"
				//				+ ", tasWindow.index={"+tasWindow.index+"}"
				//				+ ", tasWindow.T_GCL={"+tasWindow.T_GCL+"}"
				//				+ ", tasWindow.T_Pm={"+tasWindow.T_Pm+"}"
				//				+ ", tasWindow.N_Pm={"+tasWindow.N_Pm+"}"
				//				+ ", entry.getValue().size()={"+entry.getValue().size()+"}");
			}
		}

		final Map<Integer,List<TASWindow>> newTasWindowsToBeAddedMap = buildTASWindowMapToFillGCL(mapPriorityTasWindowList);

		/**
		 * Merge maps
		 */
		for(Entry<Integer,List<TASWindow>> entry : newTasWindowsToBeAddedMap.entrySet()) {
			final int priority = entry.getKey();
			final List<TASWindow> tasWindowList = entry.getValue();

			mapPriorityTasWindowList.get(priority).addAll(tasWindowList);
		}

		/**
		 * Check for errors in scheduling
		 */
		checkForErrorsInScheduling(mapPriorityTasWindowList);

		/**
		 * Print all TAS Windows
		 */
		mapPriorityTasWindowList.forEach((prio,list) -> {
			list.forEach(tasWindow -> {
				System.out.println("interfaceInfo=" + interfaceInfo.toStringInfo() + " -> " + "prio="+prio + " -> " + tasWindow);
			});
		});


		/**
		 * All the following values must be filled at this point
		 */
		mapPriorityTasWindowList.forEach((prio,list) -> {
			list.forEach(tasWindow -> {
				try {

					Objects.requireNonNull(tasWindow.index, "index cannot be null. tasWindow="+tasWindow);
					Objects.requireNonNull(tasWindow.priorityPm, "priorityPm cannot be null");
					Objects.requireNonNull(tasWindow.N_Pm, "N_Pm cannot be null. tasWindow="+tasWindow);
					Objects.requireNonNull(tasWindow.N_Pm_within_T_Pm, "N_Pm_within_T_Pm cannot be null. tasWindow="+tasWindow);
					Objects.requireNonNull(tasWindow.T_Pm, "T_Pm cannot be null");
					Objects.requireNonNull(tasWindow.T_GCL, "T_GCL cannot be null");
					Objects.requireNonNull(tasWindow.t_oi_Pm, "t_oi_Pm cannot be null");
					Objects.requireNonNull(tasWindow.t_ci_Pm, "t_ci_Pm cannot be null");
					Objects.requireNonNull(tasWindow.l_max_Pm, "l_max_Pm cannot be null");
					Objects.requireNonNull(tasWindow.l_min_Pm, "l_min_Pm cannot be null");
					Objects.requireNonNull(tasWindow.d_gb_Pm, "d_gb_Pm cannot be null");
					Objects.requireNonNull(tasWindow.t_gbi_Pm, "t_gbi_Pm cannot be null");

				}catch (Exception e) {
					System.out.println("------------------ Error ------------------");
					System.err.println("interfaceInfo="+interfaceInfo.toStringInfo());
					DataPrinterUtil.print(mapPriorityTasWindowList);
					System.out.println("-------------------------------------------");
					throw e;
				}
			});
		});


	}

	/**
	 * Calculate:
	 * 	l_max_Pm_plus used in Equation 8. 
	 * 	
	 * @param interfaceInfo
	 * @param mapPriorityTasWindowList
	 */
	@SuppressWarnings("unused")
	private void calculateTASWindowListStep2_Wrong(final InterfaceInfoEntry interfaceInfo, final Map<Integer,List<TASWindow>> mapPriorityTasWindowList){

		for(Entry<Integer,List<TASWindow>> entryTarget : mapPriorityTasWindowList.entrySet()) {

			final int priorityTarget = entryTarget.getKey();

			for(final TASWindow target : entryTarget.getValue()) {

				/**
				 * Starting with l_max_Pm_plus = 0.
				 * 
				 * This value does not cause any interference on calculations when there is no lower priority windows.
				 * See Equation 8 for more details.
				 */
				double l_max_Pm_plus = 0;

				for(Entry<Integer,List<TASWindow>> entryOther : mapPriorityTasWindowList.entrySet()) {

					final int priorityOther = entryOther.getKey();

					if(lessThan(priorityOther, priorityTarget)) {

						/**
						 * Each TASWindow instance represents a single window within a GCL period from the same priority.
						 * Even we have a list of TASWindow, all of them must have the same value for maximum and minimum frame size, 
						 * because it is calculated for each priority and not for each window into the same priority scheduling.
						 * The item in index 0 has the same value for max and min frame sizes as any other index.
						 */
						final TASWindow otherFirst = entryOther.getValue().get(0);

						//Sanity check
						Objects.requireNonNull(otherFirst.l_max_Pm, "You must calculate all 'l_max_Pm' values before using this method.");
						Objects.requireNonNull(otherFirst.l_min_Pm, "You must calculate all 'l_min_Pm' values before using this method.");

						l_max_Pm_plus = Math.max(l_max_Pm_plus, otherFirst.l_max_Pm);
					}

				}

				target.l_max_Pm_plus = l_max_Pm_plus;
			}
		}

		/**
		 * All the following values must be filled at this point
		 */
		mapPriorityTasWindowList.forEach((prio,list) -> {
			list.forEach(tasWindow -> {
				try {
					Objects.requireNonNull(tasWindow.l_max_Pm_plus, "l_max_Pm_plus cannot be null");

				}catch (Exception e) {
					DataPrinterUtil.print(mapPriorityTasWindowList);
					throw e;
				}
			});
		});

	}


	/**
	 * Calculate:
	 * 	t_ci_Pm_plus used in Equation 8.
	 * 
	 * @param interfaceInfo
	 * @param mapPriorityTasWindowList
	 */
	@SuppressWarnings("unused")
	private void calculateTASWindowListStep3_Wrong(final InterfaceInfoEntry interfaceInfo, final Map<Integer,List<TASWindow>> mapPriorityTasWindowList){

		for(Entry<Integer,List<TASWindow>> entryTarget : mapPriorityTasWindowList.entrySet()) {

			for(TASWindow target : entryTarget.getValue()) {

				//Sanity check
				Objects.requireNonNull(target.t_oi_Pm, "You must fill all 't_oi_Pm' values before call this method");

				/**
				 * Starting with t_ci_Pm_plus = target.t_oi_Pm.
				 * 
				 * When there is no lower priority windows, then there is no traffic interference.
				 * See Equation 8 for more details.
				 */
				double t_ci_Pm_plus = target.t_oi_Pm; //in microseconds (us)

				for(Entry<Integer,List<TASWindow>> entryOther : mapPriorityTasWindowList.entrySet()) {

					final int priorityOther = entryOther.getKey();

					//We are looking for lower priority windows
					if(lessThan(priorityOther, target.priorityPm)) {

						for(final TASWindow tasWindowLowerPriority : entryOther.getValue()) {

							//Sanity check
							Objects.requireNonNull(tasWindowLowerPriority.t_ci_Pm, "You must fill all 't_ci_Pm' values before call this method");

							/**
							 * Calculate t_ci_Pm_plus (Equation ??)
							 * 
							 * A lower priority window must be already opened at the target window opening time, and
							 * the closing time for a lower priority window must be after the opening time of the target window.
							 */
							if(tasWindowLowerPriority.isGateOpen(target.t_oi_Pm) && (tasWindowLowerPriority.t_ci_Pm > target.t_oi_Pm)) {
								t_ci_Pm_plus = Math.max(t_ci_Pm_plus, tasWindowLowerPriority.t_ci_Pm);
							}

						}

					}

				}//end of for(Entry<Integer,List<TASWindow>> entryOther : mapPriorityTerms.entrySet()) {

				target.t_ci_Pm_plus = t_ci_Pm_plus; //in microseconds (us)
			}

		}

		/**
		 * All the following values must be filled at this point
		 */
		mapPriorityTasWindowList.forEach((prio,list) -> {
			list.forEach(tasWindow -> {
				try {
					Objects.requireNonNull(tasWindow.t_ci_Pm_plus, "t_ci_Pm_plus cannot be null");

					if(tasWindow.t_ci_Pm_plus < tasWindow.t_oi_Pm)
						throw new InvalidParameterException("tasWindow.t_ci_Pm_plus is the nearest time when the gate G_Pm_plus is closed no earlier than tasWindow.t_oi_Pm. "
								+ "tasWindow.t_ci_Pm_plus={"+tasWindow.t_ci_Pm_plus+"} must be lesser than tasWindow.t_oi_Pm={"+tasWindow.t_oi_Pm+"}.");

				}catch (Exception e) {
					DataPrinterUtil.print(mapPriorityTasWindowList);
					throw e;
				}
			});
		});

	}


	/**
	 * Calculate:
	 * 	d_npi_Pm_plus (Equation 8)
	 * 
	 * @param interfaceInfo
	 * @param mapPriorityTasWindowList
	 */
	@SuppressWarnings("unused")
	private void calculateTASWindowListStep4_Wrong(final InterfaceInfoEntry interfaceInfo, final Map<Integer,List<TASWindow>> mapPriorityTasWindowList){

		final EthernetPhyStandard phyStandard = this.mapInterfaceInfoPhyStandard.get(interfaceInfo);

		if(Objects.equals(interfaceInfo.toStringInfo(), "SW1.3")) {
			System.out.println("Here!");
		}

		for(Entry<Integer,List<TASWindow>> entryTarget : mapPriorityTasWindowList.entrySet()) {

			for(TASWindow target : entryTarget.getValue()) {

				//Sanity check
				Objects.requireNonNull(target.t_oi_Pm, "You must fill all 't_oi_Pm' values before call this method");
				Objects.requireNonNull(target.t_ci_Pm_plus, "You must fill all 't_ci_Pm_plus' values before call this method");
				Objects.requireNonNull(target.l_max_Pm_plus, "You must fill all 'l_max_Pm_plus' values before call this method");
				/**
				 * Calculate Equation 8.
				 */
				final boolean G_Pm_plus_isOpen = G_Pm_plus(target.priorityPm, target.t_oi_Pm, mapPriorityTasWindowList);

				if(G_Pm_plus_isOpen) {
					target.d_npi_Pm_plus = Math.min(
							(target.l_max_Pm_plus * 8.0) / phyStandard.rate_bpus, 
							target.t_ci_Pm_plus - target.t_oi_Pm);
				} else {
					target.d_npi_Pm_plus = 0.0;
				}

			}

		}

		/**
		 * All the following values must be filled at this point
		 */
		mapPriorityTasWindowList.forEach((priority,list) -> {
			list.forEach(target -> {
				try {
					Objects.requireNonNull(target.d_npi_Pm_plus, "d_npi_Pm_plus cannot be null");

				}catch (Exception e) {
					DataPrinterUtil.print(mapPriorityTasWindowList);
					throw e;
				}
			});
		});

	}

	/**
	 * Calculate:
	 * 	d_npi_L (Equation 9), and 
	 * 	t_npi_L (Equation 10)
	 * 
	 * @param interfaceInfo
	 * @param mapPriorityTasWindowList
	 */
	@SuppressWarnings("unused")
	private void calculateTASWindowListStep5_Wrong(final InterfaceInfoEntry interfaceInfo, final Map<Integer,List<TASWindow>> mapPriorityTasWindowList){

		if(Objects.equals(interfaceInfo.toStringInfo(), "SW1.3")) {
			System.out.println("------------------------------------------------------");
			System.out.println("Here 2!");
		}

		for(Entry<Integer,List<TASWindow>> entryTarget : mapPriorityTasWindowList.entrySet()) {
			for(TASWindow target : entryTarget.getValue()) {

				if(target.priorityPm == 5) {
					System.out.println(".");
				}

				//Sanity check
				Objects.requireNonNull(target.t_oi_Pm, "You must fill all 't_oi_Pm' values before call this method");

				/**
				 * Starting with d_npi_L = 0.0
				 * 
				 * This term is the worst-case delay caused by lower priority traffic due to non-preemption mechanism.
				 * When there is no lower priority traffic interference, so there is no delay cause by this.
				 */
				double d_npi_L = 0.0;

				for(Entry<Integer,List<TASWindow>> entryOther : mapPriorityTasWindowList.entrySet()) {

					final int priorityOther = entryOther.getKey();

					//We are looking for lower priority windows
					if(lessThan(priorityOther, target.priorityPm)) {

						for(final TASWindow tasWindowLowerPriority : entryOther.getValue()) {

							Objects.requireNonNull(tasWindowLowerPriority.d_npi_Pm_plus, "You must calculate all 'd_npi_Pm_plus' terms before call this method");

							d_npi_L = Math.max(d_npi_L, tasWindowLowerPriority.d_npi_Pm_plus);
						}

					}

				}

				/**
				 * Calculate d_npi_L	(Equation 9)
				 */
				target.d_npi_L = d_npi_L;

				/**
				 * Calculate t_npi_L	(Equation 10)
				 */
				target.t_npi_L = target.d_npi_L + target.t_oi_Pm;


				System.out.println();
				System.out.println(target);
			}

		}

		/**
		 * All the following values must be filled at this point
		 */
		mapPriorityTasWindowList.forEach((prio,list) -> {
			list.forEach(target -> {
				try {
					Objects.requireNonNull(target.d_npi_L, "d_npi_L cannot be null");
					Objects.requireNonNull(target.t_npi_L, "t_npi_L cannot be null");

				}catch (Exception e) {
					DataPrinterUtil.print(mapPriorityTasWindowList);
					throw e;
				}
			});
		});
	}

	/**
	 * Calculate:
	 * 	d_npi_L	(Equation 9)
	 * 	t_npi_L	(Equation 10)
	 * 
	 * @param interfaceInfo
	 * @param mapPriorityTasWindowList
	 */
	private void calculateTASWindowListStep2_3_4_5_Correct(InterfaceInfoEntry interfaceInfo, Map<Integer,List<TASWindow>> mapPriorityTasWindowList){

		final EthernetPhyStandard phyStandard = this.mapInterfaceInfoPhyStandard.get(interfaceInfo);

		for(Entry<Integer,List<TASWindow>> entryTarget : mapPriorityTasWindowList.entrySet()) {

			for(TASWindow target : entryTarget.getValue()) {

				//Sanity check
				Objects.requireNonNull(target.t_oi_Pm, "You must fill all 't_oi_Pm' values before call this method");

				final Map<Integer,Double> mapPriorityMinInterferenceLowerPriority = new HashMap<Integer, Double>();

				for(Entry<Integer,List<TASWindow>> entryOther : mapPriorityTasWindowList.entrySet()) {

					final int priorityOther = entryOther.getKey();

					//We are looking for lower priority windows
					if(lessThan(priorityOther, target.priorityPm)) {

						//Zero is the default value when there is no overlapping with a lower priority window
						mapPriorityMinInterferenceLowerPriority.put(priorityOther, 0.0);

						for(final TASWindow termsLowerPriority : entryOther.getValue()) {

							//Sanity check
							Objects.requireNonNull(termsLowerPriority.t_ci_Pm, "You must fill all 't_ci_Pm' values before call this method");
							Objects.requireNonNull(termsLowerPriority.l_max_Pm, "You must fill all 'l_max_Pm' values before call this method");

							if(termsLowerPriority.isGateOpen(target.t_oi_Pm)) {

								double minInterference = Math.min((termsLowerPriority.l_max_Pm * 8.0) / phyStandard.rate_bpus, termsLowerPriority.t_ci_Pm - target.t_oi_Pm);

								/**
								 * Since it is not possible to have overlapping windows from the same priority, 
								 * I am assuming there is at most a single overlapping window from each lower priority Pm+ with the window in Pm.
								 * 
								 * If there are more than one overlapping window from the same priority, then there exists an error in related priority scheduling.
								 * This error condition is checked in the ending of calculateStep1() method and will throw an exception if occur.
								 */
								mapPriorityMinInterferenceLowerPriority.put(priorityOther, minInterference);
							}

						}

					}

				}//end of for(Entry<Integer,List<TASWindow>> entryOther : mapPriorityTerms.entrySet()) {

				/**
				 * Calculate d_npi_L	(Equation 9)
				 * 
				 * Starting with d_npi_L = 0.0
				 * The value 0.0 does not cause any interference on calculations when there is no lower priority windows.
				 */

				if(mapPriorityMinInterferenceLowerPriority.isEmpty()) {
					target.d_npi_L = 0.0;	
				}else {
					target.d_npi_L = NumberUtil.max(mapPriorityMinInterferenceLowerPriority.values());	
				}

				/**
				 * Calculate t_npi_L	(Equation 10)
				 * 
				 * Previously it was calculated in calculateTASWindowListStep5_Wrong()
				 */
				target.t_npi_L = target.d_npi_L + target.t_oi_Pm;

				/**
				 * These terms are not used anymore. 
				 * This is a workaround to avoid Exceptions when checking if each term is not null 
				 */
				target.l_max_Pm_plus = Double.NaN; //Previously it was calculated in calculateTASWindowListStep2_Wrong()
				target.t_ci_Pm_plus = Double.NaN; //Previously it was calculated in calculateTASWindowListStep3_Wrong()
				target.d_npi_Pm_plus = Double.NaN; //Previously it was calculated in calculateTASWindowListStep4_Wrong()
			}

		}

		/**
		 * All the following values must be filled at this point
		 */
		mapPriorityTasWindowList.forEach((prio,list) -> {
			list.forEach(tasWindow -> {
				try {
					Objects.requireNonNull(tasWindow.d_npi_L, "d_npi_L cannot be null");
					Objects.requireNonNull(tasWindow.t_npi_L, "t_npi_L cannot be null");

				}catch (Exception e) {
					DataPrinterUtil.print(mapPriorityTasWindowList);
					throw e;
				}
			});
		});

	}

	/**
	 * Calculate:
	 * 	t_ci_Pm_minus (Equation 13)
	 * 	t_oi_Pm_minus (Equation 14)
	 * 
	 * @param interfaceInfo
	 * @param mapPriorityTasWindowList
	 */
	private void calculateTASWindowListStep6(final InterfaceInfoEntry interfaceInfo, final Map<Integer,List<TASWindow>> mapPriorityTasWindowList){

		for(Entry<Integer,List<TASWindow>> entryTarget : mapPriorityTasWindowList.entrySet()) {
			for(TASWindow target : entryTarget.getValue()) {

				/**
				 * Starting t_ci_Pm_minus = target.t_oi_Pm.
				 * 
				 * This value 'target.t_oi_Pm' does not make any interference on calculations of Equation 13, 
				 * because we are looking for the greatest lower bound (infimum) for any time 't' after 't_oi_Pm' and Equation 15 uses the max{} function.
				 * 
				 * 
				 * Starting t_oi_Pm_minus = target.t_ci_Pm.
				 * 
				 * This value 'target.t_ci_Pm' does not make any interference on calculations of Equation 14,
				 * because we are looking for the least upper bound (supremum) for any time 't' before 't_ci_Pm' and Equation 16 used the min{} function.
				 */
				double t_ci_Pm_minus = target.t_oi_Pm;
				double t_oi_Pm_minus = target.t_ci_Pm;

				for(Entry<Integer,List<TASWindow>> entryOther : mapPriorityTasWindowList.entrySet()) {

					//Sanity check
					Objects.requireNonNull(target.t_oi_Pm, "You must fill all 't_oi_Pm' values before call this method");
					Objects.requireNonNull(target.t_ci_Pm, "You must fill all 't_ci_Pm' values before call this method");
					Objects.requireNonNull(target.priorityPm, "You must fill all 'priorityPm' values before call this method");

					final int priorityOther = entryOther.getKey();

					//We are looking for higher priority windows
					if(greaterThan(priorityOther, target.priorityPm)) {

						for(final TASWindow tasWindowHigherPriority : entryOther.getValue()) {

							/**
							 * We are only interested in windows that collide each other.
							 * If has no window collision, then there is no interference.
							 */
							if(target.hasCollision(tasWindowHigherPriority)) {

								/**
								 * 	Calculate: 	t_ci_Pm_minus (Equation 13)
								 */
								if((tasWindowHigherPriority.t_ci_Pm >= target.t_oi_Pm) && (tasWindowHigherPriority.t_oi_Pm <= target.t_oi_Pm)) {
									t_ci_Pm_minus = Math.max(t_ci_Pm_minus, tasWindowHigherPriority.t_ci_Pm);
								}

								/**
								 * 	Calculate: 	t_oi_Pm_minus (Equation 14)
								 */
								if((tasWindowHigherPriority.t_oi_Pm <= target.t_ci_Pm) && (tasWindowHigherPriority.t_ci_Pm >= target.t_ci_Pm)) {
									t_oi_Pm_minus = Math.min(t_oi_Pm_minus, tasWindowHigherPriority.t_oi_Pm);
								}

							}

						}

					} 

				}

				target.t_ci_Pm_minus = t_ci_Pm_minus;
				target.t_oi_Pm_minus = t_oi_Pm_minus;
			}

		}

		/**
		 * All the following values must be filled at this point
		 */
		mapPriorityTasWindowList.forEach((prio,list) -> {
			list.forEach(tasWindow -> {
				try {
					Objects.requireNonNull(tasWindow.t_ci_Pm_minus, "t_ci_Pm_minus cannot be null");
					Objects.requireNonNull(tasWindow.t_oi_Pm_minus, "t_oi_Pm_minus cannot be null");
				}catch (Exception e) {
					DataPrinterUtil.print(mapPriorityTasWindowList);
					throw e;
				}
			});
		});

	}

	/**
	 * Calculate:
	 * 	t_Bi_H 		(Equation 15)
	 * 	t_Ei_H 		(Equation 16)
	 * 	t_Bi_Pm 	(Equation 17)
	 * 	t_Ei_Pm		(Equation 18)
	 * 	L_bar_i_Pm (Equation 19)
	 * 	o_bar_i_Pm (Equation 20)
	 * 
	 * @param interfaceInfo
	 * @param mapPriorityTasWindowList
	 */
	private void calculateTASWindowListStep7(final InterfaceInfoEntry interfaceInfo, final Map<Integer,List<TASWindow>> mapPriorityTasWindowList){

		//System.out.println("calculateTASWindowListStep7()");

		final EthernetPhyStandard phyStandard = this.mapInterfaceInfoPhyStandard.get(interfaceInfo);

		for(Entry<Integer,List<TASWindow>> entryTarget : mapPriorityTasWindowList.entrySet()) {
			for(final TASWindow target : entryTarget.getValue()) {

				//Sanity check
				Objects.requireNonNull(target.t_npi_L, "You must fill all 't_npi_L' values before call this method");
				Objects.requireNonNull(target.t_gbi_Pm, "You must fill all 't_gbi_Pm' values before call this method");
				Objects.requireNonNull(target.l_min_Pm, "You must fill all 'l_min_Pm' values before call this method");
				Objects.requireNonNull(target.t_oi_Pm, "You must fill all 't_oi_Pm' values before call this method");
				Objects.requireNonNull(target.t_ci_Pm, "You must fill all 't_ci_Pm' values before call this method");

				/**
				 * Starting t_Bi_H = target.t_oi_Pm.
				 * 
				 * This value 'max_t_ci_Pm_minus' will be used to set the 't_Bi_H'. This term 't_Bi_H' is used only in Equations 15 and 17 (using max{}).
				 * Thus, this value does not make any interference on calculations when there is no higher priority windows.
				 * 
				 * 
				 * Starting t_Ei_H = target.t_ci_Pm.
				 * 
				 * This value 'min_t_oi_Pm_minus' will be used to set the 't_Ei_H'. This term 't_Ei_H' is used only in Equations 16 and 18 (using min{}).
				 * Thus, this value does not make any interference on calculations when there is no higher priority windows.
				 */
				double t_Bi_H = target.t_oi_Pm;
				double t_Ei_H = target.t_ci_Pm;


				//				System.out.println("interfaceInfo=" + interfaceInfo.toStringInfo() + ", target = " + target);

				/**
				 * Calculate:
				 * 	t_Bi_H (Equation 15)
				 * 	t_Ei_H (Equation 16)
				 */
				for(Entry<Integer,List<TASWindow>> entryOther : mapPriorityTasWindowList.entrySet()) {

					final int priorityOther = entryOther.getKey();

					//We are looking for higher priority windows
					if(greaterThan(priorityOther, target.priorityPm)) {

						for(final TASWindow tasWindowHigherPriority : entryOther.getValue()) {

							//							System.out.println("tasWindowHigherPriority = " + tasWindowHigherPriority);

							/**
							 * We are only interested in windows that collide each other.
							 * If has no window collision, then there is no interference.
							 */
							if(target.isGateOpen(tasWindowHigherPriority.t_ci_Pm) && !target.isGateOpen(tasWindowHigherPriority.t_oi_Pm)) {

								t_Bi_H = Math.max(t_Bi_H, tasWindowHigherPriority.t_ci_Pm);
							}

							/**
							 * We are only interested in windows that collide each other.
							 * If has no window collision, then there is no interference.
							 */
							if(target.isGateOpen(tasWindowHigherPriority.t_oi_Pm) && !target.isGateOpen(tasWindowHigherPriority.t_ci_Pm)) {

								t_Ei_H = Math.min(t_Ei_H, tasWindowHigherPriority.t_oi_Pm);
							}
						}

					}

				}

				/**
				 * Calculate:
				 * 	t_Bi_H (Equation 15)
				 * 	t_Ei_H (Equation 16)
				 */
				target.t_Bi_H = t_Bi_H;
				target.t_Ei_H = t_Ei_H;

				if(t_Bi_H > t_Ei_H)
					System.out.println("Warning: t_Bi_H={"+t_Bi_H+"} > t_Ei_H={"+t_Ei_H+"}, interfaceInfo=" + interfaceInfo.toStringInfo() + ", window=" + target);

				/**
				 * Calculate:
				 * 	t_Bi_Pm (Equation 17)
				 * 	t_Ei_Pm (Equation 18)
				 */
				target.t_Bi_Pm = Math.max(target.t_npi_L, target.t_Bi_H);
				target.t_Ei_Pm = Math.min(target.t_gbi_Pm, target.t_Ei_H);

				/**
				 * Calculate:
				 * 	L_bar_i_Pm (Equation 19)
				 */
				target.L_bar_i_Pm = 0.0;
				if(target.t_Bi_Pm < target.t_Ei_Pm)
					target.L_bar_i_Pm = Math.max(target.t_Ei_Pm - target.t_Bi_Pm, (target.l_min_Pm * 8.0)/ phyStandard.rate_bpus);

				/**
				 * calculate:
				 * 	o_bar_i_Pm (Equation 20)
				 */
				target.o_bar_i_Pm = 0.0;
				if(target.L_bar_i_Pm != 0.0)
					target.o_bar_i_Pm = target.t_Bi_Pm - target.t_oi_Pm;

			}

		}

		/**
		 * All the following values must be filled at this point
		 */
		mapPriorityTasWindowList.forEach((prio,list) -> {
			list.forEach(tasWindow -> {
				try {
					Objects.requireNonNull(tasWindow.t_Bi_H, "t_Bi_H cannot be null");
					Objects.requireNonNull(tasWindow.t_Ei_H, "t_Ei_H cannot be null");
					Objects.requireNonNull(tasWindow.t_Bi_Pm, "t_Bi_Pm cannot be null");
					Objects.requireNonNull(tasWindow.t_Ei_Pm, "t_Ei_Pm cannot be null");
					Objects.requireNonNull(tasWindow.L_bar_i_Pm, "L_bar_i_Pm cannot be null");
					Objects.requireNonNull(tasWindow.o_bar_i_Pm, "o_bar_i_Pm cannot be null");

				}catch (Exception e) {
					DataPrinterUtil.print(mapPriorityTasWindowList);
					throw e;
				}
			});
		});

	}

	/**
	 * Calculate:
	 * 	o_ji_Pm	(Equation 21)
	 * 
	 * @param interfaceInfo
	 * @param mapPriorityTasWindowList
	 */
	private void calculateTASWindowListStep8(final InterfaceInfoEntry interfaceInfo, final Map<Integer,List<TASWindow>> mapPriorityTasWindowList){

		for(Entry<Integer,List<TASWindow>> entryTarget : mapPriorityTasWindowList.entrySet()) {

			final List<TASWindow> tasWindowList = entryTarget.getValue();
			final int size = tasWindowList.size();

			//Sort by index
			tasWindowList.sort((TASWindow o1, TASWindow o2) -> Integer.compare(o1.index, o2.index));

			for(int i = 0 ; i < size ; i++) {

				final TASWindow tasWindow_i = tasWindowList.get(i);

				Objects.requireNonNull(tasWindow_i.index, "You must fill all 'index' values before call this method");
				Objects.requireNonNull(tasWindow_i.T_Pm, "You must fill all 'T_Pm' values before call this method");
				Objects.requireNonNull(tasWindow_i.o_bar_i_Pm, "You must fill all 'o_bar_i_Pm' values before call this method");

				tasWindow_i.o_ji_Pm = new Double[size];

				for(int j = 0 ; j < size ; j++) {

					final TASWindow tasWindow_j = tasWindowList.get(j);

					Objects.requireNonNull(tasWindow_j.index, "You must fill all 'index' values before call this method");
					Objects.requireNonNull(tasWindow_j.T_Pm, "You must fill all 'T_Pm' values before call this method");
					Objects.requireNonNull(tasWindow_j.o_bar_i_Pm, "You must fill all 'o_bar_i_Pm' values before call this method");

					/**
					 * Calculate: 	o_ji_Pm	(Equation 21)
					 */
					double T_Pm = tasWindow_i.T_Pm;
					double o_bar_i_Pm = tasWindow_i.o_bar_i_Pm;
					double o_bar_j_Pm = tasWindow_j.o_bar_i_Pm;

					tasWindow_i.o_ji_Pm[j] = Double.valueOf(((j - i + size) % size) * T_Pm - o_bar_i_Pm + o_bar_j_Pm);
				}
			}

		}

		/**
		 * All the following values must be filled at this point
		 */
		mapPriorityTasWindowList.forEach((prio,list) -> {
			list.forEach(tasWindow -> {
				try {
					Objects.requireNonNull(tasWindow.o_ji_Pm, "o_ji_Pm cannot be null");

					if(!Objects.equals(tasWindow.o_ji_Pm.length, (int)((long)tasWindow.N_Pm))) 
						throw new InvalidParameterException("o_ji_Pm.length={"+tasWindow.o_ji_Pm.length+"} must be equals to N_Pm={"+tasWindow.N_Pm+"}.\r\ntasWindow="+tasWindow);

					for(int j = 0 ; j < tasWindow.o_ji_Pm.length ; j++) {
						Objects.requireNonNull(tasWindow.o_ji_Pm[j], "o_ji_Pm["+j+"] cannot be null");
					}

				}catch (Exception e) {
					DataPrinterUtil.print(mapPriorityTasWindowList);
					throw e;
				}
			});
		});

	}

	//	/**
	//	 * Calculate:
	//	 * 		d_np0_L		(Equation 22)
	//	 * 		S_i_Pm		(Equation 23)
	//	 * 
	//	 * @param interfaceInfo
	//	 * @param mapPriorityTasWindowList
	//	 */
	//	private void calculateTASWindowListStep9(final InterfaceInfo interfaceInfo, final Map<Integer,List<TASWindow>> mapPriorityTasWindowList){
	//
	//		final EthernetPhyStandard phyStandard = this.mapInterfaceInfoPhyStandard.get(interfaceInfo);
	//
	//		for(Entry<Integer,List<TASWindow>> entryTarget : mapPriorityTasWindowList.entrySet()) {
	//
	//			final List<TASWindow> tasWindowList = entryTarget.getValue();
	//			final int size = tasWindowList.size();
	//
	//			for(int index = 0 ; index < size ; index++) {
	//
	//				final boolean negativePrevIndex = ((index - 1) < 0);
	//				int indexPrev = (index - 1 + size) % size;
	//
	//				final TASWindow target = tasWindowList.get(index);
	//
	//				final TASWindow previousTmp = tasWindowList.get(indexPrev);
	//				final TASWindow previous = negativePrevIndex ? 
	//						this.virtualTASWindow(index - 1, target.priorityPm, mapPriorityTasWindowList) :
	//							previousTmp;
	//
	//				final TASWindow last = tasWindowList.get(tasWindowList.size() - 1);
	//
	//				if(negativePrevIndex && last.index != 0) {
	//					System.out.println("#negativePrevIndex: target     : " + target);
	//					System.out.println("#negativePrevIndex: previousTmp: " + previousTmp);
	//					System.out.println("#negativePrevIndex: previous   : " + previous);
	//					System.out.println("#negativePrevIndex: last       : " + last);
	//					System.out.println("#negativePrevIndex: ");
	//				}
	//
	//				final Map<Integer,List<TASWindow>> mapPriorityTASWindowListCollision = getTASWindowsCollideWith(previousTmp, mapPriorityTasWindowList);
	//
	//				final Map<Integer,Double> mapPriorityMinDelayBusyTime = new HashMap<Integer, Double>(); //time in microseconds
	//
	//				for(Entry<Integer,List<TASWindow>> entryOther : mapPriorityTASWindowListCollision.entrySet()) {
	//
	//					final int priorityOther = entryOther.getKey();
	//
	//					//We are looking for lower priority windows
	//					if(lessThan(priorityOther, previousTmp.priorityPm)) {
	//
	//						for(final TASWindow tasWindowLowerPriorityCollision : entryOther.getValue()) {
	//
	//							final double calculatedMinBusyTime = Math.min((tasWindowLowerPriorityCollision.l_max_Pm * 8.0) / phyStandard.rate_bpus, previousTmp.t_Ei_Pm - tasWindowLowerPriorityCollision.t_oi_Pm);
	//
	//							/**
	//							 * Since it is not possible to have overlapping windows from the same priority, 
	//							 * I am assuming there is at most a single overlapping window from each lower priority Pm+ with the window in Pm.
	//							 * 
	//							 * If there are more than one overlapping window from the same priority, then there exists an error in related priority scheduling.
	//							 * This error condition is checked in the ending of calculateStep1() method and will throw an exception if occur.
	//							 */
	//							final double minBusyTime = mapPriorityMinDelayBusyTime.getOrDefault(priorityOther, calculatedMinBusyTime);
	//							mapPriorityMinDelayBusyTime.put(priorityOther, Math.min(minBusyTime, calculatedMinBusyTime));
	//
	//						}
	//
	//					}
	//
	//				}
	//
	//				/**
	//				 * Calculate d_np0_L	(Equation 22)
	//				 */
	//				if(mapPriorityMinDelayBusyTime.isEmpty())
	//					target.d_np0_L = 0.0;
	//				else
	//					target.d_np0_L = NumberUtil.max(mapPriorityMinDelayBusyTime.values());
	//
	//				/**
	//				 * Calculate S_i_Pm		(Equation 23)
	//				 */
	//				target.S_i_Pm = target.d_np0_L + target.t_Bi_Pm - previous.t_Ei_Pm;
	//			}
	//
	//		}
	//
	//		/**
	//		 * All the following values must be filled at this point
	//		 */
	//		mapPriorityTasWindowList.forEach((prio,list) -> {
	//			list.forEach(tasWindow -> {
	//				try {
	//					Objects.requireNonNull(tasWindow.d_np0_L, "d_np0_L cannot be null");
	//					Objects.requireNonNull(tasWindow.S_i_Pm, "S_i_Pm cannot be null");
	//
	//				}catch (Exception e) {
	//					DataPrinterUtil.print(mapPriorityTasWindowList);
	//					throw e;
	//				}
	//			});
	//		});
	//	}

	/**
	 * Calculate:
	 * 		d^{np,0}_{L}	(Equation 22)
	 * 		S^{i}_{P_m}		(Equation 23)
	 * 
	 * @param interfaceInfo
	 * @param mapPriorityTasWindowList
	 */
	private void calculateTASWindowListStep9(final InterfaceInfoEntry interfaceInfo, final Map<Integer,List<TASWindow>> mapPriorityTasWindowList){

		final EthernetPhyStandard phyStandard = this.mapInterfaceInfoPhyStandard.get(interfaceInfo);

		for(Entry<Integer,List<TASWindow>> entryTarget : mapPriorityTasWindowList.entrySet()) {

			final List<TASWindow> tasWindowList = entryTarget.getValue();
			final int size = tasWindowList.size();

			for(int index = 0 ; index < size ; index++) {

				final TASWindow target = tasWindowList.get(index);
				final TASWindow previous = this.virtualTASWindow(index - 1, target.priorityPm, mapPriorityTasWindowList);

				final Map<Integer,List<TASWindow>> mapPriorityTASWindowListCollision = getTASWindowsCollisionsMap(previous, mapPriorityTasWindowList);

				final Map<Integer,Double> mapPriorityMinDelayBusyTime = new HashMap<Integer, Double>(); //time in microseconds

				for(Entry<Integer,List<TASWindow>> entryOther : mapPriorityTASWindowListCollision.entrySet()) {

					final int priorityOther = entryOther.getKey();

					//We are looking for lower priority windows
					if(lessThan(priorityOther, previous.priorityPm)) {

						for(final TASWindow tasWindowLowerPriorityCollision : entryOther.getValue()) {

							final double calculatedMinBusyTime = Math.min(
									(tasWindowLowerPriorityCollision.l_max_Pm * 8.0) / phyStandard.rate_bpus, 
									previous.t_Ei_Pm - tasWindowLowerPriorityCollision.t_oi_Pm);

							/**
							 * Since it is not possible to have overlapping windows among the same priority, 
							 * I am assuming there is at most a single overlapping window from each lower priority Pm+ with the window in Pm.
							 * 
							 * If there are more than one overlapping window from the same priority, then there exists an error in related priority scheduling.
							 * This error condition is checked in the ending of calculateStep1() method and will throw an exception if occur.
							 */
							final double minBusyTime = mapPriorityMinDelayBusyTime.getOrDefault(priorityOther, calculatedMinBusyTime);
							mapPriorityMinDelayBusyTime.put(priorityOther, Math.min(minBusyTime, calculatedMinBusyTime));

						}

					}

				}

				/**
				 * Calculate d_np0_L	(Equation 22)
				 */
				if(mapPriorityMinDelayBusyTime.isEmpty())
					target.d_np0_L = 0.0;
				else
					target.d_np0_L = NumberUtil.max(mapPriorityMinDelayBusyTime.values());

				/**
				 * Calculate S_i_Pm		(Equation 23)
				 */
				target.S_i_Pm = target.d_np0_L + target.t_Bi_Pm - previous.t_Ei_Pm;
			}

		}

		/**
		 * All the following values must be filled at this point
		 */
		mapPriorityTasWindowList.forEach((prio,list) -> {
			list.forEach(tasWindow -> {
				try {
					Objects.requireNonNull(tasWindow.d_np0_L, "d_np0_L cannot be null");
					Objects.requireNonNull(tasWindow.S_i_Pm, "S_i_Pm cannot be null");

				}catch (Exception e) {
					DataPrinterUtil.print(mapPriorityTasWindowList);
					throw e;
				}
			});
		});
	}


	private void calculateTASWindowListStep10(final InterfaceInfoEntry targetInterfaceInfo, final Map<Integer,List<TASWindow>> mapPriorityTASWindowList){

		//		System.out.println("calculateTASWindowListStep10()");

		//Sanity check
		Objects.requireNonNull(targetInterfaceInfo);
		Objects.requireNonNull(mapPriorityTASWindowList);
		Objects.requireNonNull(this.mapInterfaceInfoPhyStandard);
		Objects.requireNonNull(this.mapInterfaceInfoPriorityPLCAWeightWRR);
		Objects.requireNonNull(this.mapInterfaceInfoPriorityMaxMinMessageSizeBytes);

		this.mapTASWindowPriorityTASWindowListCollisions = new HashMap<>();

		/**
		 * One chart for each Ethernet interface
		 */
		switch(this.executionConfig.generateTASWindowsCharts) {
		case GENERATE_INDIVIDUAL_INTERFACES_CHARTS_ONLY:
		case ALL:
			try {

				final String absolutePath = this.datasetReader.getResultAbsolutePath();
				final String nameDescription = targetInterfaceInfo.toStringInfoEth();

				final File dir = new File(absolutePath+"/TAS_charts");
				if(!dir.exists()) dir.mkdirs();

				ChartUtilsTASWindow.saveTASWindowsChart(dir.getPath() + "/" + nameDescription, 
						nameDescription, 
						targetInterfaceInfo, 
						mapPriorityTASWindowList, 
						this.executionConfig.prioritizingOrder);

			} catch (IOException e) {
				e.printStackTrace();
			}

			break;
		default:
			break;
		}


		switch(this.executionConfig.generateTASWindowsCharts) {
		case GENERATE_WINDOW_COLLISIONS_SAME_INTERFACE_CHARTS_ONLY:
		case ALL:

			/**
			 * Iterate over all priorities from TAS queues of 'targetInterfaceInfo' interface
			 */
			for(final Entry<Integer, List<TASWindow>> entry : mapPriorityTASWindowList.entrySet()) {

				final List<TASWindow> tasWindowDataList = entry.getValue();

				/**
				 * Iterate over all windows from current priority
				 */
				for(TASWindow targetWindow : tasWindowDataList) {

					final Map<Integer,List<TASWindow>> mapPriorityTASWindowListCollisions = 
							getTASWindowsCollisionsMap(targetWindow, mapPriorityTASWindowList);

					this.mapTASWindowPriorityTASWindowListCollisions.put(targetWindow, mapPriorityTASWindowListCollisions);

					try {

						final String absolutePath = this.datasetReader.getResultAbsolutePath();
						final String nameDescription = targetInterfaceInfo.toStringInfoEth()+" - P_m="+targetWindow.priorityPm+"_i="+targetWindow.index;

						final File dir = new File(absolutePath+"/TAS_charts");
						if(!dir.exists()) dir.mkdirs();

						ChartUtilsTASWindow.saveTASWindowsChart(dir.getPath() + "/" + "TAS_windows_collisions_"+nameDescription, 
								"TAS Windows Collisions " + nameDescription,
								targetInterfaceInfo,
								mapPriorityTASWindowListCollisions,
								this.executionConfig.prioritizingOrder);

					} catch (IOException e) {
						e.printStackTrace();
					}

				}
			}

			break;
		default:
			break;
		}

	}

	public TASWindow makeNewTASWindow(int newIndex, int priorityPm, Map<Integer,List<TASWindow>> mapPriorityTasWindowList) {

		final TASWindow first = mapPriorityTasWindowList.get(priorityPm).get(0);

		final int period_T_Pm_id = newIndex / first.N_Pm_within_T_Pm; //
		final int indexReference = newIndex % first.N_Pm_within_T_Pm; // 
		final double timeOffset = first.T_Pm * period_T_Pm_id;
		final TASWindow reference = mapPriorityTasWindowList.get(priorityPm).get(indexReference);
		final TASWindow tasWindow = new TASWindow();

		tasWindow.index = newIndex;
		tasWindow.priorityPm = reference.priorityPm;
		tasWindow.N_Pm = reference.N_Pm;
		tasWindow.N_Pm_within_T_Pm = reference.N_Pm_within_T_Pm;
		tasWindow.T_Pm = reference.T_Pm;
		tasWindow.T_GCL = reference.T_GCL;
		tasWindow.t_oi_Pm = reference.t_oi_Pm + timeOffset;
		tasWindow.t_ci_Pm = reference.t_ci_Pm + timeOffset;
		tasWindow.l_max_Pm = reference.l_max_Pm;
		tasWindow.l_min_Pm = reference.l_min_Pm;

		tasWindow.d_gb_Pm = reference.d_gb_Pm;
		tasWindow.t_gbi_Pm = reference.t_gbi_Pm + timeOffset;

		return tasWindow;
	}

	/**
	 * This method must be used for helping with calculations by creating a virtual TASWindow outside of actual GCL hyperperiod.
	 * 
	 * It does not require to recompute all guaranteed windows, 
	 * because this virtual TASWindow that will be created based on an actual TASWindow with all already calculated attributes, 
	 * and it will not be added to the TAS schedule. 
	 * So that, by just applying a correct offset in related attributes solves this issue.
	 *  
	 * @param newIndex
	 * @param tasWindowList
	 * 
	 * @return A virtual TASWindow outside of actual GCL hyperperiod.
	 */
	public TASWindow virtualTASWindow(int newIndex, final Integer priority, final Map<Integer,List<TASWindow>> mapPriorityTasWindowList) {

		Objects.requireNonNull(priority);
		Objects.requireNonNull(mapPriorityTasWindowList);

		final List<TASWindow> tasWindowList = mapPriorityTasWindowList.get(priority);

		//Sort list by index to avoid disordered list issues
		tasWindowList.sort((t1,t2) -> Integer.compare(t1.index, t2.index));

		final int size = tasWindowList.size();
		final int indexRef = (newIndex < 0) ? (((newIndex % size) + size) % size) : (newIndex % size);

		final TASWindow reference = tasWindowList.get(indexRef);
		final TASWindow virtualWindow = new TASWindow();

		/**
		 * Calculating actual GCL index accordingly to 'newIndex' value and related timeOffset.
		 */
		final int T_GCL_index = (newIndex < 0) ? ((newIndex - (size - 1)) / size) : (newIndex / size);
		final double timeOffset = reference.T_GCL * T_GCL_index;

		virtualWindow.index = newIndex;
		virtualWindow.priorityPm = reference.priorityPm;
		virtualWindow.N_Pm = reference.N_Pm;
		virtualWindow.N_Pm_within_T_Pm = reference.N_Pm_within_T_Pm;
		virtualWindow.T_Pm = reference.T_Pm;
		virtualWindow.T_GCL = reference.T_GCL;
		virtualWindow.t_oi_Pm = reference.t_oi_Pm + timeOffset;
		virtualWindow.t_ci_Pm = reference.t_ci_Pm + timeOffset;
		virtualWindow.l_max_Pm = reference.l_max_Pm;
		virtualWindow.l_min_Pm = reference.l_min_Pm;
		virtualWindow.l_max_Pm_plus = reference.l_max_Pm_plus;
		virtualWindow.t_ci_Pm_plus = reference.t_ci_Pm_plus + timeOffset;
		virtualWindow.d_npi_Pm_plus = reference.d_npi_Pm_plus;
		virtualWindow.d_npi_L = reference.d_npi_L;
		virtualWindow.t_npi_L = reference.t_npi_L + timeOffset;
		virtualWindow.d_gb_Pm = reference.d_gb_Pm;
		virtualWindow.t_gbi_Pm = reference.t_gbi_Pm + timeOffset;
		virtualWindow.t_ci_Pm_minus = reference.t_ci_Pm_minus + timeOffset;
		virtualWindow.t_oi_Pm_minus = reference.t_oi_Pm_minus + timeOffset;
		virtualWindow.t_Bi_H = reference.t_Bi_H + timeOffset;
		virtualWindow.t_Ei_H = reference.t_Ei_H + timeOffset;
		virtualWindow.t_Bi_Pm = reference.t_Bi_Pm + timeOffset;
		virtualWindow.t_Ei_Pm = reference.t_Ei_Pm + timeOffset;
		virtualWindow.L_bar_i_Pm = reference.L_bar_i_Pm;
		virtualWindow.o_bar_i_Pm = reference.o_bar_i_Pm;
		virtualWindow.o_ji_Pm = reference.o_ji_Pm;
		virtualWindow.d_np0_L = reference.d_np0_L;
		virtualWindow.S_i_Pm = reference.S_i_Pm;
		virtualWindow.d_i_PLCA = reference.d_i_PLCA;

		return virtualWindow;
	}



	/**
	 * Returns 'true' if there is any lower priority window than { @param priorityPm } with gate in the open state at time { @param time }, 
	 * or 'false' otherwise.
	 * 
	 * @param priorityPm
	 * @param time
	 * @param mapPriorityListTasWindow
	 * @return
	 */
	private boolean G_Pm_plus(int priorityPm, double time, Map<Integer, List<TASWindow>> mapPriorityListTasWindow) {

		boolean G_Pm_plus_isOpen = false;

		for(Entry<Integer,List<TASWindow>> entryOther : mapPriorityListTasWindow.entrySet()) {

			final int priorityOther = entryOther.getKey();

			//We are looking for lower priority windows
			if(lessThan(priorityOther, priorityPm)) {

				for(final TASWindow tasWindowLowerPriority : entryOther.getValue()) {

					//A lower priority window could be already opened at the target window opening time
					if(tasWindowLowerPriority.isGateOpen(time)) {
						G_Pm_plus_isOpen = true;
						break;
					}
				}
			}

		}

		return G_Pm_plus_isOpen;
	}

	/**
	 * Check for errors in scheduling
	 * 
	 * @param mapPriorityTasWindowList
	 */
	private static void checkForErrorsInScheduling(Map<Integer,List<TASWindow>> mapPriorityTasWindowList){

		for(Entry<Integer,List<TASWindow>> entry : mapPriorityTasWindowList.entrySet()) {
			if(hasCollision(entry.getValue())) {
				DataPrinterUtil.print(entry);
				final String message = "There is a collision between two (or more) windows from the same priority Pm=" + entry.getKey();
				throw new InvalidParameterException(message);
			}
		}

	}

	private static boolean hasCollision(List<TASWindow> tasWindowTerms) {

		//More efficient solution
		for(int i = 0 ; i < tasWindowTerms.size() ; i++) {
			final TASWindow tasWindow_i = tasWindowTerms.get(i);

			for(int j = i + 1 ; j < tasWindowTerms.size() ; j++ ) {
				final TASWindow tasWindow_j = tasWindowTerms.get(j);

				if(tasWindow_i.hasCollision(tasWindow_j))
					return true;
			}
		}

		//		for(TASWindow a : tasWindowTerms) {
		//			for(TASWindow b : tasWindowTerms) {
		//				if((a != b) && a.hasCollision(b))
		//					return true;
		//			}
		//		}

		return false;
	}

	private static long calculateGCLHyperperiod(Map<Integer, List<TASWindow>> mapPriorityTasWindowList) {

		final Set<Long> priorityPeriodSet = new HashSet<Long>();

		for(Entry<Integer, List<TASWindow>> entry : mapPriorityTasWindowList.entrySet()) {

			//Sanity check
			final Set<Long> periods = new HashSet<Long>();
			for(TASWindow tasWindowTerms : entry.getValue())
				periods.add(tasWindowTerms.T_Pm); //in microseconds (us)

			if(periods.size() != 1)
				throw new InvalidParameterException("There are some TASWindow from priority " + entry.getKey() + " with different values of T_Pm: " + periods.toString());

			priorityPeriodSet.addAll(periods); //in microseconds (us)
		}

		return NumberUtil.lcm(priorityPeriodSet); //in microseconds (us)
	}


	/**
	 * If window period for priority Pm is not equals to the GCL hyperperiod (i.e., T_Pm != T_GCL),
	 * then there are some T_Pm within a T_GCL.
	 * That occur when: \forall m,n \in [0..7] : \exists T_{P_m} \neq T_{P_n}
	 * Thus the T_GCL is LCM (Least Common Multiplier) among all T_Pm.
	 * 
	 * So only the first T_Pm has TASWindows, then one have to build other TASWindows for other T_Pm that fits within the T_GCL.
	 * 
	 * After that, it is required to recalculate TASWindow attributes because new overlapping situation for these new TASWindows will occur.  
	 * 
	 * @param currentMapPriorityTasWindowList
	 * @return
	 */
	private Map<Integer,List<TASWindow>> buildTASWindowMapToFillGCL(Map<Integer,List<TASWindow>> currentMapPriorityTasWindowList) {

		/**
		 * if T_Pm != T_GCL
		 * 
		 * Create new TASWindow to fill all available spaces within the GCL hyper-period
		 */
		final Map<Integer,List<TASWindow>> newTasWindowsToBeAddedMap = new HashMap<>();

		for(Entry<Integer,List<TASWindow>> entryPriorityTASWindow : currentMapPriorityTasWindowList.entrySet()) {

			final int priority = entryPriorityTASWindow.getKey();
			final List<TASWindow> tasWindowList = entryPriorityTASWindow.getValue();

			int index = tasWindowList.size();
			for(TASWindow tasWindow : tasWindowList) {

				final List<TASWindow> newTASWindowsToBeAddedList = new LinkedList<TASWindow>();

				/**
				 * A T_GCL period is composed of many T_Pm periods.
				 * When T_GCL = T_Pm, there is no TASWindow to be added, otherwise we have to calculate how many TASWindows have to add.
				 * 
				 * Since the TASWindows from the first T_Pm were already added before, 
				 * we need to add the remaining TASWindows related to other T_Pm periods until fill whole T_GCL with the related TASWindows.
				 * 
				 * period_T_Pm_count denotes how many T_Pm periods there are within a T_GCL hyperperiod.
				 */
				final int period_T_Pm_count = (int) (tasWindow.T_GCL / tasWindow.T_Pm);

				/**
				 * Starting from period_T_Pm_id = 1 because period_T_Pm_id = 0 was previously created and added by default
				 */
				for(int period_T_Pm_id = 1 ; period_T_Pm_id < period_T_Pm_count ; period_T_Pm_id++) {

					for(int count = 0 ; count < tasWindow.N_Pm_within_T_Pm.intValue() ; count++) {

						final TASWindow newTasWindow = this.makeNewTASWindow(index++, tasWindow.priorityPm, currentMapPriorityTasWindowList);

						newTASWindowsToBeAddedList.add(newTasWindow);
					}

				}

				newTasWindowsToBeAddedMap.put(priority, newTASWindowsToBeAddedList);
			}
		}

		return newTasWindowsToBeAddedMap;
	}

	/**
	 * 
	 * targetWindow is included in returned map
	 * 
	 * @param networkConfig
	 * @param targetInterfaceInfo
	 * @param targetWindow
	 * @return
	 */
	public Map<Integer,List<TASWindow>> getTASWindowsCollisionsMap(final TASWindow targetWindow, final Map<Integer, List<TASWindow>> mapPriorityTasWindowList){

		final Map<Integer,List<TASWindow>> mapPriorityTASWindowListCollision = new HashMap<Integer,List<TASWindow>>();

		for(Entry<Integer, List<TASWindow>> entry : mapPriorityTasWindowList.entrySet()) {

			int priority = entry.getKey();
			final List<TASWindow> tasWindowTerms = entry.getValue();

			/**
			 * Iterate over all windows from current priority
			 */
			for(TASWindow window : tasWindowTerms) {

				if(targetWindow.hasCollision(window)) {

					final List<TASWindow> tasWindowList = mapPriorityTASWindowListCollision.getOrDefault(priority, new LinkedList<TASWindow>());

					tasWindowList.add(window);
					mapPriorityTASWindowListCollision.put(priority, tasWindowList);
				}

			}

		}

		return mapPriorityTASWindowListCollision;
	}

	/**
	 * 
	 * Get all TASWindow which their guaranteed windows collide to guaranteed window of 'targetWindow' from interface 'targetInterfaceInfo'
	 * 
	 * @param networkConfig
	 * @param targetInterfaceInfo
	 * @param targetWindow
	 * @return
	 */
	public Map<InterfaceInfoEntry,Map<Integer,List<TASWindow>>> getGuaranteedTASWindowsCollideWith(InterfaceInfoEntry targetInterfaceInfo, TASWindow targetWindow){

		final Map<InterfaceInfoEntry,Map<Integer,List<TASWindow>>> mapGuaranteedWindowsCollision = new HashMap<InterfaceInfoEntry, Map<Integer,List<TASWindow>>>();

		/**
		 * Iterate over all interfaces from link where 'target' belongs to
		 */
		for(InterfaceInfoEntry interfaceInfo : mapInterfaceInfoLinkInfo.get(targetInterfaceInfo).getInterfaceInfoSet()) {

			//Do not compare to itself
			if(targetInterfaceInfo.equals(interfaceInfo))
				continue;

			/**
			 * Iterate over all priorities from 'interfaceInfo' interface
			 */
			final Map<Integer,STServerData> mapPrioritySTServerData = mapInterfaceInfoPrioritySTServerData.get(interfaceInfo);
			if(mapPrioritySTServerData == null)
				continue;

			for(int priority : mapPrioritySTServerData.keySet()) {

				if(mapInterfaceInfoPrioritySTServerData.get(interfaceInfo) == null) {
					System.out.println("---------------------------");

					System.out.println("mapInterfaceInfoPrioritySTServerData.get("+interfaceInfo+"):");
					DataPrinterUtil.print(mapInterfaceInfoPrioritySTServerData.get(interfaceInfo));

					System.out.println("mapInterfaceInfoPriorityPLCAWeightWRR.get("+interfaceInfo+"):");
					DataPrinterUtil.print(mapInterfaceInfoPriorityPLCAWeightWRR.get(interfaceInfo));

					System.out.println("mapInterfaceInfoPriorityMaxMinMessageSizeBytes.get("+interfaceInfo+"):");
					DataPrinterUtil.print(mapInterfaceInfoPriorityMaxMinMessageSizeBytes.get(interfaceInfo));

					System.out.println("---------------------------");
				}

				final STServerData stServerData = mapInterfaceInfoPrioritySTServerData.get(interfaceInfo).get(priority);

				/**
				 * Iterate over all windows from current priority
				 */
				for(TASWindow window : stServerData.getTASWindowTermsList()) {

					if(targetWindow.hasGuaranteedSlotCollision(window)) {

						final Map<Integer, List<TASWindow>> mapPriorityListTASWindow = mapGuaranteedWindowsCollision.getOrDefault(interfaceInfo, new HashMap<Integer, List<TASWindow>>());
						final List<TASWindow> listTASWindow = mapPriorityListTASWindow.getOrDefault(priority, new LinkedList<TASWindow>());

						listTASWindow.add(window);
						mapPriorityListTASWindow.put(priority, listTASWindow);
						mapGuaranteedWindowsCollision.put(interfaceInfo, mapPriorityListTASWindow);
					}

				}

			}

		}

		return mapGuaranteedWindowsCollision;
	}

	public final void assertSharedInterfacesOnlyOnLink(InterfaceInfoEntry targetInterfaceInfo) {

		System.out.println("targetInterfaceInfo="+targetInterfaceInfo);

		final Set<InterfaceInfoEntry> interfaceInfosOnLink = this.mapInterfaceInfoLinkInfo.get(targetInterfaceInfo).getInterfaceInfoSet();

		//		final boolean singlePLCAServerModelingTarget = this.mapInterfaceInfoPriorityPLCAServerData.get(targetInterfaceInfo).containsKey(NetworkConfig.KEY_PHY_NO_PRIORITY);

		for(InterfaceInfoEntry interfaceInfo : interfaceInfosOnLink) {

			//Assert all interfaces are shared medium access
			final EthernetPhyStandard phyStandard = this.mapInterfaceInfoPhyStandard.get(interfaceInfo);
			if(phyStandard.isDedicatedMediumAccess)
				throw new InvalidParameterException("It doesn't possible to build a PLCA Server for dedicated medium access interface: " + interfaceInfo + ", targetInterfaceInfo=" + targetInterfaceInfo + ", phyStandard: " + phyStandard);

			if(this.mapInterfaceInfoPriorityPLCAWeightWRR.get(interfaceInfo) == null)
				throw new InvalidParameterException("this.mapInterfaceInfoPriorityPLCAWeightWRR.get(interfaceInfo) cannot be null, interfaceInfo: " + interfaceInfo);

			//			if(this.mapInterfaceInfoPriorityMaxMinMessageSize.get(interfaceInfo) == null)
			//				throw new InvalidParameterException("this.mapInterfaceInfoPriorityMaxMinMessageSize.get(interfaceInfo) cannot be null, interfaceInfo: " + interfaceInfo);


			//			//Assert all interfaces have a PLCA Server Data assigned to them
			//			if(this.mapInterfaceInfoPriorityPLCAServerData.get(interfaceInfo).isEmpty()) 
			//				throw new InvalidParameterException("networkConfig.getMapInterfaceInfoPriorityPLCAServerData().get(interfaceInfo) cannot be empty, interfaceInfo: " + interfaceInfo);


			//			//Assert all interfaces are using the same modeling strategy. Either 'Single PLCA WRR Server Modeling' or 'Separated PLCA WRR Server Modeling'.
			//			final boolean singlePLCAServerModelingOther = this.getMapInterfaceInfoPriorityPLCAServerData().get(interfaceInfo).containsKey(NetworkConfig.KEY_PHY_NO_PRIORITY);
			//			if(singlePLCAServerModelingTarget != singlePLCAServerModelingOther)
			//				throw new InvalidParameterException("There are interfaces from same link using different modeling strategy:\r\n"
			//						+ "targetInterfaceInfo={"+targetInterfaceInfo.toStringInfo()+"}, singlePLCAServerModelingTarget={"+singlePLCAServerModelingTarget+"}\r\n"
			//						+ "interfaceInfo={"+interfaceInfo.toStringInfo()+"}, singlePLCAServerModelingOther={"+singlePLCAServerModelingOther+"}");

		}

	}

	private boolean greaterThan(int priorityA, int priorityB) {
		final int v = Integer.compare(priorityA, priorityB);

		switch(this.executionConfig.prioritizingOrder) {
		case LOWER_VALUE_HIGHER_PRIORITY: 	return (v < 0);
		case HIGHER_VALUE_HIGHER_PRIORITY: 	return (v > 0);
		default: 
			throw new IllegalArgumentException("Unexpected value: " + this.executionConfig.prioritizingOrder);
		}

		//return (this.executionConfig.prioritizingOrder == ExecutionConfig.PrioritizingOrder.LOWER_VALUE_HIGHER_PRIORITY) ? (v < 0) : (v > 0);
	}

	private boolean lessThan(int priorityA, int priorityB) {
		final int v = Integer.compare(priorityA, priorityB);

		switch(this.executionConfig.prioritizingOrder) {
		case LOWER_VALUE_HIGHER_PRIORITY: 	return (v > 0);
		case HIGHER_VALUE_HIGHER_PRIORITY: 	return (v < 0);
		default: 
			throw new IllegalArgumentException("Unexpected value: " + this.executionConfig.prioritizingOrder);
		}
		//return (this.executionConfig.prioritizingOrder == ExecutionConfig.PrioritizingOrder.LOWER_VALUE_HIGHER_PRIORITY) ? (v > 0) : (v < 0);
	}

}
