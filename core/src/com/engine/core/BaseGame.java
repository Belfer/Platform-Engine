package com.engine.core;

import com.badlogic.gdx.ApplicationAdapter;

/**
 * Created by conor on 10/08/17.
 */

public abstract class BaseGame extends ApplicationAdapter {
    private SceneManager sceneManager = new SceneManager();

    public SceneManager getSceneManager() {
        return sceneManager;
    }
}
