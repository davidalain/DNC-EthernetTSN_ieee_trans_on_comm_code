package org.networkcalculus.dnc.ethernet.tsn.entry;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import org.apache.commons.math3.util.Pair;

/**
 * 
 * @author David Alain do Nascimento (dan@cin.ufpe.br)
 *
 */
public class VirtualLinkEntry {

	public final String name;
	public final List<Pair<InterfaceInfoEntry,InterfaceInfoEntry>> route;
	
	public VirtualLinkEntry(String name, List<Pair<InterfaceInfoEntry,InterfaceInfoEntry>> routeStr) {
		super();
		this.name = name;
		this.route = routeStr;
	}
	
	public static final VirtualLinkEntry parse(String entryFromFile) {
		
		Objects.requireNonNull(entryFromFile, "entryFromFile cannot be null");

		//vl1 : ES1.0,SW1.0 ; SW1.1,SW2.0 ; SW2.1,ES4.0 ; 

		final String[] items = entryFromFile.replaceAll("\\s+","").split(":");

		final String name = items[0];
		final String[] linksStr = items[1].split(";");

		final List<Pair<InterfaceInfoEntry,InterfaceInfoEntry>> pairList = new LinkedList<Pair<InterfaceInfoEntry,InterfaceInfoEntry>>();

		for(String linkStr : linksStr) {
			
			final String[] interfacesStr = linkStr.split(",");

			final InterfaceInfoEntry source = InterfaceInfoEntry.parse(interfacesStr[0]);
			final InterfaceInfoEntry destination = InterfaceInfoEntry.parse(interfacesStr[1]);
			final Pair<InterfaceInfoEntry,InterfaceInfoEntry> pair = new Pair<InterfaceInfoEntry,InterfaceInfoEntry>(source, destination);

			pairList.add(pair);
		}

		return new VirtualLinkEntry(name, pairList);
	}
	
	public InterfaceInfoEntry getSource() {
		return route.get(0).getFirst();
	}
	
	public InterfaceInfoEntry getDestination() {
		return route.get(route.size() - 1).getSecond();
	}

	@Override
	public int hashCode() {
		return Objects.hash(route, name);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		VirtualLinkEntry other = (VirtualLinkEntry) obj;
		return Objects.equals(route, other.route) && Objects.equals(name, other.name);
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder("\n");
		
		route.forEach(pair -> { sb.append(pair.getFirst().toStringInfo() + " -> " + pair.getSecond().toStringInfo() + "\n"); });
		
		return "VirtualLinkEntry [name=" + name + ", route=" + sb.toString() + "]";
	}

	
	
	
}
