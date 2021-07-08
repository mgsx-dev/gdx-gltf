# Bugs & Hacks

# Loading process

One of GLTF design goal is "Fast loading" : glTF data structures have been designed to mirror the GPU API data as closely as possible.

Due to LibGDX platform abstraction, this implementation require to process data (mainly vertices), so loading performances are not optimal for now but could be improved by directly loading mesh data.

# Morph Targets normal

Normals are not OK when tangent attribute missing ... 

## Monkey patches

Several LibGDX classes as been overridden or monkey patched in order to implements all features.

Some of them could be pull up to LibGDX repository via pull request :

* because of morph targets, it's needed to override so many classes (eg animator) so the solution
would be to implements morph targets directly in libgdx core.
* cubemap mipmaps doesn't seams to be handled, it is auto generated for 2D textures but no way
to upload manually individual files : a POC has been made and is promising, should be better or integrated
to classic cubemap with multi texture support. As well as auto gen feature ?

## Optimizations

- use VAO SubData to directly load buffer without transformation, which is a purpose of GLTF format.
  maybe by exposing protected Mesh contructor, using custom VertexData and IndexData ...

# FEATURES

* AssetManager loader utilities
* sparse accessors
* emulate default shader based on some PBR information

## Morph Targets full implementation

- Tangent space recalculation not really defined and morphed normals doesn't really work ...
  see: https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#appendix-a-tangent-space-recalculation

## Extensions

Extension used by Blender exporter and not implemented :

* **KHR_materials_pbrSpecularGlossiness**
* **KHR_materials_clearcoat** 
* **KHR_materials_transmission** 
* **KHR_draco_mesh_compression**  

