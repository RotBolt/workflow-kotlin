/*
 * Copyright 2019 Square Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
apply plugin: 'com.android.library'
apply plugin: 'kotlin-android'

sourceCompatibility = JavaVersion.VERSION_1_8
targetCompatibility = JavaVersion.VERSION_1_8

apply from: rootProject.file('.buildscript/configure-maven-publish.gradle')

apply from: rootProject.file('.buildscript/configure-android-defaults.gradle')

apply from: rootProject.file('.buildscript/configure-compose.gradle')

dependencies {
  api project(':workflow-ui:core-android')
  api Dep.get("kotlin.stdLib.jdk8")

  implementation project(':workflow-runtime')
  implementation Dep.get("compose.foundation")
  implementation Dep.get("compose.layout")
  implementation Dep.get("compose.tooling")

  testImplementation Dep.get("test.junit")
  testImplementation Dep.get("test.truth")
}
