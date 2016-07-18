package com.framework;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.framework.components.CGameObject;
import com.framework.components.CTransform;

/**
 * Created by conor on 16/07/16.
 */
public abstract class Script {

    SceneManager sceneManager;
    Entity entity;

    CGameObject gameObject;
    CTransform transform;

    public Script (SceneManager sceneManager, Entity entity)
    {
        this.sceneManager = sceneManager;
        this.entity = entity;
    }

    public void create ()
    {
        gameObject = entity.getComponent (CGameObject.class);
        transform = entity.getComponent (CTransform.class);
    }

    public abstract void start ();
    public abstract void update (float deltaTime);
    public abstract void draw (SpriteBatch batch);

    public void drawGUI (SpriteBatch batch) {}
    public void destroy () {}

    public SceneManager getSceneManager() { return sceneManager; }
    public Entity getEntity() { return entity; }
    public CGameObject getGameObject () { return gameObject; }
    public CTransform getTransform () { return transform; }
}
