# Bugs & Hacks

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
* high-poly mesh support : limited to 32768 vertices because of libGdx short index array.
* emulate default shader based on some PBR information
* key frame interpolation (non linear ...)
* add more lighting support : multiple directional lights, point lights, spot lights ...

## Morph Targets full implementation

- Tangent space recalculation not really defined and morphed normals doesn't really work ...
  see: https://github.com/KhronosGroup/glTF/tree/master/specification/2.0#appendix-a-tangent-space-recalculation
- Client may implements at least 8 channels (8 positions or 4 position/normal or 2 pos/tan/nor) ...
- add support for 8 positions / 4 normals / 2 tangents. POC allow 2 positions / 2 normals / 2 tangents


## Extensions

* **KHR_texture_transform** partially : need to separate individual transforms (baseColor, emissive, and so on)
  https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_texture_transform/README.md
* **KHR_materials_pbrSpecularGlossiness** not yet implemented
* **KHR_lights_punctual** : https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_lights_punctual/README.md
* **KHR_materials_unlit** : https://github.com/KhronosGroup/glTF/blob/master/extensions/2.0/Khronos/KHR_materials_unlit/README.md*

# Tests

* multiple scenes
* multiple models with different Shader configurations
