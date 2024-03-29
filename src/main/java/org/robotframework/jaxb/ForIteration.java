//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.11.16 at 04:23:27 PM CET 
//


package org.robotframework.jaxb;

import jakarta.xml.bind.annotation.*;
import java.util.LinkedList;
import java.util.List;


/**
 * <p>Java class for ForIteration complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="ForIteration">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="var" type="{}ForIterationVariable" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="kw" type="{}Keyword" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="for" type="{}For" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="if" type="{}If" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="return" type="{}Return" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="msg" type="{}Message" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="doc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="status" type="{}BodyItemStatus"/>
 *       &lt;/choice>
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ForIteration")
public class ForIteration extends OutputElement {

    @XmlElements({
            @XmlElement(name = "kw", type = Keyword.class),
            @XmlElement(name = "for", type = For.class),
            @XmlElement(name = "if", type = If.class)
    })
    protected List<OutputElement> kwOrForOrIf = new LinkedList<>();

    @XmlElement(name = "var", type = ForIterationVariable.class)
    protected List<ForIterationVariable> vars = new LinkedList<>();
    @XmlElement(name = "msg", type = String.class)
    protected List<String> msg;
    @XmlElement(name = "doc", type = String.class)
    protected String doc;
    @XmlElement(name = "status", type = BodyItemStatus.class)
    protected BodyItemStatus status;
    @XmlElement(name = "return", type = Return.class)
    protected Return returnn;

    @Override
    public String getName() {
        return "ForIteration";
    }

    @Override
    public List<OutputElement> getElements() {
        return kwOrForOrIf;
    }

    public List<OutputElement> getKwOrForOrIf() {
        return kwOrForOrIf;
    }

    public List<ForIterationVariable> getVars() {
        return vars;
    }

    public List<String> getMsg() {
        return msg;
    }

    public String getDoc() {
        return doc;
    }

    public BodyItemStatus getStatus() {
        return status;
    }

    public Return getReturnn() {
        return returnn;
    }
}
