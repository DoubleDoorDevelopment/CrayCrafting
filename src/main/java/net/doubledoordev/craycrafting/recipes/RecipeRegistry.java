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

package net.doubledoordev.craycrafting.recipes;

import com.google.common.collect.Sets;
import cpw.mods.fml.common.FMLCommonHandler;
import net.doubledoordev.craycrafting.CrayCrafting;
import net.doubledoordev.craycrafting.network.ConfigSyncMessage;
import net.doubledoordev.craycrafting.network.RecipeMessage;
import net.doubledoordev.craycrafting.network.ResetMessage;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Replaces a lot of the old helper crap
 *
 * @author Dries007
 */
public class RecipeRegistry
{
    private static final ArrayList<BaseType<IRecipe>> typeList = new ArrayList<>();
    /**
     * True means that the list is a whitelist. Craycrafting only applies in the dimensions in the list.
     * False means that the list is a blacklist. Craycrafting applies in all dimensions except the ones in the list
     */
    public static boolean      listType;
    public final static Set<Integer> list = new HashSet<>();

    /**
     * Used to read from disk and from the packet send to the server.
     * Used on both sides.
     */
    public static void loadRecipesFromNBT(NBTTagCompound root) throws Exception
    {
        for (BaseType<IRecipe> baseType : typeList)
        {
            if (root.hasKey(baseType.getTypeName())) baseType.loadRecipesFromNBT(root);
        }
    }

    /**
     * Used on client only.
     */
    public static void undo()
    {
        for (BaseType<IRecipe> baseType : typeList)
        {
            baseType.undo();
        }
    }

    /**
     * Used on both sides.
     */
    protected static void register(BaseType<IRecipe> baseType)
    {
        typeList.add(baseType);
    }

    public static void setConfigFromServer(boolean listTypeP, Integer[] listP)
    {
        listType = listTypeP;
        list.clear();
        list.addAll(Sets.newHashSet(listP));
    }

    public static boolean doesCrayApplyTo(World world)
    {
        if (listType) return list.contains(world.provider.dimensionId);
        else return !list.contains(world.provider.dimensionId);
    }

    public static void sendPacketTo(EntityPlayer player)
    {
        CrayCrafting.getSnw().sendTo(new ResetMessage(), (EntityPlayerMP) player);
        CrayCrafting.getSnw().sendTo(new ConfigSyncMessage(CrayCrafting.instance.listType, CrayCrafting.instance.list), (EntityPlayerMP) player);
        for (BaseType<IRecipe> baseType : typeList)
        {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            NBTTagList nbtTagList = baseType.getNBTList();
            nbtTagCompound.setTag(baseType.getTypeName(), baseType.getNBTList());
            CrayCrafting.instance.logger.info("Sending " + baseType.getTypeName() + " " + nbtTagList.tagCount());
            CrayCrafting.getSnw().sendTo(new RecipeMessage(nbtTagCompound), (EntityPlayerMP) player);
        }
    }

    public static void sendPacketToAll()
    {
        CrayCrafting.getSnw().sendToAll(new ResetMessage());
        CrayCrafting.getSnw().sendToAll(new ConfigSyncMessage(CrayCrafting.instance.listType, CrayCrafting.instance.list));
        for (BaseType<IRecipe> baseType : typeList)
        {
            NBTTagCompound nbtTagCompound = new NBTTagCompound();
            NBTTagList nbtTagList = baseType.getNBTList();
            nbtTagCompound.setTag(baseType.getTypeName(), nbtTagList);
            CrayCrafting.instance.logger.info("Sending " + baseType.getTypeName() + " " + nbtTagList.tagCount());
            CrayCrafting.getSnw().sendToAll(new RecipeMessage(nbtTagCompound));
        }
    }

    /**
     * Used on both sides.
     */
    public static void randomizeRecipes(File recipeFile)
    {
        CrayCrafting.instance.logger.warn("randomizeRecipes " + FMLCommonHandler.instance().getSide() + " " + FMLCommonHandler.instance().getEffectiveSide());
        NBTTagCompound root = new NBTTagCompound();
        ArrayList<ItemStack> outputs = new ArrayList<ItemStack>(CraftingManager.getInstance().getRecipeList().size());
        ArrayList<IRecipe> acceptedRecipes = new ArrayList<IRecipe>();

		for (BaseType<IRecipe> baseType : typeList)
		{
			baseType.resetNBTList();
		}
		
        for (IRecipe recipe : (List<IRecipe>) CraftingManager.getInstance().getRecipeList())
        {
            for (BaseType<IRecipe> baseType : typeList)
            {
                if (baseType.accept(recipe))
                {
                    outputs.add(recipe.getRecipeOutput());
                    acceptedRecipes.add(recipe);

                    break;
                }
            }
        }

        Collections.shuffle(outputs);
        int outputIndex = 0;

        for (IRecipe recipe : acceptedRecipes)
        {
            for (BaseType<IRecipe> baseType : typeList)
            {
                if (baseType.accept(recipe))
                {
                    baseType.applyRandomization(recipe, outputs.get(outputIndex++));
                    break;
                }
            }
        }

        if (outputIndex != outputs.size())
        {
            CrayCrafting.instance.logger.warn("*********************************************************************************************");
            CrayCrafting.instance.logger.warn("** We have items left over? Index: " + outputIndex + ", Output size: " + outputs.size());
            CrayCrafting.instance.logger.warn("** There will be uncraftable items. Please report with ENTIRE log and modlist.");
            CrayCrafting.instance.logger.warn("*********************************************************************************************");
        }

        for (BaseType<IRecipe> baseType : typeList)
        {
            baseType.apply();
            root.setTag(baseType.getTypeName(), baseType.getNBTList());
        }

        try
        {
            CompressedStreamTools.write(root, recipeFile);
        }
        catch (IOException e)
        {
            CrayCrafting.instance.logger.warn("*********************************************************************************************");
            CrayCrafting.instance.logger.warn("** Fuck me. Something went wrong when saving the recipe file.");
            CrayCrafting.instance.logger.warn("** Please report with ENTIRE log and modlist.");
            CrayCrafting.instance.logger.warn("*********************************************************************************************");
            e.printStackTrace();
        }
    }
}
