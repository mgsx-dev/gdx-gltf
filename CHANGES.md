# Changes history

### 2.2.0

* Added `KHR_materials_transmission` extension support.
* Added `KHR_materials_volume` extension support.
* Added `KHR_materials_ior` extension support.
* Added `KHR_materials_specular` extension support.
* Added `KHR_materials_iridescence` extension support.
* Added `KHR_materials_emissive_strength` extension support.
* Added optional TransmissionSource renderer to SceneManager for scene refraction effect.
* Added shader parser in order to split PBR shader into several files for better maintainability.
* Added HDR/RGBE subformat support
* Added USHORT and UBYTE support for bone weights
* Fix PBR shader: Ambient occlusion texture is now applied to IBL only instead of all lights.
* Added Mirror specular effect (dynamic reflections).
* Added cascade shadow map.

### 2.1.0

* Added optional environement and skybox rotation.
* Added optional emissive intensity

### 2.0.0

* [BREAKING CHANGE] upgrade to libGDX 1.9.11, you can't use older libGDX with this version.
* [BREAKING CHANGE] MeshPlus class has been removed since libGDX now supports 64k vertices meshes. If you directly used MeshPlus in your code, you can now use Mesh class instead.
* [BREAKING CHANGE] Fog density is now controlled by fog color alpha. Make sure to have fog color alpha at 1.0 in your code to keep previous behavior.
* [BREAKING CHANGE] Tangent are automatically computed when necessary: when normal map is used and tangent attribute is missing.
* Meshes with integer indices are now partially supported, vertices are split into 64k chunks.
* Added LINES and POINTS support.
* BaseColorFactor is now optional.
* Mesh without normal is rendered as unlit.
* Shadows no longer impacts other directional lights.
* Fix light contribution when using shadows. Directional light was slightly dimmed for no reason.
* Manual gamma correction is now configurable for PBRShaderProvider.
* Added SkyBox color space conversion options.

### 1.0.0

Initial release.