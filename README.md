
LibGDX GLTF 2.0 and PBR shader implementation **Work In Progress**

![status](https://img.shields.io/badge/glTF-2%2E0-green.svg?style=flat)

[![Release](https://jitpack.io/v/mgsx-dev/gdx-gltf.svg)](https://jitpack.io/#mgsx-dev/gdx-gltf)

# LibGDX - GL Transmission Format (glTF) 2.0 Support

Implementation based on official [glTF 2.0 Specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0)

Shaders inspiried by glTF-WebGL-PBR demo :

* https://github.com/KhronosGroup/glTF-WebGL-PBR/blob/master/shaders/pbr-vert.glsl
* https://github.com/KhronosGroup/glTF-WebGL-PBR/blob/master/shaders/pbr-frag.glsl

## Demo

* HTML : the online demo is available [here](http://www.mgsx.net/gdx-gltf/) and only contains few examples.
* Desktop : the desktop demo is available [here](https://github.com/mgsx-dev/gdx-gltf/releases). It remotly loads a lot of example. see [gdx-gltf-demo readme](demo/README.md) for futher information.
* Android : the Android demo is available [on Play Store](https://play.google.com/store/apps/details?id=net.mgsx.gltf.demo) and only contains few examples (same as HTML version).

## GLTF extensions implemented

* [KHR_texture_transform](https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_texture_transform)
* [KHR_lights_punctual](https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_lights_punctual)
* [KHR_materials_unlit](https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_materials_unlit)

# Getting started

## Install

gdx-gltf is avilable via Jitpack.

ensure you have jitpack repository declared in your Gradle configuration : 

```
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

Add dependency in your core project : 

```
project(":core") {
    dependencies {
    	...
        compile "com.github.mgsx-dev.gdx-gltf:gltf:master-SNAPSHOT"
    }
}
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

see [Example code](https://github.com/mgsx-dev/gdx-gltf/blob/master/demo/core/src/net/mgsx/gltf/demo/GLTFExample.java)
for more information.

## Export models from Blender

* - 2.79b- : https://github.com/KhronosGroup/glTF-Blender-Exporter
* - 2.8+   : https://github.com/KhronosGroup/glTF-Blender-IO


# More about the library

## Project structure

This repository is made of a library and a demo :

* **gltf** project aims to be a LibGDX extension in the future.
  see [gdx-gltf readme](gltf/README.md) for futher information.
* **demo** folder contains a LibGDX demo project with usual modules (core, desktop, android, html, ...)
  see [gdx-gltf-demo readme](demo/README.md) for futher information.


## LibGDX integration notes

### Morph targets

Several classes has been hacked in order to support morph targets :

* ModelInstance
* Node
* NodePart
* NodeAnimation
* AnimationController

LibGDX could be modified to remove all this hacks by either : 

* adding morph target full support
* allowing proper overrides

### Loading process

One of GLTF design goal is "Fast loading" : glTF data structures have been designed to mirror the GPU API data as closely as possible.

Due to LibGDX platform abstraction, this implementation require to process data (mainly vertices), so loading performances are not optimal for now but could be improved by directly loading mesh data.


### Mesh limitations

LibGDX only support signed short indices, mesh are then limited to 32768 vertices. 

### WebGL limitations

LibGDX Pixmap loading from binary data is not supported by its GWT emulation. So, GLTF embeded and binary formats are not supported for html/WebGL target.

