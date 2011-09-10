package com.pgk.max.midi;

import com.cycling74.max.*;

public class MidiLearnAdapter extends MaxObject {
	
	protected MaxPatcher patcher;
	protected String tmp = null;
	protected GlobalMapState globalMap;
	
	public MidiLearnAdapter() {
		super();
		globalMap = GlobalMapState.getInstance();
		declareIO(1, 2);
		patcher = this.getParentPatcher();
	}
	
	/**
	 * Permitted lists:
	 * a list of [int int] on left inlet, channel, value
	 * a list of [map string, int] to map object name to controller chanel
	 * a list of [setname string] giving the currently selected controller
	 * a list of [setcc int] giving the active cc channel
	 */
	@Override
	public void list(Atom[] args) {
		if ( args.length >= 2)
			handleInlet0list( args );		
	}
	
	@Override
	protected void notifyDeleted() {
		//if this object is deleted, notify the global map state
		post("Deleting the client object with id: " + this.toString());
		globalMap.notifyDeleted(this);	
	}
	
	public void remove_mapping(int i, String s) {
	}
	
	/**
	 * adapter method to be overridden by subclasses
	 * @param i
	 */
	protected void doMap(int i) {
	}
	
	public void setname(String name) {
		tmp = name;
		outlet(1,tmp);
	}
	
	protected void handleInlet0list(Atom[] args) {
			int channel = args[0].getInt();
			int value = args[1].getInt();
			if (tmp != null) {
				doMap(channel);
				execute(channel, value);
				
			} else {
				execute(channel, value);
			}
	}
	
	public void clear() {
		tmp = null;
	}
	
	protected void execute(int channel, int value) {
		
	}
}
