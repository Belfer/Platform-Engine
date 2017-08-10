package com.framework.core;

import com.badlogic.ashley.core.EntityListener;
import com.badlogic.gdx.graphics.OrthographicCamera;

/**
 * Created by conor on 09/08/17.
 */

public interface IScene extends EntityListener {
    void build();

    void start();

    void render();

    void dispose();

    OrthographicCamera getGameCamera();

    OrthographicCamera getGUICamera();
}
