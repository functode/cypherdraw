package com.cypherdraw.core

import org.jsoup.Jsoup
import org.jsoup.nodes.Document.OutputSettings
import org.jsoup.nodes.Entities
import org.jsoup.parser.Parser
import org.jsoup.safety.Safelist
import java.util.UUID

class DiagramGraphTransformer(
    prefixIgnore: String? = null,
    prefixMark: String? = null,
    allowOnlyCircleNodes: Boolean = false,
    autoLabel: String? = null,
    autoUuidPropContext: AutoUuidPropContext? = null,
) {
    private val diagramEntityTextParser = DiagramEntityTextParser(HtmlSplitter())
    private val linesToPropsTransformer = LinesToPropsTransformer(autoUuidPropContext)
    private val dVertexToGNodeTransformer =
        DVertexToGNodeTransformer(
            diagramEntityTextParser,
            linesToPropsTransformer,
            prefixIgnore,
            prefixMark,
            allowOnlyCircleNodes,
            autoLabel,
        )
    private val dEdgeToGRelTransformer = DEdgeToGRelTransformer(diagramEntityTextParser, linesToPropsTransformer, prefixIgnore, prefixMark)

    fun transform(diagram: DiagramData): Graph {
        val nodes = diagram.vertexes.mapNotNull { dVertexToGNodeTransformer.transform(it) }
        val nodeIds = nodes.map { it.id }
        val rels =
            diagram.edges
                .mapNotNull { dEdgeToGRelTransformer.transform(it) }
                .filter { (it.sourceId in nodeIds) && (it.targetId in nodeIds) }
        return Graph(nodes, rels)
    }
}

data class AutoUuidPropContext(
    val propName: String,
    val globalUuidContext: String,
)

private class DVertexToGNodeTransformer(
    private val diagramEntityTextParser: DiagramEntityTextParser,
    private val linesToPropsTransformer: LinesToPropsTransformer,
    private val prefixIgnore: String?,
    private val prefixMark: String?,
    private val allowOnlyCircleNodes: Boolean,
    private val autoLabel: String?,
) {
    fun transform(vertex: DiagramVertex): GraphNode? {
        if (allowOnlyCircleNodes && (!vertex.style.startsWith("ellipse;"))) {
            return null
        }

        val textLines = diagramEntityTextParser.parse(vertex.text) ?: return null

        if (textStartsWith(textLines.firstLine, prefixIgnore)) {
            return null
        }

        val (isMarked, labelsText) =
            if (textStartsWith(textLines.firstLine, prefixMark)) {
                Pair(true, textLines.firstLine.substring(prefixMark?.length ?: 0))
            } else {
                Pair(false, textLines.firstLine)
            }

        val labels =
            labelsText
                .split(Regex(",\\s*"))
                .map { it.trim() }
                .filter { it.isNotEmpty() }
                .map { GraphLabel(it) }
                .let {
                    if (!isMarked && (autoLabel != null)) it + GraphLabel(autoLabel) else it
                }.distinct()
        val props = linesToPropsTransformer.transform(textLines.remainingLines, vertex.id.string, isMarked)
        return GraphNode(GraphNodeId(vertex.id.string), labels, props, isMarked)
    }
}

private class DEdgeToGRelTransformer(
    private val diagramEntityTextParser: DiagramEntityTextParser,
    private val linesToPropsTransformer: LinesToPropsTransformer,
    private val prefixIgnore: String?,
    private val prefixMark: String?,
) {
    fun transform(edge: DiagramEdge): GraphRel? {
        val textLines = diagramEntityTextParser.parse(edge.text) ?: return null

        if (textStartsWith(textLines.firstLine, prefixIgnore)) {
            return null
        }

        val (isMarked, type) =
            if (textStartsWith(textLines.firstLine, prefixMark)) {
                Pair(true, textLines.firstLine.substring(prefixMark?.length ?: 0))
            } else {
                Pair(false, textLines.firstLine)
            }

        if (type.isEmpty()) {
            return null
        }

        val props = linesToPropsTransformer.transform(textLines.remainingLines, edge.id.string, isMarked)
        return GraphRel(GraphLabel(type), GraphNodeId(edge.sourceId.string), GraphNodeId(edge.targetId.string), props, isMarked)
    }
}

private fun textStartsWith(
    text: String,
    prefix: String?,
) = ((prefix != null) && text.startsWith(prefix))

private data class DiagramEntityTextLines(
    val firstLine: String,
    val remainingLines: List<String>,
)

private class DiagramEntityTextParser(
    private val htmlSplitter: HtmlSplitter,
) {
    fun parse(text: String): DiagramEntityTextLines? {
        val parsedLines = htmlSplitter.split(text)
        if (parsedLines.isEmpty()) {
            return null
        }
        return DiagramEntityTextLines(parsedLines[0], parsedLines.subList(1, parsedLines.size))
    }
}

private class HtmlSplitter {
    val outputSettings: OutputSettings = OutputSettings().escapeMode(Entities.EscapeMode.xhtml)

    fun split(text: String): List<String> {
        val cleanedText = Jsoup.clean(text, "", Safelist.none().addTags("br", "div"), outputSettings).trim()
        return cleanedText
            .split("<br>", "<div>")
            .map { Jsoup.clean(it, "", Safelist.none(), outputSettings).trim() }
            .filter { it.isNotEmpty() }
            .map { Parser.unescapeEntities(it, true) }
    }
}

private val DEFAULT_PROPERTY_PREFIXES = listOf("- ", "* ", "+ ")

private class LinesToPropsTransformer(
    private val autoUuidPropContext: AutoUuidPropContext?,
    private val propertyPrefixes: List<String> = DEFAULT_PROPERTY_PREFIXES,
) {
    fun transform(
        lines: List<String>,
        localUuidContext: String,
        isMarked: Boolean,
    ): List<GraphProperty> =
        lines.mapNotNull { createGProperty(it) }.let {
            if (!isMarked && (autoUuidPropContext != null) && !it.map { p -> p.name }.contains(autoUuidPropContext.propName)) {
                val uuidContext = autoUuidPropContext.globalUuidContext + localUuidContext
                val uuid = UUID.nameUUIDFromBytes(uuidContext.toByteArray())
                it + GraphProperty(autoUuidPropContext.propName, "\"$uuid\"")
            } else {
                it
            }
        }

    private fun createGProperty(line: String): GraphProperty? {
        val propText = propertyPrefixes.find { line.startsWith(it) } ?: return null
        val propParts = line.substring(propText.length).trim().split(Regex(":\\s*"), 2)
        return if (propParts.size <= 1) null else GraphProperty(propParts[0], propParts[1])
    }
}
