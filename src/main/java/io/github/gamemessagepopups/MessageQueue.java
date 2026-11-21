package io.github.gamemessagepopups;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;

final class MessageQueue
{
	static final int MAX_VISIBLE = 3;
	static final int MAX_PENDING = 12;
	private static final long MAX_WAIT_NANOS = TimeUnit.SECONDS.toNanos(5);

	private final Deque<Pending> pending = new ArrayDeque<>(MAX_PENDING);
	private List<Popup> visible = List.of();

	synchronized void add(String text, long now, Timing timing)
	{
		if (pending.size() == MAX_PENDING)
		{
			pending.removeFirst();
		}
		pending.addLast(new Pending(text, now + timing.delay, timing));
	}

	synchronized List<Popup> advance(long now)
	{
		Pending next = pending.peekFirst();
		boolean expired = visible.stream().anyMatch(popup -> popup.isExpired(now));
		boolean canShowNext = visible.size() < MAX_VISIBLE && next != null && now - next.readyAt >= 0;
		if (!expired && !canShowNext)
		{
			return visible;
		}

		List<Popup> updated = new ArrayList<>(MAX_VISIBLE);
		for (Popup popup : visible)
		{
			if (!popup.isExpired(now))
			{
				updated.add(popup);
			}
		}

		while (updated.size() < MAX_VISIBLE && next != null && now - next.readyAt >= 0)
		{
			pending.removeFirst();
			if (now - next.readyAt < MAX_WAIT_NANOS)
			{
				updated.add(new Popup(next.text, now, next.timing));
			}
			next = pending.peekFirst();
		}

		visible = List.copyOf(updated);
		return visible;
	}

	synchronized void clear()
	{
		pending.clear();
		visible = List.of();
	}

	static final class Timing
	{
		private final long delay;
		private final long hold;
		private final long fade;

		Timing(int delayMs, int holdMs, int fadeMs)
		{
			delay = millis(delayMs, 0, GameMessagePopupsConfig.MAX_DELAY_MS);
			hold = millis(holdMs, GameMessagePopupsConfig.MIN_HOLD_MS, GameMessagePopupsConfig.MAX_HOLD_MS);
			fade = millis(fadeMs, 0, GameMessagePopupsConfig.MAX_FADE_MS);
		}

		private static long millis(int value, int min, int max)
		{
			return TimeUnit.MILLISECONDS.toNanos(Math.max(min, Math.min(max, value)));
		}
	}

	static final class Popup
	{
		final String text;
		private final long shownAt;
		private final Timing timing;

		private Popup(String text, long shownAt, Timing timing)
		{
			this.text = text;
			this.shownAt = shownAt;
			this.timing = timing;
		}

		boolean isExpired(long now)
		{
			return now - shownAt >= timing.hold + timing.fade;
		}

		float opacity(long now)
		{
			long elapsed = now - shownAt;
			if (elapsed < timing.hold)
			{
				return 1f;
			}
			if (isExpired(now))
			{
				return 0f;
			}
			return 1f - (float) (elapsed - timing.hold) / timing.fade;
		}
	}

	private static final class Pending
	{
		private final String text;
		private final long readyAt;
		private final Timing timing;

		private Pending(String text, long readyAt, Timing timing)
		{
			this.text = text;
			this.readyAt = readyAt;
			this.timing = timing;
		}
	}
}
