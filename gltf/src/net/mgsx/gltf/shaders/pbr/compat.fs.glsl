
// Extensions required for WebGL and some Android versions

#ifdef GLSL3
#define textureCubeLodEXT textureLod
#define texture2DLodEXT textureLod
#else
#ifdef USE_TEXTURE_LOD_EXT
#extension GL_EXT_shader_texture_lod: enable
#else
// Note : "textureCubeLod" is used for compatibility but should be "textureLod" for GLSL #version 130 (OpenGL 3.0+)
#define textureCubeLodEXT textureCubeLod
#define texture2DLodEXT texture2DLod
#endif
#endif

// required to have same precision in both shader for light structure
#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision highp float;
#else
#define MED
#define LOWP
#define HIGH
#endif

// translate GLSL 120 to 130
#ifdef GLSL3
#define varying in
out vec4 out_FragColor;
#define textureCube texture
#define texture2D texture
#else
#define out_FragColor gl_FragColor
#endif

// force unlitFlag when there is no lighting
#ifndef lightingFlag
#ifndef unlitFlag
#define unlitFlag
#endif
#endif
