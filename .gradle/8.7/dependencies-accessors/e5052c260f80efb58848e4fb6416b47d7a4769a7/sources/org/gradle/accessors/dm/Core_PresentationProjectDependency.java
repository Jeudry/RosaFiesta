package org.gradle.accessors.dm;

import org.gradle.api.NonNullApi;
import org.gradle.api.artifacts.ProjectDependency;
import org.gradle.api.internal.artifacts.dependencies.ProjectDependencyInternal;
import org.gradle.api.internal.artifacts.DefaultProjectDependencyFactory;
import org.gradle.api.internal.artifacts.dsl.dependencies.ProjectFinder;
import org.gradle.api.internal.catalog.DelegatingProjectDependency;
import org.gradle.api.internal.catalog.TypeSafeProjectDependencyFactory;
import javax.inject.Inject;

@NonNullApi
public class Core_PresentationProjectDependency extends DelegatingProjectDependency {

    @Inject
    public Core_PresentationProjectDependency(TypeSafeProjectDependencyFactory factory, ProjectDependencyInternal delegate) {
        super(factory, delegate);
    }

    /**
     * Creates a project dependency on the project at path ":core:presentation:designsystem"
     */
    public Core_Presentation_DesignsystemProjectDependency getDesignsystem() { return new Core_Presentation_DesignsystemProjectDependency(getFactory(), create(":core:presentation:designsystem")); }

    /**
     * Creates a project dependency on the project at path ":core:presentation:ui"
     */
    public Core_Presentation_UiProjectDependency getUi() { return new Core_Presentation_UiProjectDependency(getFactory(), create(":core:presentation:ui")); }

}
