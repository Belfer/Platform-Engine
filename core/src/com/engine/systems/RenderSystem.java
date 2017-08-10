package com.engine.systems;

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
import com.engine.components.GameObjectCmp;
import com.engine.components.TransformCmp;
import com.engine.core.IScript;
import com.engine.tiled.ParallaxMapRenderer;

import java.util.ArrayList;

import static com.engine.core.Constants.PixelToMeters;

/**
 * Created by conor on 16/07/16.
 */
public class RenderSystem extends EntitySystem {
    private ImmutableArray<Entity> gameObjectEntities;

    private OrthographicCamera camera;
    private World world;
    private SpriteBatch batch;

    private TiledMap map;
    private ParallaxMapRenderer mapRenderer;
    private Box2DDebugRenderer box2DRenderer;

    private boolean debug = true;

    private float wWidth, wHeight;
    private float vpWidth, vpHeight;

    private int[] bgLayers;
    private Vector2[] bgParallax;

    private int[] fgLayers;
    private Vector2[] fgParallax;

    public RenderSystem(OrthographicCamera camera, World world, TiledMap map) {
        this.camera = camera;
        this.world = world;
        this.map = map;
        batch = new SpriteBatch();

        MapProperties mapProperties = map.getProperties();
        wWidth = mapProperties.get("width", Integer.class) * mapProperties.get("tilewidth", Integer.class);
        wHeight = mapProperties.get("height", Integer.class) * mapProperties.get("tileheight", Integer.class);
        vpWidth = camera.viewportWidth * camera.zoom;
        vpHeight = camera.viewportHeight * camera.zoom;

        mapRenderer = new ParallaxMapRenderer(map, wWidth, wHeight);
        box2DRenderer = new Box2DDebugRenderer(true, true, false, true, true, true);

        MapLayers layers = map.getLayers();
        int gameIndex = layers.getIndex("gm");
        ArrayList<Integer> bgIndices = new ArrayList<>();
        ArrayList<Vector2> bgParallax = new ArrayList<>();

        ArrayList<Integer> fgIndices = new ArrayList<>();
        ArrayList<Vector2> fgParallax = new ArrayList<>();

        for (int i = 0; i < layers.getCount(); i++) {
            MapLayer layer = layers.get(i);
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
        for (int i = 0; i < bgIndices.size(); i++) {
            bgLayers[c] = bgIndices.get(i);
            this.bgParallax[c++] = bgParallax.get(i);
        }

        c = 0;
        fgLayers = new int[fgIndices.size()];
        this.fgParallax = new Vector2[fgIndices.size()];
        for (int i = 0; i < fgIndices.size(); i++) {
            fgLayers[c] = fgIndices.get(i);
            this.fgParallax[c++] = fgParallax.get(i);
        }
    }

    @Override
    public void addedToEngine(Engine engine) {
        gameObjectEntities = engine.getEntitiesFor(Family.all(GameObjectCmp.class, TransformCmp.class).get());
    }

    @Override
    public void removedFromEngine(Engine engine) {
    }

    @Override
    public void update(float deltaTime) {
        camera.update();

        if (camera.position.x - vpWidth / 2 < 0) {
            camera.position.x = vpWidth / 2;
        }
        if (camera.position.x + vpWidth / 2 > wWidth) {
            camera.position.x = wWidth - vpWidth / 2;
        }

        if (camera.position.y - vpHeight / 2 < 0) {
            camera.position.y = vpHeight / 2;
        }
        if (camera.position.y + vpHeight / 2 > wHeight) {
            camera.position.y = wHeight - vpHeight / 2;
        }

        mapRenderer.setView(camera);
        mapRenderer.render(bgLayers, bgParallax);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (Entity entity : gameObjectEntities) {
            GameObjectCmp gameObject = GameObjectCmp.Mapper.get(entity);

            for (IScript script : gameObject.scripts) {
                script.draw(batch);
            }
        }

        batch.end();

        mapRenderer.render(fgLayers, fgParallax);

        if (debug) {
            Matrix4 debugMatrix = camera.combined.cpy().scale(1f / PixelToMeters, 1f / PixelToMeters, 0);
            box2DRenderer.render(world, debugMatrix);
        }
    }
}
