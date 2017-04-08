package it.polimi.dima.SkitalkAudioServer.model;

import it.polimi.dima.SkitalkAudioServer.AudioConstants;
import it.polimi.dima.SkitalkAudioServer.model.commOut.ClientHandlerOut;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class StreamForwarder extends Thread {
	private DataInputStream stream;
	private HandlersList list;
	private int groupId;
	private Map<Integer, Integer> activeMap;
	
	public StreamForwarder(DataInputStream stream, HandlersList list, int groupId, Map<Integer, Integer> activeMap) {
		this.stream = stream;
		this.list = list;
		this.groupId = groupId;
		this.activeMap = activeMap;
	}
	
	@Override
	public void run() {
		ArrayList<ClientHandlerOut> myGroupMates = findMyGroupClients();
		
		if(!myGroupMates.isEmpty()) {
			byte[] abBuffer = new byte[AudioConstants.INTERNAL_BUFFER_SIZE];
			int	nBufferSize = abBuffer.length;
			try {
				int	nBytesRead = 0;
				while (nBytesRead != -1) {
					nBytesRead = stream.read(abBuffer, 0, nBufferSize);
					//create a forwarder for each client
					for(ClientHandlerOut cho : myGroupMates) {
						Forwarder f = new Forwarder(abBuffer, nBytesRead, cho);
						Thread t = new Thread(f);
						t.start();
					}
				}
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private ArrayList<ClientHandlerOut> findMyGroupClients() {
		ArrayList<ClientHandlerOut> result = new ArrayList<ClientHandlerOut>();
		Map<Integer, Integer> activeMap;
		synchronized(this.activeMap) {
			activeMap = new HashMap<Integer, Integer>(this.activeMap);
		}
		Set<Integer> userIds = activeMap.keySet();
		Iterator<Integer> it = userIds.iterator();
		for(int userId; it.hasNext(); ) {
			userId = it.next();
			if(activeMap.get(userId) == groupId)
				result.add(list.getHandlerOutById(userId));
		}
		return result;
	}
	
	private class Forwarder extends Thread {
		private byte[] buffer;
		private int nBytes;
		private ClientHandlerOut myHandler;
		
		public Forwarder(byte[] buffer, int nBytes, ClientHandlerOut myHandler) {
			this.buffer = buffer;
			this.nBytes = nBytes;
			this.myHandler = myHandler;
		}
		
		@Override
		public void run() {
			myHandler.sendAudioData(buffer, nBytes);
		}
	}
}
