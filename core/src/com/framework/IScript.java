package com.framework;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;

/**
 * Created by conor on 19/07/16.
 */
public interface IScript {
    void create ();
    void start ();
    void update (float deltaTime);
    void draw (SpriteBatch batch);
    void drawGUI (SpriteBatch batch);
    void destroy ();
}
