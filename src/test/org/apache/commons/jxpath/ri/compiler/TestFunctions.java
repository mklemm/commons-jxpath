/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//jxpath/src/test/org/apache/commons/jxpath/ri/compiler/TestFunctions.java,v 1.3 2003/01/20 00:00:27 dmitri Exp $
 * $Revision: 1.3 $
 * $Date: 2003/01/20 00:00:27 $
 *
 * ====================================================================
 * The Apache Software License, Version 1.1
 *
 *
 * Copyright (c) 1999-2001 The Apache Software Foundation.  All rights
 * reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        Apache Software Foundation (http://www.apache.org/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The Jakarta Project", "Commons", and "Apache Software
 *    Foundation" must not be used to endorse or promote products derived
 *    from this software without prior written permission. For written
 *    permission, please contact apache@apache.org.
 *
 * 5. Products derived from this software may not be called "Apache"
 *    nor may "Apache" appear in their names without prior written
 *    permission of the Apache Group.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the Apache Software Foundation and was
 * originally based on software copyright (c) 2001, Plotnix, Inc,
 * <http://www.plotnix.com/>.
 * For more information on the Apache Software Foundation, please see
 * <http://www.apache.org/>.
 */
package org.apache.commons.jxpath.ri.compiler;

import java.util.Collection;
import java.util.Map;

import org.apache.commons.jxpath.ExpressionContext;
import org.apache.commons.jxpath.Pointer;

/**
 * @author Dmitri Plotnikov
 * @version $Revision: 1.3 $ $Date: 2003/01/20 00:00:27 $
 */
public class TestFunctions {

    private int foo;
    private String bar;

    public TestFunctions() {
    }

    public TestFunctions(int foo, String bar) {
        this.foo = foo;
        this.bar = bar;
    }

    public TestFunctions(ExpressionContext context, String bar) {
        this.foo =
            ((Number) context.getContextNodePointer().getValue()).intValue();
        this.bar = bar;
    }

    public int getFoo() {
        return foo;
    }

    public String getBar() {
        return bar;
    }

    public void doit() {
    }

    public TestFunctions setFooAndBar(int foo, String bar) {
        this.foo = foo;
        this.bar = bar;
        return this;
    }

    public static TestFunctions build(int foo, String bar) {
        return new TestFunctions(foo, bar);
    }

    public String toString() {
        return "foo=" + foo + "; bar=" + bar;
    }

    public static String path(ExpressionContext context) {
        return context.getContextNodePointer().asPath();
    }

    public String instancePath(ExpressionContext context) {
        return context.getContextNodePointer().asPath();
    }

    public String pathWithSuffix(ExpressionContext context, String suffix) {
        return context.getContextNodePointer().asPath() + suffix;
    }

    public String className(
        ExpressionContext context,
        ExpressionContext child) 
    {
        return context.getContextNodePointer().asPath();
    }

    /**
     * Returns true if the current node in the current context is a map
     */
    public static boolean isMap(ExpressionContext context) {
        Pointer ptr = context.getContextNodePointer();
        return ptr == null ? false : (ptr.getValue() instanceof Map);
    }

    /**
     * Returns the number of nodes in the context that is passed as
     * the first argument.
     */
    public static int count(ExpressionContext context, Collection col) {
        return col.size();
    }
}