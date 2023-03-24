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






