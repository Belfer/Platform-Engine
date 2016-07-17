package com.framework;

import com.badlogic.ashley.core.ComponentMapper;
import com.framework.components.CGameObject;
import com.framework.components.CMaterial;
import com.framework.components.CTexture;
import com.framework.components.CTransform;

/**
 * Created by conor on 16/07/16.
 */
public class Mappers {

    public static final ComponentMapper<CGameObject>
            GAMEOBJECT = ComponentMapper.getFor(CGameObject.class);

    public static final ComponentMapper<CTransform>
            TRANSFORM = ComponentMapper.getFor(CTransform.class);

    public static final ComponentMapper<CMaterial>
            MATERIAL = ComponentMapper.getFor(CMaterial.class);

    public static final ComponentMapper<CTexture>
            TEXTURE = ComponentMapper.getFor(CTexture.class);
}
