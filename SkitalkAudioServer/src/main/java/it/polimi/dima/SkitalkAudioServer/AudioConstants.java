package it.polimi.dima.SkitalkAudioServer;

import javax.sound.sampled.AudioFormat;

public class AudioConstants {
	public static final AudioFormat.Encoding ENCODING = AudioFormat.Encoding.PCM_SIGNED;
	public static float SAMPLERATE = (float)11025.0;
	public static int SAMPLESIZEINBITS = 16;
	public static int CHANNELS = 2;
	public static int FRAMESIZE = 4;
	public static float FRAMERATE = (float)11025.0;
	public static boolean ISBIGENDIAN = false;
	public static final int INTERNAL_BUFFER_SIZE = 40960;
}