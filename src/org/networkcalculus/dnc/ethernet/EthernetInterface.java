package org.networkcalculus.dnc.ethernet;

import java.security.InvalidParameterException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.networkcalculus.dnc.AnalysisConfig.Multiplexing;
import org.networkcalculus.dnc.curves.ServiceCurve;
import org.networkcalculus.dnc.ethernet.plca.PlcaServerData;
import org.networkcalculus.dnc.ethernet.tsn.TASCurveBuilder;
import org.networkcalculus.dnc.ethernet.tsn.TASWindowsBuilder;
import org.networkcalculus.dnc.ethernet.tsn.data.STServerData;
import org.networkcalculus.dnc.ethernet.tsn.entry.InterfaceInfoEntry;
import org.networkcalculus.dnc.network.server_graph.Server;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 * 
 */
public class EthernetInterface {
	
	/**
	 * Interface id in this ethernetDevice
	 */
	private int id;

	/**
	 * Device owner of this interface
	 */
	private EthernetDevice ethernetDeviceOwner;


	private EthernetPhyStandard phyStandard;

	/**
	 * Map<Priority,ServiceCurve>
	 * 
	 * Service Curve and Server instances are inside TSNPriorityQueueServerData correlated to its priority queue.
	 * 
	 * This must be set when:
	 * 		A interface are using TSN.
	 * 
	 *  Otherwise:
	 *  	This must be null.
	 */
	private Map<Integer,STServerData> mapPrioritySTServerData;

	/**
	 * 
	 */
	private Map<Integer, Integer> mapPriorityPLCAWeightWRR;
	
	/**
	 * 
	 */
	private Map<Integer,PlcaServerData> mapPriorityPhyServerData;

	/**
	 * This must be set when:
	 * 		An interface is point-to-multi-point (e.g. 10BASE-T1S), or
	 * 		An interface is point-to-point without TSN.
	 * 
	 * Otherwise:
	 * 		This must be null.
	 */
	//	private Map<Integer,Server> mapPriorityPhyServer;

	/**
	 * 
	 * @param ethernetDeviceOwner
	 * @param id
	 * @param phyStandard
	 * @throws Exception
	 */
	public EthernetInterface(EthernetDevice ethernetDeviceOwner, int id, EthernetPhyStandard phyStandard) {
		if(ethernetDeviceOwner == null)
			throw new InvalidParameterException("ethernetDeviceOwner cannot be null");
		if(id < 0)
			throw new InvalidParameterException("interfaceId cannot be negative");

		this.id = id;
		this.ethernetDeviceOwner = ethernetDeviceOwner;
		this.phyStandard = phyStandard;

		this.mapPrioritySTServerData = new HashMap<Integer, STServerData>();
		this.mapPriorityPhyServerData = new HashMap<Integer, PlcaServerData>();
		//		this.mapPriorityPhyServer = new HashMap<Integer, Server>();
		this.mapPriorityPLCAWeightWRR = new HashMap<Integer, Integer>();
	}

	public void linkTo(Collection<EthernetInterface> interfaces) throws Exception {
		final EthernetInterface[] e = new EthernetInterface[interfaces.size()];
		linkTo(interfaces.toArray(e));
	}

	/**
	 * 
	 * Note: You cannot add an interface to a already created link. You must create a link using all required interfaces at once.
	 * 
	 * @param neighborInterfaces
	 * @throws Exception
	 */
	public void linkTo(EthernetInterface ... neighborInterfaces) throws Exception {

		final EthernetLink link = new EthernetLink(this, neighborInterfaces);

		/**
		 * Sanity checks
		 */
		//Point-to-point links must have be created by using only two interfaces
		if(neighborInterfaces.length > 1) {

			if(this.phyStandard.isDedicatedMediumAccess) 
				throw new InvalidParameterException(this + " is interface with Dedicated Medium Access and cannot be linked to more than one interface");

			for(EthernetInterface neighborInterface : neighborInterfaces) {
				if(neighborInterface.phyStandard.isDedicatedMediumAccess)
					throw new InvalidParameterException(neighborInterface + " is interface with Dedicated Access to the Medium and cannot be linked to more than one interface");
			}
		}

		//Only non-linked interfaces can be linked. 
		//Note: thus, you cannot add an interface to a already created link. You must create a link using all required interfaces at once.
		if(this.getEthernetDeviceOwner().getLink(this) != null)
			throw new InvalidParameterException(this + " is already linked");

		//Only non-linked interfaces can be linked.
		//Note: thus, you cannot add an interface to a already created link. You must create a link using all required interfaces at once.
		for(EthernetInterface neighborInterface : neighborInterfaces) {
			if(neighborInterface.getEthernetDeviceOwner().getLink(neighborInterface) != null)
				throw new InvalidParameterException(neighborInterface + " is already linked");			
		}

		//Check if both interfaces are interfaces with Dedicated Medium Access
		if((neighborInterfaces.length == 1) && ((this.phyStandard.isDedicatedMediumAccess != true) || (neighborInterfaces[0].phyStandard.isDedicatedMediumAccess != true)))
			throw new InvalidParameterException("It is not possible to link an interface with Dedicated Access to the Medium to interface with Shared Access to the Medium");

		//Adding links their correlated devices
		this.ethernetDeviceOwner.links.put(this.id, link);
		for(EthernetInterface neighborInterface : neighborInterfaces) {
			neighborInterface.ethernetDeviceOwner.links.put(neighborInterface.id, link);
		}

		EthernetInterface.addTurns(link);
	}

	private static void addTurns(EthernetLink link) throws Exception {

		if(link == null)
			throw new InvalidParameterException("link must not be null");

		for(EthernetInterface outputInterface : link.getInterfaces()) {
			for(EthernetInterface inputInterface : link.getInterfaces()) {

				//An interface cannot have a Turn to itself Server's output
				if(!Objects.equals(inputInterface, outputInterface)) {
					EthernetInterface.addTurns(outputInterface, inputInterface);
				}
			}
		}

	}

	private static void addTurns(EthernetInterface outputInterface, EthernetInterface neighborInputInterface) throws Exception {

		if(outputInterface == null)
			throw new InvalidParameterException("outputInterface must not be null");
		if(neighborInputInterface == null)
			throw new InvalidParameterException("neighborInputInterface must not be null");

		final EthernetDevice neighborDevice = neighborInputInterface.getEthernetDeviceOwner();
		final Set<EthernetInterface> neighborOutputInterfaces = neighborDevice.getInterfaces();

		neighborOutputInterfaces.remove(neighborInputInterface); //Do not create a Turn to an input interface

		/**************************************************************************************************************************
		 * 
		 * All possible combinations for ST servers and PHY servers in the output interface:
		 * 
		 * 		a) Some ST servers and no PHY servers 						=> TSN without PLCA
		 * 		b) Some ST servers and the same quantity for PHY servers 	=> TSN and PLCA (separated PLCA modeling)
		 * 		c) Some ST servers and only one PHY server 					=> TSN and PLCA (one PLCA modeling)
		 * 		d) No ST servers and only one PHY server					=> No TSN and (either no PLCA or one PLCA modeling)
		 * 		e) No ST servers and no PHY servers							=> Error! There must be at least one TSN or PHY server
		 * 
		 **************************************************************************************************************************/

		/**
		 * When there is no PHY Servers, then either: 
		 * 	There is no PLCA server, but there must be some TSN servers and they must be directly connected to neighbor servers, or
		 * 	There is an error, because this interface does not have either TSN servers or PHY servers
		 */

		final int outputStServerSize = outputInterface.mapPrioritySTServerData.size();
		final int outputPhyServerSize = outputInterface.mapPriorityPhyServerData.size();
		final PlcaServerData outputPhyServerDataNoPriority = outputInterface.mapPriorityPhyServerData.get(TASWindowsBuilder.KEY_PHY_NO_PRIORITY);

		/**
		 * Case a) Some ST servers and no PHY servers 						=> TSN without PLCA
		 * 
		 */
		if((outputStServerSize > 0) && (outputPhyServerSize == 0)){

			/**
			 * Connect each priority queue ST server to:
			 * 	other server with same priority on neighbor's output interfaces, or 
			 * 	PHY server on neighbor's output interfaces which are not using TSN
			 */

			for(Server sourceSTServer : outputInterface.getSTServers()) {

				final Integer priority = outputInterface.getNetwork().getServerPriority(sourceSTServer);
				if(priority == null)
					throw new InvalidParameterException("sourceSTServer cannot have null priority. Server=" + sourceSTServer);

				for(EthernetInterface neighborOutputInterface : neighborOutputInterfaces) {

					//If neighbor does NOT have ST Servers
					if(neighborOutputInterface.mapPrioritySTServerData.isEmpty()) {

						//Add Turn connecting Servers directly to phyServer
						final Server sinkPhyServer = neighborOutputInterface.mapPriorityPhyServerData.get(TASWindowsBuilder.KEY_PHY_NO_PRIORITY).getPlcaServer();

						if(sinkPhyServer == null)
							throw new InvalidParameterException("Neighbor interface " + neighborOutputInterface + " does not have either ST Servers or PHY Server");

						final String turnAlias = outputInterface.toStringTurnAlias(sourceSTServer, sinkPhyServer);
						outputInterface.getNetwork().addTurn(turnAlias, sourceSTServer, sinkPhyServer);						

					}
					//If neighbor does have ST Servers
					else {

						final STServerData neighborOutputSTServerData = neighborOutputInterface.mapPrioritySTServerData.get(priority);

						//Add Turn only if neighbor's interface have ST server for the same priority
						if(neighborOutputSTServerData != null) {

							final Server sinkServer = neighborOutputSTServerData.getSTServer();
							final String turnAlias = outputInterface.toStringTurnAlias(sourceSTServer, sinkServer);
							outputInterface.getNetwork().addTurn(turnAlias, sourceSTServer, sinkServer);

						}

					}

				}

			}

		} 
		/**
		 * Case b) Some ST servers and the same quantity for PHY servers 	=> TSN and PLCA (separated PLCA modeling)
		 */
		else if ((outputStServerSize > 0) && (outputStServerSize == outputPhyServerSize)) {

			/**
			 * Connect each priority queue ST server to:
			 * 	other server with same priority on neighbor's output interfaces, or 
			 * 	PHY server on neighbor's output interfaces which are not using TSN
			 */

			for(Entry<Integer,PlcaServerData> entry : outputInterface.mapPriorityPhyServerData.entrySet()) {

				final Integer priority = entry.getKey();
				final PlcaServerData phyServerData = entry.getValue();
				final Server sourcePhyServer = phyServerData.getPlcaServer();
				
				final Integer sourcePhyServerPriority = outputInterface.getNetwork().getServerPriority(sourcePhyServer);

				if(sourcePhyServerPriority == null)
					throw new InvalidParameterException("sourcePhyServer cannot have null priority. Server=" + sourcePhyServer);
				if(!Objects.equals(priority,sourcePhyServerPriority))
					throw new InvalidParameterException("phyServer cannot have different priority from map's key. Server.prio=" + sourcePhyServerPriority + ", key=" + priority);

				for(EthernetInterface neighborOutputInterface : neighborOutputInterfaces) {

					//If neighbor does NOT have ST Servers
					if(neighborOutputInterface.mapPrioritySTServerData.isEmpty()) {

						//Add Turn connecting Servers directly to phyServer
						final Server sinkPhyServer = neighborOutputInterface.mapPriorityPhyServerData.get(TASWindowsBuilder.KEY_PHY_NO_PRIORITY).getPlcaServer();

						if(sinkPhyServer == null)
							throw new InvalidParameterException("Neighbor interface " + neighborOutputInterface + " does not have either ST Servers or PHY Server");

						final String turnAlias = outputInterface.toStringTurnAlias(sourcePhyServer, sinkPhyServer);
						outputInterface.getNetwork().addTurn(turnAlias, sourcePhyServer, sinkPhyServer);						

					}
					//If neighbor does have ST Servers
					else {

						final STServerData neighborOutputSTServerData = neighborOutputInterface.mapPrioritySTServerData.get(priority);

						//Add Turn only if neighbor's interface have ST server for the same priority
						if(neighborOutputSTServerData != null) {

							final Server sinkServer = neighborOutputSTServerData.getSTServer();
							final String turnAlias = outputInterface.toStringTurnAlias(sourcePhyServer, sinkServer);
							outputInterface.getNetwork().addTurn(turnAlias, sourcePhyServer, sinkServer);

						}

					}

				}

			}

		}
		else if(
				/**
				 * Case c) Some ST servers and only one PHY server 					=> TSN and PLCA (one PLCA modeling)
				 */
				((outputStServerSize > 0) && (outputPhyServerSize == 1) && (outputPhyServerDataNoPriority != null)) 

				||

				/**
				 * Case d) No ST servers and only one PHY server					=> No TSN and (either no PLCA or one PLCA modeling)
				 */
				((outputStServerSize == 0) && (outputPhyServerSize == 1) && (outputPhyServerDataNoPriority != null))) 
		{

			final Server sourcePhyServer = outputPhyServerDataNoPriority.getPlcaServer();

			/**
			 * Iterate among neighbor's interfaces to choose the correct sinks
			 */
			for(EthernetInterface neighborOutputInterface : neighborOutputInterfaces) {

				final Set<Server> sinks;

				/**
				 * If neighbor is using TSN priority queues into neighbor output interface
				 * Then build Turns to all priority queue's servers
				 */
				if(neighborOutputInterface.mapPrioritySTServerData.isEmpty() == false) {
					sinks = neighborOutputInterface.getSTServers();	
				}
				/**
				 * If neighbor is NOT using TSN priority queues, but is using PHY Servers
				 * Then build Turn to PHY Server from this interface to another PHY server in neighbor output interface
				 */
				else if(neighborOutputInterface.mapPriorityPhyServerData.isEmpty() == false){
					sinks = neighborOutputInterface.getPhyServers();
				}
				/**
				 * Otherwise, create a empty Set to do not create a Turn through this interface
				 */
				else {
					sinks = new HashSet<Server>();
				}

				for(Server sink : sinks) {
					final String turnAlias = outputInterface.toStringTurnAlias(sourcePhyServer, sink);
					outputInterface.getNetwork().addTurn(turnAlias, sourcePhyServer, sink);
				}
			}

		}
		/**
		 * Case e)
		 * 
		 * An interface must have at least either one ST server or one PHY server
		 */
		else {

			//throw new InvalidParameterException("outputInterface: "+outputInterface+". An interface must have at least either one ST server or one PHY server");

		}

	}

	
	/**
	 * 
	 * 
	 * @param phyStandard
	 * @param tasGateScheduling
	 * @throws Exception 
	 */
	public void buildOutputServersAndInternalTurns_PlcaServer(TASWindowsBuilder networkConfigBuilder) throws Exception {

		//Sanity check
		Objects.requireNonNull(networkConfigBuilder);
		Objects.requireNonNull(networkConfigBuilder.getMapInterfaceInfoPrioritySTServerData());
		Objects.requireNonNull(networkConfigBuilder.getMapInterfaceInfoPriorityPLCAServerData());
		Objects.requireNonNull(networkConfigBuilder.getMapInterfaceInfoPhyStandard());

		final InterfaceInfoEntry interfaceInfo = this.getInterfaceInfo();

		final Map<Integer,STServerData> mapPrioritySTserverData = networkConfigBuilder.getMapInterfaceInfoPrioritySTServerData().get(interfaceInfo);
		if(mapPrioritySTserverData != null)
			this.mapPrioritySTServerData.putAll(mapPrioritySTserverData);

		final Map<Integer,PlcaServerData> mapPriorityPhyServerData = networkConfigBuilder.getMapInterfaceInfoPriorityPLCAServerData().get(interfaceInfo);
		if(mapPriorityPhyServerData != null)
			this.mapPriorityPhyServerData.putAll(mapPriorityPhyServerData);

		this.phyStandard = networkConfigBuilder.getMapInterfaceInfoPhyStandard().get(interfaceInfo);

		//TSN is NOT in use
		if(this.mapPrioritySTServerData.isEmpty()) {

			//If isDedicatedMediumAccess==true, then there is no PLCA server. If there is a server, it is a default Rate-Latency.
			if(this.phyStandard.isDedicatedMediumAccess) {

				final ServiceCurve serviceCurve = TASCurveBuilder.getInstance().buildDefaultRateLatencyServiceCurve(this.phyStandard);
				final Multiplexing multiplexing = this.ethernetDeviceOwner.getNetwork().getTASWindowCalculator().getExecutionConfig().multiplexing;
				final Server server = this.getNetwork().addServer(this.toStringServerDefaultEthernetAlias(), serviceCurve, multiplexing);
				
				final PlcaServerData phyServerData = this.mapPriorityPhyServerData.get(TASWindowsBuilder.KEY_PHY_NO_PRIORITY);

				if(phyServerData != null) {
					phyServerData.setServiceCurve(serviceCurve);
					phyServerData.setPlcaServer(server);	
				}

			} else {
				//There are one or more PLCA servers.

				final Map<Integer,ServiceCurve> mapPriorityPLCAServiceCurve = TASCurveBuilder.getInstance().buildPLCAServiceCurve(this.getNetwork(), interfaceInfo, networkConfigBuilder);

				for(Entry<Integer,ServiceCurve> entry : mapPriorityPLCAServiceCurve.entrySet()) {

					final int priority = entry.getKey();
					final ServiceCurve serviceCurve = entry.getValue();
					final Multiplexing multiplexing = this.ethernetDeviceOwner.getNetwork().getTASWindowCalculator().getExecutionConfig().multiplexing;
					final Server phyServer = this.getNetwork().addServer(this.toStringServerPlcaAlias(priority), serviceCurve, multiplexing);
					
					if(priority != TASWindowsBuilder.KEY_PHY_NO_PRIORITY)
						this.getNetwork().setServerPriority(phyServer, priority);

					this.mapPriorityPhyServerData.get(priority).setServiceCurve(serviceCurve);
					this.mapPriorityPhyServerData.get(priority).setPlcaServer(phyServer);	
				}

			}

		} 
		//TSN is being used
		else {

			//We have to create a ST Server for each priority in use
			for(Entry<Integer, STServerData> entry : this.mapPrioritySTServerData.entrySet()) {

				final int priority = entry.getKey();
				final STServerData stServerData = entry.getValue();
				final ServiceCurve stServiceCurve = stServerData.getServiceCurve();

				final Multiplexing multiplexing = this.ethernetDeviceOwner.getNetwork().getTASWindowCalculator().getExecutionConfig().multiplexing;
				final Server stServer = this.getNetwork().addServer(this.toStringSTServerAlias(priority), stServiceCurve, priority, multiplexing);

				stServerData.setSTServer(stServer);
			}

			//
			if(this.phyStandard.isDedicatedMediumAccess) {

				//There is no PHY Server when it is a interface with TSN and Dedicated Medium Access
				if(this.mapPriorityPhyServerData.isEmpty() == false)
					throw new InvalidParameterException("An interface with isDedicatedMediumAccess==true must not have values into this.mapPriorityPhyServerData");
			}
			//
			else {
				//There are one or more PLCA servers.

				final Map<Integer,ServiceCurve> plcaServiceCurve = TASCurveBuilder.getInstance().buildPLCAServiceCurve(ethernetDeviceOwner.getNetwork(), interfaceInfo, networkConfigBuilder);
				for(Entry<Integer,ServiceCurve> entry : plcaServiceCurve.entrySet()) {

					final int priority = entry.getKey();
					final ServiceCurve serviceCurve = entry.getValue();
					final Multiplexing multiplexing = this.ethernetDeviceOwner.getNetwork().getTASWindowCalculator().getExecutionConfig().multiplexing;
					final Server phyServer = this.getNetwork().addServer(this.toStringServerPlcaAlias(priority), serviceCurve, multiplexing);
					
					if(priority != TASWindowsBuilder.KEY_PHY_NO_PRIORITY)
						this.getNetwork().setServerPriority(phyServer, priority);

					this.mapPriorityPhyServerData.get(priority).setPlcaServer(phyServer);	
				}


				//Adding Turns from ST Servers to PLCA Servers
				for(Entry<Integer, STServerData> entry : this.mapPrioritySTServerData.entrySet()) {
					final int priority = entry.getKey();
					final Server stServer = entry.getValue().getSTServer();

					final PlcaServerData phyServerData = Objects.requireNonNullElse(
							this.mapPriorityPhyServerData.get(priority),
							this.mapPriorityPhyServerData.get(TASWindowsBuilder.KEY_PHY_NO_PRIORITY));

					final Server phyServer = phyServerData.getPlcaServer();
					final String turnAlias = this.toStringTurnAlias(stServer, phyServer);

					this.getNetwork().addTurn(turnAlias, stServer, phyServer);
				}
			}

		}

	}
	
	/**
	 * 
	 * 
	 * @param phyStandard
	 * @param tasGateScheduling
	 * @throws Exception 
	 */
	public void buildOutputServersAndInternalTurns_NoPlcaServer(TASWindowsBuilder networkConfigBuilder) throws Exception {

		//Sanity check
		Objects.requireNonNull(networkConfigBuilder);
		Objects.requireNonNull(networkConfigBuilder.getMapInterfaceInfoPrioritySTServerData());
		Objects.requireNonNull(networkConfigBuilder.getMapInterfaceInfoPriorityPLCAWeightWRR());
		Objects.requireNonNull(networkConfigBuilder.getMapInterfaceInfoPhyStandard());

		final InterfaceInfoEntry interfaceInfo = this.getInterfaceInfo();

		final Map<Integer,STServerData> mapPrioritySTserverData = networkConfigBuilder.getMapInterfaceInfoPrioritySTServerData().get(interfaceInfo);
		if(mapPrioritySTserverData != null)
			this.mapPrioritySTServerData.putAll(mapPrioritySTserverData);

		final Map<Integer, Integer> mapPriorityPLCAWeightWRR = networkConfigBuilder.getMapInterfaceInfoPriorityPLCAWeightWRR().get(interfaceInfo);
		if(mapPriorityPLCAWeightWRR != null)
			this.mapPriorityPLCAWeightWRR.putAll(mapPriorityPLCAWeightWRR);

		this.phyStandard = networkConfigBuilder.getMapInterfaceInfoPhyStandard().get(interfaceInfo);

		//No TSN
		if(this.mapPrioritySTServerData.get(TASWindowsBuilder.KEY_PHY_NO_PRIORITY) != null) {

			final STServerData stServerData = this.mapPrioritySTServerData.get(TASWindowsBuilder.KEY_PHY_NO_PRIORITY);
			final ServiceCurve serviceCurve = stServerData.getServiceCurve();

			final Server server = this.getNetwork().addServer(this.toStringServerDefaultEthernetAlias(), serviceCurve, TASWindowsBuilder.KEY_PHY_NO_PRIORITY);

			stServerData.setSTServer(server);
		}
		//TSN
		else {

			//We have to create a ST Server for each priority in use
			for(Entry<Integer, STServerData> entry : this.mapPrioritySTServerData.entrySet()) {

				final int priority = entry.getKey();
				final STServerData stServerData = entry.getValue();
				final ServiceCurve stServiceCurve = stServerData.getServiceCurve();

				final Server stServer = this.getNetwork().addServer(this.toStringSTServerAlias(priority), stServiceCurve, priority);

				stServerData.setSTServer(stServer);
			}

		}

	}

	public EthernetDevice getEthernetDeviceOwner() {
		return ethernetDeviceOwner;
	}

	public EthernetNetwork getNetwork() {
		return this.ethernetDeviceOwner.getNetwork();
	}

	public int getId() {
		return id;
	}

	public final EthernetLink getLink() {
		return this.ethernetDeviceOwner.getLink(this);
	}

	public final InterfaceInfoEntry getInterfaceInfo() {
		return InterfaceInfoEntry.parse(this.ethernetDeviceOwner.getName() + "." + this.id);
	}

	public Set<Server> getSTServers(){

		final Set<Server> servers = new HashSet<Server>();

		for(Entry<Integer,STServerData> entry : this.mapPrioritySTServerData.entrySet()) {
			servers.add(entry.getValue().getSTServer());
		}

		return servers;
	}

	public Set<Server> getPhyServers(){

		final Set<Server> servers = new HashSet<Server>();

		for(Entry<Integer,PlcaServerData> entry : this.mapPriorityPhyServerData.entrySet()) {
			servers.add(entry.getValue().getPlcaServer());
		}

		return servers;
	}

	public STServerData getSTServerData(Integer priority){
		return this.mapPrioritySTServerData.get(priority);
	}

	public PlcaServerData getPhyServerData(Integer priority){
		return this.mapPriorityPhyServerData.get(priority);
	}

	public EthernetPhyStandard getPhyStandard() {
		return phyStandard;
	}

	@Override
	public String toString() {
		return this.ethernetDeviceOwner.getName() + ".eth" + Integer.toString(id) + "["+this.phyStandard.name+"]";
	}

	private String toStringServerPlcaAlias(int priority) {

		if(priority == TASWindowsBuilder.KEY_PHY_NO_PRIORITY)
			return "s_" + this.toString() + "[PLCA]" + ".out";
		else
			return "s_" + this.toString() + "[PLCA]" + "{prio="+priority+"}" + ".out";
	}

	private String toStringSTServerAlias(int priority) {
		if(this.mapPriorityPLCAWeightWRR.get(priority) != null) 
			return "s_" + this.toString() + "[TAS+PLCA]" + "{prio="+priority+"}" + ".out";	
		else 
			return "s_" + this.toString() + "[TAS]" + "{prio="+priority+"}" + ".out";
	}

	private String toStringServerDefaultEthernetAlias() {
		return "s_" + this.toString() + ".out";
	}

	private String toStringTurnAlias(Server source, Server sink) {
		return (source.getAlias() + "->" + sink.getAlias());
	}

	@Override
	public int hashCode() {
		/**
		 * Note: do not use this.ethernetDeviceOwner in hash calculation. 
		 * This make a infinite recursive call of hashCode() method and produces StackOverflowError exception.
		 */
		return Objects.hash(this.ethernetDeviceOwner.getName(), this.id);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EthernetInterface other = (EthernetInterface) obj;

		/**
		 * Note: do not use this.ethernetDeviceOwner in equals calculation. 
		 * This make a infinite recursive call of equals() method and produces StackOverflowError exception.
		 */
		return Objects.equals(this.ethernetDeviceOwner.getName(), other.ethernetDeviceOwner.getName()) && 
				Objects.equals(this.id, other.id);
	}

}
