package org.networkcalculus.dnc.ethernet;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 * 
 */
public class EthernetEndSystem extends EthernetDevice{
	
	public EthernetEndSystem(EthernetNetwork network, String deviceName) {
		super(network, deviceName);
	}
	
	public String toString() {
		return (this.name);
	}
	
}
