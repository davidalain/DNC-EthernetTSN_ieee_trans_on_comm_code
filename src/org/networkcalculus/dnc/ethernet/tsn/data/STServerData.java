package org.networkcalculus.dnc.ethernet.tsn.data;

import java.util.LinkedList;
import java.util.List;

import org.networkcalculus.dnc.curves.ServiceCurve;
import org.networkcalculus.dnc.ethernet.tsn.model.TASWindow;
import org.networkcalculus.dnc.network.server_graph.Server;

/**
 * Scheduled Traffic (ST) Server's Data
 * 
 * There is a STServerData instance for each priority queue's Server into interface.
 * 
 * For a interface that runs TSN + PLCA, it may have up to 8 servers, thus up to 8 STServerData instances.
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 */
public class STServerData {

	/**
	 * The Service Curve instance
	 */
	private ServiceCurve serviceCurve;
	
	/**
	 * Server instance created after adding the serviceCurve to the system
	 */
	private Server stServer;
	
	/**
	 * A priority queue can have many windows within its period.
	 */
	private List<TASWindow> tasWindowList;
	
	/**
	 * 
	 * 
	 * @param serviceCurve
	 * @param tasWindowList
	 */
	public STServerData(ServiceCurve serviceCurve, List<TASWindow> tasWindowList) {
		super();
		this.serviceCurve = serviceCurve;
		this.tasWindowList = tasWindowList;
	}
	
	public ServiceCurve getServiceCurve() {
		return serviceCurve;
	}
	public void setServiceCurve(ServiceCurve serviceCurve) {
		this.serviceCurve = serviceCurve;
	}
	public Server getSTServer() {
		return stServer;
	}
	public void setSTServer(Server stServer) {
		this.stServer = stServer;
	}
	public List<TASWindow> getTASWindowTermsList() {
		return new LinkedList<TASWindow>(this.tasWindowList);
	}
	public void setTASGateScheduling(List<TASWindow> tasGateScheduling) {
		this.tasWindowList = new LinkedList<TASWindow>(tasGateScheduling);
	}
	
}
