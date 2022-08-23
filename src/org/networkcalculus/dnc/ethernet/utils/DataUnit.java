package org.networkcalculus.dnc.ethernet.utils;

import org.apache.commons.math3.util.Pair;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 *
 */
public enum DataUnit {
	
	/**
	 * All multipliers are expressed in bits
	 */
	
	b(1.0),
	kb(1.0e3),
	Mb(1.0e6),
	Gb(1.0e9),
	Tb(1.0e12),
	Pb(1.0e15),

	B(1.0 * 8.0),
	kB(1.0e3 * 8.0),
	MB(1.0e6 * 8.0),
	GB(1.0e9 * 8.0),
	TB(1.0e12 * 8.0),
	PB(1.0e15 * 8.0),

	kib(1024.0),
	Mib(1024.0*1024.0),
	Gib(1024.0*1024.0*1024.0),
	Tib(1024.0*1024.0*1024.0*1024.0),
	Pib(1024.0*1024.0*1024.0*1024.0*1024.0),

	kiB(1024.0 * 8.0),
	MiB(1024.0*1024.0 * 8.0),
	GiB(1024.0*1024.0*1024.0 * 8.0),
	TiB(1024.0*1024.0*1024.0*1024.0 * 8.0),
	PiB(1024.0*1024.0*1024.0*1024.0*1024.0 * 8.0);

	private final double multiplier;

	private DataUnit(double value) {
		this.multiplier = value;
	}

	public static double convert(double value, DataUnit unitIn, DataUnit unitOut) {
		return (unitIn.multiplier / unitOut.multiplier) * value;
	}
	
	public static double toBits(double value, DataUnit unitIn) {
		return convert(value, unitIn, DataUnit.b);
	}
	
	public static Pair<Double,DataUnit> create(double value, DataUnit dataUnit){
		return new Pair<Double,DataUnit>(value, dataUnit);
	}
}
