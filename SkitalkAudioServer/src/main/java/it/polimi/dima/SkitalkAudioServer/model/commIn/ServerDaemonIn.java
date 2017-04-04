package it.polimi.dima.SkitalkAudioServer.model.commIn;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.polimi.dima.SkitalkAudioServer.Constants;
import it.polimi.dima.SkitalkAudioServer.model.HandlersList;
import it.polimi.dima.SkitalkAudioServer.model.commOut.ClientHandlerOut;

public class ServerDaemonIn implements Runnable {
	private ServerSocket serverSocket;
	private HandlersList list;
	private Map<InetAddress, ClientHandlerOut> map;
	
	public ServerDaemonIn(HandlersList list, Map<InetAddress, ClientHandlerOut> map) {
		try {
			serverSocket = new ServerSocket(Constants.SERVER_PORT_IN);
		} catch (IOException e) {
			System.err.println("Error opening server socket.");
			e.printStackTrace();
		}
		this.list = list;
		this.map = map;
	}

	public void run() {
		ExecutorService executor = Executors.newCachedThreadPool();
		while(true) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
			} catch(SocketTimeoutException s) {
				
			} catch(IOException e) {
				
			}
			if(socket != null) {
				//TODO check another client of same group active
				ClientHandlerIn chi = new ClientHandlerIn(socket, map, list);
				list.getClientsIn().add(chi);
				executor.submit(chi);
			}
		}
	}
	
	

}
