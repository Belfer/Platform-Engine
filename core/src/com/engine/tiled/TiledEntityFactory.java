package com.engine.tiled;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.maps.MapLayer;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.maps.MapObjects;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.utils.reflect.ClassReflection;
import com.badlogic.gdx.utils.reflect.ReflectionException;
import com.engine.core.Constants;
import com.engine.core.EntityWrapper;
import com.engine.core.IEntityFactory;
import com.engine.core.IScript;
import com.engine.core.SceneManager;
import com.engine.core.components.ColliderCmp;
import com.engine.core.components.GameObjectCmp;
import com.engine.core.components.MaterialCmp;
import com.engine.core.components.SpriteCmp;
import com.engine.core.components.TransformCmp;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by conor on 09/08/17.
 */
public class TiledEntityFactory implements IEntityFactory {
    private SceneManager sceneManager;
    private InputMultiplexer inputMultiplexer;
    private World world;

    @Override
    public void init(SceneManager sceneManager, InputMultiplexer inputMultiplexer, World world) {
        this.sceneManager = sceneManager;
        this.inputMultiplexer = inputMultiplexer;
        this.world = world;
    }

    @Override
    public Entity buildEntity(EntityWrapper entityWrapper) {
        TiledEntityWrapper wrapper = (TiledEntityWrapper) entityWrapper;
        Entity entity = new Entity();

        String prefab = (String) wrapper.properties.get("prefab");
        assert (prefab != null);

        TiledMap prefabMap = new TmxMapPatchLoader().load(prefab);
        MapProperties prefabProperties = prefabMap.getProperties();
        MapLayer collidersLayer = prefabMap.getLayers().get("colliders");
        MapObjects colliders = collidersLayer.getObjects();

        int tilewidth = prefabProperties.get("tilewidth", 0, Integer.class);
        int tileheight = prefabProperties.get("tileheight", 0, Integer.class);

        String script = (String) prefabProperties.get("gameobject.script");
        String texture = (String) prefabProperties.get("material.texture");
        String origin = (String) prefabProperties.get("sprite.origin");
        String region = (String) prefabProperties.get("sprite.region");

        addGameObject(wrapper.entity, wrapper.name, wrapper.type, script);
        addTransform(wrapper.entity, wrapper.bounds);
        addCollider(wrapper.entity, wrapper.bounds, colliders, tilewidth, tileheight);
        addMaterial(wrapper.entity, texture, "");
        addSprite(wrapper.entity, origin, region);

        return entity;
    }

    private void addGameObject(Entity entity, String name, String tag, String script) {
        GameObjectCmp gameObject = new GameObjectCmp();
        gameObject.name = name;
        gameObject.tag = tag;

        if (script != null) {
            try {
                Class scriptClass = ClassReflection.forName(script);
                Constructor constructor = null;
                try {
                    constructor = scriptClass.getConstructor(SceneManager.class, Entity.class);
                } catch (NoSuchMethodException e) {
                    e.printStackTrace();
                }

                assert (constructor != null);
                Object object = null;
                try {
                    object = constructor.newInstance(sceneManager, entity);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                    e.printStackTrace();
                }

                assert (object != null);
                assert (object instanceof IScript);

                gameObject.script = (IScript) object;
                inputMultiplexer.addProcessor(gameObject.script);

            } catch (ReflectionException e) {
                System.err.println("Failed to construct script: " + script);
                e.printStackTrace();
            }
        }

        entity.add(gameObject);
    }

    private void addTransform(Entity entity, Rectangle bounds) {
        TransformCmp transform = new TransformCmp();
        transform.bounds = bounds;
        transform.position.x = bounds.x;
        transform.position.y = bounds.y;
        transform.position.z = 0;
        entity.add(transform);
    }

    private void addCollider(Entity entity, Rectangle bounds, MapObjects colliders, int tilewidth, int tileheight) {
        ColliderCmp colliderCmp = new ColliderCmp();

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.DynamicBody;
        bodyDef.fixedRotation = true;
        bodyDef.position.set(bounds.x * Constants.PixelToMeters, bounds.y * Constants.PixelToMeters);
        Body body = world.createBody(bodyDef);
        body.setUserData(entity);

        for (MapObject obj : colliders) {
            MapProperties properties = obj.getProperties();
            //int tiled = obj.getProperties().get ("tileId", 0, Integer.class);
            float rotation = properties.get("rotation", 0f, Float.class);

            TiledCollider collider = TiledUtil.buildCollider(obj, tilewidth, tileheight, rotation);
            if (collider.shape != null) {
                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.density = Float.parseFloat(properties.get("density", "1f", String.class));
                fixtureDef.friction = Float.parseFloat(properties.get("friction", "0.3f", String.class));
                fixtureDef.restitution = Float.parseFloat(properties.get("restitution", "0.1f", String.class));
                fixtureDef.isSensor = Boolean.parseBoolean(properties.get("sensor", "false", String.class));

                fixtureDef.shape = collider.shape;
                Fixture fixture = body.createFixture(fixtureDef);
                fixture.setUserData(obj.getName());
            }
        }

        colliderCmp.body = body;
        entity.add(colliderCmp);
    }

    private void addMaterial(Entity entity, String texture, String color) {
        MaterialCmp materialCmp = new MaterialCmp();

        if (texture != null) {
            materialCmp.texture = texture;
        }

        if (color != null) {
            materialCmp.color = Color.WHITE;
        }

        entity.add(materialCmp);
    }

    private void addSprite(Entity entity, String origin, String region) {
        SpriteCmp spriteCmp = new SpriteCmp();

        if (origin != null) {
            String[] originStrs = region.split("\\s*,\\s*");
            assert (originStrs.length == 2);

            spriteCmp.origin.x = Float.parseFloat(originStrs[0]);
            spriteCmp.origin.y = Float.parseFloat(originStrs[1]);
        }

        if (region != null) {
            String[] regionStrs = region.split("\\s*,\\s*");
            assert (regionStrs.length == 4);

            spriteCmp.region.x = Float.parseFloat(regionStrs[0]);
            spriteCmp.region.y = Float.parseFloat(regionStrs[1]);
            spriteCmp.region.width = Float.parseFloat(regionStrs[2]);
            spriteCmp.region.height = Float.parseFloat(regionStrs[3]);
        }

        entity.add(spriteCmp);
    }
}
