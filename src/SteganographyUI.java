import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;

public class SteganographyUI {

    private static JTextField inputFilePath;
    private static JTextField outputFilePath;
    private static JTextArea inputText;
    private static final int MAX_MESSAGE_LENGTH = 10000; // Maximum length of the message to be hidden

    public static BufferedImage hideText(BufferedImage img, String text) {
        int width = img.getWidth();
        int height = img.getHeight();
        int charIndex = 0;

        BufferedImage encodedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);

        // Copy the original image to the encoded image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                encodedImage.setRGB(x, y, img.getRGB(x, y));
            }
        }

        // Encode the text into the image
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                if (charIndex < text.length() && charIndex < MAX_MESSAGE_LENGTH) {
                    int charValue = text.charAt(charIndex);
                    rgb = ((charValue & 0xFF) << 24) | (rgb & 0x00FFFFFF);
                    encodedImage.setRGB(x, y, rgb);
                    charIndex++;
                } else {
                    rgb = (0) | (rgb & 0x00FFFFFF); // Padding with 0 if text ends
                    encodedImage.setRGB(x, y, rgb);
                }
            }
        }

        return encodedImage;
    }

    public static String extractText(BufferedImage img) {
        int width = img.getWidth();
        int height = img.getHeight();
        StringBuilder sb = new StringBuilder();

        // Extract text from the image
        loop:
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int rgb = img.getRGB(x, y);
                int alpha = (rgb >> 24) & 0xFF;
                if (alpha != 0) { // Check if the pixel is not a padding pixel
                    sb.append((char) alpha);
                } else {
                    break loop;
                }
            }
        }
        return sb.toString();
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Steganography Application");
        JPanel panel = new JPanel();

        inputFilePath = new JTextField(20);
        outputFilePath = new JTextField(20);
        inputText = new JTextArea(5, 20);

        JButton chooseInputFile = new JButton("Choose Input File");
        chooseInputFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                inputFilePath.setText(selectedFile.getAbsolutePath());
            }
        });

        JButton encodeButton = getjButton();

        JButton chooseOutputFile = new JButton("Choose Output File");
        chooseOutputFile.addActionListener(e -> {
            JFileChooser fileChooser = new JFileChooser();
            int returnValue = fileChooser.showOpenDialog(null);
            if (returnValue == JFileChooser.APPROVE_OPTION) {
                File selectedFile = fileChooser.getSelectedFile();
                outputFilePath.setText(selectedFile.getAbsolutePath());
            }
        });

        JButton decodeButton = new JButton("Decode");
        decodeButton.addActionListener(e -> {
            try {
                BufferedImage extractedImage = ImageIO.read(new File(outputFilePath.getText()));
                String extractedText = extractText(extractedImage);

                JFrame outputFrame = new JFrame("Extracted Text");
                JTextArea outputTextArea = new JTextArea(5, 20);
                outputTextArea.setText(extractedText);
                outputTextArea.setEditable(false);

                JPanel outputPanel = new JPanel();
                outputPanel.add(outputTextArea);
                outputFrame.add(outputPanel, BorderLayout.CENTER);
                outputFrame.pack();
                outputFrame.setVisible(true);

            } catch (IOException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        });

        panel.add(inputFilePath);
        panel.add(chooseInputFile);
        panel.add(inputText);
        panel.add(encodeButton);
        panel.add(outputFilePath);
        panel.add(chooseOutputFile);
        panel.add(decodeButton);

        frame.add(panel, BorderLayout.CENTER);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private static JButton getjButton() {
        JButton encodeButton = new JButton("Encode");
        encodeButton.addActionListener(e -> {
            try {
                BufferedImage originalImage = ImageIO.read(new File(inputFilePath.getText()));
                String secretMessage = inputText.getText();
                BufferedImage steganographicImage = hideText(originalImage, secretMessage);

                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showSaveDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    ImageIO.write(steganographicImage, "png", selectedFile);
                    outputFilePath.setText(selectedFile.getAbsolutePath());
                }
            } catch (IOException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        });
        return encodeButton;
    }
}
