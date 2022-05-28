dependencies {
  api(projects.kj.kjlib)
  api(libs.fx.base)
  api(projects.kj.json)
  api(libs.kotlinx.serialization.json)
}

plugins {
  kotlin("plugin.serialization")
}