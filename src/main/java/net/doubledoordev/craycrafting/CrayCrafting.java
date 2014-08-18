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

import com.google.common.base.Strings;
import cpw.mods.fml.client.config.IConfigElement;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import cpw.mods.fml.common.event.FMLServerStoppingEvent;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.network.NetworkRegistry;
import cpw.mods.fml.common.network.simpleimpl.SimpleNetworkWrapper;
import cpw.mods.fml.relauncher.Side;
import net.doubledoordev.craycrafting.network.ConfigSyncMessage;
import net.doubledoordev.craycrafting.network.RecipeMessage;
import net.doubledoordev.craycrafting.network.ResetMessage;
import net.doubledoordev.craycrafting.recipes.*;
import net.doubledoordev.d3core.util.ID3Mod;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.ConfigElement;
import net.minecraftforge.common.config.Configuration;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static net.doubledoordev.craycrafting.util.Constants.MODID;

/**
 * @author Dries007
 */
@Mod(modid = MODID, canBeDeactivated = false)
public class CrayCrafting implements ID3Mod
{
    @Mod.Instance(MODID)
    public static CrayCrafting instance;

    public Logger logger;
    private SimpleNetworkWrapper snw;
    private File recipeFile;
    private Configuration configuration;

    public int     timer = 0;
    public String  timermessage = "[CrayCrafting] Recipes have been rotated!";
    public boolean listType = false;
    public Integer[] list = new Integer[0];

    public static SimpleNetworkWrapper getSnw()
    {
        return instance.snw;
    }

    @Mod.EventHandler
    public void preInit(FMLPreInitializationEvent event)
    {
        logger = event.getModLog();
        MinecraftForge.EVENT_BUS.register(this);
        FMLCommonHandler.instance().bus().register(this);

        configuration = new Configuration(event.getSuggestedConfigurationFile());
        syncConfig();

        new ShapedRecipesType();
        new ShapelessRecipesType();
        new ShapedOreRecipeType();
        new ShapelessOreRecipeType();

        int id = 0;
        snw = NetworkRegistry.INSTANCE.newSimpleChannel(MODID);
        snw.registerMessage(RecipeMessage.Handler.class, RecipeMessage.class, id++, Side.CLIENT);
        snw.registerMessage(ResetMessage.Handler.class, ResetMessage.class, id++, Side.CLIENT);
        snw.registerMessage(ConfigSyncMessage.Handler.class, ConfigSyncMessage.class, id++, Side.CLIENT);
    }

    @Mod.EventHandler()
    public void eventHandler(FMLServerStoppingEvent event)
    {
        if (!MinecraftServer.getServer().isDedicatedServer()) RecipeRegistry.undo();
    }

    @Mod.EventHandler()
    public void eventHandler(FMLServerStartingEvent event)
    {
        RecipeRegistry.setConfigFromServer(listType, list);

        recipeFile = new File(DimensionManager.getCurrentSaveRootDirectory(), MODID + ".dat");
        if (recipeFile.exists())
        {
            try
            {
                RecipeRegistry.loadRecipesFromNBT(CompressedStreamTools.read(recipeFile));
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }
        else
        {
            RecipeRegistry.randomizeRecipes(recipeFile);
        }

        if (timer > 0) setupTimer();
    }

    public void setupTimer()
    {
        if (timer >= 1)
        {
            new Timer(MODID + "-Timer").schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    logger.warn("Recipe timer! Resetting all the recipes!");
                    RecipeRegistry.undo();
                    RecipeRegistry.randomizeRecipes(recipeFile);
                    if (MinecraftServer.getServer().isDedicatedServer()) RecipeRegistry.sendPacketToAll();

                    if (!Strings.isNullOrEmpty(timermessage)) MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentText(timermessage));

                    setupTimer();
                }
            }, 1000 * 60 * timer);
        }
    }

    @SubscribeEvent
    public void loginEvent(PlayerEvent.PlayerLoggedInEvent event)
    {
        if (MinecraftServer.getServer().isDedicatedServer()) RecipeRegistry.sendPacketTo(event.player);
    }

    @Override
    public void syncConfig()
    {
        configuration.setCategoryLanguageKey(MODID, "d3.craycrafting.config.craycrafting");
        configuration.setCategoryRequiresWorldRestart(MODID, true);

        timer = configuration.get(MODID, "resetTimer", timer, "For extra evil, this timer rotates the crafting every X minutes. 0 for disable.").getInt();
        timermessage = configuration.get(MODID, "timermessage", timermessage, "Message to be send to all players on timer. Empty = no message").getString();

        listType = configuration.getBoolean("listType", MODID, listType, "True means that the list is a whitelist. Craycrafting only applies in the dimensions in the list.\nFalse means that the list is a blacklist. Craycrafting applies in all dimensions except the ones in the list");
        int[] templist = configuration.get(MODID, "list", new int[0], "The black/whitelist. See listType.").getIntList();
        list = new Integer[templist.length];
        for (int i = 0; i < templist.length; i++) list[i] = templist[i];
        if (configuration.hasChanged()) configuration.save();
    }

    @Override
    public void addConfigElements(List<IConfigElement> configElements)
    {
        configElements.add(new ConfigElement(configuration.getCategory(MODID.toLowerCase())));
    }
}
