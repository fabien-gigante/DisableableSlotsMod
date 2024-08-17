package com.fabien_gigante;

import java.util.stream.IntStream;

public interface IDisableableSlots {
	public boolean isSlotDisabled(int id);
	public default boolean isSlotEnabled(int id) { return !this.isSlotDisabled(id); }

	public void setSlotEnabled(int id, boolean enabled);
	public default void enableSlot(int id) { this.setSlotEnabled(id, true);}
	public default void disableSlot(int id) { this.setSlotEnabled(id, false);}

	public default int countEnabled(int startInclusive, int endExclusive) {
		return (int)IntStream.range(startInclusive, endExclusive).filter(i -> isSlotEnabled(i)).count();
	}
}
