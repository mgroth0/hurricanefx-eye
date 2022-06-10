modtype = LIB

dependencies {
  api(libs.fx.base)
  api(projects.kj.json)
  api(libs.kotlinx.serialization.json)
  implementation(projects.kj.reflect)
}

plugins {
  kotlin("plugin.serialization")
}