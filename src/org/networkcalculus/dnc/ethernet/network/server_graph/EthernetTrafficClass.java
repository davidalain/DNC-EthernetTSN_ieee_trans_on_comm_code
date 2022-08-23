package org.networkcalculus.dnc.ethernet.network.server_graph;

import java.security.InvalidParameterException;

import org.networkcalculus.dnc.ethernet.utils.NumberUtil;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 *
 */
public enum EthernetTrafficClass {

	TT(7),
	AVB_A(6),
	AVB_B(5),
	BE(0);

	private static final int MAX_PRIORITY = 0;
	private static final int MIN_PRIORITY = 7;

	public static final int DEFAULT_NON_PRIORITIZED = MIN_PRIORITY;

	public final int priority;

	private EthernetTrafficClass(int priority) {
		if(!NumberUtil.isBetween(MIN_PRIORITY, MAX_PRIORITY, priority))
			throw new InvalidParameterException("Invalid priority="+priority);

		this.priority = priority;
	}

}
