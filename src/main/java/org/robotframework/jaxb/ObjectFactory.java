//
// This file was generated by the JavaTM Architecture for XML Binding(JAXB) Reference Implementation, v2.2.8-b130911.1802 
// See <a href="http://java.sun.com/xml/jaxb">http://java.sun.com/xml/jaxb</a> 
// Any modifications to this file will be lost upon recompilation of the source schema. 
// Generated on: 2021.11.16 at 04:23:27 PM CET 
//


package org.robotframework.jaxb;

import jakarta.xml.bind.JAXBElement;
import jakarta.xml.bind.annotation.XmlElementDecl;
import jakarta.xml.bind.annotation.XmlRegistry;
import javax.xml.namespace.QName;


/**
 * This object contains factory methods for each 
 * Java content interface and Java element interface 
 * generated in the org.robotframework.jaxb package.
 * <p>An ObjectFactory allows you to programatically 
 * construct new instances of the Java representation 
 * for XML content. The Java representation of XML 
 * content can consist of schema derived interfaces 
 * and classes representing the binding of schema 
 * type definitions, element declarations and model 
 * groups.  Factory methods for each of these are 
 * provided in this class.
 * 
 */
@XmlRegistry
public class ObjectFactory {

    private final static QName _Robot_QNAME = new QName("", "robot");
    private final static QName _ForMsg_QNAME = new QName("", "msg");
    private final static QName _ForVar_QNAME = new QName("", "var");
    private final static QName _ForIter_QNAME = new QName("", "iter");
    private final static QName _ForDoc_QNAME = new QName("", "doc");
    private final static QName _ForKw_QNAME = new QName("", "kw");
    private final static QName _ForValue_QNAME = new QName("", "value");
    private final static QName _ForStatus_QNAME = new QName("", "status");
    private final static QName _KeywordArg_QNAME = new QName("", "arg");
    private final static QName _KeywordFor_QNAME = new QName("", "for");
    private final static QName _KeywordTag_QNAME = new QName("", "tag");
    private final static QName _KeywordIf_QNAME = new QName("", "if");
    private final static QName _KeywordReturn_QNAME = new QName("", "return");
    private final static QName _KeywordTimeout_QNAME = new QName("", "timeout");

    /**
     * Create a new ObjectFactory that can be used to create new instances of schema derived classes for package: org.robotframework.jaxb
     * 
     */
    public ObjectFactory() {
    }

    /**
     * Create an instance of {@link Robot }
     * 
     */
    public Robot createRobot() {
        return new Robot();
    }

    /**
     * Create an instance of {@link Message }
     * 
     */
    public Message createMessage() {
        return new Message();
    }

    /**
     * Create an instance of {@link Metadata }
     * 
     */
    public Metadata createMetadata() {
        return new Metadata();
    }

    /**
     * Create an instance of {@link TagStat }
     * 
     */
    public TagStat createTagStat() {
        return new TagStat();
    }

    /**
     * Create an instance of {@link OutputSuite }
     * 
     */
    public OutputSuite createSuite() {
        return new OutputSuite();
    }

    /**
     * Create an instance of {@link TotalStat }
     * 
     */
    public TotalStat createTotalStat() {
        return new TotalStat();
    }

    /**
     * Create an instance of {@link Test }
     * 
     */
    public Test createTest() {
        return new Test();
    }

    /**
     * Create an instance of {@link ForIterationVariable }
     * 
     */
    public ForIterationVariable createForIterationVariable() {
        return new ForIterationVariable();
    }

    /**
     * Create an instance of {@link ForIteration }
     * 
     */
    public ForIteration createForIteration() {
        return new ForIteration();
    }

    /**
     * Create an instance of {@link Status }
     * 
     */
    public Status createStatus() {
        return new Status();
    }

    /**
     * Create an instance of {@link Return }
     * 
     */
    public Return createReturn() {
        return new Return();
    }

    /**
     * Create an instance of {@link Keyword }
     * 
     */
    public Keyword createKeyword() {
        return new Keyword();
    }

    /**
     * Create an instance of {@link Timeout }
     * 
     */
    public Timeout createTimeout() {
        return new Timeout();
    }

    /**
     * Create an instance of {@link TotalStatistics }
     * 
     */
    public TotalStatistics createTotalStatistics() {
        return new TotalStatistics();
    }

    /**
     * Create an instance of {@link For }
     * 
     */
    public For createFor() {
        return new For();
    }

    /**
     * Create an instance of {@link Statistics }
     * 
     */
    public Statistics createStatistics() {
        return new Statistics();
    }

    /**
     * Create an instance of {@link Errors }
     * 
     */
    public Errors createErrors() {
        return new Errors();
    }

    /**
     * Create an instance of {@link BodyItemStatus }
     * 
     */
    public BodyItemStatus createBodyItemStatus() {
        return new BodyItemStatus();
    }

    /**
     * Create an instance of {@link IfBranch }
     * 
     */
    public IfBranch createIfBranch() {
        return new IfBranch();
    }

    /**
     * Create an instance of {@link SuiteStat }
     * 
     */
    public SuiteStat createSuiteStat() {
        return new SuiteStat();
    }

    /**
     * Create an instance of {@link If }
     * 
     */
    public If createIf() {
        return new If();
    }

    /**
     * Create an instance of {@link TagStatistics }
     * 
     */
    public TagStatistics createTagStatistics() {
        return new TagStatistics();
    }

    /**
     * Create an instance of {@link SuiteStatistics }
     * 
     */
    public SuiteStatistics createSuiteStatistics() {
        return new SuiteStatistics();
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Robot }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "robot")
    public JAXBElement<Robot> createRobot(Robot value) {
        return new JAXBElement<Robot>(_Robot_QNAME, Robot.class, null, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Message }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "msg", scope = For.class)
    public JAXBElement<Message> createForMsg(Message value) {
        return new JAXBElement<Message>(_ForMsg_QNAME, Message.class, For.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "var", scope = For.class)
    public JAXBElement<String> createForVar(String value) {
        return new JAXBElement<String>(_ForVar_QNAME, String.class, For.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link ForIteration }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "iter", scope = For.class)
    public JAXBElement<ForIteration> createForIter(ForIteration value) {
        return new JAXBElement<ForIteration>(_ForIter_QNAME, ForIteration.class, For.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "doc", scope = For.class)
    public JAXBElement<String> createForDoc(String value) {
        return new JAXBElement<String>(_ForDoc_QNAME, String.class, For.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Keyword }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "kw", scope = For.class)
    public JAXBElement<Keyword> createForKw(Keyword value) {
        return new JAXBElement<Keyword>(_ForKw_QNAME, Keyword.class, For.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "value", scope = For.class)
    public JAXBElement<String> createForValue(String value) {
        return new JAXBElement<String>(_ForValue_QNAME, String.class, For.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BodyItemStatus }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "status", scope = For.class)
    public JAXBElement<BodyItemStatus> createForStatus(BodyItemStatus value) {
        return new JAXBElement<BodyItemStatus>(_ForStatus_QNAME, BodyItemStatus.class, For.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Message }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "msg", scope = Keyword.class)
    public JAXBElement<Message> createKeywordMsg(Message value) {
        return new JAXBElement<Message>(_ForMsg_QNAME, Message.class, Keyword.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "var", scope = Keyword.class)
    public JAXBElement<String> createKeywordVar(String value) {
        return new JAXBElement<String>(_ForVar_QNAME, String.class, Keyword.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "arg", scope = Keyword.class)
    public JAXBElement<String> createKeywordArg(String value) {
        return new JAXBElement<String>(_KeywordArg_QNAME, String.class, Keyword.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link For }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "for", scope = Keyword.class)
    public JAXBElement<For> createKeywordFor(For value) {
        return new JAXBElement<For>(_KeywordFor_QNAME, For.class, Keyword.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "doc", scope = Keyword.class)
    public JAXBElement<String> createKeywordDoc(String value) {
        return new JAXBElement<String>(_ForDoc_QNAME, String.class, Keyword.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "tag", scope = Keyword.class)
    public JAXBElement<String> createKeywordTag(String value) {
        return new JAXBElement<String>(_KeywordTag_QNAME, String.class, Keyword.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Keyword }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "kw", scope = Keyword.class)
    public JAXBElement<Keyword> createKeywordKw(Keyword value) {
        return new JAXBElement<Keyword>(_ForKw_QNAME, Keyword.class, Keyword.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link If }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "if", scope = Keyword.class)
    public JAXBElement<If> createKeywordIf(If value) {
        return new JAXBElement<If>(_KeywordIf_QNAME, If.class, Keyword.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Return }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "return", scope = Keyword.class)
    public JAXBElement<Return> createKeywordReturn(Return value) {
        return new JAXBElement<Return>(_KeywordReturn_QNAME, Return.class, Keyword.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Timeout }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "timeout", scope = Keyword.class)
    public JAXBElement<Timeout> createKeywordTimeout(Timeout value) {
        return new JAXBElement<Timeout>(_KeywordTimeout_QNAME, Timeout.class, Keyword.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link BodyItemStatus }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "status", scope = Keyword.class)
    public JAXBElement<BodyItemStatus> createKeywordStatus(BodyItemStatus value) {
        return new JAXBElement<BodyItemStatus>(_ForStatus_QNAME, BodyItemStatus.class, Keyword.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Message }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "msg", scope = Test.class)
    public JAXBElement<Message> createTestMsg(Message value) {
        return new JAXBElement<Message>(_ForMsg_QNAME, Message.class, Test.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link For }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "for", scope = Test.class)
    public JAXBElement<For> createTestFor(For value) {
        return new JAXBElement<For>(_KeywordFor_QNAME, For.class, Test.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "doc", scope = Test.class)
    public JAXBElement<String> createTestDoc(String value) {
        return new JAXBElement<String>(_ForDoc_QNAME, String.class, Test.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link String }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "tag", scope = Test.class)
    public JAXBElement<String> createTestTag(String value) {
        return new JAXBElement<String>(_KeywordTag_QNAME, String.class, Test.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Keyword }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "kw", scope = Test.class)
    public JAXBElement<Keyword> createTestKw(Keyword value) {
        return new JAXBElement<Keyword>(_ForKw_QNAME, Keyword.class, Test.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link If }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "if", scope = Test.class)
    public JAXBElement<If> createTestIf(If value) {
        return new JAXBElement<If>(_KeywordIf_QNAME, If.class, Test.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Timeout }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "timeout", scope = Test.class)
    public JAXBElement<Timeout> createTestTimeout(Timeout value) {
        return new JAXBElement<Timeout>(_KeywordTimeout_QNAME, Timeout.class, Test.class, value);
    }

    /**
     * Create an instance of {@link JAXBElement }{@code <}{@link Status }{@code >}}
     * 
     */
    @XmlElementDecl(namespace = "", name = "status", scope = Test.class)
    public JAXBElement<Status> createTestStatus(Status value) {
        return new JAXBElement<Status>(_ForStatus_QNAME, Status.class, Test.class, value);
    }

}
