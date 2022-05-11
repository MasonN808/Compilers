
public class idDetails {
    String type = null; //type = boolean, string, int
    Token token = null;
    boolean isInitialized = false;
    boolean isUsed = false;
    String value = null;
    int depth = 0;

    public idDetails(String type, boolean isInitialized, boolean isUsed, Token token, String value){
        this.type = type;
        this.token = token;
        this.isInitialized = isInitialized;
        this.isUsed = isUsed;
        this.value = value;
    }
}
