package com.xxxtea.config;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.Dict;
import cn.hutool.setting.yaml.YamlUtil;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;

/**
 * 增强Yaml解析器，能够自定义解析枚举和嵌套pojo里的枚举
 */
public class EnhanceYaml {
	Dict dict;
	CopyOptions options = new CopyOptions();
	Map<String, Function<String, Object>> convertMap = new HashMap<>();
	Map<String, Function<String, Object>> convertPathMap = new HashMap<>();

	public void addCustomConvert(Function<String, Object> convert, Class<?> targetClass) {
		convertMap.put(targetClass.getTypeName(), convert);
	}

	public void addCustomPathConvert(String path, Function<String, Object> convert) {
		// 多级解析 cluster.line.type
		if (path.contains(".")) {
			convertPathMap.put(path, convert);
		}
	}

	public <T> T load(String loadPath, Class<T> clazz) {
		this.dict = YamlUtil.loadByPath(loadPath);

		this.convertPathMap.forEach((path, convert) -> {
			String fatherPath = path.substring(0, path.lastIndexOf("."));
			String name = path.substring(path.lastIndexOf(".") + 1);
			LinkedHashMap<String, Object> hashMap = dict.getByPath(fatherPath);
			Object value = hashMap.get(name);
			Object parsedValue = convert.apply(value.toString());
			hashMap.put(name, parsedValue);
		});

		options.setConverter((type, value) -> {
			if (value == null) {
				return null;
			}

			Function<String, Object> convert = convertMap.get(type.getTypeName());
			// 判断类型是否匹配
			if (convert != null) {
				// 执行自定义转换逻辑
				return convert.apply(value.toString());
			}
			return value;
		});
		return BeanUtil.toBean(dict, clazz, options);
	}

}
