package net.mgsx.gltf.loaders.shared.data;

import net.mgsx.gltf.data.data.GLTFAccessor;
import net.mgsx.gltf.data.data.GLTFBufferView;
import net.mgsx.gltf.loaders.shared.GLTFTypes;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AccessorBuffer {
    private final GLTFAccessor accessor;
    /**
     * BufferView that represents this buffer. Is null when the accessor has no
     * buffer (is initialized with zeros).
     */
    private final GLTFBufferView bufferView;
    private ByteBuffer data;
    private int byteOffset;

    AccessorBuffer(GLTFAccessor accessor, GLTFBufferView bufferView, ByteBuffer data) {
        this.accessor = accessor;
        this.bufferView = bufferView;
        this.data = data;
        if (bufferView != null) {
            byteOffset = bufferView.byteOffset + accessor.byteOffset;
        } else {
            byteOffset = 0;
        }
    }

    public int getByteStride() {
        if (bufferView != null && bufferView.byteStride != null) {
            return bufferView.byteStride;
        } else {
            return GLTFTypes.accessorStrideSize(accessor);
        }
    }

    public int getByteOffset() {
        return byteOffset;
    }

    /**
     * If the accessor is backed by a buffer potentially used by others,
     * create a copy so this buffer can be safely manipulated
     */
    public void prepareForWriting() {
        if (bufferView != null) {
            // replace the buffer view with a copy as we will have to modify it
            prepareForReading();
            ByteBuffer clone = ByteBuffer.allocate(data.remaining());
            clone.put(data);
            clone.order(data.order());
            clone.flip();
            data = clone;
            byteOffset = 0;
        }
    }

    /**
     * The buffer will be positioned on the first element belonging
     * to the accessor.
     *
     * @see #getData()
     */
    public ByteBuffer prepareForReading() {
        data.position(getByteOffset());
        return data;
    }

    /**
     * Buffer containing the accessors' data. Make sure to consider
     * {@link #getByteStride()} and {@link #getByteOffset()} when
     * reading from this!
     */
    public ByteBuffer getData() {
        return data;
    }

    public static AccessorBuffer fromBufferView(
            GLTFAccessor glAccessor, GLTFBufferView glBufferView, DataFileResolver resolver
    ) {
        AccessorBuffer buffer = new AccessorBuffer(
                glAccessor, glBufferView, resolver.getBuffer(glBufferView.buffer)
        );
        buffer.data.position(buffer.getByteOffset());
        return buffer;
    }

    public static AccessorBuffer fromZeros(GLTFAccessor glAccessor) {
        // spec for undefined bufferView:
        // "When undefined, the accessor **MUST** be initialized with zeros"
        ByteBuffer zeros = ByteBuffer.allocate(GLTFTypes.accessorSize(glAccessor)).order(ByteOrder.LITTLE_ENDIAN);
        return new AccessorBuffer(glAccessor, null, zeros);
    }
}
