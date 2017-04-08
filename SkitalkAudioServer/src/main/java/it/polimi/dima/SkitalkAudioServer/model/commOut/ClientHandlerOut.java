package it.polimi.dima.SkitalkAudioServer.model.commOut;

import it.polimi.dima.SkitalkAudioServer.model.HandlersList;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Scanner;

public class ClientHandlerOut implements Runnable {
	private Socket socket;
	private HandlersList list;
	private int clientId;
	private Scanner socketIn;
	private DataOutputStream socketOut;
	
	public ClientHandlerOut(Socket socket, HandlersList list) {
		this.socket = socket;
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
		socketIn = new Scanner(socket.getInputStream());
		clientId = Integer.parseInt(socketIn.nextLine());
		socketIn.close();
		list.addHandlerOut(this);
		socketOut = new DataOutputStream(socket.getOutputStream());
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
}
