package com.fabien_gigante.mixin.lithium;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.caffeinemc.mods.lithium.common.hopper.LithiumStackList;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import com.fabien_gigante.IDisableableSlots;

@Mixin(LithiumStackList.class)
public abstract class LithiumStackListMixin implements List<ItemStack>  {
	private float ratioCapacity = 1;

	@Inject(method = "getSignalStrength", at=@At(
		value = "INVOKE", target = "Lnet/caffeinemc/mods/lithium/common/hopper/LithiumStackList;calculateSignalStrength(I)I", shift = Shift.BEFORE
	))
	public void calculateCapacity(Container inventory, CallbackInfoReturnable<Integer> ci) {
		this.ratioCapacity = Math.min(inventory.getContainerSize(), this.size()) / IDisableableSlots.getCapacity(inventory);
	}
	
	@ModifyVariable(method = "calculateSignalStrength", at = @At(value = "STORE", ordinal = 2), index = 3, remap = false)
	private float adjustCapacity(float f) { return f * (float)this.ratioCapacity; }
}
