package com.dygo454.myRomExporter;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.StringReader;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import net.minecraft.block.Blocks;
import net.minecraft.block.entity.LootableContainerBlockEntity;
import net.minecraft.client.gui.screen.ingame.BookScreen;
import net.minecraft.command.argument.BlockPosArgumentType;
import net.minecraft.item.ItemStack;
import net.minecraft.item.WritableBookItem;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.util.math.BlockPos;

public class ExportRomCommand {
    public static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("exp-rom")
                .then(CommandManager.argument("ChestPos", BlockPosArgumentType.blockPos())
                .then(CommandManager.argument("FirstBitPos", BlockPosArgumentType.blockPos())
                        .executes(ExportRomCommand::run))));
        dispatcher.register(CommandManager.literal("exp-rom")
                .then(CommandManager.argument("ChestPos", BlockPosArgumentType.blockPos())
                .then(CommandManager.argument("FirstBitPos", BlockPosArgumentType.blockPos())
                .then(CommandManager.argument("Direction", BlockPosArgumentType.blockPos())
                        .executes(ExportRomCommand::run)))));
        dispatcher.register(CommandManager.literal("exp-rom")
                .then(CommandManager.argument("ChestPos", BlockPosArgumentType.blockPos())
                .then(CommandManager.argument("FirstBitPos", BlockPosArgumentType.blockPos())
                .then(CommandManager.argument("ClearPrevious", BoolArgumentType.bool())
                        .executes(ExportRomCommand::run)))));
        dispatcher.register(CommandManager.literal("exp-rom")
                .then(CommandManager.argument("ChestPos", BlockPosArgumentType.blockPos())
                .then(CommandManager.argument("FirstBitPos", BlockPosArgumentType.blockPos())
                .then(CommandManager.argument("Direction", BlockPosArgumentType.blockPos())
                .then(CommandManager.argument("ClearPrevious", BoolArgumentType.bool())
                        .executes(ExportRomCommand::run))))));
    }

    private static int run(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        SimpleCommandExceptionType noQuill = new SimpleCommandExceptionType(new LiteralMessage("Error getting ROM from inventory ensure that the storage block selected contains a book and quill"));
        SimpleCommandExceptionType noStorage = new SimpleCommandExceptionType(new LiteralMessage("Error getting inventory ensure that a storage block is selected contains a book and quill"));

        BlockPos chestPos = BlockPosArgumentType.getLoadedBlockPos(context, "ChestPos");
        BlockPos currBitPos = BlockPosArgumentType.getLoadedBlockPos(context, "FirstBitPos");
        BlockPos dir;
        int xDir = 1;
        int yDir = 1;
        int zDir = 1;
        try {
            dir = BlockPosArgumentType.getLoadedBlockPos(context, "Direction");
            dir = BlockPosArgumentType.getLoadedBlockPos(context, "Direction");
            if (dir.getX() != 0) {
                xDir = dir.getX() / Math.abs(dir.getX());
            }
            if (dir.getY() != 0) {
                yDir = dir.getY() / Math.abs(dir.getY());
            }
            if (dir.getZ() != 0) {
                zDir = dir.getZ() / Math.abs(dir.getZ());
            }
        } catch (IllegalArgumentException ignored) {
        }
        boolean clearPrevious = true;
        try {
            clearPrevious = BoolArgumentType.getBool(context,"ClearPrevious");
        } catch (IllegalArgumentException ignored) {
        }
        BookScreen.WritableBookContents ROM = null;
        for (int i = 0; true; i++) {
            try {
                ItemStack stack = ((LootableContainerBlockEntity) context.getSource().getWorld().getBlockEntity(chestPos)).getStack(i);
                if (stack.getItem() instanceof WritableBookItem) {
                    ROM = new BookScreen.WritableBookContents(stack);
                }
                break;
            } catch (Exception e) {
                try {
                    if (i > ((LootableContainerBlockEntity) context.getSource().getWorld().getBlockEntity(chestPos)).size()) {
                        break;
                    }
                } catch (Exception e2) {
                    throw new CommandSyntaxException(noStorage,new LiteralMessage(noStorage.toString()));
                }
            }
        }
        if (ROM == null) {
            throw new CommandSyntaxException(noQuill,new LiteralMessage(noQuill.toString()));
        }
        int currHieght = 0;
        BlockPos currBitPosBU = currBitPos;
        if (clearPrevious) {
            for (int i = 0; i < 16; i++) {
                for (int n = 0; n < 16; n++) {
                    for (int a = 0; a < 8; a++) {
                        context.getSource().getWorld().setBlockState(currBitPos, Blocks.AIR.getDefaultState());
                        currBitPos = currBitPos.add(3 * xDir, 0, 0);
                    }
                    currBitPos = currBitPos.add(-24 * xDir, 0, 5 * zDir);
                    currHieght++;
                    if (currHieght >= 16) {
                        currHieght %= 16;
                        currBitPos = currBitPos.add(0, 2 * yDir, -80 * zDir);
                    }
                }
            }
            currBitPos = currBitPosBU;
        }
        for (int i = 0; i < ROM.getPageCount(); i++) {
            for (String line : ROM.getPage(i).getString().split("\n")) {
                for (int b = line.length()-1; b >= 0; b--) {
                    char bit = line.charAt(b);
                    if (bit == '1') {
                        context.getSource().getWorld().setBlockState(currBitPos, Blocks.REDSTONE_TORCH.getDefaultState());
                    }
                    else if (bit == '0') {
                        context.getSource().getWorld().setBlockState(currBitPos, Blocks.AIR.getDefaultState());
                    }
                    currBitPos = currBitPos.add(3*xDir,0,0);
                }
                currBitPos = currBitPos.add(-24*xDir,0,5*zDir);
                currHieght++;
                if (currHieght >= 16) {
                    currHieght %= 16;
                    currBitPos = currBitPos.add(0,2*yDir,-80*zDir);
                }
            }
        }
        return 0;
    }
}
