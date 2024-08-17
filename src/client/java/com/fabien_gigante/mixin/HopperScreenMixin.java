package com.fabien_gigante.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fabien_gigante.DisableableSlot;
import com.fabien_gigante.IDisableableSlots;
import com.fabien_gigante.DisableableHopperSlotsModClient;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.gui.screen.ingame.HopperScreen;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.HopperScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.sound.SoundEvent;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Mixin(HopperScreen.class)
public abstract class HopperScreenMixin extends HandledScreen<HopperScreenHandler> implements IDisableableSlots {
    private static final Identifier DISABLED_SLOT_TEXTURE = Identifier.ofVanilla("container/crafter/disabled_slot");
	private static final Text TOGGLEABLE_SLOT_TEXT = Text.translatable("gui.togglable_slot");
    
    protected PlayerEntity player;

	private HopperScreenMixin(HopperScreenHandler handler, PlayerInventory inventory, Text title) { super(handler, inventory, title); }
    
    @Inject(method="<init>", at=@At(value="TAIL"))
    private void init(HopperScreenHandler handler, PlayerInventory playerInventory, Text title, CallbackInfo ci) {
		DisableableHopperSlotsModClient.LOGGER.debug("HopperScreenMixin.init");
        this.player = playerInventory.player;
    }

	@Override
	protected void onMouseClick(Slot slot, int slotId, int button, SlotActionType actionType) {
		DisableableHopperSlotsModClient.LOGGER.debug("HopperScreenMixin.onMouseClick");
		if (slot != null) DisableableHopperSlotsModClient.LOGGER.debug("- slot class={} id={} x={} y={}", slot.getClass(), slot.id, slot.x, slot.y);
		if (slot instanceof DisableableSlot && !slot.hasStack() && !this.player.isSpectator()) {
			switch (actionType) {
				case PICKUP:
					if (isSlotDisabled(slotId)) this.enableSlot(slotId);
					else if (this.handler.getCursorStack().isEmpty()) this.disableSlot(slotId);
					break;
				case SWAP:
					ItemStack itemStack = this.player.getInventory().getStack(button);
					if (isSlotDisabled(slotId) && !itemStack.isEmpty()) this.enableSlot(slotId);
                default:
			}
		}
		super.onMouseClick(slot, slotId, button, actionType);
	}

	public boolean isSlotDisabled(int id) { return ((IDisableableSlots)this.handler).isSlotDisabled(id); }

	public void setSlotEnabled(int id, boolean enabled) {
		DisableableHopperSlotsModClient.LOGGER.debug("HopperScreenMixin.setSlotEnabled [{}]={}", id, enabled);		
		((IDisableableSlots)this.handler).setSlotEnabled(id, enabled);
		super.onSlotChangedState(id, this.handler.syncId, enabled);
		float f = enabled ? 1.0F : 0.75F;
		this.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 0.4F, f);
	}

	@Override
	public void drawSlot(DrawContext context, Slot slot) {
		if (slot instanceof DisableableSlot && isSlotDisabled(slot.id))
			this.drawDisabledSlot(context, slot);
		else super.drawSlot(context, slot);
	}

	protected void drawDisabledSlot(DrawContext context, Slot slot) {
		context.drawGuiTexture(DISABLED_SLOT_TEXTURE, slot.x - 1, slot.y - 1, 18, 18);
	}
    
	@Inject(method="render", at=@At(value="TAIL"))
	private void renderTooltip(DrawContext context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (this.focusedSlot instanceof DisableableSlot slot && !isSlotDisabled(slot.id)
			&& this.handler.getCursorStack().isEmpty() && !slot.hasStack() && !this.player.isSpectator())
				context.drawTooltip(this.textRenderer, TOGGLEABLE_SLOT_TEXT, mouseX, mouseY);
	}
}
