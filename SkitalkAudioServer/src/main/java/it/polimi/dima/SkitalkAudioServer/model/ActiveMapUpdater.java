package it.polimi.dima.SkitalkAudioServer.model;

import it.polimi.dima.SkitalkAudioServer.Constants;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.google.gson.JsonObject;

public class ActiveMapUpdater implements Runnable {
	private HandlersList list;
	private Map<Integer, Integer> activeMap;

	public ActiveMapUpdater(HandlersList list, Map<Integer, Integer> activeMap) {
		this.list = list;
		this.activeMap = activeMap;
	}

	public void run() {
		System.out.println("AMU: ActiveMapUpdater launched.");
		while(true) {
			updateMap();
			System.out.println("AMU: map updated. activeMap = "+activeMap.keySet());
			try {
				Thread.sleep(Constants.ACTIVE_GROUPS_MAP_UPDATE_INTERVAL*1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void updateMap() {
		Set<Integer> userIdsSet = list.getOutUserIds();
		int usersNumber = userIdsSet.size();
		HttpRequest[] requests = new HttpRequest[usersNumber];
		Thread[] threads = new Thread[usersNumber];
		int[] userIds = new int[usersNumber];
		//copy user ids from set to array
		int i = 0;
		for(int userId : userIdsSet) {
			userIds[i] = userId;
			i++;
		}
		//start server requests
		for(i = 0; i < usersNumber; i++) {
			requests[i] = new HttpRequest("http://skitalk.altervista.org/php/getActiveGroup.php", "idUser="+userIds[i]);
			threads[i] = new Thread(requests[i]);
			threads[i].start();
		}
		//create new map
		Map<Integer, Integer> newMap = new HashMap<Integer, Integer>();
		//wait for responses and update map
		for(i = 0; i < usersNumber; i++) {
			try {
				threads[i].join();
				JsonObject response = requests[i].getResponse();
				newMap.put(userIds[i], response.get("activeGroup").getAsInt());
			} catch (InterruptedException e) {
				System.err.println("AMU: ActiveMapUpdater thread interrupted. Failed to join it.");
				e.printStackTrace();
			}
		}
		//update map
		synchronized(activeMap) {
			activeMap.clear();
			activeMap.putAll(newMap);
		}
	}
}
