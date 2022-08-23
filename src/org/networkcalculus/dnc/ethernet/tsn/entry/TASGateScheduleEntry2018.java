package org.networkcalculus.dnc.ethernet.tsn.entry;

import java.security.InvalidParameterException;
import java.util.Objects;

/**
 * Time-Aware Shaper (TAS) Schedule Entry
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 */
public class TASGateScheduleEntry2018 {

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
	 * Period length in microseconds (us). 
	 * This is the period from gate open-close cycle for this priority and shall not be confused to the GCL hyperperiod.
	 */
	public final long periodLength;
	
	/**
	 * Priority
	 */
	public final int priority;

	/**
	 * 
	 * @param openTime
	 * @param closeTime
	 * @param periodLength
	 * @param priority
	 */
	public TASGateScheduleEntry2018(double openTime, double closeTime, long periodLength, int priority) {
		super();
		this.openTime = openTime;
		this.closeTime = closeTime;
		this.periodLength = periodLength;
		this.priority = priority;
	}
	
	public static final TASGateScheduleEntry2018 parse(String entryFromFile) {
		
		Objects.requireNonNull(entryFromFile, "entryFromFile cannot be null");
		
		final String[] items = entryFromFile.split("\t");
		
		final double openTime = Double.parseDouble(items[0].trim());
		final double closeTime = Double.parseDouble(items[1].trim());
		final long periodLength = Long.parseLong(items[2].trim());
		final int priority = Integer.parseInt(items[3].trim());
		
		if(openTime >= closeTime)
			throw new InvalidParameterException("openTime={"+openTime+"} must be lesser than closeTime={"+closeTime+"}. entryFromFile={"+entryFromFile+"}");

		//FIXME: uncomment this
//		if(closeTime > periodLength)
//			throw new InvalidParameterException("closeTime={"+closeTime+"} must be lesser than periodLength={"+periodLength+"}. entryFromFile={"+entryFromFile+"}");
		
		return new TASGateScheduleEntry2018(openTime, closeTime, periodLength, priority);
	}

	@Override
	public int hashCode() {
		return Objects.hash(closeTime, openTime, periodLength, priority);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TASGateScheduleEntry2018 other = (TASGateScheduleEntry2018) obj;
		return Double.doubleToLongBits(closeTime) == Double.doubleToLongBits(other.closeTime)
				&& Double.doubleToLongBits(openTime) == Double.doubleToLongBits(other.openTime)
				&& periodLength == other.periodLength && priority == other.priority;
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
		return "TASGateScheduleEntry2018 [openTime=" + openTime + ", closeTime=" + closeTime + ", periodLength="
				+ periodLength + ", priority=" + priority + "]";
	}


	

}
