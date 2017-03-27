/*
 * Copyright 2017 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.metanalysis.git

import org.metanalysis.core.versioning.Commit
import org.metanalysis.core.versioning.VersionControlSystem

import java.io.InputStream

class GitVersioningSystem : VersionControlSystem() {
    companion object {
        const val NAME: String = "git"
    }

    override val name: String
        get() = NAME

    override fun getFile(file: String, commitId: String): InputStream {
        TODO("not implemented")
        // git show commitId:file
    }

    override fun getCommit(id: String): Commit {
        TODO("not implemented")
        // git show --name-only commitId
        // git show --name-status commitId
    }

    override fun getCommitHistory(): List<Commit> {
        TODO()
        // git log
    }

    override fun getFileHistory(file: String): List<String> {
        TODO("not implemented")
        // git log file
    }
}