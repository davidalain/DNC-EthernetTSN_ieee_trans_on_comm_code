package org.networkcalculus.dnc.ethernet.tsn.entry;

import java.security.InvalidParameterException;
import java.util.Objects;

/**
 * Time-Aware Shaper (TAS) Schedule Entry
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 */
public class TASGateScheduleEntry2020 {

	/**
	 * Open time offset related to beginning of GCL cycle.
	 * Value in microseconds (us).
	 */
	public final double openTime;
	
	/**
	 * Close time offset related to beginning of GCL cycle.
	 * Value in microseconds (us).
	 */
	public final double closeTime;
	
	/**
	 * Flow name
	 */
	public final String flowname;
	
	/**
	 * TAS Window Index
	 */
	public final int windowIndex;

	/**
	 * 
	 * @param openTime
	 * @param closeTime
	 * @param periodLength
	 * @param priority
	 */
	public TASGateScheduleEntry2020(double openTime, double closeTime, String flowname, int windowIndex) {
		super();
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.flowname = flowname;
		this.windowIndex = windowIndex;
	}
	
	public static final TASGateScheduleEntry2020 parse(String entryFromFile) {
		
		Objects.requireNonNull(entryFromFile, "entryFromFile cannot be null");
		
		final String[] items = entryFromFile.split("\t");
		
		final double openTime = Double.parseDouble(items[0].trim());
		final double closeTime = Double.parseDouble(items[1].trim());
		final String flowname = items[2].trim();
		final int windowIndex = Integer.parseInt(items[3].trim());
		
		if(openTime >= closeTime)
			throw new InvalidParameterException("openTime={"+openTime+"} must be lesser than closeTime={"+closeTime+"}. entryFromFile={"+entryFromFile+"}");

		//FIXME: uncomment this
//		if(closeTime > periodLength)
//			throw new InvalidParameterException("closeTime={"+closeTime+"} must be lesser than periodLength={"+periodLength+"}. entryFromFile={"+entryFromFile+"}");
		
		return new TASGateScheduleEntry2020(openTime, closeTime, flowname, windowIndex);
	}

	@Override
	public int hashCode() {
		return Objects.hash(closeTime, openTime, flowname, windowIndex);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TASGateScheduleEntry2020 other = (TASGateScheduleEntry2020) obj;
		return Double.doubleToLongBits(closeTime) == Double.doubleToLongBits(other.closeTime)
				&& Double.doubleToLongBits(openTime) == Double.doubleToLongBits(other.openTime)
				&& Objects.equals(flowname, other.flowname) && windowIndex == other.windowIndex;
	}
	
	
	/**
	 * Check if {@param t} is between openTime and closeTime 
	 * 
	 * @param t
	 * @return
	 */
	public boolean isGateOpen(double t) {
		return (this.openTime <= t && t <= this.closeTime);
	}

	@Override
	public String toString() {
		return "TASGateScheduleEntry2020 [openTime=" + openTime + ", closeTime=" + closeTime + ", flowname=" + flowname
				+ ", windowIndex=" + windowIndex + "]";
	}

	

	

}
