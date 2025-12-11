package com.fabien_gigante;

import net.minecraft.world.Container;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class DisableableSlot extends Slot {
	protected final IDisableableSlots menu;

	public DisableableSlot(Container inventory, int index, int x, int y, IDisableableSlots handler) {
		super(inventory, index, x, y);
		this.menu = handler;
	}

	@Override
	public boolean mayPlace(ItemStack stack) {
		return menu.isSlotEnabled(this.index) && super.mayPlace(stack);
	}    
}
