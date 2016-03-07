package webserver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Vector;

public class Server extends Thread{
	private ServerSocket server;
	private int port;
	private int maxConnexion;
	private Vector<ThreadSocket> client;
	
	public Server(int port, int maxConnexion){
		this.port = port;
		this.maxConnexion = maxConnexion;
	}
	
	public void run(){
		try {
			this.demarre();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void demarre() throws IOException{
		this.server = new ServerSocket(this.port);
		System.out.println("Serveur bien demarré; port: "+this.port);
		while(true){
			new ThreadSocket(this.server.accept());
		}
		
	}
	
	
	class ThreadSocket extends Thread{
		private Socket socket;
		private BufferedReader in;
		private PrintWriter out;
		
		public ThreadSocket(Socket socket){
			super();
			this.socket = socket;
			System.out.println("Connexion réussie "+this.socket.getInetAddress().getHostAddress()+" : "+this.socket.getPort());
			this.start();
		}
		
		public void run(){
			try {
				this.out = new PrintWriter(this.socket.getOutputStream(), true);
				this.in = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
				String msg = "";
				while(true){
					
					if(this.in.ready()){
						String res = this.in.readLine();
						if(res == null || res.equals("")) break;
						msg += res;
						
					}
					
					
				}
				
				this.handle(msg);
				
				this.out.flush();
				this.out.close();
				this.in.close();
				
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			
			
		}
		
		public void handle(String msg){
			String http  = "";
			String path = "";
			String file = "";
			FileInputStream outFile = null;
			long length_file = new File(path).length();
			int type_file = 0;
			
			
			
			System.out.println("requete:"+msg);
			String msg2 = msg.substring(msg.indexOf(" ")+1);
			path = msg2.substring(1, msg2.indexOf(" "));
			//path="index.html";
		//	System.out.println(msg2+":--:"+path+":");
			if(path.equals("") || path==null){
				path="index.html";
			}
			
			if(new File(path).exists()){
				
				System.out.println("Client demande la page "+new File(path).getAbsolutePath());
				
				
				
				//type de fichier
				if(path.endsWith("zip") || path.endsWith(".exe") || path.endsWith(".tar")){
					type_file = 3;
				}else if (path.endsWith(".jpg") || path.endsWith(".jpeg")) {
			        type_file = 1;
			    }else if (path.endsWith(".gif")) {
			        type_file = 2;
			        //write out the header, 200 ->everything is ok we are all happy.
			    }else if (path.endsWith(".ico")) {
			        type_file = 4;
			        //write out the header, 200 ->everything is ok we are all happy.
			    }
				
				
				if(msg.toUpperCase().equals("") || msg == null){ 
					this.send("HTTP/1.0 200 Ok\n");
				}else if(msg.toUpperCase().startsWith("GET")){
					this.send(this.construct_http_header(200, type_file));
					this.sendFile(path);
				}else if(msg.toUpperCase().startsWith("POST")){
					
				}else if(msg.toUpperCase().startsWith("HEAD")){
					
				}else{
					this.send(this.construct_http_header(400, 0));
					this.send("<br>Mauvais requete</br>");
				}
			}else{
				this.send(this.construct_http_header(404, 0));
				this.send("<br>Page demandee inconnue</br>");
				System.out.println("Fichier inexistant");
			}
		}
		
		public String construct_http_header(int return_code, int file_type){
			String res = "HTTP/1.0 ";
			
			switch(return_code){
			case 200:
				res += "200 OK";
				break;
			case 400:
		        res += "400 Bad Request";
		        break;
		    case 403:
		        res += "403 Forbidden";
		        break;
		    case 404:
		    	res += "404 Not Found";
		        break;
		    case 500:
		    	res += "500 Internal Server Error";
		        break;
		    case 501:
		    	res += "501 Not Implemented";
		    	break;
			}
			
			res += "\r\n"; 
			res += "Connection: close\r\n"; 
			res += "Server: SimpleHTTPtutorial v0\r\n"; 
			
			switch (file_type) {
			case 0:
				res += "Content-Type: text/html\r\n";
				break;
		    case 1:
		    	res += "Content-Type: image/jpeg\r\n";
		        break;
		    case 2:
		    	res += "Content-Type: image/gif\r\n";
		    	break;
		    case 3:
		    	res += "Content-Type: application/x-zip-compressed\r\n";
		    	break;
		    case 4:
		    	res += "Content-Type: image/x-icon\r\n";
		    	break;
		    default:
		    	res += "Content-Type: text/html\r\n";
		    	break;
		    }
			
			res += "\r\n";
			
			return res;
		}
		
		public void send(String msg){
			System.out.println("reponse:"+msg);
			this.out.println(msg);
		}
		
		
		public void sendFile(String path){
			BufferedReader outFile = null; //ouverture du fichier demande
			
			try {
				outFile = new BufferedReader(new InputStreamReader(new FileInputStream(path)));
				String res = "";
				try {
					
					while(true){
						if(outFile.ready()){
							res = outFile.readLine();
							this.send(res);
						}else{
							break;
						}
					}
					
					outFile.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				
			} catch (FileNotFoundException e) {
				//e.printStackTrace();
				this.send(this.construct_http_header(400, 0));
			}
			
			
			
		}
		
	}
	
}




