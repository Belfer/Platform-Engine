package com.engine.tiled;

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
    private OrthographicCamera camera;
    private Matrix4 parallaxView = new Matrix4();
    private Matrix4 parallaxCombined = new Matrix4();
    private Vector3 worldCenter;

    public ParallaxMapRenderer(TiledMap map, float width, float height) {
        super(map);
        worldCenter = new Vector3(width * 0.5f, height * 0.5f, 0f);
    }

    @Override
    public void setView(OrthographicCamera camera) {
        this.camera = camera;
    }

    public void render(int[] layers, Vector2[] parallax) {
        if (layers.length != parallax.length) try {
            throw new Exception("Each layer must have a pairing vector!");
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < layers.length; i++) {
            MapLayer layer = map.getLayers().get(layers[i]);
            Vector2 p = parallax[i];
            if (layer.isVisible()) {
                calculateParallax(camera, p.x, p.y);

                beginRender();
                if (layer instanceof TiledMapTileLayer) {
                    renderTileLayer((TiledMapTileLayer) layer);
                } else if (layer instanceof TiledMapImageLayer) {
                    renderImageLayer((TiledMapImageLayer) layer);
                } else {
                    renderObjects(layer);
                }
                endRender();
            }
        }
    }

    private void calculateParallax(OrthographicCamera camera, float parallaxX, float parallaxY) {
        camera.update();

        Vector3 camPos = new Vector3();
        camPos.x = worldCenter.x + parallaxX * (camera.position.x - worldCenter.x);
        camPos.y = worldCenter.y + parallaxY * (camera.position.y - worldCenter.y);

        Vector3 lookAt = camPos.cpy().add(camera.direction);

        parallaxView.setToLookAt(camPos, lookAt, camera.up);
        parallaxCombined.set(camera.projection);
        Matrix4.mul(parallaxCombined.val, parallaxView.val);

        batch.setProjectionMatrix(parallaxCombined);
        float width = camera.viewportWidth * camera.zoom;
        float height = camera.viewportHeight * camera.zoom;
        viewBounds.set(camPos.x - (width * .5f), camPos.y - (height * .5f), width, height);
    }
}
