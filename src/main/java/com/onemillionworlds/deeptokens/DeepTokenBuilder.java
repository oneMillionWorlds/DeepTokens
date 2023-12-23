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

    public static Geometry bufferedImageToGeometry(BufferedImage image, float imageDepth, AssetManager assetManager){

        AWTLoader loader=new AWTLoader();

        // Step 1: Determine the Perimeter
        List<Point> perimeter = MooreNeighbourhood.detectPerimeter(image);

        List<Point> simplePerimeter = DouglasPeuckerLineSimplifier.simplify(perimeter, 5);

        System.out.println(perimeter.size() + " -> " + simplePerimeter.size());

        // Step 2: Triangulate the Perimeter
        List<Triangle> triangles = Triangulariser.triangulate(simplePerimeter);

        // Step 3: Create a Custom Mesh
        float imageWidth = image.getWidth();
        float imageHeight = image.getHeight();
        Mesh mesh = MeshBuilder.createCustomMesh(triangles, simplePerimeter, imageWidth, imageHeight, imageDepth);

        // Convert BufferedImage to JME Texture
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
