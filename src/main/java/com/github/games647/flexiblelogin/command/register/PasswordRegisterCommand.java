/*
 * This file is part of FlexibleLogin
 *
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2018 contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package com.github.games647.flexiblelogin.command.register;

import com.github.games647.flexiblelogin.FlexibleLogin;
import com.github.games647.flexiblelogin.ProtectionManager;
import com.github.games647.flexiblelogin.command.AbstractCommand;
import com.github.games647.flexiblelogin.config.Settings;
import com.github.games647.flexiblelogin.tasks.RegisterTask;
import com.google.common.collect.Lists;
import com.google.inject.Inject;

import java.util.Collection;
import java.util.List;

import org.slf4j.Logger;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.scheduler.Task;

import static org.spongepowered.api.command.args.GenericArguments.repeated;
import static org.spongepowered.api.command.args.GenericArguments.string;
import static org.spongepowered.api.text.Text.of;

public class PasswordRegisterCommand extends AbstractCommand implements RegisterCommand {

    @Inject
    private ProtectionManager protectionManager;

    @Inject
    PasswordRegisterCommand(FlexibleLogin plugin, Logger logger, Settings settings) {
        super(plugin, logger, settings);
    }

    @Override
    public CommandResult execute(CommandSource src, CommandContext args) throws CommandException {
        if (!(src instanceof Player)) {
            throw new CommandException(settings.getText().getPlayersOnly());
        }

        Collection<String> passwords = args.getAll("password");
        List<String> indexPasswords = Lists.newArrayList(passwords);
        String password = indexPasswords.get(0);
        if (!password.equals(indexPasswords.get(1))) {
            //Check if the first two passwords are equal to prevent typos
            throw new CommandException(settings.getText().getUnequalPasswords());
        }

        if (password.length() < settings.getGeneral().getMinPasswordLength()) {
            throw new CommandException(settings.getText().getTooShortPassword());
        }

        Task.builder()
                //we are executing a SQL Query which is blocking
                .async()
                .execute(new RegisterTask(plugin, protectionManager, (Player) src, password))
                .name("Register Query")
                .submit(plugin);
        return CommandResult.success();
    }

    @Override
    public CommandSpec buildSpec(Settings settings) {
        return buildPlayerCommand(settings, "register")
                .executor(this)
                .arguments(
                        repeated(
                                string(of("password")),
                                2)
                )
                .build();
    }
}
