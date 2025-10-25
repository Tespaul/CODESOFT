import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;
// removed org.json dependency to avoid external library requirement

public class CurrencyConverter {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        System.out.println("🌍 Welcome to the Currency Converter!");

        try {
            // Step 1: Get user inputs
            System.out.print("Enter base currency (e.g., USD, INR, EUR): ");
            String baseCurrency = sc.next().toUpperCase();

            System.out.print("Enter target currency (e.g., USD, INR, EUR): ");
            String targetCurrency = sc.next().toUpperCase();

            System.out.print("Enter amount to convert: ");
            double amount = sc.nextDouble();

            // Step 2: Build API URL
            // Use Frankfurter API which does not require an API key: https://www.frankfurter.app/
            String urlStr = "https://api.frankfurter.app/latest?from="
                            + baseCurrency + "&to=" + targetCurrency + "&amount=" + amount;

            // Step 3: Connect to API
            URL url = new URL(urlStr);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Step 4: Read API response
            BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            String inputLine;
            StringBuffer content = new StringBuffer();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();
            conn.disconnect();

            // Step 5: Parse JSON result (lightweight, no external libs)
            String response = content.toString();
            double result = Double.NaN;
            java.util.regex.Matcher m = java.util.regex.Pattern
                    .compile("\"result\"\\s*:\\s*([0-9.+\\-eE]+)")
                    .matcher(response);
            if (m.find()) {
                try {
                    result = Double.parseDouble(m.group(1));
                } catch (NumberFormatException nfe) {
                    // leave result as NaN
                }
            } else {
                // Try to extract the rate for the target currency (Frankfurter returns a rates object)
                java.util.regex.Matcher m2 = java.util.regex.Pattern
                        .compile("\"" + targetCurrency + "\"\\s*:\\s*([0-9.+\\-eE]+)")
                        .matcher(response);
                if (m2.find()) {
                    try {
                        double rate = Double.parseDouble(m2.group(1));
                        // frankfurter returns rate as the converted value when 'amount' is provided
                        result = rate;
                    } catch (NumberFormatException nfe) {
                        // leave as NaN
                    }
                } else {
                    System.out.println("Could not parse 'result' or target currency rate from API response: " + response);
                }
            }

            // Step 6: Display the result
            System.out.println("\n💰 Conversion Result 💰");
            System.out.println(amount + " " + baseCurrency + " = " + result + " " + targetCurrency);

        } catch (Exception e) {
            System.out.println("❌ Error fetching exchange rate. Please check your input or internet connection.");
            e.printStackTrace();
        }

        sc.close();
    }
}
