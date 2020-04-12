package net.doubledoordev.craycrafting.recipe;

import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.annotation.ParametersAreNonnullByDefault;

import com.google.gson.JsonObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.item.crafting.*;
import net.minecraft.profiler.IProfiler;
import net.minecraft.resources.IResourceManager;
import net.minecraft.util.FastRandom;
import net.minecraft.util.ResourceLocation;

@ParametersAreNonnullByDefault
public class CrayRecipeManager extends RecipeManager
{
    public static final CrayRecipeManager INSTANCE = new CrayRecipeManager();

    private static final Logger LOGGER = LogManager.getLogger();
    private static final Random RANDOM = new Random();

    private long seed;
    private long interval;

    private CrayRecipeManager() {}

    public void setSeed(long seed, long interval)
    {
        this.seed = seed;
        this.interval = interval;
    }

    public void randomizeAllRecipes()
    {
        LOGGER.info("Randomizing recipe outputs!");
        RANDOM.setSeed(FastRandom.mix(seed, interval));

        // Shaped recipes
        randomize(getRecipes(recipe -> {
            if (recipe.getType() == IRecipeType.CRAFTING && recipe.getClass() == ShapedRecipe.class)
            {
                return (ShapedRecipe) recipe;
            }
            return null;
        }), ShapedRecipe::getRecipeOutput, (recipe, output) -> recipe.recipeOutput = output);

        // Shapeless recipes
        randomize(getRecipes(recipe -> {
            if (recipe.getType() == IRecipeType.CRAFTING && recipe.getClass() == ShapelessRecipe.class)
            {
                return (ShapelessRecipe) recipe;
            }
            return null;
        }), ShapelessRecipe::getRecipeOutput, (recipe, output) -> recipe.recipeOutput = output);
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonObject> objectIn, IResourceManager resourceManagerIn, IProfiler profilerIn)
    {
        super.apply(objectIn, resourceManagerIn, profilerIn);
        if (seed != 0)
        {
            // Before world is loaded we don't have access to the world seed
            // Otherwise, every time the game triggers a data reload we need to reset our randomization
            randomizeAllRecipes();
        }
    }

    /**
     * Gets the recipes of a particular type
     *
     * @param <R>          The recipe type to return
     * @param recipeMapper A function to map a generic recipe to the recipe type, or null (if it doesn't match)
     * @return A list of all the recipes matching the given type and filter / mapper
     */
    private <R extends IRecipe<?>> List<R> getRecipes(Function<IRecipe<?>, R> recipeMapper)
    {
        return getRecipes()
            .stream()
            .map(recipeMapper)
            .filter(Objects::nonNull)
            .sorted(Comparator.comparing(IRecipe::getId)) // Sort them based on ID initially, so the randomization is always deterministic after
            .collect(Collectors.toList());
    }

    /**
     * Randomizes a set of recipes outputs
     *
     * @param recipes      The set of recipes
     * @param outputGetter A function to get the output (typically ItemStack) from a recipe
     * @param outputSetter A bi function to set the output on a recipe
     * @param <R>          The recipe type
     */
    private <R extends IRecipe<?>, T> void randomize(List<R> recipes, Function<R, T> outputGetter, BiConsumer<R, T> outputSetter)
    {
        // Shuffled Outputs
        List<T> randomizedOutputs = recipes
            .stream()
            .map(outputGetter)
            .collect(Collectors.toList());
        Collections.shuffle(randomizedOutputs, RANDOM);

        // Set each output
        recipes.forEach(recipe -> {
            if (randomizedOutputs.size() > 0)
            {
                outputSetter.accept(recipe, randomizedOutputs.remove(0));
            }
            else
            {
                LOGGER.warn("Unable to match a recipe to an output!");
            }
        });
    }
}
