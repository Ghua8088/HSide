package ide;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.io.*;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
public class Notepad extends JFrame implements ActionListener{
    
    JavaKeyword jk; // Java Keywords Object for syntax highlighting
    JMenuBar menubar; 
    JScrollPane scroll;
    JMenu file,edit,help,fonts,fontStyle,fontsize;
    JMenuItem newFile,openFile,saveFile,exit,bold,italic,underline,fontcollection,github;
    JButton viewmode;
    JButton confirmsize,confirmfont;
    JTextPane textArea;
    JPanel fontSize;
    JComboBox<String> fontselect;
    JLabel fontpreview;
    String fontS[]=GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();//gets all Font Names
    JTextField fontSizeN; // Font Size Text Field
    JFileChooser fileChooser; //open , save built-in file chooser
    JDialog fontSelector; // Font Selector Dialog
    JPanel footer;
    JLabel word_count,line_count,character_count,support,font_size;
    ImageIcon icon;
    Font f;
    boolean saved;
    public Notepad(){
        saved=false;
        setFocusTraversalKeysEnabled(true);
        try {
            UIManager.setLookAndFeel("com.sun.java.swing.plaf.windows.WindowsLookAndFeel");
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
            System.out.println("Error"+e);
        }
        try {
            jk=new JavaKeyword();
        } catch (Exception e) {
            System.out.println("exception : loading javakeyword ->"+e);
        }
        f=new Font("Arial",Font.PLAIN,20);
        icon=new ImageIcon(getClass().getResource("HSIDE.png"));
        setSize(600,400);
        setTitle("HS IDE");
        setDefaultCloseOperation(JFrame.NORMAL);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeCheck();
            }
        });
        setLocationRelativeTo(null);
        setIconImage(icon.getImage());
        menubar=new JMenuBar();
        footer=new JPanel();
        line_count=new JLabel("Line Count: 0");
        word_count=new JLabel("Word Count: 0");
        character_count=new JLabel("Character Count: 0");
        support=new JLabel("UTF-16");
        footer.add(word_count,BorderLayout.WEST);
        footer.add(character_count,BorderLayout.WEST);
        footer.add(support,BorderLayout.EAST);
        footer.add(line_count,BorderLayout.EAST);
        file=new JMenu("File");
        edit=new JMenu("Edit");
        help=new JMenu("Help"); 
        viewmode=new JButton("☀️");
        viewmode.setToolTipText("Toggle Dark Mode");
        newFile=new JMenuItem("New");
        openFile=new JMenuItem("Open");
        saveFile=new JMenuItem("Save");
        exit=new JMenuItem("Exit");
        textArea=new JTextPane();
        fileChooser=new JFileChooser();
        fonts=new JMenu("Fonts");
        fontselect=new JComboBox<>(fontS);
        fontcollection=new JMenuItem("fontcollection");
        fontStyle=new JMenu("Style");
        bold=new JMenuItem("Bold");
        italic=new JMenuItem("Italic");
        underline=new JMenuItem("Underline");
        fontSize=new JPanel();
        fontSize.setLayout(new GridLayout(1,3));
        font_size=new JLabel("size:");
        fontSizeN=new JTextField("20");
        confirmsize=new JButton("▶");
        fontsize=new JMenu("size");
        fontsize.add(fontSize);
        confirmsize.setToolTipText("Confirm Font Size");
        fontpreview=new JLabel("AaBbCc 123");
        fontSelector=new JDialog(this, "Font Selector", true);
        fontSelector.setSize(300,300);

        confirmfont=new JButton("▶");
        github=new JMenuItem("git reference");

        fontcollection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,2));
        fontSelector.add(fontselect,BorderLayout.NORTH);
        fontpreview.setFont(f);
        fontSelector.add(fontpreview,BorderLayout.CENTER);
        fontSelector.add(confirmfont,BorderLayout.SOUTH);
        textArea.addStyle("regular", null);
        textArea.addCaretListener((CaretEvent e) -> {
            updatecounts();
        } //condition for label updates
        );
        textArea.addKeyListener(new KeyAdapter() {
            @Override
            public void keyTyped(KeyEvent e) {
                // Check if the typed key is a space
                if(e.getKeyChar()==' '){
                    highlightSyntax();
                }
                //shift + ctrl to go back
                if(e.isShiftDown() && e.isControlDown()){
                    textArea.setCaretPosition(textArea.getCaretPosition()-1);
                    System.out.println("shift");
                }
            }
            
        });
        fontselect.addItemListener((ItemEvent e) -> { //updates font preview
            fontpreview.setFont(new Font((String)fontselect.getSelectedItem(),Font.PLAIN,15));
        });
        fontSize.add(font_size);
        fontSize.add(fontSizeN);
        fontSize.add(confirmsize);
        textArea.setFont(f);
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        saveFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S,2));
        openFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O,2));
        newFile.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,2));
        exit.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E,2));
        menubar.add(file);
        help.add(github);
        menubar.add(edit);
        menubar.add(help);
        menubar.add(viewmode, BorderLayout.EAST);
        file.add(newFile);
        file.addSeparator();
        file.add(openFile);
        file.add(saveFile);
        file.addSeparator();
        file.add(exit);
        fontStyle.add(bold);
        fontStyle.add(italic);
        fontStyle.add(underline);
        fonts.add(fontStyle);
        fonts.add(fontcollection);
        fonts.add(fontsize);
        edit.add(fonts);
        scroll=new JScrollPane(textArea);
        add(menubar,BorderLayout.NORTH);
        add(scroll,BorderLayout.CENTER);
        add(footer,BorderLayout.SOUTH);
        viewmode.addActionListener(this);
        confirmfont.addActionListener(this);
        fontcollection.addActionListener(this);
        newFile.addActionListener(this);
        openFile.addActionListener(this);
        saveFile.addActionListener(this);
        fonts.addActionListener(this);
        exit.addActionListener(this);
        bold.addActionListener(this);
        italic.addActionListener(this);
        underline.addActionListener(this);
        confirmsize.addActionListener(this);
        github.addActionListener(this);
        fontSelector.setLocationRelativeTo(this);
        setVisible(true);
    }
    void updatecounts(){
        word_count.setText("Word Count: "+getWordCount(textArea.getText()));
        character_count.setText("Character Count: "+getCharacterCount(textArea.getText()));
        line_count.setText("Line Count: "+getLineCount(textArea.getText()));
    }
    
    void highlightSyntax() {
        StyledDocument doc = textArea.getStyledDocument();
        String text = textArea.getText();
        doc.setCharacterAttributes(0, text.length(), textArea.getStyle("regular"), true);
        Pattern pattern = Pattern.compile("[a-zA-Z_][a-zA-Z0-9_]*|[\\p{Punct}&&[^_]]+"); // Match words and punctuation (excluding '_')
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String word = matcher.group();
            int category = jk.categorize(word);
            int start = matcher.start();
            switch (category) {
                case JavaKeyword.JAVALANGUAGE -> applyStyle(doc, start, word, Color.BLUE, true, false); // Java keywords
                case JavaKeyword.OPERATORS -> applyStyle(doc, start, word, Color.GREEN, false, true); // Operators
                case JavaKeyword.PREMIVITES -> applyStyle(doc, start, word, Color.RED, true, true); // Primitives
                case JavaKeyword.OBJECT -> applyStyle(doc, start, word, Color.ORANGE, true, false); // Objects
                default -> {break;
                }
            }
        }
    }
    private void applyStyle(StyledDocument doc, int start, String word, Color color, boolean isBold, boolean isItalic) {
        Style style = textArea.addStyle("syntaxStyle", null);
        StyleConstants.setForeground(style, color);
        StyleConstants.setBold(style, isBold);
        StyleConstants.setItalic(style, isItalic);
        doc.setCharacterAttributes(start, word.length(), style, false);
    }
    void save(){
            fileChooser.showSaveDialog(this);
            try {
                File readfile = fileChooser.getSelectedFile();
                FileOutputStream fos = new FileOutputStream(readfile);
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))) {
                    String text = textArea.getText();
                    bw.write(text);
                }
                word_count.setText("Word Count: "+getWordCount(textArea.getText()));
                character_count.setText("Character Count: "+getCharacterCount(textArea.getText()));
                saved=true;
            } catch (IOException ex) {
                System.out.println("Exception loading File Loader:"+ex);
            }
    }
    void closeCheck(){
        if(!saved && textArea.getText().length()!=0){
            int result=JOptionPane.showConfirmDialog(this,"      you have not saved, \n do you still want to close ?");
            switch (result) {
                case JOptionPane.YES_OPTION:
                    System.exit(0);
                case JOptionPane.NO_OPTION:
                    save();
                    break;
                case JOptionPane.CANCEL_OPTION:
                    break;
            }
        }
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if(e.getSource()==newFile){
            textArea.setText("");
        }
        else if(e.getSource()==openFile){
            int result=fileChooser.showOpenDialog(this);
            if(result==JFileChooser.APPROVE_OPTION){
                try{
                    jk.resetClasses();
                    File readfile=fileChooser.getSelectedFile();
                    FileInputStream fis=new FileInputStream(readfile);
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(fis))) {
                        textArea.setText("");
                        String line;
                        while((line=br.readLine())!=null){
                            textArea.setText(textArea.getText()+line+"\n");
                        }
                    }
                    updatecounts();
                    highlightSyntax();
                    saved=false;
                    jk.display();
                }
                catch(IOException ex){
                    System.out.println("Error opening file:"+ex);
                }
                
            }
        }else if(e.getSource()==saveFile){
            save();
            
        }else if(e.getSource()==bold){
            textArea.setFont(textArea.getFont().deriveFont(Font.BOLD));
        }else if(e.getSource()==italic){
            textArea.setFont(textArea.getFont().deriveFont(Font.ITALIC));
        }else if(e.getSource()==underline){
            HashMap<TextAttribute,Integer> fontAttributes = new HashMap<>();
            fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            textArea.setFont(textArea.getFont().deriveFont(fontAttributes));
        }else if(e.getSource()==confirmsize){
            textArea.setFont(new Font("Arial",Font.PLAIN,Integer.parseInt(fontSizeN.getText())));
        }else if(e.getSource()==exit){
            closeCheck();
            System.exit(0);
            
        }else if(e.getSource()==fontcollection){
            fontSelector.setVisible(true);
        }else if(e.getSource()==confirmfont){
            textArea.setFont(new Font((String)fontselect.getSelectedItem(),Font.PLAIN,Integer.parseInt(fontSizeN.getText())));
            fontSelector.setVisible(false);
        }else if(e.getSource()==viewmode){
            if(viewmode.getText().equals("☀️")){
                viewmode.setText("🌙");
                textArea.setBackground(new Color(75,75,75));
                textArea.setForeground(Color.WHITE);
                textArea.setCaretColor(Color.BLACK);
            }else{
                viewmode.setText("☀️");
                textArea.setBackground(new Color(255,255,240));
                textArea.setForeground(Color.BLACK);
                textArea.setCaretColor(Color.WHITE);
            }
        }else if(e.getSource()==github){
            try {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start " + "https://github.com/Ghua8088?tab=repositories"});
            } catch (IOException e1) {
                System.out.println("exception loading URL:"+e1);
            }
            System.out.println("Redirecting to GITHUB 💕");
        }
    }
    int getWordCount(String text){
        return text.split("\\s+").length;
    }
    int getCharacterCount(String text){
        return text.length();
    }
    int getLineCount(String text){
        return text.split("\\n").length;
    }
    public static void main(String[] args) {
        SwingUtilities.invokeLater(Notepad::new);
    }    
}
