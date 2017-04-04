package it.polimi.dima.SkitalkAudioServer.model;

import it.polimi.dima.SkitalkAudioServer.AudioConstants;
import it.polimi.dima.SkitalkAudioServer.model.commOut.ClientHandlerOut;

import java.io.DataInputStream;
import java.io.IOException;
import java.util.ArrayList;

public class StreamForwarder extends Thread {
	private DataInputStream stream;
	private HandlersList list;
	private int groupId;
	
	public StreamForwarder(DataInputStream stream, HandlersList list, int groupId) {
		this.stream = stream;
		this.list = list;
		this.groupId = groupId;
	}
	
	@Override
	public void run() {
		ArrayList<ClientHandlerOut> myList = new ArrayList<ClientHandlerOut>(list.getClientsOut());
		ArrayList<ClientHandlerOut> myGroupMates = findMyGroupClients(myList);
		
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
						f.start();
					}
				}
				stream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private ArrayList<ClientHandlerOut> findMyGroupClients(ArrayList<ClientHandlerOut> myList) {
		ArrayList<ClientHandlerOut> result = new ArrayList<ClientHandlerOut>();
		for(ClientHandlerOut cho : myList)
			if(cho.getGroupId() == groupId)
				result.add(cho);
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
