package org.networkcalculus.dnc.ethernet.network.server_graph;

import java.util.Objects;

import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.ethernet.tsn.data.STFlowData;
import org.networkcalculus.dnc.network.server_graph.Flow;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 *
 */
public class EthernetFlow{

	private String alias;
	private Integer priority;
	private ArrivalCurve arrivalCurve;
	private Flow flow;
	private STFlowData stFlowData;
	
	public EthernetFlow(String alias, Integer priority, ArrivalCurve arrivalCurve, STFlowData stFlowData) {
		super();
		this.alias = alias;
		this.priority = priority;
		this.arrivalCurve = arrivalCurve;
		this.stFlowData = stFlowData;
	}
	
	public EthernetFlow(String alias, Integer priority, ArrivalCurve arrivalCurve) {
		this(alias, priority, arrivalCurve, null);
	}
	
	public String getAlias() {
		return alias;
	}
	public void setAlias(String alias) {
		this.alias = alias;
	}
	public Integer getPriority() {
		return priority;
	}
	public void setPriority(Integer priority) {
		this.priority = priority;
	}
	public ArrivalCurve getArrivalCurve() {
		return arrivalCurve;
	}
	public void setArrivalCurve(ArrivalCurve arrivalCurve) {
		this.arrivalCurve = arrivalCurve;
	}
	public Flow getFlow() {
		return flow;
	}
	public void setFlow(Flow flow) {
		this.flow = flow;
	}
	public STFlowData getStFlowData() {
		return stFlowData;
	}
	public void setStFlowData(STFlowData stFlowData) {
		this.stFlowData = stFlowData;
	}

	public String toStringMultilined() {
		return "EthernetFlow [\n\talias=" + alias + ",\n\tpriority=" + priority + ",\n\tarrivalCurve=" + arrivalCurve + ",\n\tflow=" + flow + "\n]";
	}
	
	@Override
	public String toString() {
		return "EthernetFlow [alias=" + alias + ", priority=" + priority + ", arrivalCurve=" + arrivalCurve + ", flow="
				+ flow + ", stFlowData=" + stFlowData + "]";
	}

	@Override
	public int hashCode() {
		return Objects.hash(alias, arrivalCurve, flow, priority);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		EthernetFlow other = (EthernetFlow) obj;
		return Objects.equals(alias, other.alias) && Objects.equals(arrivalCurve, other.arrivalCurve)
				&& Objects.equals(flow, other.flow) && Objects.equals(priority, other.priority);
	}

}
