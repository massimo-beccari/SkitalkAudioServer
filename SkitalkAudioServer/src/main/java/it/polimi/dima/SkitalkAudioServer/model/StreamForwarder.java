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

public class StreamForwarder {
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
	
	public void forwardStream() {
		ArrayList<ClientHandlerOut> myGroupMates = findMyGroupClients();
		
		if(!myGroupMates.isEmpty()) {
			//instantiate and set buffer for handlers out
			byte[] abBuffer = new byte[AudioConstants.INTERNAL_BUFFER_SIZE];
			for(ClientHandlerOut cho : myGroupMates)
				cho.setBuffer(abBuffer);
			
			int	nBufferSize = abBuffer.length;
			try {
				int	nBytesRead = 0;
				while (nBytesRead != -1) {
					nBytesRead = stream.read(abBuffer, 0, nBufferSize);
					//create a forwarder for each client
					for(ClientHandlerOut cho : myGroupMates) {
						cho.setnBytes(nBytesRead);
						cho.notify();
					}
				}
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		/*if(!myGroupMates.isEmpty()) {
			PipedInputStream last = new PipedInputStream();
			TeeInputStream[] newStreams = new TeeInputStream[myGroupMates.size() - 1];
			InputStream current = stream;
			for(int i = 0; i < myGroupMates.size() - 1; i++) {
				
			}
		}*/
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
}
