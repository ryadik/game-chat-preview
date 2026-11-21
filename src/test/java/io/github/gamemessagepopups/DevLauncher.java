package io.github.gamemessagepopups;

import net.runelite.client.RuneLite;
import net.runelite.client.externalplugins.ExternalPluginManager;

public final class DevLauncher
{
	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws Exception
	{
		ExternalPluginManager.loadBuiltin(GameMessagePopupsPlugin.class);
		RuneLite.main(args);
	}
}
