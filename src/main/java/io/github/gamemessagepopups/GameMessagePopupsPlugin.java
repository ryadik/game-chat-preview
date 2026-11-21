package io.github.gamemessagepopups;

import com.google.inject.Provides;
import java.awt.Rectangle;
import java.util.regex.Pattern;
import javax.inject.Inject;
import net.runelite.api.ChatMessageType;
import net.runelite.api.Client;
import net.runelite.api.GameState;
import net.runelite.api.events.BeforeRender;
import net.runelite.api.events.ChatMessage;
import net.runelite.api.events.GameStateChanged;
import net.runelite.client.config.ConfigManager;
import net.runelite.client.eventbus.Subscribe;
import net.runelite.client.events.ConfigChanged;
import net.runelite.client.events.ProfileChanged;
import net.runelite.client.plugins.Plugin;
import net.runelite.client.plugins.PluginDescriptor;
import net.runelite.client.ui.overlay.OverlayManager;
import net.runelite.client.util.Text;

@PluginDescriptor(
	name = "Game Message Popups",
	description = "Shows game chat messages as fading text over the viewport",
	tags = {"game", "chat", "notifications", "overlay"}
)
public final class GameMessagePopupsPlugin extends Plugin
{
	private static final String LEGACY_FONT_FAMILY = "fontFamily";
	private static final Pattern WHITESPACE = Pattern.compile("\\s+");

	@Inject
	private Client client;

	@Inject
	private OverlayManager overlayManager;

	@Inject
	private ConfigManager configManager;

	@Inject
	private GameMessagePopupsConfig config;

	private volatile GameMessageOverlay overlay;

	@Provides
	GameMessagePopupsConfig provideConfig(ConfigManager configManager)
	{
		return configManager.getConfig(GameMessagePopupsConfig.class);
	}

	@Override
	protected void startUp()
	{
		migrateFontFamily();
		GameMessageOverlay newOverlay = new GameMessageOverlay(this, config);
		overlayManager.add(newOverlay);
		overlay = newOverlay;
	}

	@Override
	protected void shutDown()
	{
		GameMessageOverlay oldOverlay = overlay;
		overlay = null;
		overlayManager.remove(oldOverlay);
	}

	@Subscribe
	public void onChatMessage(ChatMessage event)
	{
		GameMessageOverlay current = overlay;
		if (current == null || !isInWorld(client.getGameState()) || event.getMessage() == null
			|| !accepts(event.getType(), config))
		{
			return;
		}

		String message = cleanMessage(event.getMessage());
		if (!message.isEmpty())
		{
			current.messages.add(message, System.nanoTime(), new MessageQueue.Timing(
				config.delayMs(), config.holdMs(), config.fadeMs()));
		}
	}

	@Subscribe
	public void onBeforeRender(BeforeRender event)
	{
		GameMessageOverlay current = overlay;
		if (current != null && client.getGameState() == GameState.LOGGED_IN)
		{
			Rectangle viewport = new Rectangle(client.getViewportXOffset(), client.getViewportYOffset(),
				client.getViewportWidth(), client.getViewportHeight());
			current.prepare(System.nanoTime(), viewport, client.getRealDimensions());
		}
	}

	@Subscribe
	public void onConfigChanged(ConfigChanged event)
	{
		GameMessageOverlay current = overlay;
		if (current != null && GameMessagePopupsConfig.GROUP.equals(event.getGroup()))
		{
			current.updateAppearance(config);
		}
	}

	@Subscribe
	public void onProfileChanged(ProfileChanged event)
	{
		migrateFontFamily();
		GameMessageOverlay current = overlay;
		if (current != null)
		{
			current.updateAppearance(config);
		}
	}

	private void migrateFontFamily()
	{
		String group = GameMessagePopupsConfig.GROUP;
		String previousFamily = configManager.getConfiguration(group, LEGACY_FONT_FAMILY);
		if (previousFamily == null)
		{
			return;
		}

		if (config.fontFamily() == PopupFontFamily.SANS_SERIF)
		{
			PopupFontFamily choice = PopupFontFamily.fromSavedName(previousFamily);
			if (choice == PopupFontFamily.CUSTOM && config.customFontFamily().isBlank())
			{
				configManager.setConfiguration(group, GameMessagePopupsConfig.CUSTOM_FONT_FAMILY, previousFamily.strip());
			}
			configManager.setConfiguration(group, GameMessagePopupsConfig.FONT_FAMILY, choice);
		}
		configManager.unsetConfiguration(group, LEGACY_FONT_FAMILY);
	}

	@Subscribe
	public void onGameStateChanged(GameStateChanged event)
	{
		GameMessageOverlay current = overlay;
		if (current != null && !isInWorld(event.getGameState()))
		{
			current.messages.clear();
		}
	}

	static boolean accepts(ChatMessageType type, GameMessagePopupsConfig config)
	{
		if (type == null)
		{
			return false;
		}

		switch (type)
		{
			case GAMEMESSAGE:
			case ENGINE:
				return true;
			case SPAM:
				return config.showFilteredMessages();
			case ITEM_EXAMINE:
			case NPC_EXAMINE:
			case OBJECT_EXAMINE:
				return config.showExamines();
			default:
				return false;
		}
	}

	static String cleanMessage(String message)
	{
		String plainText = Text.unescapeJagex(message).replace('\u00a0', ' ');
		return WHITESPACE.matcher(plainText).replaceAll(" ").strip();
	}

	private static boolean isInWorld(GameState state)
	{
		return state == GameState.LOGGED_IN || state == GameState.LOADING;
	}
}
