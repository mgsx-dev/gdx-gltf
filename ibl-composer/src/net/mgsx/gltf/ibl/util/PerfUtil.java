package net.mgsx.gltf.ibl.util;

import org.lwjgl.opengl.GL43;
import org.lwjgl.opengl.KHRDebugCallback;

import com.badlogic.gdx.Gdx;

public class PerfUtil {
	public static long milliseconds(Runnable runnable){
		GPUProfiler.timeElapsed.begin();
		long ptime = System.nanoTime();
		runnable.run();
		long ctime = System.nanoTime(); // currentTimeMillis();
		
		int n = -1;
		
		while(!GPUProfiler.samplesPassed.ready()) Thread.yield();
		int samples = GPUProfiler.samplesPassed.get();
		
		System.out.println("" + (ctime - ptime) + " |||| " + (n / 1000000) + " ### " + samples);
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
	
	
	public static void enableGPUStats(){
		Gdx.gl.glEnable(GL43.GL_DEBUG_OUTPUT);
		GL43.glDebugMessageCallback(new KHRDebugCallback(new KHRDebugCallback.Handler() {
			@Override
			public void handleMessage(int source, int type, int id, int severity, String message) {
				
			}
		}));
		// TODO GL43.glGetDebugMessageLog(count, sources, types, ids, severities, lengths, messageLog)
	}
}
