package com.onemillionworlds.deeptokens;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class MeshBuilder{


    public static Mesh createCustomMesh(List<Triangle> triangles, float imageWidth, float imageHeight, float objectDepth){
        float halfDepth = objectDepth / 2f;
        Mesh mesh = new Mesh();

        // Each triangle has 3 vertices
        List<Vector3f> vertices = new ArrayList<>(triangles.size() * 3 * 2);
        List<Vector2f> texCoords = new ArrayList<>(triangles.size() * 3 * 2);
        int[] indices = new int[triangles.size() * 3 * 2];

        //upper face
        for(int i = 0; i < triangles.size(); i++){
            Triangle t = triangles.get(i);
            // Convert 2D points to 3D vertices
            vertices.add(new Vector3f(t.a().x, t.a().y, halfDepth));
            vertices.add(new Vector3f(t.b().x, t.b().y, halfDepth));
            vertices.add(new Vector3f(t.c().x, t.c().y, halfDepth));

            // Calculate texture coordinates
            texCoords.add(new Vector2f(t.a().x / imageWidth, t.a().y / imageHeight));
            texCoords.add(new Vector2f(t.b().x / imageWidth, t.b().y / imageHeight));
            texCoords.add(new Vector2f(t.c().x / imageWidth, t.c().y / imageHeight));

            indices[i * 3] = i * 3;
            indices[i * 3 + 1] = i * 3 + 1;
            indices[i * 3 + 2] = i * 3 + 2;
        }

        //back face
        for(int i = 0; i < triangles.size(); i++){
            Triangle t = triangles.get(i);
            // Convert 2D points to 3D vertices
            vertices.add(new Vector3f(t.a().x, t.a().y, -halfDepth));
            vertices.add(new Vector3f(t.b().x, t.b().y, -halfDepth));
            vertices.add(new Vector3f(t.c().x, t.c().y, -halfDepth));

            // Calculate texture coordinates
            texCoords.add(new Vector2f(t.a().x / imageWidth, t.a().y / imageHeight));
            texCoords.add(new Vector2f(t.b().x / imageWidth, t.b().y / imageHeight));
            texCoords.add(new Vector2f(t.c().x / imageWidth, t.c().y / imageHeight));

            //reversed triangles
            indices[3*triangles.size()+i * 3] = 3*triangles.size()+i * 3 + 2;
            indices[3*triangles.size()+i * 3 + 1] =3*triangles.size()+ i * 3 + 1;
            indices[3*triangles.size()+i * 3 + 2] =3*triangles.size()+ i * 3;
        }


        // Set mesh buffers
        mesh.setBuffer(VertexBuffer.Type.Position, 3, createFloatBufferVector3(vertices));
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, createFloatBufferVector2(texCoords));

        mesh.setBuffer(VertexBuffer.Type.Index, 3, BufferUtils.createIntBuffer(indices));

        mesh.updateBound();
        return mesh;
    }

    /**
     * Generate a new FloatBuffer using the given array of Vector3f objects. The
     * FloatBuffer will be 3 * data.length long and contain the vector data as
     * data[0].x, data[0].y, data[0].z, data[1].x... etc.
     *
     * @param data
     *            array of Vector3f objects to place into a new FloatBuffer
     * @return a new direct, flipped FloatBuffer, or null if data was null
     */
    public static FloatBuffer createFloatBufferVector3(List<Vector3f> data) {
        if (data == null) {
            return null;
        }
        FloatBuffer buff = BufferUtils.createFloatBuffer(3 * data.size());
        for (Vector3f element : data) {
            if (element != null) {
                buff.put(element.x).put(element.y).put(element.z);
            } else {
                buff.put(0).put(0).put(0);
            }
        }
        buff.flip();
        return buff;
    }

    /**
     * Generate a new FloatBuffer using the given array of Vector2f objects. The
     * FloatBuffer will be 2 * data.length long and contain the vector data as
     * data[0].x, data[0].y, data[1].x... etc.
     *
     * @param data
     *            array of Vector2f objects to place into a new FloatBuffer
     * @return a new direct, flipped FloatBuffer, or null if data was null
     */
    public static FloatBuffer createFloatBufferVector2(List<Vector2f> data) {
        if (data == null) {
            return null;
        }
        FloatBuffer buff = BufferUtils.createFloatBuffer(2 * data.size());
        for (Vector2f element : data) {
            if (element != null) {
                buff.put(element.x).put(element.y);
            } else {
                buff.put(0).put(0);
            }
        }
        buff.flip();
        return buff;
    }
}
