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

import org.junit.AfterClass
import org.junit.BeforeClass
import org.junit.Test

import org.metanalysis.core.subprocess.Subprocess.execute
import org.metanalysis.core.versioning.RevisionNotFoundException
import org.metanalysis.core.versioning.VcsProxyFactory

import java.io.File

import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class GitProxyWithEmptyRepositoryTest : GitProxyTest() {
    companion object {
        @BeforeClass
        @JvmStatic fun initRepository() {
            execute("git", "init")
        }

        @AfterClass
        @JvmStatic fun cleanRepository() {
            val gitDir = ".git"
            File(gitDir).deleteRecursively()
        }

    }

    @Test fun `test connect to empty repository throws`() {
        assertFailsWith<IllegalStateException> {
            VcsProxyFactory.detect()
        }
    }
}