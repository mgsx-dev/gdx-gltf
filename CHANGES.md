# Changes history

### 2.0.0

* [BREAKING CHANGE] upgrade to libGDX 1.9.11, you can't use older libGDX with this version.
* [BREAKING CHANGE] MeshPlus class has been removed since libGDX now supports 64k vertices meshes. If you directly used MeshPlus in your code, you can now use Mesh class instead.
* [BREAKING CHANGE] Fog density is now controlled by fog color alpha. Make sure to have fog color alpha at 1.0 in your code to keep previous behavior.
* [BREAKING CHANGE] Tangent are automatically computed when necessary: when normal map is used and tangent attribute is missing.
* Meshes with integer indices are now partially supported, vertices are split into 64k chunks.
* Added LINES and POINTS support.

### 1.0.0

Initial release.