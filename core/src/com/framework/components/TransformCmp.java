package com.framework.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;

/**
 * Created by conor on 16/07/16.
 */
public class TransformCmp implements Component {

    public Rectangle bounds = new Rectangle();
    public Vector3 position = new Vector3();
    public Vector2 scale = new Vector2(1f, 1f);
    public float rotation = 0f;
}
