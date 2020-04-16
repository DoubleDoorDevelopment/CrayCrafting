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
        public final ForgeConfigSpec.BooleanValue sendRecipeRandomizationMessage;
        public final ForgeConfigSpec.ConfigValue<String> recipeRandomizationMessage;
        public final ForgeConfigSpec.BooleanValue recipeRandomizationTranslationKey;

        CommonConfig(ForgeConfigSpec.Builder builder)
        {
            builder.push("general");

            unlockAllRecipeBookRecipes = builder
                .comment("Unlock all recipe book recipes when players log in?")
                .translation(MOD_ID + ".config.unlock_all_recipe_book_recipes")
                .define("unlock_all_recipe_book_recipes", true);

            recipeRandomizationTicks = builder
                .comment("Number of ticks before recipes are randomized. 24000 = 1 in game day. Set to 0 to disable.")
                .translation(MOD_ID + ".config.recipe_randomization_ticks")
                .defineInRange("recipe_randomization_ticks", 24000, 1000, Integer.MAX_VALUE);

            sendRecipeRandomizationMessage = builder
                .comment("Should we send the player a chat message when recipes get randomized?")
                .translation(MOD_ID + ".config.send_recipe_randomization_message")
                .define("send_recipe_randomization_message", false);

            recipeRandomizationMessage = builder
                .comment("Message to send to the player when recipes are randomized. This will only be used if recipe_randomization_translation_key is set to false.")
                .translation(MOD_ID + ".config.recipe_randomization_message")
                .define("recipe_randomization_message", "Recipes have been randomized!");

            recipeRandomizationTranslationKey = builder
                .comment("Should the message sent to the player use a translation key? (This works better when both client + server have this mod, but won't work if only the server has this mod)")
                .translation(MOD_ID + ".config.recipe_randomization_translation_key")
                .define("recipe_randomization_translation_key", false);

            builder.pop();
        }
    }
}
