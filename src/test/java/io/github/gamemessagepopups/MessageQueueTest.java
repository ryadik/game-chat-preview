package io.github.gamemessagepopups;

import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class MessageQueueTest
{
	private final MessageQueue queue = new MessageQueue();

	@Test
	public void delayHoldAndFadeUseActualDisplayTime()
	{
		queue.add("first", ms(0), new MessageQueue.Timing(100, 400, 200));
		assertTrue(queue.advance(ms(99)).isEmpty());

		MessageQueue.Popup popup = queue.advance(ms(140)).get(0);
		assertEquals(1f, popup.opacity(ms(539)), 0f);
		assertEquals(1f, popup.opacity(ms(540)), 0f);
		assertEquals(0.5f, popup.opacity(ms(640)), 0.001f);
		assertEquals(0f, popup.opacity(ms(740)), 0f);
		assertTrue(queue.advance(ms(740)).isEmpty());
	}

	@Test
	public void fillsSlotsInOrderWithoutCuttingOffCurrentMessages()
	{
		MessageQueue.Timing timing = new MessageQueue.Timing(0, 400, 0);
		for (String text : List.of("one", "two", "three", "four"))
		{
			queue.add(text, ms(0), timing);
		}

		List<MessageQueue.Popup> initial = queue.advance(ms(0));
		assertEquals(MessageQueue.MAX_VISIBLE, initial.size());
		assertEquals("one", initial.get(0).text);
		assertEquals("three", initial.get(2).text);
		assertEquals(initial, queue.advance(ms(399)));
		List<MessageQueue.Popup> next = queue.advance(ms(400));
		assertEquals(1, next.size());
		assertEquals("four", next.get(0).text);
		assertEquals(1f, next.get(0).opacity(ms(799)), 0f);
		assertTrue(queue.advance(ms(800)).isEmpty());
	}

	@Test
	public void capsTheBacklogAndSkipsStaleMessages()
	{
		MessageQueue.Timing timing = new MessageQueue.Timing(0, 100, 0);
		for (int index = 0; index <= MessageQueue.MAX_PENDING; index++)
		{
			queue.add(Integer.toString(index), ms(0), timing);
		}
		assertEquals("1", queue.advance(ms(0)).get(0).text);
		queue.add("recent", ms(4999), timing);
		List<MessageQueue.Popup> remaining = queue.advance(ms(5000));
		assertEquals(1, remaining.size());
		assertEquals("recent", remaining.get(0).text);
	}

	@Test
	public void clearResetsVisibleAndPendingMessages()
	{
		queue.add("visible", ms(0), new MessageQueue.Timing(0, 100, 0));
		queue.add("pending", ms(0), new MessageQueue.Timing(100, 100, 0));
		assertEquals(1, queue.advance(ms(0)).size());
		queue.clear();
		assertTrue(queue.advance(ms(100)).isEmpty());
	}

	private static long ms(int milliseconds)
	{
		return TimeUnit.MILLISECONDS.toNanos(milliseconds);
	}
}
