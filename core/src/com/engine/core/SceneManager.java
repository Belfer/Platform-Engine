package com.engine.core;

import com.badlogic.gdx.assets.AssetManager;
import com.engine.tiled.TiledEntityFactory;
import com.engine.tiled.TiledScene;

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
        setScene(filename, TiledScene.class, TiledEntityFactory.class);
    }

    public void setScene(String filename, Class<?> sceneClass, Class<?> entityFactoryClass) {
        if (currentScene != null) {
            currentScene.dispose();
        }
        currentScene = newScene(sceneClass);
        currentScene.init(filename, this, entityFactoryClass);
        currentScene.build();
        currentScene.start();
    }

    private IScene newScene(Class<?> sceneClass) {
        Object object = null;
        try {
            object = sceneClass.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        assert (object instanceof IScene);
        return (IScene) object;
    }
}
