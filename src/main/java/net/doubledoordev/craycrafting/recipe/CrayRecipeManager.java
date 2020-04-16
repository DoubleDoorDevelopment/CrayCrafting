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

    // The list of recipe classes we know how to randomize
    public static final Set<Class<? extends IRecipe<?>>> RECIPE_CLASSES = new HashSet<>(Arrays.asList(
        ShapedRecipe.class,
        ShapelessRecipe.class,
        CampfireCookingRecipe.class,
        FurnaceRecipe.class,
        BlastingRecipe.class,
        SmokingRecipe.class,
        StonecuttingRecipe.class
    ));

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

        randomize(getRecipes()
                .stream()
                .filter(recipe -> RECIPE_CLASSES.contains(recipe.getClass()))
                .collect(Collectors.toList()), recipe -> {
                // Get the output from the recipe
                if (RECIPE_CLASSES.contains(recipe.getClass()))
                {
                    return recipe.getRecipeOutput();
                }
                return null;
            },
            (recipe, output) -> {
                // Set the new output on a recipe
                if (recipe.getClass() == ShapedRecipe.class)
                {
                    ((ShapedRecipe) recipe).recipeOutput = output;
                }
                else if (recipe.getClass() == ShapelessRecipe.class)
                {
                    ((ShapelessRecipe) recipe).recipeOutput = output;
                }
                else if (recipe.getClass() == CampfireCookingRecipe.class || recipe.getClass() == FurnaceRecipe.class || recipe.getClass() == SmokingRecipe.class || recipe.getClass() == BlastingRecipe.class)
                {
                    ((AbstractCookingRecipe) recipe).result = output;
                }
                else if (recipe.getClass() == StonecuttingRecipe.class)
                {
                    ((SingleItemRecipe) recipe).result = output;
                }
                else
                {
                    LOGGER.warn("Unable to set the output on a recipe! This could have weird consequences! Recipe = {}, Class = {}, Output = {}", recipe.getId(), recipe.getClass().getSimpleName(), output);
                }
            }
        );
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
        recipes.sort(Comparator.comparing(IRecipe::getId));
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
