package com.platut.scripts;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.WorldManifold;
import com.engine.components.ColliderCmp;
import com.engine.components.SpriteCmp;
import com.engine.core.BaseScript;
import com.engine.core.SceneManager;

import static com.engine.core.Constants.MeterToPixels;

/**
 * Created by conor on 18/07/16.
 */
public class PlayerController extends BaseScript {
    public PlayerController(SceneManager sceneManager, Entity entity) {
        super(sceneManager, entity);
    }

    OrthographicCamera gameCamera;
    SpriteCmp sprite;
    ColliderCmp collider;

    Sprite idle;
    Sprite run;
    int direction = 1;

    int KEY_LEFT = Input.Keys.LEFT;
    int KEY_RIGHT = Input.Keys.RIGHT;
    int KEY_JUMP = Input.Keys.SPACE;

    final static float MAX_VELOCITY = 15f;
    final static float JUMP_VELOCITY = 50f;
    boolean jump = false;
    boolean grounded = false;

    Fixture bodyFixture;
    Fixture sensorFixture;

    float stillTime = 0;
    long lastGroundTime = 0;

    @Override
    public void start() {
        gameCamera = getSceneManager().getCurrentScene().getGameCamera();
        gameCamera.position.set(getTransform().position);

        sprite = getComponent(SpriteCmp.class);
        collider = getComponent(ColliderCmp.class);

        idle = new Sprite(new TextureRegion(sprite.getTexture(), 0, 0, 16, 16));
        run = new Sprite(new TextureRegion(sprite.getTexture(), 0, 16, 16, 16));

        for (Fixture fixture : collider.body.getFixtureList()) {
            if (fixture.getUserData().equals("body")) {
                bodyFixture = fixture;
            } else if (fixture.getUserData().equals("sensor")) {
                sensorFixture = fixture;
            }
        }
    }

    @Override
    public void update(float deltaTime) {
        //System.out.println ("grounded: "+grounded);

        Vector2 vel = collider.body.getLinearVelocity();
        Vector2 pos = collider.body.getPosition();

        if (grounded) {
            lastGroundTime = System.nanoTime();
        } else {
            if (System.nanoTime() - lastGroundTime < 100000000) {
                grounded = true;
            }
        }

        if (Math.abs(vel.x) > MAX_VELOCITY) {
            vel.x = Math.signum(vel.x) * MAX_VELOCITY;
            collider.body.setLinearVelocity(vel.x, vel.y);
        }

        if (!Gdx.input.isKeyPressed(KEY_LEFT) && !Gdx.input.isKeyPressed(KEY_RIGHT)) {
            stillTime += Gdx.graphics.getDeltaTime();
            collider.body.setLinearVelocity(vel.x * 0.9f, vel.y);
        } else {
            stillTime = 0;
        }

        if (!grounded) {
            bodyFixture.setFriction(0f);
            sensorFixture.setFriction(0f);
        } else {
            if (!Gdx.input.isKeyPressed(KEY_LEFT) && !Gdx.input.isKeyPressed(KEY_RIGHT) && stillTime > 0.2) {
                bodyFixture.setFriction(100f);
                sensorFixture.setFriction(100f);
            } else {
                bodyFixture.setFriction(0.2f);
                sensorFixture.setFriction(0.2f);
            }
        }

        if (Gdx.input.isKeyPressed(KEY_LEFT) && vel.x > -MAX_VELOCITY) {
            direction = -1;
            collider.body.applyLinearImpulse(-2f, 0, pos.x, pos.y, true);
        }

        if (Gdx.input.isKeyPressed(KEY_RIGHT) && vel.x < MAX_VELOCITY) {
            direction = 1;
            collider.body.applyLinearImpulse(2f, 0, pos.x, pos.y, true);
        }

        if (jump) {
            jump = false;

            collider.body.setLinearVelocity(vel.x, 0);
            collider.body.setTransform(pos.x, pos.y + 0.01f, 0);
            collider.body.applyLinearImpulse(0, JUMP_VELOCITY, pos.x, pos.y, true);
        }

        getTransform().position.x = collider.body.getPosition().x * MeterToPixels;
        getTransform().position.y = collider.body.getPosition().y * MeterToPixels;

        gameCamera.position.lerp(getTransform().position, 0.1f);
    }

    @Override
    public void draw(SpriteBatch batch) {
        if (collider.body.getLinearVelocity().isZero(1f)) {
            idle.setX(getTransform().position.x - 8);
            idle.setY(getTransform().position.y - 8);
            idle.setScale(direction, 1);
            idle.draw(batch);
        } else {
            run.setX(getTransform().position.x - 8);
            run.setY(getTransform().position.y - 8);
            run.setScale(direction, 1);
            run.draw(batch);
        }
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == KEY_JUMP) jump = true;
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        if (keycode == KEY_JUMP) jump = false;
        return false;
    }

    @Override
    public void beginContact(Contact contact, Entity other) {
        checkGrounded(contact);
    }

    @Override
    public void endContact(Contact contact, Entity other) {
        checkGrounded(contact);
    }

    private void checkGrounded(Contact contact) {
        WorldManifold manifold = contact.getWorldManifold();
        float normalAngle = manifold.getNormal().angle();

        //System.out.println("normalAngle: "+normalAngle);
    }
}
