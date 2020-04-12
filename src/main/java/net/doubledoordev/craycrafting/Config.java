package net.doubledoordev.craycrafting;

import org.apache.commons.lang3.tuple.Pair;
import net.minecraftforge.common.ForgeConfigSpec;

import static net.doubledoordev.craycrafting.CrayCrafting.MOD_ID;

public final class Config
{
    public static final CommonConfig COMMON;
    public static final ForgeConfigSpec COMMON_SPEC;

    static
    {
        final Pair<CommonConfig, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(CommonConfig::new);
        COMMON_SPEC = specPair.getRight();
        COMMON = specPair.getLeft();
    }

    public static final class CommonConfig
    {
        public final ForgeConfigSpec.BooleanValue unlockAllRecipeBookRecipes;
        public final ForgeConfigSpec.IntValue recipeRandomizationTicks;

        CommonConfig(ForgeConfigSpec.Builder builder)
        {
            builder.push("general");

            unlockAllRecipeBookRecipes = builder
                .comment("Unlock all recipe book recipes when players log in?")
                .translation(MOD_ID + ".config.unlock_all_recipe_book_recipes")
                .define("unlock_all_recipe_book_recipes", true);

            recipeRandomizationTicks = builder
                .comment("Number of ticks before recipes are randomized. 24000 = 1 in game day. Set to 0 to disable.")
                .translation(MOD_ID + ".config.recipe_randomization_ticks") // todo: change default
                .defineInRange("recipe_randomization_ticks", 1000, 1000, Integer.MAX_VALUE);

            builder.pop();
        }
    }
}
