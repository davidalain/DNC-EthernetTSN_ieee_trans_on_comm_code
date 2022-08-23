package org.networkcalculus.dnc.ethernet.plca;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 *
 */
public class PlcaParameters {

	/**
	 * See "30.16.1.1.5 aPLCATransmitOpportunityTimer" in \cite[Sec. 30.16.1.1.5]{ieee_802.3cg:2019}
	 * 
	 * Default is 32-bit.
	 */
	public static final double PLCA_TO_SILENCE_SIZE_BITS = 32.0;

	/**
	 * See "148.4.4.4 Timers" in \cite[Sec. 148.4.4.4]{ieee_802.3cg:2019}
	 * 
	 * Default is 20-bit.
	 */
	public static final double PLCA_BEACON_SIZE_BITS = 20.0;

	
	/**
	 * There is a single beacon transmission in each PLCA cycle.
	 */
	public static final int PLCA_BEACON_COUNT = 1;
	
	/**
	 * In some cases, when there are TOs in SILENCE from other devices before the TO of current device with duration lesser than an IPG,
	 * a frame transmission from current device must be preceded by some COMMIT signals repeatedly during the length of an IPG.
	 *  
	 * This time length must be considered in the PLCA modeling.
	 * 
	 * See 	\cite[Sec. 5.4.3.2]{automotive_ethernet_book_kirsten_matheus:2021} and 
	 * 		\cite[Fig. 5.40]{automotive_ethernet_book_kirsten_matheus:2021} 
	 * 
	 */
	public static final double PLCA_IPG_SIZE_BITS = 32.0;
	
}
