package org.samo_lego.simpleauth.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.samo_lego.simpleauth.SimpleAuth;
import org.samo_lego.simpleauth.utils.AuthHelper;

import static com.mojang.brigadier.arguments.StringArgumentType.getString;
import static com.mojang.brigadier.arguments.StringArgumentType.word;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ChangepwCommand {
    private static Text enterNewPassword = new LiteralText(SimpleAuth.config.lang.enterNewPassword);
    private static Text enterPassword = new LiteralText(SimpleAuth.config.lang.enterPassword);
    private static Text wrongPassword = new LiteralText(SimpleAuth.config.lang.wrongPassword);
    private static Text passwordUpdated = new LiteralText(SimpleAuth.config.lang.passwordUpdated);
    private static Text cannotChangePassword = new LiteralText(SimpleAuth.config.lang.cannotChangePassword);

    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        // Registering the "/changepw" command
        dispatcher.register(literal("changepw")
            .executes(ctx -> {
                    ctx.getSource().getPlayer().sendMessage(enterPassword, false);
                    return 1;
            })
            .then(argument("oldPassword", word())
                .executes(ctx -> {
                    ctx.getSource().getPlayer().sendMessage(enterNewPassword, false);
                    return 1;
                })
                .then(argument("newPassword", word())
                    .executes( ctx -> changepw(
                            ctx.getSource(),
                            getString(ctx, "oldPassword"),
                            getString(ctx, "newPassword")
                        )
                    )
                )
            )
        );
    }

    // Method called for checking the password and then changing it
    private static int changepw(ServerCommandSource source, String oldPass, String newPass) throws CommandSyntaxException {
        // Getting the player who send the command
        ServerPlayerEntity player = source.getPlayer();

        if (SimpleAuth.config.main.enableGlobalPassword) {
            player.sendMessage(cannotChangePassword, false);
            return 0;
        }
        else if (AuthHelper.checkPass(player.getUuidAsString(), oldPass.toCharArray())) {
            if(newPass.length() < SimpleAuth.config.main.minPasswordChars) {
                player.sendMessage(new LiteralText(
                        String.format(SimpleAuth.config.lang.minPasswordChars, SimpleAuth.config.main.minPasswordChars)
                ), false);
                return 0;
            }
            else if(newPass.length() > SimpleAuth.config.main.maxPasswordChars && SimpleAuth.config.main.maxPasswordChars != -1) {
                player.sendMessage(new LiteralText(
                        String.format(SimpleAuth.config.lang.maxPasswordChars, SimpleAuth.config.main.maxPasswordChars)
                ), false);
                return 0;
            }
            SimpleAuth.db.update(
                    player.getUuidAsString(),
                    null,
                    AuthHelper.hashPass(newPass.toCharArray())
            );
            player.sendMessage(passwordUpdated, false);
            return 1;
        }
        player.sendMessage(wrongPassword, false);
        return 0;
    }
}
