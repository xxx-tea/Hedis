package com.xxxtea.parser;

import com.xxxtea.annotation.Command;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.lang.reflect.Method;

@Data
@AllArgsConstructor
public class CommandModel {
	private Method method;
	private Command command;
}
