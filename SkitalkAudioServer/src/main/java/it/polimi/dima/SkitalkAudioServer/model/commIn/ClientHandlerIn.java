package it.polimi.dima.SkitalkAudioServer.model.commIn;

import it.polimi.dima.SkitalkAudioServer.model.StreamForwarder;
import it.polimi.dima.SkitalkAudioServer.model.HandlersList;
import it.polimi.dima.SkitalkAudioServer.model.commOut.ClientHandlerOut;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;

public class ClientHandlerIn implements Runnable {
	private Socket socket;
	private DataInputStream socketIn;
	private HandlersList list;
	private Map<InetAddress, ClientHandlerOut> map;
	
	public ClientHandlerIn(Socket socket, Map<InetAddress, ClientHandlerOut> map, HandlersList list) {
		this.socket = socket;
		this.map = map;
		this.list = list;
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
		socketIn = new DataInputStream(socket.getInputStream());
		forwardAudioData();
	}
	
	private void forwardAudioData() {
		ClientHandlerOut myHandler = map.get(socket.getInetAddress());
		int groupId = myHandler.getGroupId();
		StreamForwarder sender = new StreamForwarder(socketIn, list, groupId);
		sender.start();
	}
}
