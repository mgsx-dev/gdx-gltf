package net.mgsx.gltf.scene3d.model;

public class CubicWeightVector extends WeightVector
{
	public final WeightVector tangentIn;
	public final WeightVector tangentOut;
	
	public CubicWeightVector(int count) {
		super(count);
		tangentIn = new WeightVector(count);
		tangentOut = new WeightVector(count);
	}

	
}
