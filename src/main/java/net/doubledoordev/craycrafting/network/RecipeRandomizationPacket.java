package net.doubledoordev.craycrafting.network;

import java.util.function.Supplier;

import net.minecraft.network.PacketBuffer;
import net.minecraftforge.fml.network.NetworkEvent;

import net.doubledoordev.craycrafting.recipe.CrayRecipeManager;

public class RecipeRandomizationPacket
{
    private final long seed;
    private final long interval;

    public RecipeRandomizationPacket(long seed, long interval)
    {
        this.seed = seed;
        this.interval = interval;
    }

    public RecipeRandomizationPacket(PacketBuffer buffer)
    {
        seed = buffer.readLong();
        interval = buffer.readLong();
    }

    public void encode(PacketBuffer buffer)
    {
        buffer.writeLong(seed);
        buffer.writeLong(interval);
    }

    public void handle(Supplier<NetworkEvent.Context> context)
    {
        context.get().setPacketHandled(true);
        context.get().enqueueWork(() -> {
            CrayRecipeManager.INSTANCE.setSeed(seed, interval);
            CrayRecipeManager.INSTANCE.randomizeAllRecipes();
        });
    }
}
