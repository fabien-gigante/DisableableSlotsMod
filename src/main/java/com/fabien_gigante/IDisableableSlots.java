package com.fabien_gigante;

import net.minecraft.inventory.Inventory;

public interface IDisableableSlots {
	public boolean isSlotEnabled(int id);
	public default boolean isSlotDisabled(int id) { return !isSlotEnabled(id); }

	public void setSlotEnabled(int id, boolean enabled);
	public default void enableSlot(int id) { this.setSlotEnabled(id, true);}
	public default void disableSlot(int id) { this.setSlotEnabled(id, false);}

	public default BitsPropertyDelegate getDisableableSlots() { return null; }
	public default int countSlotsEnabled(boolean enabled) {
		BitsPropertyDelegate disabledSlots = this.getDisableableSlots();
		return disabledSlots == null ? -1 : disabledSlots.count(enabled ? 0 : 1);
	}

	public static int getCapacity(Inventory inventory) {
		int capacity = inventory.size();
		if (inventory instanceof IDisableableSlots slots)
			capacity = Math.min(slots.countSlotsEnabled(true), 1);
		return capacity;
	}	
}
