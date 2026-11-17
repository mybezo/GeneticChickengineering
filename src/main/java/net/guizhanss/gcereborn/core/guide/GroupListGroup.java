package net.guizhanss.gcereborn.core.guide;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;

/**
 * A guide menu that lists other {@link ItemListGroup}s, clicking one opens it.
 * Groups without items are not listed.
 */
public class GroupListGroup extends GuideMenuGroup {

    private final List<ItemListGroup> groups = new ArrayList<>();

    @ParametersAreNonnullByDefault
    public GroupListGroup(NamespacedKey key, ItemStack item) {
        super(key, item);
    }

    @ParametersAreNonnullByDefault
    public void addGroups(ItemListGroup... groups) {
        this.groups.addAll(List.of(groups));
    }

    /**
     * Whether any of the listed groups has items.
     *
     * @return Whether this menu has content.
     */
    public boolean hasContent() {
        for (ItemListGroup group : groups) {
            if (!group.getSource().getItems().isEmpty()) {
                return true;
            }
        }
        return false;
    }

    @Nonnull
    @Override
    @ParametersAreNonnullByDefault
    protected List<GuideEntry> getEntries(Player p, PlayerProfile profile, SlimefunGuideMode mode, int page) {
        List<GuideEntry> entries = new ArrayList<>();
        for (ItemListGroup group : groups) {
            if (group.getSource().getItems().isEmpty()) {
                continue;
            }
            entries.add(new GuideEntry(group.getItem(p), (pl, slot, item, action) -> {
                group.open(p, profile, mode);
                return false;
            }));
        }
        return entries;
    }
}
