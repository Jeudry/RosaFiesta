plugins {
  alias(libs.plugins.rosafiesta.jvm.library)
}

dependencies {
  implementation(projects.core.domain)
}