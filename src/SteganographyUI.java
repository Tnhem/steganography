import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import javax.imageio.ImageIO;

public class SteganographyUI {
    private static final String ENCRYPTION_KEY = "ThisIsASecretKez"; // Should use a secure key generation mechanism
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
                String decryptedMessage = EncryptionUtils.decrypt(extractedText, ENCRYPTION_KEY);

                JFrame outputFrame = new JFrame("Extracted Text");
                JTextArea outputTextArea = new JTextArea(5, 20);
                outputTextArea.setText(decryptedMessage);
                outputTextArea.setEditable(false);
                System.out.println("Decrypted Output: " + decryptedMessage);

                JPanel outputPanel = new JPanel();
                outputPanel.add(outputTextArea);
                outputFrame.add(outputPanel, BorderLayout.CENTER);
                outputFrame.pack();
                outputFrame.setVisible(true);

            } catch (IOException ex) {
                System.out.println("Error: " + ex.getMessage());
            } catch (NoSuchPaddingException | NoSuchAlgorithmException | IllegalBlockSizeException |
                     BadPaddingException | InvalidKeyException ex) {
                throw new RuntimeException(ex);
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

                // Print the original input before encryption
                System.out.println("Original Input: " + secretMessage);

                // Encrypt the secret message
                String encryptedMessage = EncryptionUtils.encrypt(secretMessage, ENCRYPTION_KEY);

                // Hide the encrypted message in the image
                BufferedImage steganographicImage = hideText(originalImage, encryptedMessage);


                JFileChooser fileChooser = new JFileChooser();
                int returnValue = fileChooser.showSaveDialog(null);
                if (returnValue == JFileChooser.APPROVE_OPTION) {
                    File selectedFile = fileChooser.getSelectedFile();
                    ImageIO.write(steganographicImage, "png", selectedFile);
                    outputFilePath.setText(selectedFile.getAbsolutePath());
                }
            } catch (IOException | NoSuchAlgorithmException | InvalidKeyException | BadPaddingException |
                     IllegalBlockSizeException | NoSuchPaddingException ex) {
                System.out.println("Error: " + ex.getMessage());
            }
        });
        return encodeButton;
    }

    private static class EncryptionUtils {
        private static final String AES = "AES";

        public static String encrypt(String secret, String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
            try {
                Cipher cipher = Cipher.getInstance(AES);
                SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), AES);
                cipher.init(Cipher.ENCRYPT_MODE, secretKey);
                byte[] encryptedBytes = cipher.doFinal(secret.getBytes());
                return Base64.getEncoder().encodeToString(encryptedBytes);
            } catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException | BadPaddingException |
                     IllegalBlockSizeException ex) {
                throw new RuntimeException("Encryption failed: " + ex.getMessage());
            }
        }

        public static String decrypt(String encryptedSecret, String key) throws NoSuchPaddingException, NoSuchAlgorithmException, InvalidKeyException, BadPaddingException, IllegalBlockSizeException {
            try {
                Cipher cipher = Cipher.getInstance(AES);
                SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(), AES);
                cipher.init(Cipher.DECRYPT_MODE, secretKey);
                byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedSecret));
                return new String(decryptedBytes);
            } catch (BadPaddingException ex) {
                String errorMessage = "Decryption failed: " + ex.getMessage();
                displayErrorUI(errorMessage);
                throw new RuntimeException(errorMessage);
            } catch (InvalidKeyException | NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException ex) {
                throw new RuntimeException("Decryption failed: " + ex.getMessage());
            }
        }

        private static void displayErrorUI(String errorMessage) {
            JFrame errorFrame = new JFrame("Error");
            JLabel errorLabel = new JLabel(errorMessage);
            errorFrame.getContentPane().add(errorLabel, BorderLayout.CENTER);
            errorFrame.pack();
            errorFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            errorFrame.setVisible(true);
        }
    }
}


