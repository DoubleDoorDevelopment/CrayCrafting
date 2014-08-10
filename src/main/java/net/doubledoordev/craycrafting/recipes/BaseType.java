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

import cpw.mods.fml.common.FMLCommonHandler;
import net.doubledoordev.craycrafting.CrayCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.CraftingManager;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.LinkedList;
import java.util.List;

/**
 * Extend to add new recipe type to the system
 * This class registers itself, just make sure you initiate it somewhere.
 *
 * @param <T>
 * @author Dries007
 */
public abstract class BaseType<T extends IRecipe>
{
    final Class<T> type;

    final protected LinkedList<T> addedList   = new LinkedList<T>();
    final protected LinkedList<T> removedList = new LinkedList<T>();
    private         NBTTagList    nbtList     = new NBTTagList();

    boolean applied;

    /**
     * Just pass this the class your type wants. Stupid java...
     */
    @SuppressWarnings("unchecked")
    public BaseType(Class<T> type)
    {
        this.type = type;
        if (!IRecipe.class.isAssignableFrom(type)) throw new IllegalArgumentException("Type must be specified and extend IRecipe.");
        RecipeRegistry.register((BaseType<IRecipe>) this);
    }

    /**
     * Don't miss private fields...
     * Used on server only.
     */
    public abstract NBTTagCompound getNBTFromRecipe(T recipe, ItemStack newOutput) throws IllegalAccessException;

    /**
     * Don't forget to set special properties if required.
     * Used on both sides.
     */
    public abstract T getRecipeFromNBT(NBTTagCompound nbtRecipe);

    /**
     * Check everything EXCEPT the output ItemStack.
     * Used on both sides.
     */
    public abstract boolean equalsExceptOutput(T recipe1, T recipe2) throws IllegalAccessException;

    /**
     * Must be unique!
     * Used on both sides.
     */
    public String getTypeName()
    {
        return type.getSimpleName();
    }

    /**
     * Replaces an instanceof check
     * Used on both sides.
     */
    public boolean accept(IRecipe recipe)
    {
        return type.isAssignableFrom(recipe.getClass());
    }

    /**
     * Used to read from disk and from the packet send to the server.
     * Make sure you don't apply twice!
     * <p/>
     * Used on both sides.
     */
    @SuppressWarnings("unchecked")
    public void loadRecipesFromNBT(NBTTagCompound root) throws Exception
    {
        NBTTagList list = root.getTagList(getTypeName(), 10);
        for (int i = 0; i < list.tagCount(); i++)
        {
            T newRecipe = getRecipeFromNBT(list.getCompoundTagAt(i));
            addedList.add(newRecipe);

            for (IRecipe oldRecipe : (List<IRecipe>) CraftingManager.getInstance().getRecipeList())
            {
                if (type.isAssignableFrom(oldRecipe.getClass())) // instanceof basically
                {
                    if (equalsExceptOutput(newRecipe, (T) oldRecipe))
                    {
                        removedList.add((T) oldRecipe);
                        break;
                    }
                }
            }
        }
        apply();
    }

    /**
     * Used on both sides.
     */
    @SuppressWarnings("unchecked")
    public void apply()
    {
        if (!applied)
        {
            CrayCrafting.instance.logger.info("APPLY " + getTypeName() + "\t Removed " + removedList.size() + "\t  & added " + addedList.size() + "\t recipes from " + FMLCommonHandler.instance().getEffectiveSide());
            applied = true;
            CraftingManager.getInstance().getRecipeList().removeAll(removedList);
            CraftingManager.getInstance().getRecipeList().addAll(addedList);
        }
    }

    /**
     * Used on client only.
     */
    @SuppressWarnings("unchecked")
    public void undo()
    {
        if (applied)
        {
            CrayCrafting.instance.logger.info("UNDO " + getTypeName() + "\t Removed " + removedList.size() + "\t & added " + addedList.size() + "\t recipes from " + FMLCommonHandler.instance().getEffectiveSide());
            applied = false;
            CraftingManager.getInstance().getRecipeList().removeAll(addedList);
            CraftingManager.getInstance().getRecipeList().addAll(removedList);

            addedList.clear();
            removedList.clear();
        }
    }

    /**
     * Used on server only.
     */
    public NBTTagList getNBTList()
    {
        return nbtList;
    }

    /**
     * Used on server only.
     */
    public void applyRandomization(T recipe, ItemStack itemStack)
    {
        try
        {
            removedList.add(recipe);
            NBTTagCompound nbtRecipe = getNBTFromRecipe(recipe, itemStack);
            nbtList.appendTag(nbtRecipe);
            addedList.add(getRecipeFromNBT(nbtRecipe));
        }
        catch (Exception e)
        {
            CrayCrafting.instance.logger.warn("Error in " + getTypeName() + " (" + recipe + "), adding back the original.");
            removedList.remove(recipe);
            e.printStackTrace();
        }
    }
}
