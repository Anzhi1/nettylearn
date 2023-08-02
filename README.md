# nettylearn
some practice while learning netty

--BIO

BIO的主要问题在于每当有一个新的客户端请求接入时，服务端必须创建一个新的线程处理新接入的客户端链路，一个线程只能处理一个客户端连接。
在高性能服务器应用领域，往往需要面向成千上万个客户端的并发连接，这种模型无法满足高性能，高并发接入的场景。 ————BioServer

为了改善一线程一连接模型，后来又演进出了一种通过线程池或者消息队列实现1个或多个线程处理N个客户端的模型，由于它的底层通信机制依然使用
同步阻塞I/O，所以被称为 ”伪异步“。 ————BioServerV2
JDK的线程池维护一个消息队列和N个活跃线程对消息队列中的任务进行处理。由于线程池可以设置消息队列的大小和最大线程数，因此，它的占用是可控
的。无论多少个客户端并发访问，都不会导致资源的耗尽和宕机。


--NIO
与Socket类和ServerSocket类相对应，NIO也提供了SocketChannel和ServerSocketChannel两种不同的套接字通道实现。这两种新增的通道都支持
阻塞和非阻塞两种模式。
在面向流的I/O中，可以将数据直接写入或者将数据直接读到Stream对象中，在NIO库中，所有数据都是用缓冲区处理的。在读取数据时，它是直接读到缓冲区
中的；在写入数据时，写入到缓冲区中。任何时候访问NIO中的数据，都是通过缓冲区进行操作。


Netty中的OIO即BIO，现已不支持。
Netty有多种NIO实现

Common: NioEventLoopGroup/NioEventLoop/NioServerSocketChannel/NioSocketChannel
Linux:EpollEventLoopGroup/EpollEventLoop/EpollServerSocketChannel/EpollSocketChannel
macOS/BSD:KQueueEventLoopGroup/KQueueEventLoop/KQueueServerSocketChannel/KQueueSocketChannel

ServerBootStrap.channel 通过泛型+反射+工厂实现I/O模式的自定义切换

Netty对Reactor的支持
Reactor是一种开发模式，模式的核心流程：注册感兴趣的事件->扫描是否有感兴趣的事件发生->事件发生后做出相应的处理

ServerSocketChannel只监听OP_ACCEPT
服务器端SocketChannel只监听OP_WRITE OP_READ
next()用到了策略模式，里面有选择器


Reactor单线程
Reactor多线程
主从Reactor多线程

Netty的main reacotr大多并不能用到一个线程组，只能用到线程组中的一个 why?
因为服务器一般只绑定一个地址一个端口

Netty给Channel分配NIO eventloop的规则是什么
TCP是个“流”协议，所谓流，就是没有界限的一串数据，其间没有分界线。TCP底层并不了解上层业务数据的具体含义，它会根据TCP缓冲区的实际情况进行
包的划分，所以在业务上认为，一个完整的包可能会被TCP拆分成多个包进行发送，也有可能把多个小的包封装成一个大的数据包发送，这就是所谓的TCP粘包
和拆包问题。
TCP发生粘包/拆包的原因有如下三个
1.应用程序write写入的字节大小大于套接字发送缓冲区的大小或者发送的数据大于协议的MTU(必须拆包)
2.进行MSS大小的TCP分段;
3.以太网帧的payload大于MTU进行IP分片.
UDP没有半包粘包问题

处理半包粘包问题的核心：找出消息的边界


LineBasedFrameDecoder的工作原理是它依次便利ByteBuf中的可读字节，判断看是否有"\n"或者"\r\n",如果有，就以此位置为结束位置，从可读索引到
结束位置区间的字节就组成了一行。它是以换行符为结束标志的解码器，支持携带结束符或者不携带结束符两种解码方式，同时支持配置单行的最大长度。如果
连续读取到最大长度后仍然没有发现换行符，就会抛出异常，同时忽略掉之前读到的异常码流。

StringDecoder的功能非常简单，就是将接收到的对象转换为字符串，然后继续调用后面的Handler。LineBasedFrameDecoder+StringDecoder组合就是
按行切换的文本解码器。这一套组合被设计用来支持TCP的粘包和拆包

TCP以流的方式进行数据传输，上层的应用协议为了对消息进行区分，往往采用以下四种方式

1.消息长度固定，累计读取到长度总和为定长LEN的报文后，就认为读取到了一个完整的消息；将计数器置位，重新开始读取下一个数据报；  简单，空间浪费
FixedLengthFrameDecoder
2.将回车换行符作为消息结束符，例如FTP协议，这种方式在文本协议中应用比较广泛
2.1.将特殊的分隔符作为消息的结束标志，回车换行符就是一种特殊的结束分隔符    需要转义
DelimiterBasedFrameDecoder
3.通过在消息头中定义长度字段来标识消息的总长度。  用得较多，长度理论上有限制
LengthFieldBasedFrameDecoder 
Netty对上面四种应用做了统一的抽象，提供了4种解码器来解决对应的问题。

ByteToMessageDecoder中的cumulation使用了策略模式，
decode使用了模板模式

一次解码器

Http协议的弊端
(1)http协议为半双工协议。半双工协议指数据可以在客户端和服务端两个方向是传输，但是不能同时传输，即同一时刻只能由一个方向上的数据传输
(2)http消息包含消息头，消息体，换行符等，通常情况下采用文本方式传输，相比于其他的二进制通信协议,冗长且繁琐
(3)服务端不能主动通知客户端，一般通过客户端长时间轮询

Websocket介绍
Websocket协议是基于TCP的一种新的网络协议，实现了浏览器与服务器之间进行全双工通信(full-duplex)的网络技术，即服务器主动发送消息给客户端。
在Websocket中，浏览器和服务器只需要完成一次握手，两者之间就可以创建持久性的连接，并进行双向数据传输。











