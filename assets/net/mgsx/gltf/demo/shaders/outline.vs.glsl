#ifdef GL_ES
#define LOWP lowp
#define MED mediump
#define HIGH highp
precision mediump float;
#else
#define MED
#define LOWP
#define HIGH
#endif

attribute vec4 a_position;
attribute vec2 a_texCoord0;

uniform mat4 u_projTrans;
uniform vec2 u_size;

varying MED vec2 v_texCoords0;
varying MED vec2 v_texCoords1;
varying MED vec2 v_texCoords2;
varying MED vec2 v_texCoords3;
varying MED vec2 v_texCoords4;

void main(){
    v_texCoords0 = a_texCoord0 + vec2(0.0, -1.0 / u_size.y);
    v_texCoords1 = a_texCoord0 + vec2(-1.0 / u_size.x, 0.0);
    v_texCoords2 = a_texCoord0;
    v_texCoords3 = a_texCoord0 + vec2(1.0 / u_size.x, 0.0);
    v_texCoords4 = a_texCoord0 + vec2(0.0, 1.0 / u_size.y);

	gl_Position = u_projTrans * a_position;
}