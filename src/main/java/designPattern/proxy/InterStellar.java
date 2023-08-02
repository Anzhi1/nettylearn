package designPattern.proxy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class InterStellar implements Movie{
    private static final Logger log = LoggerFactory.getLogger(InterStellar.class);
    @Override
    public void play() {
        log.info("Inter Stellar is a good movie");
    }
}
