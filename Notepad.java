package ide;
import java.awt.*;
import java.awt.event.*;
import java.awt.font.TextAttribute;
import java.io.*;
import java.util.HashMap;
import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import ide.JavaKeyword.*;
import java.net.URISyntaxException;
import java.net.URL;
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
    JOptionPane notsaved;
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
            e.printStackTrace();
        }
        f=new Font("Arial",Font.PLAIN,20);
        icon=new ImageIcon(getClass().getResource("HSIDE.png"));
        setSize(600,400);
        setTitle("HS IDE");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
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
        viewmode.addActionListener(this);
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
        fontSelector.setLocationRelativeTo(this);
        confirmfont=new JButton("▶");
        github=new JMenuItem("git reference");
        confirmfont.addActionListener(this);
        fontcollection.addActionListener(this);
        fontcollection.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_F,2));
        fontSelector.add(fontselect,BorderLayout.NORTH);
        fontpreview.setFont(f);
        fontSelector.add(fontpreview,BorderLayout.CENTER);
        fontSelector.add(confirmfont,BorderLayout.SOUTH);
        textArea.addStyle("regular", null);
        textArea.addCaretListener(new CaretListener() { //condition for label updates
            @Override
            public void caretUpdate(CaretEvent e) {
                updatecounts();
            }
        });
        textArea.addKeyListener(new KeyAdapter() {
            StyledDocument doc = textArea.getStyledDocument();
            int length = doc.getLength();
            @Override
            public void keyTyped(KeyEvent e) {
                // Check if the typed key is a space
                if(e.getKeyChar()=='\n'){
                    // If the word count is greater than 1, call highlightSyntax
                    highlightSyntax();
                }
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
        setVisible(true);
    }
    void updatecounts(){
        word_count.setText("Word Count: "+getWordCount(textArea.getText()));
        character_count.setText("Character Count: "+getCharacterCount(textArea.getText()));
        line_count.setText("Line Count: "+getLineCount(textArea.getText()));
    }
    void highlightSyntax() { //Higlights Syntax using javaKeyword

        StyledDocument doc = textArea.getStyledDocument();
        String s = textArea.getText();
        String[] words = s.split("[(){}\\s]+");
        String prev="";
        doc.setCharacterAttributes(0, s.length(), textArea.getStyle("regular"), true);
        int currentIndex = 0;
        for (String word : words) {
            int category =jk.categorize(word);
            System.out.println(word);
            currentIndex = s.indexOf(word,currentIndex);
            if(prev!=null){
                if(prev.equals("class")){
                    jk.classlist.add(word);
                }else if(jk.categorize(prev)==JavaKeyword.PREMIVITES){
                    jk.obw.add(word);
                }
            }
            switch (category) {
                case JavaKeyword.JAVALANGUAGE:
                    {   
                        Style style = textArea.addStyle("bold", null);
                        Color color = new Color(100,100,150);
                        StyleConstants.setForeground(style, color);
                        StyleConstants.setBold(style, true);
                        doc.setCharacterAttributes(currentIndex, word.length(), style, false);
                        break;
                    }
                case JavaKeyword.OPERATORS:
                    {
                        Style style = textArea.addStyle("italic", null);
                        Color color = new Color(75,200,75);
                        StyleConstants.setForeground(style, color);
                        StyleConstants.setItalic(style, true);
                        doc.setCharacterAttributes(currentIndex, word.length(), style, false);
                        break;
                    }
                case JavaKeyword.PREMIVITES:
                    {
                        Style style = textArea.addStyle("boldItalic", null);
                        Color color = new Color(200,75,75);
                        StyleConstants.setForeground(style, color);
                        StyleConstants.setBold(style, true);
                        StyleConstants.setItalic(style, true);
                        doc.setCharacterAttributes(currentIndex, word.length(), style, false);
                        break;
                    }
                case JavaKeyword.OBJECT:
                    {
                        Style style = textArea.addStyle("bold", null);
                        Color color = new Color(150,175,75);
                        StyleConstants.setForeground(style, color);
                        StyleConstants.setBold(style, true);
                        doc.setCharacterAttributes(currentIndex, word.length(), style, false);
                        break;
                    }
                default:
                    break;
            }
            prev=word;
        }
    }
    void save(){
            fileChooser.showSaveDialog(this);
            try {
                File file = fileChooser.getSelectedFile();
                FileOutputStream fos = new FileOutputStream(file);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
                String text = textArea.getText();
                bw.write(text);
                bw.close();
                word_count.setText("Word Count: "+getWordCount(textArea.getText()));
                character_count.setText("Character Count: "+getCharacterCount(textArea.getText()));
                saved=true;
            } catch (Exception ex) {
                ex.printStackTrace();
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
                    File file=fileChooser.getSelectedFile();
                    FileInputStream fis=new FileInputStream(file);
                    BufferedReader br=new BufferedReader(new InputStreamReader(fis));
                    textArea.setText("");
                    String line;
                    while((line=br.readLine())!=null){
                        textArea.setText(textArea.getText()+line+"\n");
                    }
                    br.close();
                    updatecounts();
                    highlightSyntax();
                    saved=false;
                    jk.display();
                }
                catch(Exception ex){
                    ex.printStackTrace();
                }
                
            }
        }else if(e.getSource()==saveFile){
            save();
            
        }else if(e.getSource()==bold){
            textArea.setFont(textArea.getFont().deriveFont(Font.BOLD));
        }else if(e.getSource()==italic){
            textArea.setFont(textArea.getFont().deriveFont(Font.ITALIC));
        }else if(e.getSource()==underline){
            HashMap<TextAttribute,Integer> fontAttributes = new HashMap<TextAttribute, Integer>();
            fontAttributes.put(TextAttribute.UNDERLINE, TextAttribute.UNDERLINE_ON);
            textArea.setFont(textArea.getFont().deriveFont(fontAttributes));
        }else if(e.getSource()==confirmsize){
            textArea.setFont(new Font("Arial",Font.PLAIN,Integer.parseInt(fontSizeN.getText())));
        }else if(e.getSource()==exit){
            if(!saved && textArea.getText().length()!=0){
                int result=JOptionPane.showConfirmDialog(this,"      you have not saved, \n do you still want to close ?");
                if(result==JOptionPane.YES_OPTION)
                    System.exit(0);
                else if(result==JOptionPane.NO_OPTION){
                    save();
                }else if(result==JOptionPane.CANCEL_OPTION){
                    return;
                }
            }
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
            }else{
                viewmode.setText("☀️");
                textArea.setBackground(new Color(255,255,240));
                textArea.setForeground(Color.BLACK);
            }
        }else if(e.getSource()==github){
            try {
                Runtime.getRuntime().exec(new String[]{"cmd", "/c", "start " + "https://github.com/Ghua8088?tab=repositories"});
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            System.out.println("Redirecting to GITHUB");

            
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
