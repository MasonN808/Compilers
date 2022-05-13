/**
 * <h1>DataEntry Class</h1>
 * We use this DataEntry class to construct our Concrete Syntax Tree in the Trees Class.
 * Part of Parser (Project 2)
 * <p>
 * <b>Note:</b> Still in progress
 *
 * @author  Mason Nakamura
 * @since   03-04-2022
 */
public class DataEntry {
    public String temp = null; // Actually only need temp and offset for implementation but rest is useful for debugging and reference
    public String var = null;
    public int scope = 0;
    public int offset = 0;

    public DataEntry(String temp, String var, int scope, int offset){
        this.temp = temp;
        this.var = var;
        this.scope = scope;
        this.offset = offset;
    }
}
