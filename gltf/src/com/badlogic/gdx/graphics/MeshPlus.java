package com.badlogic.gdx.graphics;

import java.nio.FloatBuffer;
import java.nio.ShortBuffer;

import com.badlogic.gdx.graphics.VertexAttributes.Usage;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.math.collision.BoundingBox;
import com.badlogic.gdx.utils.GdxRuntimeException;

/**
 * Monkey patched Mesh to allow 64k max vertices 
 */
public class MeshPlus extends Mesh {
	
	private final Vector3 tmpV = new Vector3();

	public MeshPlus(boolean isStatic, int maxVertices, int maxIndices, VertexAttributes attributes) {
		super(isStatic, maxVertices, maxIndices, attributes);
	}

	@Override
	public BoundingBox extendBoundingBox (final BoundingBox out, int offset, int count, final Matrix4 transform) {
		final int numIndices = getNumIndices();
		final int numVertices = getNumVertices();
		final int max = numIndices == 0 ? numVertices : numIndices;
		if (offset < 0 || count < 1 || offset + count > max)
			throw new GdxRuntimeException("Invalid part specified ( offset=" + offset + ", count=" + count + ", max=" + max + " )");

		final FloatBuffer verts = vertices.getBuffer();
		final ShortBuffer index = indices.getBuffer();
		final VertexAttribute posAttrib = getVertexAttribute(Usage.Position);
		final int posoff = posAttrib.offset / 4;
		final int vertexSize = vertices.getAttributes().vertexSize / 4;
		final int end = offset + count;

		switch (posAttrib.numComponents) {
		case 1:
			if (numIndices > 0) {
				for (int i = offset; i < end; i++) {
					final int idx = (index.get(i) & 0xFFFF) * vertexSize + posoff;
					tmpV.set(verts.get(idx), 0, 0);
					if (transform != null) tmpV.mul(transform);
					out.ext(tmpV);
				}
			} else {
				for (int i = offset; i < end; i++) {
					final int idx = i * vertexSize + posoff;
					tmpV.set(verts.get(idx), 0, 0);
					if (transform != null) tmpV.mul(transform);
					out.ext(tmpV);
				}
			}
			break;
		case 2:
			if (numIndices > 0) {
				for (int i = offset; i < end; i++) {
					final int idx = (index.get(i) & 0xFFFF) * vertexSize + posoff;
					tmpV.set(verts.get(idx), verts.get(idx + 1), 0);
					if (transform != null) tmpV.mul(transform);
					out.ext(tmpV);
				}
			} else {
				for (int i = offset; i < end; i++) {
					final int idx = i * vertexSize + posoff;
					tmpV.set(verts.get(idx), verts.get(idx + 1), 0);
					if (transform != null) tmpV.mul(transform);
					out.ext(tmpV);
				}
			}
			break;
		case 3:
			if (numIndices > 0) {
				for (int i = offset; i < end; i++) {
					final int idx = (index.get(i) & 0xFFFF) * vertexSize + posoff;
					tmpV.set(verts.get(idx), verts.get(idx + 1), verts.get(idx + 2));
					if (transform != null) tmpV.mul(transform);
					out.ext(tmpV);
				}
			} else {
				for (int i = offset; i < end; i++) {
					final int idx = i * vertexSize + posoff;
					tmpV.set(verts.get(idx), verts.get(idx + 1), verts.get(idx + 2));
					if (transform != null) tmpV.mul(transform);
					out.ext(tmpV);
				}
			}
			break;
		}
		return out;
	}
	
	@Override
	public float calculateRadiusSquared (final float centerX, final float centerY, final float centerZ, int offset, int count,
			final Matrix4 transform) {
		int numIndices = getNumIndices();
		if (offset < 0 || count < 1 || offset + count > numIndices) throw new GdxRuntimeException("Not enough indices");

		final FloatBuffer verts = vertices.getBuffer();
		final ShortBuffer index = indices.getBuffer();
		final VertexAttribute posAttrib = getVertexAttribute(Usage.Position);
		final int posoff = posAttrib.offset / 4;
		final int vertexSize = vertices.getAttributes().vertexSize / 4;
		final int end = offset + count;

		float result = 0;

		switch (posAttrib.numComponents) {
		case 1:
			for (int i = offset; i < end; i++) {
				final int idx = (index.get(i) & 0xFFFF) * vertexSize + posoff;
				tmpV.set(verts.get(idx), 0, 0);
				if (transform != null) tmpV.mul(transform);
				final float r = tmpV.sub(centerX, centerY, centerZ).len2();
				if (r > result) result = r;
			}
			break;
		case 2:
			for (int i = offset; i < end; i++) {
				final int idx = (index.get(i) & 0xFFFF) * vertexSize + posoff;
				tmpV.set(verts.get(idx), verts.get(idx + 1), 0);
				if (transform != null) tmpV.mul(transform);
				final float r = tmpV.sub(centerX, centerY, centerZ).len2();
				if (r > result) result = r;
			}
			break;
		case 3:
			for (int i = offset; i < end; i++) {
				final int idx = (index.get(i) & 0xFFFF) * vertexSize + posoff;
				tmpV.set(verts.get(idx), verts.get(idx + 1), verts.get(idx + 2));
				if (transform != null) tmpV.mul(transform);
				final float r = tmpV.sub(centerX, centerY, centerZ).len2();
				if (r > result) result = r;
			}
			break;
		}
		return result;
	}
}
