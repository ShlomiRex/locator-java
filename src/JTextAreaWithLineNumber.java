import java.awt.Color;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;


public class JTextAreaWithLineNumber extends JScrollPane {

    private JTextArea textArea;
    private  JTextArea lines;

    public JTextAreaWithLineNumber() {
        super();

        textArea = new JTextArea();
        lines = new JTextArea("1");

        lines.setBackground(Color.LIGHT_GRAY);
        lines.setEditable(false);

        textArea.getDocument().addDocumentListener(new DocumentListener(){
            public String getText(){
                int caretPosition = textArea.getDocument().getLength();
                Element root = textArea.getDocument().getDefaultRootElement();
                String text = "1" + System.getProperty("line.separator");
                for(int i = 2; i < root.getElementIndex( caretPosition ) + 2; i++){
                    text += i + System.getProperty("line.separator");
                }
                return text;
            }
            @Override
            public void changedUpdate(DocumentEvent de) {
                lines.setText(getText());
            }

            @Override
            public void insertUpdate(DocumentEvent de) {
                lines.setText(getText());
            }

            @Override
            public void removeUpdate(DocumentEvent de) {
                lines.setText(getText());
            }

        });

        getViewport().add(textArea);
        setRowHeaderView(lines);
        setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
    }

    private String getText(){
        int caretPosition = textArea.getDocument().getLength();
        Element root = textArea.getDocument().getDefaultRootElement();
        String text = "1" + System.getProperty("line.separator");
        for(int i = 2; i < root.getElementIndex( caretPosition ) + 2; i++){
            text += i + System.getProperty("line.separator");
        }
        return text;
    }

    public void populateTextArea(String selectedFilePath) throws IOException {
        File fileToRead = new File(selectedFilePath);
        //System.out.println("File size in MB: " + fileToRead.length() / (1024 * 1024));
        FileReader fileReader = new FileReader(fileToRead);
        textArea.read(fileReader, selectedFilePath);

        lines.setText(getText());
    }

    public JTextArea getTextArea() {
        return this.textArea;
    }
}