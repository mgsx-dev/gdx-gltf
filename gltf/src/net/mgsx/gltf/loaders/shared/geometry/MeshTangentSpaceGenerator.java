package net.mgsx.gltf.loaders.shared.geometry;

import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.VertexAttributes;
import com.badlogic.gdx.math.Vector3;

class MeshTangentSpaceGenerator {
	public static void computeTangentSpace(float[] vertices, short[] indices, VertexAttributes attributesGroup, boolean computeNormals, boolean computeTangents, VertexAttribute normalMapUVs) {
		if(computeNormals) computeNormals(vertices, indices, attributesGroup);
		if(computeTangents) computeTangents(vertices, indices, attributesGroup, normalMapUVs);
	}
	
	private static void computeNormals(float[] vertices, short[] indices, VertexAttributes attributesGroup) {
		int posOffset = attributesGroup.getOffset(VertexAttributes.Usage.Position);
		int normalOffset = attributesGroup.getOffset(VertexAttributes.Usage.Normal);
		int stride = attributesGroup.vertexSize / 4;
		
		Vector3 vab = new Vector3();
		Vector3 vac = new Vector3();
		for(int index = 0, count = indices.length ; index<count ; ){
			
			int vIndexA = indices[index++] & 0xFFFF;
			float ax = vertices[vIndexA * stride + posOffset];
			float ay = vertices[vIndexA * stride + posOffset+1];
			float az = vertices[vIndexA * stride + posOffset+2];
			
			int vIndexB = indices[index++] & 0xFFFF;
			float bx = vertices[vIndexB * stride + posOffset];
			float by = vertices[vIndexB * stride + posOffset+1];
			float bz = vertices[vIndexB * stride + posOffset+2];
			
			int vIndexC = indices[index++] & 0xFFFF;
			float cx = vertices[vIndexC * stride + posOffset];
			float cy = vertices[vIndexC * stride + posOffset+1];
			float cz = vertices[vIndexC * stride + posOffset+2];
			
			vab.set(bx,by,bz).sub(ax,ay,az);
			vac.set(cx,cy,cz).sub(ax,ay,az);
			Vector3 n = vab.crs(vac).nor();
			
			vertices[vIndexA * stride + normalOffset] = n.x;
			vertices[vIndexA * stride + normalOffset+1] = n.y;
			vertices[vIndexA * stride + normalOffset+2] = n.z;
			
			vertices[vIndexB * stride + normalOffset] = n.x;
			vertices[vIndexB * stride + normalOffset+1] = n.y;
			vertices[vIndexB * stride + normalOffset+2] = n.z;
			
			vertices[vIndexC * stride + normalOffset] = n.x;
			vertices[vIndexC * stride + normalOffset+1] = n.y;
			vertices[vIndexC * stride + normalOffset+2] = n.z;
			
		}
	}
	
	// inspired by: https://gamedev.stackexchange.com/questions/68612/how-to-compute-tangent-and-bitangent-vectors
	//
	private static void computeTangents(float[] vertices, short[] indices, VertexAttributes attributesGroup, VertexAttribute normalMapUVs) {
		int posOffset = attributesGroup.getOffset(VertexAttributes.Usage.Position);
		int normalOffset = attributesGroup.getOffset(VertexAttributes.Usage.Normal);
		int tangentOffset = attributesGroup.getOffset(VertexAttributes.Usage.Tangent);
		int texCoordOffset = normalMapUVs.offset / 4;
		int stride = attributesGroup.vertexSize / 4;
		int vertexCount = vertices.length / stride;
		
		Vector3 vu = new Vector3();
		Vector3 vv = new Vector3();
		Vector3[] tan1 = new Vector3[indices.length];
		Vector3[] tan2 = new Vector3[indices.length];
		for(int i=0 ; i<indices.length ; i++) {
			tan1[i] = new Vector3();
			tan2[i] = new Vector3();
		}
		
		for(int index = 0, count = indices.length ; index<count ; ){
			
			int vIndexA = indices[index++] & 0xFFFF;
			float ax = vertices[vIndexA * stride + posOffset];
			float ay = vertices[vIndexA * stride + posOffset+1];
			float az = vertices[vIndexA * stride + posOffset+2];
			
			int vIndexB = indices[index++] & 0xFFFF;
			float bx = vertices[vIndexB * stride + posOffset];
			float by = vertices[vIndexB * stride + posOffset+1];
			float bz = vertices[vIndexB * stride + posOffset+2];
			
			int vIndexC = indices[index++] & 0xFFFF;
			float cx = vertices[vIndexC * stride + posOffset];
			float cy = vertices[vIndexC * stride + posOffset+1];
			float cz = vertices[vIndexC * stride + posOffset+2];
			
			float au = vertices[vIndexA * stride + texCoordOffset];
			float av = vertices[vIndexA * stride + texCoordOffset+1];
			
			float bu = vertices[vIndexB * stride + texCoordOffset];
			float bv = vertices[vIndexB * stride + texCoordOffset+1];
			
			float cu = vertices[vIndexC * stride + texCoordOffset];
			float cv = vertices[vIndexC * stride + texCoordOffset+1];
			
			float dx1 = bx - ax;
			float dx2 = cx - ax;
			
			float dy1 = by - ay;
			float dy2 = cy - ay;
			
			float dz1 = bz - az;
			float dz2 = cz - az;
			
			float du1 = bu - au;
			float du2 = cu - au;
			
			float dv1 = bv - av;
			float dv2 = cv - av;

			float r = 1f / (du1 * dv2 - du2 * dv1);
			
			vu.set( (dv2 * dx1 - dv1 * dx2) * r, 
					(dv2 * dy1 - dv1 * dy2) * r,
	                (dv2 * dz1 - dv1 * dz2) * r);
			
			vv.set( (du1 * dx2 - du2 * dx1) * r, 
					(du1 * dy2 - du2 * dy1) * r,
	                (du1 * dz2 - du2 * dz1) * r);
			
			tan1[vIndexA].add(vu);
			tan2[vIndexA].add(vv);
			
			tan1[vIndexB].add(vu);
			tan2[vIndexB].add(vv);
			
			tan1[vIndexC].add(vu);
			tan2[vIndexC].add(vv);
		}
		
		Vector3 tangent = new Vector3();
		Vector3 normal = new Vector3();
		Vector3 biNormal = new Vector3();
		for(int i=0 ; i<vertexCount ; i++){
			
			float nx = vertices[i * stride + normalOffset];
			float ny = vertices[i * stride + normalOffset+1];
			float nz = vertices[i * stride + normalOffset+2];
			normal.set(nx, ny, nz);
			
			Vector3 t1 = tan1[i];
			tangent.set(t1).mulAdd(normal, -normal.dot(t1)).nor();
			
			Vector3 t2 = tan2[i];
			biNormal.set(normal).crs(tangent);
			float tangentW = biNormal.dot(t2) < 0 ? -1 : 1;
			
			vertices[i * stride + tangentOffset] = tangent.x;
			vertices[i * stride + tangentOffset+1] = tangent.y;
			vertices[i * stride + tangentOffset+2] = tangent.z;
			vertices[i * stride + tangentOffset+3] = tangentW;
		}
	}
}
