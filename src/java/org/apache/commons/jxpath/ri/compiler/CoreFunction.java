/*
 * $Header: /home/jerenkrantz/tmp/commons/commons-convert/cvs/home/cvs/jakarta-commons//jxpath/src/java/org/apache/commons/jxpath/ri/compiler/CoreFunction.java,v 1.6 2002/08/10 01:37:12 dmitri Exp $
 * $Revision: 1.6 $
 * $Date: 2002/08/10 01:37:12 $
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

import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.JXPathException;
import org.apache.commons.jxpath.ri.Compiler;
import org.apache.commons.jxpath.ri.InfoSetUtil;
import org.apache.commons.jxpath.ri.EvalContext;
import org.apache.commons.jxpath.ri.model.NodePointer;
import org.apache.commons.jxpath.ri.model.beans.NullPointer;

import java.util.Collection;

/**
 * An element of the compile tree representing one of built-in functions
 * like "position()" or "number()".
 *
 * @author Dmitri Plotnikov
 * @version $Revision: 1.6 $ $Date: 2002/08/10 01:37:12 $
 */
public class CoreFunction extends Operation {

    private static final Double ZERO = new Double(0);
    private int functionCode;

    public CoreFunction(int functionCode, Expression args[]){
        super(Expression.OP_CORE_FUNCTION, args);
        this.functionCode = functionCode;
    }

    public int getFunctionCode(){
        return functionCode;
    }

    public Expression getArg1(){
        return args[0];
    }

    public Expression getArg2(){
        return args[1];
    }

    public Expression getArg3(){
        return args[2];
    }

    public int getArgumentCount(){
        if (args == null){
            return 0;
        }
        return args.length;
    }

    /**
     * Returns true if any argument is context dependent or if
     * the function is last(), position(), boolean(), local-name(),
     * name(), string(), lang(), number().
     */
    public boolean computeContextDependent(){
        if (super.computeContextDependent()){
            return true;
        }

        switch(functionCode){
            case Compiler.FUNCTION_LAST:
            case Compiler.FUNCTION_POSITION:
                return true;

            case Compiler.FUNCTION_BOOLEAN:
            case Compiler.FUNCTION_LOCAL_NAME:
            case Compiler.FUNCTION_NAME:
            case Compiler.FUNCTION_NAMESPACE_URI:
            case Compiler.FUNCTION_STRING:
            case Compiler.FUNCTION_LANG:
            case Compiler.FUNCTION_NUMBER:
                return args == null || args.length == 0;

            case Compiler.FUNCTION_COUNT:
            case Compiler.FUNCTION_ID:
            case Compiler.FUNCTION_CONCAT:
            case Compiler.FUNCTION_STARTS_WITH:
            case Compiler.FUNCTION_CONTAINS:
            case Compiler.FUNCTION_SUBSTRING_BEFORE:
            case Compiler.FUNCTION_SUBSTRING_AFTER:
            case Compiler.FUNCTION_SUBSTRING:
            case Compiler.FUNCTION_STRING_LENGTH:
            case Compiler.FUNCTION_NORMALIZE_SPACE:
            case Compiler.FUNCTION_TRANSLATE:
            case Compiler.FUNCTION_NOT:
            case Compiler.FUNCTION_TRUE:
            case Compiler.FUNCTION_FALSE:
            case Compiler.FUNCTION_SUM:
            case Compiler.FUNCTION_FLOOR:
            case Compiler.FUNCTION_CEILING:
            case Compiler.FUNCTION_ROUND:
                return false;
        }

        return false;
    }

    protected String opCodeToString(){
        String function = null;
        switch(functionCode){
            case Compiler.FUNCTION_LAST:             function = "last"; break;
            case Compiler.FUNCTION_POSITION:         function = "position"; break;
            case Compiler.FUNCTION_COUNT:            function = "count"; break;
            case Compiler.FUNCTION_ID:               function = "id"; break;
            case Compiler.FUNCTION_LOCAL_NAME:       function = "local-name"; break;
            case Compiler.FUNCTION_NAMESPACE_URI:    function = "namespace-uri"; break;
            case Compiler.FUNCTION_NAME:             function = "name"; break;
            case Compiler.FUNCTION_STRING:           function = "string"; break;
            case Compiler.FUNCTION_CONCAT:           function = "concat"; break;
            case Compiler.FUNCTION_STARTS_WITH:      function = "starts-with"; break;
            case Compiler.FUNCTION_CONTAINS:         function = "contains"; break;
            case Compiler.FUNCTION_SUBSTRING_BEFORE: function = "substring-before"; break;
            case Compiler.FUNCTION_SUBSTRING_AFTER:  function = "substring-after"; break;
            case Compiler.FUNCTION_SUBSTRING:        function = "substring"; break;
            case Compiler.FUNCTION_STRING_LENGTH:    function = "string-length"; break;
            case Compiler.FUNCTION_NORMALIZE_SPACE:  function = "normalize-space"; break;
            case Compiler.FUNCTION_TRANSLATE:        function = "translate"; break;
            case Compiler.FUNCTION_BOOLEAN:          function = "boolean"; break;
            case Compiler.FUNCTION_NOT:              function = "not"; break;
            case Compiler.FUNCTION_TRUE:             function = "true"; break;
            case Compiler.FUNCTION_FALSE:            function = "false"; break;
            case Compiler.FUNCTION_LANG:             function = "lang"; break;
            case Compiler.FUNCTION_NUMBER:           function = "number"; break;
            case Compiler.FUNCTION_SUM:              function = "sum"; break;
            case Compiler.FUNCTION_FLOOR:            function = "floor"; break;
            case Compiler.FUNCTION_CEILING:          function = "ceiling"; break;
            case Compiler.FUNCTION_ROUND:            function = "round"; break;
            case Compiler.FUNCTION_KEY:              function = "key"; break;
        }
        return super.opCodeToString() + ':' + function;
    }

    public Object compute(EvalContext context){
        return computeValue(context);
    }

    /**
     * Computes a built-in function
     */
    public Object computeValue(EvalContext context){
        switch(functionCode){
            case Compiler.FUNCTION_LAST:                return functionLast(context);
            case Compiler.FUNCTION_POSITION:            return functionPosition(context);
            case Compiler.FUNCTION_COUNT:               return functionCount(context);
            case Compiler.FUNCTION_LANG:                return functionLang(context);
            case Compiler.FUNCTION_ID:                  return functionID(context);
            case Compiler.FUNCTION_LOCAL_NAME:          return functionLocalName(context);
            case Compiler.FUNCTION_NAMESPACE_URI:       return functionNamespaceURI(context);
            case Compiler.FUNCTION_NAME:                return functionName(context);
            case Compiler.FUNCTION_STRING:              return functionString(context);
            case Compiler.FUNCTION_CONCAT:              return functionConcat(context);
            case Compiler.FUNCTION_STARTS_WITH:         return functionStartsWith(context);
            case Compiler.FUNCTION_CONTAINS:            return functionContains(context);
            case Compiler.FUNCTION_SUBSTRING_BEFORE:    return functionSubstringBefore(context);
            case Compiler.FUNCTION_SUBSTRING_AFTER:     return functionSubstringAfter(context);
            case Compiler.FUNCTION_SUBSTRING:           return functionSubstring(context);
            case Compiler.FUNCTION_STRING_LENGTH:       return functionStringLength(context);
            case Compiler.FUNCTION_NORMALIZE_SPACE:     return functionNormalizeSpace(context);
            case Compiler.FUNCTION_TRANSLATE:           return functionTranslate(context);
            case Compiler.FUNCTION_BOOLEAN:             return functionBoolean(context);
            case Compiler.FUNCTION_NOT:                 return functionNot(context);
            case Compiler.FUNCTION_TRUE:                return functionTrue(context);
            case Compiler.FUNCTION_FALSE:               return functionFalse(context);
            case Compiler.FUNCTION_NULL:                return functionNull(context);
            case Compiler.FUNCTION_NUMBER:              return functionNumber(context);
            case Compiler.FUNCTION_SUM:                 return functionSum(context);
            case Compiler.FUNCTION_FLOOR:               return functionFloor(context);
            case Compiler.FUNCTION_CEILING:             return functionCeiling(context);
            case Compiler.FUNCTION_ROUND:               return functionRound(context);
            case Compiler.FUNCTION_KEY:                 return functionKey(context);
        }
        return null;
    }

    protected Object functionLast(EvalContext context){
        assertArgCount(0);
        // Move the position to the beginning and iterate through
        // the context to count nodes.
        int old = context.getCurrentPosition();
        context.reset();
        int count = 0;
        while(context.nextNode()){
            count++;
        }

        // Restore the current position.
        if (old != 0){
            context.setPosition(old);
        }
        return new Double(count);
    }

    protected Object functionPosition(EvalContext context){
        assertArgCount(0);
        return new Integer(context.getCurrentPosition());
    }

    protected Object functionCount(EvalContext context){
        assertArgCount(1);
        Expression arg1 = getArg1();
        int count = 0;
        Object value = arg1.compute(context);
        if (value instanceof NodePointer){
            value = ((NodePointer)value).getValue();
        }
        if (value instanceof EvalContext){
            EvalContext ctx = (EvalContext)value;
            while(ctx.hasNext()){
                ctx.next();
                count++;
            }
        }
        else if (value instanceof Collection){
            count = ((Collection)value).size();
        }
        else if (value == null){
            count = 0;
        }
        else {
            count = 1;
        }
        return new Double(count);
    }

    protected Object functionLang(EvalContext context){
        assertArgCount(1);
        String lang = InfoSetUtil.stringValue(getArg1().computeValue(context));
        NodePointer pointer = (NodePointer)context.getSingleNodePointer();
        if (pointer == null){
            return Boolean.FALSE;
        }
        return pointer.isLanguage(lang) ? Boolean.TRUE: Boolean.FALSE;
    }

    protected Object functionID(EvalContext context){
        assertArgCount(1);
        String id = InfoSetUtil.stringValue(getArg1().computeValue(context));
        JXPathContext jxpathContext = context.getJXPathContext();
        NodePointer pointer = (NodePointer)jxpathContext.getContextPointer();
        return pointer.getPointerByID(jxpathContext, id);
    }

    protected Object functionKey(EvalContext context){
        assertArgCount(2);
        String key = InfoSetUtil.stringValue(getArg1().computeValue(context));
        String value = InfoSetUtil.stringValue(getArg2().computeValue(context));
        JXPathContext jxpathContext = context.getJXPathContext();
        NodePointer pointer = (NodePointer)jxpathContext.getContextPointer();
        return pointer.getPointerByKey(jxpathContext, key, value);
    }

    protected Object functionNamespaceURI(EvalContext context){
        if (getArgumentCount() == 0){
            return context.getCurrentNodePointer();
        }
        assertArgCount(1);
        Object set = getArg1().compute(context);
        if (set instanceof EvalContext){
            EvalContext ctx = (EvalContext)set;
            if (ctx.hasNext()){
                NodePointer ptr = (NodePointer)ctx.next();
                String str = ptr.getNamespaceURI();
                return str == null ? "" : str;
            }
        }
        return "";
    }

    protected Object functionLocalName(EvalContext context){
        if (getArgumentCount() == 0){
            return context.getCurrentNodePointer();
        }
        assertArgCount(1);
        Object set = getArg1().compute(context);
        if (set instanceof EvalContext){
            EvalContext ctx = (EvalContext)set;
            if (ctx.hasNext()){
                NodePointer ptr = (NodePointer)ctx.next();
                return ptr.getName().getName();
            }
        }
        return "";
    }

    protected Object functionName(EvalContext context){
        if (getArgumentCount() == 0){
            return context.getCurrentNodePointer();
        }
        assertArgCount(1);
        Object set = getArg1().compute(context);
        if (set instanceof EvalContext){
            EvalContext ctx = (EvalContext)set;
            if (ctx.hasNext()){
                NodePointer ptr = (NodePointer)ctx.next();
                return ptr.getExpandedName().toString();
            }
        }
        return "";
    }

    protected Object functionString(EvalContext context){
        if (getArgumentCount() == 0){
            return InfoSetUtil.stringValue(context.getCurrentNodePointer());
        }
        assertArgCount(1);
        return InfoSetUtil.stringValue(getArg1().computeValue(context));
    }

    protected Object functionConcat(EvalContext context){
        if (getArgumentCount() < 2){
            assertArgCount(2);
        }
        StringBuffer buffer = new StringBuffer();
        Expression args[] = getArguments();
        for (int i = 0; i < args.length; i++){
            buffer.append(InfoSetUtil.stringValue(args[i].compute(context)));
        }
        return buffer.toString();
    }

    protected Object functionStartsWith(EvalContext context){
        assertArgCount(2);
        String s1 = InfoSetUtil.stringValue(getArg1().computeValue(context));
        String s2 = InfoSetUtil.stringValue(getArg2().computeValue(context));
        return s1.startsWith(s2) ? Boolean.TRUE : Boolean.FALSE;
    }

    protected Object functionContains(EvalContext context){
        assertArgCount(2);
        String s1 = InfoSetUtil.stringValue(getArg1().computeValue(context));
        String s2 = InfoSetUtil.stringValue(getArg2().computeValue(context));
        return s1.indexOf(s2) != -1 ? Boolean.TRUE : Boolean.FALSE;
    }

    protected Object functionSubstringBefore(EvalContext context){
        assertArgCount(2);
        String s1 = InfoSetUtil.stringValue(getArg1().computeValue(context));
        String s2 = InfoSetUtil.stringValue(getArg2().computeValue(context));
        int index = s1.indexOf(s2);
        if (index == -1){
            return "";
        }
        return s1.substring(0, index);
    }

    protected Object functionSubstringAfter(EvalContext context){
        assertArgCount(2);
        String s1 = InfoSetUtil.stringValue(getArg1().computeValue(context));
        String s2 = InfoSetUtil.stringValue(getArg2().computeValue(context));
        int index = s1.indexOf(s2);
        if (index == -1){
            return "";
        }
        return s1.substring(index + s2.length());
    }

    protected Object functionSubstring(EvalContext context){
        int ac = getArgumentCount();
        if (ac != 2 && ac != 3){
            assertArgCount(2);
        }

        String s1 = InfoSetUtil.stringValue(getArg1().computeValue(context));
        double from = InfoSetUtil.doubleValue(getArg2().computeValue(context));
        if (Double.isNaN(from)){
            return "";
        }

        from = Math.round(from);
        if (ac == 2){
            if (from < 1){
                from = 1;
            }
            return s1.substring((int)from - 1);
        }
        else {
            double length = InfoSetUtil.doubleValue(getArg3().computeValue(context));
            length = Math.round(length);
            if (length < 0){
                return "";
            }

            double to = from + length;
            if (to < 1){
                return "";
            }

            if (to > s1.length() + 1){
                if (from < 1){
                    from = 1;
                }
                return s1.substring((int)from - 1);
            }

            if (from < 1){
                from = 1;
            }
            return s1.substring((int)from - 1, (int)(to - 1));
        }
    }

    protected Object functionStringLength(EvalContext context){
        String s;
        if (getArgumentCount() == 0){
            s = InfoSetUtil.stringValue(context.getCurrentNodePointer());
        }
        else {
            assertArgCount(1);
            s = InfoSetUtil.stringValue(getArg1().computeValue(context));
        }
        return new Double(s.length());
    }

    protected Object functionNormalizeSpace(EvalContext context){
        assertArgCount(1);
        String s = InfoSetUtil.stringValue(getArg1().computeValue(context));
        char chars[] = s.toCharArray();
        int out = 0;
        int phase = 0;
        for (int in = 0; in < chars.length; in++){
            switch(chars[in]){
                case 0x20:
                case 0x9:
                case 0xD:
                case 0xA:
                    if (phase == 0){      // beginning
                        ;
                    }
                    else if (phase == 1){ // non-space
                        phase = 2;
                        chars[out++] = ' ';
                    }
                    break;
                default:
                    chars[out++] = chars[in];
                    phase = 1;
            }
        }
        if (phase == 2){ // trailing-space
            out--;
        }
        return new String(chars, 0, out);
    }

    protected Object functionTranslate(EvalContext context){
        assertArgCount(3);
        String s1 = InfoSetUtil.stringValue(getArg1().computeValue(context));
        String s2 = InfoSetUtil.stringValue(getArg2().computeValue(context));
        String s3 = InfoSetUtil.stringValue(getArg3().computeValue(context));
        char chars[] = s1.toCharArray();
        int out = 0;
        for (int in = 0; in < chars.length; in++){
            char c = chars[in];
            int inx = s2.indexOf(c);
            if (inx != -1){
                if (inx < s3.length()){
                    chars[out++] = s3.charAt(inx);
                }
            }
            else {
                chars[out++] = c;
            }
        }
        return new String(chars, 0, out);
    }

    protected Object functionBoolean(EvalContext context){
        assertArgCount(1);
        return InfoSetUtil.booleanValue(getArg1().computeValue(context)) ? Boolean.TRUE : Boolean.FALSE;
    }

    protected Object functionNot(EvalContext context){
        assertArgCount(1);
        return InfoSetUtil.booleanValue(getArg1().computeValue(context)) ? Boolean.FALSE : Boolean.TRUE;
    }

    protected Object functionTrue(EvalContext context){
        assertArgCount(0);
        return Boolean.TRUE;
    }

    protected Object functionFalse(EvalContext context){
        assertArgCount(0);
        return Boolean.FALSE;
    }

    protected Object functionNull(EvalContext context){
        assertArgCount(0);
        return new NullPointer(null, context.getRootContext().getCurrentNodePointer().getLocale());
    }

    protected Object functionNumber(EvalContext context){
        if (getArgumentCount() == 0){
            return InfoSetUtil.number(context.getCurrentNodePointer());
        }
        assertArgCount(1);
        return InfoSetUtil.number(getArg1().computeValue(context));
    }

    protected Object functionSum(EvalContext context){
        assertArgCount(1);
        Object v = getArg1().compute(context);
        if (v == null){
            return ZERO;
        }
        else if (v instanceof EvalContext){
            double sum = 0.0;
            EvalContext ctx = (EvalContext)v;
            while (ctx.hasNext()){
                NodePointer ptr = (NodePointer)ctx.next();
                sum += InfoSetUtil.doubleValue(ptr);
            }
            return new Double(sum);
        }
        throw new JXPathException("Invalid argument type for 'sum': "
            + v.getClass().getName());
    }

    protected Object functionFloor(EvalContext context){
        assertArgCount(1);
        double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
        return new Double(Math.floor(v));
    }

    protected Object functionCeiling(EvalContext context){
        assertArgCount(1);
        double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
        return new Double(Math.ceil(v));
    }

    protected Object functionRound(EvalContext context){
        assertArgCount(1);
        double v = InfoSetUtil.doubleValue(getArg1().computeValue(context));
        return new Double(Math.round(v));
    }

    private void assertArgCount(int count){
        if (getArgumentCount() != count){
            throw new JXPathException("Incorrect number of argument: " + this);
        }
    }
}