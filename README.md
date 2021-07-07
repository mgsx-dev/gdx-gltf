
[![status](https://img.shields.io/badge/glTF-2%2E0-green.svg?style=flat)](https://github.com/KhronosGroup/glTF) [![GitHub release (latest SemVer including pre-releases)](https://img.shields.io/badge/semver-2.0-brightgreen)](https://semver.org/) [![Release](https://jitpack.io/v/mgsx-dev/gdx-gltf.svg)](https://jitpack.io/#mgsx-dev/gdx-gltf) [![GitHub release (latest SemVer including pre-releases)](https://img.shields.io/github/v/release/mgsx-dev/gdx-gltf?include_prereleases&sort=semver)](https://github.com/mgsx-dev/gdx-gltf/releases)

LibGDX GLTF 2.0 support and PBR shader implementation. Alternative to libGDX G3D format.

# Introduction

**What's glTF befenits over G3D/FBX in libGDX?**

* Simpler workflow : no fbx-conv required, you can load gltf files directly.
* Load cameras, lights, custom properties from Blender and other 3D softwares.
* Support loading LINES and POINTS primitives.
* Shape keys / Animated shape keys (aka MorphTarget) feature.
* Multiple animations playback
* Non linear animations keyframes interpolation ("step" and "cubic" supported)
* Out of the box shaders for normal maps, metallic/roughness, Image based lighting (IBL) and more.
* Texture coordinates transform.
* 64k vertices supported (instead of 32k). Meshes with integer indices are split to 64k chunks as well.
* Faster loading time, see [benchmark](docs/BENCHMARK.md)

**What's more than a 3D format parser in gdx-gltf library?**

* Scene management facility : Sky box, shadows, and more.
* Physic Based Rendering (PBR) shaders : for realistic (or not) high quality rendering.
* Spot light support.
* Export various objects to glTF file (whole scene, model, mesh, etc).

**Can i only load glTF files and use them with regular libgdx 3D API?**

* Yes, it's the same API, only materials differs : by default gdx-gltf uses its own shader (PBR) to enable all glTF features.
* Note that libgdx default shader doesn't implements spot lights.
* If you don't want/need high quality rendering (PBR), you still can configure loaders to use libgdx materials (and libgdx DefaultShader).

## Demo and gallery

Library demo (aka model viewer) is available for several platforms:

* HTML : [Online demo](http://www.mgsx.net/gdx-gltf/) and only contains few examples.
* Desktop : [Desktop demo](https://github.com/mgsx-dev/gdx-gltf/releases) allow you to open and inspect any GLTF file. See [gdx-gltf-demo readme](demo/README.md) for futher information.
* Android : [Android demo](https://play.google.com/store/apps/details?id=net.mgsx.gltf.demo) only contains few examples (same as HTML version).

Games made with this library:

* [Forsaken Portals](https://store.steampowered.com/app/1338220/Forsaken_Portals/)
* [Lendigastel](https://mgsx.itch.io/lendigastel) (gamejam)
* [Santa and the giant cake](https://mgsx.itch.io/santa-and-the-giant-cake) (gamejam)
* [Multiverse Racer](https://mgsx.itch.io/multiverse-racer) (gamejam)

# GL Transmission Format (glTF) 2.0 Support

Implementation based on official [glTF 2.0 Specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0)

Shaders inspired by glTF-WebGL-PBR demo :

* https://github.com/KhronosGroup/glTF-WebGL-PBR/blob/glTF-WebGL-PBR-final/shaders/pbr-vert.glsl
* https://github.com/KhronosGroup/glTF-WebGL-PBR/blob/glTF-WebGL-PBR-final/shaders/pbr-frag.glsl


GLTF extensions implemented:

* [KHR_texture_transform](https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_texture_transform)
* [KHR_lights_punctual](https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_lights_punctual)
* [KHR_materials_unlit](https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_materials_unlit)

# Getting started

## Compatibility

* gdx-gltf 1.x requires libGDX 1.9.10+
* gdx-gltf 2.x requires libGDX 1.9.11+

Before upgrading your gdx-gltf version, please read [changes history](CHANGES.md).

## Install

gdx-gltf is available via Jitpack.

ensure you have jitpack repository declared in your Gradle configuration and add a gltfVersion variable.

Version can be any release (latest release is recommended) or master-SNAPSHOT

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
	ext {
        ...
        gltfVersion = 'master-SNAPSHOT'
    }
}
```

Add dependency in your core project (replace master-SNAPSHOT by latest release to use a stable version) : 

```
project(":core") {
    dependencies {
    	...
        api "com.github.mgsx-dev.gdx-gltf:gltf:$gltfVersion"
    }
}
```

For GWT (html) projects you need to add source dependency and inherit GWT module in your core .gwt.xml file.

```
project(":html") {
    dependencies {
    	...
        api "com.github.mgsx-dev.gdx-gltf:gltf:$gltfVersion:sources"
    }
}
```

```
<module>
	<inherits name='GLTF' />
	...
</module>
```
 

## Loading asset files

### Directly 

```java
SceneAsset sceneAsset = new GLTFLoader().load(Gdx.files.internal("myModel.gltf"));
SceneAsset sceneAsset = new GLBLoader().load(Gdx.files.internal("myModel.glb"));
```

### Using AssetManager loaders

```java
assetManager.setLoader(SceneAsset.class, ".gltf", new GLTFAssetLoader());
assetManager.setLoader(SceneAsset.class, ".glb", new GLBAssetLoader());
...
assetManager.load("myModel.gltf");
...
SceneAsset sceneAsset = assetManager.get("myModel.gltf", SceneAsset.class);
```

## Render models

This library provides a convenient scene manager to handle glTF models and PBR shader.

see few provided examples for more information:

* [QuickStart](https://github.com/mgsx-dev/gdx-gltf/blob/master/demo/core/src/net/mgsx/gltf/examples/GLTFQuickStartExample.java) to quickly setup a scene.
* [Classic example](https://github.com/mgsx-dev/gdx-gltf/blob/master/demo/core/src/net/mgsx/gltf/examples/GLTFExample.java) to setup an advanced lighting environment (using HDRIs based IBL).
* [Post processing example](https://github.com/mgsx-dev/gdx-gltf/blob/master/demo/core/src/net/mgsx/gltf/examples/GLTFPostProcessingExample.java) to see how to deal with post processing effects (render to texture)

For advanced usage, please read [full documentation](docs/DOC.md)

## Export objects from libgdx

This library provides convenient methods to export various object type to glTF file.
For instance, You can create some mesh programmatically in libgdx and export them to glTF files and optionally import them in Blender:

```java
new GLTFExporter().export(model, Gdx.files.local("myModel.gltf")
```

You can also export a scene with its lights and camera. All gltf features are supported for export: animations, bones, etc.
Note that only "gltf separate files" mode is currently supported for export.

## Export models from Blender

As Blender 2.80, glTF exporter addon is included and enabled by default.

* - 2.80+  : https://github.com/KhronosGroup/glTF-Blender-IO
* - 2.79b- : https://github.com/KhronosGroup/glTF-Blender-Exporter

## Image Based Lighting (IBL)

Demo is shipped with a pre-generated lighting environment.
If you want to use others or generate them yourself, please [read IBL guide](docs/IBL.md)
Alternatively this library provide some quick IBL generators, it's not as accurate as HDRI based IBL but can be useful to quickly setup a lighting environement. see **IBLBuilder** class.

# More about the library

## Project structure

This repository is made of a library and a demo :

* **gltf** library module (LibGDX extension).
  see [gdx-gltf readme](gltf/README.md) for futher information.
* **demo** folder contains a LibGDX demo project with usual modules (core, desktop, android, html, ...)
  see [gdx-gltf-demo readme](demo/README.md) for futher information.


## Limitations

### Mesh limitations

Because LibGDX (and some GPUs) only supports unsigned short indices, meshes with integer indices are split into 64k chunks automatically at load time. For best loading performance, it's recommended to split your meshes beforehand (eg. in Blender).

Note that Blender vertex count can be misleading because exported geometry may contains more vertices because of
normal split, texture coordinates split or vertex color split.

### WebGL limitations

LibGDX Pixmap loading from binary data is not supported by its GWT emulation. So, GLTF embeded and binary formats are not supported for html/WebGL target.

### CubeMap seamless limitations

CubeMap seamless is a feature that perform correct cubeMap filtering. Without this feature, you may notice some artifacts for materials with high roughness since these artifacts are more visible at low resolution.
The library automatically enable this feature when possible, based on these conditions :

* For GLES 2 and WebGL 1, this feature is not supported at all.
* For GLES 3 and WebGL 2, this feature is always supported and always activated.
* For Desktop, it can be enabled if platform supports OpenGL 3.2+ or GL_ARB_seamless_cube_map

# Troubleshooting

## Rigged model partially rendered

It's a common pitfall. Most of the time, that means you didn't configure numBones for your SceneManager shader providers.
It's highly recommended to properly configure them for your need. Default configuration is rarely optimized or adapted to your need. 
For example, if the maximum bones among all your models is 60, and you only need one directional light for all scenes in your game, you should create your SceneManager like this: 

```
PBRShaderConfig config = PBRShaderProvider.createDefaultConfig();
config.numBones = 60;
config.numDirectionalLights = 1;
config.numPointLights = 0;

Config depthConfig = PBRShaderProvider.createDefaultDepthConfig();
depthConfig.numBones = 60;

SceneManager sceneManager = new SceneManager(new PBRShaderProvider(config), new PBRDepthShaderProvider(depthConfig));
```

## Max uniforms: Constant register limit exceeded, do not fit in N vectors

You may encounter this shader compilation error in case too many uniform needed on current hardware.

`Constant register limit exceeded at ... more than 1024 registers needed to compiled program`

or 

`Error: uniform variables in vertex shader do not fit in 256 vectors.`

It typically means you may have too many bones. A single bone takes 4 uniforms (mat4), desktop GPU typically supports 1024 uniforms and lowend mobile 256 uniforms.
That mean you should keep bones count under 50 per skeleton.


## Tangent vertex attributes

When tangent attribute is missing and needed (when normal map is used), those are generated automatically.
For best loading performance, it's recommended to export them within your GLTF files.

## Max vertex attributes : too many vertex attributes

You may encounter this error if you have too many vertex attributes in one of your mesh.

Most GPU support up to [16 vertex attributes](https://opengl.gpuinfo.org/displaycapability.php?name=GL_MAX_VERTEX_ATTRIBS)

This limit can be quickly reached depending on mesh information : 

* a_position: 1
* a_normal: 1
* a_tangent: 1 (optional)
* a_color: 1 (optional)
* a_texCoordX: up to 2 UVs layers
* a_boneWeightX: up to 8 bones influences
* a_positionX: up to 8 positions
* a_normalX: up to 8 normals
* a_tangentX: up to 8 tangents

