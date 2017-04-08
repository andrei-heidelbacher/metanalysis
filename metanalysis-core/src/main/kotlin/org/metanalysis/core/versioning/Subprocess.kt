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

package org.metanalysis.core.versioning

import org.metanalysis.core.versioning.Subprocess.Result.Error
import org.metanalysis.core.versioning.Subprocess.Result.Success
import java.io.IOException
import java.io.InputStream

object Subprocess {
    sealed class Result {
        data class Success(val input: String) : Result()
        data class Error(val exitCode: Int, val message: String) : Result()

        fun getOrNull(): String? = (this as? Success)?.input

        /**
         * @throws SubprocessException if the subprocess terminated abnormally
         */
        fun get(): String = when (this) {
            is Success -> input
            is Error -> throw SubprocessException(exitCode, message)
        }

        val isSuccess: Boolean
            get() = this is Success
    }

    @Throws(IOException::class)
    private fun InputStream.readText(): String = reader().use { it.readText() }

    /**
     * Executes the given `command` in a subprocess and returns its result.
     *
     * @param command the command which should be executed
     * @return the parsed input from `stdout` (if the subprocess terminated
     * normally) or from `stderr` (if the subprocess terminated abnormally)
     * @throws InterruptedException if the subprocess was interrupted
     * @throws IOException if any input related errors occur
     */
    @Throws(InterruptedException::class, IOException::class)
    @JvmStatic fun execute(vararg command: String): Result {
        val process = ProcessBuilder().command(*command).start()
        try {
            process.outputStream.close()
            val input = process.inputStream.readText()
            val error = process.errorStream.readText()
            val exitCode = process.waitFor()
            return if (exitCode == 0) Success(input)
            else Error(exitCode, error)
        } finally {
            process.destroy()
        }
    }
}
