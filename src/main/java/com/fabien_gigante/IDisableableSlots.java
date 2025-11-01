package com.fabien_gigante;

import net.minecraft.component.DataComponentTypes;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Items;
import net.minecraft.text.Text;
import net.minecraft.item.ItemStack;

public interface IDisableableSlots {
	public static final ItemStack INVALID_STACK = createInvalidStack();
    private static ItemStack createInvalidStack() {
        ItemStack stack = new ItemStack(Items.BARRIER, Items.BARRIER.getMaxCount());
        stack.set(DataComponentTypes.CUSTOM_NAME, Text.translatable(DisableableSlotsMod.MOD_ID + ".invalid_stack"));
        return stack;
    }

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
		return (inventory instanceof IDisableableSlots slots)
			? Math.max(slots.countSlotsEnabled(true), 1) 
			: inventory.size();
	}
}
