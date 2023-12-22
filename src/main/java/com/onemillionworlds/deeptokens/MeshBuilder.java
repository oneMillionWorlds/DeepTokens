package com.onemillionworlds.deeptokens;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

import java.util.List;

public class MeshBuilder{


    public static Mesh createCustomMesh(List<Triangle> triangles, float imageWidth, float imageHeight, float objectDepth){
        float halfDepth = objectDepth / 2f;
        Mesh mesh = new Mesh();

        // Each triangle has 3 vertices
        Vector3f[] vertices = new Vector3f[triangles.size() * 3 * 2];
        Vector2f[] texCoords = new Vector2f[triangles.size() * 3 * 2];
        int[] indices = new int[triangles.size() * 3 * 2];

        //upper face
        for(int i = 0; i < triangles.size(); i++){
            Triangle t = triangles.get(i);
            // Convert 2D points to 3D vertices
            vertices[i * 3] = new Vector3f(t.a().x, t.a().y, halfDepth);
            vertices[i * 3 + 1] = new Vector3f(t.b().x, t.b().y, halfDepth);
            vertices[i * 3 + 2] = new Vector3f(t.c().x, t.c().y, halfDepth);

            // Calculate texture coordinates
            texCoords[i * 3] = new Vector2f(t.a().x / imageWidth, t.a().y / imageHeight);
            texCoords[i * 3 + 1] = new Vector2f(t.b().x / imageWidth, t.b().y / imageHeight);
            texCoords[i * 3 + 2] = new Vector2f(t.c().x / imageWidth, t.c().y / imageHeight);

            indices[i * 3] = i * 3;
            indices[i * 3 + 1] = i * 3 + 1;
            indices[i * 3 + 2] = i * 3 + 2;
        }

        //back face
        for(int i = 0; i < triangles.size(); i++){
            Triangle t = triangles.get(i);
            // Convert 2D points to 3D vertices
            vertices[3*triangles.size()+i * 3] = new Vector3f(t.a().x, t.a().y, -halfDepth);
            vertices[3*triangles.size()+i * 3 + 1] = new Vector3f(t.b().x, t.b().y, -halfDepth);
            vertices[3*triangles.size()+i * 3 + 2] = new Vector3f(t.c().x, t.c().y, -halfDepth);

            // Calculate texture coordinates
            texCoords[3*triangles.size()+i * 3] = new Vector2f(t.a().x / imageWidth, t.a().y / imageHeight);
            texCoords[3*triangles.size()+i * 3 + 1] = new Vector2f(t.b().x / imageWidth, t.b().y / imageHeight);
            texCoords[3*triangles.size()+i * 3 + 2] = new Vector2f(t.c().x / imageWidth, t.c().y / imageHeight);

            //reversed triangles
            indices[3*triangles.size()+i * 3] = 3*triangles.size()+i * 3 + 2;
            indices[3*triangles.size()+i * 3 + 1] =3*triangles.size()+ i * 3 + 1;
            indices[3*triangles.size()+i * 3 + 2] =3*triangles.size()+ i * 3;
        }


        // Set mesh buffers
        mesh.setBuffer(VertexBuffer.Type.Position, 3, BufferUtils.createFloatBuffer(vertices));
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, BufferUtils.createFloatBuffer(texCoords));

        mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indices));

        mesh.updateBound();
        return mesh;
    }
}
