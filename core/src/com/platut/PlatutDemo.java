package com.platut;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.engine.core.BaseGame;

public class PlatutDemo extends BaseGame {
    @Override
    public void create() {
        getSceneManager().setScene("levels/level1.tmx", ExScene.class, ExEntityFactory.class);
    }

    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT | GL20.GL_DEPTH_BUFFER_BIT);
        getSceneManager().getCurrentScene().render();
    }
}
