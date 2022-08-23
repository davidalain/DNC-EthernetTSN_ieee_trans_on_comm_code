package org.networkcalculus.dnc.ethernet.tsn.entry;

import java.util.Objects;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 *
 */
public class STMessageEntry {
	
	//Example: tt1, 400, 58972.0, vl1, TT, 7, 250, 0.0

	public final String flowName;				//Flow's name.
	public final double sizeBytes;				//size in bytes.
	public final double deadlineUs;				//deadline in us (microseconds).
	public final String virtualLinkName;		//Virtual link name.
	public final String type;					//TT or RC.
	public final int priority;					//Flow's priority. From 0 (low priority) to 7 (high priority).
	public final double periodUs;				//Period length in us (microseconds) for each transmission.
	public final double offsetUs;				//Offset in us (microseconds).

	public STMessageEntry(String flowName, double sizeBytes, double deadlineUs, String virtualLinkName, String type, int priority, double periodUs, double offsetUs) {
		this.flowName = flowName;
		this.sizeBytes = sizeBytes;
		this.deadlineUs = deadlineUs;
		this.virtualLinkName = virtualLinkName;
		this.type = type;
		this.priority = priority;
		this.periodUs = periodUs;
		this.offsetUs = offsetUs;
	}

	public static final STMessageEntry parse(String entryFromFile) {
		
		Objects.requireNonNull(entryFromFile, "entryFromFile cannot be null");

		//#!R id, size(byte), deadline, <virtual link id>, type [TT, RC], priority, [period | rate] (us), [offset | ] [packed | fragmented]
		//tt1, 400, 58972.0, vl1, TT, 7, 250, 0.0

		final String[] items = entryFromFile.replaceAll("\\s+","").split(",");

		final String flowName = items[0];
		final double sizeBytes = Double.parseDouble(items[1]);
		final double deadlineUs = Double.parseDouble(items[2]);
		final String virtualLinkName = items[3];
		final String type = items[4];
		final int priority = Integer.parseInt(items[5]);
		final double periodUs = Double.parseDouble(items[6]);
		final double offsetUs = Double.parseDouble(items[7]);

		return new STMessageEntry(flowName, sizeBytes, deadlineUs, virtualLinkName, type, priority, periodUs, offsetUs);
	}

	public boolean flowEquals(STMessageEntry other) {

		if(!Objects.equals(this.flowName, other.flowName))
			return false;
		
		if(!Objects.equals(this.priority, other.priority))
			return false;
		
		if(!Objects.equals(this.type, other.type))
			return false;
		
		if(!Objects.equals(this.periodUs, other.periodUs))
			return false;
		
		if(!Objects.equals(this.virtualLinkName, other.virtualLinkName))
			return false;
		
		return true;
	}

	@Override
	public String toString() {
		return "STMessageEntry [flowName=" + flowName + ", sizeBytes=" + sizeBytes + ", deadlineUs=" + deadlineUs
				+ ", virtualLinkName=" + virtualLinkName + ", type=" + type + ", priority=" + priority + ", periodUs="
				+ periodUs + ", offsetUs=" + offsetUs + "]";
	}

}
