/*
 * Copyright (c) Microsoft Corporation
 *
 * All rights reserved.
 *
 * MIT License
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation
 * the rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and
 * to permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO
 * THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT,
 * TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.microsoft.intellij.runner.container.dockerhost;

import com.intellij.execution.ExecutionException;
import com.intellij.execution.Executor;
import com.intellij.execution.configurations.ConfigurationFactory;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.execution.configurations.RunConfigurationBase;
import com.intellij.execution.configurations.RunProfileState;
import com.intellij.execution.configurations.RuntimeConfigurationException;
import com.intellij.execution.runners.ExecutionEnvironment;
import com.intellij.openapi.options.ConfigurationException;
import com.intellij.openapi.options.SettingsEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.InvalidDataException;
import com.intellij.openapi.util.WriteExternalException;
import com.intellij.util.xmlb.XmlSerializer;
import com.microsoft.azuretools.azurecommons.util.Utils;

import org.jdom.Element;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;

public class DockerHostRunConfiguration extends RunConfigurationBase {
    // TODO: move to util
    private static final String MISSING_ARTIFACT = "A web archive (.war) artifact has not been configured.";
    private static final String INVALID_WAR_FILE = "The artifact name %s is invalid. "
            + "An artifact name may contain only the ASCII letters 'a' through 'z' (case-insensitive), "
            + "and the digits '0' through '9', '.', '-' and '_'.";
    private static final String MISSING_MODEL = "Configuration data model not initialized.";
    private static final String ARTIFACT_NAME_REGEX = "^[.A-Za-z0-9_-]+\\.(war|jar)$";
    private static final String INVALID_DOCKER_HOST = "Please specify a valid docker host.";
    private static final String INVALID_DOCKER_FILE = "Please specify a valid docker file.";
    private static final String INVALID_CERT_PATH = "Please specify a valid certificate path.";
    private static final String MISSING_IMAGE_NAME = "Please specify a valid image name.";
    private final DockerHostRunModel dockerHostRunModel;
    private boolean firstTimeCreated = true;

    protected DockerHostRunConfiguration(@NotNull Project project, @NotNull ConfigurationFactory factory, String name) {
        super(project, factory, name);
        dockerHostRunModel = new DockerHostRunModel();
    }

    public DockerHostRunModel getDockerHostRunModel() {
        return dockerHostRunModel;
    }

    @Override
    public void readExternal(Element element) throws InvalidDataException {
        super.readExternal(element);
        firstTimeCreated = Comparing.equal(element.getAttributeValue("default"), "true");
        XmlSerializer.deserializeInto(dockerHostRunModel, element);
    }

    @Override
    public void writeExternal(Element element) throws WriteExternalException {
        super.writeExternal(element);
        XmlSerializer.serializeInto(dockerHostRunModel, element);
    }

    @NotNull
    @Override
    public SettingsEditor<? extends RunConfiguration> getConfigurationEditor() {
        return new DockerHostRunSettingsEditor(this.getProject());
    }

    @Override
    public void checkConfiguration() throws RuntimeConfigurationException {

    }

    /**
     * Validate input value.
     */
    public void validate() throws ConfigurationException {
        // TODO: add more
        if (dockerHostRunModel == null) {
            throw new ConfigurationException(MISSING_MODEL);
        }
        // docker host
        if (Utils.isEmptyString(dockerHostRunModel.getDockerHost())) {
            throw new ConfigurationException(INVALID_DOCKER_HOST);
        }
        if (Utils.isEmptyString(dockerHostRunModel.getDockerFilePath())
                || !Paths.get(dockerHostRunModel.getDockerFilePath()).toFile().exists()) {
            throw new ConfigurationException(INVALID_DOCKER_FILE);
        }
        if (dockerHostRunModel.isTlsEnabled() && Utils.isEmptyString(dockerHostRunModel.getDockerCertPath())) {
            throw new ConfigurationException(INVALID_CERT_PATH);
        }
        if (Utils.isEmptyString(dockerHostRunModel.getImageName())) {
            throw new ConfigurationException(MISSING_IMAGE_NAME);
        }

        // target package
        if (Utils.isEmptyString(dockerHostRunModel.getTargetName())) {
            throw new ConfigurationException(MISSING_ARTIFACT);
        }
        if (!dockerHostRunModel.getTargetName().matches(ARTIFACT_NAME_REGEX)) {
            throw new ConfigurationException(String.format(INVALID_WAR_FILE, dockerHostRunModel.getTargetName()));
        }
    }

    @Nullable
    @Override
    public RunProfileState getState(@NotNull Executor executor, @NotNull ExecutionEnvironment executionEnvironment)
            throws ExecutionException {
        return new DockerHostRunState(getProject(), dockerHostRunModel);
    }

    public boolean isFirstTimeCreated() {
        return firstTimeCreated;
    }

    public void setFirstTimeCreated(boolean firstTimeCreated) {
        this.firstTimeCreated = firstTimeCreated;
    }

    public String getDockerHost() {
        return dockerHostRunModel.getDockerHost();
    }

    public void setDockerHost(String dockerHost) {
        dockerHostRunModel.setDockerHost(dockerHost);
    }

    public String getDockerCertPath() {
        return dockerHostRunModel.getDockerCertPath();
    }

    public void setDockerCertPath(String dockerCertPath) {
        dockerHostRunModel.setDockerCertPath(dockerCertPath);
    }

    public String getDockerFilePath() {
        return dockerHostRunModel.getDockerFilePath();
    }

    public void setDockerFilePath(String dockerFilePath) {
        dockerHostRunModel.setDockerFilePath(dockerFilePath);
    }

    public boolean isTlsEnabled() {
        return dockerHostRunModel.isTlsEnabled();
    }

    public void setTlsEnabled(boolean tlsEnabled) {
        dockerHostRunModel.setTlsEnabled(tlsEnabled);
    }

    public String getImageName() {
        return dockerHostRunModel.getImageName();
    }

    public void setImageName(String imageName) {
        dockerHostRunModel.setImageName(imageName);
    }

    public String getTagName() {
        return dockerHostRunModel.getTagName();
    }

    public void setTagName(String tagName) {
        dockerHostRunModel.setTagName(tagName);
    }

    public String getTargetPath() {
        return dockerHostRunModel.getTargetPath();
    }

    public void setTargetPath(String targetPath) {
        dockerHostRunModel.setTargetPath(targetPath);
    }

    public String getTargetName() {
        return dockerHostRunModel.getTargetName();
    }

    public void setTargetName(String targetName) {
        dockerHostRunModel.setTargetName(targetName);
    }
}
