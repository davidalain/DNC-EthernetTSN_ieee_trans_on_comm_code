package org.networkcalculus.dnc.ethernet.tsn.model;

import java.util.Collection;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.networkcalculus.dnc.ethernet.tsn.entry.InterfaceInfoEntry;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 *
 */
public class LinkInfo {

	private final Set<InterfaceInfoEntry> interfaces;
	
	public LinkInfo(InterfaceInfoEntry first, InterfaceInfoEntry ... others) {
		
		this.interfaces = new HashSet<InterfaceInfoEntry>();
		this.interfaces.add(first);
		
		for(InterfaceInfoEntry other : others) {
			this.interfaces.add(other);
		}
	}
	
	public LinkInfo(Collection<InterfaceInfoEntry> interfaces) {
		this.interfaces = new HashSet<InterfaceInfoEntry>(interfaces);
	}
	
	public void addInterfaceInfo(InterfaceInfoEntry other) {
		this.interfaces.add(other);
	}

	public final Set<InterfaceInfoEntry> getInterfaceInfoSet() {
		return new HashSet<InterfaceInfoEntry>(interfaces);
	}

	@Override
	public int hashCode() {
		return Objects.hash(interfaces);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LinkInfo other = (LinkInfo) obj;
		return Objects.equals(interfaces, other.interfaces);
	}

	@Override
	public String toString() {
		return "LinkInfo [interfaces=" + interfaces + "]";
	}

}
