package com.framework.core;

import com.badlogic.gdx.ApplicationAdapter;

/**
 * Created by conor on 10/08/17.
 */

public class BaseGame extends ApplicationAdapter {
    private SceneManager sceneManager = new SceneManager();

    public SceneManager getSceneManager() {
        return sceneManager;
    }
}