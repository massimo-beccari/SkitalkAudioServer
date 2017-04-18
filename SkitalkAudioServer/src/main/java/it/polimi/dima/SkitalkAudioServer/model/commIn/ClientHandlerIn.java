package it.polimi.dima.SkitalkAudioServer.model.commIn;

import it.polimi.dima.SkitalkAudioServer.Constants;
import it.polimi.dima.SkitalkAudioServer.model.StreamForwarder;
import it.polimi.dima.SkitalkAudioServer.model.HandlersList;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;
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
			System.err.println("CHI-"+userId+": Error opening socket streams.");
			e.printStackTrace();
		}
	}

	private void initializeConnection(Socket socket) throws IOException {
		/*socketIn = new DataInputStream(socket.getInputStream());
		String userAndGroupIds = socketIn.readLine();
		userId = socketIn.readInt();
		groupId = socketIn.readInt();*/
		userInfoIn = new Scanner(socket.getInputStream());
		String userAndGroupIds = userInfoIn.nextLine();
		//userInfoIn.close();
		Scanner sc = new Scanner(userAndGroupIds);
		userId = Integer.parseInt(sc.next());
		groupId = Integer.parseInt(sc.next());
		sc.close();
		System.out.println("CHI-"+userId+": connection established.");
		PrintWriter socketOut = new PrintWriter(socket.getOutputStream());
		if(groupCommunciationsMap.get(groupId) == null || groupCommunciationsMap.get(groupId) == Constants.NO_USER_ACTIVE_ON_GROUP) {
			System.out.println("CHI-"+userId+": begin audio communication.");
			synchronized(groupCommunciationsMap) {
				groupCommunciationsMap.put(groupId, userId);
			}
			list.addHandlerIn(this);
			socketIn = new DataInputStream(socket.getInputStream());
			forwardAudioData();
		} else {
			System.out.println("CHI-"+userId+": failed to begin audio communication. Another user is already talking.");
			socketOut.println(Constants.CHANNEL_BUSY);
			socketOut.flush();
			list.removeHandlerIn(this);
		}
		userInfoIn.close();
		socketOut.close();
	}
	
	private void forwardAudioData() {
		StreamForwarder sender = new StreamForwarder(socket, socketIn, list, userId, groupId, activeMap);
		sender.forwardStream();
		synchronized(groupCommunciationsMap) {
			groupCommunciationsMap.put(groupId, Constants.NO_USER_ACTIVE_ON_GROUP);
		}
		list.removeHandlerIn(this);
		System.out.println("CHI-"+userId+": end audio communication.");
	}
}
