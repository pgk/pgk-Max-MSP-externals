/**
 * refer to README for copyright etc...
 */

package com.pgk.max.midi;


import java.util.*;

import com.cycling74.max.Atom;

/**
 * Singleton class that contains all the midi mappings made by
 * it's client CCMidiLearn objects
 * 
 * @author panos kountanis
 *
 */
public class GlobalMapState {
	
	/** control change mappings */
	Map<Integer, Set<String>> ccMap;
	Map<String, Set<Integer>> objectNamesToControls;
	
	/** note mappings */
	Map<Integer, Set<String>> noteMap;
	Map<String, Set<Integer>> objectNamesToNotes;
	
	/** client mappings */
	Map<MidiLearnAdapter, Set<String>> clientsObjects;
	Map<String, Set<MidiLearnAdapter>> objectClients;
	
	/**
	 * zero argument constructor
	 */
	private GlobalMapState() {
		ccMap = new HashMap<Integer, Set<String>>();
		noteMap = new HashMap<Integer, Set<String>>();
		objectNamesToControls = new HashMap<String, Set<Integer>>();
		objectNamesToNotes = new HashMap<String, Set<Integer>>();
		clientsObjects = new HashMap<MidiLearnAdapter, Set<String>>();
		objectClients = new HashMap<String, Set<MidiLearnAdapter>>();
		//
		initMidiMaps();
	}
	
	/**
	 * get the single instance possible from this class
	 * @return the instance
	 */
	public static GlobalMapState getInstance() {
		return GlobalMapStateHolder.INSTANCE;
	}
	
	/** 
	 * @param i
	 * @param s
	 */
	public synchronized void removeControlMapping(int i, String s) {
		doUnmapChannelFromName(ccMap, i, s);
		doUnmapNameFromChannel(objectNamesToControls, s, i);
	}
	
	public synchronized String removeAllControlMappingsFromObject(String name) {
		//access object map to get the keys of the ccmap
		Set<Integer> mappedChannels = objectNamesToControls.get(name);
		for ( Integer channel : mappedChannels) {
			ccMap.get(channel).remove(name);
		}
		
		objectNamesToControls.get(name).clear(); //name is now mapped to the empty set
		
		return name;
	}
	
	public synchronized int removeAllObjectMappingsFromControl(int channel) {
		//access ccmap to get the keys to access object map
		Set<String> mappedNames = ccMap.get(channel);
		for ( String name : mappedNames) {
			objectNamesToControls.get(name).remove(channel);
		}
		
		ccMap.get(channel).clear();
		
		return channel;
		
	}
	
	public Atom[] getControlMapAsArray() {
		Atom[] atoms = new Atom[1];
		for (Integer key : ccMap.keySet()) {
			Set<String> mapped = ccMap.get(key);
			Atom[] list = new Atom[mapped.size() + 1];
			int i = 0;
			list[i] = Atom.newAtom(key); i++;
			for (String s : mapped) {
				list[i] = Atom.newAtom(s); i++;
			}
				
		}
		return atoms;
	}
	/**
	 * Map a channel to a named object
	 * @param i channel number
	 * @param s the name of the object to map
	 */
	public synchronized void doCCMap(MidiLearnAdapter client, int i, String s) {
		
		doMapChannelToName( ccMap, i, s );
		doMapNameToChannel( objectNamesToControls, s, i );
		doMapClientToName( client, s );
		
	}
	
	

	public synchronized void clearControl() {
		for (Integer key : ccMap.keySet()) {
			ccMap.get(key).clear();
		}
		for (String name : objectNamesToControls.keySet()) {
			objectNamesToControls.get(name).clear();
		}
	}
	
	
	
	//PRIVATE METHODS:
	private final void initMidiMaps() {
		for (int i = 0; i < 128; i++) {
			ccMap.put(i, new HashSet<String>());
			noteMap.put(i, new HashSet<String>());
		}
	}
	
	final boolean hasBeenMapped(int key) {
		return ! ccMap.get(key).isEmpty();
	}
	
	/**
	  * MidiMapStateHolder is loaded on the first execution of MidiMapState.getInstance() 
	  * or the first access to MidiMapStateHolder.INSTANCE, not before.
	*/
	private static class GlobalMapStateHolder { 
		public static final GlobalMapState INSTANCE = new GlobalMapState();
	}
	
	//Mapping helpers
	private void doMapNameToChannel(Map<String, Set<Integer>> map, String name, int channel) {
		if (!map.containsKey(name)) {
			map.put(name, new HashSet<Integer>());
		}
		Set<Integer> tmp = map.get(name);
		tmp.add(channel);
		map.put(name, tmp);
	}
	
	private void doMapChannelToName(Map<Integer, Set<String>> map, int i, String s) {
		//will already contain a list from construction
		Set<String> l = map.get(i);
		l.add(s);
		map.put(i, l);
	}
	
	private void doMapClientToName(MidiLearnAdapter client, String s) {
		// getClient 
		if (!clientsObjects.containsKey(client)) {
			clientsObjects.put(client, new HashSet<String>());
		}
		if (!objectClients.containsKey(s)) {
			objectClients.put(s, new HashSet<MidiLearnAdapter>());
		}
		Set<String> tmp = clientsObjects.get(client);
		Set<MidiLearnAdapter> tmp2 = objectClients.get(s);
		tmp.add(s);
		tmp2.add(client);
		clientsObjects.put(client, tmp);
		objectClients.put(s, tmp2);
	}
	
	//Unmapping helpers:
	private void doUnmapNameFromChannel( Map<String, Set<Integer>> map, String name, int channel ) {
		//remove the channel from the mapping related to name:
		if (map.containsKey(name)) {
			map.get(name).remove(channel);
		}
		//also remove name:
		if (map.get(name).isEmpty()) {
			map.remove(name);
		}
		
	}
	
	private void doUnmapChannelFromName( Map<Integer, Set<String>> map, int channel, String name ) {
		if (map.containsKey(channel)) {
			map.get(channel).remove(name);
		}
	}

	void notifyDeleted(MidiLearnAdapter midiLearnAdapter) {
		// TODO Auto-generated method stub
		doDeleteAdapterAndRelatedObjects(midiLearnAdapter);
		
	}

	private void doDeleteAdapterAndRelatedObjects( MidiLearnAdapter midiLearnAdapter)  {
		// get all the objects that were on the patch of this adapter
		Set<String> patcherObjects = this.clientsObjects.get(midiLearnAdapter);
		//pacherObjects could be null:
		if (patcherObjects != null) {
			for (String object : patcherObjects) {
				//remove the client from each string object:
				objectClients.get(object).remove(midiLearnAdapter);
				//unmap all mapped items
				Set<Integer> controlNums = this.objectNamesToControls.get(object);
				if (controlNums != null && !controlNums.isEmpty()) {
					for (Integer control : controlNums) {
						removeControlMapping(control, object);
					}
				}
				
				//TODO add note check
				
				//remove control from name
				//if it was the only one, remove the entry and any mapping as well:
				if (objectClients.get(object).isEmpty()) {
					
					//remove object from list
					objectClients.remove(object);
				}
				
				//finally, remove adapter and contents
				clientsObjects.remove(midiLearnAdapter);
			}
		}
			
	}

}
