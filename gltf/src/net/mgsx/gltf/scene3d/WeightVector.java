package net.mgsx.gltf.scene3d;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class WeightVector {

	public int count;
	public float [] values;

	public WeightVector() {
		this(0, 8);
	}
	
	public WeightVector(int count) {
		this(count, 8);
	}

	public WeightVector(int count, int max) {
		this.count = count;
		values = new float[max];
	}

	public WeightVector set(WeightVector weights) {
		if(weights.count > values.length) throw new GdxRuntimeException("WeightVector out of bound");
		this.count = weights.count;
		for(int i=0 ; i<count ; i++){
			values[i] = weights.values[i];
		}
		return this;
	}

	public void lerp(WeightVector value, float t) {
		if(count != value.count) throw new GdxRuntimeException("WeightVector count mismatch");
		for(int i=0 ; i<count ; i++){
			values[i] = MathUtils.lerp(values[i], value.values[i], t);
		}
	}
	
	@Override
	public String toString() {
		String s = "WeightVector(";
		for(int i=0 ; i<count ; i++){
			if(i > 0) s += ", ";
			s += values[i];
		}
		return s + ")";
	}

	public WeightVector set() {
		this.count = 0;
		return this;
	}

	public WeightVector cpy() {
		return new WeightVector(count).set(this);
	}

	public float get(int index) {
		return values[index];
	}

}
