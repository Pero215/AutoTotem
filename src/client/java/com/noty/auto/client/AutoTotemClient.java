package com.noty.auto.client;

import com.noty.auto.AutoTotem;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Identifier; // <-- NEW IMPORT
import org.lwjgl.glfw.GLFW;

/**
 * Client-side entrypoint and core logic for AutoTotem.
 * Handles keybind, health checks, and item swapping.
 */
public class AutoTotemClient implements ClientModInitializer {
    private static KeyBinding autoTotemKeybind;
    // Stores the slot ID (9-44) of the item that was swapped *out* of the off-hand, so we can swap it back.
    private static int previousOffhandSlot = -1;
    private static boolean manualSwap = false; // Flag to indicate a keybind-triggered swap

    // Constants for health thresholds (2 hearts = 4 health, 5 hearts = 10 health)
    private static final float SWAP_IN_HEALTH = 4.0F;
    private static final float SWAP_OUT_HEALTH = 10.0F;

    // Slot index for the off-hand in the PlayerScreenHandler (slot ID 40)
    private static final int OFFHAND_SLOT_ID = 40;

    // FIX: Define the custom keybind category using the Identifier of the translation key.
    // The Identifier is automatically converted to the appropriate Text component for display.
    private static final KeyBinding.Category AUTO_TOTEM_CATEGORY =
            new KeyBinding.Category(Identifier.of(AutoTotem.MOD_ID, "general")); // <-- FIXED LINE

    @Override
    public void onInitializeClient() {
        registerKeybind();
        registerClientTickEvent();
        AutoTotem.LOGGER.info("AutoTotem Keybind and Tick Registered.");
    }

    private void registerKeybind() {
        // Pass the KeyBinding.Category OBJECT
        autoTotemKeybind = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autototem.toggle", // Translation key from en_us.json
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_V, // Default key V
                AUTO_TOTEM_CATEGORY // Pass the Category OBJECT
        ));
    }

    private void registerClientTickEvent() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // 1. Handle Manual (Keybind) Swap
            while (autoTotemKeybind.wasPressed()) {
                manualSwap = true; // Set flag for keybind activation
                performAutoTotemSwap(client);
            }

            // 2. Handle Automatic (Health-based) Swap
            if (client.player.isAlive()) {
                handleHealthBasedSwap(client);
            }

            // Reset state if player dies or logs out
            if (!client.player.isAlive() || client.world == null) {
                previousOffhandSlot = -1;
                manualSwap = false;
            }
        });
    }

    /**
     * Finds the first Totem of Undying in the inventory (excluding the off-hand).
     * @return The screen handler slot index of the Totem (9-44), or -1 if none is found.
     */
    private int findTotemSlot(MinecraftClient client) {
        PlayerInventory inventory = client.player.getInventory();
        // Iterate through all 36 main inventory slots (0-35)
        for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
            if (inventory.getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                // Convert inventory index (0-35) to screen handler slot ID (9-44)
                // Hotbar 0-8 maps to screen handler slots 36-44
                // Inventory 9-35 maps to screen handler slots 9-35
                return i < 9 ? i + 36 : i;
            }
        }
        return -1;
    }

    /**
     * Transfers an item from an inventory slot to the off-hand slot (slot ID 40).
     * @param client The Minecraft client instance.
     * @param sourceSlot The screen handler slot ID of the item to move (9-44).
     * @param recordOriginalSlot Whether to record the original slot of the item being *swapped out* of the off-hand.
     */
    private void swapItems(MinecraftClient client, int sourceSlot, boolean recordOriginalSlot) {
        if (client.player == null || client.interactionManager == null) return;

        // Step 1: Record the slot the item came from if we need to swap back later
        if (recordOriginalSlot) {
            // We store the slot the totem came from as the slot to swap back to.
            previousOffhandSlot = sourceSlot;
        }

        // Step 2: Use the SWAP action. This simulates swapping the item in the source slot
        // with the item in the secondary slot (off-hand, 40).
        client.interactionManager.clickSlot(
                client.player.currentScreenHandler.syncId,
                sourceSlot,
                OFFHAND_SLOT_ID, // Secondary slot (off-hand) for SWAP type
                SlotActionType.SWAP,
                client.player
        );
        AutoTotem.LOGGER.info("Performed Totem Swap from slot {} to off-hand.", sourceSlot);
    }

    /**
     * Transfers the item back from the off-hand to its original slot.
     * @param client The Minecraft client instance.
     */
    private void swapBackItems(MinecraftClient client) {
        if (client.player == null || client.interactionManager == null || previousOffhandSlot == -1) return;

        // previousOffhandSlot holds the original slot ID (9-44) of the item that was swapped out.
        // We simulate a swap action between the off-hand (40) and the original slot.

        client.interactionManager.clickSlot(
                client.player.currentScreenHandler.syncId,
                OFFHAND_SLOT_ID,
                previousOffhandSlot, // Secondary slot (original slot) for SWAP type
                SlotActionType.SWAP,
                client.player
        );
        AutoTotem.LOGGER.info("Performed Auto-Swap Back to original slot {}.", previousOffhandSlot);
        previousOffhandSlot = -1; // Reset state
        manualSwap = false; // Reset manual flag
    }

    /**
     * Core logic for keybind-triggered swap.
     * Toggles the totem into the off-hand.
     */
    private void performAutoTotemSwap(MinecraftClient client) {
        PlayerInventory inventory = client.player.getInventory();

        boolean isOffhandTotem = inventory.getStack(OFFHAND_SLOT_ID).isOf(Items.TOTEM_OF_UNDYING);
        int totemSlotId = findTotemSlot(client);

        if (isOffhandTotem) {
            // Totem is in off-hand. Swap it back to the original slot if known.
            if (previousOffhandSlot != -1) {
                swapBackItems(client);
            }
        } else {
            // Totem is NOT in off-hand. Find one and swap it in.
            if (totemSlotId != -1) {
                // When manually swapping, we *do* want to record the item currently in off-hand
                // so the next manual keypress can swap it back.
                swapItems(client, totemSlotId, true);
            }
        }
    }

    /**
     * Handles the health-based automatic swap logic.
     */
    private void handleHealthBasedSwap(MinecraftClient client) {
        PlayerInventory inventory = client.player.getInventory();
        float health = client.player.getHealth();

        boolean isOffhandTotem = inventory.getStack(OFFHAND_SLOT_ID).isOf(Items.TOTEM_OF_UNDYING);
        int totemSlotId = findTotemSlot(client);

        // --- Auto-Swap IN (Health < 2 hearts) ---
        if (health <= SWAP_IN_HEALTH && !isOffhandTotem && totemSlotId != -1) {
            // Only auto-swap in if no manual swap is pending (to avoid conflicts)
            if (!manualSwap) {
                // Find a totem and swap the current off-hand item (if any) out
                swapItems(client, totemSlotId, true); // Record the original item slot
            }
            return;
        }

        // --- Auto-Swap BACK (Health > 5 hearts) ---
        // Check if a totem is still in the off-hand AND we have a slot to swap back to.
        if (isOffhandTotem && previousOffhandSlot != -1 && health > SWAP_OUT_HEALTH) {
            if (!manualSwap) {
                swapBackItems(client); // Swap the totem back with the recorded item.
            }
        }
    }
}