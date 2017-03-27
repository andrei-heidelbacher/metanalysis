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

package org.metanalysis.core

import java.io.File
import java.io.IOException
import java.io.InputStream
import java.net.URL
import java.util.ServiceLoader

/**
 * An abstract source file parser of a specific programming language.
 *
 * Parsers must have a public no-arg constructor.
 *
 * The file `META-INF/services/org.metanalysis.core.Parser` must be provided and
 * must contain the list of all provided parsers.
 */
abstract class Parser {
    companion object {
        private val parsers = ServiceLoader.load(Parser::class.java)
        private val languageToParser = parsers.associateBy(Parser::language)
        private val extensionToParser = parsers.flatMap { parser ->
            parser.extensions.map { extension ->
                extension to parser
            }
        }.toMap()

        /**
         * Returns a parser which can interpret the given programming
         * `language`.
         *
         * @param language the language which must be supported by the parser
         * @return the parser which supports the given `language`, or `null` if
         * no such parser was provided
         */
        @JvmStatic fun getByLanguage(language: String): Parser? =
                languageToParser[language]

        /**
         * Returns a parser which can interpret files with the given
         * `extension`.
         *
         * @param extension the file extension which must be supported by the
         * parser
         * @return the parser which supports the given file `extension`, or
         * `null` if no such parser was provided
         */
        @JvmStatic fun getByExtension(extension: String): Parser? =
                extensionToParser[extension]

        /**
         * Parses the given `file` and returns the associated code meta-data.
         *
         * @param file the file which should be parsed
         * @return the source file metadata, or `null` if none of the provided
         * parsers can interpret the given `file` extension
         * @throws SyntaxError if the given `file` contains invalid source code
         * @throws IOException if an error occurs trying to read the `file`
         * content
         */
        @Throws(SyntaxError::class, IOException::class)
        @JvmStatic fun parseFile(file: File): SourceFile? =
                getByExtension(file.extension)?.parse(file)
    }

    /** Indicates a syntax error detected in a source file. */
    class SyntaxError(
            message: String? = null,
            cause: Throwable? = null
    ) : IOException(message, cause)

    /** The programming language which can be interpreted by this parser. */
    abstract val language: String

    /** The file extensions which can be interpreted by this parser. */
    abstract val extensions: Set<String>

    /**
     * Parses the given `source` code and returns the associated code metadata.
     *
     * @param source the source code which should be parsed
     * @return the source file metadata
     * @throws SyntaxError if the given `source` is not valid source code
     */
    @Throws(SyntaxError::class)
    abstract fun parse(source: String): SourceFile

    /**
     * Parses the given `inputStream` and returns the associated code meta-data.
     *
     * @param inputStream the input stream which should be parsed
     * @return the source file metadata
     * @throws SyntaxError if the given `inputStream` contains invalid source
     * code
     * @throws IOException if an error occurs trying to read the `inputStream`
     * content
     */
    @Throws(SyntaxError::class, IOException::class)
    fun parse(inputStream: InputStream): SourceFile =
            parse(inputStream.reader().readText())

    /**
     * Parses the given `file` and returns the associated code meta-data.
     *
     * @param file the file which should be parsed
     * @return the source file metadata
     * @throws SyntaxError if the given `file` contains invalid source code
     * @throws IOException if an error occurs trying to read the `file` content
     */
    @Throws(SyntaxError::class, IOException::class)
    fun parse(file: File): SourceFile = parse(file.readText())

    /**
     * Parses the content at the given `url` and returns the associated code
     * meta-data.
     *
     * @param url the location of the content which should be parsed
     * @return the source file metadata
     * @throws SyntaxError if the content at the specified `url` contains
     * invalid source code
     * @throws IOException if an error occurs trying to read the content at the
     * specified `url`
     */
    @Throws(SyntaxError::class, IOException::class)
    fun parse(url: URL): SourceFile = parse(url.readText())
}