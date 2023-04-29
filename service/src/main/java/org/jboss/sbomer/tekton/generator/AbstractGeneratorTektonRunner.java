/**
 * JBoss, Home of Professional Open Source.
 * Copyright 2023 Red Hat, Inc., and individual contributors
 * as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.sbomer.tekton.generator;

import javax.inject.Inject;
import javax.json.Json;

import org.jboss.sbomer.config.GenerationConfig;
import org.jboss.sbomer.config.GenerationConfig.GeneratorConfig;
import org.jboss.sbomer.core.enums.GeneratorImplementation;
import org.jboss.sbomer.tekton.AbstractTektonTaskRunner;

import io.fabric8.tekton.pipeline.v1beta1.TaskRun;

public abstract class AbstractGeneratorTektonRunner extends AbstractTektonTaskRunner {

    @Inject
    GenerationConfig generationConfig;

    protected TaskRun runTektonTask(
            String tektonTaskName,
            Long id,
            GeneratorImplementation generator,
            String version,
            String args) {

        GeneratorConfig generatorConfig = generationConfig.forGenerator(generator);

        var config = Json.createObjectBuilder()
                .add("version", generatorConfig.version(version))
                .add("additional-args", generatorConfig.args(args))
                .build();

        return runTektonTask(tektonTaskName, id, config);
    }
}
