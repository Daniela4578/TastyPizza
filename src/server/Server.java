package server;

import services.UserService;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Server {

    private static final int PORT = 8080;
    private static final int CORE_THREADS = 30;
    private static final int MAX_THREADS = 80;
    private static final int QUEUE_SIZE = 100;
    private static final int IDLE_TIMEOUT = 60;
    private static final int SOCKET_TIMEOUT = 300000;

    private final ExecutorService threadPool = new ThreadPoolExecutor(
            CORE_THREADS,
            MAX_THREADS,
            IDLE_TIMEOUT, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(QUEUE_SIZE),
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private final UserService userService;
    private volatile ServerSocket serverSocket;

    public Server(UserService userService) {
        this.userService = userService;
    }

    public void start() {
        System.out.println("TastyPizza Server started on port " + PORT);
        System.out.println("Core threads:   " + CORE_THREADS);
        System.out.println("Max threads:    " + MAX_THREADS);
        System.out.println("Queue capacity: " + QUEUE_SIZE);
        System.out.println("Waiting for connections...\n");

        try {
            serverSocket = new ServerSocket(PORT);
            registerShutdownHook();

            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Socket clientSocket = serverSocket.accept();

                    clientSocket.setSoTimeout(SOCKET_TIMEOUT);

                    String clientAddress = clientSocket.getInetAddress().getHostAddress();
                    System.out.println("Client connected: " + clientAddress);

                    threadPool.execute(new ClientHandler(clientSocket, userService));

                } catch (IOException e) {
                    if (serverSocket.isClosed()) break;
                    System.out.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("Server failed to start: " + e.getMessage());
        } finally {
            shutdownThreadPool();
            try {
                if (serverSocket != null) serverSocket.close();
            } catch (IOException ignored) {}
        }
    }

    private void registerShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nServer shutting down...");
            try {
                if (serverSocket != null) serverSocket.close();
            } catch (IOException ignored) {}
        }));
    }

    private void shutdownThreadPool() {
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.SECONDS)) {
                threadPool.shutdownNow();
            }
            System.out.println("Server stopped");
        } catch (InterruptedException e) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }
    }



}