/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.cli

import picocli.CommandLine.Model.CommandSpec
import picocli.CommandLine.Model.OptionSpec
import picocli.CommandLine.Model.UsageMessageSpec
import java.lang.reflect.Field

abstract class Command : Runnable {
    internal val command by lazy {
        val spec = CommandSpec.forAnnotatedObjectLenient(this)
            .name(name)
            .usageMessage(UsageMessageSpec().description(*help.paragraphs()))
            .mixinStandardHelpOptions(standardHelpOptions)
        version?.paragraphs()?.let(spec::version)
        spec
    }

    abstract val name: String
    protected abstract val version: String?
    protected abstract val help: String
    protected abstract val standardHelpOptions: Boolean

    protected abstract fun execute()

    protected fun <T : Any> option(
        type: Class<T>,
        vararg names: String
    ): NullableOption<T> = NullableOption(OptionSpec.builder(names).type(type))

    protected inline fun <reified T : Any> option(
        vararg names: String
    ): NullableOption<T> = option(T::class.java, *names)

    private fun validateOptions() {
        val fields = arrayListOf<Field>()
        var type: Class<*>? = this::class.java
        while (type != null) {
            fields += type.declaredFields
            type = type.superclass
        }
        for (field in fields) {
            if (OptionDelegate::class.java.isAssignableFrom(field.type)) {
                field.isAccessible = true
                val delegate = field.get(this) as OptionDelegate<*>
                delegate.getValue()
            }
        }
    }

    final override fun run() {
        validateOptions()
        execute()
    }
}