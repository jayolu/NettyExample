package cc.dozer.netty.example;

/**
 * “µŒÒ≤‚ ‘¿‡
 * @author jayolu
 *
 */
public class TestBusiness {

	public static void main(String[] args) throws Exception {
        TcpServer server = new TcpServer(18080);
        TcpClient client = new TcpClient("127.0.0.1", 18080);
        
        server.init();
        client.init();
        
        for(int i=0;i<10;i++) {
        	client.send("order a mill\n");
        	Thread.sleep(1000);
        }
        Thread.sleep(100000);
        
        client.close();
        server.close();

	}

}
