package org.networkcalculus.dnc.ethernet.utils;

import java.security.InvalidParameterException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Collection;
import java.util.List;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 *
 */
public class NumberUtil {

	private static final NumberFormat FORMATTER = new DecimalFormat("#0.000000000");
	
	public static String doubleToString(double v) {
		return FORMATTER.format(v);
	}
	
	public static String doubleToString(double v, int strOutLen) {
		
		final StringBuilder sb = new StringBuilder(FORMATTER.format(v));
		
		while(sb.length() < strOutLen)
			sb.append(" ");
		
		return sb.toString();
	}
	
	/**
	 * Checks if 'value' is between 'a' and 'b'
	 *  
	 * @param a
	 * @param b
	 * @param value
	 * @return
	 */
	public static boolean isBetween(int a, int b, int value) {

		final int min = Math.min(a, b);
		final int max = Math.max(a, b);

		return (value >= min && value <= max);
	}
	
	public static <T extends Comparable<T>> boolean isBetween(T a, T b, T value) {
		
		final T min = a.compareTo(b) < 0 ? a : b;
		final T max = a.compareTo(b) > 0 ? a : b;
		
		return (value.compareTo(min) >= 0 && value.compareTo(max) <= 0);
	}

	/**
	 * From https://stackoverflow.com/questions/4201860/how-to-find-gcd-lcm-on-a-set-of-numbers
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static long gcd(long a, long b){
		while (b > 0)
		{
			long temp = b;
			b = a % b; // % is remainder
			a = temp;
		}
		return a;
	}

	/**
	 * From https://stackoverflow.com/questions/4201860/how-to-find-gcd-lcm-on-a-set-of-numbers
	 * 
	 * @param input
	 * @return
	 */
	public static long gcd(long[] input){
		long result = input[0];
		for(int i = 1; i < input.length; i++) 
			result = gcd(result, input[i]);
		return result;
	}

	/**
	 * Calculates Least Common Multiplier (LCM) among two values
	 * 
	 * Got from https://stackoverflow.com/questions/4201860/how-to-find-gcd-lcm-on-a-set-of-numbers
	 * 
	 * @param a
	 * @param b
	 * @return
	 */
	public static long lcm(long a, long b){
		return a * (b / gcd(a, b));
	}

	/**
	 * Calculates Least Common Multiplier (LCM) among an array of values
	 * 
	 * Got from https://stackoverflow.com/questions/4201860/how-to-find-gcd-lcm-on-a-set-of-numbers
	 * 
	 * @param input
	 * @return
	 */
	public static long lcm(long[] input){
		if(input == null)
			throw new InvalidParameterException("input cannot be null");
		if(input.length == 0)
			throw new InvalidParameterException("input cannot be empty");
		
		long result = input[0];
		for(int i = 1; i < input.length; i++) 
			result = lcm(result, input[i]);
		return result;
	}

	public static long gcd(List<Long> input) {
		long result = input.get(0);
		for(int i = 1; i < input.size(); i++) 
			result = gcd(result, input.get(i));
		return result;
	}

	/**
	 * Calculates Least Common Multiplier (LCM) among a collections of values
	 * 
	 * @param input
	 * @return
	 */
	public static long lcm(Collection<Long> input) {
		if(input == null)
			throw new InvalidParameterException("input cannot be null");
		if(input.isEmpty())
			throw new InvalidParameterException("input cannot be empty");
		
		long result = 1;

		for(long num : input) 
			result = lcm(result, num);
		return result;
	}
	
	public static <T extends Comparable<T>> T min(Collection<T> input) {
		if(input == null)
			throw new InvalidParameterException("input cannot be null");
		if(input.isEmpty())
			throw new InvalidParameterException("input cannot be empty");
		
		T min = input.iterator().next();
		for(T v : input) {
			min = min.compareTo(v) < 0 ? min : v;
		}
		return min;
	}
	
	public static <T extends Comparable<T>> T max(Collection<T> input) {
		if(input == null)
			throw new InvalidParameterException("input cannot be null");
		if(input.isEmpty())
			throw new InvalidParameterException("input cannot be empty");
		
		T max = input.iterator().next();
		for(T v : input) {
			max = max.compareTo(v) > 0 ? max : v;
		}
		return max;
	}

}
