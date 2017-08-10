package com.framework.core;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Created by conor on 17/07/16.
 */
public class SceneManager extends ApplicationAdapter {

    AssetManager assetManager;
    BaseScene currentScene;

    public AssetManager getAssetManager() {
        return assetManager;
    }

    public BaseScene getCurrentScene() {
        return currentScene;
    }

    public SceneManager() {
        assetManager = new AssetManager();
        currentScene = null;
    }

    @Override
    public void render() {
        if (currentScene != null) {
            Gdx.gl.glClearColor(142 / 255f, 200 / 255f, 235 / 255f, 1f);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            currentScene.render();
        }
    }

    @Override
    public void dispose() {
        currentScene.dispose();
    }

    public void setScene(String filename) {
        setScene(filename, BaseScene.class, BaseEntityFactory.class);
    }

    public void setScene(String filename, Class<?> sceneClass, Class<?> entityFactoryClass) {
        if (currentScene != null) {
            currentScene.dispose();
        }
        currentScene = getScene(filename, sceneClass, entityFactoryClass);
        currentScene.build();
        currentScene.start();
    }

    private BaseScene getScene(String filename, Class<?> sceneClass, Class<?> entityFactoryClass) {
        Object object = null;
        Constructor constructor = null;
        try {
            constructor = sceneClass.getDeclaredConstructor(SceneManager.class, TiledMap.class, Class.class);
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        }
        try {
            object = constructor.newInstance(this, new TmxMapLoader().load(filename), entityFactoryClass);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        assert (object instanceof IScene);
        return (BaseScene) object;
    }
}
