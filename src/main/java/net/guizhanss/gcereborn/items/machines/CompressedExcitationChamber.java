package net.guizhanss.gcereborn.items.machines;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.github.thebusybiscuit.slimefun4.api.items.ItemGroup;
import io.github.thebusybiscuit.slimefun4.api.items.SlimefunItemStack;
import io.github.thebusybiscuit.slimefun4.api.recipes.RecipeType;
import io.github.thebusybiscuit.slimefun4.implementation.operations.CraftingOperation;
import io.github.thebusybiscuit.slimefun4.libraries.dough.inventory.InvUtils;
import io.github.thebusybiscuit.slimefun4.libraries.dough.items.CustomItemStack;

import me.mrCookieSlime.Slimefun.Objects.SlimefunItem.abstractItems.MachineRecipe;
import me.mrCookieSlime.Slimefun.api.BlockStorage;
import me.mrCookieSlime.Slimefun.api.inventory.BlockMenu;

import net.guizhanss.gcereborn.GeneticChickengineering;
import net.guizhanss.gcereborn.core.genetics.DNA;
import net.guizhanss.gcereborn.items.chicken.ChickenTypes;
import net.guizhanss.gcereborn.utils.ChickenUtils;

/**
 * A compressed {@link ExcitationChamber}: multiple chambers combined into one.
 * <p>
 * The chicken is not consumed, the amount of chickens in the stack decides how many resources
 * are produced in one operation (up to the multiplier). The speed is the same as the original chamber,
 * the energy consumption is multiplied by the amount of chickens being processed.
 * Only the resources that fit in the output slots are produced.
 */
public class CompressedExcitationChamber extends ExcitationChamber {

    private static final int EGG_MAX_STACK = 16;

    private final int multiplier;

    @ParametersAreNonnullByDefault
    public CompressedExcitationChamber(ItemGroup itemGroup, SlimefunItemStack item, RecipeType recipeType,
                                       ItemStack[] recipe, int multiplier) {
        super(itemGroup, item, recipeType, recipe);
        this.multiplier = multiplier;
    }

    public int getMultiplier() {
        return multiplier;
    }

    /**
     * The amount of chickens processed by the operation, stored as the amount of the ingredient.
     */
    private static int getUnits(@Nullable CraftingOperation operation) {
        if (operation == null || operation.getIngredients().length == 0 || operation.getIngredients()[0] == null) {
            return 1;
        }
        return Math.max(1, operation.getIngredients()[0].getAmount());
    }

    @Override
    protected boolean takeCharge(@Nonnull Location l) {
        if (!isChargeable()) {
            return true;
        }
        // only the chickens being processed consume energy
        long needed = (long) getEnergyConsumption() * getUnits(getMachineProcessor().getOperation(l));
        long charge = getChargeLong(l);
        if (charge < needed) {
            return false;
        }
        setCharge(l, charge - needed);
        return true;
    }

    @Override
    protected void tick(@Nonnull Block b) {
        CraftingOperation operation = getMachineProcessor().getOperation(b);
        if (operation != null && operation.isFinished()) {
            BlockMenu menu = BlockStorage.getInventory(b);
            // wait until the results fit, so no item gets lost
            if (menu == null || !InvUtils.fitAll(menu.toInventory(), operation.getResults(), getOutputSlots())) {
                return;
            }
        }
        super.tick(b);
    }

    @Override
    @Nullable
    protected MachineRecipe findNextRecipe(@Nonnull BlockMenu menu) {
        int failRate = GeneticChickengineering.getConfigService().getResourceFailRate();
        Inventory inv = menu.toInventory();

        for (int slot : getInputSlots()) {
            ItemStack chicken = menu.getItemInSlot(slot);
            if (!ChickenUtils.isPocketChicken(chicken) || !ChickenUtils.isAdult(chicken)) {
                continue;
            }

            DNA dna = ChickenUtils.getDNA(chicken);
            ItemStack product = ChickenTypes.getProduct(dna.getTyping());

            // decide the result of every chicken first (resource or egg)
            int units = Math.min(chicken.getAmount(), multiplier);
            boolean[] failed = new boolean[units];
            ThreadLocalRandom random = ThreadLocalRandom.current();
            for (int i = 0; i < units; i++) {
                failed[i] = random.nextInt(100) < failRate;
            }

            // only produce what fits in the output slots
            int fitting = getFittingUnits(inv, product, failed);
            if (fitting == 0) {
                continue;
            }

            if (!applyPain(menu, chicken)) {
                continue;
            }

            // the ingredient amount stores how many chickens are processed
            ItemStack ingredient = chicken.clone();
            ingredient.setAmount(fitting);
            return new MachineRecipe(
                getDuration(dna),
                new ItemStack[] {ingredient},
                getOutputs(product, failed, fitting)
            );
        }

        return null;
    }

    @Override
    protected boolean canContinue(@Nonnull BlockMenu menu, @Nonnull CraftingOperation operation) {
        int units = getUnits(operation);
        for (int slot : getInputSlots()) {
            ItemStack chicken = menu.getItemInSlot(slot);
            if (!ChickenUtils.isPocketChicken(chicken) || !ChickenUtils.isAdult(chicken)) {
                continue;
            }
            // removing chickens during the operation cancels it
            if (chicken.getAmount() < units) {
                continue;
            }
            if (!InvUtils.fitAll(menu.toInventory(), operation.getResults(), getOutputSlots())) {
                continue;
            }
            if (!applyPain(menu, chicken)) {
                continue;
            }
            return true;
        }
        return false;
    }

    /**
     * Find the max amount of chickens whose results fit in the output slots.
     * The results of the first n chickens fit for every n below the max, so a binary search is used.
     */
    @ParametersAreNonnullByDefault
    private int getFittingUnits(Inventory inv, ItemStack product, boolean[] failed) {
        int low = 0;
        int high = failed.length;
        while (low < high) {
            int mid = (low + high + 1) / 2;
            if (InvUtils.fitAll(inv, getOutputs(product, failed, mid), getOutputSlots())) {
                low = mid;
            } else {
                high = mid - 1;
            }
        }
        return low;
    }

    /**
     * Get the results of the first units chickens, split into valid stacks.
     */
    @Nonnull
    @ParametersAreNonnullByDefault
    private static ItemStack[] getOutputs(ItemStack product, boolean[] failed, int units) {
        int eggs = 0;
        for (int i = 0; i < units; i++) {
            if (failed[i]) {
                eggs++;
            }
        }
        List<ItemStack> outputs = new ArrayList<>();
        addStacks(outputs, product, units - eggs, product.getMaxStackSize());
        addStacks(outputs, new ItemStack(Material.EGG), eggs, EGG_MAX_STACK);
        return outputs.toArray(new ItemStack[0]);
    }

    @ParametersAreNonnullByDefault
    private static void addStacks(List<ItemStack> outputs, ItemStack template, int amount, int maxStackSize) {
        int max = Math.max(1, Math.min(64, maxStackSize));
        while (amount > 0) {
            int size = Math.min(max, amount);
            ItemStack stack = template.clone();
            stack.setAmount(size);
            outputs.add(stack);
            amount -= size;
        }
    }

    @Nonnull
    @Override
    public List<ItemStack> getDisplayRecipes() {
        List<ItemStack> list = new ArrayList<>(super.getDisplayRecipes());
        list.add(new CustomItemStack(
            Material.NETHER_STAR,
            GeneticChickengineering.getLocalization().getString("lores.compressed.multiplier", multiplier)
        ));
        return list;
    }
}
