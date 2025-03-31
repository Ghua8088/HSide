package ide;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
public class JavaKeyword {
    ArrayList<String> jlw,opw,prw,classlist,obw;
    final static int JAVALANGUAGE=1;
    final static int OPERATORS=2;
    final static int PREMIVITES=3;
    final static int OBJECT=4;
    public JavaKeyword(){
        jlw=new ArrayList<>();//java language;
        opw=new ArrayList<>();//operators;
        prw=new ArrayList<>();//premitives;
        obw=new ArrayList<>();//objects;
        classlist=new ArrayList<>();
        String[] operators = {
            "+", "-", "*", "/", "%",
            "++", "--", "==", "!=", ">", ">=", "<", "<=",
            "&&", "||", "!", "&", "|", "^", "~", "<<", ">>", ">>>",
            "+=", "-=", "*=", "/=", "%=", "&=", "|=", "^=", "<<=", ">>=", ">>>="
        };
        opw.addAll(Arrays.asList(operators));
        String  premitives[]={
            "byte", "short", "int", "long", "float", "double", 
            "char", "boolean", "true", "false", "null","String","[]","System"
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
            "try", "void", "volatile", "while","new"
        };
        jlw.addAll(Arrays.asList(jl));
        
    }
    
    int categorize(String s) {
        if(jlw.contains(s)) return JAVALANGUAGE;
        if(opw.contains(s)||s.contains(".")) return OPERATORS;
        if(prw.contains(s)||classlist.contains(s)) return PREMIVITES;
        if(obw.contains(s)) return OBJECT;
        return 0;
    }
    void display(){
        System.out.println("Java Keywords:");
        System.out.println("Operators:"+opw);
        System.out.println("Premitives:"+prw);
        System.out.println("Java Language:"+jlw);
        System.out.println("Classes:"+classlist);
    }
    void resetClasses(){
        classlist.clear();
    }
    void processimport(String s){
        try {
            Runtime.getRuntime().exec(new String[]{"cmd", "D:/coding/Java/GUI/ide", "javap " +s+" > importedclasses.txt"});
            Runtime.getRuntime().exec(new String[]{"cmd", "D:/coding/Java/GUI/ide", "start"+"importedclasses.txt"});
            System.out.println("Recieved all classes from "+s);
        } catch (IOException e1) {
            System.out.println("exception loading classes:"+e1);
        }
        try {
            File f=new File(("importedclasses.txt"));
            BufferedReader br=new BufferedReader(new java.io.FileReader(f));
            String line;
            while((line=br.readLine())!=null){
                String words[]=line.split("[{}()//s+]");
                String prev="";
                for(String word:words){
                    if(categorize(prev)==PREMIVITES){
                        obw.add(word);
                    }
                }
                
            }
            System.out.println(obw);
        } catch (Exception e) {
            System.out.println("exception loading file :"+e);
        }
    }
}
