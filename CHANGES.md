# Changes history

### 2.0.0

* upgrade to libGDX 1.9.11, you can't use older libGDX with this version.
* MeshPlus class has been removed since libGDX now supports 64k vertices meshes. If you directly used MeshPlus in your code, you can now use Mesh class instead.
* Fog density is now controlled by fog color alpha. Make sure to have fog color alpha at 1.0 in your code to keep previous behavior.

### 1.0.0

Initial release.