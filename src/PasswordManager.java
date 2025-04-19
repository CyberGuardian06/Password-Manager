import java.io.*;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

public class PasswordManager {
    private static final String FILE_NAME = "credentials.txt";
    private static final String SECRET_KEY = "1234567890123456"; // 16-byte AES key

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n==== Password Manager ====");
            System.out.println("1. Add Credential");
            System.out.println("2. View Credentials");
            System.out.println("3. Delete Credential");
            System.out.println("4. Search Credentials");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // consume newline

            switch (choice) {
                case 1 -> addCredential(scanner);
                case 2 -> viewCredentials();
                case 3 -> deleteCredential(scanner);
                case 4 -> searchCredentials(scanner);
                case 5 -> {
                    System.out.println("Exiting... Goodbye!");
                    return;
                }
                default -> System.out.println("Invalid choice. Try again.");
            }
        }
    }

    private static void addCredential(Scanner scanner) throws Exception {
        System.out.print("Enter website: ");
        String website = scanner.nextLine();

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        String password = readPassword("Enter password (input hidden): ");

        String encryptedPassword = encrypt(password, SECRET_KEY);
        String data = website + "," + username + "," + encryptedPassword;

        FileWriter fw = new FileWriter(FILE_NAME, true);
        fw.write(data + "\n");
        fw.close();

        System.out.println("✅ Credential added successfully.");
    }

    private static void viewCredentials() throws Exception {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("⚠️ No credentials found.");
            return;
        }

        Scanner fileScanner = new Scanner(file);
        List<String> lines = new ArrayList<>();
        int index = 1;

        System.out.println("\nStored Credentials:");
        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();
            lines.add(line);

            String[] parts = line.split(",");
            if (parts.length == 3) {
                String decryptedPassword = decrypt(parts[2], SECRET_KEY);
                System.out.println(index + ". Website: " + parts[0]);
                System.out.println("   Username: " + parts[1]);
                System.out.println("   Password: " + decryptedPassword);
                System.out.println("---------------------------");
                index++;
            }
        }
        fileScanner.close();
    }

    private static void deleteCredential(Scanner scanner) throws Exception {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("⚠️ No credentials to delete.");
            return;
        }

        Scanner fileScanner = new Scanner(file);
        List<String> lines = new ArrayList<>();
        while (fileScanner.hasNextLine()) {
            lines.add(fileScanner.nextLine());
        }
        fileScanner.close();

        if (lines.isEmpty()) {
            System.out.println("⚠️ No credentials stored.");
            return;
        }

        // Display credentials
        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split(",");
            if (parts.length == 3) {
                String decryptedPassword = decrypt(parts[2], SECRET_KEY);
                System.out.println((i + 1) + ". Website: " + parts[0]);
                System.out.println("   Username: " + parts[1]);
                System.out.println("   Password: " + decryptedPassword);
                System.out.println("---------------------------");
            }
        }

        System.out.print("Enter the number of the credential to delete: ");
        int choice = scanner.nextInt();
        scanner.nextLine(); // consume newline

        if (choice < 1 || choice > lines.size()) {
            System.out.println("❌ Invalid selection.");
            return;
        }

        lines.remove(choice - 1);

        FileWriter fw = new FileWriter(FILE_NAME, false); // overwrite
        for (String line : lines) {
            fw.write(line + "\n");
        }
        fw.close();

        System.out.println("✅ Credential deleted successfully.");
    }

    private static void searchCredentials(Scanner scanner) throws Exception {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("⚠️ No credentials to search.");
            return;
        }

        System.out.print("Enter website or username to search: ");
        String keyword = scanner.nextLine().toLowerCase();

        Scanner fileScanner = new Scanner(file);
        int matchCount = 0;

        System.out.println("\n🔍 Search Results:");
        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();
            String[] parts = line.split(",");
            if (parts.length == 3) {
                if (parts[0].toLowerCase().contains(keyword) || parts[1].toLowerCase().contains(keyword)) {
                    String decryptedPassword = decrypt(parts[2], SECRET_KEY);
                    System.out.println("Website: " + parts[0]);
                    System.out.println("Username: " + parts[1]);
                    System.out.println("Password: " + decryptedPassword);
                    System.out.println("---------------------------");
                    matchCount++;
                }
            }
        }
        fileScanner.close();

        if (matchCount == 0) {
            System.out.println("❌ No matches found.");
        }
    }

    // Secure password input
    private static String readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] passwordChars = console.readPassword(prompt);
            return new String(passwordChars);
        } else {
            // If console is not available (e.g., in IDE), fall back
            System.out.print(prompt);
            Scanner scanner = new Scanner(System.in);
            return scanner.nextLine();
        }
    }

    // AES Encryption
    private static String encrypt(String strToEncrypt, String secret) throws Exception {
        Key aesKey = new SecretKeySpec(secret.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // AES Decryption
    private static String decrypt(String strToDecrypt, String secret) throws Exception {
        Key aesKey = new SecretKeySpec(secret.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
        return new String(decrypted);
    }
}
