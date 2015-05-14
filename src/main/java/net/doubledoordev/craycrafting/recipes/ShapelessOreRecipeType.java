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

import net.doubledoordev.craycrafting.CrayCrafting;
import net.doubledoordev.craycrafting.util.Helper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapelessOreRecipe;

import java.util.ArrayList;

import static net.doubledoordev.craycrafting.util.Constants.*;

/**
 * @author Dries007
 */
public class ShapelessOreRecipeType extends BaseType<ShapelessOreRecipe>
{
    public ShapelessOreRecipeType()
    {
        super(ShapelessOreRecipe.class);
    }

    @Override
    public NBTTagCompound getNBTFromRecipe(ShapelessOreRecipe recipe, ItemStack newOutput) throws IllegalAccessException
    {
        NBTTagCompound nbtRecipe = new NBTTagCompound();
        NBTTagList nbtInput = new NBTTagList();

        for (Object o : recipe.getInput())
        {
            if (o instanceof ArrayList)
            {
                for (String name : OreDictionary.getOreNames())
                {
                    if (OreDictionary.getOres(name).equals(o))
                    {
                        NBTTagCompound tag = new NBTTagCompound();
                        tag.setString(NBT_oredictname, name);
                        nbtInput.appendTag(tag);
                        break;
                    }
                }
            }
            else if (o instanceof ItemStack)
            {
                nbtInput.appendTag(((ItemStack) o).writeToNBT(new NBTTagCompound()));
            }
            else
            {
                CrayCrafting.instance.logger.warn("NBT RECIPE ERROR: " + o + " IS NOT STRING OR ITEMSTACK ???");
            }
        }
        nbtRecipe.setTag(NBT_input, nbtInput);
        nbtRecipe.setTag(NBT_newOutput, newOutput.writeToNBT(new NBTTagCompound()));
        nbtRecipe.setTag(NBT_oldOutput, recipe.getRecipeOutput().writeToNBT(new NBTTagCompound()));

        return nbtRecipe;
    }

    @Override
    public ShapelessOreRecipe[] getRecipesFromNBT(NBTTagCompound nbtRecipe)
    {
        ArrayList<Object> input = new ArrayList<Object>();
        ItemStack newOutput = ItemStack.loadItemStackFromNBT(nbtRecipe.getCompoundTag(NBT_newOutput));
        ItemStack oldOutput = ItemStack.loadItemStackFromNBT(nbtRecipe.getCompoundTag(NBT_oldOutput));

        NBTTagList inputs = nbtRecipe.getTagList(NBT_input, 10);
        for (int i = 0; i < inputs.tagCount(); i++)
        {
            NBTTagCompound nbtInput = inputs.getCompoundTagAt(i);
            if (nbtInput.hasKey(NBT_oredictname)) input.add(nbtInput.getString(NBT_oredictname));
            else input.add(ItemStack.loadItemStackFromNBT(nbtInput));
        }

        return new ShapelessOreRecipe[] {new DimBasedShapelessOreRecipe(true, newOutput, input.toArray()), new DimBasedShapelessOreRecipe(false, oldOutput, input.toArray())};
    }

    public static class DimBasedShapelessOreRecipe extends ShapelessOreRecipe
    {
        boolean isCrayRecipe;

        public DimBasedShapelessOreRecipe(boolean isCrayRecipe, ItemStack newOutput, Object[] objects)
        {
            super(newOutput, objects);
            this.isCrayRecipe = isCrayRecipe;
        }

        @Override
        public boolean matches(InventoryCrafting inv, World world)
        {
            return inv != null && world != null && ((RecipeRegistry.doesCrayApplyTo(world) && isCrayRecipe) || (!RecipeRegistry.doesCrayApplyTo(world) && !isCrayRecipe)) && super.matches(inv, world);
        }
    }

    @Override
    public boolean equalsExceptOutput(ShapelessOreRecipe recipe1, ShapelessOreRecipe recipe2) throws IllegalAccessException
    {
        return Helper.inputEquals(recipe1.getInput(), recipe2.getInput());
    }
}
