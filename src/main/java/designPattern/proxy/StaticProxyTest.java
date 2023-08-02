package designPattern.proxy;

public class StaticProxyTest {
    public static void main(String[] args){
        InterStellar interStellar = new InterStellar();
        InterStellarProxy interStellarProxy= new InterStellarProxy(interStellar);

        interStellar.play();

        interStellarProxy.play();
    }
}
