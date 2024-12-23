package com.cypherdraw.core

import org.w3c.dom.Document
import org.w3c.dom.Node
import org.w3c.dom.NodeList
import org.xml.sax.InputSource
import java.io.InputStream
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

@JvmInline
value class DiagramVertexId(
    val string: String,
)

@JvmInline
value class DiagramEdgeId(
    val string: String,
)

data class DiagramData(
    val vertexes: List<DiagramVertex>,
    val edges: List<DiagramEdge>,
)

data class DiagramVertex(
    val id: DiagramVertexId,
    val text: String,
    val style: String,
)

data class DiagramEdge(
    val id: DiagramEdgeId,
    val sourceId: DiagramVertexId,
    val targetId: DiagramVertexId,
    val text: String,
)

class DiagramParser {
    fun parse(inputStream: InputStream): DiagramData {
        val xPathFactory = XPathFactory.newInstance()
        val document = parseDocument(inputStream)

        val xmlNodeParser = XmlNodeParser(xPathFactory, document)
        val vertexes = xmlNodeParser.parseByAttribute("vertex", "1", ::parseDiagramVertex)
        val edges = xmlNodeParser.parseByAttribute("edge", "1", ::parseDiagramEdge)
        return DiagramData(vertexes, edges)
    }

    private fun parseDocument(inputStream: InputStream): Document {
        val dbFactory = DocumentBuilderFactory.newInstance()
        val docBuilder = dbFactory.newDocumentBuilder()
        val inputSource = InputSource(inputStream)
        return docBuilder.parse(inputSource)
    }

    private fun parseDiagramVertex(node: Node): DiagramVertex? {
        val attrId = node.getAttrValue("id") ?: return null
        val attrValue = node.getAttrValue("value") ?: ""
        val attrStyle = node.getAttrValue("style") ?: ""
        return DiagramVertex(DiagramVertexId(attrId), attrValue, attrStyle)
    }

    private fun parseDiagramEdge(node: Node): DiagramEdge? {
        val attrId = node.getAttrValue("id") ?: return null
        val attrSource = node.getAttrValue("source") ?: return null
        val attrTarget = node.getAttrValue("target") ?: return null
        val attrValue = node.getAttrValue("value") ?: ""
        return DiagramEdge(DiagramEdgeId(attrId), DiagramVertexId(attrSource), DiagramVertexId(attrTarget), attrValue)
    }

    private fun Node.getAttrValue(attr: String) = this.attributes.getNamedItem(attr)?.nodeValue
}

private class XmlNodeParser(
    private val xPathFactory: XPathFactory,
    private val document: Document,
) {
    fun <T> parseByAttribute(
        attributeName: String,
        attributeValue: String,
        mapper: (node: Node) -> T?,
    ): List<T> {
        val xmlNodes = extractXmlNodesByAttribute(attributeName, attributeValue)
        return xmlNodes.mapNotNull { mapper(it) }
    }

    private fun extractXmlNodesByAttribute(
        attributeName: String,
        attributeValue: String,
    ): List<Node> {
        val xPath = xPathFactory.newXPath()
        val xPathExpression = "//mxGraphModel/root/mxCell[contains(@$attributeName, '$attributeValue')]"
        val xmlNodesList = xPath.evaluate(xPathExpression, document, XPathConstants.NODESET) as NodeList

        val xmlNodes = mutableListOf<Node>()
        for (i in 0 until xmlNodesList.length) {
            xmlNodes.add(xmlNodesList.item(i))
        }
        return xmlNodes
    }
}
