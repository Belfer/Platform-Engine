package com.engine.tiled;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.engine.core.IScript;
import com.engine.core.SceneManager;
import com.engine.core.components.GameObjectCmp;
import com.engine.core.components.TransformCmp;

/**
 * Created by conor on 16/07/16.
 */
public abstract class TiledScript implements IScript {
    SceneManager sceneManager;
    Entity entity;

    GameObjectCmp gameObject;
    TransformCmp transform;

    public TiledScript(SceneManager sceneManager, Entity entity) {
        this.sceneManager = sceneManager;
        this.entity = entity;
    }

    @Override
    public SceneManager getSceneManager() {
        return sceneManager;
    }

    @Override
    public Entity getEntity() {
        return entity;
    }

    @Override
    public GameObjectCmp getGameObject() {
        return gameObject;
    }

    @Override
    public TransformCmp getTransform() {
        return transform;
    }

    @Override
    public <T extends Component> T getComponent(Class<T> componentClass) {
        return entity.getComponent(componentClass);
    }

    @Override
    public void create() {
        gameObject = entity.getComponent(GameObjectCmp.class);
        transform = entity.getComponent(TransformCmp.class);
    }

    @Override
    public abstract void start();

    @Override
    public abstract void update(float deltaTime);

    @Override
    public abstract void draw(SpriteBatch batch);

    @Override
    public void drawGUI(SpriteBatch batch) {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void beginContact(Contact contact, Entity other) {
    }

    @Override
    public void endContact(Contact contact, Entity other) {
    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold, Entity other) {
    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse, Entity other) {
    }

    @Override
    public boolean keyDown(int keycode) {
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }
}
