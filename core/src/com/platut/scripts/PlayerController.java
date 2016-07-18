package com.platut.scripts;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.framework.SceneManager;
import com.framework.Script;
import com.framework.components.CSprite;

/**
 * Created by conor on 18/07/16.
 */
public class PlayerController extends Script {
    public PlayerController(SceneManager sceneManager, Entity entity) {
        super(sceneManager, entity);
    }

    OrthographicCamera gameCamera;
    CSprite sprite;

    @Override
    public void start() {
        gameCamera = getSceneManager().getScene().getGameCamera();
        sprite = getEntity().getComponent (CSprite.class);
    }

    @Override
    public void update(float deltaTime) {
        Vector3 move = new Vector3 ();

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

        move = move.nor().scl (100 * deltaTime);
        getTransform().position.add (move);

        gameCamera.position.set (getTransform().position);
    }

    @Override
    public void draw(SpriteBatch batch) {
        sprite.sprite.draw (batch);
    }
}
