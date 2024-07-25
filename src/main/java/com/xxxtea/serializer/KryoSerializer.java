package com.xxxtea.serializer;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import lombok.Cleanup;
import lombok.experimental.UtilityClass;
import org.objenesis.strategy.StdInstantiatorStrategy;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

@UtilityClass
@SuppressWarnings("unchecked")
public class KryoSerializer {
	private final Kryo kryo = new Kryo();

	static {
		// 避免循环引用
		kryo.setReferences(true);
		// 无需显示注册类
		kryo.setRegistrationRequired(false);
		// 无需对序列化的类创建无参构造函数
		kryo.setInstantiatorStrategy(new StdInstantiatorStrategy());
	}

	public <T> T readObject(byte[] bytes) {
		Input input = new Input(new ByteArrayInputStream(bytes));
		return (T) kryo.readClassAndObject(input);
	}

	public byte[] writeObject(Object object) throws IOException {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		@Cleanup Output output = new Output(outputStream);
		kryo.writeClassAndObject(output, object);
		output.flush();
		return outputStream.toByteArray();
	}

	public <T> T readObject(File file) throws IOException {
		Input input = new Input(Files.newInputStream(file.toPath()));
		return (T) kryo.readClassAndObject(input);
	}

	public void writeObject(File file, Object object) throws IOException {
		@Cleanup Output output = new Output(Files.newOutputStream(file.toPath()));
		kryo.writeClassAndObject(output, object);
		output.flush();
	}

}
