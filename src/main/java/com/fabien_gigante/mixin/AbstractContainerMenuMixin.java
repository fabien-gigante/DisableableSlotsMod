package com.fabien_gigante.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import com.fabien_gigante.IDisableableSlots;
import net.minecraft.world.Container;
import net.minecraft.world.inventory.AbstractContainerMenu;

@Mixin(AbstractContainerMenu.class)
public abstract class AbstractContainerMenuMixin {
	@Redirect(method = "getRedstoneSignalFromContainer", 
		at = @At(value = "INVOKE", target = "Lnet/minecraft/world/Container;getContainerSize()I", ordinal = 1))
	private static int getCapacity(Container inventory) { return IDisableableSlots.getCapacity(inventory); }
}
