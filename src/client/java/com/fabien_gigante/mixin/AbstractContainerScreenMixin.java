package com.fabien_gigante.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.fabien_gigante.DisableableSlot;
import com.fabien_gigante.IDisableableSlots;
import com.mojang.blaze3d.platform.cursor.CursorTypes;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

@Mixin(AbstractContainerScreen.class)
public abstract class AbstractContainerScreenMixin<T extends AbstractContainerMenu> extends Screen implements IDisableableSlots {
	private static final Identifier DISABLED_SLOT_TEXTURE = Identifier.withDefaultNamespace("container/crafter/disabled_slot");
	private static final Component TOGGLEABLE_SLOT_TEXT = Component.translatable("gui.togglable_slot");

    @Shadow
    protected T menu;
    @Shadow
    protected int leftPos, topPos;
    @Shadow
    protected Slot hoveredSlot;
	protected Player player;

	private AbstractContainerScreenMixin(T menu, Inventory inventory, Component title) { super(title); }
    @Shadow
    abstract protected void handleSlotStateChanged(int slotId, int handlerId, boolean newState);     
	
	@Inject(method="<init>", at=@At(value="TAIL"))
	private void init(T menu, Inventory inventory, Component title, CallbackInfo ci) {
		this.player = inventory.player;
	}

	@Inject(method="slotClicked", at=@At(value="HEAD"))
	private void onSlotClick(Slot slot, int slotId, int button, ClickType actionType, CallbackInfo ci) {
		if (slot instanceof DisableableSlot && !slot.hasItem() && !this.player.isSpectator()) {
			switch (actionType) {
				case PICKUP:
					if (this.isSlotDisabled(slotId)) this.enableSlot(slotId);
					else if (this.menu.getCarried().isEmpty()) this.disableSlot(slotId);
					break;
				case SWAP:
					ItemStack itemStack = this.player.getInventory().getItem(button);
					if (this.isSlotDisabled(slotId) && !itemStack.isEmpty()) this.enableSlot(slotId);
					break;
				default:
			}
		}
	}

	public boolean isSlotEnabled(int id) {
         return !(this.menu instanceof IDisableableSlots slots) || slots.isSlotEnabled(id); 
    }

	public void setSlotEnabled(int id, boolean enabled) {
        if (!(this.menu instanceof IDisableableSlots slots)) return;
		slots.setSlotEnabled(id, enabled);
		handleSlotStateChanged(id, this.menu.containerId, enabled);
		float f = enabled ? 1.0F : 0.75F;
		this.player.playSound((SoundEvent)SoundEvents.UI_BUTTON_CLICK.value(), 0.4F, f);
	}

	@Inject(method="renderSlot", at=@At(value="HEAD"), cancellable=true)
	private void renderSlot(GuiGraphics graphics, Slot slot, int x, int y, CallbackInfo ci) {
		if (slot instanceof DisableableSlot) {
			int left = this.leftPos + slot.x - 2, top = this.topPos + slot.y - 2;
			if (x > left && y > top && x < left + 19 && y < top + 19)
            	graphics.requestCursor(CursorTypes.POINTING_HAND);
			if (isSlotDisabled(slot.index)) {
				this.renderDisabledSlot(graphics, slot);
            	ci.cancel();
			}
        }
	}

	private void renderDisabledSlot(GuiGraphics graphics, Slot slot) {
		graphics.blitSprite(RenderPipelines.GUI_TEXTURED, DISABLED_SLOT_TEXTURE, slot.x - 1, slot.y - 1, 18, 18);
	}
	
	@Inject(method="render", at=@At(value="TAIL"))
	private void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY, float delta, CallbackInfo ci) {
		if (this.hoveredSlot instanceof DisableableSlot slot && !isSlotDisabled(slot.index)
			&& this.menu.getCarried().isEmpty() && !slot.hasItem() && !this.player.isSpectator())
				graphics.setTooltipForNextFrame(this.font, TOGGLEABLE_SLOT_TEXT, mouseX, mouseY);
	}
}
