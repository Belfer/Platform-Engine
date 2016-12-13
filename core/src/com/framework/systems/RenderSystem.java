package com.framework.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapLayers;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.framework.Mappers;
import com.framework.Script;
import com.framework.components.CGameObject;
import com.framework.components.CTransform;
import com.framework.map.ParallaxMapRenderer;

import java.util.ArrayList;

/**
 * Created by conor on 16/07/16.
 */
public class RenderSystem extends EntitySystem {
    ImmutableArray<Entity> gameObjectEntities;

    OrthographicCamera camera;
    World world;
    SpriteBatch batch;

    TiledMap map;
    ParallaxMapRenderer mapRenderer;
    Box2DDebugRenderer box2DRenderer;
    float pixelsToMeters = 1f;
    Matrix4 debugMatrix;

    boolean debug = true;

    float wWidth, wHeight;
    float vpWidth, vpHeight;

    int[] bgLayers;
    Vector2[] bgParallax;

    int[] fgLayers;
    Vector2[] fgParallax;

    public RenderSystem (OrthographicCamera camera, World world, TiledMap map, float pixelsToMeters) {
        this.camera = camera;
        this.world = world;
        this.map = map;
        batch = new SpriteBatch();

        MapProperties mapProperties = map.getProperties();
        wWidth = mapProperties.get("width", Integer.class) * mapProperties.get("tilewidth", Integer.class);
        wHeight = mapProperties.get("height", Integer.class) * mapProperties.get("tileheight", Integer.class);
        vpWidth = camera.viewportWidth * camera.zoom;
        vpHeight = camera.viewportHeight * camera.zoom;

        mapRenderer = new ParallaxMapRenderer (map, wWidth, wHeight);
        box2DRenderer = new Box2DDebugRenderer (true, true, false, true, true, true);
        this.pixelsToMeters = pixelsToMeters;

        MapLayers layers = map.getLayers();
        int gameIndex = layers.getIndex ("gm");
        ArrayList<Integer> bgIndices = new ArrayList<Integer>();
        ArrayList<Vector2> bgParallax = new ArrayList<Vector2>();

        ArrayList<Integer> fgIndices = new ArrayList<Integer>();
        ArrayList<Vector2> fgParallax = new ArrayList<Vector2>();

        for (int i=0; i<layers.getCount(); i++) {
            MapLayer layer = layers.get (i);
            MapProperties properties = layer.getProperties();
            if (layer instanceof TiledMapTileLayer || layer instanceof TiledMapImageLayer) {
                float px = Float.parseFloat(properties.get("px", "1f", String.class));
                float py = Float.parseFloat(properties.get("py", "1f", String.class));

                if (i > gameIndex) {
                    fgIndices.add(i);
                    fgParallax.add(new Vector2(px, py));

                } else if (i <= gameIndex) {
                    bgIndices.add(i);
                    bgParallax.add(new Vector2(px, py));
                }
            }
        }

        int c = 0;
        bgLayers = new int[bgIndices.size()];
        this.bgParallax = new Vector2[bgIndices.size()];
        for (int i=0; i<bgIndices.size(); i++) {
            bgLayers[c] = bgIndices.get(i);
            this.bgParallax[c++] = bgParallax.get(i);
        }

        c = 0;
        fgLayers = new int[fgIndices.size()];
        this.fgParallax = new Vector2[fgIndices.size()];
        for (int i=0; i<fgIndices.size(); i++) {
            fgLayers[c] = fgIndices.get(i);
            this.fgParallax[c++] = fgParallax.get(i);
        }
    }

    @Override
    public void addedToEngine (Engine engine) {
        gameObjectEntities = engine.getEntitiesFor(Family.all(CGameObject.class, CTransform.class).get());
    }

    @Override
    public void removedFromEngine (Engine engine) { }

    @Override
    public void update (float deltaTime) {
        camera.update ();

        if (camera.position.x-vpWidth/2 < 0) { camera.position.x = vpWidth/2; }
        if (camera.position.x+vpWidth/2 > wWidth) { camera.position.x = wWidth-vpWidth/2; }

        if (camera.position.y-vpHeight/2 < 0) { camera.position.y = vpHeight/2; }
        if (camera.position.y+vpHeight/2 > wHeight) { camera.position.y = wHeight-vpHeight/2; }

        mapRenderer.setView (camera);
        mapRenderer.render (bgLayers, bgParallax);

        batch.setProjectionMatrix (camera.combined);
        batch.begin ();

        for (Entity entity : gameObjectEntities) {
            CGameObject gameObject = Mappers.GAMEOBJECT.get(entity);

            for (Script script : gameObject.scripts) {
                script.draw (batch);
            }
        }

        batch.end();

        mapRenderer.render (fgLayers, fgParallax);

        if (debug) {
            debugMatrix = camera.combined.cpy().scale(1f / pixelsToMeters, 1f / pixelsToMeters, 0);
            box2DRenderer.render(world, debugMatrix);
        }
    }
}
