package com.platut.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.platut.PlatutGame;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Platut-Game";
		config.width = 800;
		config.height = 480;

		new LwjglApplication(new PlatutGame(), config);
	}
}
