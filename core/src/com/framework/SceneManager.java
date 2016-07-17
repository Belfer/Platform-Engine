package com.framework;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.GL20;

/**
 * Created by conor on 17/07/16.
 */
public class SceneManager extends ApplicationAdapter {

    AssetManager assetManager;
    Scene currentScene;

    public SceneManager()
    {
        assetManager = new AssetManager();
        currentScene = null;
    }

    @Override
    public void render () {
        if (currentScene != null) {
            Gdx.gl.glClearColor(0f, 0f, 0f, 1);
            Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
            currentScene.update();
        }
    }

    public void setScene (Scene scene) { currentScene = scene; }
    public Scene getScene () { return currentScene; }

    public AssetManager getAssetManager () { return assetManager; }
}
