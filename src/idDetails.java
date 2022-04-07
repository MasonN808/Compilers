
public class idDetails {
    String type = null;
    Token token = null;
    boolean isInitialized = false;
    boolean isUsed = false;

    public idDetails(String type, Token token, boolean isInitialized, boolean isUsed){
        this.type = type;
        this.token = token;
        this.isInitialized = isInitialized;
        this.isUsed = isUsed;
    }
}
