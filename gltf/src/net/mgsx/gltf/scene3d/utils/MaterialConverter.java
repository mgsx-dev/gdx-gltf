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
		if(baseColor != null){
			material.set(ColorAttribute.createDiffuse(baseColor));
		}
		// Conversion approximation based on Blender FBX export plugin.
		// https://github.com/blender/blender-addons/blob/master/io_scene_fbx/export_fbx_bin.py#L1345
		PBRFloatAttribute rougness = material.get(PBRFloatAttribute.class, PBRFloatAttribute.Roughness);
		if(rougness != null){
			float shininess = (1 - rougness.value) * 10;
			material.set(FloatAttribute.createShininess(shininess * shininess));
			material.set(ColorAttribute.createSpecular(baseColor.cpy().mul(.5f)));
		}
		PBRFloatAttribute metallic = material.get(PBRFloatAttribute.class, PBRFloatAttribute.Metallic);
		if(metallic != null){
			material.set(ColorAttribute.createReflection(baseColor.cpy().mul(metallic.value)));
		}
	}
}
