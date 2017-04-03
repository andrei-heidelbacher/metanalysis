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

package org.metanalysis.java

import org.eclipse.jdt.core.JavaCore
import org.eclipse.jdt.core.dom.AST
import org.eclipse.jdt.core.dom.ASTParser
import org.eclipse.jdt.core.dom.ASTParser.K_COMPILATION_UNIT
import org.eclipse.jdt.core.dom.AbstractTypeDeclaration
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration
import org.eclipse.jdt.core.dom.BodyDeclaration
import org.eclipse.jdt.core.dom.CompilationUnit
import org.eclipse.jdt.core.dom.EnumConstantDeclaration
import org.eclipse.jdt.core.dom.EnumDeclaration
import org.eclipse.jdt.core.dom.FieldDeclaration
import org.eclipse.jdt.core.dom.Initializer
import org.eclipse.jdt.core.dom.MethodDeclaration
import org.eclipse.jdt.core.dom.SingleVariableDeclaration
import org.eclipse.jdt.core.dom.TypeDeclaration
import org.eclipse.jdt.core.dom.VariableDeclaration

import org.metanalysis.core.model.Node.Function
import org.metanalysis.core.model.Node.Type
import org.metanalysis.core.model.Node.Variable
import org.metanalysis.core.model.Parser
import org.metanalysis.core.model.SourceFile

import java.io.IOException

/** Java 8 language parser. */
class JavaParser : Parser() {
    companion object {
        /** The `Java` programming language supported by this parser. */
        const val LANGUAGE: String = "Java"

        /** The Java file extensions. */
        val EXTENSIONS: Set<String> = setOf("java")

        /**
         * Formats this string to a block of code by splitting it into lines
         * (delimited by `\n`), removing blank lines (those which consist only
         * of whitespaces) and trims leading and trailing whitespaces from all
         * lines.
         */
        @JvmStatic fun String.toBlock(): List<String> = this.split('\n')
                .filter(String::isNotBlank)
                .map(String::trim)
    }

    private data class Context(private val source: String) {
        private fun substring(startPosition: Int, length: Int): String =
                source.substring(startPosition, startPosition + length)

        fun getBody(method: MethodDeclaration): List<String> =
                substring(method.startPosition, method.length)
                        .dropWhile { it != '{' }.toBlock()

        fun getInitializer(variable: VariableDeclaration): List<String> =
                substring(variable.startPosition, variable.length)
                        .dropWhile { it != '=' }
                        .let {
                            if (it.firstOrNull() == '=') it.drop(1)
                            else it
                        }.toBlock()

        fun getInitializer(
                enumConstant: EnumConstantDeclaration
        ): List<String> = substring(
                enumConstant.startPosition,
                enumConstant.length
        ).toBlock()

        fun getDefaultValue(
                annotationMember: AnnotationTypeMemberDeclaration
        ): List<String> = annotationMember.default?.let { value ->
            substring(value.startPosition, value.length).toBlock()
        } ?: emptyList()
    }

    private fun <T> Collection<T>.requireDistinct(): Set<T> = toSet().let {
        require(size == it.size) { "$this contains duplicated elements!" }
        it
    }

    /** Returns the name of this node. */
    private fun AbstractTypeDeclaration.name() = name.identifier
    private fun AnnotationTypeMemberDeclaration.name() = name.identifier
    private fun EnumConstantDeclaration.name() = name.identifier
    private fun MethodDeclaration.name() = name.identifier
    private fun VariableDeclaration.name(): String = name.identifier

    /** Returns the list of all supertypes of this type. */
    private fun AbstractTypeDeclaration.supertypes() = when (this) {
        is AnnotationTypeDeclaration -> emptyList()
        is EnumDeclaration -> superInterfaceTypes()
        is TypeDeclaration -> superInterfaceTypes() + superclassType
        else -> throw AssertionError("Unknown declaration $this!")
    }.filterNotNull().map(Any::toString)

    /**
     * Returns the list of all members of this type.
     *
     * The returned elements can be of the following types:
     * - [AbstractTypeDeclaration]
     * - [AnnotationTypeMemberDeclaration]
     * - [EnumConstantDeclaration]
     * - [VariableDeclaration]
     * - [MethodDeclaration]
     * - [Initializer]
     */
    private fun AbstractTypeDeclaration.members() = (
            if (this is EnumDeclaration) enumConstants()
            else emptyList<BodyDeclaration>()
    ) + bodyDeclarations().flatMap { member ->
        if (member is FieldDeclaration) member.fragments()
        else listOf(member)
    }

    private fun Context.visit(node: AbstractTypeDeclaration): Type {
        val members = node.members().map { member ->
            when (member) {
                is AbstractTypeDeclaration -> visit(member)
                is AnnotationTypeMemberDeclaration -> visit(member)
                is EnumConstantDeclaration -> visit(member)
                is VariableDeclaration -> visit(member)
                is MethodDeclaration -> visit(member)
                is Initializer -> throw IOException("Can't parse initializers!")
                else -> throw AssertionError("Unknown declaration $member!")
            }
        }
        return Type(
                name = node.name(),
                supertypes = node.supertypes().requireDistinct(),
                members = members.requireDistinct()
        )
    }

    private fun Context.visit(node: AnnotationTypeMemberDeclaration): Variable =
            Variable(node.name(), getDefaultValue(node))

    private fun Context.visit(node: EnumConstantDeclaration): Variable =
            Variable(node.name(), getInitializer(node))

    private fun Context.visit(node: VariableDeclaration): Variable =
            Variable(node.name(), getInitializer(node))

    private fun Context.visit(node: MethodDeclaration): Function {
        val parameters = node.parameters()
                .filterIsInstance<SingleVariableDeclaration>()
        val parameterTypes = parameters.map(SingleVariableDeclaration::getType)
        return Function(
                signature = node.name() + "(${parameterTypes.joinToString()})",
                parameters = parameters.map { visit(it) },
                body = getBody(node)
        )
    }

    private fun Context.visit(node: CompilationUnit): SourceFile =
            node.types().filterIsInstance<AbstractTypeDeclaration>()
                    .map { visit(it) }.requireDistinct().let(::SourceFile)

    override val language: String
        get() = LANGUAGE

    override val extensions: Set<String>
        get() = EXTENSIONS

    @Throws(IOException::class)
    override fun parse(source: String): SourceFile {
        try {
            val jdtParser = ASTParser.newParser(AST.JLS8)
            jdtParser.setKind(K_COMPILATION_UNIT)
            jdtParser.setSource(source.toCharArray())
            val options = JavaCore.getOptions()
            JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options)
            jdtParser.setCompilerOptions(options)
            jdtParser.setIgnoreMethodBodies(true)
            val compilationUnit = jdtParser.createAST(null) as CompilationUnit
            return Context(source).visit(compilationUnit)
        } catch (e: Exception) {
            throw IOException(e)
        }
    }
}
