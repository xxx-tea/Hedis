package com.xxxtea.cluster;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClusterRequest {
	private String command;
	private Object data;
	private String address;
}
