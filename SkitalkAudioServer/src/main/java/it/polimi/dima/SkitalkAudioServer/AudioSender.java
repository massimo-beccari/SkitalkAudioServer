package it.polimi.dima.SkitalkAudioServer;

import java.net.*;
import java.io.*;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.TargetDataLine;

public class AudioSender extends Thread
{
	private static final int INTERNAL_BUFFER_SIZE = 40960;  // Dimensione buffer per l'invio dei dati
	private TargetDataLine m_targetLine;                    // Linea per la cattura dell'audio
	private Socket m_callSocket;                            // Socket di connessione al ricevente (server)
	private AudioFormat m_audioFormat;                      // Formato dell'audio
	private DataOutputStream m_dataOutputStream;            // Outputstream verso il ricevente (server)
	private boolean m_bRecording;                           // Usato per interrompere il thread principale
	
	public AudioSender() 
	{
		m_targetLine = null;
		m_callSocket = null;
		m_dataOutputStream = null;
		m_audioFormat = null;
	}
	
	/*
	 * --------------------
	 * setupFormat()
	 * --------------------
	 * Imposta il formato dell'audio
	 */
	public void setupFormat()
	{
		// Imposta il formato dell'audio
		m_audioFormat = new AudioFormat(AudioConstants.ENCODING,
												AudioConstants.SAMPLERATE, 
												AudioConstants.SAMPLESIZEINBITS, 
												AudioConstants.CHANNELS, 
												AudioConstants.FRAMESIZE, 
												AudioConstants.FRAMERATE, 
												AudioConstants.ISBIGENDIAN);
	}
	
	/*
	 * --------------------
	 * createLine()
	 * --------------------
	 * Crea la linea per la cattura dell'audio
	 */
	public void createLine() throws LineUnavailableException
	{
		DataLine.Info targetInfo = new DataLine.Info(TargetDataLine.class, 
		  m_audioFormat, 
		  INTERNAL_BUFFER_SIZE);		  
		m_targetLine = (TargetDataLine) AudioSystem.getLine( targetInfo );
		m_targetLine.open( m_audioFormat, INTERNAL_BUFFER_SIZE );	
	}
	
	/*
	 * --------------------
	 * establishConnection()
	 * --------------------
	 * Stabilisce una connessione con il ricevente (server)
	 * e prepara uno stream per l'output
	 */
	public void establishConnection( String serverip )
	{
    try {
        m_callSocket = new Socket(serverip, 4444);
    } catch (UnknownHostException e) {
        System.err.println("Ip sconosciuto");
        System.exit(1);
    } catch (IOException e) {
        System.err.println("Impossibile connttersi a " + serverip);
        System.exit(1);
    }       

   	try {
			m_dataOutputStream = new DataOutputStream(m_callSocket.getOutputStream());
		} catch (IOException e) {
			System.err.println("Couldn't get output stream.");
			System.exit(1);
		}
	}
	
	/*
	 * --------------------
	 * start()
	 * --------------------
	 * Inizia la cattura dell'audio
	 */
	public void start()
	{
		if (m_targetLine != null)
		{
			m_targetLine.start();
			super.start();
		}
	}
	
	/*
	 * --------------------
	 * run()
	 * --------------------
	 * Thread per la cattura e l'invio dei dati audio
	 */
	public void run()
	{
		if (m_dataOutputStream == null || m_targetLine == null)
		{
			System.err.println("socket o linea non inizializzate");
			System.exit(1);
		}
		
		byte[]	abBuffer = new byte[INTERNAL_BUFFER_SIZE];
		int	nBufferSize = abBuffer.length;
		m_bRecording = true;
		
		try
		{
			while (m_bRecording)
			{
        // Cattura un blocco di audio e lo inserisce nel buffer
				int	nBytesRead = m_targetLine.read(abBuffer, 0, nBufferSize);
				// Manda i dati al ricevente
				m_dataOutputStream.write(abBuffer, 0, nBytesRead);				
			}
		
		  // Chiude linea e stream
			m_targetLine.flush();
			m_targetLine.close();
			m_dataOutputStream.close();
			m_callSocket.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
	}
	
	/*
	 * --------------------
	 * stopLine()
	 * --------------------
	 * Ferma la cattura dell'audio
	 */
	public void stopLine()
	{
		m_bRecording = false;
	}
	
	/*
	 * --------------------
	 * main()
	 * --------------------
	 * Entry point
	 */
  public static void main(String[] args) throws IOException 
  {
  	if (args.length != 1)
  	{
  		printUsageAndExit();
  	}    
  	
  	String	serverip = args[0];
    	
  	AudioSender audioSender = new AudioSender();
  	
  	System.out.println("Imposta il formato dell'audio...");
    audioSender.setupFormat();
    
  	System.out.println("Connessione server...");
  	audioSender.establishConnection(serverip);    
    
    System.out.println("Apre la linea audio...");
  	try 
  	{
  		audioSender.createLine();
  	}
  	catch (LineUnavailableException e)
  	{
  		e.printStackTrace();
  		System.exit(1);
  	}
  	
  	System.out.println("Inizio streaming...");    	
   	audioSender.start();
    	
  	System.out.println("Premi ENTER per interrompere lo streaming.");
  	try
  	{
  		System.in.read();
  	}
  	catch (IOException e)
  	{
  		e.printStackTrace();
  	}
  	
  	System.out.println("Chiude la linea.");
  	audioSender.stopLine();
  }
   	
  /*
   * --------------------
   * printUsageAndExit()
   * --------------------
   * Visualizza uso dei parametri
   */
	private static void printUsageAndExit()
	{
		System.out.println("AudioSender - uso:");
		System.out.println("\tjava AudioSender <ip del server>");
		System.exit(1);
	}
 	
}