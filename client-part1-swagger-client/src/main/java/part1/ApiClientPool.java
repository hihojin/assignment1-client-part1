package part1;

import io.swagger.client.ApiClient;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ApiClientPool {

    private final BlockingQueue<ApiClient> pool;
    public ApiClientPool(int threads, String basePath) throws InterruptedException {
        pool = new LinkedBlockingQueue<>(threads);
        for (int i = 0; i < threads; i++) {
            ApiClient client = new ApiClient();
            client.setBasePath(basePath);
            pool.put(client);
        }
    }

    public ApiClient borrowClient() throws InterruptedException {
        return pool.take();
    }

    public void returnClient(ApiClient client) {
        pool.add(client);
    }

}
