package net.mgsx.gltf.scene3d.attributes;

import com.badlogic.gdx.graphics.g3d.Attribute;

import net.mgsx.gltf.scene3d.scene.CascadeShadowMap;

public class CascadeShadowMapAttribute extends Attribute
{
	public static final String Alias = "CSM";
	public static final long Type = register(Alias);
	
	public final CascadeShadowMap cascadeShadowMap;
	
	public CascadeShadowMapAttribute(CascadeShadowMap cascadeShadowMap) {
		super(Type);
		this.cascadeShadowMap = cascadeShadowMap;
	}
	@Override
	public int compareTo(Attribute o) {
		return (int)(type - o.type);
	}

	@Override
	public Attribute copy() {
		return new CascadeShadowMapAttribute(cascadeShadowMap);
	}

}
