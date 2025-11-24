🛡️ Auto Totem - Automatic Off-Hand Totem Swapper

Auto Totem is a simple yet essential client-side utility mod for Fabric that automatically moves a Totem of Undying into your off-hand when you need it most, based on your health or a custom keybind. Never panic-swap again!

✨ Features

This mod provides two primary ways to manage your Totems:

1. Health-Based Auto-Swap (Critical Survival)

When enabled, the mod continuously monitors your health and takes action when you are in critical danger:

Auto Swap In: If your health drops to 2 hearts (4.0 health points) or less, the mod will instantly search your inventory for the first available Totem of Undying and move it to your off-hand.

Auto Swap Back: If your health recovers to over 5.5 hearts (11.0 health points), the Totem will automatically be swapped back into the inventory slot where the original off-hand item was stored.

2. Manual Keybind Swap (Tactical Control)

The mod includes a customizable keybind for quick, manual swaps:

Keybind Action: Pressing the key will either swap a Totem into your off-hand (if one is available) or swap the existing Totem out, returning the previous item to its original inventory slot.

⚙️ Usage & Keybind Configuration

Before using the mod, you should configure your preferred key:

Start Minecraft with the mod installed.

Go to Options > Controls > Key Binds.

Scroll down to the AutoTotem category.

Change the key for "Toggle Totem Off-hand" (default is V) to your liking.

Important Notes:

Swap Back Priority: The mod remembers where the item that was displaced by the Totem came from. It will only swap back to that exact slot.

Manual Override: If you manually trigger the keybind, the automatic swap-back logic will be temporarily disabled until you either press the keybind again to swap the Totem out, or until the game state resets (e.g., player dies).

📦 Installation

This is a client-side Fabric mod and must be installed in your local Minecraft instance.

Install Fabric Loader: Ensure you have the Fabric Loader installed for Minecraft 1.21.

Download Mod: Download the latest version of AutoTotem.jar from the release page.

Move to Mods Folder: Place the downloaded .jar file into your Minecraft mods folder.

Run: Start Minecraft using the Fabric profile.

📜 Technical Details

Mod ID: autototem

Package: com.noty.auto

Development Environment: Fabric 1.21

📄 License

This mod is licensed under the MIT License.