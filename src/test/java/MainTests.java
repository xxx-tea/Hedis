import cn.hutool.core.thread.ThreadUtil;
import cn.hutool.core.util.RandomUtil;
import com.xxxtea.HedisClient;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.CountDownLatch;

public class MainTests {

	public static void main(String[] args) {
		int numClients = 50;
		ExecutorService executorService = Executors.newFixedThreadPool(numClients);
		CountDownLatch latch = new CountDownLatch(numClients);

		for (int i = 0; i < numClients; i++) {
			int clientId = i;
			executorService.submit(() -> {
				try {
					HedisClient client = new HedisClient("localhost", 6379);
					client.connect();
					ThreadUtil.safeSleep(RandomUtil.randomLong(20, 80));
					for (int j = 0; j < 50000; j++) {

						// 构造 SET 和 GET 命令
						String key = clientId + ":" + j;
						String value = RandomUtil.randomString(RandomUtil.randomInt(5, 50));
						client.sendCommand("SET " + key + " " + value);
						// 确保 SET 操作成功
//						String getResult = client.sendCommand("GET " + key);
//						if (!value.equals(getResult)) {
//							throw new AssertionError("SET/GET mismatch for key: " + key);
//						}
					}
					client.stop();
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					latch.countDown();
				}
			});
		}

		// 等待所有客户端完成
		try {
			latch.await();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

		executorService.shutdown();
		System.out.println("All clients finished");
	}

}
