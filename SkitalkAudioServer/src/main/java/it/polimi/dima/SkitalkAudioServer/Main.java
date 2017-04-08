package it.polimi.dima.SkitalkAudioServer;

import it.polimi.dima.SkitalkAudioServer.model.ActiveMapUpdater;
import it.polimi.dima.SkitalkAudioServer.model.HandlersList;
import it.polimi.dima.SkitalkAudioServer.model.commIn.ServerDaemonIn;
import it.polimi.dima.SkitalkAudioServer.model.commOut.ServerDaemonOut;

import java.util.Map;
import java.util.HashMap;
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
	}

}
