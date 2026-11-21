package io.github.gamemessagepopups;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.util.List;
import net.runelite.client.ui.overlay.OverlayLayer;
import net.runelite.client.ui.overlay.OverlayPosition;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class GameMessageOverlayTest
{
	private static final Rectangle FIXED_VIEWPORT = new Rectangle(4, 4, 512, 334);
	private static final Dimension FIXED_CANVAS = new Dimension(765, 503);

	@Test
	public void preparesADraggablePreviewInsideFixedViewportEvenAtTheBottomRight()
	{
		GameMessagePopupsConfig config = new GameMessagePopupsConfig()
		{
			@Override
			public boolean preview()
			{
				return true;
			}

			@Override
			public int horizontalPercent()
			{
				return 100;
			}

			@Override
			public int verticalPercent()
			{
				return 100;
			}
		};
		GameMessageOverlay overlay = new GameMessageOverlay(null, config);
		overlay.prepare(0, FIXED_VIEWPORT, FIXED_CANVAS);

		assertTrue(overlay.isMovable());
		assertFalse(overlay.isSnappable());
		assertEquals(OverlayLayer.ABOVE_WIDGETS, overlay.getLayer());
		assertEquals(OverlayPosition.DYNAMIC, overlay.getPosition());
		Rectangle safeArea = overlay.getParentBounds();
		assertEquals(new Rectangle(12, 12, 496, 318), safeArea);
		assertTrue(safeArea.contains(overlay.getBounds()));
		assertEquals(safeArea.getMaxX(), overlay.getBounds().getMaxX(), 0);
		assertEquals(safeArea.getMaxY(), overlay.getBounds().getMaxY(), 0);

		Graphics2D graphics = new BufferedImage(FIXED_CANVAS.width, FIXED_CANVAS.height, BufferedImage.TYPE_INT_ARGB).createGraphics();
		try
		{
			assertEquals(overlay.getBounds().getSize(), overlay.render(graphics));
		}
		finally
		{
			graphics.dispose();
		}
	}

	@Test
	public void updatesAnAlreadyVisibleMessageWhenTheFamilyAndBoldSettingChange()
	{
		GameMessageOverlay overlay = new GameMessageOverlay(null, new GameMessagePopupsConfig() {});
		String message = "You cannot attack this target";
		overlay.messages.add(message, 0, new MessageQueue.Timing(0, 1600, 650));
		overlay.prepare(0, FIXED_VIEWPORT, FIXED_CANVAS);
		Dimension initial = overlay.getBounds().getSize();

		overlay.updateAppearance(new GameMessagePopupsConfig()
		{
			@Override
			public PopupFontFamily fontFamily()
			{
				return PopupFontFamily.MONOSPACED;
			}

			@Override
			public boolean bold()
			{
				return false;
			}
		});
		overlay.prepare(0, FIXED_VIEWPORT, FIXED_CANVAS);

		Font font = new Font(Font.MONOSPACED, Font.PLAIN, 18);
		Dimension expected = MessageLayout.wrap(List.of(message), font, new Color(255, 83, 74), true, false, 420, 318).size;
		assertNotEquals(initial, expected);
		assertEquals(expected, overlay.getBounds().getSize());
	}

	@Test
	public void leavesTheNativePositionToRuneLiteAndHasNoHitBoxWhenIdle()
	{
		GameMessageOverlay overlay = new GameMessageOverlay(null, new GameMessagePopupsConfig() {});
		Point savedLocation = new Point(80, 65);
		overlay.setPreferredLocation(savedLocation);
		overlay.getBounds().setLocation(130, 90);
		overlay.prepare(0, FIXED_VIEWPORT, FIXED_CANVAS);

		assertEquals(savedLocation, overlay.getPreferredLocation());
		assertEquals(new Point(130, 90), overlay.getBounds().getLocation());
		assertTrue(overlay.getBounds().isEmpty());
		assertNull(overlay.render(null));
	}
}
