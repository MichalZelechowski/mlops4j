package org.mlops4j.model.validation;

import org.mlops4j.model.registry.Model;

public interface ValidationStrategy {

    ValidationStatus validate(Model reference);

}
