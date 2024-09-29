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
public class HomeProjectDependency extends DelegatingProjectDependency {

    @Inject
    public HomeProjectDependency(TypeSafeProjectDependencyFactory factory, ProjectDependencyInternal delegate) {
        super(factory, delegate);
    }

    /**
     * Creates a project dependency on the project at path ":home:data"
     */
    public Home_DataProjectDependency getData() { return new Home_DataProjectDependency(getFactory(), create(":home:data")); }

    /**
     * Creates a project dependency on the project at path ":home:presentation"
     */
    public Home_PresentationProjectDependency getPresentation() { return new Home_PresentationProjectDependency(getFactory(), create(":home:presentation")); }

}
