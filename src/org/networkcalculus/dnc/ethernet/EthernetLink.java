package org.networkcalculus.dnc.ethernet;

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 * 
 */
public class EthernetLink {

	private Set<EthernetInterface> interfaces;

	/**
	 * Create a EthernetLink with 2 or more EthernetInterfaces.
	 * 
	 * @param first
	 * @param others
	 */
	public EthernetLink(EthernetInterface first, EthernetInterface ... others) {

		this.interfaces = new HashSet<EthernetInterface>();

		Objects.requireNonNull(first, "All interfaces must not be null");
		Objects.requireNonNull(others, "All interfaces must not be null");
		for(EthernetInterface other : others) 
			Objects.requireNonNull(other, "All interfaces must not be null");	


		if(others.length < 1)
			throw new InvalidParameterException("others must contain at least one EthernetInterface");

		if(others.length > 1) {
			if(first.getPhyStandard().isDedicatedMediumAccess)
				throw new InvalidParameterException(first + " (phyStandard.isDedicatedMediumAccess=true).\n"
						+ "It is not possible to link more than two interfaces with dedicated medium access.\n"
						+ "others: " + Arrays.toString(others));

			for(EthernetInterface other : others) {
				if(other.getPhyStandard().isDedicatedMediumAccess)
					throw new InvalidParameterException(other + " (phyStandard.isDedicatedMediumAccess=true).\n"
							+ "It is not possible to link more than two interfaces with dedicated medium access.");
			}
		}

		this.interfaces.add(first);

		for(EthernetInterface other : others) {

			if(other == null)
				throw new InvalidParameterException("All interfaces must not be null");

			if(!Objects.equals(first.getPhyStandard(), other.getPhyStandard()))
				throw new InvalidParameterException("All interfaces' standard from the same link must be equals");

			final boolean success = this.interfaces.add(other);
			if(!success) 
				throw new InvalidParameterException("All interfaces instances must be different");	

		}

	}

	/**
	 * Based on this link, this method returns neighbor devices of 'deviceRef' 
	 * 
	 * @param deviceRef
	 * @return
	 */
	public List<EthernetDevice> getNeighbors(EthernetDevice deviceRef) {

		final List<EthernetDevice> devices = new LinkedList<EthernetDevice>();

		for(EthernetInterface ethInterface : this.interfaces) {
			devices.add(ethInterface.getEthernetDeviceOwner());
		}

		final boolean success = devices.remove(deviceRef);
		if(!success) 
			throw new InvalidParameterException("deviceRef must be the owner of a interface of this link");	

		return devices;
	}

	/**
	 * Returns all EthernetInterface instances from this link whom deviceRef is NOT the owner.
	 * 
	 * @param deviceRef
	 * @return
	 */
	public List<EthernetInterface> getNeighborInterfaces(EthernetDevice deviceRef) {

		final List<EthernetInterface> interfaceList = new LinkedList<EthernetInterface>();

		for(EthernetInterface ethInterface : this.interfaces) {
			if(!Objects.equals(ethInterface.getEthernetDeviceOwner(), deviceRef)) {
				interfaceList.add(ethInterface);
			}
		}

		if(interfaceList.isEmpty())
			throw new InvalidParameterException("deviceRef must be the owner of a interface of this link");

		return interfaceList;
	}
	
	/**
	 * Returns a EthernetInterface instance from this link whom deviceRef is the owner.
	 * 
	 * @param deviceRef
	 * @return
	 */
	public EthernetInterface getDeviceInterface(EthernetDevice deviceRef) {

		EthernetInterface target = null;

		for(EthernetInterface ethInterface : this.interfaces) {
			if(Objects.equals(ethInterface.getEthernetDeviceOwner(), deviceRef)) {
				target = ethInterface;
				break;
			}
		}
		
		if(target == null)
			throw new InvalidParameterException("deviceRef must be the owner of a interface of this link");

		return target;
	}

	public Set<EthernetInterface> getInterfaces() {
		return interfaces;
	}


}
