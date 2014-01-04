package com.example.hz.lockdemo;

import java.io.IOException;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.UnknownHostException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.hazelcast.cluster.PrepareMergeOperation;
import com.hazelcast.core.Hazelcast;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.instance.HazelcastInstanceImpl;
import com.hazelcast.instance.HazelcastInstanceProxy;
import com.hazelcast.nio.Address;
import com.hazelcast.spi.impl.ResponseHandlerFactory;

/**
 * Hello world!
 * 
 */
public class App {
	
	public static void main(String[] args) throws UnknownHostException, NoSuchMethodException, SecurityException, IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		Logger logger = LoggerFactory.getLogger(App.class);
		logger.info("A");
		
		HazelcastInstance instance = Hazelcast.newHazelcastInstance();
		Method method = HazelcastInstanceProxy.class.getDeclaredMethod("getOriginal");
		method.setAccessible(true);
		HazelcastInstanceImpl instanceImpl = (HazelcastInstanceImpl) method.invoke(instance);
		
		Address address = new Address("localhost", 12345);
		PrepareMergeOperation operation = new PrepareMergeOperation(address);
		operation.setNodeEngine(instanceImpl.node.nodeEngine)
			.setService(instanceImpl.node.getClusterService())
			.setResponseHandler(ResponseHandlerFactory.createEmptyResponseHandler());
		
		instance.getExecutorService("hz:service").execute(new Task(operation));
		
		try {
			System.out.println("Please wait for merge operation");
			System.in.read();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		instance.shutdown();
	}
	
	public static class Task implements Runnable, Serializable {

		private PrepareMergeOperation operation;
		
		public Task(PrepareMergeOperation operation) {
			this.operation = operation;
		}
		
		public void run() {
			operation.run();
		}
		
	}
}
