#version 460 core
#define lightColor vec3(1.0, 1.0, 1.0)
#define lightPos vec3(0.,0., 10000.0)
#define ambientStrength 0.1

out vec4 fragColor;
in vec2 uvs;
in vec3 fragPos;

uniform int tria;

void main()
{
    if (tria==0){
        vec3 objectColor = vec3(1.0,1.0,1.0);
        vec3 dpdu = dFdx(fragPos.xyz);
        vec3 dpdv = dFdy(fragPos.xyz);
        vec3 norm = normalize(cross(dpdu, dpdv)); //normála fragmentu

        //ambient
        vec3 ambient = ambientStrength * lightColor; //ambient složka

        //difuse
        vec3 lightDir = normalize(lightPos - fragPos);
        float diff = max(dot(norm, lightDir), 0.0);
        vec3 diffuse = diff * lightColor; //difuse složka

        vec3 result = (ambient + diffuse) * objectColor; // mix colors
        fragColor = vec4(result, 1.0);
    }else{
        fragColor = vec4(1.0,1.0,1.0,1.0);
    }
}