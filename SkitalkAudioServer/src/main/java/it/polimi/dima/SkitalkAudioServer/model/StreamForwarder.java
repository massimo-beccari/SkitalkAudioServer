package it.polimi.dima.SkitalkAudioServer.model;

import it.polimi.dima.SkitalkAudioServer.AudioConstants;
import it.polimi.dima.SkitalkAudioServer.model.commOut.ClientHandlerOut;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

public class StreamForwarder {
	private DataInputStream stream;
	private HandlersList list;
	private int userId;
	private int groupId;
	private Map<Integer, Integer> activeMap;
	
	public StreamForwarder(DataInputStream stream, HandlersList list, int userId, int groupId, Map<Integer, Integer> activeMap) {
		this.stream = stream;
		this.list = list;
		this.userId = userId;
		this.groupId = groupId;
		this.activeMap = activeMap;
	}
	
	public void forwardStream() {
		ArrayList<ClientHandlerOut> myGroupMates = findMyGroupClients();
		System.out.println("SF: stream forwarder launched. Group: "+myGroupMates.toString());
		if(!myGroupMates.isEmpty()) {
			//instantiate and set buffer for handlers out
			byte[] abBuffer = new byte[AudioConstants.INTERNAL_BUFFER_SIZE];			
			int	nBufferSize = abBuffer.length;
			Thread[] apsThreads = new Thread[myGroupMates.size()];
			try {
				int	nBytesRead = 0;
				nBytesRead = stream.read(abBuffer, 0, nBufferSize);
				while (nBytesRead != -1) {
					//create a forwarder for each client
					int i = 0;
					for(ClientHandlerOut cho : myGroupMates) {
						AudioPacketSender aps = new AudioPacketSender(cho.getStream(), abBuffer.clone(), nBytesRead);
						apsThreads[i] = new Thread(aps);
						apsThreads[i].start();
						i++;
					}
					for(Thread t : apsThreads)
						try {
							t.join();
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
					System.out.println("SF: "+nBytesRead+" bytes read from the stream and forwarded to client handlers.");
					nBytesRead = stream.read(abBuffer, 0, nBufferSize);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		try {
			stream.close();
		} catch (IOException e) {
			e.printStackTrace();
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
			if(this.userId != userId && activeMap.get(userId) == groupId)
				result.add(list.getHandlerOutById(userId));
		}
		return result;
	}
	
	private class AudioPacketSender extends Thread {
		private byte[] packet;
		private DataOutputStream socketOut;
		private int nBytes;
		
		public AudioPacketSender(DataOutputStream socketOut, byte[] packet, int nBytes) {
			this.socketOut = socketOut;
			this.packet = packet;
			this.nBytes = nBytes;
		}
		
		@Override
		public void run() {
			sendAudioData();
		}
		
		private void sendAudioData() {
			try {
				socketOut.write(packet, 0, nBytes);
				socketOut.flush();
			} catch (IOException e) {
				System.out.println("APS: failed to deliver audio packet. Client has closed the connection.");
				//e.printStackTrace();
			}
		}
	}
}
