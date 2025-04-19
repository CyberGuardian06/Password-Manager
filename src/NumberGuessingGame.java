import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;


public class NumberGuessingGame {
    private int targetNumber;
    private int attempts;
    private final Random random = new Random();
    private JFrame frame;
    private JTextField guessField;
    private JLabel messageLabel;
    private JButton guessButton;
    private JButton resetButton;


    public NumberGuessingGame() {
        targetNumber = random.nextInt(100) + 1;
        attempts = 0;
        createUI();
    }


    private void createUI() {
        frame = new JFrame("Number Guessing Game");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 200);
        frame.setLayout(new GridLayout(4, 1));


        JLabel instructionLabel = new JLabel("Guess a number between 1 and 100", SwingConstants.CENTER);
        frame.add(instructionLabel);


        guessField = new JTextField();
        frame.add(guessField);


        guessButton = new JButton("Guess");
        resetButton = new JButton("Reset");


        JPanel buttonPanel = new JPanel();
        buttonPanel.add(guessButton);
        buttonPanel.add(resetButton);
        frame.add(buttonPanel);


        messageLabel = new JLabel("Enter your guess and press Guess!", SwingConstants.CENTER);
        frame.add(messageLabel);


        guessButton.addActionListener(new GuessListener());
        resetButton.addActionListener(e -> resetGame());


        frame.setVisible(true);
    }


    private class GuessListener implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                int guess = Integer.parseInt(guessField.getText());
                attempts++;
                if (guess < 1 || guess > 100) {
                    messageLabel.setText("Please enter a number between 1 and 100.");
                } else if (guess < targetNumber) {
                    messageLabel.setText("Too low! Try again.");
                } else if (guess > targetNumber) {
                    messageLabel.setText("Too high! Try again.");
                } else {
                    messageLabel.setText("Correct! You guessed it in " + attempts + " attempts.");
                    guessButton.setEnabled(false);
                }
            } catch (NumberFormatException ex) {
                messageLabel.setText("Invalid input! Please enter a number.");
            }
        }
    }


    private void resetGame() {
        targetNumber = random.nextInt(100) + 1;
        attempts = 0;
        guessField.setText("");
        messageLabel.setText("Enter your guess and press Guess!");
        guessButton.setEnabled(true);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(NumberGuessingGame::new);
    }
}

