package it.polimi.dima.SkitalkAudioServer.model.commIn;

import it.polimi.dima.SkitalkAudioServer.Constants;
import it.polimi.dima.SkitalkAudioServer.model.StreamForwarder;
import it.polimi.dima.SkitalkAudioServer.model.HandlersList;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

public class ClientHandlerIn implements Runnable {
	private Socket socket;
	private DataInputStream socketIn;
	private Scanner userInfoIn;
	private HandlersList list;
	private Map<Integer, Integer> activeMap;
	private Map<Integer, Integer> groupCommunciationsMap;
	private int userId;
	private int groupId;
	
	public ClientHandlerIn(Socket socket, HandlersList list, Map<Integer, Integer> activeMap, Map<Integer, Integer> groupCommunciationsMap) {
		this.socket = socket;
		this.list = list;
		this.activeMap = activeMap;
		this.groupCommunciationsMap = groupCommunciationsMap;
	}

	public void run() {
		try {
			initializeConnection(socket);
		} catch (IOException e) {
			System.err.println("Error opening socket streams.");
			e.printStackTrace();
		}
	}

	private void initializeConnection(Socket socket) throws IOException {
		userInfoIn = new Scanner(socket.getInputStream());
		userId = Integer.parseInt(userInfoIn.nextLine());
		groupId = Integer.parseInt(userInfoIn.nextLine());
		userInfoIn.close();
		if(groupCommunciationsMap.get(groupId).equals(null) || groupCommunciationsMap.get(groupId) == Constants.NO_USER_ACTIVE_ON_GROUP) {
			synchronized(groupCommunciationsMap) {
				groupCommunciationsMap.put(groupId, userId);
			}
			list.getClientsIn().add(this);
			socketIn = new DataInputStream(socket.getInputStream());
			forwardAudioData();
		}
	}
	
	private void forwardAudioData() {
		StreamForwarder sender = new StreamForwarder(socketIn, list, groupId, activeMap);
		Thread t = new Thread(sender);
		t.start();
		try {
			t.join();
		} catch (InterruptedException e) {
			System.err.println("Failed to join StreamForwarder. The thread has been interrupted.");
			e.printStackTrace();
		}
		synchronized(groupCommunciationsMap) {
			groupCommunciationsMap.put(groupId, Constants.NO_USER_ACTIVE_ON_GROUP);
		}
	}
}
