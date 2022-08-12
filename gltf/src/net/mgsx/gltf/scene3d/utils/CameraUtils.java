package net.mgsx.gltf.scene3d.utils;

import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.utils.GdxRuntimeException;

public class CameraUtils {

    public static Camera createCamera(Camera from) {
        Camera copy;
        if (from instanceof PerspectiveCamera) {
            PerspectiveCamera camera = new PerspectiveCamera();
            camera.fieldOfView = ((PerspectiveCamera) from).fieldOfView;
            copy = camera;
        } else if (from instanceof OrthographicCamera) {
            OrthographicCamera camera = new OrthographicCamera();
            camera.zoom = ((OrthographicCamera) from).zoom;
            copy = camera;
        } else {
            throw new GdxRuntimeException("unknown camera type " + from.getClass().getName());
        }
        copy.position.set(from.position);
        copy.direction.set(from.direction);
        copy.up.set(from.up);
        copy.near = from.near;
        copy.far = from.far;
        copy.viewportWidth = from.viewportWidth;
        copy.viewportHeight = from.viewportHeight;
        return copy;
    }

}
