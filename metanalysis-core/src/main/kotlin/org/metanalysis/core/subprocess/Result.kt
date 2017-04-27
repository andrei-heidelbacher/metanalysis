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

package org.metanalysis.core.subprocess

sealed class Result {
    data class Success(val input: String) : Result()

    data class Error(val exitValue: Int, val message: String) : Result()

    fun getOrNull(): String? = (this as? Success)?.input

    /**
     * @throws SubprocessException if the subprocess terminated abnormally
     */
    fun get(): String = when (this) {
        is Success -> input
        is Error -> throw SubprocessException(exitValue, message)
    }

    val isSuccess: Boolean
        get() = this is Success
}