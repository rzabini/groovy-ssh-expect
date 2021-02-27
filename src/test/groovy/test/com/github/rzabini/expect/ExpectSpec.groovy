package test.com.github.rzabini.expect

import com.github.rzabini.expect.Expect
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.hidetake.groovy.ssh.Ssh
import org.hidetake.groovy.ssh.core.Service
import spock.lang.Shared
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class ExpectSpec extends Specification {

    @Shared
    SshServer server

    @Shared
    Service ssh
    static String PROMPT = '$'

    def startServer() {
        server = SshServer.setUpDefaultServer().with {
            host = 'localhost'
            keyPairProvider = new SimpleGeneratorHostKeyProvider()
            it
        }
        server.with {
            passwordAuthenticator = Mock(PasswordAuthenticator) {
                (0.._) * authenticate('someuser', 'somepassword', _) >> true
            }
            start()
        }
    }

    def cleanupSpec() {
        new PollingConditions().eventually {
            assert server.activeSessions.empty
        }
        server.stop()
    }

    def setupSpec() {
        startServer()

        ssh = Ssh.newService()
        ssh.settings {
            knownHosts = allowAnyHosts
            extensions.add expect: { Closure expectCommands ->
                Expect.of(operations.connection.createShellChannel(), expectCommands)
            }
        }
        ssh.remotes {
            testServer {
                host = server.host
                port = server.port
                user = 'someuser'
                password = 'somepassword'
            }
        }
    }

    def "can expect for prompt"() {
        CommandExecutor commandExecutor = Mock(CommandExecutor)
        server.shellFactory = new StubShellFactory(PROMPT, commandExecutor)
        when:
            ssh.run {
                session(ssh.remotes.testServer) {
                    expect {
                        expectOrThrow 1, PROMPT
                    }
                }
            }
        then:
            notThrown(Exception)
    }


    def "can send a command and expect a result"() {
        CommandExecutor commandExecutor = Mock(CommandExecutor)
        server.shellFactory = new StubShellFactory(PROMPT, commandExecutor)
        when:
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

        then:
            commandExecutor.processCommand("hello server") >> 'please enter password:'
            commandExecutor.processCommand("Welcome1") >> 'password OK'
    }

    def "throws exception when expected result is not found"() {
        CommandExecutor commandExecutor = Mock(CommandExecutor)
        server.shellFactory = new StubShellFactory(PROMPT, commandExecutor)
        when:
            ssh.run {
                session(ssh.remotes.testServer) {
                    expect {
                        send 'hello server'
                        expectOrThrow 1, 'please enter password:'
                        send 'Welcome2'
                        expectOrThrow 1, 'password OK'
                    }
                }
            }

        then:
            commandExecutor.processCommand("hello server") >> 'please enter password:'
            commandExecutor.processCommand("Welcome1") >> 'password OK'
            thrown Expect.TimeoutException
    }
}
