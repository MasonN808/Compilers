public class CodeGen {
    public static String[] opsArray = null; // Might make into matrix
    public static TreeST symbolTable = null;
    public static int numErrors = 0; // Keep track of errors in code gen

    public CodeGen(TreeST treeST){
        // Reset opsArray to empty string of certain length
        this.opsArray = new String[256]; // TODO: make sure 256 is the right length and not 255
        this.symbolTable = treeST;
    }

}
