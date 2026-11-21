package io.github.gamemessagepopups;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.util.ArrayList;
import java.util.List;
import net.runelite.client.ui.overlay.Overlay;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;

final class GameMessageOverlay extends Overlay
{
	private static final int VIEWPORT_MARGIN = 8;
	private static final List<String> PREVIEW = List.of("You can't do that right now.");

	final MessageQueue messages = new MessageQueue();
	private final float[] opacities = new float[MessageQueue.MAX_VISIBLE];
	private volatile Appearance appearance;
	private volatile Rectangle safeViewport = new Rectangle();
	private List<MessageQueue.Popup> currentPopups = List.of();
	private List<String> currentTexts = List.of();
	private List<String> layoutTexts = List.of();
	private Font layoutFont;
	private int layoutWidth;
	private int layoutHeight;
	private MessageLayout layout = MessageLayout.EMPTY;
	private Color layoutColor;

	GameMessageOverlay(GameMessagePopupsPlugin plugin, GameMessagePopupsConfig config)
	{
		super(plugin);
		setPosition(OverlayPosition.DYNAMIC);
		setLayer(OverlayLayer.ABOVE_WIDGETS);
		setMovable(true);
		setSnappable(false);
		updateAppearance(config);
	}

	void updateAppearance(GameMessagePopupsConfig config)
	{
		appearance = new Appearance(config);
	}

	void prepare(long now, Rectangle gameViewport, Dimension canvas)
	{
		Appearance current = appearance;
		Rectangle viewport = insetViewport(gameViewport, canvas);
		safeViewport = viewport;
		List<MessageQueue.Popup> live = messages.advance(now);

		List<String> texts = PREVIEW;
		if (current.preview)
		{
			opacities[0] = 1f;
		}
		else
		{
			if (live != currentPopups)
			{
				currentPopups = live;
				currentTexts = new ArrayList<>(live.size());
				for (MessageQueue.Popup popup : live)
				{
					currentTexts.add(popup.text);
				}
			}
			texts = currentTexts;
			for (int index = 0; index < live.size(); index++)
			{
				opacities[index] = live.get(index).opacity(now);
			}
		}

		int width = Math.min(viewport.width, current.maxWidth);
		int height = viewport.height;
		if (!texts.equals(layoutTexts) || !current.font.equals(layoutFont) || !current.color.equals(layoutColor)
			|| width != layoutWidth || height != layoutHeight)
		{
			layout = MessageLayout.wrap(texts, current.font, current.color, width, height);
			layoutTexts = texts;
			layoutFont = current.font;
			layoutColor = current.color;
			layoutWidth = width;
			layoutHeight = height;
		}

		// RuneLite clamps the position before render(), using the size already in getBounds().
		Rectangle bounds = getBounds();
		bounds.setSize(layout.size);
		if (getPreferredLocation() == null)
		{
			int x = viewport.x + Math.round(viewport.width * current.horizontalPercent / 100f) - bounds.width / 2;
			int y = viewport.y + Math.round(viewport.height * current.verticalPercent / 100f);
			bounds.setLocation(
				clamp(x, viewport.x, viewport.x + viewport.width - bounds.width),
				clamp(y, viewport.y, viewport.y + viewport.height - bounds.height));
		}
	}

	@Override
	public Rectangle getParentBounds()
	{
		return safeViewport;
	}

	@Override
	public Dimension render(Graphics2D graphics)
	{
		if (layout == MessageLayout.EMPTY)
		{
			return null;
		}

		Graphics2D textGraphics = (Graphics2D) graphics.create();
		try
		{
			layout.paint(textGraphics, opacities);
		}
		finally
		{
			textGraphics.dispose();
		}
		return layout.size;
	}

	private static Rectangle insetViewport(Rectangle gameViewport, Dimension canvas)
	{
		Rectangle viewport = gameViewport.intersection(new Rectangle(canvas));
		if (viewport.width <= 2 * VIEWPORT_MARGIN || viewport.height <= 2 * VIEWPORT_MARGIN)
		{
			return new Rectangle();
		}
		viewport.grow(-VIEWPORT_MARGIN, -VIEWPORT_MARGIN);
		return viewport;
	}

	private static int clamp(int value, int min, int max)
	{
		return Math.max(min, Math.min(max, value));
	}

	private static final class Appearance
	{
		private final Font font;
		private final Color color;
		private final int maxWidth;
		private final int horizontalPercent;
		private final int verticalPercent;
		private final boolean preview;

		private Appearance(GameMessagePopupsConfig config)
		{
			String family = config.fontFamily().strip();
			font = new Font(family.isEmpty() ? Font.SANS_SERIF : family, Font.BOLD,
				clamp(config.fontSize(), GameMessagePopupsConfig.MIN_FONT_SIZE, GameMessagePopupsConfig.MAX_FONT_SIZE));
			color = config.textColor();
			maxWidth = clamp(config.maxTextWidth(), GameMessagePopupsConfig.MIN_TEXT_WIDTH, GameMessagePopupsConfig.MAX_TEXT_WIDTH);
			horizontalPercent = clamp(config.horizontalPercent(), 0, 100);
			verticalPercent = clamp(config.verticalPercent(), 0, 100);
			preview = config.preview();
		}
	}
}
