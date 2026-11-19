package net.guizhanss.gcereborn.core.guide;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItem;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.api.researches.Research;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideImplementation;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.core.multiblocks.MultiBlockMachine;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;

import city.norain.slimefun4.VaultIntegration;

import net.guizhanss.gcereborn.GeneticChickengineering;

/**
 * A guide menu that displays the items of an {@link ItemGroup},
 * the same way the Slimefun guide does (permissions and researches included).
 */
public class ItemListGroup extends GuideMenuGroup {

    private final ItemGroup source;

    @ParametersAreNonnullByDefault
    public ItemListGroup(NamespacedKey key, ItemStack item, ItemGroup source) {
        super(key, item);
        this.source = source;
    }

    @Nonnull
    public ItemGroup getSource() {
        return source;
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    protected List<GuideEntry> getEntries(Player p, PlayerProfile profile, SlimefunGuideMode mode, int page) {
        SlimefunGuideImplementation guide = Slimefun.getRegistry().getSlimefunGuide(mode);
        boolean survival = mode == SlimefunGuideMode.SURVIVAL_MODE;
        List<GuideEntry> entries = new ArrayList<>();

        for (SlimefunItem sfItem : source.getItems()) {
            if (sfItem.isDisabledIn(p.getWorld())) {
                continue;
            }
            Research research = sfItem.getResearch();

            if (survival && !Slimefun.getPermissionsService().hasPermission(p, sfItem)) {
                List<String> message = Slimefun.getPermissionsService().getLore(sfItem);
                entries.add(new GuideEntry(
                    new CustomItemStack(ChestMenuUtils.getNoPermissionItem(), sfItem.getItemName(), message.toArray(new String[0])),
                    ChestMenuUtils.getEmptyClickHandler()
                ));
            } else if (survival && research != null && !profile.hasUnlocked(research)) {
                entries.add(new GuideEntry(getLockedItem(p, sfItem, research), (pl, slot, item, action) -> {
                    research.unlockFromGuide(guide, p, profile, sfItem, this, page);
                    return false;
                }));
            } else {
                entries.add(new GuideEntry(sfItem.getItem(), (pl, slot, item, action) -> {
                    if (survival) {
                        guide.displayItem(profile, sfItem, true);
                    } else if (pl.hasPermission("slimefun.cheat.items") && !(sfItem instanceof MultiBlockMachine)) {
                        ItemStack clone = sfItem.getItem().clone();
                        if (action.isShiftClicked()) {
                            clone.setAmount(clone.getMaxStackSize());
                        }
                        pl.getInventory().addItem(clone);
                    }
                    return false;
                }));
            }
        }

        return entries;
    }

    @Nonnull
    @ParametersAreNonnullByDefault
    private static ItemStack getLockedItem(Player p, SlimefunItem sfItem, Research research) {
        // the addon's own strings are used, not every Slimefun version has the "guide.locked-item" messages
        var localization = GeneticChickengineering.getLocalization();
        String cost;
        if (VaultIntegration.isEnabled()) {
            cost = localization.getString("guide.locked.currency-cost", String.format("%.2f", research.getCurrencyCost()));
        } else {
            cost = localization.getString("guide.locked.level-cost", research.getLevelCost());
        }
        return new CustomItemStack(
            ChestMenuUtils.getNoPermissionItem(),
            "&f" + ItemUtils.getItemName(sfItem.getItem()),
            "&7" + sfItem.getId(),
            "&4&l" + Slimefun.getLocalization().getMessage(p, "guide.locked"),
            "",
            localization.getString("guide.locked.click-to-unlock"),
            "",
            cost
        );
    }
}
