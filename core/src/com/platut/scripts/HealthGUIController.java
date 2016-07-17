package com.platut.scripts;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.framework.SceneManager;
import com.framework.Script;
import com.framework.components.CMaterial;
import com.framework.components.CTexture;
import com.framework.components.CTransform;
import com.sun.prism.Texture;

/**
 * Created by conor on 16/07/16.
 */
public class HealthGUIController extends Script {

    CTexture texture;

    public HealthGUIController(SceneManager manager, Entity entity) { super(manager, entity); }

    @Override
    public void start () {
        texture = getEntity ().getComponent (CTexture.class);
    }

    @Override
    public void update() {
        Vector2 move = new Vector2 ();

        if (Gdx.input.isKeyPressed(Input.Keys.UP)) {
            move.y = 1;
        } else if (Gdx.input.isKeyPressed(Input.Keys.DOWN)) {
            move.y = -1;
        }

        if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            move.x = -1;
        } else if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            move.x = 1;
        }

        getTransform().position.add (move.nor().scl(100 * Gdx.graphics.getDeltaTime()));
    }

    @Override
    public void render(SpriteBatch batch) {
        batch.draw (texture.getTexture(), getTransform().position.x, getTransform().position.y);
    }
}
