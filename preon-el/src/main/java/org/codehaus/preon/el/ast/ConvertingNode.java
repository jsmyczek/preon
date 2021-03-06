/**
 * Copyright (C) 2009-2010 Wilfred Springer
 *
 * This file is part of Preon.
 *
 * Preon is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 2, or (at your option) any later version.
 *
 * Preon is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
 * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along with
 * Preon; see the file COPYING. If not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Linking this library statically or dynamically with other modules is making a
 * combined work based on this library. Thus, the terms and conditions of the
 * GNU General Public License cover the whole combination.
 *
 * As a special exception, the copyright holders of this library give you
 * permission to link this library with independent modules to produce an
 * executable, regardless of the license terms of these independent modules, and
 * to copy and distribute the resulting executable under terms of your choice,
 * provided that you also meet, for each linked independent module, the terms
 * and conditions of the license of that module. An independent module is a
 * module which is not derived from or based on this library. If you modify this
 * library, you may extend this exception to your version of the library, but
 * you are not obligated to do so. If you do not wish to do so, delete this
 * exception statement from your version.
 */
package org.codehaus.preon.el.ast;

import java.util.HashSet;
import java.util.Set;

import org.codehaus.preon.el.Document;
import org.codehaus.preon.el.Reference;
import org.codehaus.preon.el.ReferenceContext;
import org.codehaus.preon.el.util.Converter;
import org.codehaus.preon.el.util.Converters;

/**
 * A {@link Node} with the ability to convert to other types of {@link Node}s.
 * Use the static {@link #tryConversion(Node, Class)} and
 * {@link #tryConversionToIntegerNode(Node, Class)} to convert Nodes in some
 * other type of node.
 * 
 * @author Wilfred Springer (wis)
 * 
 * @param <T>
 *            The target type of node.
 * @param <E>
 *            The type of context.
 * @param <S>
 *            The source type of node.
 */
public class ConvertingNode<T extends Comparable<T>, E, S> implements Node<T, E> {

    /**
     * The source {@link Node}.
     */
    private Node<S, E> source;

    /**
     * The {@link Converter} used to convert types from S to N.
     */
    private Converter<S, T> converter;

    /**
     * Constructs a new instance, accepting the {@link Converter} to be applied
     * and the source {@link Node}.
     * 
     * @param converter The {@link Converter}.
     * @param source The source {@link Node}.
     */
    public ConvertingNode(Converter<S, T> converter, Node<S, E> source) {
        this.source = source;
        this.converter = converter;
    }

    public int compareTo(E context, Node<T, E> other) {
        return this.eval(context).compareTo(other.eval(context));
    }

    public T eval(E context) {
        return converter.convert(source.eval(context));
    }

    public void gather(Set<Reference<E>> references) {
        source.gather(references);
    }

    public Class<T> getType() {
        return converter.getTargetType();
    }

    public Node<T, E> simplify() {
        return new ConvertingNode<T, E, S>(converter, source.simplify());
    }

    public Node<T, E> rescope(ReferenceContext<E> context) {
        return this;
    }

    public boolean isConstantFor(ReferenceContext<E> context) {
        return source.isConstantFor(context);
    }

    public Set<Reference<E>> getReferences() {
        Set<Reference<E>> references = new HashSet<Reference<E>>();
        gather(references);
        return references;
    }

    public boolean isParameterized() {
        return source.isParameterized();
    }

    public void document(Document target) {
        source.document(target);
    }

    public static <T extends Comparable<T>, E, S> Node<?, E> tryConversion(Node<S, E> source, Class<T> targetType) {
        Converter<S, T> converter = Converters.get(source.getType(), targetType);
        if (converter != null) {
            return new ConvertingNode<T, E, S>(converter, source);
        } else {
            return source;
        }
    }

    public static <T, E, S> Node<?, E> tryConversionToIntegerNode(Node<S, E> source) {
        Class<?> type = source.getType();
        if (Byte.class == type || Short.class == type || Long.class == type) {
            return tryConversion(source, Integer.class);
        } else {
            return source;
        }
    }

}
