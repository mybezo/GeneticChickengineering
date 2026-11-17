package net.guizhanss.gcereborn.core.guide;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Predicate;

import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;

/**
 * The main guide menu of the addon: displays the items of the main {@link ItemGroup}
 * and buttons at fixed slots (e.g. "Compressed Items").
 * It is listed in the main menu of the survival guide.
 */
public class MainGuideGroup extends ItemListGroup {

    private final Map<Integer, FixedButton> fixedButtons = new LinkedHashMap<>();

    @ParametersAreNonnullByDefault
    public MainGuideGroup(NamespacedKey key, ItemStack item, ItemGroup source) {
        super(key, item, source);
    }

    /**
     * Place a button at a fixed slot, which opens another {@link GuideMenuGroup}.
     *
     * @param slot      The slot.
     * @param target    The {@link GuideMenuGroup} to open.
     * @param condition Whether the button is shown.
     */
    @ParametersAreNonnullByDefault
    public void setFixedButton(int slot, GuideMenuGroup target, Predicate<Player> condition) {
        fixedButtons.put(slot, new FixedButton(target, condition));
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isVisible(Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        // cheat mode lists the original item group instead
        return mode == SlimefunGuideMode.SURVIVAL_MODE;
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void addFixedItems(ChestMenu menu, Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        fixedButtons.forEach((slot, button) -> {
            if (!button.condition().test(p)) {
                return;
            }
            menu.addItem(slot, button.target().getItem(p), (pl, s, item, action) -> {
                button.target().open(p, profile, mode);
                return false;
            });
        });
    }

    private record FixedButton(GuideMenuGroup target, Predicate<Player> condition) {
    }
}
