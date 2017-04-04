package it.polimi.dima.SkitalkAudioServer.model;

import it.polimi.dima.SkitalkAudioServer.model.commIn.ClientHandlerIn;
import it.polimi.dima.SkitalkAudioServer.model.commOut.ClientHandlerOut;

import java.util.ArrayList;

public class HandlersList {
	private ArrayList<ClientHandlerOut> listOut;
	private ArrayList<ClientHandlerIn> listIn;
	
	public HandlersList() {
		listOut = new ArrayList<ClientHandlerOut>();
		listIn = new ArrayList<ClientHandlerIn>();
	}

	public synchronized ArrayList<ClientHandlerOut> getClientsOut() {
		return listOut;
	}
	
	public synchronized ArrayList<ClientHandlerIn> getClientsIn() {
		return listIn;
	}
}
