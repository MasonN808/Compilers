
public class idDetails {
    String type = null; //type = boolean, string, int
    Token token = null;
    boolean isInitialized = false;
    boolean isUsed = false;
    int depth = 0;

    public idDetails(String type, boolean isInitialized, boolean isUsed, Token token){
        this.type = type;
        this.token = token;
        this.isInitialized = isInitialized;
        this.isUsed = isUsed;
    }
}
