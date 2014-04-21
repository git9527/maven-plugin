package com.travelsky.maven;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.maven.archiver.MavenArchiveConfiguration;
import org.apache.maven.archiver.MavenArchiver;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.deployer.ArtifactDeployer;
import org.apache.maven.artifact.deployer.ArtifactDeploymentException;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.artifact.repository.ArtifactRepository;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.Authentication;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.DefaultRepositoryLayout;
import org.apache.maven.execution.MavenSession;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Execute;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;
import org.apache.maven.project.artifact.ProjectArtifactMetadata;
import org.codehaus.plexus.archiver.jar.JarArchiver;

@Mojo(name = "deploy", defaultPhase = LifecyclePhase.PACKAGE)
@Execute(phase = LifecyclePhase.COMPILE)
public class TravelskyDeployMojo extends AbstractMojo {

	private static final String[] DEFAULT_EXCLUDES = new String[] { "**/package.html" };

	@Component
	private MavenProjectHelper projectHelper;

	@Parameter(required = true)
	private String[] includes;

	@Parameter(required = true)
	private String version;

	@Parameter(required = true)
	private String artifactId;

	@Parameter(defaultValue = "${project.groupId}")
	private String groupId;

	@Parameter(defaultValue = "${project.build.outputDirectory}")
	private File classesDirectory;

	@Parameter(defaultValue = "${project}")
	private MavenProject project;

	@Parameter(defaultValue = "${session}")
	private MavenSession session;
	
	@Parameter(defaultValue = "http://172.25.5.79:7081/nexus/content/repositories/releases/")
	private String releaseUrl;
	
	@Parameter(defaultValue = "deployer")
	private String userName;
	
	@Parameter(defaultValue = "deployer")
	private String password;

	@Component
	private ArtifactDeployer deployer;

	@Parameter(defaultValue = "${localRepository}", required = true, readonly = true)
	private ArtifactRepository localRepository;

	@Override
	public void execute() throws MojoExecutionException {
		// 1.生成sdk包
		File sdkFile = this.createFile(Arrays.asList(classesDirectory.getPath()), artifactId + "-" + version + ".jar");
		// 2.生成源码包
		File sourceFile = this.createFile(project.getCompileSourceRoots(), artifactId + "-" + version + "-sources.jar");
		// 3.上传两个包
		try {
			this.deployFile(sdkFile, null);
			this.deployFile(sourceFile, "sources");
		} catch (ArtifactDeploymentException e) {
			throw new MojoExecutionException("上传jar包出现问题", e);
		}
		super.getLog().info("------------------------------------------------------------------------");
		for (String string : includes) {
			super.getLog().info("待打包的文件路径:" + string);
		}
		super.getLog().info("根据以上" + includes.length + "条规则,生成sdk包:" + sdkFile.getPath());
		super.getLog().info("根据以上" + includes.length + "条规则,生成源码包:" + sourceFile.getPath());
		super.getLog().info("sdk包,源码包已上传至远程仓库:" + this.getRepository().getUrl());
	}

	private File createPomFile() throws MojoExecutionException {
		File file = new File(project.getBuild().getDirectory(), "pom.xml");
		try {
			FileWriter fileWriter = new FileWriter(file);
			fileWriter.write(this.getPomString());
			fileWriter.close();
		} catch (IOException e) {
			throw new MojoExecutionException("生成POM文件出现问题", e);
		}
		return file;
	}

	private String getPomString() {
		StringBuffer builder = new StringBuffer();
		builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n");
		builder.append("<project xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\" ");
		builder.append("xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">").append("\n");
		builder.append("<modelVersion>4.0.0</modelVersion>").append("\n");
		builder.append("<groupId>").append(groupId).append("</groupId>").append("\n");
		builder.append("<artifactId>").append(artifactId).append("</artifactId>").append("\n");
		builder.append("<version>").append(version).append("</version>").append("\n");
		builder.append("<description>").append("POM was created by Travelsky Maven Plugin").append("</description>").append("\n");
		builder.append("</project>");
		return builder.toString();
	}

	private void deployFile(File sourceFile, String classifier) throws ArtifactDeploymentException, MojoExecutionException {
		Artifact artifact = this.getJarArtifact(classifier);
		if (null == classifier || "".equals(classifier)) {
			ProjectArtifactMetadata metadata = new ProjectArtifactMetadata(artifact, this.createPomFile());
			artifact.addMetadata(metadata);
		}
		deployer.deploy(sourceFile, artifact, this.getRepository(), localRepository);
	}

	private Artifact getJarArtifact(String classifier) {
		DefaultArtifactHandler handler = new DefaultArtifactHandler("jar");
		handler.setAddedToClasspath(true);
		handler.setExtension("jar");
		return new DefaultArtifact(groupId, artifactId, version, "compile", "jar", classifier, handler);
	}

	private ArtifactRepository getRepository() {
		MavenArtifactRepository repository = new MavenArtifactRepository();
		repository.setId("nexus-releases");
		repository.setUrl(releaseUrl);
		repository.setLayout(new DefaultRepositoryLayout());
		repository.setAuthentication(new Authentication(userName, password));
		repository.setReleaseUpdatePolicy(this.getReleasePolicy());
		return repository;
	}

	private ArtifactRepositoryPolicy getReleasePolicy() {
		ArtifactRepositoryPolicy policy = new ArtifactRepositoryPolicy();
		policy.setEnabled(true);
		policy.setChecksumPolicy(ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN);
		policy.setUpdatePolicy(ArtifactRepositoryPolicy.UPDATE_POLICY_DAILY);
		return policy;
	}

	private File createFile(List<String> directorys, String fileName) throws MojoExecutionException {
		MavenArchiver archiver = new MavenArchiver();
		MavenArchiveConfiguration archive = new MavenArchiveConfiguration();
		archiver.setArchiver(new JarArchiver());
		for (String directory : directorys) {
			archiver.getArchiver().addDirectory(new File(directory), includes, DEFAULT_EXCLUDES);
		}
		File outputFile = new File(project.getBuild().getDirectory(), fileName);
		try {
			archiver.setOutputFile(outputFile);
			archive.setAddMavenDescriptor(false);
			archiver.createArchive(session, project, archive);
		} catch (Exception e) {
			throw new MojoExecutionException("打包文件时发生异常", e);
		}
		return outputFile;
	}

}
