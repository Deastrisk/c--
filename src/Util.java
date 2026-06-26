public class Util {

    public static boolean isDecimal(String s) {

        if (s == null) {
            return false;
        }

        if (s.isEmpty()) {
            return false;
        }

        boolean isNumber = true;
        int dotCount = 0;
        for (int i = 0; i < s.length(); ++i) {

            char ch = s.charAt(i);
            if (ch == '.') {
                dotCount++;
                if (dotCount > 1) {
                    return false;
                }
            }

            else if (ch < '0' || ch > '9') {
                return false;
            }
        }

        if (s.length() == 1 && dotCount == 1) {
            return false;
        }

        return isNumber;
    }

    public static boolean isNumeric(char ch) {
        return ch >= '0' && ch <= '9';
    }

    public static boolean isAlpha(char ch) {
        return ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z';
    }
}
