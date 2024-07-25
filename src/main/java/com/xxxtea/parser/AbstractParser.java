package com.xxxtea.parser;

import cn.hutool.core.annotation.AnnotationUtil;
import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.StrUtil;
import com.xxxtea.annotation.Command;
import com.xxxtea.common.Constants;
import com.xxxtea.common.HedisException;
import com.xxxtea.manager.CommandManager;
import com.xxxtea.manager.DataManager;
import com.xxxtea.manager.MemoryManager;
import com.xxxtea.model.HedisData;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Getter
public abstract class AbstractParser {
	DataManager dataManager = DataManager.INSTANCE;
	MemoryManager memoryManager = MemoryManager.INSTANCE;
	CommandManager commandManager = CommandManager.INSTANCE;
	private String result;
	@Getter
	private Map<String, CommandModel> commandMap;

	public AbstractParser() {
		this.init();
	}

	private void init() {
		commandMap = new HashMap<>();
		// 获取所有方法
		Method[] methods = ClassUtil.getDeclaredMethods(this.getClass());
		Arrays.stream(methods).forEach(method -> {
			Command command = AnnotationUtil.getAnnotation(method, Command.class);
			if (command != null) {
				commandMap.put(method.getName(), new CommandModel(method, command));
			}
		});
	}

	/**
	 * 命令解析模版方法
	 *
	 * @param command 命令
	 * @return 是否解析成功
	 */
	public boolean doParse(String command) throws HedisException {
		if (StrUtil.isBlank(command)) {
			return false;
		}
		String[] parts = StrUtil.splitToArray(command.trim(), " ");
		if (parts.length == 0) {
			return false;
		}

		lazyDeleteKey(parts);
		String cmd = parts[0].toLowerCase();
		CommandModel commandModel = getCommandMap().get(cmd);
		if (commandModel == null) {
			return false;
		}

		try {
			Object res = commandModel.getMethod().invoke(this, (Object) parts);
			// 返回数据
			this.result = res == null ? Constants.OK : (
					res.toString().isEmpty() ? Constants.EMPTY : res.toString()
			);
			// 记录到AOF日志中
			if (commandModel.getCommand().aof()) {
				commandManager.offer(command);
			}
		} catch (IllegalAccessException e) {
			throw new RuntimeException(e);
		} catch (InvocationTargetException e) {
			// 获取到方法调用中真正的异常
			throw new HedisException(e.getTargetException().getMessage());
		} catch (Exception e) {
			log.error("invoke error", e);
		}
		return true;
	}

	/**
	 * 惰性删除key
	 *
	 * @param parts 命令数组
	 */
	private void lazyDeleteKey(String[] parts) {
		if (parts.length < 2) {
			return;
		}
		String key = parts[1];
		HedisData hedisData = dataManager.get(key);
		if (hedisData != null && hedisData.isExpire()) {
			dataManager.remove(key);
		}
	}

}
