# Build Desktop demo

From root folder run distDesktop gradle task : `./gradlew distDesktop`.
It will produce a runnable jar file in **demo/desktop/build/libs** folder.

# Run Desktop Examples

Default is to boot in an empty scene, you can then open and inspect your own glTF files.

Alternatively, you can run Khronos example as follow :

	$ git clone https://github.com/KhronosGroup/glTF-Sample-Models.git
	$ gdx-gltf-demo-desktop.jar glTF-Sample-Models/2.0

Jar filename should be adapted depending on the version you built or downloaded.

# Examples

## Khronos models examples

Here is the list and status about [Khronos models examples](https://github.com/KhronosGroup/glTF-Sample-Models)

| **Model**                 | **Features** |--- **Errors** ---| **Comment** |
|---------------------------|:------------:|:-----------------|-------------|
| 2CylinderEngine			| 
| ABeautifulGame			| Refraction
| AlphaBlendModeTest		| Blending
| AnimatedCube				| Animation
| AnimatedMorphCube			| Morph Targets
| AnimatedMorphSphere		| Morph Targets
| AnimatedTriangle			| Animation
| AntiqueCamera				| PBR
| AttenuationTest			| Refraction
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
| Box With Spaces			| Char. encoding
| BrainStem					| Animation
| Buggy						| PBR
| Cameras					| Camera
| CesiumMan					| Skinning
| CesiumMilkTruck			| PBR
| ClearCoatTest				| Clear coat EXT		| ~ | Not supported
| Corset					| PBR
| Cube						|
| DamagedHelmet				| PBR
| DragonAttenuation			| Refraction
| Duck						| PBR
| EmissiveStrengthTest		| Emissive EXT
| EnvironmentTest			| IBL EXT				| ~ | Not supported
| FlightHelmet				| PBR
| Fox						| Skinning
| GearboxAssy				| PBR
| GlamVelvetSofa			| Sheen	EXT				| ~ | Not supported
| InterpolationTest			| Animations
| IridescenceDielectricSpheres  | Iridescence
| IridescenceLamp				| Iridescence
| IridescenceMetallicSpheres	| Iridescence
| IridescenceSuzanne			| Iridescence
| IridescentDishWithOlives		| Iridescence
| Lantern					| PBR
| LightsPunctualLamp		| Lights
| MaterialsVariantsShoe		| Variants EXT			| ~ | Not supported
| MetalRoughSpheres			| Metallic Roughness
| MetalRoughSpheresNoTextures	| Metallic Roughness
| MorphPrimitivesTest		| Morph Targets
| MorphStressTest			| Morph Targets			| X | too many vertex attributes : 20 > 16 (hardware dependent)
| MosquitoInAmber			| Refraction
| MultiUVTest				| Multi UV
| NormalTangentMirrorTest	| Tangent Space
| NormalTangentTest			| Tangent Space
| OrientationTest			|
| ReciprocatingSaw			| PBR
| RecursiveSkeletons		| Skinning
| RiggedFigure				| Skinning
| RiggedSimple				| Skinning
| SciFiHelmet				| PBR
| SheenChair				| Sheen	EXT				| ~ | Not supported
| SheenCloth				| Sheen	EXT				| ~ | Not supported
| SimpleMeshes				|
| SimpleMorph				| Morph Targets
| SimpleSkin				| Skinning
| SimpleSparseAccessor		| Sparse Accessor
| SpecGlossVsMetalRough		| Specular Gloss EXT
| SpecularTest				| Specular EXT
| Sponza					| PBR
| StainedGlassLamp			| Refraction
| Suzanne					| PBR
| TextureCoordinateTest		| UV
| TextureEncodingTest		| Color space
| TextureLinearInterpolationTest	| Color space 	| X | Requires sRGB GPU conversion (GLES3)
| TextureSettingsTest		| Texture Settings
| TextureTransformMultiTest	| Texture Transform EXT
| TextureTransformTest		| Texture Transform EXT
| ToyCar					| Multiple
| TransmissionRoughnessTest | Refraction
| TransmissionTest			| Refraction
| Triangle					| Basic
| TriangleWithoutIndices	| Without Indices
| TwoSidedPlane				| Culling
| Unicode Test				| Char. encoding
| UnlitTest					| Unlit EXT
| VC						| Camera Animation
| VertexColorTest			| Vertex Color
| WaterBottle				| PBR

