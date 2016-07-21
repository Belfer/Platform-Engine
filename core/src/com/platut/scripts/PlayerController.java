package com.platut.scripts;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
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

    Sprite idle;

    @Override
    public void start() {
        gameCamera = getSceneManager().getScene().getGameCamera();
        gameCamera.position.set (getTransform().position);

        sprite = getEntity().getComponent (CSprite.class);
        idle = new Sprite (new TextureRegion(sprite.sprite.getTexture(), 0, 0, 16, 16));
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

        gameCamera.position.lerp (getTransform().position, 0.1f);
    }

    @Override
    public void draw(SpriteBatch batch) {
        idle.setX (getTransform().position.x);
        idle.setY (getTransform().position.y);
        idle.draw (batch);
    }
}
