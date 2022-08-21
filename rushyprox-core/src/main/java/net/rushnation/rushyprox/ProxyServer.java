package net.rushnation.rushyprox;

import com.nukkitx.protocol.bedrock.BedrockClient;
import com.nukkitx.protocol.bedrock.BedrockPacketCodec;
import com.nukkitx.protocol.bedrock.BedrockServer;
import com.nukkitx.protocol.bedrock.v431.Bedrock_v431;
import com.nukkitx.protocol.bedrock.v448.Bedrock_v448;
import lombok.Getter;
import net.rushnation.rushyprox.command.CommandManager;
import net.rushnation.rushyprox.command.ConsoleCommandSender;
import net.rushnation.rushyprox.config.TestConfig;
import net.rushnation.rushyprox.config.TestObject;
import net.rushnation.rushyprox.console.Console;
import net.rushnation.rushyprox.event.EventManager;
import net.rushnation.rushyprox.event.list.ProxyReadyEvent;
import net.rushnation.rushyprox.network.ProxyListener;
import net.rushnation.rushyprox.player.ProxyPlayer;
import net.rushnation.rushyprox.plugin.PluginLoader;
import net.rushnation.rushyprox.plugin.PluginManager;
import net.rushnation.rushyprox.scheduler.RushProxScheduler;
import net.rushnation.rushyprox.scheduler.SchedulerHandler;
import net.rushnation.rushyprox.util.RushyProxyConfig;
import net.rushnation.rushyprox.util.ServerConfig;
import net.rushnation.rushyprox.util.ServerObject;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicBoolean;

public class ProxyServer implements RushyProx {

    @Getter
    private HashMap<String, ProxyPlayer> players = new HashMap<>();

    // Instance
    private static ProxyServer proxyServer;

    // Getter
    public static String MINECRAFT_VERSION;
    public static int PROTOCOL_VERSION;
    public static int RUNNING_PORT;
    public static final BedrockPacketCodec BEDROCK_PACKET_CODEC = Bedrock_v448.V448_CODEC;

    // Event manager
    @Getter
    private final EventManager eventManager;

    // Command manager
    @Getter
    private final CommandManager commandManager;
    @Getter
    private final ConsoleCommandSender consoleCommandSender;

    // Plugin
    @Getter
    private PluginManager pluginManager;
    @Getter
    private PluginLoader pluginLoader;

    // Scheduler
    @Getter
    private RushProxScheduler rushProxScheduler;
    @Getter
    private SchedulerHandler schedulerHandler;

    // Config
    @Getter
    private RushyProxyConfig rushyProxyConfig;
    @Getter
    private ServerConfig serverConfig;

    @Getter
    public final Logger logger = LogManager.getLogger("RushyProx");

    // Private fields
    private final AtomicBoolean shouldRun = new AtomicBoolean(true);
    private final Set<BedrockClient> connectedClients = Collections.newSetFromMap(new LinkedHashMap<>());
    private BedrockServer bedrockServer;
    private final InetSocketAddress proxyAddress;
    private Console console;

    /**
     * Here stuff will be initialized
     */
    public ProxyServer() {

        proxyServer = this;

        // Set versions
        if (BEDROCK_PACKET_CODEC != null) {
            MINECRAFT_VERSION = BEDROCK_PACKET_CODEC.getMinecraftVersion();
            PROTOCOL_VERSION = BEDROCK_PACKET_CODEC.getProtocolVersion();
        } else {
            logger.error("Could not initialize MINECRAFT_VERSION & PROTOCOL_VERSION due codec is null.");
        }

        // Set address
        this.proxyAddress = new InetSocketAddress(19132);
        RUNNING_PORT = this.proxyAddress.getPort();

        // Init other
        this.eventManager = new EventManager();
        this.pluginLoader = new PluginLoader(this);
        this.pluginManager = new PluginManager(this, this.pluginLoader, "plugins/");
        this.commandManager = new CommandManager(this);
        this.consoleCommandSender = new ConsoleCommandSender(this);

        logger.info(this.commandManager.getCommands().size());
    }

    /**
     * This will boot the proxy
     */
    public void bootProxy() {
        logger.info("----RushyProx----");
        logger.info("Booting now with Minecraft-Version: {} & Protocol-Version: {}", MINECRAFT_VERSION, PROTOCOL_VERSION);

        this.bedrockServer = new BedrockServer(this.proxyAddress);
        this.bedrockServer.setHandler(new ProxyListener(this));
        this.bedrockServer.bind().join();

        logger.info("Proxy is now listening on port {}", this.proxyAddress.getPort());

        // Initialize other stuff
        this.console = new Console(this);
        this.schedulerHandler = new SchedulerHandler(this);
        this.rushProxScheduler = new RushProxScheduler(schedulerHandler);
        this.schedulerHandler.setRushProxScheduler(this.rushProxScheduler);

        // Start scheduler
        this.schedulerHandler.prepareTickExecutor();
        this.schedulerHandler.prepareTickFuture();

        // Load configuration files
        this.loadConfigurationFiles();

        // Load plugins
        this.pluginManager.loadAllPlugins(true);

        this.eventManager.call(new ProxyReadyEvent());

        TestConfig testConfig = new TestConfig();
        testConfig.load();

        this.keepAlive();
    }

    private void loadConfigurationFiles() {
        this.rushyProxyConfig = new RushyProxyConfig();
        this.rushyProxyConfig.load();

        this.serverConfig = new ServerConfig();
        this.serverConfig.load();

        this.getLogger().info(this.rushyProxyConfig.motd);
    }

    /**
     * Method to keep thread alive while proxy is running
     */
    private void keepAlive() {
        while (this.shouldRun.get()) {
            synchronized (this) {
                try {
                    this.wait();
                } catch (Exception ignored) {
                }
            }
        }

        // Shutdown proxy server
        this.connectedClients.forEach(BedrockClient::close);
        this.bedrockServer.close();
        this.rushProxScheduler.close();

        System.exit(0);
    }

    /**
     * This will shutdown the proxy server
     */
    public void shutdown() {
        this.shouldRun.set(false);

        synchronized (this) {
            this.notify();
        }
    }

    public BedrockClient buildNewClient() {
        InetSocketAddress inetSocketAddress = new InetSocketAddress("0.0.0.0", ThreadLocalRandom.current().nextInt(20000, 35000));
        BedrockClient bedrockClient = new BedrockClient(inetSocketAddress);
        bedrockClient.bind().join();
        this.connectedClients.add(bedrockClient);
        return bedrockClient;
    }

    public static ProxyServer getProxyServer() {
        return proxyServer;
    }
}
