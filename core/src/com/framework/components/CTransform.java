package com.framework.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

/**
 * Created by conor on 16/07/16.
 */
public class CTransform implements Component {

    public Rectangle bounds = new Rectangle ();
    public Vector2 position = new Vector2 ();
    public Vector2 scale = new Vector2 ();
    public float rotation = 0;
}
