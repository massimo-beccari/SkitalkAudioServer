package it.polimi.dima.SkitalkAudioServer.model;

import it.polimi.dima.SkitalkAudioServer.model.commIn.ClientHandlerIn;
import it.polimi.dima.SkitalkAudioServer.model.commOut.ClientHandlerOut;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class HandlersList {
	private ArrayList<ClientHandlerOut> listOut;
	private ArrayList<ClientHandlerIn> listIn;
	private Map<Integer, ClientHandlerOut> mapOut;
	
	public HandlersList() {
		listOut = new ArrayList<ClientHandlerOut>();
		listIn = new ArrayList<ClientHandlerIn>();
		mapOut = new HashMap<Integer, ClientHandlerOut>();
	}
	
	public synchronized void addHandlerIn(ClientHandlerIn handler) {
		listIn.add(handler);
	}
	
	public synchronized void removeHandlerIn(ClientHandlerIn handler) {
		listIn.remove(handler);
	}
	
	public synchronized void addHandlerOut(ClientHandlerOut handler) {
		listOut.add(handler);
		mapOut.put(handler.getClientId(), handler);
	}
	
	public synchronized void removeHandlerOut(ClientHandlerOut handler) {
		listOut.remove(handler);
	}
	
	public synchronized ClientHandlerOut getHandlerOutById(int userId) {
		return mapOut.get(userId);
	}
	
	public synchronized Set<Integer> getOutUserIds() {
		Set<Integer> result = new HashSet<Integer>();
		for(ClientHandlerOut cho : listOut)
			result.add(cho.getClientId());
		return result;
	}
}
