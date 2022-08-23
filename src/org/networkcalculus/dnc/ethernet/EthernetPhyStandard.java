package org.networkcalculus.dnc.ethernet;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Describes speed and mechanism for medium access
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 * 
 */
public enum EthernetPhyStandard{

	//Common standards
	phy_10BASE_T						(10.0 	* 1.0e6, true,	"10BASE-T"),
	phy_100BASE_TX						(100.0 	* 1.0e6, true,	"100BASE-TX"),
	phy_1000BASE_TX						(1000.0 * 1.0e6, true,	"1000BASE-TX"),

	//Automotive standards
	phy_10BASE_T1S						(10.0 	* 1.0e6, false, "10BASE-T1S"),
	phy_100BASE_T1						(100.0 	* 1.0e6, true,	"100BASE-T1"),
	phy_1000BASE_T1						(1000.0	* 1.0e6, true,	"1000BASE-T1"),

	//Hypothetical standards
	phy_hypothetical_1000BASE_T1S		(1000.0 * 1.0e6, false, "1000BASE-T1S");

	
	/**
	 * Rate in bits per second (bps)
	 */
	public final double rate_bps;
	
	/**
	 * Rate in bits per microsecond (bpus)
	 */
	public final double rate_bpus;
	
	/**
	 * If not, it is a Shared Access Medium (e.g. 10BASE-T1S which is a bus network)
	 */
	public final boolean isDedicatedMediumAccess;
	
	/**
	 * 
	 */
	public final String name;

	private EthernetPhyStandard(double rate_bps, boolean isDedicatedMediumAccess, String name) {
		this.rate_bps = rate_bps;
		this.rate_bpus = rate_bps / 1.0e6;
		this.isDedicatedMediumAccess = isDedicatedMediumAccess;
		this.name = name;
	}

	/**
	 * Given a frame length (in bytes), it calculates and returns the transmission time (in microseconds) for this frame accordingly to PHY speed.
	 * 
	 * @param frameLengthBytes
	 * @return
	 */
	public final double transmissionTimeUs(final double frameLengthBytes) {
		return (frameLengthBytes * 8.0) / this.rate_bpus;
	}

	public static EthernetPhyStandard findByName(final String name){
		return Arrays.stream(values()).filter(value -> value.name.equals(name)).findFirst().orElse(null);
	}


	public static final EthernetPhyStandard convertToSharedMediumAccessPhyStandard(final EthernetPhyStandard phyStandard) {

		Objects.requireNonNull(phyStandard);
		
		if(!phyStandard.isDedicatedMediumAccess) 
			return phyStandard;
		
		final Map<EthernetPhyStandard, EthernetPhyStandard> mapDedicatedToShared = new HashMap<>();
		
		//			//Common standards
		//			phy_10BASE_T						(10.0 	* 1.0e6, true,	"10BASE-T"),
		//			phy_100BASE_TX						(100.0 	* 1.0e6, true,	"100BASE-TX"),
		//			phy_1000BASE_TX						(1000.0 * 1.0e6, true,	"1000BASE-TX"),
		//
		//			//Automotive standards
		//			phy_10BASE_T1S						(10.0 	* 1.0e6, false, "10BASE-T1S"),
		//			phy_100BASE_T1						(100.0 	* 1.0e6, true,	"100BASE-T1"),
		//			phy_1000BASE_T1						(1000.0	* 1.0e6, true,	"1000BASE-T1"),
		//
		//			//Hypothetical standards
		//			phy_hypothetical_1000BASE_T1S		(1000.0 * 1.0e6, false, "1000BASE-T1S");

		mapDedicatedToShared.put(phy_10BASE_T, phy_10BASE_T1S);
		
		mapDedicatedToShared.put(phy_1000BASE_TX, phy_hypothetical_1000BASE_T1S);
		mapDedicatedToShared.put(phy_1000BASE_T1, phy_hypothetical_1000BASE_T1S);
		
		final EthernetPhyStandard result = mapDedicatedToShared.get(phyStandard);
		
		Objects.requireNonNull(result, "dedicatedPhyType = " + phyStandard.name);
		
		return result;
	}
	
	public static final EthernetPhyStandard convertToDedicatedMediumAccessPhyStandard(final EthernetPhyStandard phyStandard) {

		Objects.requireNonNull(phyStandard);
		
		if(phyStandard.isDedicatedMediumAccess) 
			return phyStandard;
		
		final Map<EthernetPhyStandard, EthernetPhyStandard> mapDedicatedToShared = new HashMap<>();
		
		//			//Common standards
		//			phy_10BASE_T						(10.0 	* 1.0e6, true,	"10BASE-T"),
		//			phy_100BASE_TX						(100.0 	* 1.0e6, true,	"100BASE-TX"),
		//			phy_1000BASE_TX						(1000.0 * 1.0e6, true,	"1000BASE-TX"),
		//
		//			//Automotive standards
		//			phy_10BASE_T1S						(10.0 	* 1.0e6, false, "10BASE-T1S"),
		//			phy_100BASE_T1						(100.0 	* 1.0e6, true,	"100BASE-T1"),
		//			phy_1000BASE_T1						(1000.0	* 1.0e6, true,	"1000BASE-T1"),
		//
		//			//Hypothetical standards
		//			phy_hypothetical_1000BASE_T1S		(1000.0 * 1.0e6, false, "1000BASE-T1S");

		mapDedicatedToShared.put(phy_10BASE_T1S, phy_10BASE_T);
		mapDedicatedToShared.put(phy_hypothetical_1000BASE_T1S, phy_1000BASE_TX);
		
		final EthernetPhyStandard result = mapDedicatedToShared.get(phyStandard);
		
		Objects.requireNonNull(result, "sharedPhyType = " + phyStandard.name);
		
		return result;
	}

}