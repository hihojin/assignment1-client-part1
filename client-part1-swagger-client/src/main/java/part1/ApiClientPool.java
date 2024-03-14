package part1;

import io.swagger.client.ApiClient;
import io.swagger.client.api.SkiersApi;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ApiClientPool {
    // creates client pool for SkiersApi
    private final BlockingQueue<SkiersApi> pool;
    public ApiClientPool(int threads) throws InterruptedException {
        pool = new LinkedBlockingQueue<>(threads);
        for (int i = 0; i < threads; i++) {
            // SkiersApi client = new SkiersApi(new ApiClient().setBasePath("http://localhost:8080/Assignment2SkierServer_war_exploded"));
            SkiersApi client = new SkiersApi(new ApiClient().setBasePath("http://35.92.37.50:8080/Assignment2SkierServer_war"));
            pool.put(client);
        }
    }

    public SkiersApi borrowClient() throws InterruptedException {
        return pool.take();
    }

    public void returnClient(SkiersApi client) {
        pool.add(client);
    }

}
