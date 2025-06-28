package it.andrearoma2.backend;

import com.sun.net.httpserver.HttpServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.InetSocketAddress;

public class CallbackServer {
    public static void start() throws IOException {
        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(8888), 0);
            final HttpServer[] serverRef = new HttpServer[1];
            serverRef[0] = server;

            server.createContext("/callback/", exchange -> {
                String query = exchange.getRequestURI().getQuery();
                String code = query.split("=")[1];

                String response = "Login completato. Puoi chiudere la finestra!";
                exchange.sendResponseHeaders(200, response.length());
                OutputStream os = exchange.getResponseBody();
                os.write(response.getBytes());
                os.flush();
                os.close();

                new Thread(() -> {
                    try {
                        Thread.sleep(500);
                        serverRef[0].stop(0);
                        System.out.println("Server chiuso");
                    } catch (InterruptedException e){
                        e.printStackTrace();
                    }
                }).start();

                TokenExchange.exchangeCode(code);
            });

            server.start();
            System.out.println("Server in ascolto su http://localhost:8888/callback/");
        } catch (BindException e) {
        }
    }
}
