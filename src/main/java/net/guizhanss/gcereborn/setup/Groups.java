package net.guizhanss.gcereborn.setup;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;

import net.guizhanss.gcereborn.GeneticChickengineering;
import net.guizhanss.gcereborn.core.guide.GroupListGroup;
import net.guizhanss.gcereborn.core.guide.HiddenItemGroup;
import net.guizhanss.gcereborn.core.guide.ItemListGroup;
import net.guizhanss.gcereborn.core.guide.MainGuideGroup;
import net.guizhanss.gcereborn.utils.Heads;
import net.guizhanss.gcereborn.utils.Keys;

import lombok.experimental.UtilityClass;

@UtilityClass
public final class Groups {

    /**
     * The slot of the "Compressed Items" button in the main menu of the addon.
     */
    private static final int COMPRESSED_ITEMS_SLOT = 40;

    private static final boolean COMPRESSED_ENABLED = GeneticChickengineering.getConfigService().isCompressedEnabled();

    private static final ItemStack MAIN_ICON = GeneticChickengineering.getLocalization().getItemGroupItem(
        "ICON",
        Heads.CHICKEN.getTexture()
    );

    /**
     * The item group that holds the main items.
     * When compressed items are enabled, the survival guide shows it through {@link #MAIN_GUIDE},
     * which adds the "Compressed Items" button.
     */
    public static final ItemGroup MAIN = COMPRESSED_ENABLED
        ? new HiddenItemGroup(Keys.get("genetic_chickengineering"), MAIN_ICON)
        : new ItemGroup(Keys.get("genetic_chickengineering"), MAIN_ICON);

    public static final ItemGroup DICTIONARY = new ItemGroup(
        Keys.get("genetic_chickengineering_chickens"),
        GeneticChickengineering.getLocalization().getItemGroupItem(
            "DIRECTORY_ICON",
            Material.BLAST_FURNACE
        )
    );

    /**
     * The compressed machines listed in the guide (the highest enabled multiplier).
     */
    private static final ItemStack COMPRESSED_MACHINES_ICON = GeneticChickengineering.getLocalization().getItemGroupItem(
        "COMPRESSED_MACHINES",
        Heads.CHICKEN.getTexture()
    );

    public static final ItemGroup COMPRESSED_MACHINES = new HiddenItemGroup(
        Keys.get("genetic_chickengineering_compressed_machines"),
        COMPRESSED_MACHINES_ICON
    );

    /**
     * The lower multipliers of compressed machines, they are only shown in recipes.
     */
    public static final ItemGroup COMPRESSED_HIDDEN = new HiddenItemGroup(
        Keys.get("genetic_chickengineering_compressed_hidden"),
        GeneticChickengineering.getLocalization().getItemGroupItem(
            "COMPRESSED_HIDDEN",
            Material.BARRIER
        )
    );

    /**
     * The "Compressed Items" menu, opened from the main menu of the addon.
     */
    public static final GroupListGroup COMPRESSED_ITEMS = new GroupListGroup(
        Keys.get("genetic_chickengineering_compressed_items"),
        GeneticChickengineering.getLocalization().getItemGroupItem(
            "COMPRESSED_ITEMS",
            Heads.MYBEZO.getTexture()
        )
    );

    /**
     * Setup the guide menus of compressed items, must be called after all items are registered.
     */
    public static void setupGuide() {
        if (!COMPRESSED_ENABLED) {
            return;
        }
        COMPRESSED_ITEMS.addGroups(new ItemListGroup(
            Keys.get("genetic_chickengineering_compressed_machines_menu"),
            COMPRESSED_MACHINES_ICON,
            COMPRESSED_MACHINES
        ));

        MainGuideGroup mainGuide = new MainGuideGroup(Keys.get("genetic_chickengineering_guide"), MAIN_ICON, MAIN);
        mainGuide.setFixedButton(COMPRESSED_ITEMS_SLOT, COMPRESSED_ITEMS, p -> COMPRESSED_ITEMS.hasContent());
        mainGuide.register(GeneticChickengineering.getInstance());
    }
}
