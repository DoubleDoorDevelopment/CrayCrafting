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

package net.doubledoordev.craycrafting.util;

import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Useful methods, all static.
 *
 * @author Dries007
 */
public class Helper
{
    private Helper() {}

    /**
     * Use for ItemStack(s) and OreDictionary only.
     */
    public static boolean inputEquals(Object[] input1, Object[] input2)
    {
        if (input1.length != input2.length) return false;
        for (int i = 0; i < input1.length; i++)
        {
            if (input1[i] == null && input2[i] == null) continue;
            if (input1[i] == null || input2[i] == null) return false;
            if (input1[i] instanceof ItemStack && input2[i] instanceof ItemStack)
            {
                if (itemStacksEqual((ItemStack) input1[i], (ItemStack) input2[i])) continue;
            }
            else if (input1[i] instanceof ArrayList && input2[i] instanceof ArrayList)
            {
                if (inputEquals((List) input1[i], (List) input2[i])) continue;
            }

            return false;
        }
        return true;
    }

    /**
     * Use for ItemStack(s) and OreDictionary only.
     */
    public static boolean inputEquals(ItemStack[] input1, ItemStack[] input2)
    {
        if (input1.length != input2.length) return false;
        for (int i = 0; i < input1.length; i++)
        {
            if (itemStacksEqual(input1[i], input2[i])) continue;
            return false;
        }
        return true;
    }

    /**
     * Use for ItemStack(s) and OreDictionary only.
     */
    public static boolean inputEquals(List input1, List input2)
    {
        if (input1.size() != input2.size()) return false;
        for (int i = 0; i < input1.size(); i++)
        {
            Object o1 = input1.get(i);
            Object o2 = input2.get(i);
            if (o1 instanceof ItemStack && o2 instanceof ItemStack)
            {
                if (itemStacksEqual((ItemStack) o1, (ItemStack) o2)) continue;
            }
            else if (o1 instanceof ArrayList && o2 instanceof ArrayList)
            {
                if (inputEquals((List) o1, (List) o2)) continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Use for ItemStack(s) and OreDictionary only.
     */
    public static boolean itemStacksEqual(ItemStack itemStack1, ItemStack itemStack2)
    {
        return ItemStack.areItemStacksEqual(itemStack1, itemStack2) && ItemStack.areItemStackTagsEqual(itemStack1, itemStack2);
    }
}
