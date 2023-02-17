/* calculatortamer (c) 2023, GPL2 */
// to fix compile issues with java 5+

package java.lang;


public final class StringBuilder extends StringBuffer {
    public StringBuilder(){
        super();
    }
    public StringBuilder(int length){
        super(length);
    }
    public StringBuilder(String str){
        super(str);
    }
}