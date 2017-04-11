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
	private byte[] buffer;
	private int nBytes;

	public ClientHandlerOut(Socket socket, HandlersList list) {
		this.socket = socket;
		this.list = list;
	}

	public void run() {
		try {
			initializeConnection(socket);
			System.out.println("CHO"+clientId+": connection established.");
		} catch (IOException e) {
			System.err.println("CHO"+clientId+": Error opening socket output streams.");
			e.printStackTrace();
		}
		while(socket.isConnected())
			synchronized(this) {
				try {
					this.wait();
					System.out.println("CHO"+clientId+": sending audio data...");
					sendAudioData();
				} catch (InterruptedException e) {
					System.err.println("CHO"+clientId+": Failed for handler out to wait. Thread interrupted.");
					e.printStackTrace();
				}
			}
		list.removeHandlerOut(this);
		System.out.println("CHO"+clientId+": end of connection. Handler removed.");
	}

	private void initializeConnection(Socket socket) throws IOException {
		socketIn = new Scanner(socket.getInputStream());
		clientId = Integer.parseInt(socketIn.nextLine());
		socketIn.close();
		list.addHandlerOut(this);
		socketOut = new DataOutputStream(socket.getOutputStream());
	}
	
	private void sendAudioData() {
		try {
			socketOut.write(buffer, 0, nBytes);
			socketOut.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public boolean isConnected() {
		return !socket.isClosed();
	}

	public int getClientId() {
		return clientId;
	}
	
	public void setBuffer(byte[] buffer) {
		this.buffer = buffer;
	}

	public void setnBytes(int nBytes) {
		this.nBytes = nBytes;
	}
}
