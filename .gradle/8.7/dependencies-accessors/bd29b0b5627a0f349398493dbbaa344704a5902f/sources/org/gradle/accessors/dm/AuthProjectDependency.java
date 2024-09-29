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
public class AuthProjectDependency extends DelegatingProjectDependency {

    @Inject
    public AuthProjectDependency(TypeSafeProjectDependencyFactory factory, ProjectDependencyInternal delegate) {
        super(factory, delegate);
    }

    /**
     * Creates a project dependency on the project at path ":auth:data"
     */
    public Auth_DataProjectDependency getData() { return new Auth_DataProjectDependency(getFactory(), create(":auth:data")); }

    /**
     * Creates a project dependency on the project at path ":auth:domain"
     */
    public Auth_DomainProjectDependency getDomain() { return new Auth_DomainProjectDependency(getFactory(), create(":auth:domain")); }

    /**
     * Creates a project dependency on the project at path ":auth:presentation"
     */
    public Auth_PresentationProjectDependency getPresentation() { return new Auth_PresentationProjectDependency(getFactory(), create(":auth:presentation")); }

}
