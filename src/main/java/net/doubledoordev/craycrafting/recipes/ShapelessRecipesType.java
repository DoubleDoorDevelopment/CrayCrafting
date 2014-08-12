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

import net.doubledoordev.craycrafting.util.Helper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.ShapelessRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.util.ArrayList;

import static net.doubledoordev.craycrafting.util.Constants.*;

/**
 * @author Dries007
 */
public class ShapelessRecipesType extends BaseType<ShapelessRecipes>
{
    public ShapelessRecipesType()
    {
        super(ShapelessRecipes.class);
    }

    @Override
    public NBTTagCompound getNBTFromRecipe(ShapelessRecipes recipe, ItemStack newOutput) throws IllegalAccessException
    {
        NBTTagCompound nbtRecipe = new NBTTagCompound();
        NBTTagList NBTInput = new NBTTagList();
        for (Object is : recipe.recipeItems)
        {
            if (is == null) NBTInput.appendTag(new NBTTagCompound());
            else NBTInput.appendTag(((ItemStack) is).writeToNBT(new NBTTagCompound()));
        }
        nbtRecipe.setTag(NBT_input, NBTInput);
        nbtRecipe.setTag(NBT_newOutput, newOutput.writeToNBT(new NBTTagCompound()));
        nbtRecipe.setTag(NBT_oldOutput, recipe.getRecipeOutput().writeToNBT(new NBTTagCompound()));

        return nbtRecipe;
    }

    @Override
    public ShapelessRecipes[] getRecipesFromNBT(NBTTagCompound nbtRecipe)
    {
        ItemStack newOutput = ItemStack.loadItemStackFromNBT(nbtRecipe.getCompoundTag(NBT_newOutput));
        ItemStack oldOutput = ItemStack.loadItemStackFromNBT(nbtRecipe.getCompoundTag(NBT_oldOutput));
        NBTTagList list = nbtRecipe.getTagList(NBT_input, 10);

        ArrayList<ItemStack> input = new ArrayList<>();
        for (int i = 0; i < list.tagCount(); i++) input.add(ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i)));

        return new ShapelessRecipes[] {new DimBasedShapelessRecipes(true, newOutput, input), new DimBasedShapelessRecipes(false, oldOutput, input)};
    }

    public static class DimBasedShapelessRecipes extends ShapelessRecipes
    {
        boolean isCrayRecipe;

        public DimBasedShapelessRecipes(boolean isCrayRecipe, ItemStack newOutput, ArrayList<ItemStack> input)
        {
            super(newOutput, input);
            this.isCrayRecipe = isCrayRecipe;
        }

        @Override
        public boolean matches(InventoryCrafting inv, World world)
        {
            return ((RecipeRegistry.doesCrayApplyTo(world) && isCrayRecipe) || (!RecipeRegistry.doesCrayApplyTo(world) && !isCrayRecipe)) && super.matches(inv, world);
        }
    }

    @Override
    public boolean equalsExceptOutput(ShapelessRecipes recipe1, ShapelessRecipes recipe2) throws IllegalAccessException
    {
        return Helper.inputEquals(recipe1.recipeItems, recipe2.recipeItems);
    }
}
