package net.mgsx.gltf.scene3d.attributes;

import org.junit.Assert;
import org.junit.Test;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g3d.Attribute;

public class AttributesCompareTest {
	@Test
	public void testFogSame(){
		FogAttribute a = FogAttribute.createFog(0, 1, 2);
		Attribute b = a.copy();
		Assert.assertEquals(0, a.compareTo(b));
	}
	@Test
	public void testFogDifferent(){
		FogAttribute a = FogAttribute.createFog(0, 1, 2);
		FogAttribute b = (FogAttribute)a.copy();
		b.value.x = 4;
		Assert.assertNotEquals(0, a.compareTo(b));
	}
	
	@Test
	public void testIridescenceSame(){
		PBRIridescenceAttribute a = new PBRIridescenceAttribute(1, 1.5f, 100, 400);
		Attribute b = a.copy();
		Assert.assertEquals(0, a.compareTo(b));
	}
	@Test
	public void testIridescenceDifferent(){
		PBRIridescenceAttribute a = new PBRIridescenceAttribute(1, 1.5f, 100, 400);
		PBRIridescenceAttribute b = (PBRIridescenceAttribute)a.copy();
		b.factor = 4;
		Assert.assertNotEquals(0, a.compareTo(b));
	}

	@Test
	public void testVolumeSame(){
		PBRVolumeAttribute a = new PBRVolumeAttribute(1, 10, Color.WHITE);
		Attribute b = a.copy();
		Assert.assertEquals(0, a.compareTo(b));
	}
	@Test
	public void testVolumeDifferent(){
		PBRVolumeAttribute a = new PBRVolumeAttribute(1, 10, Color.WHITE);
		PBRVolumeAttribute b = (PBRVolumeAttribute)a.copy();
		b.thicknessFactor = .5f;
		Assert.assertNotEquals(0, a.compareTo(b));
	}
	
	@Test
	public void testTextureSame(){
		PBRTextureAttribute a = PBRTextureAttribute.createBaseColorTexture((Texture)null);
		a.offsetU = .2f;
		a.offsetV = .3f;
		a.scaleU = 2f;
		a.scaleV = 3f;
		a.rotationUV = 90f;
		Attribute b = a.copy();
		Assert.assertEquals(0, a.compareTo(b));
	}
	@Test
	public void testTextureDifferent(){
		PBRTextureAttribute a = PBRTextureAttribute.createBaseColorTexture((Texture)null);
		a.offsetU = .2f;
		a.offsetV = .3f;
		a.scaleU = 2f;
		a.scaleV = 3f;
		a.rotationUV = 90;
		PBRTextureAttribute b = (PBRTextureAttribute)a.copy();
		b.rotationUV = 180;
		Assert.assertNotEquals(0, a.compareTo(b));
	}
}
