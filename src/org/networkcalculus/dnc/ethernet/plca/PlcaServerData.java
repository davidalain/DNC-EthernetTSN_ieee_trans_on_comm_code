package org.networkcalculus.dnc.ethernet.plca;

import java.security.InvalidParameterException;
import java.util.List;

import org.networkcalculus.dnc.curves.ServiceCurve;
import org.networkcalculus.dnc.ethernet.tsn.data.STFlowData;
import org.networkcalculus.dnc.network.server_graph.Server;

/**
 * Physical Layer Collision Avoidance (PLCA) Server's Data
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 */
public class PlcaServerData {

	private ServiceCurve serviceCurve;
	private Server plcaServer;
	private List<STFlowData> stFlowDataList;

	/**
	 * PLCA Burst Mode disabled, plcaWeightWRR = 1
	 * PLCA Burst Mode enabled, plcaWeightWRR > 1
	 */
	private final int plcaWeightWRR;

	private final double minFrameSizeBytes;

	private final double maxFrameSizeBytes;

	public PlcaServerData(int plcaWeightWRR, List<STFlowData> stFlowDataList) {

		if(plcaWeightWRR < 1)
			throw new InvalidParameterException("plcaWeightWRR cannot be lower than 1");
		
		this.plcaWeightWRR = plcaWeightWRR;
		this.stFlowDataList = stFlowDataList;

		Double minSizeBytes = null;
		Double maxSizeBytes = null;

		if(stFlowDataList.isEmpty()) {
			/**
			 * Note: when there is no frame to be sent from a node within a PLCA cycle, 
			 * then max Transmit Opportunity (TO) length for that node is equivalent to a TO silence
			 */
			final double silenceSizeBits = PlcaParameters.PLCA_TO_SILENCE_SIZE_BITS;
			
			minSizeBytes = silenceSizeBits / 8.0;
			maxSizeBytes = silenceSizeBits / 8.0;
		}else {
			//min = Double.MAX_VALUE;
			//max = Double.MIN_VALUE;
		}

		for(STFlowData stFlowData : this.stFlowDataList) {
			if(minSizeBytes == null)
				minSizeBytes = stFlowData.getMessageMinSizeBytes();
			else
				minSizeBytes = Math.min(minSizeBytes, stFlowData.getMessageMinSizeBytes());

			if(maxSizeBytes == null)
				maxSizeBytes = stFlowData.getMessageMaxSizeBytes();
			else
				maxSizeBytes = Math.min(maxSizeBytes, stFlowData.getMessageMaxSizeBytes());
		}

		this.minFrameSizeBytes = minSizeBytes;
		this.maxFrameSizeBytes = maxSizeBytes;
	}

	public final ServiceCurve getServiceCurve() {
		return serviceCurve;
	}
	public final Server getPlcaServer() {
		return plcaServer;
	}
	public final List<STFlowData> getStFlowDataList() {
		return stFlowDataList;
	}
	public final void setServiceCurve(ServiceCurve serviceCurve) {
		this.serviceCurve = serviceCurve;
	}
	public final void setPlcaServer(Server plcaServer) {
		this.plcaServer = plcaServer;
	}
	public final void setStFlowDataList(List<STFlowData> stFlowDataList) {
		this.stFlowDataList = stFlowDataList;
	}
	public final int getPlcaWeight() {
		return plcaWeightWRR;
	}
	public final double getMinFrameSizeBytes() {
		return minFrameSizeBytes;
	}
	public final double getMaxFrameSizeBytes() {
		return maxFrameSizeBytes;
	}


}
