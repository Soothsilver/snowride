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
import java.util.Optional;


/**
 * <p>Java class for Keyword complex type.
 *
 * <p>The following schema fragment specifies the expected content contained within this class.
 *
 * <pre>
 * &lt;complexType name="Keyword">
 *   &lt;complexContent>
 *     &lt;restriction base="{http://www.w3.org/2001/XMLSchema}anyType">
 *       &lt;choice maxOccurs="unbounded">
 *         &lt;element name="kw" type="{}Keyword" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="for" type="{}For" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="if" type="{}If" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="return" type="{}Return" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="msg" type="{}Message" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="var" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="arg" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="doc" type="{http://www.w3.org/2001/XMLSchema}string" minOccurs="0"/>
 *         &lt;element name="tag" type="{http://www.w3.org/2001/XMLSchema}string" maxOccurs="unbounded" minOccurs="0"/>
 *         &lt;element name="timeout" type="{}Timeout" minOccurs="0"/>
 *         &lt;element name="status" type="{}BodyItemStatus"/>
 *       &lt;/choice>
 *       &lt;attribute name="name" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="library" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="sourcename" type="{http://www.w3.org/2001/XMLSchema}string" />
 *       &lt;attribute name="type" type="{}KeywordType" />
 *     &lt;/restriction>
 *   &lt;/complexContent>
 * &lt;/complexType>
 * </pre>
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Keyword")
public class Keyword extends OutputElement {

    @XmlElements({
            @XmlElement(name = "kw", type = Keyword.class, required = false),
            @XmlElement(name = "for", type = For.class, required = false),
            @XmlElement(name = "if", type = If.class, required = false)
    })
    protected List<OutputElement> kwOrForOrIf = new LinkedList<>();

    @XmlElement(name = "arg", type = String.class)
    protected List<String> args = new LinkedList<>();
    @XmlElement(name = "var", type = String.class)
    protected List<String> vars = new LinkedList<>();
    @XmlElement(name = "msg", type = Message.class)
    protected List<Message> msg = new LinkedList<>();
    @XmlElement(name = "tag", type = String.class)
    protected List<String> tags = new LinkedList<>();
    @XmlElement(name = "doc", type = String.class)
    protected String doc;
    @XmlElement(name = "status", type = BodyItemStatus.class)
    protected BodyItemStatus status;
    @XmlElement(name = "timeout", type = Timeout.class)
    protected Timeout timeout;
    @XmlElement(name = "return", type = Return.class)
    protected Return returnn;

    @XmlAttribute(name = "library")
    protected String library;
    @XmlAttribute(name = "sourcename")
    protected String sourcename;
    @XmlAttribute(name = "type")
    protected KeywordType type;

    // RobotFramework 3.X
    @XmlElementWrapper(name="arguments")
    @XmlElement(name = "arg", type = String.class)
    protected List<String> rf3Args = new LinkedList<>();
    @XmlElementWrapper(name="assign")
    @XmlElement(name = "var", type = String.class)
    protected List<String> rf3vars = new LinkedList<>();

    @Override
    public List<OutputElement> getElements() {
        return kwOrForOrIf;
    }

    public List<OutputElement> getKwOrForOrIf() {
        return kwOrForOrIf;
    }

    public Keyword getFailingKeyword() {
        return findFailingKeywordIn(kwOrForOrIf);
    }

    public List<String> getArguments() {
        if (!rf3Args.isEmpty()) {
            return rf3Args;
        }
        return args;
    }

    public List<String> getVariables() {
        if (!rf3vars.isEmpty()) {
            return rf3vars;
        }
        return vars;
    }

    public List<Message> getMessages() {
        return msg;
    }

    public List<String> getTags() {
        return tags;
    }

    public String getDoc() {
        return doc;
    }

    public BodyItemStatus getStatus() {
        return status;
    }

    public Timeout getTimeout() {
        return timeout;
    }

    public Return getReturnn() {
        return returnn;
    }

    public String getLibrary() {
        return library;
    }

    public void setLibrary(String value) {
        this.library = value;
    }

    public String getSourcename() {
        return sourcename;
    }

    public void setSourcename(String value) {
        this.sourcename = value;
    }

    public KeywordType getType() {
        return type;
    }

    public boolean isSetupOrTearDown() {
        return type != null && type != KeywordType.FOR && type != KeywordType.FORITEM;
    }

    public void setType(KeywordType value) {
        this.type = value;
    }

    private Keyword findFailingKeywordIn(List<OutputElement> kwOrForOrIf) {
        for (OutputElement element : kwOrForOrIf) {
            if (element instanceof Keyword) {
                Keyword keyword = (Keyword) element;
                if (keyword.getStatus().getStatus() == BodyItemStatusValue.FAIL) {
                    if (!keyword.getMessages().isEmpty()) {
                        return keyword;
                    } else if (!keyword.kwOrForOrIf.isEmpty()) {
                        return findFailingKeywordIn(keyword.kwOrForOrIf);
                    }
                }
            } else if (element instanceof For) {
                For loop = (For) element;
                Optional<ForIteration> failingIteration = loop.getElements().stream()
                        .filter(elem -> elem instanceof ForIteration)
                        .map(sc -> (ForIteration) sc)
                        .filter(elem -> elem.getStatus().getStatus() == BodyItemStatusValue.FAIL)
                        .findFirst();
                if (failingIteration.isPresent()) {
                    return findFailingKeywordIn(failingIteration.get().kwOrForOrIf);
                }
            }
            // TODO: support for failures inside of IF
        }
        return null;
    }

}
