package com.framework.map;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by conor on 25/07/16.
 */
public class ParallaxMapRenderer extends OrthogonalTiledMapRenderer {
    OrthographicCamera camera;
    Matrix4 parallaxView = new Matrix4();
    Matrix4 parallaxCombined = new Matrix4();
    Vector3 tmp = new Vector3();
    Vector3 tmp2 = new Vector3();

    float wWidth, wHeight;
    Vector3 worldCenter;

    public ParallaxMapRenderer (TiledMap map, float wWidth, float wHeight) {
        super(map);
        this.wWidth = wWidth;
        this.wHeight = wHeight;

        worldCenter = new Vector3 (wWidth/2, wHeight/2, 0f);
    }

    @Override
    public void setView (OrthographicCamera camera) {
        this.camera = camera;
    }

    public void render (int[] layers, Vector2[] parallax) {
        if (layers.length != parallax.length) try {
            throw new Exception ("Each layer must have a pairing vector!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i=0; i<layers.length; i++) {
            MapLayer layer = map.getLayers().get(layers[i]);
            Vector2 p = parallax[i];
            if (layer.isVisible()) {
                calculateParallax (camera, p.x, p.y);

                beginRender();
                if (layer instanceof TiledMapTileLayer) {
                    renderTileLayer((TiledMapTileLayer)layer);
                } else if (layer instanceof TiledMapImageLayer) {
                    renderImageLayer((TiledMapImageLayer)layer);
                } else {
                    renderObjects(layer);
                }
                endRender();
            }
        }
    }

    private void calculateParallax (OrthographicCamera camera, float parallaxX, float parallaxY) {
        camera.update ();
        tmp.x = worldCenter.x + parallaxX * (camera.position.x - worldCenter.x);
        tmp.y = worldCenter.y + parallaxY * (camera.position.y - worldCenter.y);

        parallaxView.setToLookAt (tmp, tmp2.set(tmp).add(camera.direction), camera.up);
        parallaxCombined.set (camera.projection);
        Matrix4.mul (parallaxCombined.val, parallaxView.val);

        batch.setProjectionMatrix (parallaxCombined);
        float width = camera.viewportWidth * camera.zoom;
        float height = camera.viewportHeight * camera.zoom;
        viewBounds.set (tmp.x - width/2, tmp.y - height/2, width, height);
    }
}
