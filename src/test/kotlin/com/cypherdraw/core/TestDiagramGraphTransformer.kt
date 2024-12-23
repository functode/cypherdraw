package com.cypherdraw.core

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.Test
import kotlin.test.assertEquals

internal class TestDiagramGraphTransformer {
    @TestFactory
    fun testTransformStandard() =
        listOf(
            Triple(
                "node without labels",
                DiagramData(
                    listOf(
                        DiagramVertex(DiagramVertexId("x1"), "", ""),
                    ),
                    listOf(),
                ),
                Graph(
                    listOf(),
                    listOf(),
                ),
            ),
            Triple(
                "nodes with labels",
                DiagramData(
                    listOf(
                        DiagramVertex(DiagramVertexId("x1"), "A", ""),
                        DiagramVertex(DiagramVertexId("x2"), "A,B", ""),
                        DiagramVertex(DiagramVertexId("x3"), "A, B, C", ""),
                    ),
                    listOf(),
                ),
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A"))),
                        GraphNode(GraphNodeId("x2"), listOf(GraphLabel("A"), GraphLabel("B"))),
                        GraphNode(GraphNodeId("x3"), listOf(GraphLabel("A"), GraphLabel("B"), GraphLabel("C"))),
                    ),
                    listOf(),
                ),
            ),
            Triple(
                "nodes with props",
                DiagramData(
                    listOf(
                        DiagramVertex(DiagramVertexId("x1"), "A<br>- a: 1", ""),
                        DiagramVertex(DiagramVertexId("x2"), "A<br>+ a: 1<br>* b: 2", ""),
                    ),
                    listOf(),
                ),
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A")), listOf(GraphProperty("a", "1"))),
                        GraphNode(GraphNodeId("x2"), listOf(GraphLabel("A")), listOf(GraphProperty("a", "1"), GraphProperty("b", "2"))),
                    ),
                    listOf(),
                ),
            ),
            Triple(
                "rels without nodes",
                DiagramData(
                    listOf(
                        DiagramVertex(DiagramVertexId("x1"), "A", ""),
                    ),
                    listOf(
                        DiagramEdge(DiagramEdgeId("y1"), DiagramVertexId("x1"), DiagramVertexId("x2"), "R"),
                        DiagramEdge(DiagramEdgeId("y1"), DiagramVertexId("x2"), DiagramVertexId("x3"), "R"),
                    ),
                ),
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A"))),
                    ),
                    listOf(),
                ),
            ),
            Triple(
                "rel without type",
                DiagramData(
                    listOf(
                        DiagramVertex(DiagramVertexId("x1"), "A", ""),
                        DiagramVertex(DiagramVertexId("x2"), "A", ""),
                    ),
                    listOf(
                        DiagramEdge(DiagramEdgeId("y1"), DiagramVertexId("x1"), DiagramVertexId("x2"), ""),
                    ),
                ),
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A"))),
                        GraphNode(GraphNodeId("x2"), listOf(GraphLabel("A"))),
                    ),
                    listOf(),
                ),
            ),
            Triple(
                "rels with nodes",
                DiagramData(
                    listOf(
                        DiagramVertex(DiagramVertexId("x1"), "A", ""),
                        DiagramVertex(DiagramVertexId("x2"), "A", ""),
                        DiagramVertex(DiagramVertexId("x3"), "A", ""),
                    ),
                    listOf(
                        DiagramEdge(DiagramEdgeId("y1"), DiagramVertexId("x1"), DiagramVertexId("x2"), "R"),
                        DiagramEdge(DiagramEdgeId("y2"), DiagramVertexId("x1"), DiagramVertexId("x2"), "S"),
                        DiagramEdge(DiagramEdgeId("y3"), DiagramVertexId("x2"), DiagramVertexId("x3"), "R"),
                        DiagramEdge(DiagramEdgeId("y4"), DiagramVertexId("x3"), DiagramVertexId("x3"), "S"),
                    ),
                ),
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A"))),
                        GraphNode(GraphNodeId("x2"), listOf(GraphLabel("A"))),
                        GraphNode(GraphNodeId("x3"), listOf(GraphLabel("A"))),
                    ),
                    listOf(
                        GraphRel(GraphLabel("R"), GraphNodeId("x1"), GraphNodeId("x2")),
                        GraphRel(GraphLabel("S"), GraphNodeId("x1"), GraphNodeId("x2")),
                        GraphRel(GraphLabel("R"), GraphNodeId("x2"), GraphNodeId("x3")),
                        GraphRel(GraphLabel("S"), GraphNodeId("x3"), GraphNodeId("x3")),
                    ),
                ),
            ),
            Triple(
                "rels with props",
                DiagramData(
                    listOf(
                        DiagramVertex(DiagramVertexId("x1"), "A", ""),
                        DiagramVertex(DiagramVertexId("x2"), "A", ""),
                    ),
                    listOf(
                        DiagramEdge(DiagramEdgeId("y1"), DiagramVertexId("x1"), DiagramVertexId("x2"), "R<br>- a: 1"),
                        DiagramEdge(DiagramEdgeId("y2"), DiagramVertexId("x1"), DiagramVertexId("x2"), "R<br>+ a: 1<br>* b: 2"),
                    ),
                ),
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A"))),
                        GraphNode(GraphNodeId("x2"), listOf(GraphLabel("A"))),
                    ),
                    listOf(
                        GraphRel(GraphLabel("R"), GraphNodeId("x1"), GraphNodeId("x2"), listOf(GraphProperty("a", "1"))),
                        GraphRel(
                            GraphLabel("R"),
                            GraphNodeId("x1"),
                            GraphNodeId("x2"),
                            listOf(GraphProperty("a", "1"), GraphProperty("b", "2")),
                        ),
                    ),
                ),
            ),
        ).flatMap { (name, input, expected) ->
            listOf(
                DynamicTest.dynamicTest(name) {
                    val graph = DiagramGraphTransformer().transform(input)
                    assertEquals(expected, graph)
                },
                DynamicTest.dynamicTest("$name (with prefix ignore)") {
                    val graph = DiagramGraphTransformer(prefixIgnore = "!!").transform(input)
                    assertEquals(expected, graph)
                },
                DynamicTest.dynamicTest("$name (with prefix mark)") {
                    val graph = DiagramGraphTransformer(prefixMark = "@@").transform(input)
                    assertEquals(expected, graph)
                },
            )
        }

    @TestFactory
    fun testTransformWithPrefixIgnore() =
        listOf(
            Triple(
                "nodes",
                DiagramData(
                    listOf(
                        DiagramVertex(DiagramVertexId("x1"), "A", ""),
                        DiagramVertex(DiagramVertexId("x2"), "!!A", ""),
                    ),
                    listOf(),
                ),
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A"))),
                    ),
                    listOf(),
                ),
            ),
            Triple(
                "rels",
                DiagramData(
                    listOf(
                        DiagramVertex(DiagramVertexId("x1"), "A", ""),
                        DiagramVertex(DiagramVertexId("x2"), "A", ""),
                    ),
                    listOf(
                        DiagramEdge(DiagramEdgeId("y1"), DiagramVertexId("x1"), DiagramVertexId("x2"), "R"),
                        DiagramEdge(DiagramEdgeId("y1"), DiagramVertexId("x1"), DiagramVertexId("x2"), "!!S"),
                    ),
                ),
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A"))),
                        GraphNode(GraphNodeId("x2"), listOf(GraphLabel("A"))),
                    ),
                    listOf(
                        GraphRel(GraphLabel("R"), GraphNodeId("x1"), GraphNodeId("x2")),
                    ),
                ),
            ),
        ).map { (name, input, expected) ->
            DynamicTest.dynamicTest(name) {
                val graph = DiagramGraphTransformer(prefixIgnore = "!!").transform(input)
                assertEquals(expected, graph)
            }
        }

    @TestFactory
    fun testTransformWithPrefixMark() =
        listOf(
            Triple(
                "nodes",
                DiagramData(
                    listOf(
                        DiagramVertex(DiagramVertexId("x1"), "A", ""),
                        DiagramVertex(DiagramVertexId("x2"), "!!A", ""),
                    ),
                    listOf(),
                ),
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A"))),
                        GraphNode(GraphNodeId("x2"), listOf(GraphLabel("A")), marked = true),
                    ),
                    listOf(),
                ),
            ),
            Triple(
                "rels",
                DiagramData(
                    listOf(
                        DiagramVertex(DiagramVertexId("x1"), "A", ""),
                        DiagramVertex(DiagramVertexId("x2"), "A", ""),
                    ),
                    listOf(
                        DiagramEdge(DiagramEdgeId("y1"), DiagramVertexId("x1"), DiagramVertexId("x2"), "R"),
                        DiagramEdge(DiagramEdgeId("y1"), DiagramVertexId("x1"), DiagramVertexId("x2"), "!!S"),
                    ),
                ),
                Graph(
                    listOf(
                        GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A"))),
                        GraphNode(GraphNodeId("x2"), listOf(GraphLabel("A"))),
                    ),
                    listOf(
                        GraphRel(GraphLabel("R"), GraphNodeId("x1"), GraphNodeId("x2")),
                        GraphRel(GraphLabel("S"), GraphNodeId("x1"), GraphNodeId("x2"), marked = true),
                    ),
                ),
            ),
        ).map { (name, input, expected) ->
            DynamicTest.dynamicTest(name) {
                val graph = DiagramGraphTransformer(prefixMark = "!!").transform(input)
                assertEquals(expected, graph)
            }
        }

    @Test
    fun testTransformWithAllowOnlyCircleNodes() {
        val input =
            DiagramData(
                listOf(
                    DiagramVertex(DiagramVertexId("x1"), "A", "ellipse;etc"),
                    DiagramVertex(DiagramVertexId("x2"), "A", ""),
                ),
                listOf(),
            )
        val expectedWithoutCirclesOnly =
            Graph(
                listOf(
                    GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A"))),
                    GraphNode(GraphNodeId("x2"), listOf(GraphLabel("A"))),
                ),
                listOf(),
            )
        val expectedWithCirclesOnly =
            Graph(
                listOf(
                    GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A"))),
                ),
                listOf(),
            )

        val graphWithoutCirclesOnly = DiagramGraphTransformer(allowOnlyCircleNodes = false).transform(input)
        assertEquals(expectedWithoutCirclesOnly, graphWithoutCirclesOnly)
        val graphWithCirclesOnly = DiagramGraphTransformer(allowOnlyCircleNodes = true).transform(input)
        assertEquals(expectedWithCirclesOnly, graphWithCirclesOnly)
    }

    @Test
    fun testTransformWithAutoLabel() {
        val input =
            DiagramData(
                listOf(
                    DiagramVertex(DiagramVertexId("x1"), "A", ""),
                    DiagramVertex(DiagramVertexId("x2"), "A, B", ""),
                ),
                listOf(),
            )
        val expected =
            Graph(
                listOf(
                    GraphNode(GraphNodeId("x1"), listOf(GraphLabel("A"), GraphLabel("C"))),
                    GraphNode(GraphNodeId("x2"), listOf(GraphLabel("A"), GraphLabel("B"), GraphLabel("C"))),
                ),
                listOf(),
            )

        val graph = DiagramGraphTransformer(autoLabel = "C").transform(input)
        assertEquals(expected, graph)
    }

    @Test
    fun testTransformWithAutoUuidPropContext() {
        val input =
            DiagramData(
                listOf(
                    DiagramVertex(DiagramVertexId("x1"), "A", ""),
                    DiagramVertex(DiagramVertexId("x2"), "A<br>- a: 1", ""),
                    DiagramVertex(DiagramVertexId("x3"), "A<br>- uuid: 1", ""),
                ),
                listOf(
                    DiagramEdge(DiagramEdgeId("y1"), DiagramVertexId("x1"), DiagramVertexId("x2"), "R"),
                    DiagramEdge(DiagramEdgeId("y2"), DiagramVertexId("x1"), DiagramVertexId("x1"), "S<br>- a: 1"),
                    DiagramEdge(DiagramEdgeId("y3"), DiagramVertexId("x2"), DiagramVertexId("x2"), "R<br>- uuid: 1"),
                ),
            )
        val expectedContext1 =
            Graph(
                listOf(
                    GraphNode(
                        GraphNodeId("x1"),
                        listOf(GraphLabel("A")),
                        listOf(GraphProperty("uuid", "\"bcaf9b59-00af-350b-8e46-5a7de72aa557\"")),
                    ),
                    GraphNode(
                        GraphNodeId("x2"),
                        listOf(GraphLabel("A")),
                        listOf(GraphProperty("a", "1"), GraphProperty("uuid", "\"7ca50ae1-483b-3472-aabd-42949af2026d\"")),
                    ),
                    GraphNode(GraphNodeId("x3"), listOf(GraphLabel("A")), listOf(GraphProperty("uuid", "1"))),
                ),
                listOf(
                    GraphRel(
                        GraphLabel("R"),
                        GraphNodeId("x1"),
                        GraphNodeId("x2"),
                        listOf(GraphProperty("uuid", "\"dd250e61-80e7-3e31-b183-93e07d0b6711\"")),
                    ),
                    GraphRel(
                        GraphLabel("S"),
                        GraphNodeId("x1"),
                        GraphNodeId("x1"),
                        listOf(GraphProperty("a", "1"), GraphProperty("uuid", "\"19e6aeb6-a890-3728-932b-2b1c4a94fec6\"")),
                    ),
                    GraphRel(GraphLabel("R"), GraphNodeId("x2"), GraphNodeId("x2"), listOf(GraphProperty("uuid", "1"))),
                ),
            )
        val expectedContext2 =
            Graph(
                listOf(
                    GraphNode(
                        GraphNodeId("x1"),
                        listOf(GraphLabel("A")),
                        listOf(GraphProperty("uuid", "\"0d0bd32f-9347-32ea-807d-5766f5942ed9\"")),
                    ),
                    GraphNode(
                        GraphNodeId("x2"),
                        listOf(GraphLabel("A")),
                        listOf(GraphProperty("a", "1"), GraphProperty("uuid", "\"edccbba3-29cc-390c-8ff7-7ab0abb6059d\"")),
                    ),
                    GraphNode(GraphNodeId("x3"), listOf(GraphLabel("A")), listOf(GraphProperty("uuid", "1"))),
                ),
                listOf(
                    GraphRel(
                        GraphLabel("R"),
                        GraphNodeId("x1"),
                        GraphNodeId("x2"),
                        listOf(GraphProperty("uuid", "\"c72f2866-57bf-3883-bd6f-0b87b5915a9c\"")),
                    ),
                    GraphRel(
                        GraphLabel("S"),
                        GraphNodeId("x1"),
                        GraphNodeId("x1"),
                        listOf(GraphProperty("a", "1"), GraphProperty("uuid", "\"6563c491-97f4-3d29-b3bf-71478c5f5671\"")),
                    ),
                    GraphRel(GraphLabel("R"), GraphNodeId("x2"), GraphNodeId("x2"), listOf(GraphProperty("uuid", "1"))),
                ),
            )

        val graphContext1Run1 = DiagramGraphTransformer(autoUuidPropContext = AutoUuidPropContext("uuid", "1")).transform(input)
        assertEquals(expectedContext1, graphContext1Run1)
        val graphContext1Run2 = DiagramGraphTransformer(autoUuidPropContext = AutoUuidPropContext("uuid", "1")).transform(input)
        assertEquals(expectedContext1, graphContext1Run2)
        val graphContext2 = DiagramGraphTransformer(autoUuidPropContext = AutoUuidPropContext("uuid", "2")).transform(input)
        assertEquals(expectedContext2, graphContext2)
    }
}
