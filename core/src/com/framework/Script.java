package com.framework;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.framework.components.CGameObject;
import com.framework.components.CTransform;

/**
 * Created by conor on 16/07/16.
 */
public abstract class Script implements InputProcessor {

    SceneManager sceneManager;
    Entity entity;

    CGameObject gameObject;
    CTransform transform;

    public Script (SceneManager sceneManager, Entity entity)
    {
        this.sceneManager = sceneManager;
        this.entity = entity;
    }

    public SceneManager getSceneManager() { return sceneManager; }
    public Entity getEntity() { return entity; }
    public CGameObject getGameObject () { return gameObject; }
    public CTransform getTransform () { return transform; }
    public <T extends Component> T getComponent (Class<T> componentClass) {
        return entity.getComponent(componentClass);
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

    public void beginContact (Contact contact, Entity other) { }

    public void endContact (Contact contact, Entity other) { }

    public void preSolve (Contact contact, Manifold oldManifold, Entity other) { }

    public void postSolve (Contact contact, ContactImpulse impulse, Entity other) { }

    @Override
    public boolean keyDown (int keycode) { return false; }

    @Override
    public boolean keyUp (int keycode) { return false; }

    @Override
    public boolean keyTyped (char character) { return false; }

    @Override
    public boolean touchDown (int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchUp (int screenX, int screenY, int pointer, int button) { return false; }

    @Override
    public boolean touchDragged (int screenX, int screenY, int pointer) { return false; }

    @Override
    public boolean mouseMoved (int screenX, int screenY) { return false; }

    @Override
    public boolean scrolled (int amount) { return false; }
}
