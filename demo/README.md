
# Run Desktop Examples

Default is to run a subset of [Khronos models examples](https://github.com/KhronosGroup/glTF-Sample-Models) remotly.

It's possible to run examples locally by providing absolute path to examples folder as desktop launcher first argument.

Example with Khronos models : 

	$ git clone https://github.com/KhronosGroup/glTF-Sample-Models.git
	$ ./gdx-gltf-demo.jar glTF-Sample-Models/2.0


BatchTest can be configured the same way to process a different model set.

# Examples

## Khronos models examples

Here is the list and status about [Khronos models examples](https://github.com/KhronosGroup/glTF-Sample-Models)

| **Model**                 | **Features** |--- **Errors** ---| **Comment** |
|---------------------------|:------------:|:-----------------|-------------|
| 2CylinderEngine			| ?						|  | 
| AlphaBlendModeTest		| Blending
| AnimatedCube				| Animation
| AnimatedMorphCube			| Morph Targets
| AnimatedMorphSphere		| Morph Targets
| AnimatedTriangle			| Animation
| AntiqueCamera				| PBR					| ~ | texture files format not supported (work by re-export with GIMP) |
| Avocado					| PBR
| BarramundiFish			| PBR
| BoomBox					| PBR
| BoomBoxWithAxes			| PBR
| Box						| PBR
| BoxAnimated				| Animation
| BoxInterleaved			| Data Stride
| BoxTextured				|
| BoxTexturedNonPowerOfTwo	| NPT
| BoxVertexColors			| Vertex Color
| BrainStem					| Animation
| Buggy						| PBR
| Cameras					| Camera
| CesiumMan					| Skinning
| CesiumMilkTruck			| PBR
| Corset					| PBR
| Cube						|
| DamagedHelmet				| PBR
| Duck						| PBR
| FlightHelmet				| PBR
| GearboxAssy				| PBR
| Lantern					| PBR
| MetalRoughSpheres			| Metallic Roughness
| Monster					| Skinning
| MorphPrimitivesTest		| Morph Targets
| MultiUVTest				| Multi UV
| NormalTangentMirrorTest	| Tangent Space
| NormalTangentTest			| Tangent Space
| OrientationTest			|
| ReciprocatingSaw			| PBR
| RiggedFigure				| Skinning
| RiggedSimple				| Skinning
| SciFiHelmet				| PBR					| X | Too many vertices (more than 64k) |
| SimpleMeshes				|
| SimpleMorph				| Morph Targets
| SimpleSparseAccessor		| Sparse Accessor
| SpecGlossVsMetalRough		| Specular Gloss EXT
| Sponza					| PBR
| Suzanne					| PBR
| TextureCoordinateTest		| UV
| TextureSettingsTest		| Texture Settings
| TextureTransformTest		| Texture Transform EXT	|
| Triangle					| Basic
| TriangleWithoutIndices	| Without Indices
| TwoSidedPlane				| Culling
| VC						| Camera Animation
| VertexColorTest			| Vertex Color
| WaterBottle				| PBR

