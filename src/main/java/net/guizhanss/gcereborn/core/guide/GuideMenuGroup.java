package net.guizhanss.gcereborn.core.guide;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.ChatColor;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.groups.FlexItemGroup;
import io.github.thebusybiscuit.slimefun4.api.player.PlayerProfile;
import io.github.thebusybiscuit.slimefun4.core.guide.GuideHistory;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideImplementation;
import io.github.thebusybiscuit.slimefun4.core.guide.SlimefunGuideMode;
import io.github.thebusybiscuit.slimefun4.core.services.sounds.SoundEffect;
import io.github.thebusybiscuit.slimefun4.implementation.Slimefun;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;

import me.mrCookieSlime.CSCoreLibPlugin.general.Inventory.ChestMenu;

/**
 * A guide menu that lists {@link GuideEntry entries} with the same layout as the Slimefun guide
 * (header, back button, 36 entries per page, page buttons in the footer).
 * <p>
 * By default, it is not listed in the main menu of the guide.
 */
public abstract class GuideMenuGroup extends FlexItemGroup {

    private static final int[] HEADER = {0, 2, 3, 4, 5, 6, 7, 8};
    private static final int[] FOOTER = {45, 46, 47, 48, 49, 50, 51, 52, 53};
    private static final int BACK_SLOT = 1;
    private static final int PREV_SLOT = 46;
    private static final int NEXT_SLOT = 52;
    private static final int FIRST_SLOT = 9;
    private static final int PAGE_SIZE = 36;

    @ParametersAreNonnullByDefault
    protected GuideMenuGroup(NamespacedKey key, ItemStack item) {
        super(key, item);
    }

    @Override
    @ParametersAreNonnullByDefault
    public boolean isVisible(Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        return false;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void open(Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        open(p, profile, mode, 1);
    }

    /**
     * Get the entries of this menu.
     *
     * @param p       The {@link Player}.
     * @param profile The {@link PlayerProfile}.
     * @param mode    The {@link SlimefunGuideMode}.
     * @param page    The page that is being opened.
     * @return The {@link GuideEntry entries}.
     */
    @Nonnull
    @ParametersAreNonnullByDefault
    protected abstract List<GuideEntry> getEntries(Player p, PlayerProfile profile, SlimefunGuideMode mode, int page);

    /**
     * Add items at fixed slots, they are added after the entries on every page.
     *
     * @param menu    The {@link ChestMenu}.
     * @param p       The {@link Player}.
     * @param profile The {@link PlayerProfile}.
     * @param mode    The {@link SlimefunGuideMode}.
     */
    @ParametersAreNonnullByDefault
    protected void addFixedItems(ChestMenu menu, Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        // none by default
    }

    @ParametersAreNonnullByDefault
    public void open(Player p, PlayerProfile profile, SlimefunGuideMode mode, int page) {
        if (mode == SlimefunGuideMode.SURVIVAL_MODE) {
            profile.getGuideHistory().add(this, page);
        }

        ChestMenu menu = new ChestMenu(Slimefun.getLocalization().getMessage(p, "guide.title.main"));
        menu.setEmptySlotsClickable(false);
        menu.addMenuOpeningHandler(SoundEffect.GUIDE_BUTTON_CLICK_SOUND::playFor);

        for (int slot : HEADER) {
            menu.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        for (int slot : FOOTER) {
            menu.addItem(slot, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }
        addBackButton(menu, p, profile, mode);

        List<GuideEntry> entries = getEntries(p, profile, mode, page);
        int pages = Math.max(1, (entries.size() + PAGE_SIZE - 1) / PAGE_SIZE);
        int currentPage = Math.min(Math.max(page, 1), pages);
        int start = (currentPage - 1) * PAGE_SIZE;
        int end = Math.min(start + PAGE_SIZE, entries.size());

        for (int i = start; i < end; i++) {
            GuideEntry entry = entries.get(i);
            menu.addItem(FIRST_SLOT + i - start, entry.item(), entry.handler());
        }

        menu.addItem(PREV_SLOT, ChestMenuUtils.getPreviousButton(p, currentPage, pages), (pl, slot, item, action) -> {
            if (currentPage > 1) {
                open(p, profile, mode, currentPage - 1);
            }
            return false;
        });
        menu.addItem(NEXT_SLOT, ChestMenuUtils.getNextButton(p, currentPage, pages), (pl, slot, item, action) -> {
            if (currentPage < pages) {
                open(p, profile, mode, currentPage + 1);
            }
            return false;
        });

        addFixedItems(menu, p, profile, mode);

        menu.open(p);
    }

    @ParametersAreNonnullByDefault
    private static void addBackButton(ChestMenu menu, Player p, PlayerProfile profile, SlimefunGuideMode mode) {
        SlimefunGuideImplementation guide = Slimefun.getRegistry().getSlimefunGuide(mode);
        GuideHistory history = profile.getGuideHistory();

        if (mode == SlimefunGuideMode.SURVIVAL_MODE && history.size() > 1) {
            menu.addItem(BACK_SLOT, ChestMenuUtils.getBackButton(p, "",
                "&fLeft Click: &7Return to previous page",
                "&fShift + Left Click: &7Return to main menu"
            ), (pl, slot, item, action) -> {
                if (action.isShiftClicked()) {
                    guide.openMainMenu(profile, history.getMainMenuPage());
                } else {
                    history.goBack(guide);
                }
                return false;
            });
        } else {
            menu.addItem(BACK_SLOT, ChestMenuUtils.getBackButton(p, "",
                ChatColor.GRAY + Slimefun.getLocalization().getMessage(p, "guide.back.guide")
            ), (pl, slot, item, action) -> {
                guide.openMainMenu(profile, history.getMainMenuPage());
                return false;
            });
        }
    }
}
