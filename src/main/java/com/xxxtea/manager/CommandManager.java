package com.xxxtea.manager;

import lombok.Getter;

import java.util.*;

@Getter
public class CommandManager {
	public static CommandManager INSTANCE = new CommandManager();
	private final List<String> commandList;

	private CommandManager() {
		// ArrayDeque偶尔会出现Kryo序列化失败，出现空指针异常
		commandList = new ArrayList<>();
	}

	public void offer(String command) {
		commandList.add(command);
	}

	public List<String> pollAll() {
		// 使用LinkedList优于ArrayList
		List<String> list = new LinkedList<>();
//		while (!commandQueue.isEmpty()) {
//			list.add(commandQueue.poll());
//		}
		return list;
	}
}
