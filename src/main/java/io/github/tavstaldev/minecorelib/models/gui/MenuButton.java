package io.github.tavstaldev.minecorelib.models.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.samjakob.spigui.buttons.SGButton;
import io.github.tavstaldev.minecorelib.core.GuiDupeDetector;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class MenuButton {
    private final Material material;
    private @Nullable final String headTexture;
    private final Integer amount;
    private @Nullable final String title;
    private @Nullable final String titleKey;
    private @Nullable final String loreKey;
    private @Nullable final String[] lore;
    private @Nullable final Integer slot;
    private @Nullable final Integer[] slots;
    private @Nullable final String[] commands;

    public MenuButton(Material material,
                      @Nullable String headTexture,
                      Integer amount,
                      @Nullable String title,
                      @Nullable String titleKey,
                      @Nullable String loreKey,
                      @Nullable String[] lore,
                      @Nullable Integer slot,
                      @Nullable Integer[] slots,
                      @Nullable String[] commands) {
        this.material = material;
        this.headTexture = headTexture;
        this.amount = amount;
        this.title = title;
        this.titleKey = titleKey;
        this.loreKey = loreKey;
        this.lore = lore;
        this.slot = slot;
        this.slots = slots;
        this.commands = commands;
    }

    public ItemStack toItemStack(Player player, PluginTranslator translator) {
        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta meta = itemStack.getItemMeta();
        if (titleKey != null) {
            meta.displayName(ChatUtils.translateColors(translator.localize(player, titleKey), true));
        } else if (title != null) {
            meta.displayName(ChatUtils.translateColors(translator.localize(title),true));
        }

        List<Component> lore = new ArrayList<>();
        if (loreKey != null) {
            String[] loreLines = translator.localizeArray(player, loreKey);
            for (String line : loreLines) {
                lore.add(ChatUtils.translateColors(line, true));
            }
        } else if (this.lore != null) {
            for (String line : this.lore) {
                lore.add(ChatUtils.translateColors(translator.localize(line), true));
            }
        }
        meta.lore(lore);

        meta.getPersistentDataContainer().set(GuiDupeDetector.getDupeProtectedKey(), PersistentDataType.BOOLEAN, true);

        if (headTexture != null && meta instanceof SkullMeta skullMeta) {
            PlayerProfile profile = Bukkit.createProfile(UUID.fromString("bbde04e7-ccb9-49a8-8ad8-08d11b6540d4"));
            profile.setProperty(new ProfileProperty("textures", headTexture));
            skullMeta.setPlayerProfile(profile);
            itemStack.setItemMeta(skullMeta);
        }
        else {
            itemStack.setItemMeta(meta);
        }
        return itemStack;
    }

    public SGButton get(Player player, PluginTranslator translator) {
        ItemStack itemStack = toItemStack(player, translator);
        return new SGButton(itemStack);
    }

    public Integer[] getSlots() {
        if (slots != null) {
            return slots;
        }
        else if (slot != null) {
            return new Integer[] { slot };
        }
        else {
            return new Integer[] {};
        }
    }

    public String[] getCommands() {
        return commands;
    }

    public static @Nullable MenuButton fromMap(Map<?, ?> map) {
        if (map == null) return null;

        try {
            Material material = map.containsKey("head") ? Material.PLAYER_HEAD : Material.getMaterial(((String) map.get("material")).toUpperCase());
            String headTexture = map.containsKey("head") ? (String) map.get("head") : null;
            Integer amount = (Integer) map.get("amount");
            String title = (String) map.get("title");
            String titleKey = (String) map.get("titleKey");
            String loreKey = (String) map.get("loreKey");

            String[] lore = map.containsKey("lore") && map.get("lore") instanceof List<?> loreList
                    ? loreList.stream().map(Object::toString).toArray(String[]::new)
                    : null;

            Integer slot = (Integer) map.get("slot");
            Integer[] slots = map.containsKey("slots") && map.get("slots") instanceof List<?> slotsList
                    ? slotsList.stream().map(obj -> (Integer) obj).toArray(Integer[]::new)
                    : null;

            String[] commands = map.containsKey("commands") && map.get("commands") instanceof List<?> commandList
                    ? commandList.stream().map(Object::toString).toArray(String[]::new)
                    : null;

            return new MenuButton(material, headTexture, amount, title, titleKey, loreKey, lore, slot, slots, commands);
        } catch (Exception ex) {
            return null;
        }
    }
}
