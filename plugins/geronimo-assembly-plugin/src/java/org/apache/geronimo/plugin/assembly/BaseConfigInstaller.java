/**
 *
 * Copyright 2005 The Apache Software Foundation
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package org.apache.geronimo.plugin.assembly;

import org.apache.geronimo.gbean.GBeanData;
import org.apache.geronimo.kernel.config.InvalidConfigException;
import org.apache.geronimo.kernel.repository.Artifact;
import org.apache.geronimo.kernel.repository.ArtifactManager;
import org.apache.geronimo.kernel.repository.ArtifactResolver;
import org.apache.geronimo.kernel.repository.DefaultArtifactManager;
import org.apache.geronimo.kernel.repository.DefaultArtifactResolver;
import org.apache.geronimo.kernel.repository.Environment;
import org.apache.geronimo.kernel.repository.FileWriteMonitor;
import org.apache.geronimo.kernel.repository.ListableRepository;
import org.apache.geronimo.kernel.repository.MissingDependencyException;
import org.apache.geronimo.kernel.repository.Repository;
import org.apache.geronimo.kernel.repository.WriteableRepository;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * @version $Rev$ $Date$
 */
public class BaseConfigInstaller {
    /**
     * root file of the targetConfigStore and TargetRepository.  Typically $GERONIMO_HOME of the
     * geronimo server being assembled
     */
    protected File targetRoot;
    /**
     * location of the target config store relative to targetRoot.  Typically "config-store"
     */
    protected String targetConfigStore;
    /**
     * location of the target repository relative to targetRoot.  Typically "repository"
     */
    protected String targetRepository;
    /**
     * location of the configuration to be installed relative to the sourceRepository
     */
    private String artifact;
    /**
     * location of the source repository for the dependencies
     */
    private File sourceRepository;

    public File getTargetRoot() {
        return targetRoot;
    }

    public void setTargetRoot(File targetRoot) {
        this.targetRoot = targetRoot;
    }

    public String getTargetConfigStore() {
        return targetConfigStore;
    }

    public void setTargetConfigStore(String targetConfigStore) {
        this.targetConfigStore = targetConfigStore;
    }

    public String getTargetRepository() {
        return targetRepository;
    }

    public void setTargetRepository(String targetRepository) {
        this.targetRepository = targetRepository;
    }

    public String getArtifact() {
        return artifact;
    }

    public void setArtifact(String artifact) {
        this.artifact = artifact;
    }

    public File getSourceRepository() {
        return sourceRepository;
    }

    public void setSourceRepository(File sourceRepository) {
        this.sourceRepository = sourceRepository;
    }

    protected void execute(InstallAdapter installAdapter, ListableRepository sourceRepo, WriteableRepository targetRepo) throws IOException, InvalidConfigException, MissingDependencyException {
        Artifact configId = Artifact.create(artifact);
        ArtifactManager artifactManager = new DefaultArtifactManager();
        ArtifactResolver artifactResolver = new DefaultArtifactResolver(artifactManager, sourceRepo);
        execute(configId, installAdapter, sourceRepo, targetRepo, artifactManager, artifactResolver);
    }

    protected void execute(Artifact configId, InstallAdapter installAdapter, Repository sourceRepo, WriteableRepository targetRepo, ArtifactManager artifactManager, ArtifactResolver artifactResolver) throws IOException, InvalidConfigException, MissingDependencyException {
        if (installAdapter.containsConfiguration(configId)) {
            System.out.println("Configuration " + configId + " already present in configuration store");
            return;
        }
        GBeanData config = installAdapter.install(sourceRepo, configId);
        Environment environment = (Environment) config.getAttribute("environment");
        LinkedHashSet imports = environment.getImports();
        recursiveExecute(artifactResolver, imports, installAdapter, sourceRepo, targetRepo, artifactManager);

        LinkedHashSet references = environment.getReferences();
        recursiveExecute(artifactResolver, references, installAdapter, sourceRepo, targetRepo, artifactManager);

        LinkedHashSet dependencies = environment.getDependencies();
        dependencies = recursiveResolve(artifactResolver, dependencies, sourceRepo);
        System.out.println("Installed configuration " + configId);

        FileWriteMonitor monitor = new StartFileWriteMonitor();

        for (Iterator iterator = dependencies.iterator(); iterator.hasNext();) {
            Artifact dependency = (Artifact) iterator.next();
            if (!sourceRepo.contains(dependency)) {
                throw new RuntimeException("Dependency: " + dependency + " not found in local maven repo: for configuration: " + artifact);
            }
            if (!targetRepo.contains(dependency)) {
                File sourceFile = sourceRepo.getLocation(dependency);
                InputStream in = new FileInputStream(sourceFile);
                targetRepo.copyToRepository(in, dependency, monitor);
            }
        }
        Artifact[] parentId = (Artifact[]) config.getAttribute("parentId");
        if (parentId != null) {
            for (int i = 0; i < parentId.length; i++) {
                Artifact parent = parentId[i];
                execute(parent, installAdapter, sourceRepo, targetRepo, artifactManager, artifactResolver);
            }
        }
    }

    private void recursiveExecute(ArtifactResolver artifactResolver, LinkedHashSet imports, InstallAdapter installAdapter, Repository sourceRepo, WriteableRepository targetRepo, ArtifactManager artifactManager) throws MissingDependencyException, IOException, InvalidConfigException {
        imports = artifactResolver.resolve(imports);
        for (Iterator iterator = imports.iterator(); iterator.hasNext();) {
            Artifact parentId = (Artifact) iterator.next();
            execute(parentId, installAdapter, sourceRepo, targetRepo, artifactManager, artifactResolver);
        }
    }

    private LinkedHashSet recursiveResolve(ArtifactResolver artifactResolver, LinkedHashSet dependencies, Repository repository) throws MissingDependencyException {
        dependencies = artifactResolver.resolve(dependencies);
        for (Iterator iterator = new ArrayList(dependencies).iterator(); iterator.hasNext();) {
            Artifact dependency = (Artifact) iterator.next();
            if (repository.contains(dependency)) {
                LinkedHashSet subDependencies = repository.getDependencies(dependency);
                subDependencies = recursiveResolve(artifactResolver, subDependencies, repository);
                dependencies.addAll(subDependencies);
            }
        }
        return dependencies;
    }

    protected interface InstallAdapter {

        GBeanData install(Repository sourceRepo, Artifact configId) throws IOException, InvalidConfigException;

        boolean containsConfiguration(Artifact configID);
    }

    static class StartFileWriteMonitor implements FileWriteMonitor {
        public void writeStarted(String fileDescription) {
            System.out.println("Copying " + fileDescription);
        }

        public void writeProgress(int bytes) {

        }

        public void writeComplete(int bytes) {

        }
    }
}
