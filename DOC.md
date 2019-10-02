
# LibGDX - GLTF Documentation

**Draft**

## Rendering to frame buffer

Sometimes you want ot render scenes to a FBO (Frame buffer). You can do it but you have to take some cautions: SceneManager is using FBOs internally to render shadows. So, instead of calling sceneManager.render(), you have to do something like this:

```java
sceneManager.renderShadows();
		
fbo.begin();
...
sceneManager.renderColors();
...
fbo.end();
```
