package org.networkcalculus.dnc.ethernet;

import java.io.File;
import java.io.IOException;
import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.apache.commons.math3.util.Pair;
import org.networkcalculus.dnc.AnalysisConfig;
import org.networkcalculus.dnc.AnalysisConfig.Multiplexing;
import org.networkcalculus.dnc.AnalysisConfig.MultiplexingEnforcement;
import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.Curve;
import org.networkcalculus.dnc.curves.ServiceCurve;
import org.networkcalculus.dnc.ethernet.network.server_graph.EthernetFlow;
import org.networkcalculus.dnc.ethernet.plca.PlcaServerData;
import org.networkcalculus.dnc.ethernet.tsn.ExecutionConfig.ValidateSchedulingForFrameSize;
import org.networkcalculus.dnc.ethernet.tsn.TASWindowsBuilder;
import org.networkcalculus.dnc.ethernet.tsn.data.STFlowData;
import org.networkcalculus.dnc.ethernet.tsn.data.STServerData;
import org.networkcalculus.dnc.ethernet.tsn.entry.InterfaceInfoEntry;
import org.networkcalculus.dnc.ethernet.tsn.entry.VirtualLinkEntry;
import org.networkcalculus.dnc.ethernet.tsn.model.LinkInfo;
import org.networkcalculus.dnc.ethernet.tsn.results.AnalysesResultPaperAccess2018;
import org.networkcalculus.dnc.ethernet.tsn.results.AnalysesResultPaperAccess2018.NetworkCase;
import org.networkcalculus.dnc.ethernet.utils.ChartUtilNCCurve;
import org.networkcalculus.dnc.ethernet.utils.DataPrinterUtil;
import org.networkcalculus.dnc.ethernet.utils.DataUnit;
import org.networkcalculus.dnc.network.server_graph.Flow;
import org.networkcalculus.dnc.network.server_graph.Server;
import org.networkcalculus.dnc.network.server_graph.ServerGraph;
import org.networkcalculus.dnc.network.server_graph.Turn;
import org.networkcalculus.dnc.tandem.analyses.SeparateFlowAnalysis;
import org.networkcalculus.dnc.tandem.analyses.TotalFlowAnalysis;
import org.networkcalculus.num.Num;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 * 
 */
public class EthernetNetwork {

	private Set<EthernetDevice> devices;
	private Set<EthernetFlow> ethernetFlows;

	private ServerGraph serverGraph;
	private final TASWindowsBuilder tasWindowsBuilder;

	private final AnalysisConfig analysisConfig;

	/**
	 * Map<String = Analysis instance class name (to avoid hashCode() issue), Map<EthernetDevice = device instance, Double = backlog>>
	 */
	private Map<String, Map<EthernetDevice, Double>> mapAnalysisNameBacklogDevice; 

	private Map<Server, EthernetDevice> mapServerEthernetDevices; //Mapping output interfaces' servers to its Ethernet device

	private Map<Server,Integer> mapServerPriority;

	public EthernetNetwork(TASWindowsBuilder tasWindowsBuilder) throws Exception {
		this.serverGraph = new ServerGraph();

		this.devices = new HashSet<EthernetDevice>();
		this.ethernetFlows = new HashSet<EthernetFlow>();

		this.mapAnalysisNameBacklogDevice = new HashMap<String, Map<EthernetDevice, Double>>();
		this.mapServerEthernetDevices = new HashMap<Server, EthernetDevice>();
		this.mapServerPriority = new HashMap<Server, Integer>();

		this.tasWindowsBuilder = tasWindowsBuilder;

		this.analysisConfig = new AnalysisConfig();
		if(tasWindowsBuilder.getExecutionConfig().multiplexing == Multiplexing.FIFO)
			this.analysisConfig.enforceMultiplexing(MultiplexingEnforcement.GLOBAL_FIFO);

		this.buildNetwork();
	}

	/**
	 * @return the networkConfig
	 */
	public final TASWindowsBuilder getTASWindowCalculator() {
		return tasWindowsBuilder;
	}

	public void addDevice(EthernetDevice device) {
		this.devices.add(device);
	}

	public Set<EthernetFlow> getFlows(){
		return this.ethernetFlows;
	}

	public void putServerDevice(Server server, EthernetDevice device) {
		this.mapServerEthernetDevices.put(server, device);
	}

	public void saveBacklog(TotalFlowAnalysis tfa) {

		//Iterate over all network's servers
		for(Entry<Server, Set<Num>> entry : tfa.getServerBacklogBoundMap().entrySet()) {

			final Server server = entry.getKey();
			final Set<Num> setBacklogNum = entry.getValue();

			//TODO: Why backlog is a set of Nums? Why not only one Num?
			double backlog = 0.0;
			for(Num b : setBacklogNum) {
				backlog += b.doubleValue();
			}

			final EthernetDevice device = this.mapServerEthernetDevices.get(server);
			final String analysisClassName = tfa.getClass().getSimpleName();

			if(device == null)
				//throw new InvalidParameterException("this.mapServersEthernetDevices.get(server="+server+") have returned null");
				continue;

			final Map<EthernetDevice, Double> mapDeviceBacklog = 
					mapAnalysisNameBacklogDevice.getOrDefault(analysisClassName, new HashMap<EthernetDevice, Double>());

			final double oldBacklog = mapDeviceBacklog.getOrDefault(device, 0.0);

			mapDeviceBacklog.put(device, oldBacklog + backlog);
			mapAnalysisNameBacklogDevice.put(analysisClassName, mapDeviceBacklog);
		}
	}

	public void addEthernetFlows(Map<EthernetFlow,Pair<EthernetDevice,EthernetDevice>> mapFlowSourceDestination) throws Exception {

		for(Entry<EthernetFlow,Pair<EthernetDevice,EthernetDevice>> entry : mapFlowSourceDestination.entrySet()) {
			final EthernetFlow ethernetFlow = entry.getKey();
			final EthernetDevice source = entry.getValue().getFirst();
			final EthernetDevice destination = entry.getValue().getSecond();

			switch(this.tasWindowsBuilder.getExecutionConfig().plcaModeling) {
			case SEPARATED_PLCA_SERVER_MODELING:
				addEthernetFlow_NoPlcaServer(ethernetFlow, source, destination);
				break;
			case SINGLE_PLCA_SERVER_MODELING:
				addEthernetFlow_PlcaServer(ethernetFlow, source, destination);
				break;
			}

			//			if(Objects.equals(this.networkConfigBuilder.getExecutionConfig().plcaModeling, ExecutionConfig.PLCAModeling.SEPARATED_PLCA_SERVER_MODELING))
			//				addEthernetFlow_PlcaServer(ethernetFlow, source, destination);
			//			else
			//				addEthernetFlow_NoPlcaServer(ethernetFlow, source, destination);
		}

	}

	public void addEthernetFlow_PlcaServer(EthernetFlow ethernetFlow, EthernetDevice sourceDevice, EthernetDevice destinationDevice) throws Exception {

		if(!(sourceDevice instanceof EthernetEndSystem))
			throw new InvalidParameterException("sourceDevice must be an EthernetEndSystem");
		if(!(destinationDevice instanceof EthernetEndSystem))
			throw new InvalidParameterException("destinationDevice must be an EthernetEndSystem");

		/**
		 * Getting destination single link because it is a EndSystem and it have a single link only, so ".getLink(0)" may not fail.
		 * Getting last hop interfaces which is connected to the destination EndSystem ".getNeighborInterfaces(destinationDevice)"
		 * Getting first interface from last hop which is connected the destination, because it have only one link to it ".get(0)"
		 */


		/**
		 * FIXME: 
		 * 	Assuming (weakly) both source and destination devices being EndSystem and they having a single interface only.
		 * 	If either source or destination device is not a EndSystem this code is wrong.
		 * 	The correct code must calculate the route from source to destination device and get the correct sink interface.
		 */
		final int interfaceId = 0; 

		final EthernetInterface sinkLastNeighborOutputInterface = destinationDevice.getLink(interfaceId).getNeighborInterfaces(destinationDevice).get(0);

		/**
		 * Source's output servers
		 */
		final STServerData sourceSTServerData = sourceDevice.getInterface(interfaceId).getSTServerData(ethernetFlow.getPriority());
		final Server sourceSTServer = sourceSTServerData.getSTServer();

		final EthernetInterface sourceInterface = sourceDevice.getInterface(interfaceId);

		final PlcaServerData sourcePhyServerDataPriority = sourceInterface.getPhyServerData(ethernetFlow.getPriority());
		final PlcaServerData sourcePhyServerDataNoPriority = sourceInterface.getPhyServerData(TASWindowsBuilder.KEY_PHY_NO_PRIORITY);
		final PlcaServerData sourcePhyServerData = (sourcePhyServerDataPriority != null) ?
				sourcePhyServerDataPriority : sourcePhyServerDataNoPriority;

		/**
		 * Sink's neighbor output servers
		 */
		final PlcaServerData sinkPhyServerDataPriority = sinkLastNeighborOutputInterface.getPhyServerData(ethernetFlow.getPriority());
		final PlcaServerData sinkPhyServerDataNoPriority = sinkLastNeighborOutputInterface.getPhyServerData(TASWindowsBuilder.KEY_PHY_NO_PRIORITY);
		final PlcaServerData sinkPhyServerData = (sinkPhyServerDataPriority != null) ? 
				sinkPhyServerDataPriority : sinkPhyServerDataNoPriority;

		final STServerData sinkSTServerData = sinkLastNeighborOutputInterface.getSTServerData(ethernetFlow.getPriority());
		final Server sinkServer;

		//It is required to get the latest server in flow' path
		//If there is a server instance in sinkPhyServerData, then use it, i.e. there is a PLCA server
		//Otherwise, use the server from TAS which is stored in sinkSTServerData, i.e. there is no PLCA server
		sinkServer = (sinkPhyServerData != null) ? 
				sinkPhyServerData.getPlcaServer() : sinkSTServerData.getSTServer();

		final Flow flow;
		/** If both interfaces (source and sink) are from the same link **/
		if(sourceDevice.getLink(interfaceId).getInterfaces().contains(sinkLastNeighborOutputInterface)) {
			final List<Server> path = new LinkedList<Server>();

			path.add(sourceSTServer); //Add ST Server

			if(sourcePhyServerData != null)
				path.add(sourcePhyServerData.getPlcaServer()); //Add PLCA Server

			flow = this.serverGraph.addFlow(ethernetFlow.getAlias(), ethernetFlow.getArrivalCurve(), path);

		} else {
			final List<Turn> path = this.getShortestPath(ethernetFlow.getPriority(), sourceSTServer, sinkServer);
			flow = this.serverGraph.addFlow(ethernetFlow.getAlias(), ethernetFlow.getArrivalCurve(), path);
		}

		ethernetFlow.setFlow(flow);

		this.ethernetFlows.add(ethernetFlow);
	}

	public void addEthernetFlow_NoPlcaServer(EthernetFlow ethernetFlow, EthernetDevice sourceDevice, EthernetDevice destinationDevice) throws Exception {

		if(!(sourceDevice instanceof EthernetEndSystem))
			throw new InvalidParameterException("sourceDevice must be an EthernetEndSystem");
		if(!(destinationDevice instanceof EthernetEndSystem))
			throw new InvalidParameterException("destinationDevice must be an EthernetEndSystem");

		/**
		 * Getting destination single link because it is a EndSystem and it have a single link only, so ".getLink(0)" may not fail.
		 * Getting last hop interfaces which is connected to the destination EndSystem ".getNeighborInterfaces(destinationDevice)"
		 * Getting first interface from last hop which is connected the destination, because it have only one link to it ".get(0)"
		 */


		/**
		 * FIXME: 
		 * 	Assuming (weakly) both source and destination devices being EndSystem and they having a single interface only.
		 * 	If either source or destination device is not a EndSystem this code is wrong.
		 * 	The correct code must calculate the route from source to destination device and get the correct sink interface.
		 */
		final int interfaceId = 0;   

		final EthernetInterface sinkLastNeighborOutputInterface = destinationDevice.getLink(interfaceId).getNeighborInterfaces(destinationDevice).get(0);

		/**
		 * Source's output servers
		 */
		final EthernetInterface sourceInterface = sourceDevice.getInterface(interfaceId);
		final STServerData sourceSTServerData = Objects.requireNonNullElse(
				sourceInterface.getSTServerData(ethernetFlow.getPriority()),
				sourceInterface.getSTServerData(TASWindowsBuilder.KEY_PHY_NO_PRIORITY));
		final Server sourceSTServer = sourceSTServerData.getSTServer();

		final STServerData sinkSTServerData = Objects.requireNonNullElse(
				sinkLastNeighborOutputInterface.getSTServerData(ethernetFlow.getPriority()),
				sinkLastNeighborOutputInterface.getSTServerData(TASWindowsBuilder.KEY_PHY_NO_PRIORITY));
		final Server sinkServer = sinkSTServerData.getSTServer();

		final Flow flow;
		/** If both interfaces (source and sink) are from the same link **/
		if(sourceDevice.getLink(interfaceId).getInterfaces().contains(sinkLastNeighborOutputInterface)) {

			final List<Server> path = new LinkedList<Server>();
			path.add(sourceSTServer); //Add ST Server

			flow = this.serverGraph.addFlow(ethernetFlow.getAlias(), ethernetFlow.getArrivalCurve(), path);

		} else {
			final List<Turn> path = this.getShortestPath(ethernetFlow.getPriority(), sourceSTServer, sinkServer);

			flow = this.serverGraph.addFlow(ethernetFlow.getAlias(), ethernetFlow.getArrivalCurve(), path);
		}

		ethernetFlow.setFlow(flow);

		this.ethernetFlows.add(ethernetFlow);
	}

	private Set<Server> filterServersByPriority(int priority, Set<Server> servers){

		final Set<Server> result = new HashSet<Server>(servers);
		final Set<Server> toBeRemoved = new HashSet<Server>();

		result.forEach(server -> {
			Integer prio = this.getServerPriority(server);
			if(prio != null && prio != priority)
				toBeRemoved.add(server);
		});

		result.removeAll(toBeRemoved);

		return result;
	}

	/**
	 * Calculates the shortest path between src and snk according to Dijkstra's
	 * algorithm.
	 * 
	 * This code was got from ServerGraph class of DNC project and David Alain do Nascimento did some little changes to be compliant to priority usage
	 *
	 * @param priority
	 * 			  Flow's priority. The possible path must be related to this priority.
	 * 
	 * @param src
	 *            The path's source.
	 * @param snk
	 *            The path's sink.
	 * @return Dijkstra shortest path from src to snk.
	 * @throws Exception
	 *             Could not find a shortest path for some reason.
	 */
	public List<Turn> getShortestPath(int priority, Server src, Server snk) throws Exception {
		Set<Server> visited = new HashSet<Server>();

		final Map<Server, List<Turn>> paths_turns = new HashMap<Server, List<Turn>>();
		final Map<Server, List<Server>> paths_servers = new HashMap<Server, List<Server>>();

		paths_turns.put(src, new LinkedList<Turn>());
		paths_servers.put(src, new LinkedList<Server>(Collections.singleton(src)));

		final LinkedList<Server> queue = new LinkedList<Server>();
		queue.add(src);
		visited.add(src);

		while (!queue.isEmpty()) {
			final Server s = queue.getLast();
			queue.remove(s);

			Set<Server> successors_s = this.filterServersByPriority(priority, this.serverGraph.getSuccessors(s));
			for (Server successor : successors_s) {

				final LinkedList<Turn> path_turns_tmp = new LinkedList<Turn>(paths_turns.get(s));

				final LinkedList<Server> path_servers_tmp;
				if (paths_servers.containsKey(s)) {
					path_servers_tmp = new LinkedList<Server>(paths_servers.get(s));
				} else {
					path_servers_tmp = new LinkedList<Server>(Collections.singleton(src));
				}

				path_turns_tmp.add(this.serverGraph.findTurn(s, successor));
				path_servers_tmp.add(successor);

				if (!visited.contains(successor)) {
					paths_turns.put(successor, path_turns_tmp);
					paths_servers.put(successor, path_servers_tmp);

					queue.add(successor);
					visited.add(successor);
				} else {
					if (paths_turns.get(successor).size() > path_turns_tmp.size()) {
						paths_turns.put(successor, path_turns_tmp);
						paths_servers.put(successor, path_servers_tmp);

						queue.add(successor);
					}
				}
			}
		}

		if (paths_turns.get(snk) == null) {
			throw new Exception("No path from server " + src.getAlias() + " to server " + snk.getAlias() + " found");
		}

		return paths_turns.get(snk);
	}

	public Server addServer(String stringSTServerAlias, ServiceCurve stServiceCurve, int priority, Multiplexing multiplexing) {
		final Server server = this.serverGraph.addServer(stringSTServerAlias, stServiceCurve, multiplexing);
		this.setServerPriority(server, priority);
		return server;
	}

	public Server addServer(String stringSTServerAlias, ServiceCurve stServiceCurve, int priority) {
		final Server server = this.serverGraph.addServer(stringSTServerAlias, stServiceCurve);
		this.setServerPriority(server, priority);
		return server;
	}

	public Server addServer(String serverAlias, ServiceCurve serviceCurve, Multiplexing multiplexing) {
		final Server server = this.serverGraph.addServer(serverAlias, serviceCurve, multiplexing);
		return server;
	}

	public Server addServer(String stringSTServerAlias, ServiceCurve stServiceCurve) {
		final Server server = this.serverGraph.addServer(stringSTServerAlias, stServiceCurve);
		this.setServerPriority(server, TASWindowsBuilder.KEY_PHY_NO_PRIORITY);
		return server;
	}

	public Turn addTurn(String turnAlias, Server sourceSTServer, Server sinkPhyServer) throws Exception {
		return this.serverGraph.addTurn(turnAlias, sourceSTServer, sinkPhyServer);
	}

	public void addFlowTokenBucket(double rate, DataUnit rateUnit, double burst, DataUnit burstUnit, EthernetDevice sourceDevice, EthernetDevice sinkDevice) throws Exception {

		final ArrivalCurve arrivalCurve = Curve.getFactory().createTokenBucket(
				DataUnit.convert(rate, rateUnit, DataUnit.b), 
				DataUnit.convert(burst, burstUnit, DataUnit.b));

		final EthernetFlow ethernetFlow = new EthernetFlow(null, null, arrivalCurve);

		//		if(Objects.equals(this.networkConfigBuilder.getExecutionConfig().plcaModeling, ExecutionConfig.PLCAModeling.SEPARATED_PLCA_SERVER_MODELING))
		//			addEthernetFlow_NoPlcaServer(ethernetFlow, sourceDevice, sinkDevice);
		//		else
		//			addEthernetFlow_PlcaServer(ethernetFlow, sourceDevice, sinkDevice);

		switch(this.tasWindowsBuilder.getExecutionConfig().plcaModeling) {
		case SEPARATED_PLCA_SERVER_MODELING:
			addEthernetFlow_NoPlcaServer(ethernetFlow, sourceDevice, sinkDevice);
			break;
		case SINGLE_PLCA_SERVER_MODELING:
			addEthernetFlow_PlcaServer(ethernetFlow, sourceDevice, sinkDevice);
			break;
		}
	}

	public void addFlowTokenBucket(String alias, double rate, DataUnit rateUnit, double burst, DataUnit burstUnit, EthernetDevice sourceDevice, EthernetDevice sinkDevice) throws Exception {

		final ArrivalCurve arrivalCurve = Curve.getFactory().createTokenBucket(
				DataUnit.convert(rate, rateUnit, DataUnit.b), 
				DataUnit.convert(burst, burstUnit, DataUnit.b));

		final EthernetFlow ethernetFlow = new EthernetFlow(null, null, arrivalCurve);

		switch(this.tasWindowsBuilder.getExecutionConfig().plcaModeling) {
		case SEPARATED_PLCA_SERVER_MODELING:
			addEthernetFlow_NoPlcaServer(ethernetFlow, sourceDevice, sinkDevice);
			break;
		case SINGLE_PLCA_SERVER_MODELING:
			addEthernetFlow_PlcaServer(ethernetFlow, sourceDevice, sinkDevice);
			break;
		}

		//		if(Objects.equals(this.networkConfigBuilder.getExecutionConfig().plcaModeling, ExecutionConfig.PLCAModeling.SEPARATED_PLCA_SERVER_MODELING))
		//			addEthernetFlow_NoPlcaServer(ethernetFlow, sourceDevice, sinkDevice);
		//		else
		//			addEthernetFlow_PlcaServer(ethernetFlow, sourceDevice, sinkDevice);
	}

	public void printDevicesBacklog() {

		final NumberFormat nf = new DecimalFormat("#0.000");

		System.out.println("--- Ethernet Devices Backlog Report ---");
		for(Entry<String, Map<EthernetDevice, Double>> entry : this.mapAnalysisNameBacklogDevice.entrySet()) {

			System.out.println(" -- Analysis: " + entry.getKey() + " --");

			//Converting Collection to Set and then to List to avoid duplicates
			List<EthernetDevice> devices = new LinkedList<EthernetDevice>(new HashSet<EthernetDevice>(this.mapServerEthernetDevices.values()));

			//Sort by device name
			devices.sort((EthernetDevice o1, EthernetDevice o2) -> o1.getName().compareTo(o2.getName()));

			for(EthernetDevice device : devices) {
				Double backlog = entry.getValue().get(device);
				if(backlog == null)
					backlog = 0.0;

				System.out.println("Device=" + device + ", Backlog=" + nf.format(DataUnit.convert(backlog, DataUnit.b, DataUnit.kb)) + " kb");
			}

			System.out.println();
		}

	}

	public void saveCurves() throws IOException{

		final String absolutePath = this.tasWindowsBuilder.getDatasetReader().getResultAbsolutePath();

		final File dir = new File(absolutePath+"/Curves_charts");
		if(!dir.exists())
			dir.mkdirs();

		final ChartUtilNCCurve curvePlotUtil = new ChartUtilNCCurve();

		switch(this.tasWindowsBuilder.getExecutionConfig().generateNCCurvesCharts) {
		case GENERATE_ARRIVAL_CURVES_CHARTS_ONLY:
		case ALL:
		{

			for(EthernetFlow ethernetFlow : this.ethernetFlows) {
				final String nameDescription = ethernetFlow.getAlias()
						//.replaceAll("\\[", "_")
						//.replaceAll("\\]", "_")
						.replaceAll("\\>", "");
				curvePlotUtil.saveCurveChart(ethernetFlow.getArrivalCurve(), ethernetFlow.getAlias(), "Arrival Curve", dir.getPath() + "/AC_" + nameDescription);
			}

		}
		break;

		case NO:
		default:
			break;
		}


		switch(this.tasWindowsBuilder.getExecutionConfig().generateNCCurvesCharts) {
		case GENERATE_SERVICE_CURVES_CHARTS_ONLY:
		case ALL:
		{

			for(EthernetDevice device : this.devices) {

				for(EthernetInterface iface : device.getInterfaces()) {

					for(int i = 0 ; i < 8 ; i++) {

						final STServerData stServerData = iface.getSTServerData(i);

						if(stServerData != null) {

							final String nameDescription = stServerData.getSTServer().getAlias()
									//.replaceAll("\\[", "_")
									//.replaceAll("\\]", "_")
									.replaceAll("\\>", "");
							curvePlotUtil.saveCurveChart(stServerData.getServiceCurve(), stServerData.getSTServer().getAlias(), "Service Curve", dir.getPath() + "/SC_" + nameDescription);

						}
						
						
						final PlcaServerData plcaServerData = iface.getPhyServerData(i);

						if(plcaServerData != null) {

							final String nameDescription = plcaServerData.getPlcaServer().getAlias()
									//.replaceAll("\\[", "_")
									//.replaceAll("\\]", "_")
									.replaceAll("\\>", "");
							curvePlotUtil.saveCurveChart(plcaServerData.getServiceCurve(), plcaServerData.getPlcaServer().getAlias(), "Service Curve", dir.getPath() + "/SC_" + nameDescription);

						}
						
					}
					
					final PlcaServerData singlePlcaServerData = iface.getPhyServerData(TASWindowsBuilder.KEY_PHY_NO_PRIORITY);

					if(singlePlcaServerData != null) {

						final String nameDescription = singlePlcaServerData.getPlcaServer().getAlias()
								//.replaceAll("\\[", "_")
								//.replaceAll("\\]", "_")
								.replaceAll("\\>", "");
						curvePlotUtil.saveCurveChart(singlePlcaServerData.getServiceCurve(), singlePlcaServerData.getPlcaServer().getAlias(), "Service Curve", dir.getPath() + "/SC_" + nameDescription);

					}
					
				}
			}

		}
		break;

		case NO:
		default:
			break;
		}

	}

	public void performAnalysis(){

		final String strBefore = "mod_DavidAlain_";
		final int indexBegin = this.tasWindowsBuilder.getDatasetReader().getDirpath().indexOf(strBefore) + strBefore.length();
		final int indexEnd = this.tasWindowsBuilder.getDatasetReader().getDirpath().lastIndexOf("TABLE") - 1;
		final String networkCaseStr = this.tasWindowsBuilder.getDatasetReader().getDirpath().substring(indexBegin, indexEnd);
		NetworkCase networkCase = AnalysesResultPaperAccess2018.NetworkCase.parse(networkCaseStr);

		final String datasetCase = this.tasWindowsBuilder.getDatasetReader().getDirpath().substring(
				this.tasWindowsBuilder.getDatasetReader().getDirpath().length()-6, 
				this.tasWindowsBuilder.getDatasetReader().getDirpath().length()-3);

		//Order flows by alias
		final List<EthernetFlow> ethernetFlowsList = new LinkedList<>(this.ethernetFlows);
		ethernetFlowsList.sort(new Comparator<EthernetFlow>() {
			@Override
			public int compare(EthernetFlow o1, EthernetFlow o2) {
				return o1.getAlias().compareTo(o2.getAlias());
			}
		});

		/**
		 * Perform analysis 
		 */
		for(EthernetFlow ethernetFlow : ethernetFlowsList) {

			Flow flow_of_interest = ethernetFlow.getFlow();

			final String flowName = flow_of_interest.getAlias().split("_")[0];
			final double deadlineUs = this.tasWindowsBuilder.getMapFlowNameSTFlowData().get(flowName).getSTMessageEntry().deadlineUs;

			if(this.tasWindowsBuilder.getExecutionConfig().analysesResult != null)
				this.tasWindowsBuilder.getExecutionConfig().analysesResult.put(networkCase, tasWindowsBuilder.getExecutionConfig().multiplexing, datasetCase, flowName, AnalysesResultPaperAccess2018.Key.DEADLINE, deadlineUs);

			System.out.println("Flow of interest : " + flow_of_interest.toString());
			System.out.println();

			// Analyze the network
			// TFA
			System.out.println("--- Total Flow Analysis ---");
			TotalFlowAnalysis tfa = new TotalFlowAnalysis(this.serverGraph, analysisConfig);

			try {
				tfa.performAnalysis(flow_of_interest);
				System.out.println("delay bound     : " + tfa.getDelayBound());
				System.out.println("     delay per server : " + tfa.getServerDelayBoundMapString());
				System.out.println("backlog bound   : " + tfa.getBacklogBound());
				System.out.println("     backlog per server : " + tfa.getServerBacklogBoundMapString());
				System.out.println("alpha per server: " + tfa.getServerAlphasMapString());

				this.saveBacklog(tfa);

				if(this.tasWindowsBuilder.getExecutionConfig().analysesResult != null)
					this.tasWindowsBuilder.getExecutionConfig().analysesResult.put(networkCase, tasWindowsBuilder.getExecutionConfig().multiplexing, datasetCase, flowName, AnalysesResultPaperAccess2018.Key.TFA_DELAY_BOUND, tfa.getDelayBound().doubleValue());

			} catch (Exception e) {
				System.out.println("TFA analysis failed");
				e.printStackTrace();
			}

			System.out.println();

			// SFA
			System.out.println("--- Separated Flow Analysis ---");
			SeparateFlowAnalysis sfa = new SeparateFlowAnalysis(this.serverGraph, analysisConfig);

			try {
				sfa.performAnalysis(flow_of_interest);
				System.out.println("e2e SFA SCs     : " + sfa.getLeftOverServiceCurves());
				System.out.println("     per server : " + sfa.getServerLeftOverBetasMapString());
				System.out.println("xtx per server  : " + sfa.getServerAlphasMapString());
				System.out.println("delay bound     : " + sfa.getDelayBound());
				System.out.println("backlog bound   : " + sfa.getBacklogBound());

				if(this.tasWindowsBuilder.getExecutionConfig().analysesResult != null)
					this.tasWindowsBuilder.getExecutionConfig().analysesResult.put(networkCase, tasWindowsBuilder.getExecutionConfig().multiplexing, datasetCase, flowName, AnalysesResultPaperAccess2018.Key.SFA_DELAY_BOUND, sfa.getDelayBound().doubleValue());

			} catch (Exception e) {
				System.out.println("SFA analysis failed");
				e.printStackTrace();
			}

			System.out.println();

			/*
			if(tasWindowsBuilder.getExecutionConfig().multiplexing == Multiplexing.ARBITRARY) {

				//PMOO and TMA runs only under ARBITRARY multiplexing
				//FIFO multiplexing cannot be used for PMOO and TMA

				// PMOO
				System.out.println("--- PMOO Analysis ---");
				PmooAnalysis pmoo = new PmooAnalysis(this.serverGraph, analysisConfig);

				try {
					pmoo.performAnalysis(flow_of_interest);
					System.out.println("e2e PMOO SCs    : " + pmoo.getLeftOverServiceCurves());
					System.out.println("xtx per server  : " + pmoo.getServerAlphasMapString());
					System.out.println("delay bound     : " + pmoo.getDelayBound());
					System.out.println("backlog bound   : " + pmoo.getBacklogBound());

					if(this.tasWindowsBuilder.getExecutionConfig().analysesResult != null)
						this.tasWindowsBuilder.getExecutionConfig().analysesResult.put(networkCase, tasWindowsBuilder.getExecutionConfig().multiplexing, datasetCase, flowName, AnalysesResultPaperAccess2018.Key.PMOO_DELAY_BOUND, pmoo.getDelayBound().doubleValue());

				} catch (Exception e) {
					System.out.println("PMOO analysis failed");
					e.printStackTrace();
				}

				System.out.println();

				// TMA
				System.out.println("--- Tandem Matching Analysis ---");
				TandemMatchingAnalysis tma = new TandemMatchingAnalysis(this.serverGraph, analysisConfig);

				try {
					tma.performAnalysis(flow_of_interest);
					System.out.println("e2e TMA SCs     : " + tma.getLeftOverServiceCurves());
					System.out.println("xtx per server  : " + tma.getServerAlphasMapString());
					System.out.println("delay bound     : " + tma.getDelayBound());
					System.out.println("backlog bound   : " + tma.getBacklogBound());

					if(this.tasWindowsBuilder.getExecutionConfig().analysesResult != null)
						this.tasWindowsBuilder.getExecutionConfig().analysesResult.put(networkCase, tasWindowsBuilder.getExecutionConfig().multiplexing, datasetCase, flowName, AnalysesResultPaperAccess2018.Key.TMA_DELAY_BOUND, tma.getDelayBound().doubleValue());

				} catch (Exception e) {
					System.out.println("TMA analysis failed");
					e.printStackTrace();
				}

			}

			System.out.println();
			 */

		}

		System.out.println();
	}


	public void printEthernetDeviceInfo(EthernetDevice ethernetDevice) {

		System.out.println();

		System.out.println(ethernetDevice.getName() + "'s neigborhood:");
		DataPrinterUtil.print(ethernetDevice.getNeighbors());
		System.out.println();

		System.out.println(ethernetDevice.getName() + "'s servers:");
		for(EthernetInterface ethernetInterface : ethernetDevice.getInterfaces()) {
			DataPrinterUtil.print(ethernetInterface.getSTServers());
			DataPrinterUtil.print(ethernetInterface.getPhyServers());
		}
		System.out.println();

		System.out.println(ethernetDevice.getName() + "'s output turns:");
		for(EthernetInterface ethernetInterface : ethernetDevice.getInterfaces()) {
			System.out.println("Servers at interface " + ethernetInterface.toString());
			for(Server s : ethernetInterface.getSTServers()) {
				DataPrinterUtil.print(this.serverGraph.getOutTurns(s));
			}
			for(Server s : ethernetInterface.getPhyServers()) {
				DataPrinterUtil.print(this.serverGraph.getOutTurns(s));
			}
			System.out.println();
		}

		System.out.println("---------------------------------");
	}

	public void printEthernetFlowInfo(EthernetFlow ethernetFlow) throws Exception {

		Flow flow_of_interest = ethernetFlow.getFlow();
		System.out.println("Flow: " + flow_of_interest.getAlias());

		final List<String> list = new LinkedList<>();
		final List<Turn> turns = flow_of_interest.getTurnsOnPath();

		list.add(turns.get(0).getSource().getAlias());

		System.out.println("Path:");
		for(Turn turn : turns) {

			final String alias = turn.getDest().getAlias();

			if(!list.contains(alias)) {
				list.add(turn.getDest().getAlias());
			}
		}

		for(String serverStr : list) {
			System.out.println("\t" + serverStr);	
		}

		System.out.println();
	}

	public void printAllServersAndTurns() {

		final List<Server> listServers = new LinkedList<Server>(this.serverGraph.getServers());
		//Sort by Alias
		listServers.sort((Server s1, Server s2) -> s1.getAlias().compareTo(s2.getAlias()));

		final List<Turn> listTurns = new LinkedList<Turn>(this.serverGraph.getTurns());
		//Sort by Alias
		listTurns.sort((Turn t1, Turn t2) -> t1.getAlias().compareTo(t2.getAlias()));

		System.out.println(DataPrinterUtil.toString(listServers));
		System.out.println();
		System.out.println(DataPrinterUtil.toString(listTurns));

		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");
		System.out.println(DataPrinterUtil.toString2(tasWindowsBuilder.getMapInterfaceInfoPrioritySTFlowDataList()));
		System.out.println("+++++++++++++++++++++++++++++++++++++++++++++");

	}

	public void printAllEthernetFlows() {

		final List<EthernetFlow> sortedList = new LinkedList<EthernetFlow>(ethernetFlows);

		sortedList.sort((EthernetFlow o1, EthernetFlow o2) -> o1.getAlias().compareTo(o2.getAlias()));

		/**
		 * Print all flows
		 */
		for(EthernetFlow ethernetFlow : sortedList) {
			System.out.println(ethernetFlow.toStringMultilined());
		}
	}

	public Set<EthernetDevice> getDevices(){
		return new HashSet<EthernetDevice>(this.devices);
	}

	/**
	 * Gets the InterfaceInfo instance, discover its related source and destination device's names,
	 *  and return the output interface's instance from source device which connects to destination device. 
	 * 
	 * @param ethernetNetwork
	 * @param interfaceInfo
	 * 
	 * @return	An EthernetInterface
	 */
	public final EthernetInterface getOutputInterfaceByInterfaceInfo(InterfaceInfoEntry interfaceInfo) {
		return this.getDeviceByName(interfaceInfo.deviceName).getInterface(interfaceInfo.interfaceId);
	}

	public final EthernetDevice getDeviceByName(String deviceName) {

		final Set<EthernetDevice> devices = this.getDevices();

		for(EthernetDevice dev : devices) {

			if(Objects.equals(dev.getName(), deviceName)) {
				return dev;
			}

		}

		throw new InvalidParameterException("There is no device with name " + deviceName);
	}

	/**
	 * Build Servers and Internal Turns
	 * 
	 * @throws Exception
	 */
	public void buildOutputServersAndInternalTurns() throws Exception {
		buildOutputServersAndInternalTurns(this.devices, this.tasWindowsBuilder);
	}

	/**
	 * Discover all device's name and interface's id, 
	 * 	and
	 * Build all devices and put all related interfaces
	 * 
	 * @param networkConfigBuilder
	 * @return
	 */
	private final Set<EthernetDevice> buildDevices(TASWindowsBuilder networkConfigBuilder){

		final Set<EthernetDevice> devices = new HashSet<EthernetDevice>();

		final Map<String, Set<Integer>> mapDeviceNameInterfaceIds = new HashMap<String, Set<Integer>>();

		/**
		 * Discover all device's name and interface's id
		 */
		for(Entry<InterfaceInfoEntry, LinkInfo> entry : networkConfigBuilder.getMapInterfaceInfoLinkInfo().entrySet()) {

			final String deviceName = entry.getKey().deviceName;
			final int interfaceId = entry.getKey().interfaceId;

			final Set<Integer> idSet = mapDeviceNameInterfaceIds.getOrDefault(deviceName, new HashSet<Integer>());
			idSet.add(interfaceId);
			mapDeviceNameInterfaceIds.put(deviceName, idSet);
		}


		/**
		 * Build all devices and put all related interfaces
		 */
		for(Entry<String, Set<Integer>> entry : mapDeviceNameInterfaceIds.entrySet()) {

			final String deviceName = entry.getKey();
			final int interfacesCount = entry.getValue().size();
			final EthernetDevice device;
			if(interfacesCount > 1) {
				device = new EthernetSwitch(this, deviceName);
			} else {
				device = new EthernetEndSystem(this, deviceName);
			}

			for(int id : entry.getValue()) {
				device.putInterface(id);
			}

			devices.add(device);
		}


		return devices;
	}

	/**
	 * Build Servers and Internal Turns
	 * 
	 * @param devices
	 * @param networkConfigBuilder
	 * @throws Exception
	 */
	private void buildOutputServersAndInternalTurns(Set<EthernetDevice> devices, TASWindowsBuilder networkConfigBuilder) throws Exception {

		for(EthernetDevice device : devices) {
			for(EthernetInterface ethernetInterface : device.getInterfaces()) {

				switch(this.tasWindowsBuilder.getExecutionConfig().plcaModeling) {
				case SEPARATED_PLCA_SERVER_MODELING:
					ethernetInterface.buildOutputServersAndInternalTurns_NoPlcaServer(networkConfigBuilder);
					break;
				case SINGLE_PLCA_SERVER_MODELING:
					ethernetInterface.buildOutputServersAndInternalTurns_PlcaServer(networkConfigBuilder);
					break;
				}

				//				if(Objects.equals(this.networkConfigBuilder.getExecutionConfig().plcaModeling, ExecutionConfig.PLCAModeling.SEPARATED_PLCA_SERVER_MODELING))
				//					ethernetInterface.buildOutputServersAndInternalTurns_NoPlcaServer(networkConfigBuilder);
				//				else
				//					ethernetInterface.buildOutputServersAndInternalTurns_PlcaServer(networkConfigBuilder);
			}
		}
	}

	/**
	 * Build links
	 * 
	 * @param networkConfigBuilder
	 * @throws Exception
	 */
	private void buildLinks(TASWindowsBuilder networkConfigBuilder) throws Exception {
		/**
		 * Build links
		 */
		for(Entry<InterfaceInfoEntry, LinkInfo> entry : networkConfigBuilder.getMapInterfaceInfoLinkInfo().entrySet()) {

			Set<EthernetInterface> interfaces = new HashSet<EthernetInterface>(); //Using Set to avoid duplicates

			for(InterfaceInfoEntry info : entry.getValue().getInterfaceInfoSet()) {
				interfaces.add(this.getDeviceByName(info.deviceName).getInterface(info.interfaceId));
			}

			//Remove the first one to avoid creating link with repeated interfaces
			final List<EthernetInterface> others = new LinkedList<EthernetInterface>(interfaces);
			final EthernetInterface target = others.remove(0);

			if(target.getLink() == null)
				target.linkTo(others);

		}
	}

	/**
	 * Add all flows got from dataset
	 * 
	 * @param tasWindowsBuilder
	 * @throws Exception
	 */
	private void addFlowsFromDataset(TASWindowsBuilder tasWindowsBuilder) throws Exception {

		for(Entry<STFlowData,VirtualLinkEntry> entry : tasWindowsBuilder.getMapSTFlowDataVirtualLink().entrySet()) {

			final STFlowData stFlowData = entry.getKey();
			final VirtualLinkEntry virtualLinkEntry = entry.getValue();
			final int priority = stFlowData.getSTMessageEntry().priority;

			final EthernetDevice source = this.getDeviceByName(virtualLinkEntry.getSource().deviceName);
			final EthernetDevice destination = this.getDeviceByName(virtualLinkEntry.getDestination().deviceName);
			final String flowAlias = stFlowData.getSTMessageEntry().flowName + 
					"_[" + 
					(virtualLinkEntry.getSource().deviceName + "." + virtualLinkEntry.getSource().interfaceId) +
					"->" +
					(virtualLinkEntry.getDestination().deviceName + "." + virtualLinkEntry.getDestination().interfaceId) +
					"]";

			final EthernetFlow ethernetFlow = new EthernetFlow(flowAlias, priority, stFlowData.getArrivalCurve(), stFlowData);

			//Check if all frames fit into their related priority queue guaranteed window in the whole flow's path
			validateScheduling(entry, this.tasWindowsBuilder.getExecutionConfig().validateSchedulingForFrameSize);

			//			if(this.networkConfigBuilder.getExecutionConfig().validateSchedulingForFrameSize == ExecutionConfig.ValidateSchedulingForFrameSize.YES)
			//				validateScheduling(entry);

			switch(this.tasWindowsBuilder.getExecutionConfig().plcaModeling) {
			case SEPARATED_PLCA_SERVER_MODELING:
				addEthernetFlow_NoPlcaServer(ethernetFlow, source, destination);
				break;
			case SINGLE_PLCA_SERVER_MODELING:
				addEthernetFlow_PlcaServer(ethernetFlow, source, destination);
				break;
			}

			//			if(Objects.equals(this.networkConfigBuilder.getExecutionConfig().plcaModeling, ExecutionConfig.PLCAModeling.SEPARATED_PLCA_SERVER_MODELING))
			//				addEthernetFlow_NoPlcaServer(ethernetFlow, source, destination);
			//			else
			//				addEthernetFlow_PlcaServer(ethernetFlow, source, destination);
		}

	}

	private void buildNetwork() throws Exception {

		/**
		 * Calculate all TASWindows
		 */
		this.tasWindowsBuilder.build();

		/**
		 * Discover all device's name and interface's id,
		 * 	and
		 * Build all devices and put all related interfaces
		 */
		this.devices = buildDevices(this.tasWindowsBuilder);

		/**
		 * Build Servers and Internal Turns
		 */
		buildOutputServersAndInternalTurns(this.devices, this.tasWindowsBuilder);

		/**
		 * Build links
		 */
		buildLinks(this.tasWindowsBuilder);

		System.out.println("##################################################################");
		this.printAllServersAndTurns();
		System.out.println("##################################################################");

		/**
		 * Add all flows got from dataset
		 */
		addFlowsFromDataset(this.tasWindowsBuilder);

		System.out.println("==================================================================");
		this.printAllEthernetFlows();
		System.out.println("==================================================================");



		switch(tasWindowsBuilder.getExecutionConfig().saveServerGraph) {
		case YES:

			final String absolutePath = this.tasWindowsBuilder.getDatasetReader().getResultAbsolutePath();

			final int tableIndex = absolutePath.indexOf("TABLE_") + "TABLE_".length();
			final int caseIndex = absolutePath.lastIndexOf("_in");

			final String tableNum = absolutePath.substring(tableIndex, tableIndex + 1);
			final String caseNum = absolutePath.substring(caseIndex - 1, caseIndex);

			this.serverGraph.saveAs(absolutePath, "ServerGraph_Table"+tableNum+"_Case"+caseNum+".java");

			break;
		default:
			break;
		}

	}

	/**
	 * Check if all frames fits into its related priority queue scheduling window in full path
	 * 
	 * @param entry
	 */
	private void validateScheduling(Entry<STFlowData,VirtualLinkEntry> entry, ValidateSchedulingForFrameSize validateSchedulingForFrameSize) {

		if(validateSchedulingForFrameSize == ValidateSchedulingForFrameSize.NO)
			return;

		final STFlowData stFlowData = entry.getKey();
		final VirtualLinkEntry virtualLinkEntry = entry.getValue();
		final int priority = stFlowData.getSTMessageEntry().priority;

		//Check if all frames fits into its related priority queue scheduling window in full path
		virtualLinkEntry.route.forEach(pair -> {
			final EthernetInterface ethernetInterface = this.getOutputInterfaceByInterfaceInfo(pair.getFirst());
			final STServerData stServerData = ethernetInterface.getSTServerData(priority);

			stServerData.getTASWindowTermsList().forEach(window -> {
				final double transmissionTimeMessageMaxSize = ethernetInterface.getPhyStandard().transmissionTimeUs(stFlowData.getMessageMaxSizeBytes());

				final double guaranteedLengthForTransmission;
				switch(this.tasWindowsBuilder.getExecutionConfig().plcaModeling) {
				case SEPARATED_PLCA_SERVER_MODELING:		guaranteedLengthForTransmission = (window.L_bar_i_Pm + window.d_gb_Pm - window.d_i_PLCA);	break;
				case SINGLE_PLCA_SERVER_MODELING:	guaranteedLengthForTransmission = (window.L_bar_i_Pm + window.d_gb_Pm);						break;
				default:							
					throw new IllegalArgumentException("Unexpected value: " + this.tasWindowsBuilder.getExecutionConfig().plcaModeling);
				}

				//				if(Objects.equals(this.networkConfigBuilder.getExecutionConfig().plcaModeling, ExecutionConfig.PLCAModeling.SEPARATED_PLCA_SERVER_MODELING))
				//					guaranteedTimeWindow = (window.L_bar_i_Pm - window.d_i_PLCA);
				//				else
				//					guaranteedTimeWindow = (window.L_bar_i_Pm);

				if((window.L_bar_i_Pm <= 0.0) || (transmissionTimeMessageMaxSize > guaranteedLengthForTransmission)) {
					
					final String errorMessage = "\n" 
							+ "ST Flow "+stFlowData.getSTMessageEntry().flowName+" has a frame which is larger than the guaranteed window in some Ethernet interface on its path.\n"
							+ "required transmission time={"+transmissionTimeMessageMaxSize+" us}\n"
							+ "guaranteed window length={"+window.L_bar_i_Pm+" us}\n"
							+ "guaranteed length for transmission={" + guaranteedLengthForTransmission + "}\n"
							+ "ethernetInterface="+ethernetInterface+"\n"
							+ "window=" + window;

					if(validateSchedulingForFrameSize == ValidateSchedulingForFrameSize.YES_PRINT_IF_INVALID) {

						System.err.println("Warning: "+(ValidateSchedulingForFrameSize.YES_PRINT_IF_INVALID) + errorMessage);

					} else if(validateSchedulingForFrameSize == ValidateSchedulingForFrameSize.YES_THROW_ERROR_IF_INVAID) {

						throw new InvalidParameterException(errorMessage);

					} else {
						
						throw new RuntimeException("Condition not checked! You must implement it first.");
					}

				}
			});
		});

	}

	public final Integer getServerPriority(Server server) {
		return this.mapServerPriority.get(server);
	}

	public final void setServerPriority(Server server, Integer priority) {
		this.mapServerPriority.put(server, priority);
	}


}
