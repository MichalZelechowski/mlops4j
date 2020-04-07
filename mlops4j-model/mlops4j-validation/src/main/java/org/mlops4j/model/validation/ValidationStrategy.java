package org.mlops4j.model.validation;

import org.mlops4j.model.registry.ModelReference;

public interface ValidationStrategy {

    ValidationStatus validate(ModelReference reference);

}
