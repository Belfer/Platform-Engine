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
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapRenderer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.World;
import com.framework.Mappers;
import com.framework.Script;
import com.framework.components.CGameObject;
import com.framework.components.CSprite;
import com.framework.components.CTransform;

import java.util.ArrayList;

/**
 * Created by conor on 16/07/16.
 */
public class RenderSystem extends EntitySystem {
    ImmutableArray<Entity> gameObjectEntities;

    OrthographicCamera camera;
    World world;
    SpriteBatch batch;

    TiledMapRenderer mapRenderer;
    Box2DDebugRenderer box2DRenderer;

    int[] bgLayers;
    int[] fgLayers;

    public RenderSystem (OrthographicCamera camera, World world, TiledMap map) {
        this.camera = camera;
        this.world = world;
        batch = new SpriteBatch();

        mapRenderer = new OrthogonalTiledMapRenderer (map, 1f);
        box2DRenderer = new Box2DDebugRenderer (true, true, false, true, true, true);

        MapLayers layers = map.getLayers();
        int gameIndex = layers.getIndex ("gm");
        ArrayList<Integer> bgcounter = new ArrayList<Integer>();
        ArrayList<Integer> fgcounter = new ArrayList<Integer>();

        for (int i=0; i<layers.getCount(); i++) {
            MapLayer layer = layers.get (i);
            if (layer instanceof TiledMapTileLayer || layer instanceof TiledMapImageLayer) {
                if (i > gameIndex) {
                    fgcounter.add(i);
                } else if (i <= gameIndex) {
                    bgcounter.add(i);
                }
            }
        }

        int c = 0;
        bgLayers = new int[bgcounter.size()];
        for (int i : bgcounter) bgLayers[c++] = i;

        c = 0;
        fgLayers = new int[fgcounter.size()];
        for (int i : fgcounter) fgLayers[c++] = i;
    }

    @Override
    public void addedToEngine (Engine engine) {
        gameObjectEntities = engine.getEntitiesFor(Family.all(CGameObject.class, CTransform.class).get());
    }

    @Override
    public void removedFromEngine (Engine engine) { }

    @Override
    public void update (float deltaTime) {
        /*for (Entity entity : engine.getEntitiesFor(Family.all(CTransform.class, CSprite.class).get())) {
            CTransform transform = Mappers.TRANSFORM.get(entity);
            CSprite sprite = Mappers.SPRITE.get(entity);
            sprite.sprite.setX (transform.position.x);
            sprite.sprite.setY (transform.position.y);
        }*/

        camera.update ();
        mapRenderer.setView (camera);
        mapRenderer.render (bgLayers);

        batch.setProjectionMatrix (camera.combined);
        batch.begin ();

        for (Entity entity : gameObjectEntities) {
            CGameObject gameObject = Mappers.GAMEOBJECT.get(entity);

            for (Script script : gameObject.scripts) {
                script.draw (batch);
            }
        }

        batch.end();

        mapRenderer.render (fgLayers);

        box2DRenderer.render (world, camera.combined);
    }
}
