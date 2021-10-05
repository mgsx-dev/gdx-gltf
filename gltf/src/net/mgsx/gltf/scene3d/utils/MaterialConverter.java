package net.mgsx.gltf.scene3d.utils;

import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;

import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.scene.Scene;

/**
 * Utility to convert PBR materials to default materials in order to be used with DefaultShader.
 */
public class MaterialConverter {
	public static void makeCompatible(Scene scene){
		makeCompatible(scene.modelInstance.materials);
	}
	public static void makeCompatible(Iterable<Material> materials){
		for(Material m : materials){
			makeCompatible(m);
		}
	}
	public static void makeCompatible(Material material){
		PBRColorAttribute baseColor = material.get(PBRColorAttribute.class, PBRColorAttribute.BaseColorFactor);
		if(baseColor != null){
			material.set(ColorAttribute.createDiffuse(baseColor.color));
		}
	}
}
