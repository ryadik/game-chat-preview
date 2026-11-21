package io.github.gamemessagepopups;

import java.awt.Font;

public enum PopupFontFamily
{
	SANS_SERIF(Font.SANS_SERIF, "Sans Serif"),
	SERIF(Font.SERIF, "Serif"),
	MONOSPACED(Font.MONOSPACED, "Monospaced"),
	ARIAL("Arial", "Arial"),
	GEORGIA("Georgia", "Georgia"),
	VERDANA("Verdana", "Verdana"),
	COURIER_NEW("Courier New", "Courier New"),
	TIMES_NEW_ROMAN("Times New Roman", "Times New Roman"),
	CUSTOM("", "Custom");

	private final String family;
	private final String label;

	PopupFontFamily(String family, String label)
	{
		this.family = family;
		this.label = label;
	}

	String resolve(String customFamily)
	{
		if (this != CUSTOM)
		{
			return family;
		}
		return customFamily.isBlank() ? Font.SANS_SERIF : customFamily.strip();
	}

	static PopupFontFamily fromSavedName(String savedName)
	{
		String family = savedName.strip();
		if (family.isEmpty())
		{
			return SANS_SERIF;
		}
		for (PopupFontFamily option : values())
		{
			if (option.family.equalsIgnoreCase(family))
			{
				return option;
			}
		}
		return CUSTOM;
	}

	@Override
	public String toString()
	{
		return label;
	}
}
