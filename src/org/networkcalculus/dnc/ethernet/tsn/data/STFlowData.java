package org.networkcalculus.dnc.ethernet.tsn.data;

import java.util.Objects;

import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.ethernet.tsn.entry.STMessageEntry;

/**
 * Scheduled Traffic (ST) Flow's Data
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 */
public class STFlowData {

	private STMessageEntry stMessageEntry; 	//Message related to this flow
	private ArrivalCurve arrivalCurve;

	public STFlowData(STMessageEntry stMessageEntry) {
		
		Objects.requireNonNull(stMessageEntry, "message cannot be null");
		
		this.stMessageEntry = stMessageEntry;
	}
	
	public STMessageEntry getSTMessageEntry() {
		return stMessageEntry;
	}
	
	public ArrivalCurve getArrivalCurve() {
		return arrivalCurve;
	}
	public void setArrivalCurve(ArrivalCurve arrivalCurve) {
		this.arrivalCurve = arrivalCurve;
	}
	
	public double getMessageMaxSizeBytes() {
		return this.stMessageEntry.sizeBytes;
	}
	public double getMessageMinSizeBytes() {
		return this.stMessageEntry.sizeBytes;
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(stMessageEntry, arrivalCurve);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		STFlowData other = (STFlowData) obj;
		return Objects.equals(stMessageEntry, other.stMessageEntry)
				&& Objects.equals(arrivalCurve, other.arrivalCurve);
	}

	@Override
	public String toString() {
		return "STFlowData [stMessageEntry=" + stMessageEntry + ", arrivalCurve=" + arrivalCurve + "]";
	}



	
	
}
