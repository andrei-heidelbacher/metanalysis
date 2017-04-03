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

package org.metanalysis.core.model

import org.junit.Test

import org.metanalysis.core.model.Node.Function
import org.metanalysis.core.model.Node.Type
import org.metanalysis.core.model.Node.Variable
import org.metanalysis.test.assertEquals

class TypeTest {
    @Test fun `test find variable`() {
        val name = "version"
        val expected = Variable(name, listOf("1"))
        val type = Type(
                name = "IClass",
                members = setOf(
                        Type(name),
                        Function(name, emptyList()),
                        expected
                )
        )
        val actual = type.find<Variable>(name)
        assertEquals(expected, actual)
    }

    @Test fun `test find function`() {
        val name = "version"
        val expected = Function(
                signature = name,
                parameters = emptyList(),
                body = listOf("{", "  return 1;", "}")
        )
        val type = Type(
                name = "IClass",
                members = setOf(
                        Type(name),
                        Variable(name),
                        expected
                )
        )
        val actual = type.find<Function>(name)
        assertEquals(expected, actual)
    }

    @Test fun `test find type`() {
        val name = "version"
        val expected = Type(name, setOf("Object"))
        val type = Type(
                name = "IClass",
                members = setOf(
                        Function(name, emptyList()),
                        Variable(name),
                        expected
                )
        )
        val actual = type.find<Type>(name)
        assertEquals(expected, actual)
    }
}
