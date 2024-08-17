package com.fabien_gigante.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fabien_gigante.IDisableableSlots;

import net.minecraft.network.packet.c2s.play.SlotChangedStateC2SPacket;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;

@Mixin(ServerPlayNetworkHandler.class)
public abstract class ServerPlayNetworkHandlerMixin {
	@Shadow
	public ServerPlayerEntity player;

	@Inject(method = "onSlotChangedState", at = @At("TAIL"))
	private void onSlotChangedState(SlotChangedStateC2SPacket packet, CallbackInfo ci) {
		if (!this.player.isSpectator() && packet.screenHandlerId() == this.player.currentScreenHandler.syncId)
			if (this.player.currentScreenHandler instanceof IDisableableSlots screenHandler)
				screenHandler.setSlotEnabled(packet.slotId(), packet.newState());
	}
}