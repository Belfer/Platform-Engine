package com.engine.core;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.graphics.Texture;
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
import com.engine.components.ButtonCmp;
import com.engine.components.ColliderCmp;
import com.engine.components.GameObjectCmp;
import com.engine.components.MaterialCmp;
import com.engine.components.SpriteCmp;
import com.engine.components.TransformCmp;
import com.engine.tiled.TmxMapPatchLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by conor on 09/08/17.
 */
public class BaseEntityFactory implements IEntityFactory {
    SceneManager sceneManager;
    InputMultiplexer inputMultiplexer;
    World world;

    public BaseEntityFactory(SceneManager sceneManager, InputMultiplexer inputMultiplexer, World world) {
        this.sceneManager = sceneManager;
        this.inputMultiplexer = inputMultiplexer;
        this.world = world;
    }

    @Override
    public boolean buildEntity(Entity entity, String name, String type, Rectangle bounds, MapProperties properties) {
        String prefab = (String) properties.get("prefab");
        if (prefab != null) {
            TiledMap prefabMap = new TmxMapPatchLoader().load(prefab);
            MapProperties prefabProperties = prefabMap.getProperties();
            MapLayer collidersLayer = prefabMap.getLayers().get("colliders");
            MapObjects colliders = collidersLayer.getObjects();

            int tilewidth = prefabProperties.get("tilewidth", 0, Integer.class);
            int tileheight = prefabProperties.get("tileheight", 0, Integer.class);

            String image = (String) prefabProperties.get("image");
            String script = (String) prefabProperties.get("script");

            addGameObject(entity, name, type, script);
            addTransform(entity, bounds);
            addCollider(entity, bounds, colliders, tilewidth, tileheight);
            addSprite(entity, image);

        } else {
            String image = (String) properties.get("image");
            String script = (String) properties.get("script");

            addGameObject(entity, name, type, script);
            addTransform(entity, bounds);
            addSprite(entity, image);
        }
        return true;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void addGameObject(Entity entity, String name, String tag, String scriptSrc) {
        GameObjectCmp gameObject = new GameObjectCmp();
        gameObject.name = name;
        gameObject.tag = tag;

        if (!scriptSrc.isEmpty()) {
            String[] scripts = scriptSrc.split("\\s");
            for (String scr : scripts) {
                try {
                    Class scrClass = ClassReflection.forName(scr);
                    Constructor constructor = null;
                    try {
                        constructor = scrClass.getConstructor(SceneManager.class, Entity.class);
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
                    assert (object instanceof BaseScript);

                    BaseScript script = (BaseScript) object;
                    inputMultiplexer.addProcessor(script);
                    gameObject.scripts.add(script);

                } catch (ReflectionException e) {
                    String script = scr.isEmpty() ? "<empty>" : scr;
                    System.err.println("Failed to load [" + name + "] script: " + script);
                    e.printStackTrace();
                }
            }
        }

        entity.add(gameObject);
    }

    @Override
    public void addTransform(Entity entity, Rectangle bounds) {
        TransformCmp transform = new TransformCmp();
        transform.bounds = bounds;
        transform.position.x = bounds.x;
        transform.position.y = bounds.y;
        transform.position.z = 0;
        entity.add(transform);
    }

    @Override
    public void addCollider(Entity entity, Rectangle bounds, MapObjects colliders, int tilewidth, int tileheight) {
        ColliderCmp collider = new ColliderCmp();

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

            ColliderUtil.ColliderWrapper wrapper = ColliderUtil.correctShape(obj, tilewidth, tileheight, rotation);
            if (wrapper.shape != null) {
                FixtureDef fixtureDef = new FixtureDef();
                fixtureDef.density = Float.parseFloat(properties.get("density", "1f", String.class));
                fixtureDef.friction = Float.parseFloat(properties.get("friction", "0.3f", String.class));
                fixtureDef.restitution = Float.parseFloat(properties.get("restitution", "0.1f", String.class));
                fixtureDef.isSensor = Boolean.parseBoolean(properties.get("sensor", "false", String.class));

                fixtureDef.shape = wrapper.shape;
                Fixture fixture = body.createFixture(fixtureDef);
                fixture.setUserData(obj.getName());

                collider.shape = wrapper.shape;
            }
        }

        collider.body = body;
        entity.add(collider);
    }

    @Override
    public void addSprite(Entity entity, String imageSrc) {
        MaterialCmp material = new MaterialCmp();
        SpriteCmp sprite = new SpriteCmp();

        String[] images = imageSrc.split("\\s");
        for (String img : images) {
            sceneManager.getAssetManager().load(img, Texture.class);
            material.images.add(img);
        }

        entity.add(sprite);
        entity.add(material);
    }

    @Override
    public void addButton(Entity entity, Rectangle bounds, String imageSrc) {
        MaterialCmp material = new MaterialCmp();
        ButtonCmp button = new ButtonCmp();
        button.button.setBounds(bounds.x, bounds.y, bounds.width, bounds.height);

        String[] images = imageSrc.split("\\s");
        for (String img : images) {
            sceneManager.getAssetManager().load(img, Texture.class);
            material.images.add(img);
        }

        //stage.addActor (button.button);
        entity.add(button);
        entity.add(material);
    }
}
