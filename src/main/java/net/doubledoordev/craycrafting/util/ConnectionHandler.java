///*
// * Copyright (c) 2014, DoubleDoorDevelopment
// * All rights reserved.
// *
// * Redistribution and use in source and binary forms, with or without
// * modification, are permitted provided that the following conditions are met:
// *
// *  Redistributions of source code must retain the above copyright notice, this
// *   list of conditions and the following disclaimer.
// *
// *  Redistributions in binary form must reproduce the above copyright notice,
// *   this list of conditions and the following disclaimer in the documentation
// *   and/or other materials provided with the distribution.
// *
// *  Neither the name of the project nor the names of its
// *   contributors may be used to endorse or promote products derived from
// *   this software without specific prior written permission.
// *
// * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
// * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
// * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
// * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
// * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
// * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
// * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
// * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
// * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
// */
//
//package net.doubledoordev.craycrafting.util;
//
//import ccm.craycrafting.recipes.RecipeRegistry;
//import cpw.mods.fml.common.network.IConnectionHandler;
//import cpw.mods.fml.common.network.Player;
//import net.minecraft.network.INetworkManager;
//import net.minecraft.network.NetLoginHandler;
//import net.minecraft.network.packet.NetHandler;
//import net.minecraft.network.packet.Packet1Login;
//import net.minecraft.server.MinecraftServer;
//
///**
// * Used to send packet only. Could also use an IPlayerTracker but mhe.
// *
// * @author Dries007
// */
//public class ConnectionHandler implements IConnectionHandler
//{
//    @Override
//    public void playerLoggedIn(Player player, NetHandler netHandler, INetworkManager manager)
//    {
//        if (MinecraftServer.getServer().isDedicatedServer()) RecipeRegistry.sendPacketTo(player);
//    }
//
//    @Override
//    public String connectionReceived(NetLoginHandler netHandler, INetworkManager manager)
//    {
//        return null;
//    }
//
//    @Override
//    public void connectionOpened(NetHandler netClientHandler, String server, int port, INetworkManager manager)
//    {
//
//    }
//
//    @Override
//    public void connectionOpened(NetHandler netClientHandler, MinecraftServer server, INetworkManager manager)
//    {
//
//    }
//
//    @Override
//    public void connectionClosed(INetworkManager manager)
//    {
//
//    }
//
//    @Override
//    public void clientLoggedIn(NetHandler clientHandler, INetworkManager manager, Packet1Login login)
//    {
//
//    }
//}
