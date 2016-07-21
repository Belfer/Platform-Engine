package com.framework.systems;

import com.badlogic.ashley.core.Engine;
import com.badlogic.ashley.core.Entity;
import com.badlogic.ashley.core.EntitySystem;
import com.badlogic.ashley.core.Family;
import com.badlogic.ashley.utils.ImmutableArray;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.framework.Mappers;
import com.framework.Script;
import com.framework.components.CGameObject;
import com.framework.components.CSprite;
import com.framework.components.CTransform;

/**
 * Created by conor on 17/07/16.
 */
public class GUISystem extends EntitySystem {
    ImmutableArray<Entity> spriteEntities;
    ImmutableArray<Entity> gameObjectEntities;

    OrthographicCamera camera;
    SpriteBatch batch;

    Stage stage;
    Table table;

    public GUISystem (OrthographicCamera camera, Stage stage) {
        this.camera = camera;
        this.stage = stage;
        batch = new SpriteBatch ();

        //stage = new Stage();
        //Gdx.input.setInputProcessor(stage);

        table = new Table();
        table.setFillParent(true);
        stage.addActor(table);

        table.setDebug(true);
    }

    @Override
    public void addedToEngine (Engine engine) {
        spriteEntities = engine.getEntitiesFor(Family.all(CTransform.class, CSprite.class).get());
        gameObjectEntities = engine.getEntitiesFor(Family.all(CGameObject.class, CTransform.class).get());
    }

    @Override
    public void removedFromEngine (Engine engine) {
        //stage.dispose();
    }

    @Override
    public void update (float deltaTime) {
        /*for (Entity entity : spriteEntities) {
            CTransform transform = Mappers.TRANSFORM.get(entity);
            CSprite sprite = Mappers.SPRITE.get(entity);
            sprite.sprite.setX (transform.position.x);
            sprite.sprite.setY (transform.position.y);
        }*/
        stage.act (deltaTime);
        stage.draw ();

        camera.update();

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        for (Entity entity : gameObjectEntities) {
            CGameObject gameObject = Mappers.GAMEOBJECT.get(entity);

            for (Script script : gameObject.scripts) {
                script.drawGUI (batch);
            }
        }

        batch.end();
    }
}
