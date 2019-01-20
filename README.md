
LibGDX GLTF 2.0 and PBR shader implementation **Work In Progress**

# GL Transmission Format (glTF) 2.0

Implementation based on official [glTF 2.0 Specification](https://github.com/KhronosGroup/glTF/tree/master/specification/2.0)

Shaders inspiried by glTF-WebGL-PBR demo :

* https://github.com/KhronosGroup/glTF-WebGL-PBR/blob/master/shaders/pbr-vert.glsl
* https://github.com/KhronosGroup/glTF-WebGL-PBR/blob/master/shaders/pbr-frag.glsl

# Demo

* HTML : the online demo is available [here](http://www.mgsx.net/gdx-gltf/) and only contains few examples.
* Desktop : the desktop demo is available [here](https://github.com/mgsx-dev/gdx-gltf/releases). It remotly loads a lot of example. see [gdx-gltf-demo readme](demo/README.md) for futher information.

# GLTF extensions implemented

* [KHR_texture_transform](https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_texture_transform)
* [KHR_lights_punctual](https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_lights_punctual)
* [KHR_materials_unlit](https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_materials_unlit)

# Project structure

This repository is made of a library and a demo :

* **gltf** project aims to be a LibGDX extension in the future.
  see [gdx-gltf readme](gltf/README.md) for futher information.
* **demo** folder contains a LibGDX demo project with usual modules (core, desktop, android, html, ...)
  see [gdx-gltf-demo readme](demo/README.md) for futher information.

# Blender exporter

* - 2.79b- : https://github.com/KhronosGroup/glTF-Blender-Exporter
* - 2.8+   : https://github.com/KhronosGroup/glTF-Blender-IO

# LibGDX integration notes

## Morph targets

Several classes has been hacked in order to support morph targets :

* ModelInstance
* Node
* NodePart
* NodeAnimation
* AnimationController

LibGDX could be modified to remove all this hacks by either : 

* adding morph target full support
* allowing proper overrides

## Loading process

One of GLTF design goal is "Fast loading" : glTF data structures have been designed to mirror the GPU API data as closely as possible.

Due to LibGDX platform abstraction, this implementation require to process data (mainly vertices), so loading performances are not optimal for now but could be improved by directly loading mesh data.


## Mesh limitations

LibGDX only support signed short indices, mesh are then limited to 32768 vertices. 

## WebGL limitations

LibGDX Pixmap loading from binary data is not supported by its GWT emulation. So, GLTF embeded and binary formats are not supported for html/WebGL target.


