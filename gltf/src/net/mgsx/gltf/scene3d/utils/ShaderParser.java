package net.mgsx.gltf.scene3d.utils;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * ShaderParser allows to recursively load shader code split into several files.
 * 
 * It brings support for file inclusion like: <pre>#include&lt;part.glsl&gt;</pre>
 * 
 * Given paths are relative to the file declaring the include statement.
 * 
 * @author mgsx
 *
 */
public class ShaderParser {
	private final static String includeBefore = "#include <";
	private final static String includeAfter = ">";
	
	public static String parse(FileHandle file){
		String content = file.readString();
		String[] lines = content.split("\n");
		String result = "";
		for(String line : lines){
			String cleanLine = line.trim();
			
			if(cleanLine.startsWith(includeBefore)){
				int end = cleanLine.indexOf(includeAfter, includeBefore.length());
				if(end < 0) throw new GdxRuntimeException("malformed include: " + cleanLine);
				String path = cleanLine.substring(includeBefore.length(), end);
				FileHandle subFile = file.sibling(path);
				result += "\n//////// " + path + "\n";
				result += parse(subFile);
			}else{
				result += line + "\n";
			}
		}
		return result;
	}
	
}
