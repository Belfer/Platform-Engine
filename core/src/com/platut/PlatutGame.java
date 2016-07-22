package com.platut;

import com.badlogic.ashley.core.Entity;
import com.badlogic.gdx.maps.MapProperties;
import com.badlogic.gdx.math.Rectangle;
import com.framework.IEntityFactory;
import com.framework.Scene;
import com.framework.SceneManager;

public class PlatutGame extends SceneManager {

	class EntityFactory implements IEntityFactory
	{
		@Override
		public boolean buildEntity (Entity entity, String name, String type, Rectangle bounds, MapProperties properties) {
			return false;
		}
	}

	@Override
	public void create () {
		setEntityFactory (new EntityFactory ());
		setScene (new Scene (this, "levels/level1.tmx"));
	}
}
