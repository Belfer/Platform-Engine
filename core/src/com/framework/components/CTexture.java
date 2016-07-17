package com.framework.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;

/**
 * Created by conor on 17/07/16.
 */
public class CTexture implements Component {
    TextureRegion texture = null;

    public CTexture (Texture texture)
    {
        assert (texture != null);
        this.texture = new TextureRegion (texture);
    }

    public CTexture (TextureRegion texture)
    {
        assert (texture != null);
        this.texture = texture;
    }

    public TextureRegion getTexture () { return texture; }
}
