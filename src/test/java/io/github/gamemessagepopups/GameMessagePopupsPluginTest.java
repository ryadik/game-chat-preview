package io.github.gamemessagepopups;

import java.util.EnumSet;
import net.runelite.api.ChatMessageType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class GameMessagePopupsPluginTest
{
	@Test
	public void defaultAllowListContainsOnlyGameAndEngineMessages()
	{
		GameMessagePopupsConfig config = new GameMessagePopupsConfig() {};
		EnumSet<ChatMessageType> allowed = EnumSet.of(ChatMessageType.GAMEMESSAGE, ChatMessageType.ENGINE);
		assertAllowList(config, allowed);
		assertFalse(GameMessagePopupsPlugin.accepts(null, config));
	}

	@Test
	public void optionalTypesDoNotEnableClanPrivateOrPublicChat()
	{
		GameMessagePopupsConfig config = new GameMessagePopupsConfig()
		{
			@Override
			public boolean showFilteredMessages()
			{
				return true;
			}

			@Override
			public boolean showExamines()
			{
				return true;
			}
		};
		assertAllowList(config, EnumSet.of(
			ChatMessageType.GAMEMESSAGE, ChatMessageType.ENGINE, ChatMessageType.SPAM,
			ChatMessageType.ITEM_EXAMINE, ChatMessageType.NPC_EXAMINE, ChatMessageType.OBJECT_EXAMINE));
	}

	@Test
	public void stripsGameFormattingAndPreservesPrintableEscapes()
	{
		assertEquals("Too close < 2 tiles", GameMessagePopupsPlugin.cleanMessage(" <col=ff0000>Too close</col><br><lt> 2 tiles "));
		assertEquals("", GameMessagePopupsPlugin.cleanMessage("<img=0><br>"));
	}

	private static void assertAllowList(GameMessagePopupsConfig config, EnumSet<ChatMessageType> allowed)
	{
		for (ChatMessageType type : ChatMessageType.values())
		{
			assertEquals(type.toString(), allowed.contains(type), GameMessagePopupsPlugin.accepts(type, config));
		}
	}
}
