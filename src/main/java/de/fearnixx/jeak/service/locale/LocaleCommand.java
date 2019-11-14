package de.fearnixx.jeak.service.locale;

import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.service.command.ICommandExecutionContext;
import de.fearnixx.jeak.service.command.spec.Commands;
import de.fearnixx.jeak.service.command.spec.ICommandSpec;
import de.fearnixx.jeak.service.teamspeak.IUserService;
import de.fearnixx.jeak.teamspeak.data.IUser;

import java.util.Locale;
import java.util.Optional;

import static de.fearnixx.jeak.service.command.spec.Commands.argumentSpec;
import static de.fearnixx.jeak.service.command.spec.Commands.paramSpec;

public class LocaleCommand {

    @Inject
    private ILocalizationService localeService;

    @Inject
    private IUserService userService;

    public ICommandSpec getCommandSpec() {
        return Commands.commandSpec("locale", "frw:locale")
                .parameters(
                        paramSpec().optional(paramSpec("locale", Locale.class)),
                        paramSpec().optional(paramSpec("user", IUser.class))
                )
                .executor(this::typedInvoke)
                .build();
    }

    public ICommandSpec getArgCommandSpec() {
        return Commands.commandSpec("locale-arg", "frw:locale-arg")
                .arguments(
                        argumentSpec().optional(argumentSpec("locale", "l", Locale.class)),
                        argumentSpec().optional(argumentSpec("user", "u", IUser.class))
                )
                .executor(this::typedInvoke)
                .build();
    }

    private void typedInvoke(ICommandExecutionContext ctx) {
        Optional<Locale> optLocale = ctx.getOne("locale", Locale.class);
        Optional<IUser> optUser = ctx.getOne("user", IUser.class);
        if (optLocale.isPresent()) {
            Locale selectedLocale = optLocale.get();
            IUser target = optUser.orElse(ctx.getSender());

            if ("XX".equals(selectedLocale.getLanguage())) {
                localeService.setLocaleForClient(target.getClientUniqueID(), null);
            } else {
                localeService.setLocaleForClient(target.getClientUniqueID(), selectedLocale);
            }
        } else {
            Locale currentLocale =
                    optUser.map(u -> localeService.getLocaleOfUser(u))
                            .orElseGet(() -> localeService.getLocaleOfClient(ctx.getSender()));
            ctx.getConnection().sendRequest(ctx.getSender().sendMessage(currentLocale.toLanguageTag()));
        }
    }
}
