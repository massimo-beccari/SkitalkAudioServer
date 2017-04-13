package it.polimi.dima.SkitalkAudioServer;

import it.polimi.dima.SkitalkAudioServer.model.ActiveMapUpdater;
import it.polimi.dima.SkitalkAudioServer.model.HandlersList;
import it.polimi.dima.SkitalkAudioServer.model.commIn.ServerDaemonIn;
import it.polimi.dima.SkitalkAudioServer.model.commOut.ClientHandlerOut;
import it.polimi.dima.SkitalkAudioServer.model.commOut.ServerDaemonOut;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Main {

	public static void main(String[] args) {
		ExecutorService executor = Executors.newCachedThreadPool();
		//instantiate shared data structures
		HandlersList handlersList = new HandlersList();
		Map<Integer, Integer> activeMap = new HashMap<Integer, Integer>();
		Map<Integer, Integer> groupCommunciationsMap = new HashMap<Integer, Integer>();
		//run active map updater
		ActiveMapUpdater activeMapUpdater = new ActiveMapUpdater(handlersList, activeMap);
		executor.submit(activeMapUpdater);
		//run server daemon out
		ServerDaemonOut serverDaemonOut = new ServerDaemonOut(handlersList);
		executor.submit(serverDaemonOut);
		//run server daemon in
		ServerDaemonIn serverDaemonIn = new ServerDaemonIn(handlersList, activeMap, groupCommunciationsMap);
		executor.submit(serverDaemonIn);
		//run handlers out garbage collector
		Main.HandlersOutGarbageCollector hogc = new Main.HandlersOutGarbageCollector(handlersList);
		executor.submit(hogc);
	}

	private static class HandlersOutGarbageCollector extends Thread {
		private HandlersList list;
		
		public HandlersOutGarbageCollector(HandlersList list) {
			this.list = list;
		}
		
		@Override
		public void run() {
			System.out.println("HOGB: garbage collector launched.");
			while(true) {
				Set<Integer> ids = list.getOutUserIds();
				Iterator<Integer> it = ids.iterator();
				for(int id; it.hasNext(); ) {
					id = it.next();
					ClientHandlerOut cho = list.getHandlerOutById(id);
					cho.updateState();
				}
				System.out.println("HOGB: handlers terminated collected.");
				try {
					Thread.sleep(Constants.CLIENT_HANDLERS_OUT_GARBAGE_COLLECTOR_INTERVAL*1000);
				} catch (InterruptedException e) {
					System.err.println("Failed for garbage collector to wait. Thread interrupted.");
					e.printStackTrace();
				}
			}
		}
	}
}
