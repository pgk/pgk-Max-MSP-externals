/**
 * refer to README for copyright etc...
 */

package com.pgk.max.midi;


import java.util.*;
import com.cycling74.max.*;

/**
 * 
 * @author panoskountanis
 *
 */
public class CCMidiLearn extends MidiLearnAdapter {
	
	public CCMidiLearn() {
		super();
	}
	
	public void clear() {
		super.clear();
		globalMap.clearControl();
	}
	
	public void getmap() {
		
		for (Integer key : globalMap.ccMap.keySet()) {
			if (globalMap.hasBeenMapped(key)) {
				Set<String> mapped = globalMap.ccMap.get(key);
				Atom[] list = new Atom[mapped.size() + 1];
				int i = 0;
				list[i] = Atom.newAtom(key); i++;
				for (String s : mapped) {
					list[i] = Atom.newAtom(s); i++;
				}
				outlet(0, list);
			}		
		}
	}
	
	public void remove_mapping(int i, String s) {
		globalMap.removeControlMapping(i, s);
	}

	protected void doMap(int i) {
		// TODO Auto-generated method stub
		globalMap.doCCMap(this, i, tmp);
		tmp = null;	
	}



	protected void execute(int channel, int value) {
		
		Set<String> associatedObjects = globalMap.ccMap.get(channel);
		MaxBox obj = null;
		
		for (String objectName : associatedObjects) {
			if (globalMap.clientsObjects.get(this).contains(objectName)) {
				obj = patcher.getNamedBox(objectName);
				if (obj != null) {
					if (!obj.send(value)) {
						post("Message not sent to " + objectName);
					}
				}
			}	
		}	
	}

}
