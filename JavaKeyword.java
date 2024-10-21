package ide;
import java.util.ArrayList;
import java.util.Arrays;
public class JavaKeyword {
    ArrayList<String> jlw,opw,prw;
    public JavaKeyword(){
        jlw=new ArrayList<>();
        opw=new ArrayList<>();        
        prw=new ArrayList<>();
        String[] operators = {
            "+", "-", "*", "/", "%",
            "++", "--", "==", "!=", ">", ">=", "<", "<=",
            "&&", "||", "!", "&", "|", "^", "~", "<<", ">>", ">>>",
            "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "<<=", ">>=", ">>>=","}","{"
        };
        opw.addAll(Arrays.asList(operators));
        String  premitives[]={
            "byte", "short", "int", "long", "float", "double", 
            "char", "boolean", "true", "false", "null","String","[]"
        };
        prw.addAll(Arrays.asList(premitives));
        String[] jl={
            "abstract", "assert", "boolean", "break", "byte", "case", "catch", 
            "char", "class", "const", "continue", "default", "do", "double", 
            "else", "enum", "extends", "final", "finally", "float", "for", 
            "goto", "if", "implements", "import", "instanceof", "int", "interface", 
            "long", "native", "new", "null", "package", "private", "protected", 
            "public", "return", "short", "static", "strictfp", "super", 
            "switch", "synchronized", "this", "throw", "throws", "transient", 
            "try", "void", "volatile", "while"
        };
        jlw.addAll(Arrays.asList(jl));
    }
    
    int categorize(String s) {
        if(jlw.contains(s)) return 1;
        if(opw.contains(s)) return 2;
        if(prw.contains(s)) return 3;
        return 0;
    }
}
