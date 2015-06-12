package net.guidowb.mingming.test;

import net.guidowb.mingming.worker.MingMingWorker;

public class WorkerManager {

	@SuppressWarnings("rawtypes")
	public static Process fork(Class clazz) throws Exception {
		String separator = System.getProperty("file.separator");
		String classpath = System.getProperty("java.class.path");
		String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
		ProcessBuilder processBuilder =  new ProcessBuilder(path, "-cp", classpath, clazz.getName());
		return processBuilder.start();
	}
	
	public static void createWorker() throws Exception {
		fork(MingMingWorker.class);
	}
}
