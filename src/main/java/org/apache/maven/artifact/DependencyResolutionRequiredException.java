package org.apache.maven.artifact;

public class DependencyResolutionRequiredException extends Exception {
	
	private static final long serialVersionUID = 1L;

	public DependencyResolutionRequiredException(Artifact artifact) {
		super("Attempted to access the artifact " + artifact + "; which has not yet been resolved");
	}
}
