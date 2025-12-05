package com.onemillionworlds.deeptokens;

import com.jme3.app.SimpleApplication;
import com.jme3.light.PointLight;
import com.jme3.material.Material;
import com.jme3.math.ColorRGBA;
import com.jme3.math.Vector3f;
import com.jme3.scene.Geometry;
import com.jme3.scene.shape.Box;
import com.jme3.system.AppSettings;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

public class TestApplication extends SimpleApplication{


    public static void main(String[] args){
        AppSettings settings = new AppSettings(true);
        settings.setSamples(16);
        TestApplication app = new TestApplication();
        app.setDisplayStatView(false);
        app.setSettings(settings);
        app.start(); // start the game
    }

    @Override
    public void simpleInitApp() {
        cam.setFrustumPerspective(45f, (float) cam.getWidth() / cam.getHeight(), 0.01f, 100f);

        viewPort.setBackgroundColor(ColorRGBA.White);

        String imagePath = "/specialBlocks.png";
        DeepTokenBuilder deepTokenBuilder = new DeepTokenBuilder(1, 0.1f);
        deepTokenBuilder.setDirtyEdgeReduction(5);
        //deepTokenBuilder.setEdgeSimplificationEpsilon(0.75f);
        deepTokenBuilder.setEdgeTint(new ColorRGBA(0.75f, 0.75f, 0.75f, 1));

        Geometry deepToken3 = deepTokenBuilder.bufferedImageToLitGeometry(loadBufferedImageFromResources("/forestsBad.png"),  assetManager);
        //Geometry deepToken3 = deepTokenBuilder.bufferedImageToLitGeometry(loadBufferedImageFromResources("/simpleIslandAndHole.png"),  assetManager);
        atTestPosition(new Vector3f(0, 0, 0), deepToken3);

        cam.setLocation(new Vector3f(0, 0, 2));
        cam.lookAt(new Vector3f(0, 0, 0), Vector3f.UNIT_Y);
/*

        int hash = 0;
        long count = 0;
        long start = System.currentTimeMillis();
        while(true){
            Geometry test = deepTokenBuilder.bufferedImageToLitGeometry(loadBufferedImageFromResources("/weatherIcon.png"),  assetManager);
            hash += test.hashCode();
            count++;
            if (hash < -50){
                hash +=45;
                if (hash == 1){
                    System.out.println("Hash is one");
                    break;
                }

            }

            if (count % 100 == 0){
                System.out.println("Hash: " + hash + " count: " + count + " time: " + (System.currentTimeMillis()-start)/(float)count);
            }
        }


        System.out.println(hash);

*/

        //Geometry deepToken = deepTokenBuilder.bufferedImageToLitGeometry(loadBufferedImageFromResources(imagePath),  assetManager);
        //atTestPosition(new Vector3f(0, 0, 0), deepToken);
        /*
        Geometry deepToken2 = deepTokenBuilder.bufferedImageToUnshadedGeometry(loadBufferedImageFromResources("/largeWeaponMount.png"),  assetManager);
        deepToken2.setLocalTranslation(0, 0, 1);
        rootNode.attachChild(deepToken2);
        */

        Vector3f lightPosition = new Vector3f(5, 10, 10);
        PointLight pointLight = new PointLight();

        pointLight.setPosition(lightPosition);
        rootNode.addLight(pointLight);
        Geometry sun = microBox(ColorRGBA.Red, 0.5f);
        sun.setLocalTranslation(lightPosition);
        rootNode.attachChild(sun);
    }

    public void atTestPosition(Vector3f position, Geometry geometry){
        geometry.setLocalTranslation(position);
        rootNode.attachChild(geometry);

        Geometry wireFrame = geometry.clone(true);
        wireFrame.getMaterial().getAdditionalRenderState().setWireframe(true);
        wireFrame.setLocalTranslation(position.addLocal(1, 0, 0));
        //rootNode.attachChild(wireFrame);
    }

    public static BufferedImage loadBufferedImageFromResources(String address){
        try {
            InputStream stream = TestApplication.class.getResourceAsStream(address);
            if (stream == null){
                throw new RuntimeException("No resource with address " + address);
            }
            return ImageIO.read(TestApplication.class.getResourceAsStream(address));
        } catch (IOException e) {
            throw new RuntimeException("Error with file " + address, e);
        }
    }

    private Geometry microBox(ColorRGBA colorRGBA, float halfSize){
        Box b = new Box(halfSize, halfSize, halfSize);
        Geometry geom = new Geometry("debugHandBox", b);
        Material mat = new Material(assetManager,
                "Common/MatDefs/Misc/Unshaded.j3md");
        mat.setColor("Color", colorRGBA);
        geom.setMaterial(mat);
        return geom;
    }
}
