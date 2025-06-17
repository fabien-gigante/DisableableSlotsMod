package com.fabien_gigante.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fabien_gigante.DisableableSlot;
import com.fabien_gigante.IDisableableSlots;

import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Mixin(HandledScreen.class)
public abstract class HandledScreenMixin<T extends ScreenHandler> extends Screen implements IDisableableSlots {
	private static final Identifier DISABLED_SLOT_TEXTURE = Identifier.ofVanilla("container/crafter/disabled_slot");
	private static final Text TOGGLEABLE_SLOT_TEXT = Text.translatable("gui.togglable_slot");

    @Shadow
    protected T handler;
    @Shadow
    protected Slot focusedSlot;
	protected PlayerEntity player;

	private HandledScreenMixin(T handler, PlayerInventory inventory, Text title) { super(title); }
    @Shadow
    abstract protected void onSlotChangedState(int slotId, int handlerId, boolean newState);     
	
	@Inject(method="<init>", at=@At(value="TAIL"))
	private void init(T handler, PlayerInventory inventory, Text title, CallbackInfo ci) {
		this.player = inventory.player;
	}

	@Inject(method="onMouseClick(Lnet/minecraft/screen/slot/Slot;IILnet/minecraft/screen/slot/SlotActionType;)V", at=@At(value="HEAD"))
	private void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType, CallbackInfo ci) {
		if (slot instanceof DisableableSlot && !slot.hasStack() && !this.player.isSpectator()) {
			switch (actionType) {
				case PICKUP:
					if (this.isSlotDisabled(slotId)) this.enableSlot(slotId);
					else if (this.handler.getCursorStack().isEmpty()) this.disableSlot(slotId);
					break;
				case SWAP:
					ItemStack itemStack = this.player.getInventory().getStack(button);
					if (this.isSlotDisabled(slotId) && !itemStack.isEmpty()) this.enableSlot(slotId);
					break;
				default:
			}
		}
	}

	public boolean isSlotEnabled(int id) {
         return !(this.handler instanceof IDisableableSlots slots) || slots.isSlotEnabled(id); 
    }

	public void setSlotEnabled(int id, boolean enabled) {
        if (!(this.handler instanceof IDisableableSlots slots)) return;
		slots.setSlotEnabled(id, enabled);
		onSlotChangedState(id, this.handler.syncId, enabled);
		float f = enabled ? 1.0F : 0.75F;
		this.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 0.4F, f);
	}

	@Inject(method="drawSlot", at=@At(value="HEAD"), cancellable=true)
	private void drawSlot(DrawContext context, Slot slot, CallbackInfo ci) {
		if (slot instanceof DisableableSlot && isSlotDisabled(slot.id)) {
			this.drawDisabledSlot(context, slot);
            ci.cancel();
        }
	}

	private void drawDisabledSlot(DrawContext context, Slot slot) {
		context.drawGuiTexture(RenderPipelines.GUI_TEXTURED, DISABLED_SLOT_TEXTURE, slot.x - 1, slot.y - 1, 18, 18);
	}
	
	@Inject(method="render", at=@At(value="TAIL"))
	private void renderTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (this.focusedSlot instanceof DisableableSlot slot && !isSlotDisabled(slot.id)
			&& this.handler.getCursorStack().isEmpty() && !slot.hasStack() && !this.player.isSpectator())
				context.drawTooltip(this.textRenderer, TOGGLEABLE_SLOT_TEXT, mouseX, mouseY);
	}
}
