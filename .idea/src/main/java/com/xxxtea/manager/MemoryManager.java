package com.xxxtea.manager;

import com.xxxtea.model.HedisData;
import jdk.nashorn.internal.ir.debug.ObjectSizeCalculator;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class MemoryManager {
	public static MemoryManager INSTANCE = new MemoryManager();
	private final ObjectSizeCalculator calculator;

	public MemoryManager() {
		ObjectSizeCalculator.MemoryLayoutSpecification specification = ObjectSizeCalculator.getEffectiveMemoryLayoutSpecification();
		this.calculator = new ObjectSizeCalculator(specification);
	}

	public long calculate(Object obj) {
		return calculator.calculateObjectSize(obj);
	}

	/**
	 * 获取已使用的内存
	 */
	public long getUsedMemory() {
		return calculator.calculateObjectSize(DataManager.INSTANCE.getMap());
	}

	/**
	 * 占用是否超过了预设的内存
	 */
	public boolean isOverMemory() {
		return getUsedMemory() > 5000;
	}

	/**
	 * 清除所有缓存
	 */
	public int clearAll() {
		Map<String, HedisData> map = DataManager.INSTANCE.getMap();
		int size = map.size();
		map.clear();
		return size;
	}

	/**
	 * 按缓存命中次数清除缓存
	 */
	public void clearByHitCount() {
//		clear(Comparator.comparingInt(Record::getHitCount));
	}

	/**
	 * 按缓存空间大小清除缓存
	 */
	public void clearByObjectSize() {
//		clear(Comparator.comparingLong(ObjectSizeCalculator::getObjectSize));
	}

	/**
	 * 清除缓存
	 */
	public void clear() {
		DataManager dataManager = DataManager.INSTANCE;
		Map<String, HedisData> map = dataManager.getMap();
		List<Map.Entry<String, HedisData>> entryList = new ArrayList<>(map.entrySet());
		entryList.sort(Comparator.comparingLong(entry -> entry.getValue().getAccessTime()));
	}

}
