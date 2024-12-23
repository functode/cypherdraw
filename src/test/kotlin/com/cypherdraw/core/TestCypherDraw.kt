package com.cypherdraw.core

import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.TestFactory
import kotlin.test.assertEquals
import kotlin.test.fail

internal class TestCypherDraw {
    @TestFactory
    fun testIntegration() =
        listOf(
            Pair(
                "test-diagram-basic",
                listOf(
                    Pair(
                        "match",
                    ) { parsedDiagram: DiagramData ->
                        val graph = DiagramGraphTransformer().transform(parsedDiagram)
                        CypherGenerator().generateMatchStatement(graph)
                    },
                    Pair(
                        "create",
                    ) { parsedDiagram: DiagramData ->
                        val graph = DiagramGraphTransformer().transform(parsedDiagram)
                        CypherGenerator().generateCreateStatement(graph)
                    },
                    Pair(
                        "match-only-circles",
                    ) { parsedDiagram: DiagramData ->
                        val graph = DiagramGraphTransformer(allowOnlyCircleNodes = true).transform(parsedDiagram)
                        CypherGenerator().generateMatchStatement(graph)
                    },
                    Pair(
                        "create-only-circles",
                    ) { parsedDiagram: DiagramData ->
                        val graph = DiagramGraphTransformer(allowOnlyCircleNodes = true).transform(parsedDiagram)
                        CypherGenerator().generateCreateStatement(graph)
                    },
                ),
            ),
            Pair(
                "test-diagram-ignore",
                listOf(
                    Pair(
                        "match",
                    ) { parsedDiagram: DiagramData ->
                        val graph = DiagramGraphTransformer(prefixIgnore = "!!").transform(parsedDiagram)
                        CypherGenerator().generateMatchStatement(graph)
                    },
                    Pair(
                        "create",
                    ) { parsedDiagram: DiagramData ->
                        val graph = DiagramGraphTransformer(prefixIgnore = "!!").transform(parsedDiagram)
                        CypherGenerator().generateCreateStatement(graph)
                    },
                ),
            ),
            Pair(
                "test-diagram-mark",
                listOf(
                    Pair(
                        "create",
                    ) { parsedDiagram: DiagramData ->
                        val graph = DiagramGraphTransformer(prefixMark = "@@").transform(parsedDiagram)
                        CypherGenerator().generateCreateStatement(graph)
                    },
                    Pair(
                        "create-auto-label",
                    ) { parsedDiagram: DiagramData ->
                        val graph = DiagramGraphTransformer(prefixMark = "@@", autoLabel = "TT").transform(parsedDiagram)
                        CypherGenerator().generateCreateStatement(graph)
                    },
                    Pair(
                        "create-auto-uuid",
                    ) { parsedDiagram: DiagramData ->
                        val graph =
                            DiagramGraphTransformer(
                                prefixMark = "@@",
                                autoUuidPropContext = AutoUuidPropContext("uuid", "seed"),
                            ).transform(parsedDiagram)
                        CypherGenerator().generateCreateStatement(graph)
                    },
                ),
            ),
        ).flatMap { (fileName, testIteration) ->
            testIteration.map { (testName, cypherGeneratorFunc) ->
                val resultsFileName = "$fileName-$testName"
                DynamicTest.dynamicTest(resultsFileName) {
                    val diagramInputStream = this::class.java.getResource("$fileName.drawio")?.openStream() ?: fail()
                    val expected = this::class.java.getResource("$resultsFileName.cypher")?.readText()

                    val parsedDiagram = DiagramParser().parse(diagramInputStream)
                    val cypher = cypherGeneratorFunc(parsedDiagram)
                    assertEquals(expected, cypher)
                }
            }
        }
}
