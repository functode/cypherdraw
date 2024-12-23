package com.cypherdraw.core

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TestCypherGenerator {
    @TestFactory
    fun testGenerateGraphNode() =
        listOf(
            GraphNode(GraphNodeId("x"), listOf(GraphLabel("A")))
                to "(`x`:A)",
            GraphNode(GraphNodeId("x"), listOf(GraphLabel("A"), GraphLabel("B")))
                to "(`x`:A:B)",
            GraphNode(GraphNodeId("x"), listOf(GraphLabel("A")), listOf(GraphProperty("a", "1")))
                to "(`x`:A {a: 1})",
            GraphNode(GraphNodeId("x"), listOf(GraphLabel("A")), listOf(GraphProperty("a", "1"), GraphProperty("b", "2")))
                to "(`x`:A {a: 1, b: 2})",
        ).map { (input, expected) ->
            DynamicTest.dynamicTest(expected) {
                assertEquals(expected, input.toCypher())
            }
        }

    @TestFactory
    fun testGenerateGraphRel() =
        listOf(
            GraphRel(GraphLabel("R"), GraphNodeId("x1"), GraphNodeId("x2"))
                to "(`x1`)-[:R]->(`x2`)",
            GraphRel(GraphLabel("R"), GraphNodeId("x1"), GraphNodeId("x2"), listOf(GraphProperty("a", "1")))
                to "(`x1`)-[:R {a: 1}]->(`x2`)",
            GraphRel(GraphLabel("R"), GraphNodeId("x1"), GraphNodeId("x2"), listOf(GraphProperty("a", "1"), GraphProperty("b", "2")))
                to "(`x1`)-[:R {a: 1, b: 2}]->(`x2`)",
        ).map { (input, expected) ->
            DynamicTest.dynamicTest(expected) {
                assertEquals(expected, input.toCypher())
            }
        }

    @Test
    fun testGenerateMatchCypher() {
        val expected = this::class.java.getResource("test-match.cypher")?.readText()
        val generated = CypherGenerator().generateMatchStatement(getSimpleTestGraph())
        assertEquals(expected, generated)
    }

    @Test
    fun testGenerateCreateCypher() {
        val expected = this::class.java.getResource("test-create.cypher")?.readText()
        val generated = CypherGenerator().generateCreateStatement(getSimpleTestGraph())
        assertEquals(expected, generated)
    }

    private fun getSimpleTestGraph() =
        Graph(
            listOf(
                GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A"))),
                GraphNode(GraphNodeId("x2"), listOf(GraphLabel("B")), listOf(GraphProperty("a", "1"))),
            ),
            listOf(
                GraphRel(GraphLabel("R"), GraphNodeId("x1"), GraphNodeId("x2"), listOf(GraphProperty("a", "1"))),
            ),
        )

    @TestFactory
    fun testGenerateMatchCreateCypher() =
        listOf(
            Pair(
                "empty",
                Graph(
                    listOf(),
                    listOf(),
                ),
            ),
            Pair(
                "one-node",
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A"))),
                    ),
                    listOf(),
                ),
            ),
            Pair(
                "one-marked-node",
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A")), marked = true),
                    ),
                    listOf(),
                ),
            ),
            Pair(
                "graph-one-marked-node",
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A")), marked = true),
                        GraphNode(GraphNodeId("x2"), listOf(GraphLabel("B"))),
                    ),
                    listOf(
                        GraphRel(GraphLabel("R"), GraphNodeId("x1"), GraphNodeId("x2")),
                    ),
                ),
            ),
            Pair(
                "graph-one-marked-rel",
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A"))),
                        GraphNode(GraphNodeId("x2"), listOf(GraphLabel("B"))),
                    ),
                    listOf(
                        GraphRel(GraphLabel("R"), GraphNodeId("x1"), GraphNodeId("x2"), marked = true),
                    ),
                ),
            ),
            Pair(
                "graph-one-marked-node-and-rel",
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A")), marked = true),
                        GraphNode(GraphNodeId("x2"), listOf(GraphLabel("B"))),
                    ),
                    listOf(
                        GraphRel(GraphLabel("R"), GraphNodeId("x1"), GraphNodeId("x2"), marked = true),
                    ),
                ),
            ),
            Pair(
                "graph-two-marked-nodes-and-rel",
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A")), marked = true),
                        GraphNode(GraphNodeId("x2"), listOf(GraphLabel("B")), marked = true),
                    ),
                    listOf(
                        GraphRel(GraphLabel("R"), GraphNodeId("x1"), GraphNodeId("x2"), marked = true),
                    ),
                ),
            ),
            Pair(
                "graph",
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A")), marked = true),
                        GraphNode(GraphNodeId("x2"), listOf(GraphLabel("B")), marked = true),
                        GraphNode(GraphNodeId("x3"), listOf(GraphLabel("C"))),
                    ),
                    listOf(
                        GraphRel(GraphLabel("R"), GraphNodeId("x1"), GraphNodeId("x2"), marked = true),
                        GraphRel(GraphLabel("S"), GraphNodeId("x2"), GraphNodeId("x3")),
                    ),
                ),
            ),
        ).map { (name, input) ->
            DynamicTest.dynamicTest(name) {
                val expected = this::class.java.getResource("test-match-create-$name.cypher")?.readText()
                val generated = CypherGenerator().generateCreateStatement(input)
                assertEquals(expected, generated)
            }
        }
}
