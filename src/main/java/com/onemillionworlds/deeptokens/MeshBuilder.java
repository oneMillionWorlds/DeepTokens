package com.onemillionworlds.deeptokens;

import com.jme3.math.Vector2f;
import com.jme3.math.Vector3f;
import com.jme3.scene.Mesh;
import com.jme3.scene.VertexBuffer;
import com.jme3.util.BufferUtils;

import java.awt.Point;
import java.nio.FloatBuffer;
import java.util.ArrayList;
import java.util.List;

public class MeshBuilder{


    public static Mesh createCustomMesh(List<Triangle> triangles, List<List<Point>> edges, float imageWidthPixels, float imageHeightPixels, float objectWidth, float objectDepth) {
        float halfDepth = objectDepth / 2f;
        Mesh mesh = new Mesh();

        float pixelScale = objectWidth / imageWidthPixels;

        // Initialize lists for vertices, texture coordinates, and indices
        List<Vector3f> vertices = new ArrayList<>();
        List<Vector3f> normals = new ArrayList<>();
        List<Vector2f> texCoords = new ArrayList<>();
        List<Integer> indices = new ArrayList<>();

        // Add upper and lower face vertices, texture coords, and indices
        addFace(triangles, vertices, normals, texCoords, indices, halfDepth, imageWidthPixels, imageHeightPixels, pixelScale, true); // upper face
        addFace(triangles, vertices, normals, texCoords, indices, -halfDepth, imageWidthPixels, imageHeightPixels, pixelScale, false); // lower face

        // Add edge vertices, texture coords, and indices
        for(List<Point> edge : edges){
            addEdges(edge, vertices, normals, texCoords, indices, halfDepth, imageWidthPixels, pixelScale, imageHeightPixels);
        }

        // Convert lists to arrays
        int[] indicesArray = indices.stream().mapToInt(i -> i).toArray();

        // Set mesh buffers
        mesh.setBuffer(VertexBuffer.Type.Position, 3, createFloatBufferVector3(vertices));
        mesh.setBuffer(VertexBuffer.Type.Normal, 3, createFloatBufferVector3(normals));
        mesh.setBuffer(VertexBuffer.Type.TexCoord, 2, createFloatBufferVector2(texCoords));
        mesh.setBuffer(VertexBuffer.Type.Index, 1, BufferUtils.createIntBuffer(indicesArray));
        mesh.updateBound();

        return mesh;
    }

    private static void addFace(List<Triangle> triangles, List<Vector3f> vertices, List<Vector3f> normals, List<Vector2f> texCoords, List<Integer> indices, float z, float imageWidth, float imageHeight, float pixelScale, boolean isUpper) {
        int startIdx = vertices.size();
        Vector3f normal = new Vector3f(0, 0, isUpper ? 1 : -1);
        for (Triangle t : triangles) {
            // Add vertices
            vertices.add(new Vector3f(t.a().x*pixelScale, t.a().y*pixelScale, z));
            vertices.add(new Vector3f(t.b().x*pixelScale, t.b().y*pixelScale, z));
            vertices.add(new Vector3f(t.c().x*pixelScale, t.c().y*pixelScale, z));

            normals.add(normal);
            normals.add(normal);
            normals.add(normal);

            // Add texture coordinates
            texCoords.add(new Vector2f(t.a().x / imageWidth, t.a().y / imageHeight));
            texCoords.add(new Vector2f(t.b().x / imageWidth, t.b().y / imageHeight));
            texCoords.add(new Vector2f(t.c().x / imageWidth, t.c().y / imageHeight));

            // Add indices
            if (isUpper) {
                indices.add(startIdx);
                indices.add(startIdx + 1);
                indices.add(startIdx + 2);
            } else {
                // Reverse the triangle winding for the lower face
                indices.add(startIdx + 2);
                indices.add(startIdx + 1);
                indices.add(startIdx);
            }
            startIdx += 3;
        }
    }

    private static void addEdges(List<Point> edges, List<Vector3f> vertices, List<Vector3f> normals, List<Vector2f> texCoords, List<Integer> indices, float halfDepth, float imageWidth, float pixelScale, float imageHeight) {
        int startIdx = vertices.size();

        List<Vector3f> normalsForEdge = calculateOutwardNormals(edges);

        for (int i = 0; i < edges.size(); i++) {
            Point current = edges.get(i);
            Point next = edges.get((i + 1) % edges.size()); // Wrap around to the first point

            // Define vertices for the edge quad
            Vector3f v1 = new Vector3f(current.x*pixelScale, current.y*pixelScale, -halfDepth);
            Vector3f v2 = new Vector3f(current.x*pixelScale, current.y*pixelScale, halfDepth);
            Vector3f v3 = new Vector3f(next.x*pixelScale, next.y*pixelScale, halfDepth);
            Vector3f v4 = new Vector3f(next.x*pixelScale, next.y*pixelScale, -halfDepth);

            // Add vertices
            vertices.add(v1);
            vertices.add(v2);
            vertices.add(v3);
            vertices.add(v4);

            normals.add(normalsForEdge.get(i));
            normals.add(normalsForEdge.get(i));
            normals.add(normalsForEdge.get((i+1)% edges.size()));
            normals.add(normalsForEdge.get((i+1)% edges.size()));

            // Texture coordinates for the edge based on the last pixel color
            Vector2f texCoord1 = new Vector2f(current.x / imageWidth, current.y / imageHeight);
            Vector2f texCoord2 = new Vector2f(current.x / imageWidth, current.y / imageHeight);
            Vector2f texCoord3 = new Vector2f(next.x / imageWidth, next.y / imageHeight);
            Vector2f texCoord4 = new Vector2f(next.x / imageWidth, next.y / imageHeight);

            // Add texture coordinates
            texCoords.add(texCoord1);
            texCoords.add(texCoord2);
            texCoords.add(texCoord3);
            texCoords.add(texCoord4);

            // Add two triangles to form the quad
            indices.add(startIdx + 2);
            indices.add(startIdx + 1);
            indices.add(startIdx);

            indices.add(startIdx + 3);
            indices.add(startIdx + 2);
            indices.add(startIdx);

            startIdx += 4;
        }
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

    public static List<Vector3f> calculateOutwardNormals(List<Point> edges) {
        List<Vector3f> normals = new ArrayList<>();
        int size = edges.size();

        for (int i = 0; i < size; i++) {
            Point current = edges.get(i);
            Point prev = edges.get(i > 0 ? i - 1 : size - 1);
            Point next = edges.get((i + 1) % size);

            Vector3f normalPrev = calculateNormal(prev, current);
            Vector3f normalNext = calculateNormal(current, next);

            // Average of the two normals
            Vector3f averageNormal = new Vector3f((normalPrev.x + normalNext.x) / 2f, (normalPrev.y + normalNext.y) / 2f, 0);
            normals.add(averageNormal);
        }

        return normals;
    }

    private static Vector3f calculateNormal(Point from, Point to) {
        // Normal calculation for a segment
        float dx = to.x - from.x;
        float dy = to.y - from.y;

        // Perpendicular in 2D (swap and negate one component)
        return new Vector3f(-dy, dx, 0).normalizeLocal();
    }
}
