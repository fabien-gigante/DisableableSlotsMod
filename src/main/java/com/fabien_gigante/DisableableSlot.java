package com.fabien_gigante;

import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.slot.Slot;

public class DisableableSlot extends Slot {
    protected final IDisableableSlots handler;

	public DisableableSlot(Inventory inventory, int index, int x, int y, IDisableableSlots handler) {
		super(inventory, index, x, y);
		this.handler = handler;
	}

	@Override
	public boolean canInsert(ItemStack stack) {
		return handler.isSlotEnabled(id) && super.canInsert(stack);
	}    
}
