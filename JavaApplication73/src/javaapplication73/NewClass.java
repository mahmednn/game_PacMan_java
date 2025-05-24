/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package javaapplication73;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.HeadlessException;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.FileWriter;
import java.net.URI;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import static javaapplication73.JavaApplication73.score;
import static javaapplication73.NewClass.diff;
import javafx.scene.layout.Border;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JProgressBar;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 *
 * @author sanad
 */
public class NewClass extends JDialog{
     public static String username,password;
     public static int diff=6;
     private JTextField usernameField;
     private JPasswordField passwordField;
     private JButton loginButton, registerButton;
     private static final String DB_PATH = "jdbc:ucanaccess://C:\\ucanaccess\\game_db.accdb";
     
      public NewClass() {
        setTitle( "LOGIN & REGISTER");
        setModal(true);
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 250);
        setLocationRelativeTo(null);
        setLayout(new GridBagLayout());
   
          addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) {
                System.exit(0);
            }            
            });
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel userLabel = new JLabel("USERNAME:");
        usernameField = new JTextField(20);

        JLabel passLabel = new JLabel("PASSWORD:");
        passwordField = new JPasswordField(20);

        loginButton = new JButton("LOGIN");
        registerButton = new JButton("REGISTER");

        gbc.gridx = 0;
        gbc.gridy = 0;
        add(userLabel, gbc);

        gbc.gridx = 1;
        add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        add(passLabel, gbc);

        gbc.gridx = 1;
        add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        add(loginButton, gbc);

        gbc.gridx = 1;
        add(registerButton, gbc);

        loginButton.addActionListener(e -> loginUser());
        registerButton.addActionListener(e -> registerUser());
        
          setVisible(true);
    }

    //USER LOGIN METHOD
    private void loginUser() {
        username = usernameField.getText();
       password = new String(passwordField.getPassword());
       if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "PLEASE FILL-IN ALL FIELDS!", "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT users.UserID, passwords.password, scores.score FROM users " +
                     "JOIN passwords ON users.UserID = passwords.UserID " +
                     "JOIN scores ON users.UserID = scores.UserID " +
                     "WHERE users.username=? AND passwords.password=?")) {

            stmt.setString(1, username);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int score = rs.getInt("score");
                this.setDefaultCloseOperation(this.HIDE_ON_CLOSE);
                JOptionPane.showMessageDialog(this, "LOGIN SUCCESSFUL! \n YOUR CURRENT SCORE IS: " + score, "WELCOME!", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "USER DOES NOT EXIST!", "ERROR", JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "AN ERROR HAS OCCURED WHILE TRYING TO CONNECT TO THE DATABASE!", "TOUBLESHOOT!", JOptionPane.ERROR_MESSAGE);
        }
    }

    //USER CREATION METHOD
    private void registerUser() {
        String username = usernameField.getText();
        String password = new String(passwordField.getPassword());

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "PLEASE FILL-IN ALL FIELDS!", "ERROR", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement checkStmt = conn.prepareStatement("SELECT * FROM users WHERE username=?")) {

            checkStmt.setString(1, username);
            ResultSet rs = checkStmt.executeQuery();
            if (rs.next()) {
                JOptionPane.showMessageDialog(this, "USER ALREADY EXISTS, PLEASE LOGIN.", "WARNING", JOptionPane.WARNING_MESSAGE);
            } else {
                try (PreparedStatement insertUserStmt = conn.prepareStatement(
                        "INSERT INTO users (username) VALUES (?)", Statement.RETURN_GENERATED_KEYS)) {
                    insertUserStmt.setString(1, username);
                    insertUserStmt.executeUpdate();

                    ResultSet generatedKeys = insertUserStmt.getGeneratedKeys();
                    if (generatedKeys.next()) {
                        int userID = generatedKeys.getInt(1);

                        //INSERTING USERs PASSWORD
                        try (PreparedStatement insertPassStmt = conn.prepareStatement(
                                "INSERT INTO passwords (UserID, password) VALUES (?, ?)")) {
                            insertPassStmt.setInt(1, userID);
                            insertPassStmt.setString(2, password);
                            insertPassStmt.executeUpdate();
                        }

                        //INSERTING USERs SCORE
                        try (PreparedStatement insertScoreStmt = conn.prepareStatement(
                                "INSERT INTO scores (UserID, score) VALUES (?, ?)")) {
                            insertScoreStmt.setInt(1, userID);
                            insertScoreStmt.setInt(2, 0);
                            insertScoreStmt.executeUpdate();
                        }

                        JOptionPane.showMessageDialog(this, "USER CREATED SUCCESSFULLY! \n YOU CAN NOW LOGIN.", "USER CREATION", JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "AN ERROR HAS OCCURED WHILE TRYING TO CONNECT TO THE DATABASE!", "TROUBLESHOOT!", JOptionPane.ERROR_MESSAGE);
        }
    }
    //SCORE UPDATING METHOD
    public static void updateScore(String username, int newScore) {
        try (Connection conn = DriverManager.getConnection(DB_PATH);
             PreparedStatement stmt = conn.prepareStatement(
                     "UPDATE scores SET score=? WHERE UserID = (SELECT UserID FROM users WHERE username=?)")) {

            stmt.setInt(1, newScore);
            stmt.setString(2, username);
            stmt.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(null, "AN ERROR OCCURED WHILE TRYING TO UPDATE YOUR SCORE!", "TROUBLESHOOT!", JOptionPane.ERROR_MESSAGE);
        }
    }
     
    public static void main(String[]args){
        
        new NewClass();
        
        //INITIALISING FRAME WITH ITS PROPERTIES
        JFrame fr = new JFrame("PAC-MAN: HUNGRIER THAN EVER!");
        fr.setLayout(null);
        fr.setSize(390, 290);
        fr.setResizable(true);
        fr.setLocationRelativeTo(null);
        fr.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        
        //INITIALISING IMAGES
        ImageIcon icon = new ImageIcon("pac1.jpg");
        ImageIcon icon1 = new ImageIcon("info.jpg");
        ImageIcon icon2 = new ImageIcon("mute.jpg");
        ImageIcon icon3 = new ImageIcon("sound.jpg");
        ImageIcon icon4 = new ImageIcon("play.jpg");
        
        //INITIALISING LABEL THAT HOLDS BACKGROUND & COMPONENTS
        JLabel l = new JLabel(icon);
        l.setBounds(0, 0, 380, 250);
        
        //INITIALISING BUTTONS WITH THEIR PROPERTIES
        JButton b1 = new JButton("START");
        b1.setBounds(70, 170, 100, 30);
        b1.setBackground(Color.white);
        b1.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 5));
        b1.setToolTipText("PAC-MAN IS WAITING FOR YOU!");
        
        JButton b2 = new JButton("SAVE");
        b2.setBounds(70, 205, 100, 30);
        b2.setBackground(Color.white);
        b2.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY, 5));
        b2.setToolTipText("SAVE YOUR PROGRESS TO PICK UP FROM WHERE YOU LEFT OFF!");
        
        JButton b3 = new JButton(icon1);
        b3.setBounds(10, 15, 21, 21);
        b3.setToolTipText("LEARN MORE ABOUT THIS GAME!");
        
        JButton b4 = new JButton(icon3);
        b4.setBounds(10, 45, 21, 21);
        b4.setToolTipText("MUTE SOUND");
        
        JButton b5 = new JButton(icon4);
        b5.setBounds(335, 15, 24, 27);
        b5.setToolTipText("WATCH TUTORIAL");
        
        //INITIALISING ComboBox WITH ITS PROPERTIES
        JComboBox cb = new JComboBox();
        cb.addItem("EASY");
        cb.addItem("MEDIUM");
        cb.addItem("HARD");
        cb.setBounds(200, 170, 140, 64);
        cb.setBackground(Color.white);
        cb.setBorder(BorderFactory.createTitledBorder("SELECT DIFFICULTY"));

        //B1 ActionListener
        b1.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                
                //CREATING A FRAME FOR THE GAME
                JFrame fr2 = new JFrame("PAC-MAN: HUNGRIER THAN EVER!");
                fr2.setSize(400, 400);
                fr2.setLocationRelativeTo(null);
                JavaApplication73 j = new JavaApplication73();
                j.init();
                j.start();
                
                //GAME FRAME WindowListener
                fr2.addWindowListener(new WindowAdapter(){
                    @Override
                    public void windowClosing(WindowEvent e) {
                        fr2.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                       int result = JOptionPane.showConfirmDialog(fr2, 
                                "ARE YOU SURE YOU WANT TO EXIT? \n ANY UNSAVED PROGRESS WILL BE LOST!", 
                                "EXIT",JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                        
                       switch(result){
                           case JOptionPane.YES_OPTION :
                               fr2.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);
                               break;
                           case JOptionPane.NO_OPTION :
                               //IF USER CHOSE NO THEN GAME FRAME IS KEPT
                              fr2.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                       }
                        
                    }

                });
                
                fr2.add(j);
                fr2.setVisible(true);
            }
            
        });
        
        //B1 MouseListener
        b1.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent e) {
                fr.setCursor(Cursor.HAND_CURSOR);
                b1.setBackground(Color.LIGHT_GRAY);
                b1.setFont(new Font("SANS",Font.BOLD,14));
            }
        @Override
            public void mouseExited(MouseEvent e) {
                fr.setCursor(Cursor.DEFAULT_CURSOR);
                b1.setBackground(Color.white);
                b1.setFont(null);
            }
            
        });
        
        //B2 MouseListener
        b2.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent e) {
                fr.setCursor(Cursor.HAND_CURSOR);
                b2.setBackground(Color.LIGHT_GRAY);
                b2.setFont(new Font("SANS",Font.BOLD,14));
            }

            @Override
            public void mouseExited(MouseEvent e) {
                fr.setCursor(Cursor.DEFAULT_CURSOR);
                b2.setBackground(Color.WHITE);
                b2.setFont(null);
            }
            
            @Override
            public void mousePressed(MouseEvent e) {
                fr.setCursor(Cursor.WAIT_CURSOR);
            }
            
        });
        
        //B2 ActionListener
        b2.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(fr, "PROGRESS SAVED SUCCESSFULLY!","SAVE", JOptionPane.INFORMATION_MESSAGE);
                updateScore(username, score);
            }
            
        });
        
        //B3 ActionListener
        b3.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                JOptionPane.showMessageDialog(fr, "Pac-Man is a classic arcade game released in 1980 by Namco, \n"
                        + " featuring a yellow, circular character navigating a maze while eating pellets  \n"
                        + " The objective is to clear the maze of all pellets  \n"
                        + " allowing Pac-Man to eat them for extra points. \n"
                        + " The game’s simple yet addictive gameplay, \n"
                        + " along with its iconic sound effects and colorful design, \n"
                        + " has made it one of the most enduring and recognizable video games of all time." , "ABOUT PAC-MAN",
                        JOptionPane.INFORMATION_MESSAGE);
            }
            
        });
        
        //B3 MouseListener
        b3.addMouseListener(new MouseAdapter(){
          
            @Override
            public void mousePressed(MouseEvent e) {
                fr.setCursor(Cursor.WAIT_CURSOR);
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                fr.setCursor(Cursor.HAND_CURSOR);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                fr.setCursor(Cursor.DEFAULT_CURSOR);
            }
            
        });
       
        //B4 ActionListener
        b4.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
               if(b4.getIcon() == icon2){
                          b4.setIcon(icon3);
                          b4.setToolTipText("MUTE SOUND");
                      } else {
                          b4.setIcon(icon2);
                          b4.setToolTipText("UNMUTE SOUND");
                      }
            }
            
        });
        
        //B4 MouseListener
        b4.addMouseListener(new MouseAdapter(){
            
            @Override
            public void mouseEntered(MouseEvent e){
                fr.setCursor(Cursor.HAND_CURSOR);
            }
            
            @Override
            public void mouseExited(MouseEvent e){
                fr.setCursor(Cursor.DEFAULT_CURSOR);
            }
        });
        
        //B5 ActionListener
        b5.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                fr.setCursor(Cursor.WAIT_CURSOR);
                try{
                  Desktop d = Desktop.getDesktop();
                  URI uri = new URI("https://www.youtube.com/watch?v=TKdqJoaIBBM");
                  d.browse(uri);
                } catch(Exception x){
                    x.getMessage();
                }
            }
            
        });
        
        //B5 MouseListener
        b5.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent e){
                fr.setCursor(Cursor.HAND_CURSOR);
            }
            
            @Override
            public void mouseExited(MouseEvent e){
                fr.setCursor(Cursor.DEFAULT_CURSOR);
            }
    });
       
        //CB MouseListener
        cb.addMouseListener(new MouseAdapter(){
            @Override
            public void mouseEntered(MouseEvent e) {
                fr.setCursor(Cursor.HAND_CURSOR);
            }
            @Override
            public void mouseExited(MouseEvent e) {
                fr.setCursor(Cursor.DEFAULT_CURSOR);
            }
            
        });
        
        //CB ActionListener
        cb.addActionListener(new ActionListener(){
            @Override
            public void actionPerformed(ActionEvent e) {
                if(cb.getSelectedItem() == "EASY"){
                    diff =10;
                } else if(cb.getSelectedItem() == "MEDIUM"){
                    diff=6;
                } else if(cb.getSelectedItem() == "HARD") {
                    diff=2;
                }
            }
            
        });
        
        //MAIN FRAME WindowListener
        fr.addWindowListener(new WindowAdapter(){
            @Override
            public void windowClosing(WindowEvent e) {
                fr.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                int choice = JOptionPane.showConfirmDialog(fr, "ARE YOU SURE YOU WANT TO EXIT?", 
                        "EXIT", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);
                
                switch(choice){
                    case JOptionPane.YES_OPTION:
                        //IF USER CHOSE TO LEAVE A DIALOG APPERS TO ASK FOR A REVIEW ON THE GAME
                        try {
                        JDialog d = new JDialog(fr,"DROP REVIEW",true);
                            d.setSize(200, 200);
                            JTextArea a = new JTextArea();
                            a.setLineWrap(true);
                            JButton sub = new JButton("SUBMIT");
                            sub.setBackground(Color.LIGHT_GRAY);
                            sub.setToolTipText("SUBMIT YOUR REVIEW!");
                            JScrollPane sp = new JScrollPane(a);
                            
                            //SUB BUTTON ActionListener THAT WRITES THE USERS GIVEN REVIEW INTO A TEXT FILE 
                            sub.addActionListener(new ActionListener(){
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        fr.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                       int op = JOptionPane.showConfirmDialog(fr, "         SEE YOU SOON!", "BYE!", JOptionPane.OK_CANCEL_OPTION ,JOptionPane.WARNING_MESSAGE);
                       
                       if(JOptionPane.OK_OPTION == op){
                          
                          fr.dispose();
                          d.dispose();
                          System.exit(0);
                       }
                       
                        try{ FileWriter w = new FileWriter("review.txt");
                        w.write(a.getText());
                        w.close(); } catch(Exception s){
                            System.out.println(s.getMessage());
                        }
                       
                       
                    }
                            });
                            
                            d.setLocationRelativeTo(null);
                            d.add(sp,BorderLayout.CENTER);
                            d.add(sub,BorderLayout.SOUTH);
                            d.setVisible(true);
                            fr.add(d);
                        } catch (Exception ex){
                        }
                        break;
                        
                    case JOptionPane.NO_OPTION:
                        //IF USER CHOSE NO THEN THE MAIN FRAME IS KEPT
                        fr.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                }
            }

        });
        
        //ADDING COMPONENTS TO LABEL
        l.add(b1);
        l.add(b2);
        l.add(b3);
        l.add(b4);
        l.add(b5);
        l.add(cb);
        
        //ADDING LABEL TO FRAME AND MAKING THE FRAME VISIBLE
        fr.add(l);
        fr.setVisible(true);
    }
}