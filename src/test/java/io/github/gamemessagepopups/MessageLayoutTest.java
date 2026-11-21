package io.github.gamemessagepopups;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class MessageLayoutTest
{
	private static final Font FONT = new Font(Font.SANS_SERIF, Font.BOLD, 18);

	@Test
	public void wrapsWordsAndLongUnbrokenStringsWithinTheAvailableWidth()
	{
		int maxWidth = 150;
		int maxHeight = 180;
		MessageLayout shortText = MessageLayout.wrap(List.of("Look out!"), FONT, Color.RED, maxWidth, maxHeight);
		for (String text : List.of("The spell won't work on this target right now.", "X".repeat(150)))
		{
			MessageLayout wrapped = MessageLayout.wrap(List.of(text), FONT, Color.RED, maxWidth, maxHeight);
			assertTrue(wrapped.size.width <= maxWidth);
			assertTrue(wrapped.size.height <= maxHeight);
			assertTrue(wrapped.size.height > shortText.size.height);
		}
	}

	@Test
	public void fitsTheStackAtLargeFontSizesAndPrioritizesRecentMessagesInSmallViewports()
	{
		Font large = FONT.deriveFont(48f);
		List<String> messages = List.of("First ".repeat(30), "Second ".repeat(20), "Third");
		MessageLayout layout = MessageLayout.wrap(messages, large, Color.RED, 496, 318);
		assertTrue(layout.size.width <= 496);
		assertTrue(layout.size.height <= 318);

		MessageLayout oneRow = MessageLayout.wrap(messages, large, Color.RED, 496, 70);
		MessageLayout newest = MessageLayout.wrap(List.of("Third"), large, Color.RED, 496, 70);
		assertEquals(newest.size, oneRow.size);
		assertSame(MessageLayout.EMPTY, MessageLayout.wrap(messages, large, Color.RED, 496, 22));
	}

	@Test
	public void paintsBothFillAndOutlineAtTheRequestedOpacity()
	{
		MessageLayout layout = MessageLayout.wrap(List.of("Preview"), FONT, Color.RED, 360, 100);
		BufferedImage image = new BufferedImage(layout.size.width, layout.size.height, BufferedImage.TYPE_INT_ARGB);
		Graphics2D graphics = image.createGraphics();
		try
		{
			layout.paint(graphics, new float[] {0.5f});
		}
		finally
		{
			graphics.dispose();
		}

		boolean hasFill = false;
		boolean hasOutline = false;
		for (int y = 0; y < image.getHeight(); y++)
		{
			for (int x = 0; x < image.getWidth(); x++)
			{
				int pixel = image.getRGB(x, y);
				int alpha = pixel >>> 24;
				assertTrue(alpha <= 128);
				if (alpha > 0)
				{
					int red = (pixel >>> 16) & 255;
					hasFill |= red == 255;
					hasOutline |= red < 64;
				}
			}
		}
		assertTrue(hasFill);
		assertTrue(hasOutline);
	}
}
