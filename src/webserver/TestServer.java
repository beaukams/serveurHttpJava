package webserver;

public class TestServer {
	public static void main(String args []){
		Server serv = new Server(20019,17);
		serv.start();
	}
}
