package com.engine.core;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by conor on 17/07/16.
 */
public class SceneManager {
    AssetManager assetManager;
    IScene currentScene;

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public IScene getCurrentScene() {
        return currentScene;
    }

    public SceneManager() {
        assetManager = new AssetManager();
        currentScene = null;
    }

    public void setScene(String filename) {
        setScene(filename, BaseScene.class, BaseEntityFactory.class);
    }

    public void setScene(String filename, Class<?> sceneClass, Class<?> entityFactoryClass) {
        if (currentScene != null) {
            currentScene.dispose();
        }
        currentScene = newScene(filename, sceneClass, entityFactoryClass);
        currentScene.build();
        currentScene.start();
    }

    private IScene newScene(String filename, Class<?> sceneClass, Class<?> entityFactoryClass) {
        Object object = null;
        Constructor constructor = null;
        try {
            constructor = sceneClass.getDeclaredConstructor(SceneManager.class, TiledMap.class, Class.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            object = constructor.newInstance(this, new TmxMapLoader().load(filename), entityFactoryClass);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        assert (object instanceof IScene);
        return (IScene) object;
    }
}
