package net.guidowb.mingming.test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CanaryPool {
	
	private String controllerUrl;
	private ArrayList<Process> canaries = new ArrayList<Process>();
	private File canaryJar = null;

	public CanaryPool(String controllerUrl) {
		this.controllerUrl = controllerUrl;
	}

	public void shutdown() {
		while (!canaries.isEmpty()) stopCanary();
	}

	public void start(int count) {
		while (canaries.size() < count) startCanary();
		while (canaries.size() > count) stopCanary();
	}

	private void stopCanary() {
		Process canary = canaries.remove(0);
		canary.destroyForcibly();
	}

	private void startCanary() {
		Map<String, String> env = new HashMap<String, String>();
		env.put("CONTROLLER", controllerUrl);
		if (canaryJar == null) canaryJar = buildJarFile("mingming-canary");
		Process canary = fork(canaryJar, env);
		canaries.add(canary);
	}

	private static File findProject(String projectName) {
		try {
			File currentDirectory = new File(".").getCanonicalFile();
			while (true) {
				File projectDirectory = new File(currentDirectory, projectName);
				if (projectDirectory.exists() && projectDirectory.isDirectory()) return projectDirectory;
				File parentDirectory = new File(currentDirectory, "..").getCanonicalFile();
				if (parentDirectory.equals(currentDirectory)) throw new FileNotFoundException(projectName);
				currentDirectory = parentDirectory;
			}
		} catch(Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static File buildJarFile(String projectName) {
		try {
			File project = findProject(projectName);
			String shell = System.getenv("SHELL");
			ProcessBuilder processBuilder =  new ProcessBuilder(shell, "-c", "gradle assemble");
			processBuilder.directory(project);
			processBuilder.inheritIO();
			Process build = processBuilder.start();
			build.waitFor();
			File jarFile = findJarFile(projectName);
			if (jarFile == null) throw new FileNotFoundException(projectName);
			return jarFile;
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private static File findJarFile(String projectName) {
		File project = findProject(projectName);
		File directory = new File(project, "/build/libs");
		File[] jars = directory.listFiles(new FilenameFilter() {

			@Override
			public boolean accept(File dir, String name) {
				if (name.matches(projectName + "-[0-9]+\\.[0-9]+\\.[0-9]+-[A-Z]+\\.jar")) return true;
				return false;
			}
			
		});
		if (jars.length > 0) return jars[0];
		else return null;
	}

	private static Process fork(File jar, Map<String, String> env) {
		try {
			String separator = System.getProperty("file.separator");
			String path = System.getProperty("java.home") + separator + "bin" + separator + "java";
			ProcessBuilder processBuilder =  new ProcessBuilder(path, "-Dserver.port=0", "-jar", jar.getCanonicalPath());
			if (env != null) processBuilder.environment().putAll(env);
			processBuilder.inheritIO();
			return processBuilder.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
