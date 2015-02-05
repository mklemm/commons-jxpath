/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.commons.jxpath.ri.axes;

import java.util.HashMap;

import junit.framework.TestCase;

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.NestedTestBean;
import org.apache.commons.jxpath.Pointer;
import org.apache.commons.jxpath.TestNull;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.VariablePointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPointer;
import org.apache.commons.jxpath.ri.model.beans.BeanPropertyPointer;
import org.apache.commons.jxpath.ri.model.beans.CollectionPointer;
import org.apache.commons.jxpath.ri.model.beans.NullElementPointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;
import org.apache.commons.jxpath.ri.model.beans.NullPropertyPointer;
import org.apache.commons.jxpath.ri.model.beans.TestBeanFactory;
import org.apache.commons.jxpath.ri.model.dom.DOMNodePointer;
import org.apache.commons.jxpath.ri.model.dynamic.DynamicPointer;
import org.apache.commons.jxpath.ri.model.dynamic.DynamicPropertyPointer;

public class SimplePathInterpreterTest extends TestCase {

    private TestBeanWithNode bean;
    private JXPathContext context;

    protected void setUp() throws Exception {
        bean = TestBeanWithNode.createTestBeanWithDOM();
        HashMap submap = new HashMap();
        submap.put("key", new NestedTestBean("Name 9"));
        submap.put("strings", bean.getNestedBean().getStrings());
        bean.getList().add(new int[]{1, 2});
        bean.getList().add(bean.getVendor());
        bean.getMap().put("Key3",
            new Object[]{
                new NestedTestBean("some"),
                new Integer(2),
                bean.getVendor(),
                submap
            }
        );
        bean.getMap().put("Key4", bean.getVendor());
        bean.getMap().put("Key5", submap);
        bean.getMap().put("Key6", new Object[0]);
        context = JXPathContext.newContext(null, bean);
        context.setLenient(true);
        context.setFactory(new TestBeanFactory());
    }

    public void testDoStepNoPredicatesPropertyOwner() {
        // Existing scalar property
        assertValueAndPointer("/int",
                new Integer(1),
                "/int",
                "Bb",
                "BbB");

        // self::
        assertValueAndPointer("/./int",
                new Integer(1),
                "/int",
                "Bb",
                "BbB");

        // Missing property
        assertNullPointer("/foo",
                "/foo",
                "Bn");

        // existingProperty/existingScalarProperty
        assertValueAndPointer("/nestedBean/int",
                new Integer(1),
                "/nestedBean/int",
                "BbBb",
                "BbBbB");

        // existingProperty/collectionProperty
        assertValueAndPointer("/nestedBean/strings",
                bean.getNestedBean().getStrings(),
                "/nestedBean/strings",
                "BbBb",
                "BbBbC");

        // existingProperty/missingProperty
        assertNullPointer("/nestedBean/foo",
                "/nestedBean/foo",
                "BbBn");

        // map/missingProperty
        assertNullPointer("/map/foo",
                "/map[@name_='foo']",
                "BbDd");

        // Existing property by search in collection
        assertValueAndPointer("/list/int",
                new Integer(1),
                "/list[3]/int",
                "BbBb",
                "BbBbB");

        // Missing property by search in collection
        assertNullPointer("/list/foo",
                "/list[1]/foo",
                "BbBn");

        // existingProperty/missingProperty/missingProperty
        assertNullPointer("/nestedBean/foo/bar",
                "/nestedBean/foo/bar",
                "BbBnNn");

        // collection/existingProperty/missingProperty
        assertNullPointer("/list/int/bar",
                "/list[3]/int/bar",
                "BbBbBn");

        // collectionProperty/missingProperty/missingProperty
        assertNullPointer("/list/foo/bar",
                "/list[1]/foo/bar",
                "BbBnNn");

        // map/missingProperty/anotherStep
        assertNullPointer("/map/foo/bar",
                "/map[@name_='foo']/bar",
                "BbDdNn");

        // Existing dynamic property
        assertValueAndPointer("/map/Key1",
                "Value 1",
                "/map[@name_='Key1']",
                "BbDd",
                "BbDdB");

        // collectionProperty
        assertValueAndPointer("/integers",
                bean.getIntegers(),
                "/integers",
                "Bb",
                "BbC");
    }

    public void testDoStepNoPredicatesStandard() {
        // Existing DOM node
        assertValueAndPointer("/vendor/location/address/city",
                "Fruit Market",
                "/vendor/location[2]/address[1]/city[1]",
                "BbMMMM");

        // Missing DOM node
        assertNullPointer("/vendor/location/address/pity",
                "/vendor/location[1]/address[1]/pity",
                "BbMMMn");

        // Missing DOM node inside a missing element
        assertNullPointer("/vendor/location/address/itty/bitty",
                "/vendor/location[1]/address[1]/itty/bitty",
                "BbMMMnNn");

        // Missing DOM node by search for the best match
        assertNullPointer("/vendor/location/address/city/pretty",
                "/vendor/location[2]/address[1]/city[1]/pretty",
                "BbMMMMn");
    }

    public void testDoStepPredicatesPropertyOwner() {
        // missingProperty[@name_=foo]
        assertNullPointer("/foo[@name_='foo']",
                "/foo[@name_='foo']",
                "BnNn");

        // missingProperty[index]
        assertNullPointer("/foo[3]",
                "/foo[3]",
                "Bn");
    }

    public void testDoStepPredicatesStandard() {
        // Looking for an actual XML attribute called "name"
        // nodeProperty/name[@name_=value]
        assertValueAndPointer("/vendor/contact[@name='jack']",
                "Jack",
                "/vendor/contact[2]",
                "BbMM");

        // Indexing in XML
        assertValueAndPointer("/vendor/contact[2]",
                "Jack",
                "/vendor/contact[2]",
                "BbMM");

        // Indexing in XML, no result
        assertNullPointer("/vendor/contact[5]",
                "/vendor/contact[5]",
                "BbMn");

        // Combination of search by name and indexing in XML
        assertValueAndPointer("/vendor/contact[@name='jack'][2]",
                "Jack Black",
                "/vendor/contact[4]",
                "BbMM");

        // Combination of search by name and indexing in XML
        assertValueAndPointer("/vendor/contact[@name='jack'][2]",
                "Jack Black",
                "/vendor/contact[4]",
                "BbMM");
    }

    public void testDoPredicateName() {
        // existingProperty[@name=existingProperty]
        assertValueAndPointer("/nestedBean[@name_='int']",
                new Integer(1),
                "/nestedBean/int",
                "BbBb",
                "BbBbB");

        // /self::node()[@name_=existingProperty]
        assertValueAndPointer("/.[@name_='int']",
                new Integer(1),
                "/int",
                "Bb",
                "BbB");

        // dynamicProperty[@name_=existingProperty]
        assertValueAndPointer("/map[@name_='Key1']",
                "Value 1",
                "/map[@name_='Key1']",
                "BbDd",
                "BbDdB");

        // existingProperty[@name_=collectionProperty]
        assertValueAndPointer("/nestedBean[@name_='strings']",
                bean.getNestedBean().getStrings(),
                "/nestedBean/strings",
                "BbBb",
                "BbBbC");

        // existingProperty[@name_=missingProperty]
        assertNullPointer("/nestedBean[@name_='foo']",
                "/nestedBean[@name_='foo']",
                "BbBn");

        // map[@name_=collectionProperty]
        assertValueAndPointer("/map[@name_='Key3']",
                bean.getMap().get("Key3"),
                "/map[@name_='Key3']",
                "BbDd",
                "BbDdC");
                
        // map[@name_=missingProperty]
        assertNullPointer("/map[@name_='foo']",
                "/map[@name_='foo']",
                "BbDd");

        // collectionProperty[@name_=...] (find node)
        assertValueAndPointer("/list[@name='fruitco']",
                context.getValue("/vendor"),
                "/list[5]",
                "BbCM");

        // collectionProperty[@name_=...] (find map entry)
        assertValueAndPointer("/map/Key3[@name_='key']/name",
                "Name 9",
                "/map[@name_='Key3'][4][@name_='key']/name",
                "BbDdCDdBb",
                "BbDdCDdBbB");

        // map/collectionProperty[@name_...]
        assertValueAndPointer("map/Key3[@name_='fruitco']",
                context.getValue("/vendor"),
                "/map[@name_='Key3'][3]",
                "BbDdCM");

        // Bean property -> DOM Node, name match
        assertValueAndPointer("/vendor[@name_='fruitco']",
                context.getValue("/vendor"),
                "/vendor",
                "BbM");

        // Bean property -> DOM Node, name mismatch
        assertNullPointer("/vendor[@name_='foo']",
                "/vendor[@name_='foo']",
                "BbMn");

        assertNullPointer("/vendor[@name_='foo'][3]",
                "/vendor[@name_='foo'][3]",
                "BbMn");

        // existingProperty(bean)[@name_=missingProperty]/anotherStep
        assertNullPointer("/nestedBean[@name_='foo']/bar",
                "/nestedBean[@name_='foo']/bar",
                "BbBnNn");

        // map[@name_=missingProperty]/anotherStep
        assertNullPointer("/map[@name_='foo']/bar",
                "/map[@name_='foo']/bar",
                "BbDdNn");

        // existingProperty(node)[@name_=missingProperty]/anotherStep
        assertNullPointer("/vendor[@name_='foo']/bar",
                "/vendor[@name_='foo']/bar",
                "BbMnNn");

        // existingProperty(node)[@name_=missingProperty][index]/anotherStep
        assertNullPointer("/vendor[@name_='foo'][3]/bar",
                "/vendor[@name_='foo'][3]/bar",
                "BbMnNn");

        // Existing dynamic property + existing property
        assertValueAndPointer("/map[@name_='Key2'][@name_='name']",
                "Name 6",
                "/map[@name_='Key2']/name",
                "BbDdBb",
                "BbDdBbB");

        // Existing dynamic property + existing property + index
        assertValueAndPointer("/map[@name_='Key2'][@name_='strings'][2]",
                "String 2",
                "/map[@name_='Key2']/strings[2]",
                "BbDdBb",
                "BbDdBbB");

        // bean/map/map/property
        assertValueAndPointer("map[@name_='Key5'][@name_='key']/name",
                "Name 9",
                "/map[@name_='Key5'][@name_='key']/name",
                "BbDdDdBb",
                "BbDdDdBbB");

        assertNullPointer("map[@name_='Key2'][@name_='foo']",
                "/map[@name_='Key2'][@name_='foo']",
                "BbDdBn");

        assertNullPointer("map[@name_='Key2'][@name_='foo'][@name_='bar']",
                "/map[@name_='Key2'][@name_='foo'][@name_='bar']",
                "BbDdBnNn");

        // bean/map/node
        assertValueAndPointer("map[@name_='Key4'][@name_='fruitco']",
                context.getValue("/vendor"),
                "/map[@name_='Key4']",
                "BbDdM");
    }

    public void testDoPredicatesStandard() {
        // bean/map/collection/node
        assertValueAndPointer("map[@name_='Key3'][@name_='fruitco']",
                context.getValue("/vendor"),
                "/map[@name_='Key3'][3]",
                "BbDdCM");

        // bean/map/collection/missingNode
        assertNullPointer("map[@name_='Key3'][@name_='foo']",
                "/map[@name_='Key3'][4][@name_='foo']",
                "BbDdCDd");

        // bean/map/node
        assertValueAndPointer("map[@name_='Key4'][@name_='fruitco']",
                context.getValue("/vendor"),
                "/map[@name_='Key4']",
                "BbDdM");

        // bean/map/emptyCollection[@name_=foo]
        assertNullPointer("map[@name_='Key6'][@name_='fruitco']",
                "/map[@name_='Key6'][@name_='fruitco']",
                "BbDdCn");

        // bean/node[@name_=foo][index]
        assertValueAndPointer("/vendor/contact[@name_='jack'][2]",
                "Jack Black",
                "/vendor/contact[4]",
                "BbMM");

        // bean/node[@name_=foo][missingIndex]
        assertNullPointer("/vendor/contact[@name_='jack'][5]",
                "/vendor/contact[@name_='jack'][5]",
                "BbMnNn");

        // bean/node/.[@name_=foo][index]
        assertValueAndPointer("/vendor/contact/.[@name_='jack']",
                "Jack",
                "/vendor/contact[2]",
                "BbMM");
    }

    public void testDoPredicateIndex() {
        // Existing dynamic property + existing property + index
        assertValueAndPointer("/map[@name_='Key2'][@name_='strings'][2]",
                "String 2",
                "/map[@name_='Key2']/strings[2]",
                "BbDdBb",
                "BbDdBbB");

        // existingProperty[@name_=collectionProperty][index]
        assertValueAndPointer("/nestedBean[@name_='strings'][2]",
                bean.getNestedBean().getStrings()[1],
                "/nestedBean/strings[2]",
                "BbBb",
                "BbBbB");

        // existingProperty[@name_=missingProperty][index]
        assertNullPointer("/nestedBean[@name_='foo'][3]",
                "/nestedBean[@name_='foo'][3]",
                "BbBn");

        // existingProperty[@name_=collectionProperty][missingIndex]
        assertNullPointer("/nestedBean[@name_='strings'][5]",
                "/nestedBean/strings[5]",
                "BbBbE");

        // map[@name_=collectionProperty][index]
        assertValueAndPointer("/map[@name_='Key3'][2]",
                new Integer(2),
                "/map[@name_='Key3'][2]",
                "BbDd",
                "BbDdB");

        // map[@name_=collectionProperty][missingIndex]
        assertNullPointer("/map[@name_='Key3'][5]",
                "/map[@name_='Key3'][5]",
                "BbDdE");

        // map[@name_=collectionProperty][missingIndex]/property
        assertNullPointer("/map[@name_='Key3'][5]/foo",
                "/map[@name_='Key3'][5]/foo",
                "BbDdENn");

        // map[@name_=map][@name_=collection][index]
        assertValueAndPointer("/map[@name_='Key5'][@name_='strings'][2]",
                "String 2",
                "/map[@name_='Key5'][@name_='strings'][2]",
                "BbDdDd",
                "BbDdDdB");

        // map[@name_=map][@name_=collection][missingIndex]
        assertNullPointer("/map[@name_='Key5'][@name_='strings'][5]",
                "/map[@name_='Key5'][@name_='strings'][5]",
                "BbDdDdE");

        // Existing dynamic property + indexing
        assertValueAndPointer("/map[@name_='Key3'][2]",
                new Integer(2),
                "/map[@name_='Key3'][2]",
                "BbDd",
                "BbDdB");

        // Existing dynamic property + indexing
        assertValueAndPointer("/map[@name_='Key3'][1]/name",
                "some",
                "/map[@name_='Key3'][1]/name",
                "BbDdBb",
                "BbDdBbB");

        // map[@name_=missingProperty][index]
        assertNullPointer("/map[@name_='foo'][3]",
                "/map[@name_='foo'][3]",
                "BbDdE");

        // collectionProperty[index]
        assertValueAndPointer("/integers[2]",
                new Integer(2),
                "/integers[2]",
                "Bb",
                "BbB");

        // existingProperty/collectionProperty[index]
        assertValueAndPointer("/nestedBean/strings[2]",
                bean.getNestedBean().getStrings()[1],
                "/nestedBean/strings[2]",
                "BbBb",
                "BbBbB");

        // existingProperty[index]/existingProperty
        assertValueAndPointer("/list[3]/int",
                new Integer(1),
                "/list[3]/int",
                "BbBb",
                "BbBbB");

        // existingProperty[missingIndex]
        assertNullPointer("/list[6]",
                "/list[6]",
                "BbE");

        // existingProperty/missingProperty[index]
        assertNullPointer("/nestedBean/foo[3]",
                "/nestedBean/foo[3]",
                "BbBn");

        // map[@name_=missingProperty][index]
        assertNullPointer("/map/foo[3]",
                "/map[@name_='foo'][3]",
                "BbDdE");

        // existingProperty/collectionProperty[missingIndex]
        assertNullPointer("/nestedBean/strings[5]",
                "/nestedBean/strings[5]",
                "BbBbE");

        // map/collectionProperty[missingIndex]/property
        assertNullPointer("/map/Key3[5]/foo",
                "/map[@name_='Key3'][5]/foo",
                "BbDdENn");

        // map[@name_=map]/collection[index]
        assertValueAndPointer("/map[@name_='Key5']/strings[2]",
                "String 2",
                "/map[@name_='Key5'][@name_='strings'][2]",
                "BbDdDd",
                "BbDdDdB");

        // map[@name_=map]/collection[missingIndex]
        assertNullPointer("/map[@name_='Key5']/strings[5]",
                "/map[@name_='Key5'][@name_='strings'][5]",
                "BbDdDdE");

        // scalarPropertyAsCollection[index]
        assertValueAndPointer("/int[1]",
                new Integer(1),
                "/int",
                "Bb",
                "BbB");

        // scalarPropertyAsCollection[index]
        assertValueAndPointer(".[1]/int",
                new Integer(1),
                "/int",
                "Bb",
                "BbB");
    }

    public void testInterpretExpressionPath() {
        context.getVariables().declareVariable("array", new String[]{"Value1"});
        context.getVariables().declareVariable("testnull", new TestNull());

        assertNullPointer("$testnull/nothing[2]",
                "$testnull/nothing[2]",
                "VBbE");
    }

    private void assertValueAndPointer(
            String path, Object expectedValue, String expectedPath,
            String expectedSignature)
    {
        assertValueAndPointer(
            path,
            expectedValue,
            expectedPath,
            expectedSignature,
            expectedSignature);
    }
    
    private void assertValueAndPointer(
            String path, Object expectedValue, String expectedPath,
            String expectedSignature, String expectedValueSignature)
    {
        Object value = context.getValue(path);
        assertEquals("Checking value: " + path, expectedValue, value);

        Pointer pointer = context.getPointer(path);
        assertEquals("Checking pointer: " + path,
                expectedPath, pointer.toString());

        assertEquals("Checking signature: " + path,
                expectedSignature, pointerSignature(pointer));
        
        Pointer vPointer = ((NodePointer) pointer).getValuePointer();
        assertEquals("Checking value pointer signature: " + path,
                expectedValueSignature, pointerSignature(vPointer));
    }

    private void assertNullPointer(String path, String expectedPath,
            String expectedSignature)
    {
        Pointer pointer = context.getPointer(path);
        assertNotNull("Null path exists: " + path,
                    pointer);
        assertEquals("Null path as path: " + path,
                    expectedPath, pointer.asPath());
        assertEquals("Checking Signature: " + path,
                    expectedSignature, pointerSignature(pointer));
                
        Pointer vPointer = ((NodePointer) pointer).getValuePointer();
        assertTrue("Null path is null: " + path,
                    !((NodePointer) vPointer).isActual());
        assertEquals("Checking value pointer signature: " + path,
                    expectedSignature + "N", pointerSignature(vPointer));
    }

    /**
     * Since we need to test the internal Signature of a pointer,
     * we will get a signature which will contain a single character
     * per pointer in the chain, representing that pointer's type.
     */
    private String pointerSignature(Pointer pointer) {
        if (pointer == null) {
            return "";
        }

        char type = '?';
        if (pointer instanceof NullPointer) {                 type = 'N'; }
        else if (pointer instanceof NullPropertyPointer) {    type = 'n'; }
        else if (pointer instanceof NullElementPointer) {     type = 'E'; }
        else if (pointer instanceof VariablePointer) {        type = 'V'; }
        else if (pointer instanceof CollectionPointer) {      type = 'C'; }
        else if (pointer instanceof BeanPointer) {            type = 'B'; }
        else if (pointer instanceof BeanPropertyPointer) {    type = 'b'; }
        else if (pointer instanceof DynamicPointer) {         type = 'D'; }
        else if (pointer instanceof DynamicPropertyPointer) { type = 'd'; }
        else if (pointer instanceof DOMNodePointer) {         type = 'M'; }
        else {
            System.err.println("UNKNOWN TYPE: " + pointer.getClass());
        }
        NodePointer parent = 
            ((NodePointer) pointer).getImmediateParentPointer();
        return pointerSignature(parent) + type;
    }
}

