package io.github.gamemessagepopups;

import java.awt.Color;
import java.awt.Font;
import net.runelite.client.config.Config;
import net.runelite.client.config.ConfigGroup;
import net.runelite.client.config.ConfigItem;
import net.runelite.client.config.ConfigSection;
import net.runelite.client.config.Range;

@ConfigGroup(GameMessagePopupsConfig.GROUP)
public interface GameMessagePopupsConfig extends Config
{
	String GROUP = "gameMessagePopups";
	int MIN_FONT_SIZE = 10;
	int MAX_FONT_SIZE = 48;
	int MAX_DELAY_MS = 5000;
	int MIN_HOLD_MS = 100;
	int MAX_HOLD_MS = 10000;
	int MAX_FADE_MS = 5000;
	int MIN_TEXT_WIDTH = 120;
	int MAX_TEXT_WIDTH = 900;

	@ConfigSection(name = "Position", description = "Place the popups in the game viewport", position = 0)
	String POSITION = "position";

	@ConfigSection(name = "Appearance", description = "Style and size of the popup text", position = 1)
	String APPEARANCE = "appearance";

	@ConfigSection(name = "Timing", description = "Delay, hold, and fade for new messages", position = 2)
	String TIMING = "timing";

	@ConfigSection(name = "Messages", description = "Which game messages to show", position = 3)
	String MESSAGES = "messages";

	@ConfigItem(keyName = "preview", name = "Preview", description = "Show a persistent sample while logged in. Hold Alt and drag it to position the overlay.", position = 0, section = POSITION)
	default boolean preview()
	{
		return false;
	}

	@Range(max = 100)
	@ConfigItem(keyName = "horizontalPercent", name = "Horizontal position (%)", description = "Default text center, from left (0) to right (100). Alt + right-click the overlay to restore this position after dragging.", position = 1, section = POSITION)
	default int horizontalPercent()
	{
		return 50;
	}

	@Range(max = 100)
	@ConfigItem(keyName = "verticalPercent", name = "Vertical position (%)", description = "Default distance from the top of the viewport. Applies when the overlay has no saved drag position.", position = 2, section = POSITION)
	default int verticalPercent()
	{
		return 18;
	}

	@ConfigItem(keyName = "fontFamily", name = "Font family", description = "A font family installed on your computer, or a Java family such as SansSerif, Serif or Monospaced.", position = 0, section = APPEARANCE)
	default String fontFamily()
	{
		return Font.SANS_SERIF;
	}

	@Range(min = MIN_FONT_SIZE, max = MAX_FONT_SIZE)
	@ConfigItem(keyName = "fontSize", name = "Font size", description = "Bold text size in game pixels.", position = 1, section = APPEARANCE)
	default int fontSize()
	{
		return 18;
	}

	@ConfigItem(keyName = "textColor", name = "Text color", description = "Popup text color.", position = 2, section = APPEARANCE)
	default Color textColor()
	{
		return new Color(255, 83, 74);
	}

	@Range(min = MIN_TEXT_WIDTH, max = MAX_TEXT_WIDTH)
	@ConfigItem(keyName = "maxTextWidth", name = "Maximum width (px)", description = "Wrap text at this width or the available viewport width, whichever is smaller.", position = 3, section = APPEARANCE)
	default int maxTextWidth()
	{
		return 420;
	}

	@Range(max = MAX_DELAY_MS)
	@ConfigItem(keyName = "delayMs", name = "Delay (ms)", description = "Minimum wait after a message arrives.", position = 0, section = TIMING)
	default int delayMs()
	{
		return 0;
	}

	@Range(min = MIN_HOLD_MS, max = MAX_HOLD_MS)
	@ConfigItem(keyName = "holdMs", name = "Display time (ms)", description = "How long text stays fully visible once it appears.", position = 1, section = TIMING)
	default int holdMs()
	{
		return 1600;
	}

	@Range(max = MAX_FADE_MS)
	@ConfigItem(keyName = "fadeMs", name = "Fade-out (ms)", description = "How long text takes to fade after the display time. Set to 0 for an instant disappearance.", position = 2, section = TIMING)
	default int fadeMs()
	{
		return 650;
	}

	@ConfigItem(keyName = "showFilteredMessages", name = "Show filterable messages", description = "Include repetitive game messages of type SPAM, even when Game Chat is filtered.", position = 0, section = MESSAGES)
	default boolean showFilteredMessages()
	{
		return false;
	}

	@ConfigItem(keyName = "showExamines", name = "Show examines", description = "Include item, NPC and object descriptions from Examine.", position = 1, section = MESSAGES)
	default boolean showExamines()
	{
		return false;
	}
}
