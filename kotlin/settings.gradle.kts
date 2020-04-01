/*
 * Copyright 2017 Square Inc.
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
rootProject.name = "workflow"

include ':internal-testing-utils'
include ':legacy:legacy-workflow-core'
include ':legacy:legacy-workflow-rx2'
include ':legacy:legacy-workflow-test'
include ':samples:containers:app-poetry'
include ':samples:containers:app-raven'
include ':samples:containers:android'
include ':samples:containers:common'
include ':samples:containers:hello-back-button'
include ':samples:containers:poetry'
include ':samples:dungeon:app'
include ':samples:dungeon:common'
include ':samples:dungeon:timemachine'
include ':samples:dungeon:timemachine-shakeable'
include ':samples:hello-compose-binding'
include ':samples:hello-terminal:hello-terminal-app'
include ':samples:hello-terminal:terminal-workflow'
include ':samples:hello-terminal:todo-terminal-app'
include ':samples:hello-workflow'
include ':samples:hello-workflow-fragment'
include ':samples:recyclerview'
include ':samples:tictactoe:app'
include ':samples:tictactoe:common'
include ':samples:todo-android:app'
include ':samples:todo-android:common'
include ':trace-encoder'
include ':workflow-core'
include ':workflow-runtime'
include ':workflow-rx2'
include ':workflow-testing'
include ':workflow-tracing'
include ':workflow-ui:backstack-common'
include ':workflow-ui:backstack-android'
include ':workflow-ui:core-common'
include ':workflow-ui:core-compose'
include ':workflow-ui:core-android'
include ':workflow-ui:modal-common'
include ':workflow-ui:modal-android'
