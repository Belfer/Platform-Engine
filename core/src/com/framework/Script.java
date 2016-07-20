package com.framework;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.framework.components.CGameObject;
import com.framework.components.CTransform;

/**
 * Created by conor on 16/07/16.
 */
public abstract class Script implements IScript {

    SceneManager sceneManager;
    Entity entity;

    CGameObject gameObject;
    CTransform transform;

    public Script (SceneManager sceneManager, Entity entity)
    {
        this.sceneManager = sceneManager;
        this.entity = entity;
    }

    @Override
    public void create ()
    {
        gameObject = entity.getComponent (CGameObject.class);
        transform = entity.getComponent (CTransform.class);
    }

    @Override
    public abstract void start ();

    @Override
    public abstract void update (float deltaTime);

    @Override
    public abstract void draw (SpriteBatch batch);

    @Override
    public void drawGUI (SpriteBatch batch) {}

    @Override
    public void destroy () {}

    public SceneManager getSceneManager() { return sceneManager; }
    public Entity getEntity() { return entity; }
    public CGameObject getGameObject () { return gameObject; }
    public CTransform getTransform () { return transform; }
}
