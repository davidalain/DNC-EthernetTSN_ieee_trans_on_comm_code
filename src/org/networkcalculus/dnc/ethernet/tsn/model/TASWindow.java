package org.networkcalculus.dnc.ethernet.tsn.model;

import java.util.Arrays;

/**
 * This class represents all required values for the Time-Aware Shaper priority scheduling Service Curve calculation.
 * 
 * Note: 
 * 	All time references within this class are in us (microseconds)
 * 
 * Note2:
 * 	Initializing all Double and Integer values with null to avoid using any value before to be properly filled.
 * 
 * Note3: All these equations are related to [Zhao, 2018].
 * 	[Zhao, 2018] Zhao, L., Pop, P., & Craciunas, S. S. (2018). Worst-case latency analysis for IEEE 802.1 Qbv time sensitive networks using network calculus. Ieee Access, 6, 41803-41815.
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 */
public class TASWindow {

	/**
	 * Window index within GCL hyper-period with which have the same priority; 
	 */
	public Integer index = null;
	
	/**
	 * N_{P_m}
	 * 
	 * the limited number of length cases \overline{L}_{P_m} of guaranteed time slot for Pm traffic within a GCL hyper-period;
	 */
	public Long N_Pm = null;
	
	/**
	 * Created by David Alain do Nascimento.
	 * 
	 * The limited number of length cases \overline{L}_{P_m} of guaranteed time slot for Pm traffic within a T_Pm period;
	 */
	public Integer N_Pm_within_T_Pm = null;

	/**
	 * P_m
	 * 
	 * Priority
	 */
	public Integer priorityPm = null;

	/**
	 * T_{GCL}
	 * 
	 * The hyperperiod, which is the Least Common Multiple (LCM) of open-close cycles for all the priority queues of the critical traffic.
	 * 
	 * Value in microseconds (us)
	 */
	public Long T_GCL = null;

	/**
	 * t^{o,i}_{P_m} 
	 * 
	 * the beginning time of the open window w^{i}_{P_m};
	 * 
	 * Value in microseconds (us)
	 */
	public Double t_oi_Pm = null;

	/**
	 * t^{c,i}_{P_m} 
	 * 
	 * the end time of the open window w^{i}_{P_m};
	 * 
	 * Value in microseconds (us)
	 */
	public Double t_ci_Pm = null;

	/**
	 * l^{max}_{P_m}
	 * 
	 * Maximum frame size (in bytes) for flows with priority Pm.
	 * 
	 * Value in bytes.
	 */
	public Double l_max_Pm = null;

	/**
	 * l^{min}_{P_m}
	 * 
	 * Minimum frame size (in bytes) for flows with priority Pm.
	 * 
	 * Value in bytes.
	 */
	public Double l_min_Pm = null;

	/**
	 * T_{P_m}
	 * 
	 * The open-close cycle of the Gate G_Pm.
	 * 
	 * Value in microseconds (us).
	 */
	public Long T_Pm = null;

	/**
	 * l^{max}_{P_{m^{+}}}.
	 * 
	 * Maximum frame size (in bytes) for all flows with lower priority than Pm.
	 * 
	 * Value in bytes.
	 */
	public Double l_max_Pm_plus = null;

	/**
	 * t^{c,i}_{P_{m^{+}}}
	 * 
	 * Closing window time for priority P_{m^{+}} (lower priority than P_m) for the i-th window.
	 * 
	 * Eq. ?? Before Eq. 8.
	 * 
	 * The nearest time when the gate G_{P_{m^{+}}} is closed no earlier than t^{o,i}_{P_m}
	 * 
	 * Value in microseconds (us).
	 */
	public Double t_ci_Pm_plus = null;

	/**
	 * d^{np,i}_{P_{m^{+}}}
	 * 
	 * Worst-case non-preemption latency for the Pm traffic in the i-th open window w^{i}_{P_m} caused by a lower priority frame from queues Q_Pm+ (Q_{P_{m^{+}}})
	 * 
	 * Eq. 8.
	 * 	= min( l_max_Pm+ / C , t_ci_Pm+ - t_oi_Pm ), if G_Pm+(t_oi_Pm)=1.	
	 * 	= 0,                                         if G_Pm+(t_oi_Pm)=0.
	 * 
	 * Value in microseconds (us).
	 */
	public Double d_npi_Pm_plus = null;

	/**
	 * d^{np,i}_{L}
	 * 
	 * The worst-case non-preemption latency for the Pm traffic in the i-th open-window w^{i}_{P_m}.
	 * 
	 * It is the maximum latency caused by a lower priority frame due to the non-preemption.
	 * Thus, a higher priority frame must wait for finishing the transmission of a lower priority one.
	 * 
	 * Eq. 9.
	 * 	= max{ d_npi_Pm+ } | (m+1 <= m+ < n)	
	 * 
	 * Value in microseconds (us)
	 */
	public Double d_npi_L = null; 

	/**
	 * Eq. 10:
	 * 
	 * t^{np,i}_{L} = d^{np,i}_{L} + t^{o,i}_{P_m}
	 * 
	 * The starting time of guaranteed time slot \overline{L}^i_{P_m} for Pm traffic in w^{i}_{P_m} by just considering the lower priority traffic
	 * 
	 * Value in microseconds (us)
	 */
	public Double t_npi_L = null;		

	/**
	 * Eq. 11:
	 * 
	 * d^{gb}_{P_m} = \frac{l^{max}_{P_m}}{C}
	 * 
	 * Guard band length for priority Pm.
	 * 
	 * Transmission time for the maximum frame size (in bits) for priority Pm.
	 *  
	 * Value in microseconds (us)
	 */
	public Double d_gb_Pm = null;

	/**
	 * Eq. 12:
	 * 
	 * t^{gb,i}_{P_m} = t^{c,i}_{P_m} - d^{gb}_{P_m}.
	 * 
	 * Guard band starting time for priority Pm.
	 *  
	 * Value in microseconds (us).
	 */
	public Double t_gbi_Pm = null;

	/**
	 * Eq. 13:
	 * 
	 * t^{c,i}_{P_{m^{-}}} = \inf_{t \leq t^{o,i}_{P_m}} \{ G_{P_{m^{-}}}(t) = 0 \}.
	 * 
	 * Closing window time for priority Pm- (higher priority than Pm).
	 * 
	 * Value in microseconds (us).
	 */
	public Double t_ci_Pm_minus = null;

	/**
	 * Eq. 14:
	 * 
	 * t^{o,i}_{P_{m^{-}}} = \sup_{t \leq t^{c,i}_{P_m}} \{ G_{P_{m^{-}}}(t) = 0 \}.
	 * 
	 * Opening window time for priority Pm- (higher priority than Pm).
	 * 
	 * Value in microseconds (us).
	 */
	public Double t_oi_Pm_minus = null;

	/**
	 * Eq. 15:
	 * 
	 * t^{B,i}_{H} = \max_{1 \leq m^{-} \leq m-1} \left \{ t^{c,i}_{P_{m^{-}}}  \right \}.
	 * 
	 * Value in microseconds (us).
	 */
	public Double t_Bi_H = null;

	/**
	 * Eq. 16:
	 * 
	 * t^{E,i}_{H} = \min_{1 \leq m^{-} \leq m-1} \left \{ t^{o,i}_{P_{m^{-}}}  \right \}.
	 * 
	 * Value in microseconds (us).
	 */
	public Double t_Ei_H = null;

	/**
	 * t^{B,i}_{P_m}
	 * 
	 * Eq. 17.
	 *  = max(t_npi_L, t_Bi_H).
	 *  
	 * Value in microseconds (us)
	 */
	public Double t_Bi_Pm = null;

	/**
	 * t^{E,i}_{P_m}
	 * 
	 * Eq. 18.
	 *  = min(t_gbi_Pm, t_Ei_H).
	 *  
	 * Value in microseconds (us)
	 */
	public Double t_Ei_Pm = null;

	/**
	 * \overline{L}^i_{P_m}
	 * 
	 * i \in [1; N_{P_m}], the length of the i-th guaranteed time slot for Pm traffic;
	 * 
	 * Eq. 19
	 *  = max( t_Ei_Pm - t_Bi_Pm, l_min_Pm ), if t_Bi_Pm < t_Ei_Pm.
	 *  = 0,                                  if t_Bi_Pm >= t_Ei_Pm.
	 *  
	 * Value in microseconds (us).
	 */
	public Double L_bar_i_Pm = null;

	/**
	 * Eq. 20:
	 * 
	 * \overline{o}^i_{P_m} = t^{B,i}_{P_m} - t^{o,i}_{P_m} \Big | _{\overline{L}^i_{P_m} \neq 0}
	 * 
	 * Value in microseconds (us).
	 */
	public Double o_bar_i_Pm = null;

	/**
	 * Eq. 21:
	 * 
	 * o^{j,i}_{P_m} = (j - i) \cdot T_{P_m} - \overline{o}^i_{P_m} + \overline{o}^j_{P_m}.
	 * 
	 * However, this term can easily be computed by using:
	 * 
	 * o^{j,i}_{P_m} = t^{B,j}_{P_m} - t^{B,i}_{P_m}
	 * 
	 * j \in [i + 1; i + N_{P_m} - 1], 
	 * the relative offset, which is the time interval between the 
	 * starting time of the i-th and j-th guaranteed time slots for Pm traffic, 
	 * by taking the i-th guaranteed time slot as the reference;
	 *  
	 * Value in microseconds (us).
	 */
	public Double[] o_ji_Pm = null;

	/**
	 * d^{np,0}_{L}
	 * 
	 * Eq. 22.
	 *  = max{ min{ l_max_Pm+/C, t_Eiminus1_Pm - t_o_Pm+ } } | (M=1 <= m+ <= n).
	 *  
	 * Value in microseconds (us)	
	 */
	public Double d_np0_L = null;

	/**
	 * S^i_{P_m}
	 * 
	 * the maximum waiting time for the Pm traffic at its beginning backlog period by considering the guaranteed time slot \overline{L}^i_{P_m} as the benchmark;
	 * 
	 * Eq. 23.
	 *  = d_np0_L + t_Bi_Pm - t_Eiminus1_Pm.
	 *  
	 * Value in microseconds (us)
	 */
	public Double S_i_Pm = null;


	/**
	 * d^{i}_{PLCA}
	 * 
	 * Term created by David Alain do Nascimento.
	 * It represents the worst-case delay a frame can experience within an i-th guaranteed window when using PLCA for PHY access.
	 * 
	 * This term is only used in 'No PLCA-WRR Server' modeling.
	 * It is another term to be subtracted from 't' in the Service Curve equation. 
	 */
	public Double d_i_PLCA = null;

	
	/**
	 * Checks if gate is open for a given time 't'
	 * 
	 * @param t
	 * @return
	 */
	public boolean isGateOpen(double t) {
		return ((this.t_oi_Pm <= t) && (t <= this.t_ci_Pm));
	}

	/**
	 * Checks if a given time 't' is within of a guaranteed slot
	 * 
	 * @param t
	 * @return
	 */
	public boolean isWithinGuaranteedSlot(double t) {
		return ((this.t_Bi_Pm <= t) && (t <= this.t_Ei_Pm));
	}

	/**
	 * Check if other window is within of this window 
	 * 
	 * @param other
	 * @return
	 */
	private boolean isWithin(TASWindow other) {
		return ((this.t_oi_Pm <= other.t_oi_Pm) && (this.t_ci_Pm >= other.t_ci_Pm));
	}

	/**
	 * Checks if both 'this' and 'other' windows collide each other
	 * 
	 * @param other
	 * @return
	 */
	public boolean hasCollision(TASWindow other) {
		return ((this.t_oi_Pm < other.t_oi_Pm && this.t_ci_Pm > other.t_oi_Pm) || 	//'this' (left) window has collision with 'other' (right) window
				(this.t_oi_Pm > other.t_oi_Pm && this.t_oi_Pm < other.t_ci_Pm) ||	//'this' (right) window has collision with 'other' (left) window
				this.isWithin(other) || 											//'other' window is within 'this' window
				other.isWithin(this));												//'this' window is within 'other' window
	}

	/**
	 * Checks if both 'this' and 'other' windows collide each other
	 * 
	 * @param other
	 * @return
	 */
	public boolean hasGuaranteedSlotCollision(TASWindow other) {
		return (this.isWithinGuaranteedSlot(other.t_Bi_Pm) || 		//'other' window guaranteed slot begins within 'this' guaranteed slot
				this.isWithinGuaranteedSlot(other.t_Ei_Pm)) 		//'other' window guaranteed slot ends within 'this' guaranteed slot
				&&
				((this.L_bar_i_Pm > 0.0) && (other.L_bar_i_Pm > 0.0)); //Both guaranteed slots have length greater than 0.0
	}

	@Override
	public String toString() {
		return "TASWindow [index=" + index + ", N_Pm=" + N_Pm + ", priorityPm=" + priorityPm + ", T_GCL=" + T_GCL
				+ ", t_oi_Pm=" + t_oi_Pm + ", t_ci_Pm=" + t_ci_Pm + ", l_max_Pm=" + l_max_Pm + ", l_min_Pm=" + l_min_Pm
				+ ", T_Pm=" + T_Pm + ", l_max_Pm_plus=" + l_max_Pm_plus + ", t_ci_Pm_plus=" + t_ci_Pm_plus
				+ ", d_npi_Pm_plus=" + d_npi_Pm_plus + ", d_npi_L=" + d_npi_L + ", t_npi_L=" + t_npi_L + ", d_gb_Pm="
				+ d_gb_Pm + ", t_gbi_Pm=" + t_gbi_Pm + ", t_ci_Pm_minus=" + t_ci_Pm_minus + ", t_oi_Pm_minus="
				+ t_oi_Pm_minus + ", t_Bi_H=" + t_Bi_H + ", t_Ei_H=" + t_Ei_H + ", t_Bi_Pm=" + t_Bi_Pm + ", t_Ei_Pm="
				+ t_Ei_Pm + ", L_bar_i_Pm=" + L_bar_i_Pm + ", o_bar_i_Pm=" + o_bar_i_Pm + ", o_ji_Pm="
				+ Arrays.toString(o_ji_Pm) + ", d_np0_L=" + d_np0_L + ", S_i_Pm=" + S_i_Pm + "]";
	}

}
