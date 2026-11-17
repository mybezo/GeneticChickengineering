package net.guizhanss.gcereborn.items.machines;

import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.core.machines.MachineProcessor;
import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.ItemUtils;
import io.github.thebusybiscuit.slimefun4.utils.ChestMenuUtils;

import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenuPreset;

import net.guizhanss.gcereborn.GeneticChickengineering;
import net.guizhanss.gcereborn.core.genetics.DNA;
import net.guizhanss.gcereborn.items.GCEItems;
import net.guizhanss.gcereborn.items.chicken.ChickenTypes;
import net.guizhanss.gcereborn.utils.GuiItems;
import net.guizhanss.gcereborn.utils.ChickenUtils;

public class ExcitationChamber extends AbstractMachine {

    private static final int[] BACKGROUND = new int[] {
        0, 1, 2, 6, 7, 8,
        9, 10, 11, 15, 16, 17,
        18, 19, 20, 21, 23, 24,
        25, 26
    };
    private static final int[] INPUT_BORDER = new int[] {3, 5, 12, 13, 14};
    private static final int[] OUTPUT_BORDER = new int[] {27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 44};
    private static final int[] INPUT_SLOTS = new int[] {4};
    private static final int[] OUTPUT_SLOTS = new int[] {37, 38, 39, 40, 41, 42, 43};

    public ExcitationChamber(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType, ItemStack[] recipe) {
        super(itemGroup, item, recipeType, recipe);
    }

    @Override
    @Nonnull
    public ItemStack getProgressBar() {
        return GCEItems.POCKET_CHICKEN.clone();
    }

    @Override
    public int[] getInputSlots() {
        return INPUT_SLOTS;
    }

    @Override
    public int[] getOutputSlots() {
        return OUTPUT_SLOTS;
    }

    @Override
    protected void constructMenu(@Nonnull BlockMenuPreset preset) {
        for (int i : BACKGROUND) {
            preset.addItem(i, ChestMenuUtils.getBackground(), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int i : INPUT_BORDER) {
            preset.addItem(i, ChestMenuUtils.getInputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }

        for (int i : OUTPUT_BORDER) {
            preset.addItem(i, ChestMenuUtils.getOutputSlotTexture(), ChestMenuUtils.getEmptyClickHandler());
        }

        preset.addItem(INFO_SLOT, GuiItems.BLACK_PANE, ChestMenuUtils.getEmptyClickHandler());

        for (int i : getOutputSlots()) {
            preset.addMenuClickHandler(i, (p, slot, cursor, action) -> cursor != null && !cursor.getType().isAir());
        }
    }

    @Override
    protected void tick(@Nonnull Block b) {
        super.tick(b);
        BlockMenu inv = BlockStorage.getInventory(b);
        MachineProcessor<CraftingOperation> processor = getMachineProcessor();
        CraftingOperation operation = processor.getOperation(b);
        if (operation != null && !canContinue(inv, operation)) {
            processor.endOperation(b);
            inv.replaceExistingItem(INFO_SLOT, GuiItems.BLACK_PANE);
        }
    }

    /**
     * Checks every tick whether the current operation can continue.
     * The chicken can be hurt every tick when pain is enabled.
     *
     * @param menu      The {@link BlockMenu} of the machine.
     * @param operation The current {@link CraftingOperation}.
     * @return Whether the operation can continue.
     */
    protected boolean canContinue(@Nonnull BlockMenu menu, @Nonnull CraftingOperation operation) {
        return findNextRecipe(menu) != null;
    }

    /**
     * Get the duration of producing a resource, in seconds.
     *
     * @param dna The {@link DNA} of the chicken.
     * @return The duration in seconds.
     */
    protected int getDuration(@Nonnull DNA dna) {
        var config = GeneticChickengineering.getConfigService();
        if (config.isTest()) {
            return 1;
        }
        // at least 1, otherwise high speed machines would get a non-positive duration
        return Math.max(1, (config.getResourceBaseTime() + dna.getTier() - 2 * ChickenUtils.getDNAStrength(dna)) / getSpeed());
    }

    /**
     * Apply pain to the chicken when pain is enabled.
     *
     * @param menu    The {@link BlockMenu} of the machine.
     * @param chicken The chicken {@link ItemStack} in the machine.
     * @return Whether the chicken can work.
     */
    protected boolean applyPain(@Nonnull BlockMenu menu, @Nonnull ItemStack chicken) {
        var config = GeneticChickengineering.getConfigService();
        if (!config.isPainEnabled()) {
            return true;
        }
        if (!ChickenUtils.survivesPain(chicken) && !config.isPainDeathEnabled()) {
            return false;
        }
        ChickenUtils.possiblyHarm(chicken);
        if (ChickenUtils.getHealth(chicken) <= 0d) {
            ItemUtils.consumeItem(chicken, false);
            playDeathSound(menu);
            return false;
        }
        return true;
    }

    @Override
    @Nullable
    protected MachineRecipe findNextRecipe(@Nonnull BlockMenu menu) {
        var config = GeneticChickengineering.getConfigService();
        for (int slot : getInputSlots()) {
            ItemStack chicken = menu.getItemInSlot(slot);

            if (!ChickenUtils.isPocketChicken(chicken) || !ChickenUtils.isAdult(chicken)) {
                continue;
            }

            // Set the progress bar to always be the resource, since players
            // can abort the recipe if they know the egg is coming
            DNA dna = ChickenUtils.getDNA(chicken);
            ItemStack resourceIcon = ChickenTypes.getProduct(dna.getTyping());

            ItemStack chickResource;
            if (ThreadLocalRandom.current().nextInt(100) < config.getResourceFailRate()) {
                chickResource = new ItemStack(Material.EGG);
            } else {
                chickResource = resourceIcon.clone();
            }

            /* Speed calculation
             * All recipes have a base speed of 14 (by default)
             * All recipes add 1 second/DNA tier
             * All recipes subtract 2 seconds/DNA strength (dominant pairs)
             *         | normal    | boosted
             *  Tier 0 | 2-14 sec  | 1-7 sec
             *  Tier 1 | 5-15 sec  | 2-7 sec
             *  Tier 2 | 8-16 sec  | 4-8 sec
             *  Tier 3 | 11-17 sec | 5-8 sec
             *  Tier 4 | 14-18 sec | 7-9 sec
             *  Tier 5 | 17-19 sec | 8-9 sec
             *  Tier 6 | 20 sec    | 10 sec
             */
            MachineRecipe recipe = new MachineRecipe(
                getDuration(dna),
                new ItemStack[] {chicken},
                new ItemStack[] {chickResource}
            );
            if (!InvUtils.fitAll(menu.toInventory(), recipe.getOutput(), getOutputSlots())) {
                continue;
            }

            if (!applyPain(menu, chicken)) {
                continue;
            }

            return recipe;
        }

        return null;
    }

}
