package net.mgsx.gltf.scene3d.utils;

import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g3d.Material;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.attributes.FloatAttribute;

import net.mgsx.gltf.scene3d.attributes.PBRColorAttribute;
import net.mgsx.gltf.scene3d.attributes.PBRFloatAttribute;
import net.mgsx.gltf.scene3d.scene.Scene;

/**
 * Utility to convert PBR materials to default materials in order to be used with DefaultShader.
 * Some conversion are approximation because PBR and Gouraud lighting models are very different.
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
		PBRColorAttribute baseColorAttribute = material.get(PBRColorAttribute.class, PBRColorAttribute.BaseColorFactor);
		Color baseColor = baseColorAttribute != null ? baseColorAttribute.color : Color.WHITE;
		material.set(ColorAttribute.createDiffuse(baseColor));
		material.set(ColorAttribute.createSpecular(baseColor));

		PBRFloatAttribute rougnessAttribute = material.get(PBRFloatAttribute.class, PBRFloatAttribute.Roughness);
		// default roughness is 1 as per GLTF specification.
		float roughness = rougnessAttribute != null ? rougnessAttribute.value : 1;
		
		// Conversion approximation based on Blender FBX export plugin.
		// https://github.com/blender/blender-addons/blob/master/io_scene_fbx/export_fbx_bin.py#L1345
		float shininess = (1 - roughness) * 10;
		if(shininess > 0){
			material.set(FloatAttribute.createShininess(shininess * shininess));
		}
	}
}
