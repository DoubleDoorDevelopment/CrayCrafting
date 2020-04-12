/*
 * Copyright (c) 2014, DoubleDoorDevelopment
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 *  Neither the name of the project nor the names of its
 *   contributors may be used to endorse or promote products derived from
 *   this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package net.doubledoordev.craycrafting;

import java.util.Collection;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.item.crafting.RecipeManager;
import net.minecraft.network.play.server.SUpdateRecipesPacket;
import net.minecraft.resources.IFutureReloadListener;
import net.minecraft.resources.SimpleReloadableResourceManager;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.server.FMLServerAboutToStartEvent;

import net.doubledoordev.craycrafting.recipe.CrayRecipeManager;


/**
 * @author Dries007, AlcatrazEscapee
 */
@Mod(CrayCrafting.MOD_ID)
public final class CrayCrafting
{
    public static final String MOD_ID = "craycrafting";

    private static final Logger LOGGER = LogManager.getLogger();

    public CrayCrafting()
    {
        LOGGER.info("Oh! Girl, you got me actin' so cray cray. When you tell me you won't be my baby. - Sev'ral Timez");
        LOGGER.debug("Hello debug logging");

        // Setup config
        ModLoadingContext.get().registerConfig(ModConfig.Type.COMMON, Config.COMMON_SPEC);

        // Register event handlers
        MinecraftForge.EVENT_BUS.register(this);
    }

    /**
     * Unlock all recipes for a player when they log in
     * Same as /recipe give @p *
     * Copied from {@link net.minecraft.command.impl.RecipeCommand}
     *
     * @param event {@link PlayerEvent.PlayerLoggedInEvent}
     */
    @SubscribeEvent
    public void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (Config.COMMON.unlockAllRecipeBookRecipes.get())
        {
            PlayerEntity player = event.getPlayer();
            if (player.getServer() != null)
            {
                Collection<IRecipe<?>> recipes = player.getServer().getRecipeManager().getRecipes();
                int unlocked = player.unlockRecipes(recipes);
                if (unlocked > 0)
                {
                    player.sendMessage(new TranslationTextComponent("commands.recipe.give.success.single", unlocked, player.getDisplayName()));
                }
            }
        }
    }

    /**
     * We first randomize recipe outputs on world load, as that's the earliest we can access the seed
     * Since we do this out of sync with vanilla's recipe reloading, we need to do our own syncing
     *
     * @param event {@link WorldEvent.Load}
     */
    @SubscribeEvent
    public void onWorldLoad(WorldEvent.Load event)
    {
        if (!event.getWorld().isRemote() && event.getWorld().getDimension().getType() == DimensionType.OVERWORLD)
        {
            LOGGER.debug("World load - initializing cray recipe manager with world seed and reloading recipes.");
            // Initialize crafting randomizer based on world seed
            triggerRecipeReload(event.getWorld());
        }
    }

    /**
     * Listen to world (overworld) ticks to know when to sync recipes
     *
     * @param event {@link TickEvent.WorldTickEvent}
     */
    @SubscribeEvent
    public void onWorldTick(TickEvent.WorldTickEvent event)
    {
        if (!event.world.isRemote && event.world.getDimension().getType() == DimensionType.OVERWORLD)
        {
            long interval = Config.COMMON.recipeRandomizationTicks.get();
            if (event.world.getGameTime() != 0 && interval != 0 && event.world.getGameTime() % interval == 0)
            {
                LOGGER.debug("Triggering recipe reload due to random interval elapsed!");
                triggerRecipeReload(event.world);
            }
        }
    }

    @SubscribeEvent
    public void onServerAboutToStart(FMLServerAboutToStartEvent event)
    {
        // This is the dirty hacks that make this mod work
        // We replace the vanilla RecipeManager, but it's already too late!
        // We need to also replace it's entry in the resource reload listener
        LOGGER.debug("Replacing vanilla recipe manager!");
        if (event.getServer().getResourceManager() instanceof SimpleReloadableResourceManager)
        {
            SimpleReloadableResourceManager resourceManager = (SimpleReloadableResourceManager) event.getServer().getResourceManager();
            RecipeManager recipeManager = event.getServer().getRecipeManager();

            // Replace the server recipe manager
            event.getServer().recipeManager = CrayRecipeManager.INSTANCE;

            // Replace the entry in the resource manager
            replaceRecipeManagerInList(resourceManager.reloadListeners, recipeManager);
            replaceRecipeManagerInList(resourceManager.initTaskQueue, recipeManager);
        }
        else
        {
            LOGGER.error("Unknown resource manager, unable to replace! This mod will not function!");
        }
    }

    private void replaceRecipeManagerInList(List<IFutureReloadListener> list, IFutureReloadListener oldItem)
    {
        int i = list.indexOf(oldItem);
        if (i != -1)
        {
            list.set(i, CrayRecipeManager.INSTANCE);
        }
        else
        {
            LOGGER.error("Unable to replace recipe manager, many things may be broken!");
        }
    }

    private void triggerRecipeReload(IWorld world)
    {
        if (world instanceof ServerWorld)
        {
            long interval = 0;
            if (Config.COMMON.recipeRandomizationTicks.get() != 0)
            {
                interval = (1 + ((ServerWorld) world).getGameTime()) / Config.COMMON.recipeRandomizationTicks.get();
            }
            CrayRecipeManager.INSTANCE.setSeed(world.getSeed(), interval);
            CrayRecipeManager.INSTANCE.randomizeAllRecipes();
            world.getPlayers().forEach(player -> player.sendMessage(new TranslationTextComponent(MOD_ID + ".message.randomized_recipes")));

            // Copied from PlayerList, to sync recipe changes
            SUpdateRecipesPacket updateRecipePacket = new SUpdateRecipesPacket(CrayRecipeManager.INSTANCE.getRecipes());
            ((ServerWorld) world).getServer().getPlayerList().getPlayers().forEach(player -> {
                player.connection.sendPacket(updateRecipePacket);
                player.getRecipeBook().init(player);
            });
        }
        else
        {
            LOGGER.warn("Not a server world, unable to reload recipes.");
        }
    }
}
