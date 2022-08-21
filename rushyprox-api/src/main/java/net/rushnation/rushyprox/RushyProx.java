package net.rushnation.rushyprox;

import net.rushnation.rushyprox.event.IEventManager;
import net.rushnation.rushyprox.scheduler.IRushProxScheduler;
import org.apache.logging.log4j.Logger;

public interface RushyProx {

    IEventManager getEventManager();

    IRushProxScheduler getRushProxScheduler();

    Logger getLogger();

}
