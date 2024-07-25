import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.xxxtea.HedisClient;
import org.junit.Test;

public class MockTests {

	@Test
	public void name() throws Exception {
		HedisClient hedisClient = new HedisClient("localhost", 6379);
		hedisClient.connect();
		for (int i = 0; i < 100; i++) {
			String key = RandomUtil.randomString(RandomUtil.randomInt(3, 20));
			String value = RandomUtil.randomString(RandomUtil.randomInt(2, 500));
			hedisClient.sendCommand(StrUtil.format("set {} {}", key, value));
		}
		hedisClient.stop();
	}

}
