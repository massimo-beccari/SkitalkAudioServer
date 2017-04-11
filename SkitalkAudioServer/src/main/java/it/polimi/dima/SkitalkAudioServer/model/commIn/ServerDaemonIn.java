package it.polimi.dima.SkitalkAudioServer.model.commIn;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.polimi.dima.SkitalkAudioServer.Constants;
import it.polimi.dima.SkitalkAudioServer.model.HandlersList;

public class ServerDaemonIn implements Runnable {
	private ServerSocket serverSocket;
	private HandlersList list;
	private Map<Integer, Integer> activeMap;
	private Map<Integer, Integer> groupCommunciationsMap;
	
	public ServerDaemonIn(HandlersList list, Map<Integer, Integer> activeMap, Map<Integer, Integer> groupCommunciationsMap) {
		try {
			System.out.println("SDI: server daemon in launched. Opening server socket in...");
			serverSocket = new ServerSocket(Constants.SERVER_PORT_IN);
		} catch (IOException e) {
			System.err.println("SDI: Error opening server socket.");
			e.printStackTrace();
		}
		this.list = list;
		this.activeMap = activeMap;
		this.groupCommunciationsMap = groupCommunciationsMap;
	}

	public void run() {
		int n = 0;
		ExecutorService executor = Executors.newCachedThreadPool();
		while(true) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
			} catch(SocketTimeoutException s) {
				
			} catch(IOException e) {
				
			}
			if(socket != null) {
				n++;
				System.out.println("SDI: Client connection received at daemon in. nIN = "+n);
				ClientHandlerIn chi = new ClientHandlerIn(socket, list, activeMap, groupCommunciationsMap);
				executor.submit(chi);
			}
		}
	}
	
	

}
