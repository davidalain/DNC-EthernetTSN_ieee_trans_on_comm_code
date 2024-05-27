package org.networkcalculus.dnc.ethernet.tsn;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Set;

import org.networkcalculus.dnc.curves.ArrivalCurve;
import org.networkcalculus.dnc.curves.Curve;
import org.networkcalculus.dnc.curves.ServiceCurve;
import org.networkcalculus.dnc.ethernet.EthernetNetwork;
import org.networkcalculus.dnc.ethernet.EthernetPhyStandard;
import org.networkcalculus.dnc.ethernet.plca.PlcaParameters;
import org.networkcalculus.dnc.ethernet.plca.PlcaServerData;
import org.networkcalculus.dnc.ethernet.tsn.ExecutionConfig.PLCAModeling;
import org.networkcalculus.dnc.ethernet.tsn.data.STFlowData;
import org.networkcalculus.dnc.ethernet.tsn.entry.InterfaceInfoEntry;
import org.networkcalculus.dnc.ethernet.tsn.model.TASWindow; 

/**
 * Note: All these equations are related to [Zhao, 2018].
 * 	[Zhao, 2018] Zhao, L., Pop, P., & Craciunas, S. S. (2018). Worst-case latency analysis for IEEE 802.1 Qbv time sensitive networks using network calculus. Ieee Access, 6, 41803-41815.
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 */
public class TASCurveBuilder {

	private static TASCurveBuilder instance;

	public static TASCurveBuilder getInstance() {
		if(instance == null)
			instance = new TASCurveBuilder();

		return instance;
	}

	private TASCurveBuilder() {

	}

	/**
	 * An Arrival Curve for ST traffic in TSN is composed by a sum of many Time-Shifted Periodic (Staircase) curves [Zhao, 2018, p.29].
	 * We are using a linear approximation to this staircase based on [Bouillard, 2018, p.150].
	 * 
	 * [Zhao, 2018] Zhao, L., Pop, P., & Craciunas, S. S. (2018). Worst-case latency analysis for IEEE 802.1 Qbv time sensitive networks using network calculus. Ieee Access, 6, 41803-41815.
	 * [Bouillard, 2018] Bouillard, Anne, Marc Boyer, and Euriell Le Corronc. Deterministic Network Calculus: From Theory to Practical Implementation. John Wiley & Sons, 2018.
	 * 
	 * @param stFlowData
	 * @return
	 */
	public final ArrivalCurve buildSTArrivalCurve(STFlowData stFlowData) {

		final double messageMaxSizeBytes = stFlowData.getMessageMaxSizeBytes();
		final double messageMaxSizeBits = (messageMaxSizeBytes * 8.0);
		final double messagePeriodUs = stFlowData.getSTMessageEntry().periodUs;
		final double messageOffsetUs = stFlowData.getSTMessageEntry().offsetUs;

		final double rateBitsPerUs = messageMaxSizeBits/messagePeriodUs; //Rate in bits per microsecond (bpus)
		final double burstBits = //burst in bits
				(messageMaxSizeBits * (messagePeriodUs - messageOffsetUs)) 
				/ 
				(messagePeriodUs); 

		final double R = rateBitsPerUs;
		final double b = burstBits;

		final ArrivalCurve newMessageCurve = Curve.getFactory().createTokenBucket(R, b);

		return newMessageCurve;
	}

	/**
	 * 
	 * This method builds a Rate-Latency Service curve for a single priority
	 * 
	 * See:
	 * 		Eq. 5 and Eq. 6 in [Zhao, 2018]
	 * 		Eq. 44 and Eq. 45 in [Zhao, 2020]
	 * 		Fig. 2 in [Gollan, 2007, pg.4]
	 * 
	 * 	[Zhao, 2018] Zhao, L., Pop, P., & Craciunas, S. S. (2018). Worst-case latency analysis for IEEE 802.1 Qbv time sensitive networks using network calculus. Ieee Access, 6, 41803-41815. 
	 * 	[Zhao, 2020] Zhao, Luxi, et al. "Latency Analysis of Multiple Classes of AVB Traffic in TSN with Standard Credit Behavior using Network Calculus." arXiv preprint arXiv:2005.08256 (2020).
	 * 	[Gollan, 2007, pg.4] Gollan, Nicos, and Jens Schmitt. On the TDMA Design Problem Under Real-Time Constraints in Wireless Sensor Networks. Technical Report 359/07, University of Kaiserslautern, Germany, 2007.
	 * 
	 * @param phyStandard
	 * @param tasWindowList
	 * @return
	 */
	public final ServiceCurve buildSTRateLatencyServiceCurve(EthernetPhyStandard phyStandard, List<TASWindow> tasWindowList, PLCAModeling plcaModeling) {

		ServiceCurve beta_Pm = null;
		ServiceCurve[] beta_i_Pm = null;

		//Sanity check
		if(phyStandard == null)
			throw new InvalidParameterException("phyStandard cannot be null");
		if(tasWindowList == null)
			throw new InvalidParameterException("tasWindowList cannot be null");
		if(tasWindowList.isEmpty())
			throw new InvalidParameterException("tasWindowList cannot be empty");

		final Set<Long> periodLenghtSet = new HashSet<Long>();
		tasWindowList.forEach(tasWindow -> periodLenghtSet.add(tasWindow.T_GCL));
		if(periodLenghtSet.size() != 1)
			throw new InvalidParameterException("There are more than one T_GCL value among TASWindow instances: " + periodLenghtSet);

		/**
		 * All T_GCL values are equal for TASWindow from the same EthernetInterface 
		 */
		final double T_GCL = (double) tasWindowList.get(0).T_GCL;
		final int size = tasWindowList.size();

		beta_i_Pm = new ServiceCurve[size];

		for(int i = 0 ; i < size ; i++) {

			TASWindow window_i = tasWindowList.get(i);

			Objects.requireNonNull(window_i.L_bar_i_Pm, "window_i.L_bar_i_Pm cannot be null");
			Objects.requireNonNull(window_i.S_i_Pm, "window_i.S_i_Pm cannot be null");
			Objects.requireNonNull(window_i.o_ji_Pm, "window_i.o_ji_Pm cannot be null");

			for(int j = i ; j <= i + window_i.N_Pm - 1 ; j++) {

				TASWindow window_j = tasWindowList.get(j % size);

				Objects.requireNonNull(window_j.L_bar_i_Pm, "window_j.L_bar_i_Pm cannot be null");
				Objects.requireNonNull(window_j.S_i_Pm, "window_j.S_i_Pm cannot be null");
				Objects.requireNonNull(window_j.o_ji_Pm,"window_j.o_ji_Pm cannot be null");

				/*
				 * d_i_PLCA term is used only for the PLCA modeling that does not use a server for PLCA.
				 */
				if(Objects.equals(plcaModeling, ExecutionConfig.PLCAModeling.SEPARATED_PLCA_SERVER_MODELING))
					Objects.requireNonNull(window_j.d_i_PLCA, "window_j.d_i_PLCA cannot be null");

				/**
				 * Terms based in [Zhao, 2018] and [Zhao, 2020]
				 */
				final double L_bar_j_Pm = window_j.L_bar_i_Pm;	//Guaranteed service for index j
				final double S_i_Pm = window_i.S_i_Pm;			//Maximum waiting time for the backlog period
				final double o_ji_Pm = window_i.o_ji_Pm[j % size];

				//final double d_i_PLCA = Objects.equals(plcaModeling, ExecutionConfig.PLCAModeling.SEPARATED_PLCA_SERVER_MODELING) ? window_i.d_i_PLCA : 0.0;

				final double d_i_PLCA;
				switch(plcaModeling) {
				case SEPARATED_PLCA_SERVER_MODELING:			d_i_PLCA = window_i.d_i_PLCA;	break;
				case SINGLE_PLCA_SERVER_MODELING:		d_i_PLCA = 0.0;					break;
				default:
					throw new IllegalArgumentException("Unexpected value: " + plcaModeling);
				}

				final double t0 = ((T_GCL - L_bar_j_Pm - S_i_Pm - o_ji_Pm) + d_i_PLCA); //in microsecond (us)

				/**
				 * Terms based in [Gollan, 2007, pg.4] and [Zhao, 2018]
				 */
				final double C = phyStandard.rate_bpus; 	//Link capacity in bits per microseconds (bpus)
				final double s = L_bar_j_Pm;				//in microsecond (us)
				final double f = T_GCL;						//in microsecond (us)
				final double R = (s/f)*C;					//rate in bits per microseconds (bpus)
				final double T = (f - s) - t0;				//latency (in microseconds)


				final ServiceCurve beta_ji_Pm = Curve.getFactory().createRateLatency(R, T);

				//				System.out.println("[new] serviceCurve="+beta_ji_Pm);
				//				System.out.println("[before] serviceCurve="+beta_i_Pm[i]);


				/**
				 * [Zhao, 2018, Eq. 4]
				 */
				if(beta_i_Pm[i] == null)
					beta_i_Pm[i] = beta_ji_Pm;
				else
					beta_i_Pm[i] = Curve.getUtils().add(beta_i_Pm[i], beta_ji_Pm);

				//				System.out.println("[after] serviceCurve="+beta_i_Pm[i]);
				//				System.out.println();

			}

		}

		/**
		 * [Zhao, 2018, Eq. 7]
		 */
		beta_Pm = beta_i_Pm[0];
		for(int i = 1 ; i < size ; i++) {
			beta_Pm = Curve.getUtils().min(beta_Pm, beta_i_Pm[i]);
		}
		return beta_Pm;
	}

	/**
	 * 
	 * This method builds a Rate-Latency Service curve for a single priority
	 * 
	 * See:
	 * 		Eq. 5 and Eq. 6 in [Zhao, 2018]
	 * 		Eq. 44 and Eq. 45 in [Zhao, 2020]
	 * 		Fig. 2 in [Gollan, 2007, pg.4]
	 * 
	 * 	[Zhao, 2018] Zhao, L., Pop, P., & Craciunas, S. S. (2018). Worst-case latency analysis for IEEE 802.1 Qbv time sensitive networks using network calculus. Ieee Access, 6, 41803-41815. 
	 * 	[Zhao, 2020] Zhao, Luxi, et al. "Latency Analysis of Multiple Classes of AVB Traffic in TSN with Standard Credit Behavior using Network Calculus." arXiv preprint arXiv:2005.08256 (2020).
	 * 	[Gollan, 2007, pg.4] Gollan, Nicos, and Jens Schmitt. On the TDMA Design Problem Under Real-Time Constraints in Wireless Sensor Networks. Technical Report 359/07, University of Kaiserslautern, Germany, 2007.
	 * 
	 * @param phyStandard
	 * @param tasWindowDataList
	 * @return
	 */
	public final Map<Integer,ServiceCurve> buildSTplusPLCARateLatencyServiceCurve(InterfaceInfoEntry targetInterfaceInfo, TASWindowsBuilder networkConfigBuilder, PLCAModeling plcaModeling){

		ServiceCurve beta_Pm = null;
		ServiceCurve[] beta_i_Pm = null;

		final Map<Integer,ServiceCurve> mapPriorityServiceCurve = new HashMap<>();

		final EthernetPhyStandard phyStandard = networkConfigBuilder.getMapInterfaceInfoPhyStandard().get(targetInterfaceInfo);

		final Map<Integer,List<TASWindow>> mapPriorityTasWindowList = networkConfigBuilder.getMapInterfaceInfoPriorityTasWindowList().get(targetInterfaceInfo);

		//Sanity check
		if(phyStandard == null)
			throw new InvalidParameterException("phyStandard cannot be null");

		//Sanity check
		final Set<Long> tGclSet = new HashSet<Long>();
		for(Entry<Integer,List<TASWindow>> entry : mapPriorityTasWindowList.entrySet()) {

			/**
			 * All T_GCL values must be equal among all TASWindow from the same EthernetInterface
			 */
			final List<TASWindow> tasWindowList = entry.getValue();
			tasWindowList.forEach(tasWindow -> tGclSet.add(tasWindow.T_GCL));
			if(tGclSet.size() != 1)
				throw new InvalidParameterException("There are more than one T_GCL value among TASWindow instances: " + tGclSet);
		}

		final double T_GCL = (double) tGclSet.iterator().next();

		for(Entry<Integer,List<TASWindow>> entry : mapPriorityTasWindowList.entrySet()) {

			final int priority = entry.getKey();
			final List<TASWindow> tasWindowList = entry.getValue();

			final int size = tasWindowList.size();

			beta_i_Pm = new ServiceCurve[size];

			for(int i = 0 ; i < size ; i++) {

				TASWindow window_i = tasWindowList.get(i);

				Objects.requireNonNull(window_i.L_bar_i_Pm, "window_i.L_bar_i_Pm cannot be null");
				Objects.requireNonNull(window_i.S_i_Pm, "window_i.S_i_Pm cannot be null");
				Objects.requireNonNull(window_i.o_ji_Pm,"window_i.o_ji_Pm cannot be null");

				if(Objects.equals(plcaModeling, ExecutionConfig.PLCAModeling.SEPARATED_PLCA_SERVER_MODELING))
					Objects.requireNonNull(window_i.d_i_PLCA,"window_i.d_i_PLCA cannot be null");

				for(int j = i ; j <= i + window_i.N_Pm - 1 ; j++) {

					TASWindow window_j = tasWindowList.get(j % size);

					Objects.requireNonNull(window_j.L_bar_i_Pm, "window_j.L_bar_i_Pm cannot be null");
					Objects.requireNonNull(window_j.S_i_Pm, "window_j.S_i_Pm cannot be null");
					Objects.requireNonNull(window_j.o_ji_Pm, "window_j.o_ji_Pm cannot be null");

					/**
					 * Terms based in [Zhao, 2018] and [Zhao, 2020]
					 */
					final double L_bar_j_Pm = window_j.L_bar_i_Pm;	//Guaranteed service for index j
					final double S_i_Pm = window_i.S_i_Pm;			//Maximum waiting time for the backlog period
					final double o_ji_Pm = window_i.o_ji_Pm[j % size];

					//					final double d_i_PLCA = Objects.equals(plcaModeling, ExecutionConfig.PLCAModeling.SEPARATED_PLCA_SERVER_MODELING) ? window_i.d_i_PLCA : 0.0;
					final double d_i_PLCA;
					switch(plcaModeling) {
					case SEPARATED_PLCA_SERVER_MODELING:	d_i_PLCA = window_i.d_i_PLCA;	break;
					default:						d_i_PLCA = 0.0;					break;
					}

					final double t0 = ((T_GCL - L_bar_j_Pm - S_i_Pm - o_ji_Pm) + d_i_PLCA); //in microsecond (us)

					/**
					 * Terms based in [Gollan, 2007, pg.4] and [Zhao, 2018]
					 */
					final double C = phyStandard.rate_bpus; 	//Link capacity in bits per microseconds (bpus)
					final double s = L_bar_j_Pm;				//in microsecond (us)
					final double f = T_GCL;						//in microsecond (us)
					final double R = (s/f)*C;					//rate in bits per microseconds (bpus)
					final double T = (f - s) - t0;				//latency (in seconds)

					final ServiceCurve beta_ji_Pm = Curve.getFactory().createRateLatency(R, T); 

					System.out.println("[new] serviceCurve="+beta_ji_Pm);
					System.out.println("[before] serviceCurve="+beta_i_Pm);

					if(beta_i_Pm[i] == null)
						beta_i_Pm[i] = beta_ji_Pm;
					else
						beta_i_Pm[i] = Curve.getUtils().add(beta_i_Pm[i], beta_ji_Pm);

					System.out.println("[after] serviceCurve="+beta_i_Pm);

				}

			}

			beta_Pm = beta_i_Pm[0];
			for(int i = 1 ; i < size ; i++) {
				beta_Pm = Curve.getUtils().min(beta_Pm, beta_i_Pm[i]);
			}

			mapPriorityServiceCurve.put(priority, beta_Pm);
		}

		return mapPriorityServiceCurve;
	}

	public final ServiceCurve buildDefaultRateLatencyServiceCurve(EthernetPhyStandard phyStandard) {

		final double rateBitsPerUs = phyStandard.rate_bpus;		//rate in bits per microseconds (bpus)
		final double latencyUs = 0.0;							//latency in microseconds (us)

		final double R = rateBitsPerUs;
		final double T = latencyUs;

		return Curve.getFactory().createRateLatency(R, T);
	}

	/**
	 * PLCA service curve equations
	 * 
	 * Based on [Soni, 2018, eq.4]
	 * 
	 * [Soni, 2018] Aakash Soni, Xiaoting Li, Jean-Luc Scharbarg, and Christian Fraboul. 2018. WCTT analysis of avionics Switched Ethernet Network with WRR Scheduling. In Proceedings of the 26th International Conference on Real-Time Networks and Systems (RTNS ’18). Association for Computing Machinery, New York, NY, USA, 213–222. DOI:https://doi.org/10.1145/3273905.3273925
	 * 
	 * @param ethernetNetwork
	 * @param targetInterface
	 * @param networkConfigBuilder
	 * @return
	 */
	public final Map<Integer,ServiceCurve> buildPLCAServiceCurve(EthernetNetwork ethernetNetwork, InterfaceInfoEntry targetInterface, TASWindowsBuilder networkConfigBuilder) {

		//Sanity check
		Objects.requireNonNull(ethernetNetwork);
		Objects.requireNonNull(targetInterface);
		Objects.requireNonNull(networkConfigBuilder);

		final Map<Integer,ServiceCurve> mapPriorityServiceCurve = new HashMap<Integer, ServiceCurve>();

		final EthernetPhyStandard phyStandardTarget = networkConfigBuilder.getMapInterfaceInfoPhyStandard().get(targetInterface);

		//Assert all interfaces are shared medium access
		for(InterfaceInfoEntry interfaceInfo : networkConfigBuilder.getMapInterfaceInfoLinkInfo().get(targetInterface).getInterfaceInfoSet()) {

			final EthernetPhyStandard phyStandardCurrent = networkConfigBuilder.getMapInterfaceInfoPhyStandard().get(interfaceInfo);
			if(phyStandardCurrent.isDedicatedMediumAccess)
				throw new InvalidParameterException("It doesn't possible to build a PLCA Server for dedicated medium access interface: " + interfaceInfo + ", phyStandard: " + phyStandardCurrent);
		}

		/**
		 * Calculate values used in Weighted Round Robin scheduling
		 */
		for(int priorityTarget : networkConfigBuilder.getMapInterfaceInfoPriorityPLCAServerData().get(targetInterface).keySet()) {

			double q_i = 0;
			double Q_i = 0;
			double minFrameSizeBitsTargetInterface = 0;
			double maxFrameSizeBitsTargetInterface = 0;

			/**
			 * Firstly, add the beacon
			 */
			Q_i += PlcaParameters.PLCA_BEACON_COUNT * PlcaParameters.PLCA_BEACON_SIZE_BITS;

			/**
			 * Iterate over Ethernet interfaces from the same multidrop bus
			 */
			for(InterfaceInfoEntry interfaceInfo : networkConfigBuilder.getMapInterfaceInfoLinkInfo().get(targetInterface).getInterfaceInfoSet()) {

				if(networkConfigBuilder.getMapInterfaceInfoPriorityPLCAServerData().get(interfaceInfo).isEmpty()) {
					throw new InvalidParameterException("networkConfig.getMapInterfaceInfoPriorityPLCAServerData().get(interfaceInfo) cannot be empty, interfaceInfo: " + interfaceInfo);
				}

				/**
				 * Iterate over PLCA Servers into current Ethernet interface
				 */
				for(Entry<Integer,PlcaServerData> entry : networkConfigBuilder.getMapInterfaceInfoPriorityPLCAServerData().get(interfaceInfo).entrySet()) {

					final int priority = entry.getKey();
					final PlcaServerData plcaServerData = entry.getValue();

					if(plcaServerData == null)
						throw new InvalidParameterException("plcaServerData cannot be null. interfaceInfo="+interfaceInfo+", priority="+priority);

					final double plcaWeightWRR = plcaServerData.getPlcaWeight();
					final double minFrameSizeBits = 8.0 * plcaServerData.getMinFrameSizeBytes();
					final double maxFrameSizeBits = 8.0 * plcaServerData.getMaxFrameSizeBytes();

					//System.out.println("Calculating Quanta.\n\ttarget=        "+target+",\n\tinterfaceInfo= " + interfaceInfo + ",\n\tpriority=       " + priority+",\n\tpriorityTarget= "+priorityTarget);

					if(Objects.equals(interfaceInfo, targetInterface) && (priority == priorityTarget)) {
						q_i = plcaWeightWRR * (PlcaParameters.PLCA_IPG_SIZE_BITS + minFrameSizeBits);
						minFrameSizeBitsTargetInterface = minFrameSizeBits;
						maxFrameSizeBitsTargetInterface = maxFrameSizeBits;
						//System.out.println("(q_i = plcaWeightWRR * minFrameSize) ==> " + q_i + " = " + plcaWeightWRR + " * " + minFrameSize);
					} else {
						Q_i += plcaWeightWRR * (PlcaParameters.PLCA_IPG_SIZE_BITS + maxFrameSizeBits);
						//System.out.println("(Q_i += plcaWeightWRR * maxFrameSize) ==> " + Q_i + " += " + plcaWeightWRR + " * " + maxFrameSize);
					}
					//System.out.println();

				}

			}
			
			/**
			 * Build PLCA Service Curve using Rate-Latency Service Curve model
			 */
			final double R = (q_i / (q_i + Q_i)) * phyStandardTarget.rate_bpus;		//in bits per microsecond (bpus)
			final double T = Q_i / phyStandardTarget.rate_bpus;						//in microseconds (us)
			/**
			 * TODO:
			 */
			//final double R = (minFrameSizeBitsTargetInterface / (q_i + Q_i)) * phyStandardTarget.rate_bpus;	//in bits per microsecond (bpus)
			//final double T = (PlcaParameters.PLCA_IPG_SIZE_BITS + Q_i) / phyStandardTarget.rate_bpus;			//in microseconds (us)
			

			//System.out.println("R = " + R);
			//System.out.println("T = " + T);
			//System.out.println("phyStandardTarget.rate = " + phyStandardTarget.rate);

			final ServiceCurve serviceCurve = Curve.getFactory().createRateLatency(R, T);

			mapPriorityServiceCurve.put(priorityTarget, serviceCurve);
			networkConfigBuilder.getMapInterfaceInfoPriorityPLCAServerData().get(targetInterface).get(priorityTarget).setServiceCurve(serviceCurve);
		}

		return mapPriorityServiceCurve;
	}




}
