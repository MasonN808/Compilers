public class CodeGen {
    public static String[] opsArray = null; // Might make into matrix
    public static TreeST symbolTable = null;
    public static TreeAST ast = null;
    public static int curIndex = 0;

    public static int numErrors = 0; // Keep track of errors in code gen

    public CodeGen(TreeST symbolTable, TreeAST ast){
        // Reset opsArray to empty string of certain length
        this.opsArray = new String[256]; // TODO: make sure 256 is the right length and not 255
        this.symbolTable = symbolTable; // Might not actually need the symbol table since semantic completed successfully (prereq)
        this.ast = ast;
    }

    public static void generateOpCodes(){
        /*
            -Important Pre-requisites:
                -No redeclared errors
                -....
            -Pseudo Code:
                -Traverse through each node in the AST
                -Assign particular opt code for certain nodes
         */
    }

    public static String[][] arrayToMatrix(){
        // TODO: make this
        return null;
    }

    public static String[] replaceTemp(){
        // TODO: make this
        return null;
    }



}
