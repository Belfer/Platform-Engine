package com.platut.desktop;

import com.badlogic.gdx.backends.lwjgl.LwjglApplication;
import com.badlogic.gdx.backends.lwjgl.LwjglApplicationConfiguration;
import com.platut.PlatutDemo;

public class DesktopLauncher {
	public static void main (String[] arg) {
		LwjglApplicationConfiguration config = new LwjglApplicationConfiguration();
		config.title = "Platut Demo";
		config.width = 800;
		config.height = 480;
		config.vSyncEnabled = true;

		new LwjglApplication(new PlatutDemo(), config);
	}
}
