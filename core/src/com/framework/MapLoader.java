package com.framework;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.objects.RectangleMapObject;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapImageLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;

import static com.framework.Constants.PixelToMeters;

/**
 * Created by conor on 09/08/17.
 */

public class MapLoader {
    public static void loadMap(Engine engine, IEntityFactory entityFactory, TiledMap map, World world, ColliderLoader colliderLoader) {
        for (MapLayer mapLayer : map.getLayers()) {
            if (mapLayer instanceof TiledMapTileLayer) {

                // Load tile collider from colliderLoader map
                TiledMapTileLayer tileLayer = (TiledMapTileLayer) mapLayer;
                for (int i = 0; i < tileLayer.getWidth(); i++) {
                    for (int j = 0; j < tileLayer.getHeight(); j++) {

                        TiledMapTileLayer.Cell cell = tileLayer.getCell(i, j);
                        if (cell != null) {
                            ColliderLoader.ColliderWrapper wrapper = colliderLoader.colliders.get(cell.getTile().getId());

                            if (wrapper != null) {
                                if (wrapper.shape != null) {
                                    BodyDef bodyDef = new BodyDef();
                                    bodyDef.type = BodyDef.BodyType.StaticBody;

                                    int tilewidth = 16;
                                    int tileheight = 16;
                                    float x = (i * tilewidth * PixelToMeters) + wrapper.offset.x;
                                    float y = (j * tileheight * PixelToMeters) + wrapper.offset.y;
                                    bodyDef.position.set(x, y);

                                    Body body = world.createBody(bodyDef);

                                    FixtureDef fixtureDef = new FixtureDef();
                                    fixtureDef.shape = wrapper.shape;
                                    fixtureDef.density = 1f;
                                    fixtureDef.friction = 0.5f;
                                    fixtureDef.restitution = 0f;
                                    body.createFixture(fixtureDef);
                                }
                            }
                        }
                    }
                }

            } else if (mapLayer instanceof TiledMapImageLayer) {
                // TODO add image colliderLoader?

            } else {
                for (MapObject obj : mapLayer.getObjects()) {
                    Entity entity = new Entity();

                    Rectangle bounds = ((RectangleMapObject) obj).getRectangle();
                    MapProperties properties = obj.getProperties();
                    String name = obj.getName();
                    String type = (String) properties.get("type");
                    boolean autoLoad = false;

                    entityFactory.buildEntity(entity, name, type, bounds, properties);
                    engine.addEntity(entity);
                }
            }
        }
    }
}
