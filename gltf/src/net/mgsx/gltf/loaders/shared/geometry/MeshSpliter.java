package net.mgsx.gltf.loaders.shared.geometry;

import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.IntArray;
import com.badlogic.gdx.utils.IntIntMap;
import com.badlogic.gdx.utils.IntMap;
import com.badlogic.gdx.utils.IntMap.Entry;

class MeshSpliter {

	public static void split(Array<float[]> splitVertices, Array<short[]> splitIndices, float[] vertices, VertexAttributes attributes, int[] indices,
			int verticesPerPrimitive) {
		
		// Values used by some graphics APIs as "primitive restart" values are disallowed.
		// Specifically, the value 65535 (in UINT16) cannot be used as a vertex index. 
		int size16 = 65535;
		
		int stride = attributes.vertexSize / 4;
		
		int vertexMaxSize = size16 * stride;
		
		IntArray primitiveIndices = new IntArray(verticesPerPrimitive);
		IntArray remainingIndices = new IntArray();
		
		int maxIndexFound = 0;
		for(int i=0 ; i<indices.length ; i++){
			maxIndexFound = Math.max(maxIndexFound, indices[i]);
		}
		
		IntMap<IntArray> groups = new IntMap<IntArray>();
		for(int i=0 , count=indices.length; i<count ; ){
			int index0 = indices[i++];
			primitiveIndices.add(index0);
			int group0 = index0 / size16;
			boolean sameGroup = true;
			for(int j=1 ; j<verticesPerPrimitive ; j++){
				int indexI = indices[i++];
				primitiveIndices.add(indexI);
				int groupI = indexI / size16;
				if(groupI != group0){
					sameGroup = false;
				}
			}
			if(sameGroup){
				IntArray group = groups.get(group0);
				if(group == null) groups.put(group0, group = new IntArray());
				for(int j=0 ; j<verticesPerPrimitive ; j++){
					group.add(primitiveIndices.get(j) - group0 * size16);
				}
			}else{
				remainingIndices.addAll(primitiveIndices);
			}
			
			primitiveIndices.clear();
		}
		
		int maxGroup = 0;
		for(Entry<IntArray> entry : groups){
			maxGroup = Math.max(maxGroup, entry.key);
		}
		
		IntArray lastGroup = groups.get(maxGroup);
		int maxIndex = 0;
		for(int i=0 ; i<lastGroup.size ; i++){
			maxIndex = Math.max(maxIndex, lastGroup.get(i));
		}
		
		for(int i=0 ; i<=maxGroup ; i++){
			float[] groupVertices = new float[vertexMaxSize];
			int offset = i * size16 * stride;
			int size = Math.min(vertices.length - offset, groupVertices.length);
			System.arraycopy(vertices, offset, groupVertices, 0, size);
			splitVertices.add(groupVertices);
		}
		
		
		float[] lastVertices = splitVertices.peek();
		
		IntArray toProcess = new IntArray();
		
		while(remainingIndices.size > 0){
			if(maxIndex < 0 || maxIndex >= size16-1){
				maxIndex = -1;
				groups.put(++maxGroup, lastGroup = new IntArray());
				splitVertices.add(lastVertices = new float[vertexMaxSize]);
			}
			IntIntMap reindex = new IntIntMap();
			for(int i=0 ; i<remainingIndices.size ; i++){
				int oindex = remainingIndices.get(i);
				int tindex = reindex.get(oindex, -1);
				
				if(tindex < 0){
					tindex = maxIndex+1;
					if(tindex >= size16){
						toProcess.add(oindex);
					}else{
						reindex.put(oindex, tindex);
						maxIndex = tindex;
					}
				}
				lastGroup.add(tindex);
			}
			
			for(com.badlogic.gdx.utils.IntIntMap.Entry entry : reindex){
				System.arraycopy(vertices, entry.key * stride, lastVertices, entry.value * stride, stride);
			}
			
			if(toProcess.size == 0) break;
			
			remainingIndices.clear();
			IntArray tmp = toProcess;
			toProcess = remainingIndices;
			remainingIndices = tmp;
			maxIndex = -1;
		}
		
		for(int i=0 ; i<=maxGroup ; i++){
			IntArray group = groups.get(i);
			short[] shortIndices = new short[group.size];
			for(int j=0 ; j<group.size ; j++){
				int index = group.get(j);
				shortIndices[j] = (short)index;
			}
			splitIndices.add(shortIndices);
		}
		
		int size = (maxIndex+1) * stride;
		float[] tmp = new float[size];
		System.arraycopy(lastVertices, 0, tmp, 0, size);
		splitVertices.set(splitIndices.size-1, tmp);
	}
}
