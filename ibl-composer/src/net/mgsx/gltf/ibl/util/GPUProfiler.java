package net.mgsx.gltf.ibl.util;

import org.lwjgl.opengl.GL15;
import org.lwjgl.opengl.GL33;

public class GPUProfiler {

	public static class Metric{
		private boolean enabled = false; // XXX gl error !?
		private final int glTarget;
		private int query;
		
		public Metric(int glTarget) {
			this.glTarget = glTarget;
			if(enabled) query = GL15.glGenQueries(); // XXX gl error !? 
		}
		public void begin(){
			if(enabled) GL15.glBeginQuery(glTarget, query);
		}
		public void end(){
			if(enabled) GL15.glEndQuery(glTarget);
		}
		public boolean ready(){
			return GL15.glGetQueryObjecti(query, GL15.GL_QUERY_RESULT_AVAILABLE) != 0;
		}
		public int get(){
			if(enabled) return GL15.glGetQueryObjecti(query, GL15.GL_QUERY_RESULT);
			return -1;
		}
	}
	
	public static final Metric samplesPassed = new Metric(GL15.GL_SAMPLES_PASSED);
	public static final Metric timeElapsed = new Metric(GL33.GL_TIME_ELAPSED);
	public static final Metric timestamp = new Metric(GL33.GL_TIMESTAMP);
}
