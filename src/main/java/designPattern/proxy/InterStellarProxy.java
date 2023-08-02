package designPattern.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterStellarProxy implements Movie{

    private final InterStellar interStellar;
    InterStellarProxy(InterStellar in){
        interStellar=in;
    }

    private static final Logger log = LoggerFactory.getLogger(InterStellarProxy.class);
    @Override
    public void play() {
        log.info("proxy before");
        interStellar.play();
        log.info("proxy after");
    }
}
