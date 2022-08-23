package org.networkcalculus.dnc.ethernet;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 * 
 */
public abstract class EthernetDevice {

	protected String name; //Device name
	protected EthernetNetwork ethernetNetwork;
	protected Map<Integer, EthernetLink> links;				//<Interface ID, Ethernet Link>
	protected Map<Integer, EthernetInterface> interfaces; 	//<Interface ID, Ethernet interface>

	public EthernetDevice(EthernetNetwork network, String deviceName) {
		this.name = deviceName;
		this.ethernetNetwork = network;
		this.links = new HashMap<Integer, EthernetLink>();
		this.interfaces = new HashMap<Integer, EthernetInterface>();
		
		this.ethernetNetwork.addDevice(this);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public EthernetNetwork getNetwork() {
		return ethernetNetwork;
	}

	public void setNetwork(EthernetNetwork network) {
		this.ethernetNetwork = network;
	}

	public Set<Map.Entry<Integer,EthernetLink>> getLinksEntrySet() {
		return links.entrySet();
	}

	public Set<Map.Entry<Integer, EthernetInterface>> getInterfacesEntrySet() {
		return interfaces.entrySet();
	}

	public Set<EthernetInterface> getInterfaces() {
		return new HashSet<EthernetInterface>(this.interfaces.values());
	}

	public EthernetInterface getInterface(int id) {
		return interfaces.get(id);
	}
	
	public EthernetDevice putInterface(int id) {
		return putInterface(id, null);
	}
	
	public EthernetDevice putInterface(int id, EthernetPhyStandard phyStandard) {
		this.interfaces.put(id, new EthernetInterface(this, id, phyStandard));
		return this;
	}

	public Set<EthernetDevice> getNeighbors() {

		final Set<EthernetDevice> devices = new HashSet<EthernetDevice>();

		for(Entry<Integer, EthernetLink> entry : this.links.entrySet()) {
			devices.addAll(entry.getValue().getNeighbors(this));
		}

		return devices;
	}

	public EthernetLink getLink(int interfaceId) {
		return getLink(this.getInterface(interfaceId));
	}

	public EthernetLink getLink(EthernetInterface ethernetInterfaceRef) {

		if(!Objects.equals(ethernetInterfaceRef.getEthernetDeviceOwner(), this))
			throw new InvalidParameterException("this EthernetDevice instance must be the owner of ethernetInterface"); 

		EthernetLink target = null;

		for(Entry<Integer, EthernetLink> entry : this.links.entrySet()) {
			
			if(entry.getValue().getInterfaces().contains(ethernetInterfaceRef)) {
				target = entry.getValue();
				break;
			}
			
		}

		return target;
	}

//	public void linkTo(int interfaceId, EthernetDevice neighborDevice, int neighborInterfaceId) throws Exception {
//
//		final EthernetInterface sourceEthernetInterface = this.getInterface(interfaceId);
//		final EthernetInterface sinkDeviceInterface = neighborDevice.getInterface(neighborInterfaceId);
//
//		linkTo(sourceEthernetInterface, sinkDeviceInterface);
//	}
//
//	public void linkTo(EthernetInterface thisInterface, EthernetInterface neighborInterface) throws Exception {
//
//		final EthernetLink link = new EthernetLink(thisInterface, neighborInterface);
//
//		if(!Objects.equals(this, thisInterface.getEthernetDeviceOwner())) 
//			throw new InvalidParameterException("this EthernetDevice instance must be the owner of thisInterface");
//
//		if(this.getLink(thisInterface) != null)
//			throw new InvalidParameterException("thisInterface is already linked");
//
//		if(neighborInterface.getEthernetDeviceOwner().getLink(neighborInterface) != null)
//			throw new InvalidParameterException("neighborInterface is already linked");
//
//		this.links.put(thisInterface.getId(),link);
//		neighborInterface.getEthernetDeviceOwner().links.put(neighborInterface.getId(), link);
//
//		addTurns(thisInterface, neighborInterface); //forward
//		addTurns(neighborInterface, thisInterface); //backward
//	}

	/**
	 * This method must link output servers accordingly to Ethernet standard.
	 * 	
	 * For a interface with Dedicated Access to the Medium without TSN:
	 * 	A interface has only 1 output server.
	 * 
	 * For a Point-to-point link with TSN:
	 * 	A interface has up to 8 output servers (one for each priority queue).
	 * 
	 * For a Point-to-Multi-Point (10BASE-T1S) without TSN:
	 * 	A interface has only 1 output server (the WRR based Server for PLCA modeling).
	 * 
	 * For a Point-to-Multi-Point (10BASE-T1S) with TSN:
	 * 	A interface has 8 servers (one for each priority queue) and 
	 * 	all of them connected to 1 output server  (the WRR based Server for PLCA modeling).
	 * 
	 * Thus, for TSN in a interface with Dedicated Access to the Medium, an interface may have at 8 Turns coming from this interface (one for each priority queue server).
	 * Otherwise, an interface have only one Turn coming from this interface. 
	 * 
	 * As, we are not handling frame priority changing into any hop, so a priority queue server must be connected (by creating a Turn) to the same priority queue server in the next hop.
	 * Thus, we are going to have at most 8*(N - 1) Turns coming from this interface, where N is amount of interfaces in the neighbor device. 
	 * Note the neighbor's input interface must not to be counted, that is (N - 1).
	 * 
	 * @param outputInterface
	 * @param neighborInputInterface
	 * @throws Exception
	 */
	
//	private void addTurns(EthernetInterface outputInterface, EthernetInterface neighborInputInterface) throws Exception {
//
//		throw new InvalidParameterException("Implement this!");
//		
//		if(outputInterface == null)
//			throw new InvalidParameterException("outputInterface must not be null");
//		if(neighborInputInterface == null)
//			throw new InvalidParameterException("neighborInputInterface must not be null");
//
//		final EthernetDevice othertDevice = neighborInputInterface.getEthernetDeviceOwner();
//
//		final Server outputServer = outputInterface.getOutputServer();
//
//		for(EthernetInterface ethernetInterface : othertDevice.getInterfaces()) {
//
//			if(!Objects.equals(neighborInputInterface, ethernetInterface)) {
//
//				final Server otherDeviceOutputServer = ethernetInterface.getOutputServer();
//				final String turnAlias = outputServer.getAlias() + "->" + otherDeviceOutputServer.getAlias();
//				
//				othertDevice.network.getServerGraph().addTurn(turnAlias, outputServer, otherDeviceOutputServer);
//			}
//
//		}
//
//	}

	public List<EthernetLink> findLinks(EthernetDevice otherDevice) {

		final List<EthernetLink> linksToOtherDevice = new LinkedList<EthernetLink>();
		
		for(Entry<Integer, EthernetLink> entry : this.links.entrySet()) {

			for(EthernetDevice dev : entry.getValue().getNeighbors(this)) {
			
				if(Objects.equals(dev, otherDevice)) { 
					linksToOtherDevice.add(entry.getValue());
				}
				
			}
			
		}

		return linksToOtherDevice;
	}

	@Override
	public int hashCode() {
		//Note: do not use this.neighbors in hash calculation. This makes a infinite recursive call of hashCode() method and produces StackOverflowError exception.
		return Objects.hash(this.name, this.interfaces.values(), this.ethernetNetwork);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;

		//Note: do not use this.neighbors in equals comparison. This makes a infinite recursive call of equals() method and produces StackOverflowError exception.
		EthernetDevice other = (EthernetDevice) obj;
		return 	Objects.equals(this.name, other.name) &&
				Objects.equals(this.interfaces, other.interfaces) &&
				Objects.equals(this.ethernetNetwork, other.ethernetNetwork);
	}

	@Override
	public abstract String toString();

}
