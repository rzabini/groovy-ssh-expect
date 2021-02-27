package test.com.github.rzabini.expect;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StubShell extends CommandExecutionHelper {
    private final CommandExecutor commandExecutor;
    private final String prompt;

    public StubShell(String prompt, CommandExecutor commandExecutor) {
        this.prompt = prompt;
        this.commandExecutor = commandExecutor;
    }

    @Override
    protected boolean handleCommandLine(String command) throws Exception {
        getOutputStream().write((commandExecutor.processCommand(command) + "\n" + prompt).getBytes(StandardCharsets.UTF_8));
        getOutputStream().flush();

        return !"exit".equals(command);
    }

    @Override
    public void run() {
        try {
            getOutputStream().write(prompt.getBytes());
            getOutputStream().flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        super.run();
    }

}