package example

import org.w3c.dom.HTMLTemplateElement
import widget.Widget

private fun template(templateHtml: String): HTMLTemplateElement {
    val template = Widget(templateHtml).container.firstElementChild
    checkNotNull(template)
    if (template !is HTMLTemplateElement)
        error("The root element must be a <template>, instead it is: {$templateHtml}")
    return template
}

open class Template(templateHtml: String) : Widget(template(templateHtml))