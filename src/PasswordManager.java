import java.io.*;
import java.nio.file.Files;
import java.util.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.util.Base64;

public class PasswordManager {
    private static final String FILE_NAME = "credentials.txt";
    private static final String SECRET_KEY = "1234567890123456"; // 16-character AES key

    public static void main(String[] args) throws Exception {
        Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("\n==== Password Manager ====");
            System.out.println("1. Add Credential");
            System.out.println("2. View Credentials");
            System.out.println("3. Search Credentials");
            System.out.println("4. Delete Credential");
            System.out.println("5. Exit");
            System.out.print("Choose an option: ");

            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline

            switch (choice) {
                case 1 -> addCredential(scanner);
                case 2 -> viewCredentials();
                case 3 -> searchCredentials(scanner);
                case 4 -> deleteCredential(scanner);
                case 5 -> {
                    System.out.println("Exiting...");
                    return;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void addCredential(Scanner scanner) throws Exception {
        System.out.print("Enter website: ");
        String website = scanner.nextLine();

        System.out.print("Enter username: ");
        String username = scanner.nextLine();

        String password = readPassword("Enter password (hidden input): ");
        String encryptedPassword = encrypt(password);

        FileWriter fw = new FileWriter(FILE_NAME, true);
        fw.write(website + "," + username + "," + encryptedPassword + "\n");
        fw.close();

        System.out.println("✅ Credential saved!");
    }

    private static void viewCredentials() throws Exception {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("⚠️ No credentials found.");
            return;
        }

        List<String> lines = new ArrayList<>(Files.readAllLines(file.toPath()));
        if (lines.isEmpty()) {
            System.out.println("⚠️ No credentials found.");
            return;
        }

        int index = 1;
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 3) {
                System.out.println(index++ + ". Website: " + parts[0]);
                System.out.println("   Username: " + parts[1]);
                System.out.println("   Password: " + decrypt(parts[2]));
                System.out.println("-----------------------------");
            }
        }
    }

    private static void searchCredentials(Scanner scanner) throws Exception {
        System.out.print("Enter website or username to search: ");
        String keyword = scanner.nextLine().toLowerCase();

        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("⚠️ No credentials found.");
            return;
        }

        boolean found = false;
        Scanner fileScanner = new Scanner(file);
        while (fileScanner.hasNextLine()) {
            String line = fileScanner.nextLine();
            String[] parts = line.split(",");
            if (parts.length == 3 && (parts[0].toLowerCase().contains(keyword) || parts[1].toLowerCase().contains(keyword))) {
                System.out.println("Website: " + parts[0]);
                System.out.println("Username: " + parts[1]);
                System.out.println("Password: " + decrypt(parts[2]));
                System.out.println("-----------------------------");
                found = true;
            }
        }
        fileScanner.close();

        if (!found) {
            System.out.println("❌ No matches found.");
        }
    }

    private static void deleteCredential(Scanner scanner) throws Exception {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            System.out.println("⚠️ No credentials to delete.");
            return;
        }

        List<String> lines = new ArrayList<>(Files.readAllLines(file.toPath()));
        if (lines.isEmpty()) {
            System.out.println("⚠️ No credentials to delete.");
            return;
        }

        int index = 1;
        for (String line : lines) {
            String[] parts = line.split(",");
            if (parts.length == 3) {
                System.out.println(index++ + ". Website: " + parts[0]);
                System.out.println("   Username: " + parts[1]);
                System.out.println("   Password: " + decrypt(parts[2]));
                System.out.println("-----------------------------");
            }
        }

        System.out.print("Enter the number of the credential to delete: ");
        int delIndex = scanner.nextInt();
        scanner.nextLine(); // Consume newline

        if (delIndex < 1 || delIndex > lines.size()) {
            System.out.println("❌ Invalid selection.");
            return;
        }

        lines.remove(delIndex - 1);
        Files.write(file.toPath(), lines);

        System.out.println("✅ Credential deleted.");
    }

    // Secure password input
    private static String readPassword(String prompt) {
        Console console = System.console();
        if (console != null) {
            char[] passwordChars = console.readPassword(prompt);
            return new String(passwordChars);
        } else {
            // Fallback for IDEs without console support
            System.out.print(prompt);
            Scanner scanner = new Scanner(System.in);
            return scanner.nextLine();
        }
    }

    // Encrypt with AES
    private static String encrypt(String strToEncrypt) throws Exception {
        Key aesKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
        byte[] encrypted = cipher.doFinal(strToEncrypt.getBytes());
        return Base64.getEncoder().encodeToString(encrypted);
    }

    // Decrypt with AES
    private static String decrypt(String strToDecrypt) throws Exception {
        Key aesKey = new SecretKeySpec(SECRET_KEY.getBytes(), "AES");
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, aesKey);
        byte[] decrypted = cipher.doFinal(Base64.getDecoder().decode(strToDecrypt));
        return new String(decrypted);
    }
}
