package io.github.gamemessagepopups;

import java.awt.Font;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class PopupFontFamilyTest
{
	@Test
	public void recognizesSavedFamiliesAndKeepsOtherNamesAsCustom()
	{
		assertEquals(PopupFontFamily.SANS_SERIF, PopupFontFamily.fromSavedName(" "));
		assertEquals(PopupFontFamily.SANS_SERIF, PopupFontFamily.fromSavedName("SansSerif"));
		assertEquals(PopupFontFamily.COURIER_NEW, PopupFontFamily.fromSavedName(" courier new "));
		PopupFontFamily custom = PopupFontFamily.fromSavedName("Helvetica Neue");
		assertEquals(PopupFontFamily.CUSTOM, custom);
		assertEquals("Helvetica Neue", custom.resolve(" Helvetica Neue "));
		assertEquals(Font.SANS_SERIF, custom.resolve(""));
	}
}
