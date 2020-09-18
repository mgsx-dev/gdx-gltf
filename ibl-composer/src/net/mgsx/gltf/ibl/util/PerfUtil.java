package net.mgsx.gltf.ibl.util;

public class PerfUtil {
	public static long milliseconds(Runnable runnable){
		long ptime = System.nanoTime();
		runnable.run();
		long ctime = System.nanoTime();
		return (ctime - ptime) / 1000000L;
	}
	public static String millisecondsHuman(Runnable runnable) {
		return String.valueOf(milliseconds(runnable)) + "ms";
	}
	
	public static float seconds(Runnable runnable){
		return (float)milliseconds(runnable) / 1000f;
	}

	public static String secondsHuman(Runnable runnable) {
		return String.format("%.0f", seconds(runnable)) + "s";
	}
	
}
