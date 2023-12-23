package com.onemillionworlds.deeptokens;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.util.List;

public class DeepTokenBuilder{

    double edgeSimplificationEpsilon = 1;

    float tokenDepth;

    float tokenWidth;

    /**
     * @param tokenWidth The width of the token (height will be implicitly determined by the image)
     * @param tokenDepth The thickness of the token
     */
    public DeepTokenBuilder(float tokenWidth, float tokenDepth){
        this.tokenWidth = tokenWidth;
        this.tokenDepth = tokenDepth;
    }

    /**
     * The edgeSimplificationEpsilon is the maximum distance between the original edge and the simplified edge.
     * the default of 1 is basically lossless (no more than 1 pixel) but can be increased to reduce the number of triangles.
     */
    public void setEdgeSimplificationEpsilon(double edgeSimplificationEpsilon){
        this.edgeSimplificationEpsilon = edgeSimplificationEpsilon;
    }

    public Mesh bufferedImageToMesh(BufferedImage image){
        // Step 1: Determine the Perimeter
        List<Point> perimeter = MooreNeighbourhood.detectPerimeter(image);

        List<Point> simplePerimeter = DouglasPeuckerLineSimplifier.simplify(perimeter, edgeSimplificationEpsilon);

        //System.out.println(perimeter.size() + " -> " + simplePerimeter.size());

        // Step 2: Triangulate the Perimeter
        List<Triangle> triangles = Triangulariser.triangulate(simplePerimeter);

        // Step 3: Create a Custom Mesh
        float imageWidth = image.getWidth();
        float imageHeight = image.getHeight();
        return MeshBuilder.createCustomMesh(triangles, simplePerimeter, imageWidth, imageHeight, tokenWidth, tokenDepth);
    }

    public Geometry bufferedImageToUnshadedGeometry(BufferedImage image, AssetManager assetManager){

        Mesh mesh = bufferedImageToMesh(image);

        // Convert BufferedImage to JME Texture
        AWTLoader loader=new AWTLoader();
        Texture texture = new Texture2D(loader.load(image, true));

        // Create material and apply texture
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", texture);

        // Create geometry and apply material
        Geometry geom = new Geometry("ImageGeometry", mesh);
        geom.setMaterial(mat);

        return geom;
    }

}
