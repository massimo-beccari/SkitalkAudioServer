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
	private DataOutputStream stream;
	private Scanner socketIn;

	public ClientHandlerOut(Socket socket, HandlersList list) {
		this.socket = socket;
		this.list = list;
	}

	public void run() {
		try {
			initializeConnection(socket);
			System.out.println("CHO-"+clientId+": connection established.");
		} catch (IOException e) {
			System.err.println("CHO-"+clientId+": Error opening socket output streams.");
			e.printStackTrace();
		}
	}

	private void initializeConnection(Socket socket) throws IOException {
		socketIn = new Scanner(socket.getInputStream());
		clientId = Integer.parseInt(socketIn.nextLine());
		stream = new DataOutputStream(socket.getOutputStream());
		list.addHandlerOut(this);
	}
	
	public void updateState() {
		if(socket.isClosed()) {
			list.removeHandlerOut(this);
			System.out.println("CHO-"+clientId+": end of connection. Handler removed.");
			socketIn.close();
		}
	}
	
	public boolean isConnected() {
		return !socket.isClosed();
	}

	public int getClientId() {
		return clientId;
	}

	public DataOutputStream getStream() {
		return stream;
	}
}
