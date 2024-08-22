package com.fabien_gigante.mixin.lithium;

import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.fabien_gigante.IDisableableSlots;

import me.jellysquid.mods.lithium.common.hopper.LithiumStackList;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;

@Mixin(LithiumStackList.class)
public abstract class LithiumStackListMixin implements List<ItemStack>  {
    @Shadow(remap = false) int maxCountPerStack;
    private int cachedCapacity = 1;

    @Inject(method = "getSignalStrength", at=@At(
        value = "INVOKE", target = "Lme/jellysquid/mods/lithium/common/hopper/LithiumStackList;calculateSignalStrength(I)I", shift = Shift.BEFORE
    ))
    public void calculateCapacity(Inventory inventory, CallbackInfoReturnable<Integer> ci) {
        this.cachedCapacity = IDisableableSlots.getCapacity(inventory);
    }

    // TODO : better injection
    int calculateSignalStrength(int inventorySize) {
        int i = 0;
        float f = 0.0F;

        inventorySize = Math.min(inventorySize, this.size());
        for (int j = 0; j < inventorySize; ++j) {
            ItemStack itemStack = this.get(j);
            if (!itemStack.isEmpty()) {
                f += (float) itemStack.getCount() / (float) Math.min(this.maxCountPerStack, itemStack.getMaxCount());
                ++i;
            }
        }

        f /= (float) this.cachedCapacity;
        return MathHelper.floor(f * 14.0F) + (i > 0 ? 1 : 0);
    }
}
