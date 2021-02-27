# groovy-ssh-expect

Experimental [extension](https://gradle-ssh-plugin.github.io/docs/#_dsl_extension_system) for [groovy-ssh](https://github.com/int128/groovy-ssh)
to add an [expect](https://en.wikipedia.org/wiki/Expect)-like interface.

This is an integration of [Expect for Java](https://github.com/ronniedong/Expect-for-Java), and adds two commands to
interact with a SSH server:

- send (_command_): sends a _command_ string to the server standard input
- expectOrThrow( timeout, expected): waits for _timeout_ seconds for the _expected_ text to appear on server standard
  output

### Usage example

```groovy
ssh.settings {
    extensions.add expect: { Closure expectCommands ->
        Expect.of(operations.connection.createShellChannel(), expectCommands)
    }
}


ssh.run {
    session(ssh.remotes.testServer) {
        expect {
            send 'hello server'
            expectOrThrow 1, 'please enter password:'
            send 'Welcome1'
            expectOrThrow 1, 'password OK'
        }
    }
}
```
