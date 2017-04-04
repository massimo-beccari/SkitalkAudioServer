package it.polimi.dima.SkitalkAudioServer.model.commOut;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Map;
import java.util.Scanner;

public class ClientHandlerOut implements Runnable {
	private Socket socket;
	private int clientId;
	private int groupId;
	private Scanner socketIn;
	private DataOutputStream socketOut;
	private Map<InetAddress, ClientHandlerOut> map;
	
	public ClientHandlerOut(Socket socket, Map<InetAddress, ClientHandlerOut> map) {
		this.socket = socket;
		this.map = map;
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
		socketIn = new Scanner(socket.getInputStream());
		socketOut = new DataOutputStream(socket.getOutputStream());
		clientId = Integer.parseInt(socketIn.nextLine());
		groupId = Integer.parseInt(socketIn.nextLine());
		socketIn.close();
		map.put(socket.getInetAddress(), this);
	}
	
	public void sendAudioData(byte[] buffer, int nBytes) {
		try {
			socketOut.write(buffer, 0, nBytes);
			socketOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public int getClientId() {
		return clientId;
	}

	public int getGroupId() {
		return groupId;
	}
}
