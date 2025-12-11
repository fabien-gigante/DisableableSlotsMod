package com.fabien_gigante.mixin.lithium;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import com.fabien_gigante.IDisableableSlots;

import net.caffeinemc.mods.lithium.common.hopper.HopperHelper;
import net.minecraft.world.Container;

@Mixin(HopperHelper.class)
public class HopperHelperMixin {
	// NOTE
	// - ordinals 0 and 2 are used to iterate the slots, and should be kept as is
	// - ordinals 1 and 3 are used to compute the signal strength and should be modified
	@Redirect(method = "determineComparatorUpdatePattern", 
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;getContainerSize()I", ordinal = 1))
	private static int capacity_1(Container inventory) { return IDisableableSlots.getCapacity(inventory); }

	@Redirect(method = "determineComparatorUpdatePattern", 
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;getContainerSize()I", ordinal = 3))
	private static int capacity_3(Container inventory) { return IDisableableSlots.getCapacity(inventory); }
}
