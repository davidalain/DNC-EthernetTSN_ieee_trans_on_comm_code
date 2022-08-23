package org.networkcalculus.dnc.ethernet;

import java.util.List;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 * 
 */
public class EthernetSwitch extends EthernetDevice{
	
	public EthernetSwitch(EthernetNetwork network, String deviceName, List<EthernetInterface> interfaces) {
		super(network, deviceName);
		
		for(EthernetInterface ethernetInterface : interfaces) {
			this.interfaces.put(ethernetInterface.getId(), ethernetInterface);
		}
	}
	
	public EthernetSwitch(EthernetNetwork network, String deviceName) {
		super(network, deviceName);
	}
	
	@Override
	public String toString() {
		return (this.name);
	}

}
