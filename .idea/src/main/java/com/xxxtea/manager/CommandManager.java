package com.xxxtea.manager;

import lombok.Getter;

import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

@Getter
public class CommandManager {
	public static CommandManager INSTANCE = new CommandManager();

	private final Queue<String> commandQueue;

	private CommandManager() {
		commandQueue = new ArrayDeque<>();
	}

	public void offer(String command) {
		commandQueue.offer(command);
	}

	public List<String> pollAll() {
		// 使用LinkedList优于ArrayList
		List<String> list = new LinkedList<>();
		while (!commandQueue.isEmpty()) {
			list.add(commandQueue.poll());
		}
		return list;
	}
}
