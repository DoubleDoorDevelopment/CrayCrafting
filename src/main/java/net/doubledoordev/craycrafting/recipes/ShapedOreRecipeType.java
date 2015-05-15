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

import com.google.common.collect.HashBiMap;
import net.doubledoordev.craycrafting.CrayCrafting;
import net.doubledoordev.craycrafting.util.Helper;
import net.minecraft.inventory.InventoryCrafting;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.world.World;
import net.minecraftforge.oredict.OreDictionary;
import net.minecraftforge.oredict.ShapedOreRecipe;

import java.lang.reflect.Field;
import java.util.*;

import static net.doubledoordev.craycrafting.util.Constants.*;

/**
 * @author Dries007
 */
public class ShapedOreRecipeType extends BaseType<ShapedOreRecipe>
{
    public static final Field ShapedOreRecipe_width;
    public static final Field ShapedOreRecipe_height;
    public static final Field ShapedOreRecipe_mirror;

    static
    {
        try
        {
            ShapedOreRecipe_mirror = ShapedOreRecipe.class.getDeclaredField("mirrored");
            ShapedOreRecipe_mirror.setAccessible(true);

            ShapedOreRecipe_width = ShapedOreRecipe.class.getDeclaredField("width");
            ShapedOreRecipe_width.setAccessible(true);

            ShapedOreRecipe_height = ShapedOreRecipe.class.getDeclaredField("height");
            ShapedOreRecipe_height.setAccessible(true);
        }
        catch (NoSuchFieldException e)
        {
            CrayCrafting.instance.logger.warn("This is going to be a problem later, so I'm stopping it here.");
            throw new RuntimeException(e);
        }
    }

    public ShapedOreRecipeType()
    {
        super(ShapedOreRecipe.class);
    }

    @Override
    public NBTTagCompound getNBTFromRecipe(ShapedOreRecipe recipe, ItemStack newOutput) throws IllegalAccessException
    {
        NBTTagCompound nbtRecipe = new NBTTagCompound();
        NBTTagList NBTInput = new NBTTagList();

        int width = ShapedOreRecipe_width.getInt(recipe);
        int height = ShapedOreRecipe_height.getInt(recipe);

        /**
         * Build a map to convert the object array into recipe format.
         */
        HashBiMap<Character, Object> map = HashBiMap.create();
        HashMap<ArrayList, Object> arrayListMap = new HashMap<ArrayList, Object>(); // Lookup map for oredict entries.
        for (Object o : recipe.getInput())
        {
            if (o == null) continue;
            if (map.containsValue(o)) continue;
            if (o instanceof ArrayList)
            {
                for (String name : OreDictionary.getOreNames())
                {
                    if (OreDictionary.getOres(name).equals(o))
                    {
                        if (map.containsValue(name)) break;
                        map.put(DUMMY_CHARS.charAt(map.size()), name);
                        arrayListMap.put((ArrayList) o, name);
                        break;
                    }
                }
            }
            else
            {
                map.put(DUMMY_CHARS.charAt(map.size()), o);
            }
        }

        /**
         * Make the recipe strings
         * aka: "aa ", "aa ", "aa "
         */
        char[][] chars = new char[height][width];
        for (int h = 0; h < height; h++)
        {
            for (int w = 0; w < width; w++)
            {
                int i = h * width + w;

                Object input = recipe.getInput()[i];

                if (input == null)
                {
                    chars[h][w] = ' ';
                }
                else if (input instanceof ArrayList)
                {
                    Object o = arrayListMap.get(input);
                    if (o != null)
                    {
                        chars[h][w] = map.inverse().get(o);
                    }
                    else
                    {
                        chars[h][w] = ' ';
                    }
                }
                else
                {
                    chars[h][w] = map.inverse().get(input);
                }
            }
            String line = new String(chars[h]);
            NBTInput.appendTag(new NBTTagString(line));
        }
        nbtRecipe.setTag(NBT_input, NBTInput);

        /**
         * Add the char to itemstack thing
         * aka: 'a' = "plank"
         */
        NBTTagCompound nbtMap = new NBTTagCompound();
        for (Map.Entry<Character, Object> entry : map.entrySet())
        {
            if (entry.getValue() instanceof String) nbtMap.setString(entry.getKey().toString(), entry.getValue().toString());
            else if (entry.getValue() instanceof ItemStack) nbtMap.setTag(entry.getKey().toString(), ((ItemStack) entry.getValue()).writeToNBT(new NBTTagCompound()));
            else
            {
                CrayCrafting.instance.logger.warn("NBT RECIPE ERROR: " + entry.getValue() + " IS NOT STRING OR ITEMSTACK ???");
            }
        }
        nbtRecipe.setTag(NBT_map, nbtMap);
        nbtRecipe.setTag(NBT_newOutput, newOutput.writeToNBT(new NBTTagCompound()));
        nbtRecipe.setTag(NBT_oldOutput, recipe.getRecipeOutput().writeToNBT(new NBTTagCompound()));
        nbtRecipe.setBoolean(NBT_mirror, ShapedOreRecipe_mirror.getBoolean(recipe));

        return nbtRecipe;
    }

    @Override
    public ShapedOreRecipe[] getRecipesFromNBT(NBTTagCompound nbtRecipe)
    {
        ArrayList<Object> input = new ArrayList<Object>(); // Becomes entire recipe input
        ItemStack newOutput = ItemStack.loadItemStackFromNBT(nbtRecipe.getCompoundTag(NBT_newOutput));
        ItemStack oldOutput = ItemStack.loadItemStackFromNBT(nbtRecipe.getCompoundTag(NBT_oldOutput));

        NBTTagList inputs = nbtRecipe.getTagList(NBT_input, 8);
        for (int i = 0; i < inputs.tagCount(); i++) input.add(inputs.getStringTagAt(i));
        NBTTagCompound map = nbtRecipe.getCompoundTag(NBT_map);
        //noinspection unchecked
        for (String name : (Set<String>) map.func_150296_c())
        {
            input.add(name.charAt(0));
            NBTBase entry = map.getTag(name);
            if (entry instanceof NBTTagString) input.add(((NBTTagString) entry).func_150285_a_());
            else input.add(ItemStack.loadItemStackFromNBT((NBTTagCompound) entry));
        }

        return new ShapedOreRecipe[] { new DimBasedShapedOreRecipe(true, newOutput, input.toArray()).setMirrored(nbtRecipe.getBoolean(NBT_mirror)), new DimBasedShapedOreRecipe(false, oldOutput, input.toArray()).setMirrored(nbtRecipe.getBoolean(NBT_mirror))};
    }

    public static class DimBasedShapedOreRecipe extends ShapedOreRecipe
    {
        boolean isCrayRecipe;
        public DimBasedShapedOreRecipe(boolean isCrayRecipe, ItemStack newOutput, Object[] objects)
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
    public boolean equalsExceptOutput(ShapedOreRecipe recipe1, ShapedOreRecipe recipe2) throws IllegalAccessException
    {
        return Helper.inputEquals(recipe1.getInput(), recipe2.getInput()) && (ShapedOreRecipe_mirror.getBoolean(recipe1) == ShapedOreRecipe_mirror.getBoolean(recipe2));
    }
}
