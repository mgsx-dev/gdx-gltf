package net.mgsx.gltf.demo;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.GdxRuntimeException;
import com.badlogic.gdx.utils.Json;
import com.badlogic.gdx.utils.ObjectMap.Entry;

import net.mgsx.gltf.demo.data.ModelEntry;
import net.mgsx.gltf.loaders.glb.GLBLoader;
import net.mgsx.gltf.loaders.gltf.GLTFLoader;
import net.mgsx.gltf.scene3d.scene.SceneAsset;

public class GLTFTest extends ApplicationAdapter 
{
	private String samplesPath;
	
	public GLTFTest() {
		this("models");
	}
	
	public GLTFTest(String samplesPath) {
		this.samplesPath = samplesPath;
	}
	
	@Override
	public void create() 
	{
		FileHandle rootFolder = Gdx.files.internal(samplesPath);	
		
		FileHandle file = rootFolder.child("model-index.json");
		
		Array<ModelEntry> entries = new Json().fromJson(Array.class, ModelEntry.class, file);
		
		for(ModelEntry entry : entries){
			for(Entry<String, String> variant : entry.variants){
				FileHandle baseFolder = rootFolder.child(entry.name).child(variant.key);
				
				FileHandle glFile = baseFolder.child(variant.value);
				
				long ptime = System.currentTimeMillis();
				SceneAsset sceneAsset;
				try{
					if(glFile.extension().equals("gltf")){
						sceneAsset = new GLTFLoader().load(glFile);
					}else if(glFile.extension().equals("glb")){
						sceneAsset = new GLBLoader().load(glFile);
					}else{
						throw new GdxRuntimeException("unknown file extension " + glFile.extension());
					}
					long ctime = System.currentTimeMillis();
					float dtime = (ctime - ptime) / 1000f;
					System.out.println(entry.name + " | " + variant.key + " | ok | " + dtime);
					sceneAsset.dispose();
				}catch(GdxRuntimeException e){
					System.err.println(entry.name + " | " + variant.key + " | error | " + e.getMessage());
				}
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
				}
			}
		}
	}
}
