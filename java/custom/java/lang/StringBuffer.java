/*
 *   
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package java.lang;

import com.sun.cldchi.jvm.JVM;

/**
 * A string buffer implements a mutable sequence of characters. 
 * A string buffer is like a {@link String}, but can be modified. At any 
 * point in time it contains some particular sequence of characters, but 
 * the length and content of the sequence can be changed through certain 
 * method calls.
 * <p>
 * String buffers are safe for use by multiple threads. The methods 
 * are synchronized where necessary so that all the operations on any 
 * particular instance behave as if they occur in some serial order 
 * that is consistent with the order of the method calls made by each of 
 * the individual threads involved. 
 * <p>
 * String buffers are used by the compiler to implement the binary 
 * string concatenation operator <code>+</code>. For example, the code:
 * <p><blockquote><pre>
 *     x = "a" + 4 + "c"
 * </pre></blockquote><p>
 * is compiled to the equivalent of: 
 * <p><blockquote><pre>
 *     x = new StringBuffer().append("a").append(4).append("c")
 *                           .toString()
 * </pre></blockquote>
 * which creates a new string buffer (initially empty), appends the string
 * representation of each operand to the string buffer in turn, and then
 * converts the contents of the string buffer to a string. Overall, this avoids
 * creating many temporary strings.
 * <p>
 * The principal operations on a <code>StringBuffer</code> are the 
 * <code>append</code> and <code>insert</code> methods, which are 
 * overloaded so as to accept data of any type. Each effectively 
 * converts a given datum to a string and then appends or inserts the 
 * characters of that string to the string buffer. The 
 * <code>append</code> method always adds these characters at the end 
 * of the buffer; the <code>insert</code> method adds the characters at 
 * a specified point. 
 * <p>
 * For example, if <code>z</code> refers to a string buffer object 
 * whose current contents are "<code>start</code>", then 
 * the method call <code>z.append("le")</code> would cause the string 
 * buffer to contain "<code>startle</code>", whereas 
 * <code>z.insert(4, "le")</code> would alter the string buffer to 
 * contain "<code>starlet</code>". 
 * <p>
 * In general, if sb refers to an instance of a <code>StringBuffer</code>, 
 * then <code>sb.append(x)</code> has the same effect as 
 * <code>sb.insert(sb.length(),&nbsp;x)</code>.
 * <p>
 * Every string buffer has a capacity. As long as the length of the 
 * character sequence contained in the string buffer does not exceed 
 * the capacity, it is not necessary to allocate a new internal 
 * buffer array. If the internal buffer overflows, it is 
 * automatically made larger. 
 *
 * @version 12/17/01 (CLDC 1.1)
 * @see     java.io.ByteArrayOutputStream
 * @see     java.lang.String
 * @since   JDK1.0, CLDC 1.0
 */
 
public class StringBuffer {

    /**
     * The value is used for character storage.
     */
    private char value[];

    /** 
     * The count is the number of characters in the buffer.
     */
    private int count;

    /**
     * A flag indicating whether the buffer is shared 
     */
    private boolean shared;

    /**
     * Constructs a string buffer with no characters in it and an 
     * initial capacity of 16 characters. 
     */
    private native void init();
    public StringBuffer() {
        init();
    }

    /**
     * Constructs a string buffer with no characters in it and an 
     * initial capacity specified by the <code>length</code> argument. 
     *
     * @param      length   the initial capacity.
     * @exception  NegativeArraySizeException  if the <code>length</code>
     *               argument is less than <code>0</code>.
     */
    private native void init(int length);
    public StringBuffer(int length) {
        init(length);
    }

    /**
     * Constructs a string buffer so that it represents the same 
     * sequence of characters as the string argument; in other
     * words, the initial contents of the string buffer is a copy of the 
     * argument string. The initial capacity of the string buffer is 
     * <code>16</code> plus the length of the string argument. 
     *
     * @param   str   the initial contents of the buffer.
     */
    private native void init(String str);
    public StringBuffer(String str) {
        init(str);
    }

    /**
     * Returns the length (character count) of this string buffer.
     *
     * @return  the length of the sequence of characters currently 
     *          represented by this string buffer.
     */
    public native int length();

    /**
     * Returns the current capacity of the String buffer. The capacity
     * is the amount of storage available for newly inserted
     * characters; beyond which an allocation will occur.
     *
     * @return  the current capacity of this string buffer.
     */
    public native int capacity();

    /**
     * Copies the buffer value.  This is normally only called when shared
     * is true.  It should only be called from a synchronized method.
     */
    private native final void copy();

    /**
     * Ensures that the capacity of the buffer is at least equal to the
     * specified minimum.
     * If the current capacity of this string buffer is less than the 
     * argument, then a new internal buffer is allocated with greater 
     * capacity. The new capacity is the larger of: 
     * <ul>
     * <li>The <code>minimumCapacity</code> argument. 
     * <li>Twice the old capacity, plus <code>2</code>. 
     * </ul>
     * If the <code>minimumCapacity</code> argument is nonpositive, this
     * method takes no action and simply returns.
     *
     * @param   minimumCapacity   the minimum desired capacity.
     */
    public native void ensureCapacity(int minimumCapacity);

    /**
     * Sets the length of this string buffer.
     * This string buffer is altered to represent a new character sequence 
     * whose length is specified by the argument. For every nonnegative 
     * index <i>k</i> less than <code>newLength</code>, the character at 
     * index <i>k</i> in the new character sequence is the same as the 
     * character at index <i>k</i> in the old sequence if <i>k</i> is less 
     * than the length of the old character sequence; otherwise, it is the 
     * null character <code>'\u0000'</code>. 
     *  
     * In other words, if the <code>newLength</code> argument is less than 
     * the current length of the string buffer, the string buffer is 
     * truncated to contain exactly the number of characters given by the 
     * <code>newLength</code> argument. 
     * <p>
     * If the <code>newLength</code> argument is greater than or equal 
     * to the current length, sufficient null characters 
     * (<code>'&#92;u0000'</code>) are appended to the string buffer so that 
     * length becomes the <code>newLength</code> argument. 
     * <p>
     * The <code>newLength</code> argument must be greater than or equal 
     * to <code>0</code>. 
     *
     * @param      newLength   the new length of the buffer.
     * @exception  IndexOutOfBoundsException  if the
     *               <code>newLength</code> argument is negative.
     * @see        java.lang.StringBuffer#length()
     */
    public native void setLength(int newLength);

    /**
     * The specified character of the sequence currently represented by 
     * the string buffer, as indicated by the <code>index</code> argument, 
     * is returned. The first character of a string buffer is at index 
     * <code>0</code>, the next at index <code>1</code>, and so on, for 
     * array indexing. 
     * <p>
     * The index argument must be greater than or equal to 
     * <code>0</code>, and less than the length of this string buffer. 
     *
     * @param      index   the index of the desired character.
     * @return     the character at the specified index of this string buffer.
     * @exception  IndexOutOfBoundsException  if <code>index</code> is 
     *             negative or greater than or equal to <code>length()</code>.
     * @see        java.lang.StringBuffer#length()
     */
    public native char charAt(int index);

    /**
     * Characters are copied from this string buffer into the 
     * destination character array <code>dst</code>. The first character to 
     * be copied is at index <code>srcBegin</code>; the last character to 
     * be copied is at index <code>srcEnd-1</code>. The total number of 
     * characters to be copied is <code>srcEnd-srcBegin</code>. The 
     * characters are copied into the subarray of <code>dst</code> starting 
     * at index <code>dstBegin</code> and ending at index:
     * <p><blockquote><pre>
     * dstbegin + (srcEnd-srcBegin) - 1
     * </pre></blockquote>
     *
     * @param      srcBegin   start copying at this offset in the string buffer.
     * @param      srcEnd     stop copying at this offset in the string buffer.
     * @param      dst        the array to copy the data into.
     * @param      dstBegin   offset into <code>dst</code>.
     * @exception  NullPointerException if <code>dst</code> is 
     *             <code>null</code>.
     * @exception  IndexOutOfBoundsException  if any of the following is true:
     *             <ul>
     *             <li><code>srcBegin</code> is negative
     *             <li><code>dstBegin</code> is negative
     *             <li>the <code>srcBegin</code> argument is greater than 
     *             the <code>srcEnd</code> argument.
     *             <li><code>srcEnd</code> is greater than 
     *             <code>this.length()</code>, the current length of this 
     *             string buffer.
     *             <li><code>dstBegin+srcEnd-srcBegin</code> is greater than 
     *             <code>dst.length</code>
     *             </ul>
     */
    public native void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin);

    /**
     * The character at the specified index of this string buffer is set 
     * to <code>ch</code>. The string buffer is altered to represent a new 
     * character sequence that is identical to the old character sequence, 
     * except that it contains the character <code>ch</code> at position 
     * <code>index</code>. 
     * <p>
     * The offset argument must be greater than or equal to 
     * <code>0</code>, and less than the length of this string buffer. 
     *
     * @param      index   the index of the character to modify.
     * @param      ch      the new character.
     * @exception  IndexOutOfBoundsException  if <code>index</code> is 
     *             negative or greater than or equal to <code>length()</code>.
     * @see        java.lang.StringBuffer#length()
     */
    public native void setCharAt(int index, char ch);

    /**
     * Appends the string representation of the <code>Object</code> 
     * argument to this string buffer. 
     * <p>
     * The argument is converted to a string as if by the method 
     * <code>String.valueOf</code>, and the characters of that 
     * string are then appended to this string buffer. 
     *
     * @param   obj   an <code>Object</code>.
     * @return  a reference to this <code>StringBuffer</code> object.
     * @see     java.lang.String#valueOf(java.lang.Object)
     * @see     java.lang.StringBuffer#append(java.lang.String)
     */
    public StringBuffer append(Object obj) {
        return append(String.valueOf(obj));
    }

    /**
     * Appends the string to this string buffer. 
     * <p>
     * The characters of the <code>String</code> argument are appended, in 
     * order, to the contents of this string buffer, increasing the 
     * length of this string buffer by the length of the argument. 
     * If <code>str</code> is <code>null</code>, then the four characters 
     * <code>"null"</code> are appended to this string buffer.
     * <p>
     * Let <i>n</i> be the length of the old character sequence, the one 
     * contained in the string buffer just prior to execution of the 
     * <code>append</code> method. Then the character at index <i>k</i> in 
     * the new character sequence is equal to the character at index <i>k</i> 
     * in the old character sequence, if <i>k</i> is less than <i>n</i>; 
     * otherwise, it is equal to the character at index <i>k-n</i> in the 
     * argument <code>str</code>.
     *
     * @param   str   a string.
     * @return  a reference to this <code>StringBuffer</code>.
     */
      public native StringBuffer append(String str);

    /**
     * Appends the string representation of the <code>char</code> array 
     * argument to this string buffer. 
     * <p>
     * The characters of the array argument are appended, in order, to 
     * the contents of this string buffer. The length of this string 
     * buffer increases by the length of the argument. 
     * <p>
     * The overall effect is exactly as if the argument were converted to 
     * a string by the method {@link String#valueOf(char[])} and the 
     * characters of that string were then {@link #append(String) appended} 
     * to this <code>StringBuffer</code> object.
     *
     * @param   str   the characters to be appended.
     * @return  a reference to this <code>StringBuffer</code> object.
     */
    public native StringBuffer append(char str[]);

    /**
     * Appends the string representation of a subarray of the 
     * <code>char</code> array argument to this string buffer. 
     * <p>
     * Characters of the character array <code>str</code>, starting at 
     * index <code>offset</code>, are appended, in order, to the contents 
     * of this string buffer. The length of this string buffer increases 
     * by the value of <code>len</code>. 
     * <p>
     * The overall effect is exactly as if the arguments were converted to 
     * a string by the method {@link String#valueOf(char[],int,int)} and the
     * characters of that string were then {@link #append(String) appended} 
     * to this <code>StringBuffer</code> object.
     *
     * @param   str      the characters to be appended.
     * @param   offset   the index of the first character to append.
     * @param   len      the number of characters to append.
     * @return  a reference to this <code>StringBuffer</code> object.
     */
    public native StringBuffer append(char str[], int offset, int len);

    /**
     * Appends the string representation of the <code>boolean</code> 
     * argument to the string buffer. 
     * <p>
     * The argument is converted to a string as if by the method 
     * <code>String.valueOf</code>, and the characters of that 
     * string are then appended to this string buffer. 
     *
     * @param   b   a <code>boolean</code>.
     * @return  a reference to this <code>StringBuffer</code>.
     * @see     java.lang.String#valueOf(boolean)
     * @see     java.lang.StringBuffer#append(java.lang.String)
     */
    public native StringBuffer append(boolean b);

    /**
     * Appends the string representation of the <code>char</code> 
     * argument to this string buffer. 
     * <p>
     * The argument is appended to the contents of this string buffer. 
     * The length of this string buffer increases by <code>1</code>. 
     * <p>
     * The overall effect is exactly as if the argument were converted to 
     * a string by the method {@link String#valueOf(char)} and the character 
     * in that string were then {@link #append(String) appended} to this 
     * <code>StringBuffer</code> object.
     *
     * @param   c   a <code>char</code>.
     * @return  a reference to this <code>StringBuffer</code> object.
     */
    public native StringBuffer append(char c);

    /**
     * Appends the string representation of the <code>int</code> 
     * argument to this string buffer. 
     * <p>
     * The argument is converted to a string as if by the method 
     * <code>String.valueOf</code>, and the characters of that 
     * string are then appended to this string buffer. 
     *
     * @param   i   an <code>int</code>.
     * @return  a reference to this <code>StringBuffer</code> object.
     * @see     java.lang.String#valueOf(int)
     * @see     java.lang.StringBuffer#append(java.lang.String)
     */
    public native StringBuffer append(int i);

    /**
     * Appends the string representation of the <code>long</code> 
     * argument to this string buffer. 
     * <p>
     * The argument is converted to a string as if by the method 
     * <code>String.valueOf</code>, and the characters of that 
     * string are then appended to this string buffer. 
     *
     * @param   l   a <code>long</code>.
     * @return  a reference to this <code>StringBuffer</code> object.
     * @see     java.lang.String#valueOf(long)
     * @see     java.lang.StringBuffer#append(java.lang.String)
     */
    public native StringBuffer append(long l);

    /**
     * Appends the string representation of the <code>float</code>
     * argument to this string buffer.
     * <p>
     * The argument is converted to a string as if by the method
     * <code>String.valueOf</code>, and the characters of that
     * string are then appended to this string buffer.
     *
     * @param   f   a <code>float</code>.
     * @return  a reference to this <code>StringBuffer</code> object.
     * @see     java.lang.String#valueOf(float)
     * @see     java.lang.StringBuffer#append(java.lang.String)
     * @since   CLDC 1.1
     */
    public StringBuffer append(float f) {
        return append(String.valueOf(f));
    }

    /**
     * Appends the string representation of the <code>double</code>
     * argument to this string buffer.
     * <p>
     * The argument is converted to a string as if by the method
     * <code>String.valueOf</code>, and the characters of that
     * string are then appended to this string buffer.
     *
     * @param   d   a <code>double</code>.
     * @return  a reference to this <code>StringBuffer</code> object.
     * @see     java.lang.String#valueOf(double)
     * @see     java.lang.StringBuffer#append(java.lang.String)
     * @since   CLDC 1.1
     */
    public StringBuffer append(double d) {
        return append(String.valueOf(d));
    }

    /**
     * Removes the characters in a substring of this <code>StringBuffer</code>.
     * The substring begins at the specified <code>start</code> and extends to
     * the character at index <code>end - 1</code> or to the end of the
     * <code>StringBuffer</code> if no such character exists. If
     * <code>start</code> is equal to <code>end</code>, no changes are made.
     *
     * @param      start  The beginning index, inclusive.
     * @param      end    The ending index, exclusive.
     * @return     This string buffer.
     * @exception  StringIndexOutOfBoundsException  if <code>start</code>
     *             is negative, greater than <code>length()</code>, or
     *             greater than <code>end</code>.
     * @since      JDK1.2
     */
    public native StringBuffer delete(int start, int end);

    /**
     * Removes the character at the specified position in this
     * <code>StringBuffer</code> (shortening the <code>StringBuffer</code>
     * by one character).
     *
     * @param       index  Index of character to remove
     * @return      This string buffer.
     * @exception   StringIndexOutOfBoundsException  if the <code>index</code>
     *              is negative or greater than or equal to
     *              <code>length()</code>.
     * @since       JDK1.2
     */
    public native StringBuffer deleteCharAt(int index);

    /**
     * Inserts the string representation of the <code>Object</code> 
     * argument into this string buffer. 
     * <p>
     * The second argument is converted to a string as if by the method 
     * <code>String.valueOf</code>, and the characters of that 
     * string are then inserted into this string buffer at the indicated 
     * offset. 
     * <p>
     * The offset argument must be greater than or equal to 
     * <code>0</code>, and less than or equal to the length of this 
     * string buffer. 
     *
     * @param      offset   the offset.
     * @param      obj      an <code>Object</code>.
     * @return     a reference to this <code>StringBuffer</code> object.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     * @see        java.lang.String#valueOf(java.lang.Object)
     * @see        java.lang.StringBuffer#insert(int, java.lang.String)
     * @see        java.lang.StringBuffer#length()
     */
    public StringBuffer insert(int offset, Object obj) {
        return insert(offset, String.valueOf(obj));
    }

    /**
     * Inserts the string into this string buffer. 
     * <p>
     * The characters of the <code>String</code> argument are inserted, in 
     * order, into this string buffer at the indicated offset, moving up any 
     * characters originally above that position and increasing the length 
     * of this string buffer by the length of the argument. If 
     * <code>str</code> is <code>null</code>, then the four characters 
     * <code>"null"</code> are inserted into this string buffer.
     * <p>
     * The character at index <i>k</i> in the new character sequence is 
     * equal to:
     * <ul>
     * <li>the character at index <i>k</i> in the old character sequence, if 
     * <i>k</i> is less than <code>offset</code> 
     * <li>the character at index <i>k</i><code>-offset</code> in the 
     * argument <code>str</code>, if <i>k</i> is not less than 
     * <code>offset</code> but is less than <code>offset+str.length()</code> 
     * <li>the character at index <i>k</i><code>-str.length()</code> in the 
     * old character sequence, if <i>k</i> is not less than 
     * <code>offset+str.length()</code>
     * </ul><p>
     * The offset argument must be greater than or equal to 
     * <code>0</code>, and less than or equal to the length of this 
     * string buffer. 
     *
     * @param      offset   the offset.
     * @param      str      a string.
     * @return     a reference to this <code>StringBuffer</code> object.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     * @see        java.lang.StringBuffer#length()
     */
    public native StringBuffer insert(int offset, String str);

    /**
     * Inserts the string representation of the <code>char</code> array 
     * argument into this string buffer. 
     * <p>
     * The characters of the array argument are inserted into the 
     * contents of this string buffer at the position indicated by 
     * <code>offset</code>. The length of this string buffer increases by 
     * the length of the argument. 
     * <p>
     * The overall effect is exactly as if the argument were converted to 
     * a string by the method {@link String#valueOf(char[])} and the 
     * characters of that string were then 
     * {@link #insert(int,String) inserted} into this 
     * <code>StringBuffer</code>  object at the position indicated by
     * <code>offset</code>.
     *
     * @param      offset   the offset.
     * @param      str      a character array.
     * @return     a reference to this <code>StringBuffer</code> object.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     */
    public native StringBuffer insert(int offset, char str[]);

    /**
     * Inserts the string representation of the <code>boolean</code> 
     * argument into this string buffer. 
     * <p>
     * The second argument is converted to a string as if by the method 
     * <code>String.valueOf</code>, and the characters of that 
     * string are then inserted into this string buffer at the indicated 
     * offset. 
     * <p>
     * The offset argument must be greater than or equal to 
     * <code>0</code>, and less than or equal to the length of this 
     * string buffer. 
     *
     * @param      offset   the offset.
     * @param      b        a <code>boolean</code>.
     * @return     a reference to this <code>StringBuffer</code> object.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     * @see        java.lang.String#valueOf(boolean)
     * @see        java.lang.StringBuffer#insert(int, java.lang.String)
     * @see        java.lang.StringBuffer#length()
     */
    public native StringBuffer insert(int offset, boolean b);

    /**
     * Inserts the string representation of the <code>char</code> 
     * argument into this string buffer. 
     * <p>
     * The second argument is inserted into the contents of this string 
     * buffer at the position indicated by <code>offset</code>. The length 
     * of this string buffer increases by one. 
     * <p>
     * The overall effect is exactly as if the argument were converted to 
     * a string by the method {@link String#valueOf(char)} and the character 
     * in that string were then {@link #insert(int, String) inserted} into 
     * this <code>StringBuffer</code> object at the position indicated by
     * <code>offset</code>.
     * <p>
     * The offset argument must be greater than or equal to 
     * <code>0</code>, and less than or equal to the length of this 
     * string buffer. 
     *
     * @param      offset   the offset.
     * @param      c        a <code>char</code>.
     * @return     a reference to this <code>StringBuffer</code> object.
     * @exception  IndexOutOfBoundsException  if the offset is invalid.
     * @see        java.lang.StringBuffer#length()
     */
    public native StringBuffer insert(int offset, char c);

    /**
     * Inserts the string representation of the second <code>int</code> 
     * argument into this string buffer. 
     * <p>
     * The second argument is converted to a string as if by the method 
     * <code>String.valueOf</code>, and the characters of that 
     * string are then inserted into this string buffer at the indicated 
     * offset. 
     * <p>
     * The offset argument must be greater than or equal to 
     * <code>0</code>, and less than or equal to the length of this 
     * string buffer. 
     *
     * @param      offset   the offset.
     * @param      i        an <code>int</code>.
     * @return     a reference to this <code>StringBuffer</code> object.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     * @see        java.lang.String#valueOf(int)
     * @see        java.lang.StringBuffer#insert(int, java.lang.String)
     * @see        java.lang.StringBuffer#length()
     */
    public native StringBuffer insert(int offset, int i);

    /**
     * Inserts the string representation of the <code>long</code> 
     * argument into this string buffer. 
     * <p>
     * The second argument is converted to a string as if by the method 
     * <code>String.valueOf</code>, and the characters of that 
     * string are then inserted into this string buffer at the position 
     * indicated by <code>offset</code>. 
     * <p>
     * The offset argument must be greater than or equal to 
     * <code>0</code>, and less than or equal to the length of this 
     * string buffer. 
     *
     * @param      offset   the offset.
     * @param      l        a <code>long</code>.
     * @return     a reference to this <code>StringBuffer</code> object.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     * @see        java.lang.String#valueOf(long)
     * @see        java.lang.StringBuffer#insert(int, java.lang.String)
     * @see        java.lang.StringBuffer#length()
     */
    public native StringBuffer insert(int offset, long l);

    /**
     * Inserts the string representation of the <code>float</code>
     * argument into this string buffer.
     * <p>
     * The second argument is converted to a string as if by the method
     * <code>String.valueOf</code>, and the characters of that
     * string are then inserted into this string buffer at the indicated
     * offset.
     * <p>
     * The offset argument must be greater than or equal to
     * <code>0</code>, and less than or equal to the length of this
     * string buffer.
     *
     * @param      offset   the offset.
     * @param      f        a <code>float</code>.
     * @return     a reference to this <code>StringBuffer</code> object.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     * @see        java.lang.String#valueOf(float)
     * @see        java.lang.StringBuffer#insert(int, java.lang.String)
     * @see        java.lang.StringBuffer#length()
     * @since      CLDC 1.1
     */
    public StringBuffer insert(int offset, float f) {
        return insert(offset, String.valueOf(f));
    }

    /**
     * Inserts the string representation of the <code>double</code>
     * argument into this string buffer.
     * <p>
     * The second argument is converted to a string as if by the method
     * <code>String.valueOf</code>, and the characters of that
     * string are then inserted into this string buffer at the indicated
     * offset.
     * <p>
     * The offset argument must be greater than or equal to
     * <code>0</code>, and less than or equal to the length of this
     * string buffer.
     *
     * @param      offset   the offset.
     * @param      d        a <code>double</code>.
     * @return     a reference to this <code>StringBuffer</code> object.
     * @exception  StringIndexOutOfBoundsException  if the offset is invalid.
     * @see        java.lang.String#valueOf(double)
     * @see        java.lang.StringBuffer#insert(int, java.lang.String)
     * @see        java.lang.StringBuffer#length()
     * @since      CLDC 1.1
     */
    public StringBuffer insert(int offset, double d) {
        return insert(offset, String.valueOf(d));
    }

    /**
     * The character sequence contained in this string buffer is 
     * replaced by the reverse of the sequence. 
     * <p>
     * Let <i>n</i> be the length of the old character sequence, the one 
     * contained in the string buffer just prior to execution of the 
     * <code>reverse</code> method. Then the character at index <i>k</i> in 
     * the new character sequence is equal to the character at index 
     * <i>n-k-1</i> in the old character sequence.
     *
     * @return  a reference to this <code>StringBuffer</code> object..
     * @since   JDK1.0.2
     */
    public native StringBuffer reverse();

    /**
     * Converts to a string representing the data in this string buffer.
     * A new <code>String</code> object is allocated and initialized to 
     * contain the character sequence currently represented by this 
     * string buffer. This <code>String</code> is then returned. Subsequent 
     * changes to the string buffer do not affect the contents of the 
     * <code>String</code>. 
     * <p>
     * Implementation advice: This method can be coded so as to create a new
     * <code>String</code> object without allocating new memory to hold a 
     * copy of the character sequence. Instead, the string can share the 
     * memory used by the string buffer. Any subsequent operation that alters 
     * the content or capacity of the string buffer must then make a copy of 
     * the internal buffer at that time. This strategy is effective for 
     * reducing the amount of memory allocated by a string concatenation 
     * operation when it is implemented using a string buffer.
     *
     * @return  a string representation of the string buffer.
     */
    public native String toString();

    // The following two methods are needed by String to efficiently
    // convert a StringBuffer into a String.  They are not public.
    // They shouldn't be called by anyone but String.
    final native void setShared();
    final native char[] getValue();

}
