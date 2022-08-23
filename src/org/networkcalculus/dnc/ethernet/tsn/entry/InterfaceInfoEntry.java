package org.networkcalculus.dnc.ethernet.tsn.entry;

import java.security.InvalidParameterException;
import java.util.Objects;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 *
 */
public class InterfaceInfoEntry {

	public final String deviceName;
	public final int interfaceId;
	
	public InterfaceInfoEntry(String deviceName, int interfaceId) {
		this.deviceName = deviceName;
		this.interfaceId = interfaceId;
	}
	
	/**
	 * Example value of input {@param entryFromFile}:
	 * 		"ES1.0"
	 * 
	 * @param entryFromFile
	 */
	public static final InterfaceInfoEntry parse(String entryFromFile) {
		
		Objects.requireNonNull(entryFromFile, "info cannot be null");
		
		entryFromFile = entryFromFile.replaceAll("\\s+","");
		
		int idx = entryFromFile.indexOf(",");
		if(idx != -1)
			entryFromFile = entryFromFile.substring(0, idx);
		
		final String[] parts = entryFromFile.split("\\.");

		if(parts.length != 2)
			throw new InvalidParameterException("Invalid input: " + entryFromFile);
		
		if((parts[0].length() < 1) || (parts[1].length() < 1))
			throw new InvalidParameterException("Invalid input: " + entryFromFile);
		
		return new InterfaceInfoEntry(parts[0], Integer.parseInt(parts[1]));
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(deviceName, interfaceId);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		InterfaceInfoEntry other = (InterfaceInfoEntry) obj;
		return Objects.equals(deviceName, other.deviceName) && interfaceId == other.interfaceId;
	}

	@Override
	public String toString() {
		return "InterfaceInfo [deviceName=" + deviceName + ", interfaceId=" + interfaceId + "]";
	}

	
	public String toStringInfo() {
		return this.deviceName + "." + this.interfaceId;
	}
	
	public String toStringInfoEth() {
		return this.deviceName + ".eth" + this.interfaceId;
	}
}
