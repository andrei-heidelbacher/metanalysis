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

@file:JvmName("Builders")

package org.chronolens.test.core.model

import org.chronolens.core.model.AddNode
import org.chronolens.core.model.EditFunction
import org.chronolens.core.model.EditType
import org.chronolens.core.model.EditVariable
import org.chronolens.core.model.Function
import org.chronolens.core.model.Project
import org.chronolens.core.model.RemoveNode
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourceNode.Companion.CONTAINER_SEPARATOR
import org.chronolens.core.model.SourceNode.Companion.MEMBER_SEPARATOR
import org.chronolens.core.model.Type
import org.chronolens.core.model.Variable
import org.chronolens.core.model.parentId
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply

fun id(): IdBuilder = IdBuilder()

fun project(init: Init<ProjectBuilder>): Project =
    ProjectBuilder().apply(init).build()

fun sourceFile(path: String, init: Init<SourceFileBuilder>): SourceFile =
    SourceFileBuilder(path).apply(init).build()

fun type(id: String, init: Init<TypeBuilder>): Type {
    val parentId = id.parentId ?: error("Invalid type id '$id'!")
    val name = id.substringAfterLast(CONTAINER_SEPARATOR)
    return TypeBuilder(name).apply(init).build(parentId)
}

fun function(id: String, init: Init<FunctionBuilder>): Function {
    val parentId = id.parentId ?: error("Invalid function id '$id'!")
    val signature = id.substringAfterLast(MEMBER_SEPARATOR)
    return FunctionBuilder(signature).apply(init).build(parentId)
}

fun variable(id: String, init: Init<VariableBuilder>): Variable {
    val parentId = id.parentId ?: error("Invalid variable id '$id'!")
    val name = id.substringAfterLast(MEMBER_SEPARATOR)
    return VariableBuilder(name).apply(init).build(parentId)
}

fun addSourceFile(path: String, init: Init<SourceFileBuilder>): AddNode =
    AddNode(sourceFile(path, init))

fun addType(id: String, init: Init<TypeBuilder>): AddNode =
    AddNode(type(id, init))

fun addFunction(id: String, init: Init<FunctionBuilder>): AddNode =
    AddNode(function(id, init))

fun addVariable(id: String, init: Init<VariableBuilder>): AddNode =
    AddNode(variable(id, init))

fun removeNode(id: String): RemoveNode = RemoveNode(id)

fun editType(id: String, init: Init<EditTypeBuilder>): EditType =
    EditTypeBuilder(id).apply(init).build()

fun editFunction(id: String, init: Init<EditFunctionBuilder>): EditFunction =
    EditFunctionBuilder(id).apply(init).build()

fun editVariable(id: String, init: Init<EditVariableBuilder>): EditVariable =
    EditVariableBuilder(id).apply(init).build()

internal inline fun <reified T> newBuilder(simpleId: String): T =
    T::class.java.getConstructor(String::class.java).newInstance(simpleId)

internal fun <T> Array<T>.requireDistinct(): Set<T> {
    val set = LinkedHashSet<T>(size)
    for (element in this) {
        require(element !in set) { "Duplicated element '$element'!" }
        set += element
    }
    return set
}
