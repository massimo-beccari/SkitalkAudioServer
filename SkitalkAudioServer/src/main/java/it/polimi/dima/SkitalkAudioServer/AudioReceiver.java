package it.polimi.dima.SkitalkAudioServer;

import java.net.*;
import java.io.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

public class AudioReceiver {
	private static final int INTERNAL_BUFFER_SIZE = 128000;
	private ServerSocket m_serverSocket;
	private Socket m_clientSocket;
	private DataInputStream m_dataInputStream;
	private AudioFormat m_audioFormat;
	private SourceDataLine m_line;
	
	public AudioReceiver() {
		m_serverSocket = null;
		m_clientSocket = null;
		m_dataInputStream = null;
		m_audioFormat = null;
		m_line = null;
	}
	
	public void setupFormat() {
		m_audioFormat = new AudioFormat(AudioConstants.ENCODING,
				AudioConstants.SAMPLERATE,
				AudioConstants.SAMPLESIZEINBITS,
				AudioConstants.CHANNELS,
				AudioConstants.FRAMESIZE,
				AudioConstants.FRAMERATE, 
				AudioConstants.ISBIGENDIAN);
	}	


	public void acceptConnection()	{
		// Crea il socket...
		try {
			m_serverSocket = new ServerSocket(Constants.SERVER_PORT_OUT);
		} catch (IOException e) {
			System.err.println("Impossibile ascoltare sulla porta: "+Constants.SERVER_PORT_OUT+".");
			System.exit(1);
		}

		// In attesa della connessione...
		try {
			m_clientSocket = m_serverSocket.accept();
		} catch (IOException e) {
			System.err.println("Accept Fallito.");
			System.exit(1);
		}
	
		// Chiamata ricevuta
		try {
			m_dataInputStream = new DataInputStream( m_clientSocket.getInputStream() );
		} catch (IOException e) {
			System.err.println("Impossibile aprire input stream.");
			System.exit(1);
		}
	}

	public void createLine() throws LineUnavailableException {
		DataLine.Info	targetInfo = new DataLine.Info(SourceDataLine.class,
				m_audioFormat);
		m_line = (SourceDataLine) AudioSystem.getLine( targetInfo );
		m_line.open( m_audioFormat );
	}
	
	public void receiveAndPlayAudio() throws IOException {
		m_line.start();

		int	nBytesRead = 0;
		byte[]	abData = new byte[INTERNAL_BUFFER_SIZE];		
	
		while (nBytesRead != -1) {
			nBytesRead = m_dataInputStream.read(abData, 0, abData.length);
			if (nBytesRead >= 0) {
				m_line.write(abData, 0, nBytesRead);
			}
		}
		
		m_line.drain();
		m_line.close();
	
		m_clientSocket.close();
		m_serverSocket.close();
	}

	public static void main(String[] args) throws IOException  {
		AudioReceiver audioReceiver = new AudioReceiver();

		audioReceiver.setupFormat();

		System.out.println("In ascolto sulla porta 4444...");	
		audioReceiver.acceptConnection();
		System.out.println("Chiamata ricevuta...");	
	
		try {
			audioReceiver.createLine();
		} catch (LineUnavailableException e) {
			e.printStackTrace();
			System.exit(1);
		}
	
		System.out.println("Riproduzione audio...");	
		audioReceiver.receiveAndPlayAudio();
		System.out.println("Riproduzione terminata.");
	}
}

