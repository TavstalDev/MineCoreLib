package io.github.tavstaldev.minecorelib.models.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.samjakob.spigui.buttons.SGButton;
import com.samjakob.spigui.menu.SGMenu;
import io.github.tavstaldev.minecorelib.core.GuiDupeDetector;
import io.github.tavstaldev.minecorelib.core.PluginTranslator;
import io.github.tavstaldev.minecorelib.utils.ChatUtils;
import io.github.tavstaldev.minecorelib.utils.GuiUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
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
    private @Nullable final List<String> lore;
    private @Nullable final Integer slot;
    private @Nullable final List<String> slots;
    private @Nullable final List<String> commands;

    private List<Integer> slotCache = null;

    public MenuButton(Material material,
                      @Nullable String headTexture,
                      Integer amount,
                      @Nullable String title,
                      @Nullable String titleKey,
                      @Nullable String loreKey,
                      @Nullable List<String> lore,
                      @Nullable Integer slot,
                      @Nullable List<String> slots,
                      @Nullable List<String> commands) {
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

    public Material getMaterial() {return material; }
    public @Nullable String getHeadTexture() { return headTexture; }
    public Integer getAmount() { return  amount; }
    public @Nullable String getTitle() { return title; }
    public @Nullable String getTitleKey() { return titleKey; }
    public @Nullable String getLoreKey() { return  loreKey; }
    public @Nullable List<String> getLore() { return lore; }
    public @Nullable Integer getSlot() { return slot; }
    public @Nullable List<String> getSlotList() { return slots; }
    public @Nullable List<String> getCommands() { return commands; }

    public ItemStack toItemStack(Player player, PluginTranslator translator) {
        ItemStack itemStack = new ItemStack(material, amount);
        ItemMeta meta = itemStack.getItemMeta();
        if (titleKey != null) {
            meta.displayName(ChatUtils.translateColors(translator.localize(player, titleKey), true));
        } else if (title != null) {
            meta.displayName(ChatUtils.translateColors(title,true));
        }

        List<Component> lore = new ArrayList<>();
        if (loreKey != null) {
            String[] loreLines = translator.localizeArray(player, loreKey);
            for (String line : loreLines) {
                lore.add(ChatUtils.translateColors(line, true));
            }
        } else if (this.lore != null) {
            for (String line : this.lore) {
                if (line == null)
                    continue;
                lore.add(ChatUtils.translateColors(line, true));
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

    public List<Integer> getSlots() {
        if (slots != null) {
            if (slotCache != null)
                return slotCache;
            slotCache = GuiUtils.resolveSlots(slots);
            return slotCache;
        }
        else if (slot != null) {
            return List.of(slot);
        }
        else {
            return new ArrayList<>();
        }
    }

    public void apply(final Player player, final PluginTranslator translator, SGMenu sgMenu, MenuBase menu) {
        List<Integer> slots = getSlots();
        if (slots.isEmpty())
            return;

        String playerName = player.getName();
        List<String> commands = getCommands();
        SGButton btn = get(player, translator);
        if (commands != null && !commands.isEmpty()) {
            btn = btn.withListener((InventoryClickEvent event) -> {
                for (String cmd : commands) {
                    menu.executeCommand(player, cmd.replace("%player%", playerName));
                }
            });
        }
        for (Integer slot : slots) {
            sgMenu.setButton(0, slot, btn);
        }
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        if (headTexture != null) {
            map.put("head", headTexture);
        } else {
            map.put("material", material.toString());
        }
        map.put("amount", amount);
        if (title != null) {
            map.put("title", title);
        }
        if (titleKey != null) {
            map.put("titleKey", titleKey);
        }
        if (loreKey != null) {
            map.put("loreKey", loreKey);
        }
        if (lore != null) {
            map.put("lore", lore);
        }
        if (slot != null) {
            map.put("slot", slot);
        }
        if (slots != null) {
            map.put("slots", slots);
        }
        if (commands != null) {
            map.put("commands", commands);
        }
        return map;
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

            List<String> lore = map.containsKey("lore") && map.get("lore") instanceof List<?> loreList
                    ? loreList.stream().map(Object::toString).toList()
                    : null;

            Integer slot = (Integer) map.get("slot");
            List<String> slots = map.containsKey("slots") && map.get("slots") instanceof List<?> slotsList
                    ? slotsList.stream().map(Object::toString).toList()
                    : null;

            List<String> commands = map.containsKey("commands") && map.get("commands") instanceof List<?> commandList
                    ? commandList.stream().map(Object::toString).toList()
                    : null;

            return new MenuButton(material, headTexture, amount, title, titleKey, loreKey, lore, slot, slots, commands);
        } catch (Exception ex) {
            return null;
        }
    }
}
