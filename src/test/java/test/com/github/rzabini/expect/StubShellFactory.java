package test.com.github.rzabini.expect;

import org.apache.sshd.common.Factory;
import org.apache.sshd.server.command.Command;

public class StubShellFactory implements Factory<Command> {

    private final CommandExecutor commandExecutor;
    private final String prompt;

    public StubShellFactory(String prompt, CommandExecutor commandExecutor) {
        this.prompt = prompt;
        this.commandExecutor = commandExecutor;
    }

    @Override
    public Command create() {
        return new StubShell(prompt, commandExecutor);
    }
}