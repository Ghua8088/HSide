package ide;

public class AIBridge {
    private static final AIClient instance = new AIClient(); // or lazy init
    public static AIClient getInstance() {
        return instance;
    }
}
