package com.onemillionworlds.deeptokens;

import com.jme3.asset.AssetManager;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.scene.Geometry;
import com.jme3.scene.Mesh;
import com.jme3.texture.Texture;
import com.jme3.texture.Texture2D;
import com.jme3.texture.plugins.AWTLoader;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class DeepTokenBuilder{

    private Optional<ColorRGBA> edgeTint = Optional.empty();

    private double edgeSimplificationEpsilon = 1;

    private float minimumSharpAngle = (float)Math.toRadians(30);

    private final float tokenDepth;

    private final float tokenWidth;

    private boolean flipY = true;

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
     * the default of 0.75 is basically lossless (no more than 1 pixel) but can be increased to reduce the number of triangles.
     */
    @SuppressWarnings("unused")
    public void setEdgeSimplificationEpsilon(double edgeSimplificationEpsilon){
        this.edgeSimplificationEpsilon = edgeSimplificationEpsilon;
    }

    /**
     * The minimumSharpAngle is the minimum angle between two adjacent edge points that will be considered a sharp edge.
     * This affects the normals that are generated (either smoothly changing or sharp).
     */
    @SuppressWarnings("unused")
    public void setMinimumSharpAngle(float minimumSharpAngle){
        this.minimumSharpAngle = minimumSharpAngle;
    }


    /**
     * By default, the edge is the same colour as the last pixel of the image. But you can tint it if you want; e.g. make
     * it a little darker or lighter than the image.
     */
    public void setEdgeTint(ColorRGBA edgeTint){
        this.edgeTint = Optional.ofNullable(edgeTint);
    }

    /**
     * Usually images have an opposite definition of Y to JME. This will flip the image vertically which is the default and
     * usually what you want. But if you have already flipped the image yourself, you can set this to false.
     * @param flipY
     */
    public void setFlipY(boolean flipY){
        this.flipY = flipY;
    }

    private static BufferedImage createFlipped(BufferedImage image)
    {
        AffineTransform at = new AffineTransform();
        at.concatenate(AffineTransform.getScaleInstance(1, -1));
        at.concatenate(AffineTransform.getTranslateInstance(0, -image.getHeight()));
        return createTransformed(image, at);
    }

    private static BufferedImage createTransformed(
            BufferedImage image, AffineTransform at)
    {
        BufferedImage newImage = new BufferedImage(
                image.getWidth(), image.getHeight(),
                BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = newImage.createGraphics();
        g.transform(at);
        g.drawImage(image, 0, 0, null);
        g.dispose();
        return newImage;
    }

    public Mesh bufferedImageToMesh(BufferedImage image){
        if (flipY) {
            image = createFlipped(image);
        }

        // Step 1: Determine the Perimeter
        List<List<Point>> perimeters = MooreNeighbourhood.detectPerimeter(image);

        List<List<Point>> simplePerimeters = DouglasPeuckerLineSimplifier.simplifyAll(perimeters, edgeSimplificationEpsilon);

        EdgeAndHoleSeparator.EdgesAndHoles edgesAndHoles = EdgeAndHoleSeparator.separatePerimeters(simplePerimeters);

        List<EdgeHoleMapper.EdgeWithContainedHoles> edgeWithContainedHoles = EdgeHoleMapper.mapHolesToEdges(edgesAndHoles.edges(), edgesAndHoles.holes());

        List<Triangle> triangles = new ArrayList<>();

        for(EdgeHoleMapper.EdgeWithContainedHoles perimeter : edgeWithContainedHoles){
            triangles.addAll(Triangulariser.triangulate(perimeter.asSinglePerimeter()));
        }

        // Step 3: Create a Custom Mesh
        float imageWidth = image.getWidth();
        float imageHeight = image.getHeight();
        return MeshBuilder.createCustomMesh(triangles, simplePerimeters, imageWidth, imageHeight, tokenWidth, tokenDepth, minimumSharpAngle, edgeTint);
    }


    public Geometry bufferedImageToUnshadedGeometry(BufferedImage image, AssetManager assetManager){

        Mesh mesh = bufferedImageToMesh(image);

        // Convert BufferedImage to JME Texture
        Texture texture = imageToTexture(image);

        // Create material and apply texture
        Material mat = new Material(assetManager, "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setTexture("ColorMap", texture);

        if(edgeTint.isPresent()){
            mat.setBoolean("VertexColor", true);
        }

        // Create geometry and apply material
        Geometry geom = new Geometry("ImageGeometry", mesh);
        geom.setMaterial(mat);

        return geom;
    }

    public Geometry bufferedImageToLitGeometry(BufferedImage image, AssetManager assetManager){

        Mesh mesh = bufferedImageToMesh(image);

        // Convert BufferedImage to JME Texture
        Texture texture = imageToTexture(image);

        // Create material and apply texture
        Material mat = new Material(assetManager, "Common/MatDefs/Light/Lighting.j3md");
        mat.setTexture("DiffuseMap", texture);

        if(edgeTint.isPresent()){
            mat.setBoolean("UseVertexColor", true);
        }

        // Create geometry and apply material
        Geometry geom = new Geometry("ImageGeometry", mesh);
        geom.setMaterial(mat);

        return geom;
    }

    Texture imageToTexture(BufferedImage image){
        AWTLoader loader=new AWTLoader();
        return new Texture2D(loader.load(ImageEdgeExpander.processImage(image, (int)Math.ceil(edgeSimplificationEpsilon)), flipY));
    }

}
