package com.engine.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.scenes.scene2d.ui.Button;

/**
 * Created by conor on 21/07/16.
 */
public class ButtonCmp implements Component {
    public Button button = new Button();

    public static final ComponentMapper<ButtonCmp>
            Mapper = ComponentMapper.getFor(ButtonCmp.class);
}
