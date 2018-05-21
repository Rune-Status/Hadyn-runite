#version 330 core

uniform mat4 projection;
uniform mat4 model;
uniform vec3 color;

layout(location = 0) in vec3 pos;

out vec3 fragment_color;

void main(){
  gl_Position = projection * model * vec4(pos, 1.0);
  fragment_color = color;
}