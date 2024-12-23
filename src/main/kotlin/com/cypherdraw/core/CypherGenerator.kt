package com.cypherdraw.core

@JvmInline
value class GraphNodeId(
    private val string: String,
) {
    fun toCypher() = "`$string`"
}

@JvmInline
value class GraphLabel(
    private val string: String,
) {
    fun toCypher() = ":$string"
}

data class Graph(
    val nodes: List<GraphNode> = emptyList(),
    val rels: List<GraphRel> = emptyList(),
) {
    fun toCypher() =
        listOf(
            nodesToCypher(nodes),
            relsToCypher(rels),
        ).joinNonEmpty(",\n")

    private fun nodesToCypher(nodes: List<GraphNode>) = nodes.joinToString(separator = ",\n") { it.toCypher() }

    private fun relsToCypher(rels: List<GraphRel>) = rels.joinToString(separator = ",\n") { it.toCypher() }
}

data class GraphNode(
    val id: GraphNodeId,
    val labels: List<GraphLabel> = emptyList(),
    val props: List<GraphProperty> = emptyList(),
    val marked: Boolean = false,
) {
    fun toCypher(): String {
        val nodeDetails =
            listOf(
                id.toCypher() + labels.joinToString(separator = "") { it.toCypher() },
                graphPropsToCypher(props),
            ).joinNonEmpty(" ")
        return "($nodeDetails)"
    }
}

data class GraphRel(
    val type: GraphLabel,
    val sourceId: GraphNodeId,
    val targetId: GraphNodeId,
    val props: List<GraphProperty> = emptyList(),
    val marked: Boolean = false,
) {
    fun toCypher(): String {
        val relDetails =
            listOf(
                type.toCypher(),
                graphPropsToCypher(props),
            ).joinNonEmpty(" ")
        return "(${sourceId.toCypher()})-[$relDetails]->(${targetId.toCypher()})"
    }
}

data class GraphProperty(
    val name: String,
    val value: String,
) {
    fun toCypher() = "$name: $value"
}

private fun graphPropsToCypher(props: List<GraphProperty>) =
    if (props.isEmpty()) "" else props.joinToString(separator = ", ", prefix = "{", postfix = "}") { it.toCypher() }

private fun List<String>.joinNonEmpty(separator: String) = filter { it.isNotEmpty() }.joinToString(separator = separator)

class CypherGenerator {
    fun generateMatchStatement(graph: Graph): String {
        val matchWithoutReturn = genMatchWithoutReturn(graph)
        if (matchWithoutReturn.isEmpty()) {
            return ""
        }
        return "${matchWithoutReturn}RETURN *\n"
    }

    fun generateCreateStatement(graph: Graph): String {
        val (markedNodes, nonMarkedNodes) = graph.nodes.partition { it.marked }
        val (matchGraph, createGraph) =
            if (markedNodes.isNotEmpty()) {
                val (markedRels, nonMarkedRels) = graph.rels.partition { it.marked }
                val markedNodesIds = markedNodes.map { it.id }
                val relsToMatch = markedRels.filter { (it.sourceId in markedNodesIds) && (it.targetId in markedNodesIds) }
                Pair(Graph(markedNodes, relsToMatch), Graph(nonMarkedNodes, nonMarkedRels))
            } else {
                val relsToCreate = graph.rels.filter { !it.marked }
                Pair(Graph(), Graph(graph.nodes, relsToCreate))
            }

        if (createGraph.nodes.isEmpty() && createGraph.rels.isEmpty()) {
            return generateMatchStatement(matchGraph)
        }

        val matchWithoutReturn = genMatchWithoutReturn(matchGraph)
        val statementParts = createGraph.toCypher()
        return "${matchWithoutReturn}CREATE\n${statementParts}\nRETURN *\n"
    }

    private fun genMatchWithoutReturn(graph: Graph): String {
        if (graph.nodes.isEmpty()) {
            return ""
        }

        val statementParts = graph.toCypher()
        return "MATCH\n${statementParts}\n"
    }
}
