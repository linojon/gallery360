uniform mat4 u_MVP;

attribute vec4 a_Position;
attribute vec2 a_TexCoordinate;

varying vec3 v_Position;
varying vec2 v_TexCoordinate;

void main() {
   // Pass through the texture coordinate.
   v_TexCoordinate = a_TexCoordinate;

   // Multiply the vertex by the matrix to get the final point in normalized screen coordinates.
   gl_Position = u_MVP * a_Position;
}
