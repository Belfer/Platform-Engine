package com.engine.core.components;

import com.badlogic.ashley.core.Component;
import com.badlogic.ashley.core.ComponentMapper;
import com.badlogic.gdx.scenes.scene2d.ui.Widget;

/**
 * Created by conor on 21/07/16.
 */
public class WidgetCmp implements Component {
    public Widget widget = null;

    public static final ComponentMapper<WidgetCmp>
            Mapper = ComponentMapper.getFor(WidgetCmp.class);
}
