
# Loading time (comparative benchmark)

Context:
* Same test model in various format
* 4 spheres containing 36864 indices and 24576 vertices each (12288 triangles each).
* Run on desktop

All exported from Blender, FBX converted to G3D like this:
`$ fbx-conv -o G3DJ four-spheres.fbx four-spheres.g3dj`
`$ fbx-conv -o G3DB four-spheres.fbx four-spheres.g3db`

resources can be found in [gltf/testRes/benchmark](../gltf/testRes/benchmark)
benchmark code can be found in [gltf/test/net/mgsx/gltf/Benchmark.java](../gltf/test/net/mgsx/gltf/Benchmark.java)

Results:

```
Consistency check...
Warmup...
Measuring...
G3DJ (100 iterations), average time:277.72ms
G3DB (100 iterations), average time:46.17ms
GLTF separated (100 iterations), average time:11.09ms
GLTF embeded (100 iterations), average time:46.93ms
GLB (100 iterations), average time:11.02ms
OBJ (100 iterations), average time:132.8ms
Done.
```
