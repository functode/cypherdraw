package com.cypherdraw.cli

import com.cypherdraw.core.AutoUuidPropContext
import com.cypherdraw.core.CypherGenerator
import com.cypherdraw.core.DiagramGraphTransformer
import com.cypherdraw.core.DiagramParser
import com.github.ajalt.clikt.core.CoreCliktCommand
import com.github.ajalt.clikt.core.CoreNoOpCliktCommand
import com.github.ajalt.clikt.core.main
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.inputStream
import com.github.ajalt.clikt.parameters.types.outputStream

fun main(args: Array<String>) = CypherDrawCmd().subcommands(MatchCmd(), CreateCmd()).main(args)

class CypherDrawCmd : CoreNoOpCliktCommand(name = "cypher-draw")

abstract class AbstractCypherDrawSubCmd(
    name: String,
) : CoreCliktCommand(name = name) {
    val prefixIgnore by option("-i", help = "Prefix on the first line of vertex or edge text that marks it to be ignored")
    val allowOnlyCircleNodes by option("-c", help = "Take into account only nodes that have circle as a shape").flag()

    val outputStream by option("-o", help = "Output file for generated Cypher code").outputStream()
    val inputStream by argument(help = "Input file in draw.io XML format").inputStream()
}

class MatchCmd : AbstractCypherDrawSubCmd(name = "match") {
    override fun run() {
        val diagram = DiagramParser().parse(inputStream)
        val graph =
            DiagramGraphTransformer(
                prefixIgnore = prefixIgnore,
                allowOnlyCircleNodes = allowOnlyCircleNodes,
            ).transform(diagram)
        val cypher = CypherGenerator().generateMatchStatement(graph)
        val os = outputStream
        os?.bufferedWriter()?.write(cypher) ?: echo(cypher)
    }
}

class CreateCmd : AbstractCypherDrawSubCmd(name = "create") {
    private val prefixMark by option(
        "-m",
        help = "Prefix on the first line of vertex text that marks it to be used for matching",
    )
    private val autoLabel by option(
        "-l",
        help = "Label name that should be automatically added to created nodes",
    )
    private val autoUuidProp by option(
        "-u",
        help = "Name of the property that should be automatically added to created nodes, containing auto-generated UUIDs",
    )
    private val autoUuidGlobalContext by option(
        "-g",
        help = "Name or other text that is taken into account when generating property with UUID",
    )

    override fun run() {
        val diagram = DiagramParser().parse(inputStream)
        val aUuidProp = autoUuidProp
        val autoUuidPropContext = aUuidProp?.let { AutoUuidPropContext(aUuidProp, autoUuidGlobalContext ?: "") }
        val graph =
            DiagramGraphTransformer(
                prefixIgnore = prefixIgnore,
                prefixMark = prefixMark,
                allowOnlyCircleNodes = allowOnlyCircleNodes,
                autoLabel = autoLabel,
                autoUuidPropContext = autoUuidPropContext,
            ).transform(diagram)
        val cypher = CypherGenerator().generateCreateStatement(graph)
        val os = outputStream
        os?.bufferedWriter()?.write(cypher) ?: echo(cypher)
    }
}
