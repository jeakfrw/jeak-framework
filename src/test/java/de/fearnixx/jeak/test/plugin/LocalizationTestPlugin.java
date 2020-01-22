package de.fearnixx.jeak.test.plugin;

import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.JeakBotPlugin;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.reflect.LocaleUnit;
import de.fearnixx.jeak.service.command.CommandException;
import de.fearnixx.jeak.service.command.ICommandContext;
import de.fearnixx.jeak.service.command.ICommandReceiver;
import de.fearnixx.jeak.service.command.ICommandService;
import de.fearnixx.jeak.service.locale.ILocalizationUnit;
import de.fearnixx.jeak.teamspeak.TargetType;
import de.fearnixx.jeak.teamspeak.cache.IDataCache;
import de.fearnixx.jeak.teamspeak.data.IClient;
import de.fearnixx.jeak.test.AbstractTestPlugin;

@JeakBotPlugin(id = "localetest")
public class LocalizationTestPlugin extends AbstractTestPlugin implements ICommandReceiver {

    @Inject
    private ICommandService commandService;

    @Inject
    private IDataCache cache;

    @Inject
    @LocaleUnit("testlocale")
    private ILocalizationUnit testUnit;

    public LocalizationTestPlugin() {
        addTest("unit_injected");
    }

    @Listener
    public void onPreInitialize(IBotStateEvent.IPreInitializeEvent event) {
        commandService.registerCommand("locale-test", this);

        if (testUnit != null && testUnit.getUnitId().equals("testlocale")) {
            success("unit_injected");
            testUnit.loadDefaultsFromResource(this.getClass().getClassLoader(), "localization/testUnit.json");
        }
    }

    @Override
    public void receive(ICommandContext ctx) throws CommandException {
        if (ctx.getTargetType() == TargetType.CLIENT) {
            String invokerid = ctx.getRawEvent().getProperty("invokerid").get();
            IClient client = cache.getClientMap().get(Integer.parseInt(invokerid));

            String enTPL = testUnit.getContext("en").uncheckedGetMessage("test.message");
            String deTPL = testUnit.getContext("de").uncheckedGetMessage("test.message");
            String chosenTPL = testUnit.getContext(client).uncheckedGetMessage("test.message");

            String enMsg = String.format("Testing localization. Next should be in English: %s", enTPL);
            String deMsg = String.format("Testing localization. Next should be in German: %s", deTPL);
            String chosenMsg = String.format("Testing localization. Next should be in your selected: %s", chosenTPL);
            ctx.getRawEvent().getConnection().sendRequest(client.sendMessage(enMsg));
            ctx.getRawEvent().getConnection().sendRequest(client.sendMessage(deMsg));
            ctx.getRawEvent().getConnection().sendRequest(client.sendMessage(chosenMsg));
        }
    }
}
