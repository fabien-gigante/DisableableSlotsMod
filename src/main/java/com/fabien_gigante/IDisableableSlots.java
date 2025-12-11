package com.fabien_gigante;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public interface IDisableableSlots {
	public static final ItemStack INVALID_STACK = createInvalidStack();
    private static ItemStack createInvalidStack() {
        ItemStack stack = new ItemStack(Items.BARRIER, Items.BARRIER.getDefaultMaxStackSize());
        stack.set(DataComponents.CUSTOM_NAME, Component.translatable(DisableableSlotsMod.MOD_ID + ".invalid_stack"));
        return stack;
    }

	public boolean isSlotEnabled(int id);
	public default boolean isSlotDisabled(int id) { return !isSlotEnabled(id); }

	public void setSlotEnabled(int id, boolean enabled);
	public default void enableSlot(int id) { this.setSlotEnabled(id, true);}
	public default void disableSlot(int id) { this.setSlotEnabled(id, false);}

	public default BitsContainerData getDisableableSlots() { return null; }
	public default int countSlotsEnabled(boolean enabled) {
		BitsContainerData disabledSlots = this.getDisableableSlots();
		return disabledSlots == null ? -1 : disabledSlots.count(enabled ? 0 : 1);
	}

	public static int getCapacity(Container inventory) {
		return (inventory instanceof IDisableableSlots slots)
			? Math.max(slots.countSlotsEnabled(true), 1) 
			: inventory.getContainerSize();
	}
}
