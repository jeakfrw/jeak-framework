package de.fearnixx.jeak.service.util;

import de.fearnixx.jeak.reflect.IInjectionService;
import de.fearnixx.jeak.service.command.ICommandService;

public abstract class UtilCommands {

    public static void registerCommands(ICommandService commandService,
                                        IInjectionService injectionService) {
        final WhereAmI whereAmI = new WhereAmI();
        final WhoAmICommand whoAmI = new WhoAmICommand();

        injectionService.injectInto(whereAmI);
        injectionService.injectInto(whoAmI);
        commandService.registerCommand("whereami", whereAmI);
        commandService.registerCommand("whoami", whoAmI);
    }
}
