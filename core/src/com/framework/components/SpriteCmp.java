package com.framework.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;

/**
 * Created by conor on 17/07/16.
 */
public class SpriteCmp implements Component {
    public Sprite sprite = new Sprite();

    public Texture getTexture() {
        return sprite.getTexture();
    }

    public float getWidth() {
        return sprite.getWidth();
    }

    public float getHeight() {
        return sprite.getHeight();
    }
}
