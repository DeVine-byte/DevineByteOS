package org.devinebyte.compiler.core;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class ProjectModelTest {

    @Test
    void shouldInstantiateProjectModel() {

        ProjectModel model = new ProjectModel();

        assertNotNull(model);

    }

}
