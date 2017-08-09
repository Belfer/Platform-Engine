package com.framework;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.framework.components.GameObjectCmp;
import com.framework.components.TransformCmp;

/**
 * Created by conor on 09/08/17.
 */

public interface IScript extends InputProcessor {
    SceneManager getSceneManager();

    Entity getEntity();

    GameObjectCmp getGameObject();

    TransformCmp getTransform();

    <T extends Component> T getComponent(Class<T> componentClass);

    void create();

    void start();

    void update(float deltaTime);

    void draw(SpriteBatch batch);

    void drawGUI(SpriteBatch batch);

    void destroy();

    void beginContact(Contact contact, Entity other);

    void endContact(Contact contact, Entity other);

    void preSolve(Contact contact, Manifold oldManifold, Entity other);

    void postSolve(Contact contact, ContactImpulse impulse, Entity other);
}
