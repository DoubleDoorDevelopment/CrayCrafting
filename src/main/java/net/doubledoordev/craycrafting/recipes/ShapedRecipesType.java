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
import net.minecraft.item.crafting.ShapedRecipes;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.lang.reflect.Field;

import static net.doubledoordev.craycrafting.util.Constants.*;

/**
 * @author Dries007
 */
public class ShapedRecipesType extends BaseType<ShapedRecipes>
{
    public static final Field ShapedRecipes_field_92101_f;

    static
    {
        ShapedRecipes_field_92101_f = ShapedRecipes.class.getDeclaredFields()[4];
        ShapedRecipes_field_92101_f.setAccessible(true);
    }

    public ShapedRecipesType()
    {
        super(ShapedRecipes.class);
    }

    @Override
    public NBTTagCompound getNBTFromRecipe(ShapedRecipes recipe, ItemStack newOutput) throws IllegalAccessException
    {
        NBTTagCompound nbtRecipe = new NBTTagCompound();
        nbtRecipe.setInteger(NBT_recipeWidth, recipe.recipeWidth);
        nbtRecipe.setInteger(NBT_recipeHeight, recipe.recipeHeight);
        NBTTagList NBTInput = new NBTTagList();
        for (ItemStack is : recipe.recipeItems)
        {
            if (is == null) NBTInput.appendTag(new NBTTagCompound());
            else NBTInput.appendTag(is.writeToNBT(new NBTTagCompound()));
        }
        nbtRecipe.setTag(NBT_input, NBTInput);
        nbtRecipe.setTag(NBT_newOutput, newOutput.writeToNBT(new NBTTagCompound()));
        nbtRecipe.setTag(NBT_oldOutput, recipe.getRecipeOutput().writeToNBT(new NBTTagCompound()));
        nbtRecipe.setBoolean(NBT_field_92101_f, ShapedRecipes_field_92101_f.getBoolean(recipe));

        return nbtRecipe;
    }

    @Override
    public ShapedRecipes[] getRecipesFromNBT(NBTTagCompound nbtRecipe)
    {
        int width = nbtRecipe.getInteger(NBT_recipeWidth);
        int height = nbtRecipe.getInteger(NBT_recipeHeight);
        NBTTagList list = nbtRecipe.getTagList(NBT_input, 10);

        ItemStack[] input = new ItemStack[list.tagCount()];
        for (int i = 0; i < list.tagCount(); i++) input[i] = ItemStack.loadItemStackFromNBT(list.getCompoundTagAt(i));

        ItemStack newOutput = ItemStack.loadItemStackFromNBT(nbtRecipe.getCompoundTag(NBT_newOutput));
        ItemStack oldOutput = ItemStack.loadItemStackFromNBT(nbtRecipe.getCompoundTag(NBT_oldOutput));

        DimBasedShapedRecipes newRecipes = new DimBasedShapedRecipes(true, width, height, input, newOutput);
        if (nbtRecipe.getBoolean(NBT_field_92101_f)) newRecipes.func_92100_c();

        DimBasedShapedRecipes oldRecipes = new DimBasedShapedRecipes(false, width, height, input, oldOutput);
        if (nbtRecipe.getBoolean(NBT_field_92101_f)) oldRecipes.func_92100_c();

        return new ShapedRecipes[] {newRecipes, oldRecipes};
    }

    public static class DimBasedShapedRecipes extends ShapedRecipes
    {
        boolean isCrayRecipe;

        public DimBasedShapedRecipes(boolean isCrayRecipe, int p_i1917_1_, int p_i1917_2_, ItemStack[] p_i1917_3_, ItemStack p_i1917_4_)
        {
            super(p_i1917_1_, p_i1917_2_, p_i1917_3_, p_i1917_4_);
            this.isCrayRecipe = isCrayRecipe;
        }

        @Override
        public boolean matches(InventoryCrafting inv, World world)
        {
            return inv != null && world != null && ((RecipeRegistry.doesCrayApplyTo(world) && isCrayRecipe) || (!RecipeRegistry.doesCrayApplyTo(world) && !isCrayRecipe)) && super.matches(inv, world);
        }
    }

    @Override
    public boolean equalsExceptOutput(ShapedRecipes recipe1, ShapedRecipes recipe2) throws IllegalAccessException
    {

        return recipe1.recipeHeight == recipe2.recipeHeight &&
                recipe1.recipeWidth == recipe2.recipeWidth &&
                Helper.inputEquals(recipe1.recipeItems, recipe2.recipeItems) &&
                ShapedRecipes_field_92101_f.getBoolean(recipe1) == ShapedRecipes_field_92101_f.getBoolean(recipe2);
    }
}
