package com.fabien_gigante.mixin.lithium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.fabien_gigante.IDisableableSlots;

import net.caffeinemc.mods.lithium.common.hopper.HopperHelper;
import net.minecraft.inventory.Inventory;

@Mixin(HopperHelper.class)
public class HopperHelperMixin {
	
	@Redirect(method = "determineComparatorUpdatePattern", 
		at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;size()I", ordinal = 1))
	private static int capacity_1(Inventory inventory) { return IDisableableSlots.getCapacity(inventory); }

	@Redirect(method = "determineComparatorUpdatePattern", 
		at = @At(value = "INVOKE", target = "Lnet/minecraft/inventory/Inventory;size()I", ordinal = 3))
	private static int capacity_3(Inventory inventory) { return IDisableableSlots.getCapacity(inventory); }
}
