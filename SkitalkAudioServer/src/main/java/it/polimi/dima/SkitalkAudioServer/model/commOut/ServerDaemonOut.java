package it.polimi.dima.SkitalkAudioServer.model.commOut;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import it.polimi.dima.SkitalkAudioServer.Constants;
import it.polimi.dima.SkitalkAudioServer.model.HandlersList;

public class ServerDaemonOut implements Runnable {
	private ServerSocket serverSocket;
	private HandlersList list;
	
	public ServerDaemonOut(HandlersList list) {
		try {
			System.out.println("SDO: server daemon out launched. Opening server socket out...");
			serverSocket = new ServerSocket(Constants.SERVER_PORT_OUT);
		} catch (IOException e) {
			System.err.println("SDO: Error opening server socket.");
			e.printStackTrace();
		}
		this.list = list;
	}

	public void run() {
		ExecutorService executor = Executors.newCachedThreadPool();
		int n = 0;
		while(true) {
			Socket socket = null;
			try {
				socket = serverSocket.accept();
			} catch(SocketTimeoutException s) {
				
			} catch(IOException e) {
				
			}
			if(socket != null) {
				n++;
				System.out.println("SDO: Client connection received at daemon out. nOUT = "+n);
				ClientHandlerOut cho = new ClientHandlerOut(socket, list);
				executor.submit(cho);
			}
		}
	}
	
	

}
