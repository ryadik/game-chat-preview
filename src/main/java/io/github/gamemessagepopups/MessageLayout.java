package io.github.gamemessagepopups;

import java.awt.AlphaComposite;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.LineMetrics;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.text.AttributedString;
import java.util.ArrayList;
import java.util.List;

final class MessageLayout
{
	private static final int PADDING = 4;
	private static final int MAX_LINES_PER_MESSAGE = 4;
	private static final String ELLIPSIS = "…";
	private static final int SHADOW_OFFSET = 2;
	private static final Color SHADOW_COLOR = new Color(0, 0, 0, 175);
	private static final Color OUTLINE_COLOR = new Color(22, 9, 9);
	private static final BasicStroke OUTLINE = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND);
	private static final FontRenderContext FONT_CONTEXT = new FontRenderContext(null, true, true);
	static final MessageLayout EMPTY = new MessageLayout(new Dimension(), List.of());

	final Dimension size;
	private final List<Block> blocks;

	private MessageLayout(Dimension size, List<Block> blocks)
	{
		this.size = size;
		this.blocks = blocks;
	}

	static MessageLayout wrap(List<String> messages, Font font, Color color, boolean outline, boolean shadow, int maxWidth, int maxHeight)
	{
		if (messages.isEmpty() || maxWidth <= 2 * PADDING)
		{
			return EMPTY;
		}

		LineMetrics metrics = font.getLineMetrics("Ag", FONT_CONTEXT);
		int lineHeight = (int) Math.ceil(metrics.getHeight()) + 2;
		int availableRows = (maxHeight - 2 * PADDING) / lineHeight;
		if (availableRows < 1)
		{
			return EMPTY;
		}

		int top = 0;
		int width = 0;
		List<Block> blocks = new ArrayList<>();
		int first = Math.max(0, messages.size() - availableRows);
		for (int index = first; index < messages.size(); index++)
		{
			int subsequentMessages = messages.size() - index - 1;
			int rowLimit = Math.min(MAX_LINES_PER_MESSAGE, availableRows - subsequentMessages);
			List<TextLayout> lines = wrapMessage(messages.get(index), font, maxWidth - 2 * PADDING, rowLimit);
			BufferedImage image = renderBlock(lines, color, outline, shadow, maxWidth, lineHeight, metrics.getAscent());
			blocks.add(new Block(index, top, image));
			width = Math.max(width, image.getWidth());
			top += lines.size() * lineHeight;
			availableRows -= lines.size();
		}

		return new MessageLayout(new Dimension(width, top + 2 * PADDING), blocks);
	}

	void paint(Graphics2D graphics, float[] opacities)
	{
		for (Block block : blocks)
		{
			float opacity = opacities[block.messageIndex];
			if (opacity > 0)
			{
				graphics.setComposite(AlphaComposite.SrcOver.derive(opacity));
				graphics.drawImage(block.image, (size.width - block.image.getWidth()) / 2, block.top, null);
			}
		}
	}

	private static BufferedImage renderBlock(List<TextLayout> lines, Color color, boolean outline, boolean shadow,
		int maxWidth, int lineHeight, float ascent)
	{
		int textWidth = 0;
		for (TextLayout line : lines)
		{
			textWidth = Math.max(textWidth, (int) Math.ceil(line.getBounds().getWidth()));
		}

		int width = Math.min(maxWidth, textWidth + 2 * PADDING);
		int height = lines.size() * lineHeight + 2 * PADDING;
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB_PRE);
		Graphics2D graphics = image.createGraphics();
		try
		{
			graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			graphics.setStroke(OUTLINE);
			float baseline = PADDING + ascent;
			for (TextLayout line : lines)
			{
				double x = width / 2.0 - line.getBounds().getCenterX();
				Shape glyph = line.getOutline(AffineTransform.getTranslateInstance(x, baseline));
				if (shadow)
				{
					graphics.setColor(SHADOW_COLOR);
					graphics.translate(SHADOW_OFFSET, SHADOW_OFFSET);
					graphics.fill(glyph);
					graphics.translate(-SHADOW_OFFSET, -SHADOW_OFFSET);
				}
				if (outline)
				{
					graphics.setColor(OUTLINE_COLOR);
					graphics.draw(glyph);
				}
				graphics.setColor(color);
				graphics.fill(glyph);
				baseline += lineHeight;
			}
		}
		finally
		{
			graphics.dispose();
		}
		return image;
	}

	private static List<TextLayout> wrapMessage(String text, Font font, int width, int rowLimit)
	{
		AttributedString attributed = new AttributedString(text);
		attributed.addAttribute(TextAttribute.FONT, font);
		LineBreakMeasurer measurer = new LineBreakMeasurer(attributed.getIterator(), FONT_CONTEXT);
		List<TextLayout> wrapped = new ArrayList<>(rowLimit);
		while (measurer.getPosition() < text.length() && wrapped.size() < rowLimit)
		{
			int start = measurer.getPosition();
			TextLayout line = measurer.nextLayout(width);
			if (wrapped.size() == rowLimit - 1 && measurer.getPosition() < text.length())
			{
				measurer.setPosition(start);
				TextLayout ellipsis = new TextLayout(ELLIPSIS, font, FONT_CONTEXT);
				float prefixWidth = width - ellipsis.getAdvance();
				if (prefixWidth > 0)
				{
					int end = measurer.nextOffset(prefixWidth);
					line = new TextLayout(text.substring(start, end).stripTrailing() + ELLIPSIS, font, FONT_CONTEXT);
				}
				else
				{
					line = ellipsis;
				}
			}
			wrapped.add(line);
		}
		return wrapped;
	}

	private static final class Block
	{
		private final int messageIndex;
		private final int top;
		private final BufferedImage image;

		private Block(int messageIndex, int top, BufferedImage image)
		{
			this.messageIndex = messageIndex;
			this.top = top;
			this.image = image;
		}
	}
}
