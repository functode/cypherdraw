package com.cypherdraw.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

internal class TestDiagramParser {
    @Test
    fun testGenerateMatchCypher() {
        val diagramInputStream = this::class.java.getResource("test-diagram-basic.drawio")?.openStream() ?: fail()
        val expectedDiagram =
            DiagramData(
                listOf(
                    DiagramVertex(
                        DiagramVertexId("FGtR0-8Av16mA7pavE6J-1"),
                        "<b><font style=\"font-size: 14px;\">A</font></b>",
                        "ellipse;whiteSpace=wrap;html=1;aspect=fixed;",
                    ),
                    DiagramVertex(
                        DiagramVertexId("FGtR0-8Av16mA7pavE6J-10"),
                        "<span style=\"font-size: 14px;\"><b>B, C</b></span><br><div>- a: 2</div><div>- b: \"test\"</div>",
                        "ellipse;whiteSpace=wrap;html=1;aspect=fixed;",
                    ),
                    DiagramVertex(
                        DiagramVertexId("FGtR0-8Av16mA7pavE6J-13"),
                        "<b><font style=\"font-size: 14px;\">D</font></b><div>* c: true</div>",
                        "rounded=0;whiteSpace=wrap;html=1;",
                    ),
                    DiagramVertex(
                        DiagramVertexId("FGtR0-8Av16mA7pavE6J-2"),
                        "",
                        "ellipse;whiteSpace=wrap;html=1;aspect=fixed;",
                    ),
                    DiagramVertex(
                        DiagramVertexId("1-VsqYT4SuquwrnNaygr-1"),
                        "<b><font style=\"font-size: 14px;\">A</font></b>",
                        "ellipse;whiteSpace=wrap;html=1;aspect=fixed;",
                    ),
                ),
                listOf(
                    DiagramEdge(
                        DiagramEdgeId("FGtR0-8Av16mA7pavE6J-3"),
                        DiagramVertexId("FGtR0-8Av16mA7pavE6J-1"),
                        DiagramVertexId("FGtR0-8Av16mA7pavE6J-2"),
                        "<b><font style=\"font-size: 13px;\">R</font></b>",
                    ),
                    DiagramEdge(
                        DiagramEdgeId("FGtR0-8Av16mA7pavE6J-12"),
                        DiagramVertexId("FGtR0-8Av16mA7pavE6J-1"),
                        DiagramVertexId("FGtR0-8Av16mA7pavE6J-10"),
                        "<b><font style=\"font-size: 13px;\">T</font></b><div>- a: 2</div><div>- b: \"test\"</div>",
                    ),
                    DiagramEdge(
                        DiagramEdgeId("FGtR0-8Av16mA7pavE6J-14"),
                        DiagramVertexId("FGtR0-8Av16mA7pavE6J-1"),
                        DiagramVertexId("FGtR0-8Av16mA7pavE6J-13"),
                        "",
                    ),
                ),
            )

        val parsedDiagram = DiagramParser().parse(diagramInputStream)
        assertEquals(expectedDiagram.vertexes.sortedBy { v -> v.id.string }, parsedDiagram.vertexes.sortedBy { v -> v.id.string })
        assertEquals(expectedDiagram.edges.sortedBy { v -> v.id.string }, parsedDiagram.edges.sortedBy { v -> v.id.string })
    }
}
